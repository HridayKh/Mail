POST /webhooks/mailgun
	- Receive incoming email events from Mailgun

GET /v1/emails
	- List all emails

GET /v1/emails/{email_id}
	- Get details of a specific email

GET /v1/emails/{email_id}/attachments
	- List attachments for an email

GET /v1/emails/{email_id}/attachments/{attachment_id}
	- Download a specific attachment

GET /v1/emails/{email_id}/raw
	- Get raw body (text or HTML) of an email

DELETE /v1/emails/{email_id}
	- Delete an email
