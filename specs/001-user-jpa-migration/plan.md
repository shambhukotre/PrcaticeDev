# Implementation Plan: User Service Database Migration

**Branch**: `user-jpa-migration` | **Date**: April 10, 2026 | **Spec**: `specs/001-user-jpa-migration/spec.md`
**Input**: Feature specification from `/specs/001-user-jpa-migration/spec.md`

**Note**: This plan details the migration of the User service from in-memory ArrayList to H2 database with Spring Data JPA, maintaining backward compatibility with all existing API endpoints.

## Summary

Migrate the SmartCollab User service from transient in-memory ArrayList storage to persistent H2 database with Spring Data JPA. This enables data persistence across application restarts, provides scalable search operations, and enforces data validation constraints. The migration maintains 100% API backward compatibility—all existing endpoints and client code require no changes. Implementation follows Spring Boot best practices with 8 sequential phases, including dependency configuration, entity mapping, repository pattern, service layer refactoring, validation, error handling, testing, and deployment preparation.

## Technical Context

**Language/Version**: Java 17 with Spring Boot 4.0.4  
**Primary Dependencies**: Spring Boot Starter Web, Spring Data JPA, H2 Database, Jakarta Bean Validation  
**Storage**: H2 in-file database with auto-incremented Long IDs  
**Testing**: Spring Boot Test, Mockito for unit tests, integration tests for repository layer  
**Target Platform**: Linux/Windows server, deployed as standalone JAR  
**Project Type**: Web service (REST API backend) - microservice architecture  
**Performance Goals**: Search operations under 100ms for datasets up to 10,000 users  
**Constraints**: <200ms p95 response time for CRUD operations, <100MB memory footprint  
**Scale/Scope**: Initial deployment targets under 100,000 user records; 16 seed users in current implementation

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

**Status**: No constitution guidelines document provided. Template found at `.specify/memory/constitution.md` but contains only structure placeholders. Proceeding with standard Spring Boot best practices:
- ✅ Spring Boot standard conventions applied
- ✅ Test-driven approach (existing unit tests in place)
- ✅ Backward compatibility maintained (zero breaking changes)
- ✅ Repository pattern enforced for data access
- ✅ Validation layer implemented at entity level

## Project Structure

### Documentation (this feature)

```text
specs/001-user-jpa-migration/
├── spec.md              # Feature specification (existing)
├── plan.md              # This file - implementation plan
├── research.md          # Phase 0 - Research findings and decisions (to be generated)
├── data-model.md        # Phase 1 - Entity design and database schema (to be generated)
├── quickstart.md        # Phase 1 - Quick start guide for developers (to be generated)
├── contracts/           # Phase 1 - API contracts (to be generated)
│   ├── user-api.md      # User REST API contract
│   └── user-repository.md # UserRepository interface contract
└── requirements.md      # Checklist with acceptance criteria
```

### Source Code Structure (Backend)

**Current Structure**:
```text
Backend/Backend/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/back/Backend/
│   │   │   ├── BackendApplication.java
│   │   │   ├── Controller/
│   │   │   │   └── UserController.java
│   │   │   ├── Model/
│   │   │   │   └── User.java (currently POJO, will add @Entity)
│   │   │   ├── Service/
│   │   │   │   └── UserService.java (currently uses ArrayList, will use Repository)
│   │   │   ├── Repository/
│   │   │   │   └── UserRepository.java (to be created - extends JpaRepository)
│   │   │   ├── Exception/
│   │   │   │   └── UserNotFoundException.java (to be created)
│   │   │   ├── DTO/
│   │   │   │   └── UserValidationErrorResponse.java (to be created)
│   │   │   └── Config/
│   │   │       └── ValidationConfig.java (to be created if needed)
│   │   └── resources/
│   │       ├── application.properties (will add database configuration)
│   │       └── data.sql (to be created - seed data migration)
│   └── test/
│       ├── java/com/back/Backend/Service/
│       │   └── UserServiceTest.java (existing, to be updated)
│       └── java/com/back/Backend/Repository/
│           └── UserRepositoryTest.java (to be created - integration tests)
└── target/                # Build artifacts
```

