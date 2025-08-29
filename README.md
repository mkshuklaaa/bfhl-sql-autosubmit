# BFHL SQL AutoSubmit (Spring Boot)

### What it does
- On startup:
  1) POSTs to `.../hiring/generateWebhook/JAVA` with your name/regNo/email.
  2) Reads `{ webhook, accessToken }` from the response.
  3) Decides **Question 1** (odd) or **Question 2** (even) using the last two digits of your `regNo`.
  4) Sends `{ "finalQuery": "<SQL>" }` to the **returned webhook URL** with header `Authorization: <accessToken>` (no `Bearer`).

### Configure
Edit `src/main/resources/application.yml`:
```yaml
app:
  name: "John Doe"
  regNo: "REG12347"
  email: "john@example.com"
