# User Service Database Migration

## Implementation Strategy

MVP First: Start with User Story 1 (P1) - Persist User Data Across Application Restarts. This delivers the core value of the migration with immediate, measurable benefits.

Incremental Delivery: Each user story builds on the previous, allowing for iterative development and early feedback.

## Dependencies

User Story Completion Order:
- US1 (P1) - Foundational persistence
- US2 (P1) - Search functionality  
- US3 (P1) - Data validation
- US4 (P1) - Full CRUD consistency
- US5 (P2) - Auto-ID generation

## Parallel Execution Examples

Per User Story:
- US1: Can be implemented in parallel with US2 if repository layer is shared
- US2: Independent once repository search methods exist
- US3: Parallel with US4, both depend on entity validation
- US4: Requires US1 completion for service layer
- US5: Depends on JPA entity setup (US1)

## Phase 1: Setup

- [X] T001 Add spring-boot-starter-data-jpa dependency to Backend/Backend/pom.xml
- [X] T002 Add com.h2database:h2 dependency to Backend/Backend/pom.xml
- [X] T003 Add spring-boot-starter-validation dependency to Backend/Backend/pom.xml
- [X] T004 Configure H2 database connection in Backend/Backend/src/main/resources/application.properties
- [X] T005 Verify DataSource auto-configuration compatibility in Backend/Backend/src/main/resources/application.properties

## Phase 2: Foundational

- [X] T006 [P] Add @Entity and @Table annotations to User class in Backend/Backend/src/main/java/com/back/Backend/Model/User.java
- [X] T007 [P] Add @Id and @GeneratedValue annotations to id field in Backend/Backend/src/main/java/com/back/Backend/Model/User.java
- [X] T008 [P] Add @Column(nullable = false) to name and email fields in Backend/Backend/src/main/java/com/back/Backend/Model/User.java
- [X] T009 [P] Add @NotBlank and @Email validation annotations to User class in Backend/Backend/src/main/java/com/back/Backend/Model/User.java
- [X] T010 [P] Add equals() and hashCode() methods to User class in Backend/Backend/src/main/java/com/back/Backend/Model/User.java
- [X] T011 Create Repository package directory Backend/Backend/src/main/java/com/back/Backend/Repository/
- [X] T012 Create UserRepository interface in Backend/Backend/src/main/java/com/back/Backend/Repository/UserRepository.java
- [X] T013 Add custom query methods to UserRepository interface

## Phase 3: User Story 1 - Persist User Data Across Application Restarts

**Goal**: System administrators need user data to persist when the application is restarted, ensuring no loss of user information.

**Independent Test Criteria**: Can be fully tested by creating a user via API, restarting the application, and verifying the user still exists.

**Tests**:
- [ ] T032 [US1] Create integration test for data persistence across application restarts

**Implementation Tasks**:
- [X] T014 [US1] Replace ArrayList with UserRepository injection in UserService constructor in Backend/Backend/src/main/java/com/back/Backend/Service/UserService.java
- [X] T015 [US1] Update getAllUsers() method to use userRepository.findAll() in Backend/Backend/src/main/java/com/back/Backend/Service/UserService.java
- [X] T016 [US1] Update getUserById() method to use userRepository.findById() in Backend/Backend/src/main/java/com/back/Backend/Service/UserService.java
- [X] T017 [US1] Update addUser() method to use userRepository.save() in Backend/Backend/src/main/java/com/back/Backend/Service/UserService.java
- [X] T018 [US1] Update updateUser() method to handle Optional and save in Backend/Backend/src/main/java/com/back/Backend/Service/UserService.java
- [X] T019 [US1] Update deleteUser() method to use userRepository.existsById() and deleteById() in Backend/Backend/src/main/java/com/back/Backend/Service/UserService.java
- [X] T020 [US1] Update searchUsers() method to use userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase() in Backend/Backend/src/main/java/com/back/Backend/Service/UserService.java
- [X] T025 [US1] Create data.sql file with 16 seed users in Backend/Backend/src/main/resources/data.sql

