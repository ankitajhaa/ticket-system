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