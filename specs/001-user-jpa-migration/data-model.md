# Data Model: User Service Database Design

**Feature**: User Service Database Migration  
**Date**: April 10, 2026  
**Status**: Design Phase 1 - Complete

## Overview

This document defines the data model for the User service after migration from in-memory ArrayList to persistent H2 database with Spring Data JPA. The design maintains the existing User domain model while adding persistence layer mappings, validation constraints, and database schema specifications.

---

## Entity Design

### User Entity

#### Entity Definition

```java
package com.back.Backend.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "user")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Column(nullable = false, length = 255)
    private String name;
    
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    @Column(nullable = false, length = 255)
    private String email;
    
    // Constructors
    public User() {
    }
    
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
    
    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    // equals and hashCode (based on id)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
```

#### Field Specifications

| Field | Type | JPA Type | Constraints | Notes |
|-------|------|----------|-------------|-------|
| `id` | Long | `@Id @GeneratedValue(AUTO)` | NOT NULL, PRIMARY KEY | Auto-incremented by database |
| `name` | String | `@Column(nullable=false)` | NOT NULL, max 255 chars | Display name, required |
| `email` | String | `@Column(nullable=false)` | NOT NULL, max 255 chars, valid email format | User email, required, RFC 5322 format |

#### Validation Rules

| Field | Validator | Error Message | Rule |
|-------|-----------|---------------|------|
| `name` | `@NotBlank` | "Name is required" | Cannot be null or empty after trim |
| `email` | `@NotBlank` | "Email is required" | Cannot be null or empty after trim |
| `email` | `@Email` | "Email should be valid" | Must be valid email format per RFC 5322 |

#### State Lifecycle

```
NEW (Transient)
    ↓ (save)
PERSISTENT (Managed by JpaRepository)
    ↓ (update)
PERSISTENT (Modified)
    ↓ (delete)
DETACHED (Removed from database)
```

---

## Database Schema

### H2 Database Configuration

**Database Type**: H2 embedded file-based  
**File Location**: `./data/smartcollab.db`  
**Connection String**: `jdbc:h2:file:./data/smartcollab`  
**Schema Auto-Creation**: Enabled via Hibernate DDL Auto (`update` mode)

### Table: user

```sql
CREATE TABLE user (
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
);
```

#### Table Specification

| Column | Type | Constraints | Default | Notes |
|--------|------|-------------|---------|-------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Sequence | Unique identifier |
| `name` | VARCHAR(255) | NOT NULL | None | User display name |
| `email` | VARCHAR(255) | NOT NULL | None | User email address |

#### Indexes

| Index Name | Columns | Type | Purpose |
|------------|---------|------|---------|
| PRIMARY | id | PRIMARY KEY | Entity ID lookup |
| (none) | email | Regular | Optional future: email uniqueness validation |
| (none) | name | Regular | Optional future: name-based search optimization |

**Index Strategy**: Currently minimal indexing for MVP. Future optimization opportunities:
- Unique index on `email` if business requires email uniqueness
- Regular index on `name` if search performance becomes bottleneck

#### Constraints

- **Primary Key**: `id` - ensures entity uniqueness and identity
- **Not Null**: `name`, `email` - ensures data completeness
- **Domain Validation**: Email format validated at application layer via `@Email`

### Seed Data

Initial 16 seed users loaded on application startup via `data.sql`:

```sql
INSERT INTO user (id, name, email) VALUES (1, 'Shambhu', 'shambhu@test.com');
INSERT INTO user (id, name, email) VALUES (2, 'Rahul', 'rahul@gmail.com');
INSERT INTO user (id, name, email) VALUES (3, 'Suresh', 'Suresh@gmail.com');
INSERT INTO user (id, name, email) VALUES (4, 'Ramesh', 'Ramesh@gmail.com');
INSERT INTO user (id, name, email) VALUES (5, 'Amit', 'amit@gmail.com');
INSERT INTO user (id, name, email) VALUES (6, 'Priya', 'priya@gmail.com');
INSERT INTO user (id, name, email) VALUES (7, 'Vikram', 'vikram@gmail.com');
INSERT INTO user (id, name, email) VALUES (8, 'Neha', 'neha@gmail.com');
INSERT INTO user (id, name, email) VALUES (9, 'Rajesh', 'rajesh@gmail.com');
INSERT INTO user (id, name, email) VALUES (10, 'Anjali', 'anjali@gmail.com');
INSERT INTO user (id, name, email) VALUES (11, 'Sunil', 'sunil@gmail.com');
INSERT INTO user (id, name, email) VALUES (12, 'Pooja', 'pooja@test.com');
INSERT INTO user (id, name, email) VALUES (13, 'Manish', 'manish@gmail.com');
INSERT INTO user (id, name, email) VALUES (14, 'Kavita', 'kavita@gmail.com');
INSERT INTO user (id, name, email) VALUES (15, 'Sanjay', 'sanjay@gmail.com');
INSERT INTO user (id, name, email) VALUES (16, 'Ritu', 'Ritu@yaho.com');
```

