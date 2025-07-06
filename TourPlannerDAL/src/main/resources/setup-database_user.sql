-- Create database (if not exists)
-- Note: In PostgreSQL, you need to connect to a database first
-- This script assumes you're connected to the default 'postgres' database

-- Create database
CREATE DATABASE tourplannerdb;

-- Connect to the new database
\c tourplannerdb;

-- Tour table
CREATE TABLE IF NOT EXISTS tour (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    tour_description TEXT,
    from_location VARCHAR(255),
    to_location VARCHAR(255),
    transport_type VARCHAR(50),
    tour_distance DOUBLE PRECISION DEFAULT 0.0,
    estimated_time DOUBLE PRECISION DEFAULT 0.0,
    quick_notes TEXT,
    encoded_route_geometry TEXT,
    start_coords TEXT,
    end_coords TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Log table
CREATE TABLE IF NOT EXISTS log (
    id SERIAL PRIMARY KEY,
    tour_id INTEGER NOT NULL,
    date DATE NOT NULL,
    time TIME NOT NULL,
    comment TEXT,
    difficulty INTEGER DEFAULT 1,
    total_distance DOUBLE PRECISION DEFAULT 0.0,
    total_time TIME,
    rating INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tour_id) REFERENCES tour(id) ON DELETE CASCADE
);

-- Indexes for better performance
CREATE INDEX IF NOT EXISTS idx_tour_name ON tour(name);
CREATE INDEX IF NOT EXISTS idx_tour_locations ON tour(from_location, to_location);
CREATE INDEX IF NOT EXISTS idx_log_tour_id ON log(tour_id);
CREATE INDEX IF NOT EXISTS idx_log_date ON log(date);

-- Constraints for data integrity
ALTER TABLE log ADD CONSTRAINT chk_difficulty CHECK (difficulty >= 1 AND difficulty <= 5);
ALTER TABLE log ADD CONSTRAINT chk_rating CHECK (rating >= 1 AND rating <= 5);
ALTER TABLE tour ADD CONSTRAINT chk_distance CHECK (tour_distance >= 0);
ALTER TABLE tour ADD CONSTRAINT chk_estimated_time CHECK (estimated_time >= 0);

-- Create user with restricted privileges
-- Drop user if exists (PostgreSQL syntax)
DROP USER IF EXISTS tourplanner_user;

-- Create new user
CREATE USER tourplanner_user WITH PASSWORD '1234';

-- Grant only necessary privileges for tourplannerdb
GRANT CONNECT ON DATABASE tourplannerdb TO tourplanner_user;
GRANT USAGE ON SCHEMA public TO tourplanner_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO tourplanner_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO tourplanner_user;

-- Grant privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO tourplanner_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO tourplanner_user;

-- The user CANNOT:
-- - Access other databases (unless explicitly granted)
-- - Create/drop databases
-- - Create/drop users
-- - Access system catalogs (unless explicitly granted)
-- - Create schemas
-- - Kill processes

-- Views for frequent queries
CREATE OR REPLACE VIEW tour_with_stats AS
SELECT 
    t.*,
    COUNT(l.id) as log_count,
    AVG(l.rating) as avg_rating,
    AVG(l.difficulty) as avg_difficulty,
    SUM(l.total_distance) as total_logged_distance
FROM tour t
LEFT JOIN log l ON t.id = l.tour_id
GROUP BY t.id;

-- Grant access to the view
GRANT SELECT ON tour_with_stats TO tourplanner_user;

-- Insert sample data
INSERT INTO tour (name, tour_description, from_location, to_location, transport_type, tour_distance, estimated_time, quick_notes) VALUES
('Vienna to Salzburg', 'Scenic route through Austria', 'Vienna', 'Salzburg', 'Car', 295.5, 3.5, 'Beautiful mountain views'),
('City Walk Vienna', 'Historical city center tour', 'Stephansdom', 'Schönbrunn Palace', 'Walking', 12.3, 4.0, 'Don''t forget camera'),
('Danube Cycle Path', 'Cycling along the Danube', 'Melk', 'Dürnstein', 'Bicycle', 35.2, 2.5, 'Great for families')
ON CONFLICT (name) DO UPDATE SET 
    tour_description = EXCLUDED.tour_description,
    from_location = EXCLUDED.from_location,
    to_location = EXCLUDED.to_location;

INSERT INTO log (tour_id, date, time, comment, difficulty, total_distance, total_time, rating) VALUES
(1, '2024-06-15', '09:30:00', 'Perfect weather for driving', 2, 295.5, '03:45:00', 5),
(1, '2024-07-01', '10:00:00', 'Heavy traffic in Vienna', 3, 295.5, '04:15:00', 3),
(2, '2024-06-20', '14:00:00', 'Lots of tourists but enjoyable', 1, 12.3, '04:30:00', 4),
(3, '2024-07-05', '08:00:00', 'Early morning start, great views', 2, 35.2, '02:45:00', 5)
ON CONFLICT (tour_id, date, time) DO UPDATE SET 
    comment = EXCLUDED.comment,
    difficulty = EXCLUDED.difficulty,
    total_distance = EXCLUDED.total_distance,
    total_time = EXCLUDED.total_time,
    rating = EXCLUDED.rating;

-- Setup confirmation
SELECT 'Database and user setup completed successfully' as status;
SELECT COUNT(*) as tour_count FROM tour;
SELECT COUNT(*) as log_count FROM log;

-- Show permissions for the new user
SELECT 'Permissions for tourplanner_user:' as info;
\du tourplanner_user