# Mail App API Documentation

## Webhooks

### **POST** `/webhooks/mailgun`
- Receive incoming email events from Mailgun

**Incoming Payload:** mailgun webhook multipart/form encoded payload

**Response:** `200 OK` on successful processing
```json
{
  "status": "success",
  "message": "Webhook received and processed"
}
```

---

## Folders

**GET** `/v1/folders`
- List all folders

**POST** `/v1/folders`
- Create a new folder

**GET** `/v1/folders/{folder_id}`
- Get folder details

**PATCH** `/v1/folders/{folder_id}`
- Update folder (name, color, type)

**DELETE** `/v1/folders/{folder_id}`
- Delete a folder

---

## Emails in Folders

**GET** `/v1/folders/{folder_id}/emails`
- List all emails in a folder
- Query params: `?status=SENT&isRead=false`

**GET** `/v1/folders/{folder_id}/emails/{email_id}`
- Get specific email in a folder

> all other email operations are done via direct email endpoints below

---

## Attachments

**GET** `/v1/folders/{folder_id}/emails/{email_id}/attachments`
- List attachments for an email

```json
[
  {
    "id": 1,
    "fileName": "document.pdf",
    "mimeType": "application/pdf",
    "fileSizeBytes": 102400,
    "fileUrl": "SIGNED DIRECT OCI DOWNLOAD URL"
  },
  {
    "id": 2,
    "fileName": "image.jpg",
    "mimeType": "image/jpeg",
    "fileSizeBytes": 204800,
    "fileUrl": "SIGNED DIRECT OCI DOWNLOAD URL"
  }
]
```

> no other operation other than this is supported for attachments as they are read-only with emails

---

## Emails (Direct Access)

**GET** `/v1/emails`
- List all emails across folders
- email query params:
	- offset (default: 0)
	- limit (default: 20, max: 100)
	- sortby (subject, textBody, to, fromEmail, fromName, senderAddress, senderIdentity.displayName, senderIdentity.email, timestamp)
	- isRead
	- isArchived
	- subject query
	- body query
	- subject-body combined query
	- startDate
	- endDate
	- parentEmail
	- hasParentEmail
	- mailgunMessageId
	- emailStatus
	- hasAttachments
	- folderIds (comma-separated list of folder IDs)

	> to get deleted/sent/spam emails, get the special folder of respectiv types


**POST** `/v1/emails`
- Create/send new email

**POST** `/v1/emails/draft`
- Create/save new draft email

**GET** `/v1/emails/{email_id}`
- Get email details

**PUT** `/v1/emails/{email_id}`
- Update email or send email draft

**DELETE** `/v1/emails/{email_id}`
- Delete email

**GET** `/v1/emails/{email_id}/raw/text`
- Get raw body text

**GET** `/v1/emails/{email_id}/raw/html`
- Get raw body HTML

---

## Domains

### **GET** `/v1/domains`
- List all domains

#### Responses

- **200 OK**
```json
[
  {
    "id": 1,
    "name": "hridaykh.in",
    "mailgunRegion": "EU",
    "createdAt": "2025-12-30T15:46:49.917791"
  },
  {
    "id": 2,
    "name": "auth.hridaykh.in",
    "mailgunRegion": "EU",
    "createdAt": "2025-12-30T15:55:47.380432"
  }
]
```

### **POST** `/v1/domains`
- Add new domain

#### Request body
```json
{
  "name": "AUTH.hridAykh.in",
  "mailgunRegion": "EU"
}
```

#### Responses

- **201 Created**
```json
{
  "id": 51,
  "name": "auth.hridaykh.in",
  "mailgunRegion": "EU",
  "createdAt": "2025-12-30T15:55:47.380432"
}
```

- **409 Conflict**
```json
{
  "error": "Domain with name auth.hridaykh.in already exists."
}
```

- **400 Bad Request**
```json
{
  "error": "empty Domain name not allowed"
}
```

### **GET** `/v1/domains/{domain_id}`
- Get domain details

- Path parameters:
  - `domain_id` (required): positive integer ID of the domain

#### Responses

- **200 OK**
```json
{
  "id": 2,
  "name": "auth.hridaykh.in",
  "mailgunRegion": "EU",
  "createdAt": "2025-12-30T15:55:47.380432"
}
```

- **400 Bad Request** (examples)

Missing `domain_id`:
```json
{ "error": "Domain ID is required" }
```

Invalid format:
```json
{ "error": "Invalid domain ID format: must be a number" }
```

Non-positive ID:
```json
{ "error": "Domain ID must be a positive number" }
```

