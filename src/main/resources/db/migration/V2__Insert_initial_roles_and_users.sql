INSERT INTO roles (name) VALUES
('ROLE_USER'),
('ROLE_ADMIN')
ON CONFLICT (name) DO NOTHING;

INSERT INTO users (username, email, password, first_name, last_name, is_active) VALUES
('admin', 'admin@zi.local', '$2a$12$8i9zJ4lrqraFpdnFJYDH8O3RtbrdRGF7qZi8yt9JWYsH4kcc8zR/S', 'Admin', 'System', true),
('user1', 'user1@zi.local', '$2a$12$8i9zJ4lrqraFpdnFJYDH8O3RtbrdRGF7qZi8yt9JWYsH4kcc8zR/S', 'Default', 'User', true)
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'user1' AND r.name = 'ROLE_USER'
ON CONFLICT (user_id, role_id) DO NOTHING;
