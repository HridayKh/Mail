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

### **PUT** `/v1/domains/{domain_id}`
- Update domain (verify status, region)

### **DELETE** `/v1/domains/{domain_id}`
- Remove domain

---

## Sender Identities (Domain-Scoped)

**GET** `/v1/domains/{domain_id}/identities`
- List sender identities for a domain

**POST** `/v1/domains/{domain_id}/identities`
- Create sender identity

**GET** `/v1/domains/{domain_id}/identities/{identity_id}`
- Get identity details

**PUT** `/v1/domains/{domain_id}/identities/{identity_id}`
- Update identity (display name, default status)

**DELETE** `/v1/domains/{domain_id}/identities/{identity_id}`
- Remove sender identity


