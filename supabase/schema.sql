-- ==============================================================================
-- CHECKD HACKATHON EVENT ATTENDANCE SYSTEM
-- Supabase PostgreSQL Schema & Functions
-- ==============================================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. PARTICIPANTS TABLE
CREATE TABLE IF NOT EXISTS participants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usn VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    qr_token VARCHAR(255) NOT NULL UNIQUE,
    registration_status VARCHAR(50) DEFAULT 'confirmed',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Index for instant QR token lookup
CREATE INDEX IF NOT EXISTS idx_participants_qr_token ON participants(qr_token);
CREATE INDEX IF NOT EXISTS idx_participants_usn ON participants(usn);

-- 2. EVENTS TABLE
CREATE TABLE IF NOT EXISTS events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    event_date DATE DEFAULT CURRENT_DATE,
    start_time TIME,
    end_time TIME,
    status VARCHAR(50) DEFAULT 'open', -- 'open', 'closed'
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_events_status ON events(status);

-- 3. EVENT ELIGIBILITY TABLE
CREATE TABLE IF NOT EXISTS event_eligibility (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    participant_id UUID NOT NULL REFERENCES participants(id) ON DELETE CASCADE,
    CONSTRAINT uq_event_participant UNIQUE (event_id, participant_id)
);

CREATE INDEX IF NOT EXISTS idx_eligibility_event ON event_eligibility(event_id);
CREATE INDEX IF NOT EXISTS idx_eligibility_participant ON event_eligibility(participant_id);

-- 4. USERS (ADMINS / VOLUNTEERS)
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) DEFAULT 'volunteer', -- 'admin', 'volunteer'
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. ATTENDANCE RECORDS TABLE
CREATE TABLE IF NOT EXISTS attendance_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    participant_id UUID NOT NULL REFERENCES participants(id) ON DELETE CASCADE,
    checked_in_at TIMESTAMPTZ DEFAULT NOW(),
    checked_in_by UUID REFERENCES users(id) ON DELETE SET NULL,
    status VARCHAR(50) DEFAULT 'present',
    CONSTRAINT uq_attendance_event_participant UNIQUE (event_id, participant_id)
);

CREATE INDEX IF NOT EXISTS idx_attendance_event ON attendance_records(event_id);
CREATE INDEX IF NOT EXISTS idx_attendance_participant ON attendance_records(participant_id);
CREATE INDEX IF NOT EXISTS idx_attendance_checked_in_at ON attendance_records(checked_in_at DESC);

-- 6. ATTENDANCE AUDIT LOGS
CREATE TABLE IF NOT EXISTS attendance_audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attendance_id UUID NOT NULL REFERENCES attendance_records(id) ON DELETE CASCADE,
    old_status VARCHAR(50),
    new_status VARCHAR(50),
    modified_by UUID REFERENCES users(id) ON DELETE SET NULL,
    modified_at TIMESTAMPTZ DEFAULT NOW()
);

-- 7. SYNC METADATA TABLE (For Offline-First Delta Detection)
CREATE TABLE IF NOT EXISTS sync_metadata (
    key VARCHAR(100) PRIMARY KEY,
    last_modified_at TIMESTAMPTZ DEFAULT NOW()
);

-- Initialize global sync metadata
INSERT INTO sync_metadata (key, last_modified_at)
VALUES ('global', NOW())
ON CONFLICT (key) DO NOTHING;

-- Trigger Function to update sync_metadata whenever events, participants, or eligibility change
CREATE OR REPLACE FUNCTION update_sync_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO sync_metadata (key, last_modified_at)
    VALUES ('global', NOW())
    ON CONFLICT (key) DO UPDATE SET last_modified_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Attach triggers to detect schema or structural changes
DROP TRIGGER IF EXISTS trg_events_sync ON events;
CREATE TRIGGER trg_events_sync AFTER INSERT OR UPDATE OR DELETE ON events
    FOR EACH STATEMENT EXECUTE FUNCTION update_sync_timestamp();

DROP TRIGGER IF EXISTS trg_participants_sync ON participants;
CREATE TRIGGER trg_participants_sync AFTER INSERT OR UPDATE OR DELETE ON participants
    FOR EACH STATEMENT EXECUTE FUNCTION update_sync_timestamp();

DROP TRIGGER IF EXISTS trg_eligibility_sync ON event_eligibility;
CREATE TRIGGER trg_eligibility_sync AFTER INSERT OR UPDATE OR DELETE ON event_eligibility
    FOR EACH STATEMENT EXECUTE FUNCTION update_sync_timestamp();

-- ==============================================================================
-- RPC FUNCTION: mark_bulk_attendance
-- Efficient batch check-in RPC used when syncing offline scans back to Supabase
-- ==============================================================================
CREATE OR REPLACE FUNCTION mark_bulk_attendance(
    p_event_id UUID,
    p_student_ids UUID[]
)
RETURNS JSON AS $$
DECLARE
    v_inserted INTEGER := 0;
    v_event_status VARCHAR;
