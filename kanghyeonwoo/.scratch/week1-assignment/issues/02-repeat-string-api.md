# 02 — Repeat String API (POST /string/repeat)

**What to build:** An endpoint (`POST /string/repeat`) that takes a JSON payload with a string `value` and returns a JSON response duplicating the string into `string_one` and `string_two` fields.

**Blocked by:** 01 — Application Startup & Health Check API

**Status:** resolved

- [x] `RepeatStringRequest` DTO correctly parses JSON body with property `value`.
- [x] `RepeatStringResponse` DTO serializes properties to JSON fields `string_one` and `string_two`.
- [x] `POST /string/repeat` accepts valid JSON and returns HTTP 200 with the expected duplicate string fields.
- [x] Business logic for string replication is encapsulated strictly in `AssignmentService` rather than `AssignmentController`.
- [x] Automated MockMvc test validates request/response payload mapping and serialization.
