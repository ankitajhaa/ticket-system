# 🎫 Ticket Management System

A production-ready backend REST API built with **Spring Boot** for managing customer support tickets end-to-end. The system handles the full ticket lifecycle, role-based access control, SLA enforcement, async email notifications, bulk CSV operations, and operational metrics.

## Problem Statement

The company previously managed support tickets manually, resulting in:

- Poor tracking of issues
- Delayed agent responses
- No visibility into ticket lifecycle or SLA compliance

This system replaces that with a centralized, secure backend enforcing role-based workflows, automated SLA tracking, async notifications, and operational observability.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| Security | Spring Security + JWT |
| Database | MySQL 8 (Docker) |
| ORM | Spring Data JPA / Hibernate |
| Filtering | JpaSpecification + CriteriaBuilder |
| Caching | Caffeine Cache |
| Email | JavaMailSender (SMTP) |
| Async Processing | Spring `@Async` |
| Scheduling | Spring `@Scheduled` |
| CSV Processing | OpenCSV |
| Metrics | Micrometer + Prometheus (Docker) |
| Health Monitoring | Spring Boot Actuator |
| API Docs | SpringDoc OpenAPI (Swagger UI) |

---

### Key Design Principles

- **Layered architecture** — clear separation between controller, service, and data layers
- **Interface-based services** — `AuthService`, `TicketService`, `NotificationService` all backed by `Impl` classes for easy swapping
- **State machine** — ticket status transitions are strictly enforced, invalid transitions return `409`
- **@Transactional** — all write operations are atomic, audit log saved in the same transaction as the ticket update
- **Async by default** — email sending, CSV import, and CSV export all run via `@Async` to avoid blocking HTTP threads


### 1. Clone the repository

```bash
git clone https://github.com/ankita-jha/ticket-system.git
cd ticket-system
```

### 2. Start Docker services

```bash
docker-compose up -d
```

