-- =========================================================
-- V1__init_schema.sql
-- Hospital Management System - Initial schema
-- Target: PostgreSQL 15+ (Neon.tech)
-- =========================================================

-- Extension for UUID generation (Neon supports pgcrypto by default)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ---------------------------------------------------------
-- USERS TABLE
-- Stores both PATIENT and DOCTOR (and ADMIN) accounts.
-- Identity comes from Firebase (Google OAuth) - firebase_uid is unique.
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL PRIMARY KEY,
    firebase_uid    VARCHAR(128) NOT NULL UNIQUE,
    email           VARCHAR(255) NOT NULL UNIQUE,
    full_name       VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL DEFAULT 'PATIENT'
                        CHECK (role IN ('PATIENT', 'DOCTOR', 'ADMIN')),
    department      VARCHAR(100),          -- only relevant when role = DOCTOR
    phone_number    VARCHAR(20),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Lookups happen by firebase_uid on every authenticated request,
-- and by email/role when listing doctors per department.
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_firebase_uid ON users (firebase_uid);
CREATE INDEX IF NOT EXISTS idx_users_role_department ON users (role, department);

-- ---------------------------------------------------------
-- APPOINTMENTS TABLE
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS appointments (
    id              BIGSERIAL PRIMARY KEY,
    patient_id      BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    doctor_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    department      VARCHAR(100) NOT NULL,
    appointment_date DATE NOT NULL,
    time_slot       VARCHAR(20) NOT NULL,   -- e.g. '09:00-09:30'
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED')),
    reason          VARCHAR(500),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    -- Prevent double-booking the same doctor for the same slot
    CONSTRAINT uq_doctor_slot UNIQUE (doctor_id, appointment_date, time_slot)
);

-- Query patterns to optimize for (10k+ rows):
--   1. "My appointments" for a patient, newest first, paginated -> (patient_id, appointment_date DESC)
--   2. Doctor's schedule for a given day -> (doctor_id, appointment_date)
--   3. Admin/status filtering -> (status)
CREATE INDEX IF NOT EXISTS idx_appt_patient_date ON appointments (patient_id, appointment_date DESC);
CREATE INDEX IF NOT EXISTS idx_appt_doctor_date   ON appointments (doctor_id, appointment_date);
CREATE INDEX IF NOT EXISTS idx_appt_status         ON appointments (status);

-- Keep updated_at fresh automatically
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_users_updated_at ON users;
CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_appointments_updated_at ON appointments;
CREATE TRIGGER trg_appointments_updated_at
    BEFORE UPDATE ON appointments
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
