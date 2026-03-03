-- Create categories table
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    icon VARCHAR(255)
);

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    phone VARCHAR(255),
    address TEXT,
    role VARCHAR(50) NOT NULL DEFAULT 'CUSTOMER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    bio TEXT,
    languages VARCHAR(500),
    certifications VARCHAR(500),
    average_rating DOUBLE PRECISION DEFAULT 0.0,
    total_ratings INTEGER DEFAULT 0
);

-- Create tours table
CREATE TABLE IF NOT EXISTS tours (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price_per_person DECIMAL(19, 2) NOT NULL,
    duration_hours INTEGER NOT NULL,
    max_participants INTEGER NOT NULL,
    location VARCHAR(500),
    meeting_point VARCHAR(500),
    itinerary TEXT,
    image_url VARCHAR(1000),
    category_id BIGINT NOT NULL,
    guide_id BIGINT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    average_rating DOUBLE PRECISION DEFAULT 0.0,
    total_ratings INTEGER DEFAULT 0,
    CONSTRAINT fk_tour_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT fk_tour_guide FOREIGN KEY (guide_id) REFERENCES users(id)
);

-- Create bookings table
CREATE TABLE IF NOT EXISTS bookings (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    tour_id BIGINT NOT NULL,
    guide_id BIGINT NOT NULL,
    tour_date_time TIMESTAMP NOT NULL,
    number_of_participants INTEGER NOT NULL,
    contact_phone VARCHAR(255),
    special_requests TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    total_price DECIMAL(19, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    CONSTRAINT fk_booking_customer FOREIGN KEY (customer_id) REFERENCES users(id),
    CONSTRAINT fk_booking_tour FOREIGN KEY (tour_id) REFERENCES tours(id),
    CONSTRAINT fk_booking_guide FOREIGN KEY (guide_id) REFERENCES users(id)
);

-- Create reviews table
CREATE TABLE IF NOT EXISTS reviews (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    guide_id BIGINT NOT NULL,
    tour_id BIGINT NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_review_customer FOREIGN KEY (customer_id) REFERENCES users(id),
    CONSTRAINT fk_review_guide FOREIGN KEY (guide_id) REFERENCES users(id),
    CONSTRAINT fk_review_tour FOREIGN KEY (tour_id) REFERENCES tours(id)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_tours_category ON tours(category_id);
CREATE INDEX IF NOT EXISTS idx_tours_guide ON tours(guide_id);
CREATE INDEX IF NOT EXISTS idx_tours_active ON tours(active);
CREATE INDEX IF NOT EXISTS idx_bookings_customer ON bookings(customer_id);
CREATE INDEX IF NOT EXISTS idx_bookings_tour ON bookings(tour_id);
CREATE INDEX IF NOT EXISTS idx_bookings_guide ON bookings(guide_id);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON bookings(status);
CREATE INDEX IF NOT EXISTS idx_reviews_guide ON reviews(guide_id);
CREATE INDEX IF NOT EXISTS idx_reviews_tour ON reviews(tour_id);
CREATE INDEX IF NOT EXISTS idx_reviews_booking ON reviews(booking_id);
