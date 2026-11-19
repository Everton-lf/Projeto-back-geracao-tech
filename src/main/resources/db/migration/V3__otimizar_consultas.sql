-- Índices de apoio para buscas e filtros mais frequentes no catálogo.
CREATE INDEX idx_products_enabled ON products(enabled);
CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_categories_name ON category(name);
CREATE INDEX idx_categories_use_in_menu ON category(use_in_menu);
