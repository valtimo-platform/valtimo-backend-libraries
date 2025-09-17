---
theme: default
background: https://source.unsplash.com/1920x1080/?technology,database
class: text-center
highlighter: shiki
lineNumbers: false
info: |
  ## Optimistic Locking Exception Analysis
  Understanding and solving concurrency issues in Valtimo document management

  ### How to run this presentation:
  1. Install Slidev: npm install -g @slidev/cli
  2. Run: slidev optimistic-locking-slides.md
  3. Or use: npx slidev optimistic-locking-slides.md
drawings:
  persist: false
transition: slide-left
title: Optimistic Locking Exception Analysis
mdc: true
---

# Optimistic Locking Exception Analysis

Understanding and solving concurrency issues in Valtimo document management

<div class="pt-12">
  <span @click="$slidev.nav.next" class="px-2 py-1 rounded cursor-pointer" hover="bg-white bg-opacity-10">
    Press Space for next page <carbon:arrow-right class="inline"/>
  </span>
</div>

---

# The Problem

<div class="text-sm">

Optimistic locking in Valtimo treats all conflicts the same way, regardless of whether they represent real business conflicts or can be safely resolved automatically.

</div>

<div class="grid grid-cols-2 gap-4 mt-6">

<div>

## Current Limitations
<div class="text-sm">

- Version not exposed to frontend
- No coordination mechanism
- Forced retry patterns
- No conflict analysis

</div>
</div>

<div>

## Impact at Den Haag
<div class="text-sm">

- Multiple parallel processes
- Long-running transactions fail
- Inconsistent retry implementations
- User frustration

</div>
</div>

</div>

---

# Two Fundamental Conflict Types

<div class="grid grid-cols-2 gap-6">

<div>

## 1. Timing Conflicts ✅
<div class="text-sm">

**Auto-Resolvable**

- User A updates person info
- Background process updates status
- **No logical conflict**
- Could be safely merged
- Currently causes unnecessary failures

</div>
</div>

<div>

## 2. Business Conflicts ⚠️
<div class="text-sm">

**Requires Resolution**

- User A sets name to "John"
- User B sets name to "Jane"
- **Real business conflict**
- Requires business logic
- Currently masked by retries

</div>
</div>

</div>

---

# Root Cause Analysis

<div class="grid grid-cols-1 gap-3">

## Current Situation: Optimistic Locking Only
<div class="text-sm">

- Each `JsonSchemaDocument` has a `@Version` field
- **No pessimistic locking mechanisms exist**
- Developers forced to implement retry logic

</div>

## What Happens in Practice

<div class="text-sm">

### Long-Running Transactions (High Failure Rate)
1. Process A: `GET /document/123` → version=5
2. Process A: Complex business logic (2-5 minutes)
3. Other processes increment version to 6, 7, etc.
4. Process A: `PUT /document/123` with version=5 → **OptimisticLockingException**

</div>
</div>

---

# Core Issues

<div class="grid grid-cols-2 gap-6">

<div>

## Technical Problems
<div class="text-sm">

- No coordination mechanism
- Inconsistent retry implementations
- Risk of infinite retry loops
- Long transactions almost always fail

</div>
</div>

<div>

## Business Impact
<div class="text-sm">

- Lost user work
- Poor user experience
- Data overwrites
- Production stability issues

</div>
</div>

</div>

<div class="text-center mt-6 text-sm font-bold text-red-500">
Core Issue: Absence of pessimistic locking forces unreliable retry patterns
</div>

---

# Solution 1: Safe Retries for Delta Updates

<div class="text-sm">
For operations that work with patches instead of full document updates
</div>

<div class="grid grid-cols-2 gap-6 mt-4">

<div>

## ✅ Benefits
<div class="text-sm">

- Retries are technically safe
- Works for field-specific updates
- Good for background processes
- Minimal implementation complexity

</div>
</div>

<div>

## ⚠️ Limitations
<div class="text-sm">

- Business risk of data overwrites
- Not currently available in product
- Relies on Camunda retry mechanisms
- Doesn't solve user conflicts

</div>
</div>

</div>

```java
// Safe for delta updates
updateField("status", "APPROVED");
updateField("assignee", "user123");
```

---

# Solution 2: Atomic Read-Update Operations

<div class="text-sm font-bold">
Recommended for immediate implementation
</div>

<div class="mt-4">

```java
documentService.updateDocument(documentId, document -> {
    // Modify document here - no concurrency exceptions possible
    document.setStatus("APPROVED");
    document.setAssignee("user123");
});
```