**Structure Decision**: Extend existing Spring Boot Maven multi-module structure. No new directories required beyond adding Repository layer, Exception handling, and DTO packages. Keep existing Controller/Service/Model organization. Add database configuration to application.properties. Add seed data migration script.

## Complexity Tracking

No Constitution violations identified. Implementation uses standard Spring Boot patterns:
- Repository pattern enforces clean data access layer separation
- Validation annotations provide declarative constraint definition
- Exception handling centralizes error responses
- DTOs separate internal domain models from API contracts

## Implementation Phases

### Phase 0: Setup & Dependencies

**Objective**: Configure project dependencies and database connection settings

**Tasks**:

1. **Add Maven Dependencies**
   - Add `spring-boot-starter-data-jpa` to pom.xml
   - Add `com.h2database:h2` database driver
   - Add `org.springframework.boot:spring-boot-starter-validation` for Jakarta Bean Validation
   - Verify versions align with Spring Boot 4.0.4 parent
   - **File**: `Backend/Backend/pom.xml`

2. **Configure H2 Database**
   - Create H2 database file: `data/smartcollab.db`
   - Add database connection properties to `application.properties`:
     ```properties
     spring.datasource.url=jdbc:h2:file:./data/smartcollab
     spring.datasource.driverClassName=org.h2.Driver
     spring.datasource.username=sa
     spring.datasource.password=
     spring.h2.console.enabled=true
     spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
     spring.jpa.hibernate.ddl-auto=update
     spring.jpa.show-sql=false
     spring.jpa.properties.hibernate.format_sql=true
     ```
   - **File**: `Backend/Backend/src/main/resources/application.properties`

3. **Remove DataSource Auto-Configuration Exclusion** (if needed)
   - Current application.properties excludes DataSourceAutoConfiguration
   - Remove or verify compatibility when JPA is enabled
   - **File**: `Backend/Backend/src/main/resources/application.properties`

**Success Criteria**:
- Maven build completes without dependency conflicts
- Application starts without errors
- H2 Console accessible at `http://localhost:8080/h2-console`

---

### Phase 1: Entity Mapping - Add JPA Annotations to User

**Objective**: Transform User POJO into JPA-managed entity with proper mappings and validation

**Tasks**:

1. **Update User.java with JPA Annotations**
   - Add `@Entity` class-level annotation
   - Add `@Table(name = "user")` for explicit table mapping
   - Add `@Id` annotation to `id` field
   - Add `@GeneratedValue(strategy = GenerationType.AUTO)` for auto-increment
   - Add `@Column(nullable = false)` constraints
   - Add validation annotations:
     - `@NotBlank(message = "Name is required")` on name
     - `@NotBlank(message = "Email is required")` on email
     - `@Email(message = "Email should be valid")` on email
   - Import: `jakarta.persistence.*`, `jakarta.validation.constraints.*`
   - **File**: `Backend/Backend/src/main/java/com/back/Backend/Model/User.java`

2. **Add equals() and hashCode() Methods** (recommended for entities)
   - Required for proper entity identity management
   - Use `@EqualsAndHashCode` from Lombok or implement manually
   - Base on `id` field only

3. **Verify Constructor Compatibility**
   - No-arg constructor required by JPA (already present)
   - Full constructor should work with new fields (existing)

**Dependencies Added**:
- `jakarta.persistence` (provided by spring-boot-starter-data-jpa)
- `jakarta.validation.constraints` (provided by spring-boot-starter-validation)

**Success Criteria**:
- User class compiles with JPA annotations
- Entity is recognized by Spring Data JPA
- No serialization issues with validation annotations

---

### Phase 2: Create UserRepository Interface

