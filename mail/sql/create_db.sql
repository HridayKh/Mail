-- Users table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_login DATETIME
);

-- Email folders (Inbox, Sent, etc.)
CREATE TABLE folders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Emails table
CREATE TABLE emails (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL, -- Owner of the email
    folder_id INT,        -- Folder (Inbox, Sent, etc.)
    sender VARCHAR(255) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    body TEXT,
    sent_at DATETIME,
    received_at DATETIME,
    is_read BOOLEAN DEFAULT FALSE,
    is_starred BOOLEAN DEFAULT FALSE,
    is_deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (folder_id) REFERENCES folders(id) ON DELETE SET NULL
);

-- Email labels (for tagging emails)
CREATE TABLE labels (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(7), -- HEX color code
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Email-label mapping (many-to-many)
CREATE TABLE email_labels (
    email_id INT NOT NULL,
    label_id INT NOT NULL,
    PRIMARY KEY (email_id, label_id),
    FOREIGN KEY (email_id) REFERENCES emails(id) ON DELETE CASCADE,
    FOREIGN KEY (label_id) REFERENCES labels(id) ON DELETE CASCADE
);

-- Attachments table
CREATE TABLE attachments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email_id INT NOT NULL,
    filename VARCHAR(255) NOT NULL,
    file_url VARCHAR(512) NOT NULL,
    mime_type VARCHAR(100),
    size INT,
    uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (email_id) REFERENCES emails(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_user_email ON emails(user_id, recipient, sender);
CREATE INDEX idx_email_folder ON emails(folder_id);
CREATE INDEX idx_email_read ON emails(is_read);
CREATE INDEX idx_attachment_email ON attachments(email_id);