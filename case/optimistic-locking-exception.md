# Optimistic Locking Exception Analysis

## Summary

**Problem**: Den Haag is experiencing frequent OptimisticLockingException failures in production when ValueResolverService updates documents. These are primarily timing conflicts where concurrent processes update different parts of the same document, causing unnecessary failures despite no real business conflicts.

**Recommended Solution**: Implement **Solution 2 - Atomic Read-Update Operations** with configuration-based opt-in. This uses pessimistic locking (FOR UPDATE) to eliminate timing-based OptimisticLockingException while maintaining full backward compatibility.

**Key Benefits**:
- Zero breaking changes - existing deployments continue working unchanged
- Simple configuration - Den Haag can enable atomic updates via configuration property
- Eliminates OptimisticLockingException for short transactions in ValueResolver
- Gradual adoption - other teams can opt-in when ready
- May become the default behavior in a future major Valtimo release

---

## Problem

Optimistic locking is implemented in Valtimo to prevent lost updates when multiple processes modify the same document simultaneously. However, the current implementation has significant limitations and treats all conflicts the same way, regardless of whether they represent real business conflicts or can be safely resolved automatically.

### Version Not Passed to Frontend
The document version is not exposed to the frontend, which limits the effectiveness of optimistic locking. The protection only works at the backend service level.

### Two Fundamental Conflict Types

When an OptimisticLockingException occurs, it represents one of two fundamentally different situations:

#### 1. Timing Conflicts (Auto-Resolvable)
Multiple processes happen to update the same document simultaneously, but the changes don't actually conflict:

**Examples:**
- **User A** updates person information while **Background Process** updates document status
- **Process A** assigns a case while **Process B** adds a comment
- **User A** modifies company data while **User B** modifies person data
- Two processes update different workflow variables

**Characteristics:**
- Changes affect different fields or document sections
- No logical business conflict exists
- Could be safely merged or retried
- Currently causes unnecessary failures

#### 2. Legitimate Business Conflicts (Requires Resolution)
Multiple processes attempt to modify the same business data in conflicting ways:

**Examples:**
- **User A** and **User B** both change the same person's name to different values
- **Process A** assigns case to User X while **Process B** assigns to User Y
- **User A** sets status to "Approved" while **Background Process** sets to "Rejected"
- Two processes update the same workflow variable with different values

**Characteristics:**
- Changes affect the same fields with different values
- Represents real business decision conflict
- Requires business logic to resolve (merge rules, user choice, etc.)
- Currently masked by retry mechanisms or causes data loss

### Root Cause Analysis

**Current Situation - Optimistic Locking Only:**
- Each `JsonSchemaDocument` has a `@Version` field managed by JPA/Hibernate
- **No pessimistic locking mechanisms exist** in the current system
- When concurrent updates occur, the second one fails with `OptimisticLockingFailureException`
- **Developers are forced to implement retry logic** to handle these exceptions

**What Actually Happens in Practice:**

**Scenario 1: Long-Running Transactions (High Failure Rate)**
1. **Process A**: `GET /document/123` → receives document with version=5
2. **Process A**: Performs complex business logic (takes 2-5 minutes)
3. **During this time**: Any other process that updates the document increments version to 6, 7, etc.
4. **Process A**: `PUT /document/123` with version=5 → **OptimisticLockingFailureException** (version is now 7)
5. **Result**: Long-running transactions have very high failure rates

**Scenario 2: Concurrent Short Operations**
1. **Process A**: `GET /document/123` → receives document with version=5
2. **Process B**: `GET /document/123` → receives document with version=5
3. **Process A**: `PUT /document/123` with version=5 → succeeds, version becomes 6
4. **Process B**: `PUT /document/123` with version=5 → **OptimisticLockingFailureException**

**The Real Problems:**

1. **No Coordination Mechanism**: Without pessimistic locking, there's no way to coordinate access during long-running operations

2. **Forced Retry Pattern**: Developers must implement retry logic for `OptimisticLockingFailureException`, leading to:
   - Inconsistent retry implementations across the codebase
   - Risk of infinite retry loops
   - Difficulty distinguishing between retriable and non-retriable conflicts

3. **Long Transactions Almost Always Fail**: The longer a transaction takes, the higher the probability another process will modify the document in the meantime

4. **No Conflict Analysis**: The system cannot distinguish between:
   - Timing conflicts (different fields modified, could be safely merged)
   - Real business conflicts (same fields modified with different values)

**Core Issue**: The absence of pessimistic locking forces all coordination to happen through optimistic failures and developer-implemented retries, which is unreliable for long-running operations and creates inconsistent error handling patterns.

## Potential Solutions

### 1. Safe Retries for Delta/Patch Updates
For operations that work with patches or deltas instead of full document updates, implement automatic retry mechanisms:

- **Technical feasibility**: Retries are safe since we know exactly which fields are being modified
- **Business risk**: May still result in data overwrites if business logic conflicts exist
- **Implementation gap**: Currently not available in the product; developers rely on Camunda retry mechanisms
- **Scope**: Works well for field-specific updates and background processes

### 2. Atomic Read-Update Operations
Introduce a method to atomically read and update documents in a single transaction:

```java
documentService.updateDocument(documentId, document -> {
    // Modify document here - no concurrency exceptions possible
});
```