## Phase 4: User Story 2 - Query Users by Name and Email

**Goal**: End users need to search for other users by name or email to find collaborators in the platform.

**Independent Test Criteria**: Can be tested independently by calling the search endpoint with name/email parameters and verifying correct results are returned from the database.

**Tests**:
- [ ] T037 [US2] Test search functionality by name and email via API endpoints

**Implementation Tasks**:
- [X] T037 [US2] Verify search methods work correctly with database queries

## Phase 5: User Story 3 - Validate Required User Fields

**Goal**: The system must ensure data quality by validating that required fields (name and email) are provided and properly formatted before storing user records.

**Independent Test Criteria**: Can be tested independently by attempting to create/update users with missing or invalid email formats and verifying validation errors are returned.

**Tests**:
- [ ] T038 [US3] Test validation errors for missing name and invalid email format

**Implementation Tasks**:
- [X] T021 [US3] Create UserNotFoundException class in Backend/Backend/src/main/java/com/back/Backend/Exception/UserNotFoundException.java
- [X] T022 [US3] Create ValidationErrorResponse DTO in Backend/Backend/src/main/java/com/back/Backend/DTO/ValidationErrorResponse.java
- [X] T023 [US3] Add @ExceptionHandler methods to UserController for validation and not found errors in Backend/Backend/src/main/java/com/back/Backend/Controller/UserController.java
- [X] T024 [US3] Add @Valid annotations to UserController method parameters in Backend/Backend/src/main/java/com/back/Backend/Controller/UserController.java
- [X] T026 [US3] Update getUserById in UserController to throw UserNotFoundException if not found in Backend/Backend/src/main/java/com/back/Backend/Controller/UserController.java
- [X] T027 [US3] Update updateUser in UserController to throw UserNotFoundException if not found in Backend/Backend/src/main/java/com/back/Backend/Controller/UserController.java
- [X] T028 [US3] Update deleteUser in UserController to handle not found case in Backend/Backend/src/main/java/com/back/Backend/Controller/UserController.java

## Phase 6: User Story 4 - CRUD Operations Return Consistent Results

**Goal**: All existing REST API endpoints must continue working exactly as before, ensuring backward compatibility with client applications.

**Independent Test Criteria**: Can be tested independently by executing full CRUD workflow: create user, retrieve by ID, retrieve all, update, delete. This verifies the entire data flow through the database layer.

**Tests**:
- [ ] T039 [US4] Test full CRUD workflow: create, read, update, delete user

**Implementation Tasks**:
- [X] T039 [US4] Verify all CRUD operations work consistently with database backend

## Phase 7: User Story 5 - Auto-Generate User IDs

**Goal**: The system must automatically generate unique IDs when creating new users, eliminating the need for clients to provide IDs.

**Independent Test Criteria**: Can be tested independently by creating users without providing IDs and verifying each receives a unique, auto-generated ID.

**Tests**:
- [ ] T040 [US5] Test auto-generated IDs are unique and sequential

**Implementation Tasks**:
- [X] T040 [US5] Verify auto-ID generation works correctly for new users

## Phase 8: Polish & Cross-Cutting Concerns

- [X] T029 Update UserServiceTest to mock UserRepository instead of ArrayList in Backend/Backend/src/test/java/com/back/Backend/Service/UserServiceTest.java
- [X] T030 Create UserRepositoryIntegrationTest for repository queries in Backend/Backend/src/test/java/com/back/Backend/Repository/UserRepositoryIntegrationTest.java
- [X] T031 Create API integration tests for all endpoints in Backend/Backend/src/test/java/com/back/Backend/Controller/UserControllerIntegrationTest.java
- [X] T033 Run mvn clean package to verify build
- [X] T034 Ensure data directory exists for H2 database file
- [X] T035 Configure production settings in application.properties
- [X] T036 Perform post-deployment verification of all CRUD operations