- **404 Not Found**
```json
{ "error": "Domain not found" }
```

### **PATCH** `/v1/domains/{domain_id}`
- Update domain (name, mailgunRegion)

#### Request body (partial updates allowed)
```json
{
  "name": "new.example.com",
  "mailgunRegion": "EU"
}
```

#### Responses

- **200 OK** (returns updated domain)
```json
{
  "id": 2,
  "name": "new.example.com",
  "mailgunRegion": "EU",
  "createdAt": "2025-12-30T15:55:47.380432"
}
```

- **400 Bad Request** (examples)
```json
{ "error": "Request body is required" }
```

```json
{ "error": "empty Domain name not allowed" }
```

- **409 Conflict**
```json
{ "error": "Domain with name new.example.com already exists." }
```

### **DELETE** `/v1/domains/{domain_id}`
- Remove domain

#### Responses

- **200 OK**
```json
{
    "status": "deleted",
    "id": 3,
    "name": "a.hridaykh.in",
    "mailgunRegion": "US",
    "createdAt": "2026-01-01T14:57:18.278322"
}
```

- **400 Bad Request** (invalid or missing id)
```json
{ "error": "Domain ID is required" }
```

```json
{ "error": "Invalid domain ID format: must be a number" }
```

- **404 Not Found**
```json
{ "error": "Domain not found" }
```

---

## Sender Identities

### Domain-scoped

#### **GET** `/v1/domains/{domain_id}/senders`
- List sender identities for a domain

##### Responses

- **200 OK**
```json
[
  {
    "id": 1,
    "email": "h.khanna",
    "displayName": "Hriday Khanna",
    "isDefault": false
  },
  {
    "id": 2,
    "email": "hk.khanna",
    "displayName": "Hriday Khanna",
    "isDefault": true
  }
]
```

#### **POST** `/v1/domains/{domain_id}/senders`
- Create sender identity for the domain

##### Request
```json
{
  "email": "new.sender",
  "displayName": "New Sender",
  "isDefault": false
}
```

##### Responses

- **201 Created**
```json
{
  "id": 42,
  "email": "new.sender",
  "displayName": "New Sender",
  "isDefault": false
}
```

- **400 Bad Request**
```json
{ "error": "email is required" }
```

- **409 Conflict**
```json
{ "error": "Sender with email new.sender already exists for this domain" }
```

#### **GET** `/v1/domains/{domain_id}/senders/{sender_id}`
- Get identity details

##### Responses

- **200 OK**
```json
{
  "id": 2,
  "email": "hk.khanna",
  "displayName": "Hriday Khanna",
  "isDefault": true
}
```

- **404 Not Found**
```json
{ "error": "Sender not found" }
```

#### **PATCH** `/v1/domains/{domain_id}/senders/{sender_id}`
- Update identity (display name, default status)

##### Request
```json
{
  "displayName": "Updated Name",
  "isDefault": true
}
```

##### Responses

- **200 OK**
```json
{
  "id": 2,
  "email": "hk.khanna",
  "displayName": "Updated Name",
  "isDefault": true
}
```

- **400 Bad Request**
```json
{ "error": "Invalid sender id format" }
```

#### **DELETE** `/v1/domains/{domain_id}/senders/{sender_id}`
- Remove sender identity

##### Responses

- **200 OK**
```json
{ "status": "deleted", "id": 2 }
```

### Global endpoints

#### **GET** `/v1/senders`
- List sender identities across domains

##### Responses

- **200 OK**
```json
[
  {
    "id": 1,
    "email": "h.khanna",
    "displayName": "Hriday Khanna",
    "isDefault": false
  },
  {
    "id": 2,
    "email": "hk.khanna",
    "displayName": "Hriday Khanna",
    "isDefault": true
  }
]
```

#### **GET** `/v1/senders/{sender_id}`
- Get sender identity by id

##### Responses

- **200 OK**
```json
{
  "id": 2,
  "email": "hk.khanna",
  "displayName": "Hriday Khanna",
  "isDefault": true
}
```

- **404 Not Found**
```json
{ "error": "Sender not found" }
```

#### **PATCH** `/v1/senders/{sender_id}`
- Update identity (display name, default status)

##### Request
```json
{ "displayName": "New Name", "isDefault": false }
```

##### Responses

- **200 OK**
```json
{
  "id": 2,
  "email": "hk.khanna",
  "displayName": "New Name",
  "isDefault": false
}
```

#### **DELETE** `/v1/senders/{sender_id}`
- Remove a sender identity

##### Responses

- **200 OK**
```json
{ "status": "deleted", "id": 2 }
```