- **Mechanism**: Uses database-level write locks (FOR UPDATE) to prevent concurrent access
- **Benefit**: Eliminates OptimisticLockingException for short transactions
- **Den Haag solution**: Can be implemented in ValueResolverDelegateService to solve their immediate issue
- **Trade-off**: May lead to lock contention under high concurrent load, but indicates need for implementation review

### 3. Consistency Boundaries
Define logical consistency units within documents through schema-based boundaries:

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "x-consistency-boundaries": {
    "personal": {
      "method": "merge",
      "conflictResolution": "manual",
      "description": "Personal information requiring consistency"
    },
    "contact": {
      "method": "last-writer-wins",
      "description": "Contact data can be overwritten"
    },
    "case-status": {
      "method": "merge",
      "conflictResolution": "business-rules",
      "description": "Case status and assignment"
    }
  },
  "properties": {
    "name": {
      "type": "string",
      "x-consistency-boundary": "personal"
    },
    "bsn": {
      "type": "string",
      "x-consistency-boundary": "personal"
    },
    "email": {
      "type": "string",
      "x-consistency-boundary": "contact"
    },
    "status": {
      "type": "string",
      "x-consistency-boundary": "case-status"
    },
    "assignee": {
      "type": "string",
      "x-consistency-boundary": "case-status"
    }
  }
}
```

- **Concept**: Mark which properties belong together and cannot be updated concurrently
- **Business value**: Split single documents into multiple logical consistent units based on business rules
- **Example**: Personal info updates can happen concurrently with status updates, but name and BSN must be updated together
- **Implementation challenge**: Complex to implement since documents still need single database write
- **Three-way merge requirement**: Even with boundaries, still need to implement merge logic:
  - Compare base version (when editing started) with current version (in database)
  - Identify which boundaries were modified in each version
  - Auto-merge non-conflicting boundaries using boundary-specific strategies
  - Flag conflicting boundaries for manual resolution or business rule application
  - Reconstruct final document from merged boundaries
- **Transactional complexity**: Rollback becomes problematic with merged documents:
  - If merge fails partway through, need to rollback the entire document to original state
  - Cannot rollback individual boundaries since document is stored as single entity
  - Partial merge failures require complex compensation logic
  - Database transaction scope must encompass entire merge process
- **Technical approaches**: JSON patch at database level, document splitting, or hybrid coordination mechanisms
- **Dependency**: Still requires one of the other solutions as foundation

### 4. Document Splitting
Split large documents into multiple smaller documents based on logical boundaries:

- **Concept**: Break single document into separate entities (e.g., PersonDocument, ContactDocument, CaseStatusDocument)
- **Benefit**: Reduces scope of conflicts since updates target smaller, more focused documents
- **Same fundamental problem**: OptimisticLockingException still occurs, just at smaller scale
- **New challenges**:
  - Cross-document consistency becomes complex
  - Referential integrity between split documents
  - Potential for distributed transaction issues
  - API changes required for consumers
- **Trade-off**: Fewer conflicts per document but more complexity in maintaining relationships

### 5. Event Sourcing with Centralized Command Processing
Completely revise the document update model to use command sourcing with centralized processing:

- **Concept**: Instead of updating documents directly, source commands and process them centrally using consistent hashing
- **Benefit**: Eliminates concurrency conflicts entirely since commands are processed sequentially per document
- **Implementation**: Commands queued and processed by single handler per document (based on document ID hash)
- **Complete architecture change**: Would require fundamental revamp of how Valtimo works
- **Not a serious option**: Too disruptive for existing system and would break backward compatibility
- **Academic interest only**: Demonstrates that the problem can be solved but at prohibitive cost

## Recommendation

Given the urgent production issues at Den Haag and the need for a practical solution, we recommend a phased approach starting with the most immediately implementable option:

### Phase 1: Implement Atomic Read-Update Operations (Solution 2)
**Priority: Immediate - Address Den Haag's urgent issue**

- **Implementation**: Add `documentService.updateDocument(documentId, callback)` method using FOR UPDATE locking
- **Backward compatibility**: Implement as additional method alongside existing APIs
- **Target integration**: Specifically implement in ValueResolverDelegateService to solve Den Haag's immediate problem
- **Timeline**: Can be implemented and deployed quickly with minimal risk

### Phase 2: Documentation and Guidelines
**Priority: Concurrent with Phase 1**

- **Long-running transaction guidance**: Document when NOT to use the new atomic method to avoid blocking
- **Best practices documentation**: Provide guidelines on effective document update patterns
- **Usage examples**: Show proper implementation patterns and common anti-patterns
- **Performance considerations**: Document potential lock contention scenarios and mitigation strategies

### Phase 3: Address User Lost-Update Issues
**Optional: After Phase 1 deployment**

- **Problem identification**: Two concurrent users working on the same document are likely to overwrite each other's work
- **User experience impact**: Current system provides no indication of concurrent editing or conflict resolution
- **Investigation required**: Analyze user workflows and identify where lost updates commonly occur
- **Solution design**: Consider user-facing conflict detection and resolution mechanisms

### Implementation Notes

**Phase 1 Benefits:**
- Solves Den Haag's immediate OptimisticLockingException issues
- Provides foundation for future enhancements
- Minimal disruption to existing codebase
- Can be selectively applied to problem areas

**Phase 1 Limitations:**
- May cause lock contention under high concurrent load
- Does not address user-vs-user conflicts
- Requires careful usage guidelines to avoid blocking

This approach prioritizes immediate relief while building toward more comprehensive solutions for user experience and conflict management.

