-- Add new columns to Ticket
ALTER TABLE Ticket ADD COLUMN priority ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL DEFAULT 'MEDIUM';
ALTER TABLE Ticket ADD COLUMN sla_deadline DATETIME;
ALTER TABLE Ticket ADD COLUMN sla_breached BOOLEAN NOT NULL DEFAULT FALSE;

-- Add ESCALATED to status enum
ALTER TABLE Ticket MODIFY COLUMN status ENUM('OPEN','ASSIGNED', 
'IN_PROGRESS', 'RESOLVED', 'CLOSED') NOT NULL DEFAULT 'OPEN';

-- Create SLA_config table
CREATE TABLE IF NOT EXISTS SLA_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL UNIQUE,
    resolution_hours INT NOT NULL,
    reminder_hours VARCHAR(50) NOT NULL
);

DROP TABLE IF EXISTS Notification_log;

CREATE TABLE IF NOT EXISTS Notification_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    notification_type ENUM(
        'SLA_WARNING',
        'SLA_BREACH',
        'TICKET_ASSIGNED',
        'PRIORITY_CHANGED',
        'IMPORT_COMPLETE',
        'EXPORT_COMPLETE',
        'IMPORT_FAILED',
        'EXPORT_FAILED'
    ) NOT NULL,
    status ENUM('PENDING','SENT','FAILED') NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    last_attempt_at DATETIME,
    created_at DATETIME,
    FOREIGN KEY (ticket_id) REFERENCES Ticket(id)
);

-- Create Comment table
CREATE TABLE IF NOT EXISTS Comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    comment_type ENUM('PUBLIC','INTERNAL') NOT NULL,
    created_at DATETIME,
    FOREIGN KEY (ticket_id) REFERENCES Ticket(id),
    FOREIGN KEY (author_id) REFERENCES Users(id)
);

-- Seed SLAConfig
INSERT INTO SLA_config (priority, resolution_hours, reminder_hours) VALUES
('CRITICAL', 4,  '2,1,0.5'),
('HIGH',   8,  '4,2,1'),
('MEDIUM',24, '12,4,1'),
('LOW',  72, '24,8,1');

-- Indexes
CREATE INDEX idx_ticket_status ON Ticket(status);
CREATE INDEX idx_ticket_priority ON Ticket(priority);
CREATE INDEX idx_ticket_sla_deadline ON Ticket(sla_deadline);
CREATE INDEX idx_ticket_sla_breached ON Ticket(sla_breached);
CREATE INDEX idx_ticket_created_by ON Ticket(created_by);
CREATE INDEX idx_ticket_assigned_agent ON Ticket(assigned_agent);
CREATE INDEX idx_ticket_created_at ON Ticket(created_at);



INSERT INTO users (id, username, password, email, role, created_at, updated_at) VALUES
(1, 'admin', 'adminpass', 'admin@company.com', 'ADMIN', NOW(), NOW()),
(2, 'agent_john', 'password123', 'john@company.com', 'AGENT', NOW(), NOW()),
(3, 'agent_sarah', 'password123', 'sarah@company.com', 'AGENT', NOW(), NOW()),
(4, 'customer_raj', 'password123', 'raj@gmail.com', 'CUSTOMER', NOW(), NOW()),
(5, 'customer_amit', 'password123', 'amit@gmail.com', 'CUSTOMER', NOW(), NOW());


INSERT INTO sla_config (id, priority, resolution_hours, reminder_hours) VALUES
(1, 'LOW', 72, '24,48'),
(2, 'MEDIUM', 48, '12,24'),
(3, 'HIGH', 24, '6,12'),
(4, 'CRITICAL', 8, '2,4');



INSERT INTO ticket 
(id, title, description, status, priority, sla_deadline, sla_breached, created_by, assigned_agent, created_at, updated_at)
VALUES
(1, 'Login Issue', 'User unable to login to the system', 'OPEN', 'HIGH',
 NOW() + INTERVAL 24 HOUR, false, 4, 2, NOW(), NOW()),

(2, 'Payment Failure', 'Payment gateway not processing transactions', 'IN_PROGRESS', 'CRITICAL',
 NOW() + INTERVAL 8 HOUR, false, 5, 3, NOW(), NOW()),

(3, 'Bug in Dashboard', 'Graphs not loading properly', 'OPEN', 'MEDIUM',
 NOW() + INTERVAL 48 HOUR, false, 4, 2, NOW(), NOW()),

(4, 'Password Reset', 'Customer cannot reset password', 'RESOLVED', 'LOW',
 NOW() + INTERVAL 72 HOUR, false, 5, 3, NOW(), NOW());



INSERT INTO comment (id, ticket_id, author_id, content, comment_type, created_at) VALUES
(1, 1, 4, 'I cannot login since morning', 'CUSTOMER', NOW()),
(2, 1, 2, 'We are investigating the issue', 'AGENT', NOW()),
(3, 2, 5, 'Payment fails every time', 'CUSTOMER', NOW()),
(4, 2, 3, 'Checking payment gateway logs', 'AGENT', NOW()),
(5, 3, 4, 'Dashboard graphs not loading', 'CUSTOMER', NOW());



INSERT INTO audit_log 
(id, ticket_id, updated_by, actor_type, action_type, old_value, new_value, created_at)
VALUES
(1, 1, 2, 'AGENT', 'STATUS_CHANGE', 'OPEN', 'IN_PROGRESS', NOW()),
(2, 1, 2, 'AGENT', 'ASSIGN_AGENT', 'NULL', 'agent_john', NOW()),
(3, 2, 3, 'AGENT', 'STATUS_CHANGE', 'OPEN', 'IN_PROGRESS', NOW()),
(4, 4, 3, 'AGENT', 'STATUS_CHANGE', 'IN_PROGRESS', 'RESOLVED', NOW());