**Objective**: Define repository interface extending JpaRepository with custom query methods

**Tasks**:

1. **Create Repository Package** (if not exists)
   - Create directory: `Backend/Backend/src/main/java/com/back/Backend/Repository/`

2. **Create UserRepository.java Interface**
   - Extend `JpaRepository<User, Long>` (User entity, Long ID type)
   - Define query methods:
     ```java
     @Repository
     public interface UserRepository extends JpaRepository<User, Long> {
         List<User> findByNameContainingIgnoreCase(String name);
         List<User> findByEmail(String email);
         List<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);
     }
     ```
   - Annotations:
     - `@Repository` marks as Spring Data repository
     - Optional `@Query` for complex queries if needed later
   - **File**: `Backend/Backend/src/main/java/com/back/Backend/Repository/UserRepository.java`

3. **Import Statements**
   - `org.springframework.data.jpa.repository.JpaRepository`
   - `org.springframework.stereotype.Repository`
   - `com.back.Backend.Model.User`

**Spring Data JPA Query Methods**:
- `findByNameContainingIgnoreCase`: Partial name match (case-insensitive)
- `findByEmail`: Exact email match
- `findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase`: Combined search

**Success Criteria**:
- Repository interface compiles
- Proxy implementation generated automatically by Spring
- CRUD methods inherited from JpaRepository (save, findById, findAll, delete, etc.)

---

### Phase 3: Refactor UserService - Replace ArrayList with Repository

**Objective**: Replace in-memory ArrayList with injected UserRepository while maintaining existing API

**Tasks**:

1. **Replace ArrayList with UserRepository Injection**
   - Remove: `private List<User> users = new ArrayList<>();`
   - Add: `@Autowired private UserRepository userRepository;`
   - Remove: Constructor initialization code (ArrayList population)
   - **File**: `Backend/Backend/src/main/java/com/back/Backend/Service/UserService.java`

2. **Update getAllUsers() Method**
   ```java
   public List<User> getAllUsers() {
       return userRepository.findAll();
   }
   ```

3. **Update getUserById() Method**
   ```java
   public User getUserById(Long id) {
       return userRepository.findById(id).orElse(null);
   }
   ```

4. **Update addUser() Method**
   ```java
   public User addUser(User user) {
       return userRepository.save(user);
   }
   ```
   - Note: ID auto-generation now handled by database

5. **Update updateUser() Method**
   ```java
   public User updateUser(Long id, User user) {
       Optional<User> existingUser = userRepository.findById(id);
       if (existingUser.isPresent()) {
           User userToUpdate = existingUser.get();
           userToUpdate.setName(user.getName());
           userToUpdate.setEmail(user.getEmail());
           return userRepository.save(userToUpdate);
       }
       return null;
   }
   ```

6. **Update deleteUser() Method**
   ```java
   public boolean deleteUser(Long id) {
       if (userRepository.existsById(id)) {
           userRepository.deleteById(id);
           return true;
       }
       return false;
   }
   ```

7. **Update searchUsers() Method**
   ```java
   public List<User> searchUsers(String query, String email) {
       if (query != null && !query.isEmpty()) {
           return userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
       }
       return userRepository.findAll();
   }
   ```

**API Compatibility**:
- ✅ Method signatures remain unchanged
- ✅ Return types identical (List<User>, User, boolean)
- ✅ All existing endpoints continue to work without modification
- ✅ No client-side code changes required

**Success Criteria**:
- UserService uses UserRepository for all persistence operations
- All methods return identical results as ArrayList implementation
- No SQL exceptions during basic CRUD operations

---

### Phase 4: Add Exception Handling & Validation

**Objective**: Implement proper error responses for validation and missing resource scenarios

**Tasks**:

