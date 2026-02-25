# 🎫 Ticket Management System

A backend system for managing customer support tickets with role-based access, assignment workflows, status tracking, and audit history.

---

## 📌 Problem Statement

The company currently manages customer support tickets manually, leading to:

- Poor tracking of issues
- Delayed responses
- No visibility into ticket lifecycle

This project provides a centralized backend system to manage tickets efficiently and securely.

---

## 👥 User Roles

### Admin
- View all tickets
- Assign or reassign agents
- Monitor system activity

### Support Agent
- View assigned tickets
- Claim unassigned tickets
- Update ticket progress and status

### Customer
- Create support tickets
- View own tickets
- Track ticket progress

---

## 🚀 Core Features

- Ticket creation and management
- Role-based access control (RBAC)
- Ticket assignment to agents
- Status tracking and updates
- Filtering and searching tickets
- Audit logging and history tracking
- JWT-based authentication

---

## 🏗️ System Architecture

The application follows a **layered backend architecture** for maintainability and scalability.

### 1️⃣ API Layer (REST Endpoints)

Responsibilities:

- Handle HTTP requests
- Validate request data
- Return JSON responses
- Manage HTTP status codes

### 2️⃣ Authentication & Authorization

- JWT-based authentication
- Stateless security
- Role-based permission enforcement

**Authentication Flow**

1. User logs in with credentials
2. Server validates user
3. JWT access & refresh tokens generated
4. Access token used for protected endpoints

---

### 3️⃣ Service / Business Logic Layer

Handles:

- Role-based validation
- Assignment logic
- Permission rules
- Status transitions
- Business constraints

---

### 4️⃣ Data Access Layer

- ORM-based database interaction
- CRUD operations
- Entity relationships
- Query filtering

---

## 🗄️ Database Design

### Database Used

**MySQL**

Why MySQL?

- Strong relational model
- ACID compliance
- Good performance for CRUD-heavy systems
- Excellent indexing support
- Seamless ORM integration

---

## 🧩 Data Models

### User

| Field | Description |
|---|---|
| id | Primary key |
| username | Unique username |
| email | Unique email |
| password | Encrypted password |
| role | ADMIN / SUPPORT_AGENT / CUSTOMER |
| created_at | Timestamp |
| updated_at | Timestamp |

---

### Ticket

| Field | Description |
|---|---|
| id | Primary key |
| title | Ticket title |
| description | Issue description |
| status | OPEN / ASSIGNED / IN_PROGRESS / RESOLVED / CLOSED |
| created_by | FK → User |
| assigned_agent | FK → User |
| created_at | Timestamp |
| updated_at | Timestamp |

---

### Audit Log

| Field | Description |
|---|---|
| id | Primary key |
| ticket_id | FK → Ticket |
| updated_by | FK → User |
| old_value | Previous value |
| new_value | Updated value |
| action | Action performed |
| timestamp | Time of action |

---

## 🔌 API Overview

### Authentication

#### Login

`POST /api/users/login/`

Request:

```json
{
  "email": "user@email.com",
  "password": "password123"
} 
```

Response:

```json
{
  "access": "jwt_access_token",
  "refresh": "jwt_refresh_token",
  "expiry_time": 60
} 
```

### Ticket APIs

#### Create Ticket (Customer only)

`POST /api/tickets/`

```json
{
  "title": "Login issue",
  "description": "Unable to login to dashboard"
} 
```

#### View Tickets

`GET /api/tickets/`

Behavior based on role:
- Customer → own tickets
- Agent → assigned tickets
- Admin → all tickets

#### View Single Ticket

`GET /api/tickets/{id}`

Access rules:
- Admin → any ticket
- Agent → only assigned tickets
- Customer → own tickets

#### Update Ticket

`PATCH /api/tickets/{id}`

##### Agent Claim Ticket
```json
{
  "action": "claim"
} 
```

##### Agent Update Progress
```json
{
  "action": "update_progress",
  "status": "IN_PROGRESS"
} 
```

##### Admin Assign Agent
```json
{
  "action": "assign",
  "assigned_agent": 3
} 
```

### Security Strategy
- JWT token authentication
- Role-based access control
- Ownership validation
- Endpoint-level permission checks
- Protected routes for sensitive operations

### Business Rules
- Customers can access only their tickets
- Agents can update only assigned tickets
- Closed tickets cannot be modified
- Status values must be valid
- One ticket → one assigned agent

## Running the Project

1. Clone Repository

```bash
git clone https://github.com/ankitajhaa/ticket-system
```

2. Configure database in application.properties

3.  Run application

```bash
mvn spring-boot:run
```

4. Open Swagger UI

```bash
http://localhost:8080/swagger-ui/index.html
```

### Assumptions & Design Decisions

#### Assumptions
- One ticket assigned to one agent
- Authentication required for all APIs
- Default ticket status = OPEN

#### Key Decisions
- JWT for authentication
- RBAC for permission handling
- Business logic handled in service layer