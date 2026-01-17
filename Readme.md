# URL Shortener Project Assigment 2

This project is a feature-rich URL shortener implemented in Java using Spring Boot. It supports 
1) custom short codes,
2) expiry dates,
3) analytics,
4) user roles,
5) batch creation, and
6) password protection.

## Assignment Answers

### Q1: Previous 10 Shortened URLs
**SQL Query:**
```sql
SELECT original_url, short_code, created_at 
FROM urls 
ORDER BY created_at DESC 
LIMIT 10;
```
**Real-time Analytics:**
For real-time analytics, we would use a streaming platform (like Apache Kafka or RabbitMQ) to push events to a specialized time-series database (like InfluxDB or ClickHouse) or an analytics dashboard (like Grafana). Querying a transactional SQL database for high-volume real-time analytics can degrade performance.

### Q2: Empty URL Handling
When an empty URL is provided, the API returns:
- **Status Code:** `400 Bad Request`
- **Error Message:** "URL cannot be empty"
- **Implementation:** Validated in `UrlServiceImpl.java`.

### Q3: Click Tracking & Popular URLs
**Schema Change:**
- `visits` (Long): Incremented on every redirect.
- `last_accessed_at` (Timestamp): Updated to `NOW()` on every redirect.

**SQL Query (Top 10 Popular):**
```sql
SELECT short_code, visits 
FROM urls 
ORDER BY visits DESC, last_accessed_at DESC 
LIMIT 10;
```

### Q4: Multiple Short Codes for Same URL (Top 10)
**SQL Query:**
```sql
SELECT original_url, COUNT(*) as usage_count
FROM urls
GROUP BY original_url
ORDER BY usage_count DESC
LIMIT 10;
```

### Q5: User Management & API Keys
We implemented a `users` table and linked it to `urls` via `user_id`. API Keys are passed in headers to authenticate requests.

### Q6: Issues with API Keys
1.  **Security Risk:** If leaked, anyone can impersonate the user until the key is rotated.
2.  **No Granular Control:** Usually gives full access to the account, unlike scoped tokens (OAuth).
3.  **Hard to Rotate:** Requires updating all clients using the key manually.
4.  **No Expiry:** Unlike JWTs, they valid indefinitely unless manually revoked.

### Q7: Other User Management Ways
1.  **OAuth2 / OpenID Connect:** Delegated authorization (e.g., "Login with Google").
2.  **JWT (JSON Web Tokens):** Stateless authentication with built-in expiry (Implemented and used in this project).
3.  **Session-based Authentication:** Server-side sessions (traditional cookies).
4.  **mTLS (Mutual TLS):** Certificate-based authentication for machine-to-machine.

### Q8: Expiry Date
Implemented `expiryDate` column.
- **Creation:** Users can pass an optional `expiryDate`.
- **Redirect:** Code checks `IF expiryDate < NOW() THEN return 410 GONE`.

### Q9: Custom Short Codes
Implemented optional `customCode` parameter in `/shorten`.
- **Logic:** Checks if `customCode` exists. If yes -> 409 Conflict. If no -> Assigns logic.

### Q10: Batch Creation
**Choice:** Created a new endpoint `/shorten/batch`.
- **Pros of separate endpoint:** Clearer contract (`List<String>` input), optimized bulk processing, doesn't clutter the single-creation logic.
- **Error Handling:** Returns a list of results where each item has a status (`SUCCESS`/`FAILURE`) and error message if applicable. This ensures one failure doesn't block the entire batch.

### Q11: Authentication vs Authorization
- **Authentication (Who are you?):** Verifying the identity of a user (e.g., logging in with password).
- **Authorization (What can you do?):** Verifying if the authenticated user has permission to perform an action (e.g., only "ENTERPRISE" users can use batch shortening).

### Q12: Pricing Tiers (RBAC)
Implemented `Role` enum (`HOBBY`, `ENTERPRISE`).
- **Batch Endpoint:** Checks `if (user.role == ENTERPRISE)`. If not -> `403 Forbidden`.

### Q13: Edit & Deactivate
Implemented `/edit/{shortCode}`.
- Users can update `expiryDate` to the past to deactivate a link.
- Users can also update the `customCode` or `password`.

### Q14: Password Protection
Added `password` column.
- **Set:** Optional field in `UrlRequestDto`.
- **Access:** `/redirect` endpoint accepts `?password=...`.
- **Logic:** If URL has password and request password matches -> Redirect. Else -> 401/403 Error.

### Q15: List All URLs
Implemented `GET /users/{userId}` to list all URLs belonging to a specific user.

### Q16 [BONUS]: Health Endpoint
Implemented `/health` endpoint.
- **Checks:** Database connectivity logic.
- **Response:** `{"status": "UP", "database": "UP"}`.
- **Security:** Whitelisted to be public.

### Q19 [BONUS]: Custom Domains
To support custom domains (e.g., `go.userdomain.com/abc`), we would implement the current architecture:
1.  **DNS CNAME Record:** The user points their custom domain (CNAME) to our application's valid domain/load balancer.
2.  **SSL/TLS Provisioning:** We utilize a service (like Let's Encrypt) to dynamically generate and serve SSL certificates for the user's custom domain to ensure HTTPS security.
3.  **Host Header Logic:** The backend reads the HTTP `Host` header of the incoming request to identify if the traffic is coming from a custom domain or our default domain.
4.  **Database Scoping:** The lookup query is strictly scoped by domain (`SELECT * FROM urls WHERE short_code = ? AND domain = ?`), ensuring collisions between the same short code on different domains are impossible.
