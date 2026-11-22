MERGE INTO member (member_id, nickname, password, email, role, created_at, updated_at)
    KEY (member_id)
    VALUES (1, 'testuser', 'test1234!', 'test@test.com','MEMBER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO member (member_id, nickname, password, email,role, created_at, updated_at)
    KEY (member_id)
    VALUES (2, 'testuser2', 'test1234!', 'test2@test.com', 'MEMBER',CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
