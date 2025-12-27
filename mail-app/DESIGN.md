# Mail App Backend Design

## Architecture Overview

This backend is built using **Quarkus 3.30.5** with **Java 21**, designed for a **single-user personal email system** with support for multiple sender identities and domains.	

- **Core Framework**: Quarkus (REST, Hibernate ORM, Panache)
- **Database**: MySQL
- **Email Provider**: Mailgun (API & Webhooks)
- **Authentication**: Custom Cookie-based auth via 3rd-party API verification
- **Architecture Style**: Layered (Resource -> Service -> Repository -> Entity)

---

## Authentication & Security

Since this is a personal project, authentication is handled via a custom mechanism:

1.  **Cookie Interception**: A `ContainerRequestFilter` intercepts all incoming requests to `/api/*`.
2.  **Validation**: It extracts a specific authentication cookie.
3.  **Verification**: The cookie value is sent to a configured **3rd Party API**.
    *   **Success**: API returns user info. Request proceeds. Context is set with the single user's details.
    *   **Failure**: Request is aborted with `401 Unauthorized` or redirected to the login page.
4.  **Single User Context**: While the system supports multiple "Identities" (From addresses), they all belong to the single authenticated owner.

---

## Database Schema / Entities

* Refer to the `entities/` directory for full entity definitions.

## REST API Endpoints

### Identity & Domain Management
- `GET /v1/identities` - List all sender identities
- `POST /v1/identities` - Create a new sender identity (e.g., "steve <aaa@hridaykh.in>")
- `GET /v1/domains` - List verified domains

### Email Operations
- `POST /v1/emails` - Compose/Send email (Requires `senderIdentityId`)
- `GET /v1/emails` - List emails (Filter by Folder, Search)
- `GET /v1/emails/{id}` - Get full email details
- `POST /v1/emails/{id}/reply` - Reply (Auto-selects correct `SenderIdentity` based on who the original email was sent to)

### Webhooks (Mailgun Integration)
- `POST /v1/webhooks/mailgun` - Endpoint for Mailgun to push incoming emails and events.
    - Verifies Mailgun signature.
    - Parses MIME content.
    - Saves to Database (INBOX).
    - Handles "Delivered", "Failed", "Spam" events.

---

## Service Layer Logic

### 1. Authentication Filter (`AuthFilter.java`)
```java
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {
    @Inject ThirdPartyAuthService authService;

    public void filter(ContainerRequestContext context) {
        Cookie cookie = context.getCookies().get("AUTH_SESSION");
        if (cookie == null || !authService.isValid(cookie.getValue())) {
            throw new NotAuthorizedException("Invalid session");
        }
        // Set SecurityContext
    }
}
```

### 2. Mailgun Service (`MailgunService.java`)
Handles interaction with Mailgun API.
- **Sending**: Uses Mailgun API (or SMTP) to send emails using the specific domain credentials.
- **Webhook Processing**:
    - Validates `timestamp`, `token`, and `signature` using the Mailgun signing key.
    - Extracts attachments and uploads them to local storage/S3.
    - Maps incoming JSON/MIME to `Email` entity.
    - Detects `In-Reply-To` headers to thread messages correctly.

### 3. Email Service (`EmailService.java`)
- **Sending Logic**:
    - User selects `SenderIdentity` (e.g., ID 1 = "hriday <alex@hridaykh.in>").
    - Service validates that `alex@hridaykh.in` is a valid identity.
    - Constructs email with correct `From` header.
    - Queues for sending via Mailgun.
- **Receiving Logic (via Webhook)**:
    - When email arrives for `alex@hridaykh.in`, Mailgun posts to webhook.
    - System identifies the recipient `alex@hridaykh.in`.
    - Stores email in INBOX.
    - (Optional) Tags it with the specific Identity for filtering.

---

## DTOs (Data Transfer Objects)

### EmailDTO
```java
public class EmailDTO {
    public String subject;
    public String body;
    public List<RecipientDTO> to;
    public List<RecipientDTO> cc;
    public List<RecipientDTO> bcc;
    public LocalDateTime scheduledAt; // optional
    public List<Long> attachmentIds;
    public String unsubscribeHeader; // optional
}
```

### RecipientDTO
```java
public class RecipientDTO {
    public String email;
    public String displayName;
}
```

### EmailResponseDTO
```java
public class EmailResponseDTO {
    public Long id;
    public String subject;
    public String body;
    public UserDTO sender;
    public List<RecipientResponseDTO> recipients;
    public List<AttachmentResponseDTO> attachments;
    public LocalDateTime sentAt;
    public String threadId;
    public EmailStatus status;
    public boolean isRead;
    public boolean isImportant;
    public boolean isArchived;
}
```

---

## Repositories (Panache)

```java
@ApplicationScoped
public class EmailRepository implements PanacheRepository<Email> {
    public List<Email> findByThreadId(String threadId) {
        return list("threadId = ?1 order by sentAt", threadId);
    }
    
    public List<Email> findScheduledEmails() {
        return list("status = ?1 and scheduledAt <= ?2", 
                    EmailStatus.SCHEDULED, LocalDateTime.now());
    }
}

@ApplicationScoped
public class UserEmailRepository implements PanacheRepository<UserEmail> {
    public PanacheQuery<UserEmail> findByUserAndFolder(Long userId, Long folderId) {
        return find("user.id = ?1 and folder.id = ?2 and isDeleted = false", 
                    userId, folderId);
    }
    
    public List<UserEmail> searchEmails(Long userId, String query) {
        return list("user.id = ?1 and isDeleted = false and " +
                   "(lower(email.subject) like ?2 or lower(email.body) like ?2)",
                   userId, "%" + query.toLowerCase() + "%");
    }
}

@ApplicationScoped
public class FolderRepository implements PanacheRepository<Folder> {
    public List<Folder> findByUser(Long userId) {
        return list("user.id = ?1 order by sortOrder", userId);
    }
    
    public Folder findByUserAndType(Long userId, FolderType type) {
        return find("user.id = ?1 and type = ?2", userId, type).firstResult();
    }
}

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {
    public User findByEmail(String email) {
        return find("email", email).firstResult();
    }
}

@ApplicationScoped
public class AttachmentRepository implements PanacheRepository<Attachment> {
    public List<Attachment> findByEmail(Long emailId) {
        return list("email.id", emailId);
    }
}
```

---

## Configuration (`application.properties`)

```properties
# Mailgun Configuration
mailgun.api.key=${MAILGUN_API_KEY}
mailgun.webhook.signing-key=${MAILGUN_WEBHOOK_KEY}
mailgun.domain.default=hridaykh.in

# 3rd Party Auth
auth.api.url=https://api.hridaykh.in/user/validate

# Storage
app.storage.location=/var/mail-app/data
```

## Development Roadmap

1.  **Setup**: Configure Quarkus with MySQL and Mailgun dependencies.
2.  **Auth**: Implement the Cookie -> API check filter.
3.  **Identities**: Create APIs to manage Domains and Sender Identities.
4.  **Webhooks**: Implement the Mailgun POST handler to receive emails.
5.  **Sending**: Implement sending logic using selected Identities.
6.  **Frontend**: Update UI to allow selecting "From" address in Compose window.
