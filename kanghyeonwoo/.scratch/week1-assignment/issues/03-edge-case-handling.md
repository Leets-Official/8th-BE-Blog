# 03 — Input Validation & Edge Case Handling

**What to build:** Robust handling for edge cases when calling `POST /string/repeat`, ensuring empty strings, null values, or malformed JSON payloads are handled safely without unhandled runtime exceptions.

**Blocked by:** 02 — Repeat String API (POST /string/repeat)

**Status:** resolved

- [x] Handle null or missing `value` in the request body gracefully with predictable response values.
- [x] Handle empty strings (`""`) or whitespace strings correctly.
- [x] Automated tests cover edge cases (null value, empty string).
