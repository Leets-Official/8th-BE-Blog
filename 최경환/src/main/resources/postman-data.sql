INSERT INTO user (id, name, created_at, updated_at)
VALUES (1, 'Postman Owner', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user (id, name, created_at, updated_at)
VALUES (2, 'Postman Other User', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO post (id, title, content, is_private, user_id, created_at, updated_at)
VALUES (50, 'Soft delete test post', 'Disposable seeded post', false, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO comment (id, content, user_id, post_id, created_at, updated_at)
VALUES (70, 'Disposable seeded comment', 1, 50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
