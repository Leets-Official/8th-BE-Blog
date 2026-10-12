-- 회원 가입 API가 없으므로, 게시글 작성에 사용할 회원을 미리 넣어둔다.
INSERT INTO member (member_id, email, name, password, tel, is_banned, created_at, updated_at)
VALUES (101, 'hyunbo@leets.com', '김현보', 'password', '010-1111-1111', false, NOW(), NOW()),
       (102, 'leets@leets.com', '리츠', 'password', '010-2222-2222', false, NOW(), NOW());