---

## Repository Layer

### UserRepository Interface

```java
package com.back.Backend.Repository;

import com.back.Backend.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find users by name (case-insensitive partial match)
     * @param name partial or full name to search for
     * @return list of users matching the name
     */
    List<User> findByNameContainingIgnoreCase(String name);
    
    /**
     * Find user by exact email match
     * @param email the email address to search for
     * @return list containing user with matching email (0 or 1 results)
     */
    List<User> findByEmail(String email);
    
    /**
     * Find users by name OR email (case-insensitive, partial match on both)
     * @param name search term for name field
     * @param email search term for email field
     * @return list of users matching either name or email
     */
    List<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);
}
```

#### Inherited Methods (from JpaRepository)

| Method | Signature | Purpose |
|--------|-----------|---------|
| `save` | `User save(User entity)` | Create new or update existing user |
| `saveAll` | `List<User> saveAll(Iterable<User> entities)` | Batch create/update |
| `findById` | `Optional<User> findById(Long id)` | Get user by ID |
| `findAll` | `List<User> findAll()` | Get all users |
| `findAllById` | `List<User> findAllById(Iterable<Long> ids)` | Get users by ID list |
| `count` | `long count()` | Get total user count |
| `deleteById` | `void deleteById(Long id)` | Delete user by ID |
| `delete` | `void delete(User entity)` | Delete specific user |
| `deleteAll` | `void deleteAll()` | Delete all users (be careful!) |
| `existsById` | `boolean existsById(Long id)` | Check if user exists |

#### Custom Query Methods

**1. findByNameContainingIgnoreCase(String name)**
- **SQL Generated**: `SELECT * FROM user WHERE LOWER(name) LIKE LOWER(?)`
- **Example**: `findByNameContainingIgnoreCase("raj")` → returns [Rajesh, Sanjay]
- **Use Case**: Search by partial name

**2. findByEmail(String email)**
- **SQL Generated**: `SELECT * FROM user WHERE email = ?`
- **Example**: `findByEmail("amit@gmail.com")` → returns [Amit]
- **Use Case**: Get specific user by email (unique lookup)

**3. findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email)**
- **SQL Generated**: `SELECT * FROM user WHERE LOWER(name) LIKE LOWER(?) OR LOWER(email) LIKE LOWER(?)`
- **Example**: Both called with "test" → returns [Shambhu, Pooja]
- **Use Case**: Combined search across name and email fields

---

## Performance Considerations

### Query Performance

| Query Type | Scale (users) | Expected Time | Notes |
|------------|---------------|---------------|-------|
| Get all users | 10,000 | <50ms | Full table scan, acceptable for MVP |
| Get by ID | 10,000 | <10ms | Primary key lookup, O(1) |
| Search by name | 10,000 | <100ms | LIKE query, acceptable per spec |
| Search by email | 10,000 | <50ms | Indexed field (future optimization) |

### Optimization Opportunities (Future)

1. **Database Indexing**
   - Add index on `email` for faster lookups
   - Add index on `name` if search becomes bottleneck
   - Evaluate composite indexes for combined searches

2. **Pagination**
   - Add pagination to `findAll()` for large result sets
   - Modify search methods to support pagination: `Page<User> findByNameContainingIgnoreCase(String name, Pageable pageable)`

3. **Caching**
   - Cache frequently accessed users (by ID)
   - Invalidate cache on user updates
   - Use Spring Cache abstraction

4. **Database Connection Pool**
   - Configure HikariCP for connection pooling
   - Monitor pool usage in production

---

## Relationships & Future Extensibility

### Current State

