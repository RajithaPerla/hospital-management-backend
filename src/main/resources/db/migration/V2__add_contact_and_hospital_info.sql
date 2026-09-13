-- =========================================================
-- V2__add_contact_and_hospital_info.sql
-- Adds:
--   1. contact_inquiries - messages submitted via the Contact Us form
--   2. hospital_info     - the hospital's own address/phone/hours,
--                          shown on the Contact Us & Directions pages
-- =========================================================

CREATE TABLE IF NOT EXISTS contact_inquiries (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    message     VARCHAR(2000) NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'NEW'
                    CHECK (status IN ('NEW', 'REVIEWED', 'RESOLVED')),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Contact form submissions are almost always queried "most recent first"
-- (e.g. an admin inbox view), so index on created_at descending.
CREATE INDEX IF NOT EXISTS idx_contact_inquiries_created_at ON contact_inquiries (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_contact_inquiries_status ON contact_inquiries (status);

-- ---------------------------------------------------------
-- hospital_info: a single-row table holding the hospital's own
-- public-facing contact details, so the address/phone/hours shown
-- on the Directions and Contact Us pages come from the database
-- instead of being hardcoded in the frontend.
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS hospital_info (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(150) NOT NULL,
    address_line1       VARCHAR(255) NOT NULL,
    address_line2       VARCHAR(255),
    city                VARCHAR(100) NOT NULL,
    state               VARCHAR(100) NOT NULL,
    postal_code         VARCHAR(20) NOT NULL,
    phone_number        VARCHAR(20) NOT NULL,
    email               VARCHAR(255),
    operating_hours     VARCHAR(500) NOT NULL,
    parking_info        VARCHAR(500),
    transit_info        VARCHAR(500),
    latitude            NUMERIC(9,6),
    longitude           NUMERIC(9,6),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

DROP TRIGGER IF EXISTS trg_hospital_info_updated_at ON hospital_info;
CREATE TRIGGER trg_hospital_info_updated_at
    BEFORE UPDATE ON hospital_info
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Seed the single hospital_info row (edit these values to your real details).
INSERT INTO hospital_info (
    name, address_line1, address_line2, city, state, postal_code,
    phone_number, email, operating_hours, parking_info, transit_info,
    latitude, longitude
)
SELECT
    'City Care Hospital', '123 Wellness Avenue', 'Suite 100', 'Springfield', 'IL', '62701',
    '+1 (555) 123-4567', 'info@citycarehospital.test',
    'Mon–Fri: 7:00 AM – 9:00 PM, Sat–Sun: 8:00 AM – 6:00 PM',
    'Free visitor parking available in Lot B, accessible from Wellness Avenue.',
    'Bus routes 12 and 47 stop directly outside the main entrance. Nearest metro: Springfield Central (0.4 mi).',
    39.781721, -89.650148
WHERE NOT EXISTS (SELECT 1 FROM hospital_info);
