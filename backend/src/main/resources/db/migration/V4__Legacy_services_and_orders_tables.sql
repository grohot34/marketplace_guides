-- Legacy tables for backward compatibility with old /services and /orders endpoints.
-- The app uses tours and bookings; these tables stay empty so existing code does not fail.

CREATE TABLE IF NOT EXISTS services (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(19, 2) NOT NULL,
    duration_minutes INTEGER,
    image_url VARCHAR(1000),
    category_id BIGINT NOT NULL,
    provider_id BIGINT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_service_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT fk_service_provider FOREIGN KEY (provider_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    provider_id BIGINT,
    scheduled_date_time TIMESTAMP NOT NULL,
    address TEXT,
    notes TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    total_price DECIMAL(19, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES users(id),
    CONSTRAINT fk_order_service FOREIGN KEY (service_id) REFERENCES services(id),
    CONSTRAINT fk_order_provider FOREIGN KEY (provider_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_services_category ON services(category_id);
CREATE INDEX IF NOT EXISTS idx_services_provider ON services(provider_id);
CREATE INDEX IF NOT EXISTS idx_orders_customer ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_service ON orders(service_id);
CREATE INDEX IF NOT EXISTS idx_orders_provider ON orders(provider_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