1. **Create Custom Exception Classes**
   - Create `Backend/Backend/src/main/java/com/back/Backend/Exception/` package
   - Create `UserNotFoundException.java`:
     ```java
     @ResponseStatus(HttpStatus.NOT_FOUND)
     public class UserNotFoundException extends RuntimeException {
         public UserNotFoundException(String message) {
             super(message);
         }
     }
     ```

2. **Create DTO for Validation Errors**
   - Create `Backend/Backend/src/main/java/com/back/Backend/DTO/` package
   - Create `ValidationErrorResponse.java` for validation error responses
   - Include fields: `timestamp`, `status`, `errors` (Map of field->messages), `path`

3. **Update UserController with Error Handling**
   - Add `@ExceptionHandler` methods for validation exceptions
   - Handle `MethodArgumentNotValidException` for request body validation
   - Handle `UserNotFoundException` for missing resources
   - Return HTTP 400 for validation errors
   - Return HTTP 404 for not found

4. **Add @Valid Annotation to Controller Methods**
   - Add `@Valid` to `@RequestBody User user` parameters
   - Triggers validation defined in User entity
   - Returns validation errors automatically

**Success Criteria**:
- Validation errors return HTTP 400 with detailed error messages
- Missing user returns HTTP 404 with appropriate message
- All error responses use consistent JSON format

---

### Phase 5: Data Migration & Seeding

**Objective**: Migrate existing in-memory data to database and establish seed data strategy

**Tasks**:

1. **Create data.sql Seed Script**
   - File: `Backend/Backend/src/main/resources/data.sql`
   - Contains: 16 seed users from current UserService constructor
   - Executed automatically by Spring on application startup
   - Format: INSERT statements with explicit values

2. **Database Schema Creation**
   - Spring JPA auto-creates schema when `spring.jpa.hibernate.ddl-auto=update`
   - User table created with columns: id, name, email
   - Primary key on id (auto-increment)
   - NOT NULL constraints on name and email

3. **One-Time Data Preservation** (if existing production data)
   - Export ArrayList state before migration
   - Create INSERT script from exported data
   - Run after application deployment

**Seed Data** (16 users):
```
Shambhu, Rahul, Suresh, Ramesh, Amit, Priya, Vikram, Neha, 
Rajesh, Anjali, Sunil, Pooja, Manish, Kavita, Sanjay, Ritu
```

**Success Criteria**:
- Application starts and creates user table automatically
- Seed data inserted on first run
- 16 users queryable via API immediately after startup
- Data persists across application restarts

---

### Phase 6: API Endpoint Updates (if needed)

**Objective**: Verify API endpoints continue working and update response handling

**Tasks**:

1. **Verify GET /api/users**
   - Returns all users from database
   - No changes needed (repository.findAll() returns List)

2. **Verify GET /api/users/{id}**
   - Add 404 handling for missing IDs
   - Update to throw UserNotFoundException if not found
   - Return HTTP 404 with proper error message

3. **Verify POST /api/users**
   - Database auto-generates ID
   - Remove client-provided ID handling
   - Add validation error response (HTTP 400)

4. **Verify PUT /api/users/{id}**
   - Update existing user
   - Add 404 handling if ID doesn't exist
   - Add validation error response

5. **Verify DELETE /api/users/{id}**
   - Delete from database
   - Add 404 handling

6. **Verify GET /api/users/search**
   - Use repository search methods
   - Update endpoint to use new query methods
   - Maintain existing parameter structure

**Success Criteria**:
- All endpoints maintain same request/response contract
- HTTP status codes appropriate (200, 201, 400, 404)
- No breaking changes to API

---

### Phase 7: Testing & Validation

**Objective**: Verify data persistence, backward compatibility, and error handling

**Tasks**:

1. **Unit Tests** (Update existing)
   - File: `Backend/Backend/src/test/java/com/back/Backend/Service/UserServiceTest.java`
   - Mock UserRepository instead of ArrayList
   - Test all CRUD operations
   - Test search methods

