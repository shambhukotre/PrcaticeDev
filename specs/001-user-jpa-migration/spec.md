# Feature Specification: User Service Database Migration

**Feature Branch**: `user-jpa-migration`  
**Created**: April 10, 2026  
**Status**: Draft  
**Input**: Migrate User service from in-memory ArrayList to H2 database with Spring Data JPA

## Overview

This specification details the migration of the User service from an in-memory ArrayList data structure to a persistent H2 database with Spring Data JPA. The migration maintains full backward compatibility with existing API endpoints while enabling data persistence, scalability, and standardized database operations.

## User Scenarios & Testing

### User Story 1 - Persist User Data Across Application Restarts (Priority: P1)

**Description**: System administrators need user data to persist when the application is restarted, ensuring no loss of user information.

**Why this priority**: Data persistence is foundational—without it, the application cannot be deployed to production. This is the core value delivered by the migration.

**Independent Test**: Can be fully tested by creating a user via API, restarting the application, and verifying the user still exists. This delivers immediate, measurable value.

**Acceptance Scenarios**:

1. **Given** application is running with user data in the database, **When** application is restarted, **Then** all previously created users are still accessible via GET endpoints
2. **Given** new users have been added, **When** application is stopped and restarted, **Then** the new users remain in the system
3. **Given** users have been modified, **When** application is restarted, **Then** user modifications are persisted

---

### User Story 2 - Query Users by Name and Email (Priority: P1)

**Description**: End users need to search for other users by name or email to find collaborators in the platform.

**Why this priority**: Search functionality is critical for platform usability. The current ArrayList search works but needs to transition to database queries for scalability.

**Independent Test**: Can be tested independently by calling the search endpoint with name/email parameters and verifying correct results are returned from the database.

**Acceptance Scenarios**:

1. **Given** multiple users exist in the database, **When** searching by name, **Then** all users matching the name (partial match) are returned
2. **Given** users exist with different emails, **When** searching by email, **Then** exact matching email returns the correct user
3. **Given** an empty search query, **When** searching, **Then** an appropriate response is returned (empty or all results depending on design)
4. **Given** a search query matches multiple users, **When** searching, **Then** all matching users are returned in a consistent order

---

### User Story 3 - Validate Required User Fields (Priority: P1)

**Description**: The system must ensure data quality by validating that required fields (name and email) are provided and properly formatted before storing user records.

**Why this priority**: Data validation prevents corrupt data from entering the database and protects data integrity. This is a blocking requirement for production readiness.

**Independent Test**: Can be tested independently by attempting to create/update users with missing or invalid email formats and verifying validation errors are returned.

**Acceptance Scenarios**:

1. **Given** a user creation request with missing name, **When** submitted, **Then** validation error is returned and user is not created
2. **Given** a user creation request with missing email, **When** submitted, **Then** validation error is returned and user is not created
3. **Given** a user creation request with invalid email format, **When** submitted, **Then** validation error is returned and user is not created
4. **Given** a user creation request with valid name and properly formatted email, **When** submitted, **Then** user is created successfully

---

### User Story 4 - CRUD Operations Return Consistent Results (Priority: P1)

**Description**: All existing REST API endpoints must continue working exactly as before, ensuring backward compatibility with client applications.

**Why this priority**: Existing client code depends on these endpoints. Breaking compatibility would require redeploying all consuming applications.

**Independent Test**: Can be tested independently by executing full CRUD workflow: create user, retrieve by ID, retrieve all, update, delete. This verifies the entire data flow through the database layer.

**Acceptance Scenarios**:

1. **Given** a valid User object, **When** calling POST /api/users, **Then** user is created in database and returned with auto-generated ID
2. **Given** a user exists in the database, **When** calling GET /api/users/{id}, **Then** the correct user record is returned
3. **Given** users exist in the database, **When** calling GET /api/users, **Then** all users are returned
4. **Given** a user exists with an ID, **When** calling PUT /api/users/{id}, **Then** user fields are updated in database
5. **Given** a user exists in the database, **When** calling DELETE /api/users/{id}, **Then** user is removed from database and subsequent GET returns null

---

### User Story 5 - Auto-Generate User IDs (Priority: P2)

**Description**: The system must automatically generate unique IDs when creating new users, eliminating the need for clients to provide IDs.

**Why this priority**: Automatic ID generation is a standard database practice that ensures uniqueness and simplifies client code. This is P2 because it's primarily an internal implementation improvement.

**Independent Test**: Can be tested independently by creating users without providing IDs and verifying each receives a unique, auto-generated ID.

**Acceptance Scenarios**:

1. **Given** a user creation request without an ID field, **When** submitted, **Then** database auto-generates a unique ID
2. **Given** multiple users are created sequentially, **When** checking their IDs, **Then** each ID is unique and sequentially incremented

---

### Edge Cases

- What happens when a user attempts to create a user with a duplicate email (if uniqueness constraint is added)?
- How does the system handle concurrent user creation requests?
- What happens when database connectivity is lost during a CRUD operation?
- How are invalid email formats handled—what constitutes valid email format for this system?
- What happens when attempting to update/delete a non-existent user ID?