**No relationships** in current design. User entity is standalone.

### Future Relationship Opportunities

| Relationship | Type | With Entity | Notes |
|--------------|------|-------------|-------|
| User → Courses | One-to-Many | Course | User creates/manages courses |
| User → Documents | One-to-Many | Document | User creates/manages documents |
| User → Collaborations | Many-to-Many | User | Users collaborate on courses/documents |

These relationships are **out of scope** for current migration but design accommodates future extensibility through:
- Consistent ID generation strategy
- Validation framework in place
- Repository pattern for easy extension

---

## Validation Flow

### Request → Validation → Database

```
REST API Request (JSON)
    ↓
Spring MVC Deserialization
    ↓
@Valid Annotation Trigger
    ↓
Jakarta Bean Validation Processor
    ↓
    ├─ @NotBlank on name → Check not empty
    ├─ @NotBlank on email → Check not empty
    └─ @Email on email → Check valid email format
    ↓
Validation Pass?
    ├─ YES → UserService.addUser() → UserRepository.save()
    └─ NO → Return HTTP 400 with error details
```

### Error Response Format

```json
{
  "timestamp": "2026-04-10T12:34:56.789Z",
  "status": 400,
  "errors": {
    "name": "Name is required",
    "email": "Email should be valid"
  },
  "path": "/api/users"
}
```

---

## Data Integrity

### Referential Integrity

- No foreign keys in current design (no relationships)
- Future relationships will enforce referential integrity

### Domain Constraints

- **Email Format**: Enforced via `@Email` validator
- **Non-Null Fields**: Enforced via database `NOT NULL` and `@NotBlank`
- **Unique IDs**: Enforced via PRIMARY KEY auto-increment

### Data Quality Measures

1. **Application Layer**: Validation annotations prevent invalid data creation
2. **Database Layer**: NOT NULL constraints, PRIMARY KEY uniqueness
3. **Testing Layer**: Integration tests verify constraints enforced

---

## Migration Strategy

### From ArrayList to Database

**Timeline**: Immediate on Phase 0 deployment

**Steps**:
1. Deploy new code with JPA annotations and repository
2. Application initializes with empty database
3. Hibernate DDL auto-creates user table
4. `data.sql` inserts 16 seed users
5. API endpoints resume with database backing

**Rollback Strategy**:
- Keep ArrayList implementation in git history
- Tag release before JPA migration
- Revert if critical issues discovered
- Keep backup of initial data.sql before any schema changes

---

## Testing Strategy

### Unit Testing

**Mocking**: Repository is mocked in UserService unit tests

```java
@Mock
UserRepository userRepository;

@Before
public void setup() {
    MockitoAnnotations.openMocks(this);
}

@Test
public void testGetAllUsers() {
    User user1 = new User(1L, "Test", "test@test.com");
    when(userRepository.findAll()).thenReturn(Arrays.asList(user1));
    
    List<User> result = userService.getAllUsers();
    
    assertEquals(1, result.size());
    assertEquals("Test", result.get(0).getName());
}
```

### Integration Testing

**Real Database**: Tests use H2 in-memory database

```java
@DataJpaTest
public class UserRepositoryIntegrationTest {
    
    @Autowired
    UserRepository userRepository;
    
    @Test
    public void testFindByNameContainingIgnoreCase() {
        User user = new User("Rajesh", "rajesh@gmail.com");
        userRepository.save(user);
        
        List<User> result = userRepository.findByNameContainingIgnoreCase("raj");
        
        assertEquals(1, result.size());
        assertEquals("Rajesh", result.get(0).getName());
    }
}
```

---

## Configuration

### Spring Boot Configuration (application.properties)

```properties
# H2 Database Configuration
spring.datasource.url=jdbc:h2:file:./data/smartcollab
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# H2 Console (for debugging)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA Hibernate Configuration
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update

# JPA Logging
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
```

---

## Summary

The User data model represents a straightforward entity-to-relational mapping:
- **Entity**: Single User class with JPA annotations
- **Database**: Single user table with 3 columns (id, name, email)
- **Repository**: JpaRepository interface with 3 custom query methods
- **Validation**: Declarative constraints at entity level
- **Performance**: Optimized for MVP scale (<100k users)
- **Extensibility**: Designed for future relationships and features

This design maintains 100% backward compatibility while enabling persistent storage and scalable database operations.