2. **Integration Tests** (Create new)
   - File: `Backend/Backend/src/test/java/com/back/Backend/Repository/UserRepositoryIntegrationTest.java`
   - Use `@DataJpaTest` annotation
   - Test repository queries: findByNameContainingIgnoreCase, findByEmail
   - Test custom query methods
   - Use H2 in-memory database for testing

3. **API Integration Tests**
   - Test all REST endpoints via TestRestTemplate or MockMvc
   - Test backward compatibility (same inputs → same outputs)
   - Test validation error responses
   - Test persistence (create, restart, verify data exists)

4. **Test Scenarios** (from spec):
   - **Persistence**: Create user, restart app, verify user exists
   - **Search**: Query by name and email, verify correct results
   - **Validation**: Invalid email, missing name, verify 400 response
   - **CRUD**: Complete create-read-update-delete cycle
   - **Auto-ID**: Create multiple users, verify unique sequential IDs
   - **Edge Cases**: Duplicate email, concurrent requests, null values

5. **Test Data Fixtures**
   - Use seed data for consistent test scenarios
   - Create specific test users for each test case

**Success Criteria**:
- All existing tests pass with repository implementation
- Integration tests verify database operations
- API tests confirm backward compatibility
- Persistence test passes (data survives restart)
- Performance: CRUD operations < 200ms, search < 100ms

---

### Phase 8: Deployment & Verification

**Objective**: Prepare application for production deployment with database

**Tasks**:

1. **Build Verification**
   - Run: `mvn clean package`
   - Verify: No compilation errors
   - Verify: JAR builds successfully
   - Check: No DataSource auto-configuration conflicts

2. **Database File Location**
   - H2 database file: `./data/smartcollab.db`
   - Create `/data` directory if not exists
   - Ensure write permissions for application process

3. **Environment Configuration**
   - Verify application.properties has database URL
   - Configure for production server paths
   - Set `spring.jpa.hibernate.ddl-auto=validate` (production)

4. **Deployment Checklist**
   - ✅ All dependencies added to pom.xml
   - ✅ User entity has JPA annotations
   - ✅ UserRepository interface created
   - ✅ UserService uses repository
   - ✅ Validation annotations added
   - ✅ Exception handling implemented
   - ✅ Seed data script created
   - ✅ All tests pass
   - ✅ API endpoints verified
   - ✅ H2 database configured
   - ✅ Data persists across restarts

5. **Post-Deployment Verification**
   - Create new user via API
   - Retrieve user by ID
   - Search users by name
   - Update user
   - Delete user
   - Restart application
   - Verify user still exists
   - Check H2 Console for data integrity

**Success Criteria**:
- Application deploys successfully
- All CRUD operations work
- Data persists across restarts
- All tests pass (100% backward compatibility)
- No performance degradation

---

## Key Implementation Decisions

### 1. ID Generation Strategy
- **Decision**: Use `GenerationType.AUTO` with H2 database
- **Rationale**: Simple, automatic, sufficient for current scale (<100k users)
- **Alternatives Considered**: UUID (no, added complexity), SEQUENCE (overkill for this scale)

### 2. Database Choice
- **Decision**: H2 embedded database with file persistence
- **Rationale**: Zero configuration, file-based persistence, perfect for MVP phase
- **Alternatives Considered**: PostgreSQL (added complexity), SQLite (limited Spring Boot support)

### 3. Query Method Strategy
- **Decision**: Derive query methods from naming conventions
- **Rationale**: Readable, maintainable, no custom SQL needed
- **Alternatives Considered**: `@Query` annotations (more complex initially)

### 4. Validation Approach
- **Decision**: Entity-level validation annotations
- **Rationale**: Enforced at JPA layer, automatic controller error handling
- **Alternatives Considered**: Service-layer validation (redundant)

### 5. Backward Compatibility
- **Decision**: Service layer unchanged from client perspective
- **Rationale**: Zero client changes, transparent migration
- **Alternatives Considered**: New endpoints (unnecessary complexity)

