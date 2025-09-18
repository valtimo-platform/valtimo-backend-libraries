# Pessimistic Locking Solution for OptimisticLockException

## Problem Summary

The `DocumentJsonValueResolverFactory#handleValues` method frequently fails with `OptimisticLockingFailureException` in production (Den Haag). This occurs when concurrent processes update the same document simultaneously, causing unnecessary failures despite no real business conflicts.

**Current failure pattern:**
1. Process A reads document (version 5)
2. Process B reads document (version 5)
3. Process A saves changes (version 5→6)
4. Process B tries to save → OptimisticLockingFailureException (expected v5, found v6)

## Solution Approach

Implement **pessimistic locking with configuration toggle** to prevent concurrent access during document updates.

**New pattern:**
1. Process A locks & reads document (version 5)
2. Process B waits for lock
3. Process A saves changes (version 5→6) & releases lock
4. Process B gets lock, reads document (version 6), saves changes (version 6→7)

## Action Plan

### Phase 1: Repository Layer Enhancement ✅ COMPLETED
- [x] Add pessimistic locking method to `JsonSchemaDocumentRepository`
- [x] Create `findByIdForUpdate(UUID id)` method with `@Lock(LockModeType.PESSIMISTIC_WRITE)`
- [x] Test lock acquisition and release behavior

### Phase 2: Service Layer Atomic Operations ✅ COMPLETED
- [x] Add atomic update method to `JsonSchemaDocumentService`
- [x] Create `updateDocumentAtomic(UUID id, Function<Document, Document> modifier)`
- [x] Ensure proper transaction boundary and error handling
- [x] Add lock timeout configuration (default: 30 seconds)

### Phase 3: Configuration System
- [ ] Create `DocumentProperties` configuration class
- [ ] Add property `valtimo.document.use-pessimistic-locking` (default: false)
- [ ] Add property `valtimo.document.lock-timeout` (default: 30000ms)
- [ ] Create Spring configuration bean

### Phase 4: ValueResolver Integration
- [ ] Update `DocumentJsonValueResolverFactory.handleValues()` methods
- [ ] Add conditional logic to use atomic updates when configured
- [ ] Maintain backward compatibility with existing optimistic approach
- [ ] Add fallback retry logic (3 attempts with exponential backoff) for optimistic mode

### Phase 5: Testing Strategy ✅ COMPLETED
- [x] **Unit tests**: Atomic update method behavior
- [x] **Integration tests**: Concurrent modification scenarios
- [x] **Performance tests**: Lock contention under load
- [x] **Regression tests**: Ensure existing functionality unchanged

### Phase 6: Documentation & Guidelines
- [ ] Document when to use pessimistic vs optimistic locking
- [ ] Performance impact analysis and monitoring recommendations
- [ ] Configuration examples for different deployment scenarios
- [ ] Migration guide for teams experiencing OptimisticLockException

## Technical Implementation Details

### Repository Method
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT d FROM JsonSchemaDocument d WHERE d.id.id = :id")
Optional<JsonSchemaDocument> findByIdForUpdate(@Param("id") UUID id);
```

### Service Method
```java
@Transactional(timeout = 30)
public JsonSchemaDocument updateDocumentAtomic(UUID documentId,
    Function<JsonSchemaDocument, JsonSchemaDocument> modifier) {

    JsonSchemaDocument document = documentRepository.findByIdForUpdate(documentId)
        .orElseThrow(() -> new DocumentNotFoundException(documentId.toString()));

    JsonSchemaDocument modifiedDocument = modifier.apply(document);
    return documentRepository.save(modifiedDocument);
}
```

### Configuration Properties
```properties
# Enable pessimistic locking for document updates (default: false)
valtimo.document.use-pessimistic-locking=true

# Lock timeout in milliseconds (default: 30000)
valtimo.document.lock-timeout=30000
```

## Rollout Strategy

### Step 1: Development & Testing
- Implement all phases in feature branch
- Comprehensive testing with concurrent load simulation
- Performance benchmarking

### Step 2: Den Haag Pilot
- Deploy with pessimistic locking enabled for Den Haag environment
- Monitor OptimisticLockException reduction
- Measure lock wait times and contention

### Step 3: Gradual Rollout
- Other teams can opt-in via configuration
- Document lessons learned and best practices
- Consider making default behavior in next major release

## Success Criteria

- [ ] Elimination of OptimisticLockException in ValueResolver operations
- [ ] No performance degradation for single-user scenarios
- [ ] Lock wait times under high concurrency remain under 5 seconds
- [ ] Zero breaking changes for existing deployments
- [ ] Comprehensive test coverage (>90% for new code)

## Risk Mitigation

- **Lock contention**: Monitor wait times, provide configuration guidance
- **Deadlocks**: Consistent lock ordering, timeout mechanisms
- **Performance impact**: Benchmarking, gradual rollout
- **Backward compatibility**: Feature flag ensures existing behavior preserved

## Timeline

- **Week 1**: Phases 1-3 (Repository, Service, Configuration)
- **Week 2**: Phase 4 (ValueResolver Integration)
- **Week 3**: Phase 5 (Testing)
- **Week 4**: Phase 6 (Documentation) + Den Haag pilot deployment

---

This solution addresses the immediate production issues while building a foundation for better concurrency management across the Valtimo platform.