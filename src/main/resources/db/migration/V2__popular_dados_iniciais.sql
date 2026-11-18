INSERT INTO users (firstname, surname, email, password, created_at, updated_at) VALUES
('Ana', 'Silva', 'ana.silva@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NOW(), NOW()),
('Carlos', 'Souza', 'carlos.souza@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NOW(), NOW()),
('Mariana', 'Oliveira', 'mariana.oliveira@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NOW(), NOW()),
('Admin', 'Sistema', 'admin@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NOW(), NOW());

INSERT INTO category (name, slug, use_in_menu, created_at, updated_at) VALUES
('Tênis', 'tenis', 1, NOW(), NOW()),
('Camisetas', 'camisetas', 1, NOW(), NOW()),
('Calças', 'calcas', 1, NOW(), NOW()),
('Bonés', 'bones', 1, NOW(), NOW()),
('Headphones', 'headphones', 1, NOW(), NOW()),
('Acessórios', 'acessorios', 0, NOW(), NOW());