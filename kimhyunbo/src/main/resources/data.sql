-- 테스트용 회원을 미리 넣어둔다. 비밀번호는 둘 다 'password1234'를 BCrypt로 암호화한 값이다.
INSERT INTO member (member_id, email, nickname, password, is_banned, created_at, updated_at)
VALUES (101, 'hyunbo@leets.com', '김현보', '$2a$10$MIAnc9mJuXkoHY3J97B/LOA3EjGvqmZ7vNdr9CgCloDgPR7XXCXYu', false, NOW(), NOW()),
       (102, 'leets@leets.com', '리츠', '$2a$10$MIAnc9mJuXkoHY3J97B/LOA3EjGvqmZ7vNdr9CgCloDgPR7XXCXYu', false, NOW(), NOW());