This starts MySQL and Prometheus. See [Docker Setup](#docker-setup) for the full compose file.

### 3. Configure the application

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Fill in your values — see [Configuration](#configuration).

### 4. Build and run

```bash
mvn clean install
mvn spring-boot:run
```

### 5. Access

| Service | URL |
|---|---|
| API Base | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` |
| Prometheus | `http://localhost:9090` |
| Actuator Health | `http://localhost:8080/actuator/health` |
| Actuator Metrics | `http://localhost:8080/actuator/prometheus` |

---

## Docker Setup

### `docker-compose.yml`

```yaml
version: '3.8'

services:

  mysql:
    image: mysql:8.0
    container_name: ticket-mysql
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: ticket_system
      MYSQL_USER: ticketuser
      MYSQL_PASSWORD: ticketpassword
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  prometheus:
    image: prom/prometheus:latest
    container_name: ticket-prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
    depends_on:
      - mysql

volumes:
  mysql_data:
```

### `prometheus.yml`

Place this in the project root alongside `docker-compose.yml`:

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'ticket-system'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8080']
```

## Configuration

All configuration lives in `src/main/resources/application.properties`:

```properties
# ── Server
server.port=8080

# ── Database (match Docker Compose values)
spring.datasource.url=jdbc:mysql://localhost:3306/ticket_system
spring.datasource.username=ticketuser
spring.datasource.password=ticketpassword
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# ── JWT
jwt.secret=your_jwt_secret_key_min_32_chars
jwt.access-token-expiry=60        # minutes
jwt.refresh-token-expiry=10080    # minutes (7 days)

# ── Email (Gmail SMTP example)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# ── Notifications
notification.retry.max-attempts=3

# ── SLA Scheduler interval in milliseconds (default 30 minutes)
sla.scheduler.interval=1800000

# ── Actuator + Prometheus
management.endpoints.web.exposure.include=health,prometheus
management.endpoint.prometheus.enabled=true
```

> **Gmail users:** Generate an App Password at [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords). Your regular Gmail password will not work with SMTP.

---

## Database Setup

Tables are auto-created maually Hibernate validates them on the  first run (`ddl-auto=validate`).

### Seed SLA Config

Run this after the app starts for the first time:

```sql
INSERT INTO sla_config (priority, resolution_hours, reminder_hours) VALUES
('CRITICAL', 4,  '2,1,0.5'),
('HIGH',     8,  '4,2,1'),
('MEDIUM',   24, '12,6,2'),
('LOW',      72, '24,12,4');
```

### Seed First Admin User

Generate a BCrypt hash for your chosen password at [bcrypt-generator.com](https://bcrypt-generator.com) or via Spring's `BCryptPasswordEncoder`, then insert:

```sql
INSERT INTO users (username, email, password, role, created_at, updated_at)
VALUES ('admin', 'admin@example.com', '$2a$10$YOUR_BCRYPT_HASH_HERE', 'ADMIN', NOW(), NOW());
```

### Recommended Indexes

```sql
CREATE INDEX idx_tickets_status       ON tickets(status);
CREATE INDEX idx_tickets_priority     ON tickets(priority);
CREATE INDEX idx_tickets_assigned     ON tickets(assigned_agent_id);
CREATE INDEX idx_tickets_created_by   ON tickets(created_by_id);
CREATE INDEX idx_tickets_created_at   ON tickets(created_at);
CREATE INDEX idx_tickets_sla_deadline ON tickets(sla_deadline);
```



## Running the App

```bash
# Development
mvn spring-boot:run
```

---

## API Reference

All protected endpoints require:
```
Authorization: Bearer <your_jwt_token>
```

Use the **Authorize** button in Swagger UI to set your token globally for all requests.

---

## API Endpoints

### Authentication
- `POST /api/auth/signup` — Register new customer account
- `POST /api/auth/login` — Login and receive JWT tokens
- `POST /api/auth/create-support-agent` — Create support agent *(Admin only)*
- `PATCH /api/auth/users/{id}/role` — Update user role *(Admin only)*

### Tickets
- `POST /api/tickets` — Create ticket *(Customer only)*
- `GET /api/tickets` — List tickets *(role-scoped)*
- `GET /api/tickets/{id}` — Get ticket by ID
- `PATCH /api/tickets/{id}` — Update ticket *(CLAIM / ASSIGN / UPDATE_PROGRESS / SET_PRIORITY)*

### Comments
- `POST /api/tickets/{id}/comments` — Add comment
- `GET /api/tickets/{id}/comments` — Get comments for ticket

### Notifications
- `POST /api/notifications/{id}/retry` — Retry failed notification *(Admin only)*

### Bulk Operations
- `POST /api/admin/tickets/import` — Bulk import tickets from CSV *(Admin only)*
- `POST /api/admin/tickets/export` — Export tickets to CSV *(Admin only)*

### Metrics & Health
- `GET /actuator/prometheus` — Prometheus metrics
- `GET /actuator/health` — Application health check
### Authentication


## Ticket Lifecycle

Tickets follow a strict one-way state machine. Any attempt to make an invalid transition returns `409 Conflict` with a message showing the allowed next states.

OPEN->ASSIGNED->IN_PROGRESS->RESOLVED->CLOSED
 

**Special cases:**
- Admin **reassignment** on `ASSIGNED` or `IN_PROGRESS` tickets swaps the agent without changing status
- Admin reassignment is blocked on `RESOLVED` and `CLOSED` tickets
- `CLOSED` tickets reject all updates and new comments with `409`

---

## SLA Management

SLA deadlines are auto-calculated on ticket creation using the `SLAConfig` table:

```
slaDeadline = createdAt + resolutionHours (for that priority)
```

When priority is changed via `SET_PRIORITY`, the deadline recalculates from the **current time**:

```
slaDeadline = now + resolutionHours (of new priority)
```

The SLA scheduler runs every 30 minutes (configurable via `sla.scheduler.interval`) and:

1. Finds tickets where `slaDeadline < now` and not yet marked breached
2. Sets `slaBreached = true` on each
3. Saves an `SLA_BREACHED` audit log entry (actor type: SYSTEM)
4. Sends breach emails to the assigned agent and admin
5. Finds active tickets approaching their deadline
6. Sends warning reminder emails at intervals configured in `SLAConfig.reminderHours` (e.g. `"4,2,1"` = warn at 4h, 2h, 1h remaining)

**`slaBreached` is a permanent flag.** Once set to `true` it is never reset — it is a historical fact that the ticket breached its SLA. Priority changes and resolution do not clear it. The full breach history is also preserved in the audit log independently.

---

## Notifications

All emails are sent asynchronously via `@Async`. Every attempt is logged in `NotificationLog` **before** sending, so records survive app crashes mid-send.

### Duplicate Prevention

Before every send, the service checks if a `SENT` record already exists for the same `(ticket, recipient, notificationType)` combination. Duplicates are silently skipped. The database unique constraint serves as a final safety net for race conditions.

### Retry Logic

A separate lightweight retry scheduler picks up `FAILED` notifications where `retryCount < maxRetries`. After max retries are exhausted, the notification stays `FAILED` and can be manually retriggered by an Admin via the retry API.

### Email Types

| Type | Recipient | Trigger |
|---|---|---|
| `TICKET_ASSIGNED` | Customer + Agent | CLAIM or ASSIGN action |
| `PRIORITY_CHANGED` | Assigned Agent only | SET_PRIORITY action |
| `SLA_WARNING` | Assigned Agent only | Scheduler — approaching deadline |
| `SLA_BREACH` | Assigned Agent + Admin | Scheduler — deadline passed |
| `IMPORT_COMPLETE` | Admin | CSV import finished |
| `IMPORT_FAILED` | Admin | CSV import error |
| `EXPORT_COMPLETE` | Admin (with CSV attached) | CSV export finished |
| `EXPORT_FAILED` | Admin | CSV export error |

---

## Bulk Import & Export

### Import CSV Format

```csv
customerReference,priority,title,description
5,HIGH,Login issue,Cannot log into dashboard
3,MEDIUM,Billing error,Charged twice for subscription
```

- `customerReference` — valid customer user ID in the system
- `priority` — one of `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
- Each row is validated independently — failed rows do not block valid rows
- Admin receives an email summary with success/failure counts and per-row failure details

### Export CSV Columns

```
id, title, description, status, priority, assignedAgent, createdBy, slaDeadline, createdAt
```

`ALL` exports every ticket. `SLA_BREACHED` exports only tickets where `slaBreached = true`.

---

## Audit Logging

Every significant action produces an `AuditLog` entry saved within the same `@Transactional` block as the operation itself.

| Audit Action | Trigger | Actor Type |
|---|---|---|
| `AGENT_ASSIGNED` | CLAIM or ASSIGN | AGENT or ADMIN |
| `STATUS_CHANGED` | UPDATE_PROGRESS | AGENT or ADMIN |
| `PRIORITY_CHANGED` | SET_PRIORITY | AGENT or ADMIN |
| `SLA_BREACHED` | SLA Scheduler | SYSTEM |
| `COMMENT_ADDED` | New comment posted | AGENT, ADMIN, or CUSTOMER |
| `BULK_IMPORT` | CSV import complete | ADMIN |

Audit logs are returned in:
- `PATCH /api/tickets/{id}` — the entry for the current action only
- `GET /api/tickets/{id}` — full history, visible to Admin and assigned Agent only

## Assumptions & Design Decisions

### Assumptions

- One ticket is assigned to only one agent at a time
- All endpoints require authentication — no public access
- Ticket status defaults to `OPEN` on creation
- Customer sets priority on creation, defaults to `MEDIUM` if not provided
- Admin and Agent can update priority later via `SET_PRIORITY`
- `SLAConfig` is seeded via SQL — no code change needed to adjust SLA timings
- If a ticket has no assigned agent at breach time, only admin is notified

### Key Decisions

| Decision | Reason |
|---|---|
| JWT access + refresh tokens with different expiry (1h / 7d) | Stateless, scalable, no server-side session storage |
| Admin blocked from creating tickets in service layer | Enforces role separation — admins manage, customers submit |
| Support agents created by Admin only, not self-registered | Prevents unauthorized privilege escalation |
| `ADMIN` role cannot be assigned via the role update endpoint | Admin creation restricted to DB seeder only — reduces attack surface |
| PATCH response includes the latest audit log entry | Avoids an extra GET call from the client after every update |
| `slaBreached` is never reset once set to true | Permanent historical signal — priority changes and resolution do not erase breach history |
| Duplicate notifications prevented via `NotificationLog` lookup | Same table used for retry tracking and deduplication — no separate mechanism needed |
| `SLAConfig` cached via Caffeine with evict-on-change | Data is static in normal operation — TTL would evict valid cache unnecessarily |
| CSV import and export processed via `@Async` | Avoids blocking HTTP thread for large files — admin notified via email on completion |
| `ASSIGN` bypasses state machine on reassignment | Reassignment is an agent swap, not a status transition — status only moves when ticket is `OPEN` |
| Comment visibility enforced in service layer | Customers never receive `INTERNAL` comments, not even as null fields in the response |
| Metrics via Micrometer counters incremented at point of action | Near-zero overhead — no DB queries needed for reporting |

---