### 6. Data Migration Strategy
- **Decision**: seed data via data.sql, auto-schema creation via Hibernate
- **Rationale**: Automatic on startup, no manual steps required
- **Alternatives Considered**: Flyway/Liquibase (overkill for this phase)

---

## Success Metrics & Validation

| Metric | Target | Validation Method |
|--------|--------|-------------------|
| API Backward Compatibility | 100% | Run all existing endpoint tests |
| Data Persistence | 100% | Create user → restart → verify exists |
| Validation Accuracy | 100% | Invalid input → HTTP 400 response |
| Search Performance | <100ms | Query 10k users, measure response time |
| CRUD Performance | <200ms p95 | Benchmark all operations |
| Test Coverage | >80% | JaCoCo report |
| Database File Size | <10MB | Monitor data.db file size |

---

## Risk Mitigation

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|-----------|
| DataSource auto-config conflict | Medium | High | Remove exclusion, use spring.jpa properties |
| Validation failing on API | Medium | High | Test with invalid inputs before Phase 7 |
| Data migration loss | Low | Critical | Keep ArrayList backup until verification complete |
| Concurrent request issues | Low | Medium | Use transaction support from Spring |
| Database file corruption | Low | Medium | Regular backups of data.db file |

---

## Dependencies & Versions

```xml
<!-- Spring Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
    <!-- Version inherited from parent: 4.0.4 -->
</dependency>

<!-- H2 Database -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
    <!-- Version inherited from parent: 2.1.214 -->
</dependency>

<!-- Jakarta Bean Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
    <!-- Version inherited from parent: 4.0.4 -->
</dependency>
```

All versions aligned with Spring Boot 4.0.4 parent, Java 17 compatibility verified.

---

## Timeline Estimate

| Phase | Duration | Dependencies |
|-------|----------|--------------|
| Phase 0: Dependencies | 1 day | None |
| Phase 1: Entity Mapping | 1 day | Phase 0 complete |
| Phase 2: Repository | 0.5 day | Phase 1 complete |
| Phase 3: Service Refactor | 1 day | Phase 2 complete |
| Phase 4: Error Handling | 1 day | Phase 3 complete |
| Phase 5: Data Migration | 0.5 day | Phase 4 complete |
| Phase 6: API Updates | 0.5 day | Phase 5 complete |
| Phase 7: Testing | 2 days | Phase 6 complete |
| Phase 8: Deployment | 1 day | Phase 7 complete |
| **Total** | **8 days** | Sequential |

---

## Files Modified/Created

**Modified Files**:
- `Backend/Backend/pom.xml` - Add dependencies
- `Backend/Backend/src/main/java/com/back/Backend/Model/User.java` - Add JPA annotations
- `Backend/Backend/src/main/java/com/back/Backend/Service/UserService.java` - Replace ArrayList with repository
- `Backend/Backend/src/main/java/com/back/Backend/Controller/UserController.java` - Add validation
- `Backend/Backend/src/main/resources/application.properties` - Add database config

**Created Files**:
- `Backend/Backend/src/main/java/com/back/Backend/Repository/UserRepository.java` - JpaRepository interface
- `Backend/Backend/src/main/java/com/back/Backend/Exception/UserNotFoundException.java` - Custom exception
- `Backend/Backend/src/main/java/com/back/Backend/DTO/ValidationErrorResponse.java` - Error DTO
- `Backend/Backend/src/main/resources/data.sql` - Seed data
- `Backend/Backend/src/test/java/com/back/Backend/Repository/UserRepositoryIntegrationTest.java` - Integration tests

---

## Next Steps

After Phase 1 (Design) completion:
1. ✅ Review `data-model.md` with team for entity design approval
2. ✅ Review `contracts/` for API contract validation
3. Generate tasks from implementation plan using `/speckit.tasks` command
4. Create GitHub issues from tasks for sprint planning
5. Begin Phase 0 implementation

