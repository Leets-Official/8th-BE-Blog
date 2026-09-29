# 01 — Application Startup & Health Check API

**What to build:** Configure the Spring Boot application environment so that it starts successfully without database connectivity, and provide an operational health check endpoint (`GET /health`) returning `"ok"`.

**Blocked by:** None — can start immediately

**Status:** resolved

- [x] Spring Boot application boots cleanly without requiring active MySQL credentials or DataSource configuration.
- [x] `GET /health` endpoint responds with HTTP 200 OK and body `"ok"`.
- [x] Controller routes the request and relies on the Service layer to obtain the health status.
- [x] Automated MockMvc test verifies the `GET /health` endpoint response and status.