BEGIN
    -- 1. Validate event existence and status
    SELECT status INTO v_event_status FROM events WHERE id = p_event_id;
    IF v_event_status IS NULL THEN
        RETURN json_build_object('success', false, 'error', 'event_not_found', 'inserted', 0);
    END IF;

    IF v_event_status != 'open' THEN
        RETURN json_build_object('success', false, 'error', 'event_closed', 'inserted', 0);
    END IF;

    -- 2. Insert valid, eligible participants in a single atomic transaction
    WITH eligible_students AS (
        SELECT ee.participant_id
        FROM event_eligibility ee
        WHERE ee.event_id = p_event_id
          AND ee.participant_id = ANY(p_student_ids)
    )
    INSERT INTO attendance_records (event_id, participant_id, checked_in_at, status)
    SELECT p_event_id, es.participant_id, NOW(), 'present'
    FROM eligible_students es
    ON CONFLICT (event_id, participant_id) DO NOTHING;

    GET DIAGNOSTICS v_inserted = ROW_COUNT;

    RETURN json_build_object(
        'success', true,
        'inserted', v_inserted,
        'event_id', p_event_id
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ==============================================================================
-- RPC FUNCTION: mark_attendance
-- Single QR scan RPC (handles QR token lookup, eligibility check & insert)
-- ==============================================================================
CREATE OR REPLACE FUNCTION mark_attendance(
    p_event_id UUID,
    p_qr_token VARCHAR
)
RETURNS JSON AS $$
DECLARE
    v_participant RECORD;
    v_event_status VARCHAR;
    v_is_eligible BOOLEAN;
    v_already_checked_in BOOLEAN;
    v_inserted INTEGER := 0;
BEGIN
    -- 1. Check event status
    SELECT status INTO v_event_status FROM events WHERE id = p_event_id;
    IF v_event_status IS NULL THEN
        RETURN json_build_object('success', false, 'error', 'event_not_found');
    END IF;
    IF v_event_status != 'open' THEN
        RETURN json_build_object('success', false, 'error', 'event_closed');
    END IF;

    -- 2. Find participant by QR token
    SELECT id, usn, name INTO v_participant FROM participants WHERE qr_token = p_qr_token;
    IF v_participant.id IS NULL THEN
        RETURN json_build_object('success', false, 'error', 'invalid_token');
    END IF;

    -- 3. Check eligibility
    SELECT EXISTS (
        SELECT 1 FROM event_eligibility
        WHERE event_id = p_event_id AND participant_id = v_participant.id
    ) INTO v_is_eligible;

    IF NOT v_is_eligible THEN
        RETURN json_build_object(
            'success', false,
            'error', 'not_eligible',
            'participant_name', v_participant.name,
            'participant_usn', v_participant.usn
        );
    END IF;

    -- 4. Check if already checked in
    SELECT EXISTS (
        SELECT 1 FROM attendance_records
        WHERE event_id = p_event_id AND participant_id = v_participant.id
    ) INTO v_already_checked_in;

    IF v_already_checked_in THEN
        RETURN json_build_object(
            'success', true,
            'already_checked_in', true,
            'participant_id', v_participant.id,
            'participant_name', v_participant.name,
            'participant_usn', v_participant.usn
        );
    END IF;

    -- 5. Insert new attendance record
    INSERT INTO attendance_records (event_id, participant_id, checked_in_at, status)
    VALUES (p_event_id, v_participant.id, NOW(), 'present')
    ON CONFLICT (event_id, participant_id) DO NOTHING;

    GET DIAGNOSTICS v_inserted = ROW_COUNT;

    RETURN json_build_object(
        'success', true,
        'already_checked_in', false,
        'participant_id', v_participant.id,
        'participant_name', v_participant.name,
        'participant_usn', v_participant.usn,
        'checked_in_at', NOW()
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ==============================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ==============================================================================
ALTER TABLE participants ENABLE ROW LEVEL SECURITY;
ALTER TABLE events ENABLE ROW LEVEL SECURITY;
ALTER TABLE event_eligibility ENABLE ROW LEVEL SECURITY;
ALTER TABLE attendance_records ENABLE ROW LEVEL SECURITY;
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE sync_metadata ENABLE ROW LEVEL SECURITY;

-- Allow public read access to events & participants for authorized volunteers
CREATE POLICY "Public Read Events" ON events FOR SELECT USING (true);
CREATE POLICY "Public Read Participants" ON participants FOR SELECT USING (true);
CREATE POLICY "Public Read Eligibility" ON event_eligibility FOR SELECT USING (true);
CREATE POLICY "Public Read Attendance" ON attendance_records FOR SELECT USING (true);
CREATE POLICY "Public Read SyncMeta" ON sync_metadata FOR SELECT USING (true);

-- Allow inserting attendance
CREATE POLICY "Insert Attendance" ON attendance_records FOR INSERT WITH CHECK (true);

-- Admin full access based on auth.uid() matching users.id with role='admin'
CREATE POLICY "Admin All Events" ON events FOR ALL USING (
    EXISTS (SELECT 1 FROM users WHERE id = auth.uid() AND role = 'admin')
);
CREATE POLICY "Admin All Participants" ON participants FOR ALL USING (
    EXISTS (SELECT 1 FROM users WHERE id = auth.uid() AND role = 'admin')
);
CREATE POLICY "Admin All Eligibility" ON event_eligibility FOR ALL USING (
    EXISTS (SELECT 1 FROM users WHERE id = auth.uid() AND role = 'admin')
);
