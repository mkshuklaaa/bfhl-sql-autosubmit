# Bajaj Finserv Health | Qualifier 1 | JAVA

Spring Boot app that:
- On startup, calls `generateWebhook` to get a webhook and `accessToken`.
- Solves the SQL problem (see below) and stores the final SQL query.
- Submits `{ "finalQuery": "..." }` to the returned webhook URL using the JWT token in the `Authorization` header.
- If no webhook is returned, it falls back to `testWebhook/JAVA` per the brief.

## Run

```bash
# Build
mvn -q -DskipTests package


BFH_NAME="Manish Kumar Shukla" BFH_REGNO="22BCY10065" BFH_EMAIL="manishkumarshukla2022@vitbhopal.ac.in" java -jar target/bajaj-finserv-health-solution-1.0.0.jar
```

Environment variables:
- `BFH_NAME` (default: `Manish Kumar Shukla`)
- `BFH_REGNO` (default: `22BCY10065`)
- `BFH_EMAIL` (default: `manishkumarshukla2022@vitbhopal.ac.in`)
- `BFH_USE_BEARER` set to `true` to prefix the token with `Bearer ` (default: **false**, as per spec).

## Endpoints used (as per problem)

- POST `https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA`
  Body:
  ```json
  { "name": "Manish Kumar Shukla", "regNo": "22BCY10065", "email": "manishkumarshukla2022@vitbhopal.ac.in" }
  ```
  Response (example):
  ```json
  { "webhook": "https://...", "accessToken": "<jwt>" }
  ```

- POST `<webhook from response>` (or `https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA`)
  Headers:
  ```
  Authorization: <accessToken>
  Content-Type: application/json
  ```
  Body:
  ```json
  { "finalQuery": "YOUR_SQL_QUERY_HERE" }
  ```

## SQL Problem & Final Query

**Problem:** Find the highest salary credited **not on the 1st day of any month** and return:
- `SALARY`
- `NAME` = `FIRST_NAME` + ' ' + `LAST_NAME`
- `AGE` = integer years between today and `DOB`
- `DEPARTMENT_NAME`

**Final SQL (PostgreSQL):**

```sql
SELECT 
  p.amount AS salary,
  e.first_name || ' ' || e.last_name AS name,
  EXTRACT(YEAR FROM age(current_date, e.dob))::int AS age,
  d.department_name
FROM payments p
JOIN employee e ON e.emp_id = p.emp_id
JOIN department d ON d.department_id = e.department
WHERE EXTRACT(DAY FROM p.payment_time) <> 1
ORDER BY p.amount DESC
LIMIT 1;
```

If you need MySQL instead, use:

```sql
SELECT 
  p.amount AS salary,
  CONCAT(e.first_name, ' ', e.last_name) AS name,
  TIMESTAMPDIFF(YEAR, e.dob, CURDATE()) AS age,
  d.department_name
FROM payments p
JOIN employee e ON e.emp_id = p.emp_id
JOIN department d ON d.department_id = e.department
WHERE DAY(p.payment_time) <> 1
ORDER BY p.amount DESC
LIMIT 1;
```

## Submission Checklist

- Public GitHub repo with code and JAR (you can upload `target/bfh-qualifier1-java-1.0.0.jar`).
- Public JAR download link.
- Fill this form: https://forms.office.com/r/5Kzb1h7fre

---

This project contains no controller endpoints; the flow is triggered on app startup.