</div>

<div class="grid grid-cols-2 gap-6 mt-4">

<div>

## ✅ Benefits
<div class="text-sm">

- Eliminates OptimisticLockingException
- Uses database FOR UPDATE locks
- Backward compatible
- Quick to implement

</div>
</div>

<div>

## ⚠️ Trade-offs
<div class="text-sm">

- Potential lock contention
- Requires usage guidelines
- Not for long-running operations
- Doesn't solve user-vs-user conflicts

</div>
</div>

</div>

---

# Solution 3: Consistency Boundaries

<div class="text-sm">
Define logical consistency units within documents
</div>

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "x-consistency-boundaries": {
    "personal": {
      "method": "merge",
      "conflictResolution": "manual"
    },
    "contact": {
      "method": "last-writer-wins"
    },
    "case-status": {
      "method": "merge",
      "conflictResolution": "business-rules"
    }
  }
}
```

<div class="text-sm font-bold text-orange-500">
Complex to implement - requires three-way merge logic and transactional complexity
</div>

---

# Solution 4: Document Splitting

<div class="text-sm">
Split large documents into smaller, focused documents
</div>

<div class="grid grid-cols-2 gap-6 mt-4">

<div>

## Concept
<div class="text-sm">

- PersonDocument
- ContactDocument
- CaseStatusDocument
- Reduces conflict scope

</div>
</div>

<div>

## Challenges
<div class="text-sm">

- Same fundamental problem remains
- Cross-document consistency
- Referential integrity issues
- API changes required

</div>
</div>

</div>

<div class="text-center mt-6 text-sm text-orange-500">
Trade-off: Fewer conflicts per document but more complexity in relationships
</div>

---

# Solution 5: Event Sourcing

<div class="text-sm">
Complete architectural redesign using command sourcing
</div>

<div class="grid grid-cols-2 gap-6 mt-4">

<div>

## Concept
<div class="text-sm">

- Commands instead of direct updates
- Centralized processing
- Consistent hashing
- Sequential processing per document

</div>
</div>

<div>

## Reality Check
<div class="text-sm">

- Eliminates conflicts entirely
- **Complete architecture change**
- **Not a serious option**
- Too disruptive for existing system

</div>
</div>

</div>

<div class="text-center mt-6 text-sm text-red-500 font-bold">
Academic interest only - demonstrates problem is solvable but at prohibitive cost
</div>

---

# Recommendation: Phased Approach

<div class="grid grid-cols-1 gap-4">

## Phase 1: Atomic Read-Update (Immediate) 🚀
<div class="text-sm">

- Implement `documentService.updateDocument(documentId, callback)`
- Target ValueResolverDelegateService for Den Haag
- Backward compatible, minimal risk

</div>

## Phase 2: Documentation & Guidelines (Concurrent) 📚
<div class="text-sm">

- When NOT to use atomic method
- Best practices for document updates
- Performance considerations

</div>

## Phase 3: User Lost-Update Issues (Next Sprint) 👥
<div class="text-sm">

- Investigate user-vs-user conflicts
- Design conflict resolution mechanisms
- Improve user experience

</div>
</div>

---

# Implementation Benefits & Limitations

<div class="grid grid-cols-2 gap-6">

<div>

## ✅ Phase 1 Benefits
<div class="text-sm">

- Solves Den Haag's immediate issues
- Foundation for future enhancements
- Minimal disruption to codebase
- Selective application to problem areas

</div>
</div>

<div>

## ⚠️ Phase 1 Limitations
<div class="text-sm">

- May cause lock contention
- Doesn't address user-vs-user conflicts
- Requires careful usage guidelines
- Not suitable for long transactions

</div>
</div>

</div>

<div class="text-center mt-6 text-sm text-blue-500 font-bold">
Prioritizes immediate relief while building toward comprehensive solutions
</div>

---

# Next Steps

<div class="grid grid-cols-1 gap-3 text-sm">

1. **Immediate**: Implement atomic read-update operations
2. **Document**: Create usage guidelines and best practices
3. **Deploy**: Target ValueResolverDelegateService for Den Haag
4. **Monitor**: Track lock contention and performance
5. **Investigate**: User conflict scenarios and solutions

</div>

<div class="text-center mt-8">
  <div class="text-lg font-bold text-green-500">
    Let's solve Den Haag's immediate problem! 🎯
  </div>
</div>

---

# Questions & Discussion

<div class="text-center text-4xl mt-16">
💬
</div>

<div class="text-center mt-6 text-sm">
Ready to discuss implementation details and next steps
</div>