## Requirements

### Functional Requirements

- **FR-001**: System MUST replace in-memory ArrayList with H2 database as the storage mechanism for all user data
- **FR-002**: System MUST add JPA annotations (@Entity, @Id, @GeneratedValue, @Column) to User class to define database mapping
- **FR-003**: System MUST auto-generate user IDs using GenerationType.AUTO when new users are created
- **FR-004**: System MUST create UserRepository interface extending JpaRepository to provide CRUD operations
- **FR-005**: System MUST update UserService to use UserRepository instead of ArrayList
- **FR-006**: System MUST implement search functionality using JPA @Query or derived query methods for searching by name and email
- **FR-007**: System MUST validate required fields: name (non-empty string) and email (valid email format)
- **FR-008**: System MUST maintain backward compatibility with all existing API endpoints (/api/users GET/POST/PUT/DELETE/{id}, /api/users/search)
- **FR-009**: System MUST add Spring Data JPA dependency to pom.xml (spring-boot-starter-data-jpa)
- **FR-010**: System MUST add H2 database dependency to pom.xml (com.h2database:h2)
- **FR-011**: System MUST configure H2 database connection properties in application.properties (spring.datasource.url, spring.datasource.username, spring.datasource.password)
- **FR-012**: System MUST enable automatic schema creation/updates via spring.jpa.hibernate.ddl-auto property
- **FR-013**: System MUST return HTTP 400 Bad Request with validation error messages when invalid data is submitted
- **FR-014**: System MUST return HTTP 404 Not Found when attempting to retrieve/update/delete non-existent users

### Key Entities

- **User**: Represents a user in the platform with persistent storage
  - `id` (Long): Unique identifier, auto-generated by database
  - `name` (String): User's display name, required, non-empty
  - `email` (String): User's email address, required, valid email format
  - Relationships: None in current design (potential future relationships to Courses, Documents)

## Success Criteria

### Measurable Outcomes

- **SC-001**: All existing API endpoints continue to work without modification to client code (zero breaking changes)
- **SC-002**: User data persists across application restarts—no data loss after reboot
- **SC-003**: Search operations complete in under 100ms for datasets of up to 10,000 users (performance baseline)
- **SC-004**: System correctly validates and rejects invalid user creation/update requests with appropriate error messages
- **SC-005**: Auto-generated user IDs are unique and non-repeating across all user creation operations
- **SC-006**: All CRUD operations (Create, Read, Update, Delete) function correctly through the database layer
- **SC-007**: System handles 100+ concurrent CRUD operations without data corruption or deadlocks
- **SC-008**: Migration is completed without requiring client-side code changes to any consuming applications

## Assumptions

- **Target User Base**: Assumes the application will be used by authenticated users of the SmartCollab platform, currently without multi-tenancy requirements
- **Data Volume**: Assumes the initial deployment will have fewer than 100,000 user records; further optimization may be needed at larger scales
- **Email Uniqueness**: Assumes email uniqueness is not enforced at the database level in this iteration (can be added in future versions)
- **Email Format Validation**: Assumes standard RFC 5322-compliant email format validation is sufficient (no domain whitelist required)
- **ID Generation Strategy**: Assumes database auto-increment ID generation is acceptable (no need for distributed ID generation like UUID)
- **Database Location**: Assumes H2 database file will be stored locally on the application server; backup/replication is out of scope for this phase
- **Transaction Behavior**: Assumes Spring Data JPA default transaction handling is sufficient without custom transaction boundaries
- **Existing Dependencies**: Assumes Spring Boot 4.0.4 and existing Spring MVC infrastructure will be extended with Spring Data JPA (no version conflicts)
- **Client Applications**: Assumes all clients expect the same User DTO structure; no API versioning is required
- **Testing Responsibility**: Assumes integration tests will validate database operations; unit tests may mock the repository layer

## Design Artifacts

### Database Schema

```
TABLE: user
├── id (BIGINT, PRIMARY KEY, AUTO_INCREMENT)
├── name (VARCHAR(255), NOT NULL)
└── email (VARCHAR(255), NOT NULL)

Indexes:
├── PRIMARY KEY on id
└── (Optional for future) UNIQUE INDEX on email
```

### Entity Design

```
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;
    
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    @Column(nullable = false)
    private String email;
    
    // Getters, setters, constructors...
}
```

### Repository Interface

```
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByNameContainingIgnoreCase(String name);
    List<User> findByEmail(String email);
    List<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);
}
```

### Implementation Approach

1. **Phase 1**: Add dependencies and configure H2 in application.properties
2. **Phase 2**: Annotate User entity with JPA mappings
3. **Phase 3**: Create UserRepository interface
4. **Phase 4**: Update UserService to use repository instead of ArrayList
5. **Phase 5**: Add validation annotations to User entity
6. **Phase 6**: Update error handling in UserController for validation errors
7. **Phase 7**: Test all endpoints and verify backward compatibility
8. **Phase 8**: Populate database with initial seed data or migration from existing system


