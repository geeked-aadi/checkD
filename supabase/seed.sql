-- ==============================================================================
-- CHECKD HACKATHON SAMPLE SEED DATA
-- Populates events and participants matching the reference UI
-- ==============================================================================

-- 1. Insert Admin User
INSERT INTO users (id, email, role)
VALUES 
    ('a0000000-0000-0000-0000-000000000001', 'admin@hackathon.org', 'admin'),
    ('a0000000-0000-0000-0000-000000000002', 'volunteer@hackathon.org', 'volunteer')
ON CONFLICT (email) DO NOTHING;

-- 2. Insert Events
INSERT INTO events (id, name, description, event_date, start_time, end_time, status)
VALUES
    ('e1111111-1111-1111-1111-111111111111', 'Event 1', 'Opening ceremony and team onboarding', CURRENT_DATE, '08:00', '09:00', 'open'),
    ('e2222222-2222-2222-2222-222222222222', 'Breakfast', 'Morning breakfast and tea distribution', CURRENT_DATE, '09:00', '10:30', 'open'),
    ('e3333333-3333-3333-3333-333333333333', 'Lunch', 'Afternoon buffet lunch distribution', CURRENT_DATE, '13:00', '14:30', 'open'),
    ('e4444444-4444-4444-4444-444444444444', 'Dinner', 'Night dinner distribution', CURRENT_DATE, '20:00', '21:30', 'open')
ON CONFLICT (id) DO NOTHING;

-- 3. Insert Featured Participants from the UI reference
INSERT INTO participants (id, usn, name, email, qr_token, registration_status)
VALUES
    ('p0000001-0000-0000-0000-000000000001', '1RV22CS001', 'Aditya Sharma', 'aditya.sharma@example.com', 'QR_TOK_1RV22CS001', 'confirmed'),
    ('p0000002-0000-0000-0000-000000000002', '1RV22CS017', 'Saanvi Rao', 'saanvi.rao@example.com', 'QR_TOK_1RV22CS017', 'confirmed'),
    ('p0000003-0000-0000-0000-000000000003', '1RV22CS023', 'Mohan Kulkarni', 'mohan.kulkarni@example.com', 'QR_TOK_1RV22CS023', 'confirmed'),
    ('p0000004-0000-0000-0000-000000000004', '1RV22CS036', 'Ishita Verma', 'ishita.verma@example.com', 'QR_TOK_1RV22CS036', 'confirmed'),
    ('p0000005-0000-0000-0000-000000000005', '1RV22CS041', 'Rohan Mehta', 'rohan.mehta@example.com', 'QR_TOK_1RV22CS041', 'confirmed')
ON CONFLICT (usn) DO NOTHING;

-- 4. Generate Remaining 195 Participants (Total 200 participants)
DO $$
DECLARE
    i INT;
    v_usn VARCHAR;
    v_name VARCHAR;
    v_email VARCHAR;
    v_qr VARCHAR;
    v_id UUID;
BEGIN
    FOR i IN 6..200 LOOP
        v_usn := '1RV22CS' || LPAD(i::text, 3, '0');
        v_name := 'Participant ' || i;
        v_email := 'student' || i || '@example.com';
        v_qr := 'QR_TOK_' || v_usn;
        v_id := gen_random_uuid();

        INSERT INTO participants (id, usn, name, email, qr_token, registration_status)
        VALUES (v_id, v_usn, v_name, v_email, v_qr, 'confirmed')
        ON CONFLICT (usn) DO NOTHING;
    END LOOP;
END $$;

-- 5. Set Eligibility (All 200 participants eligible for all 4 events)
INSERT INTO event_eligibility (event_id, participant_id)
SELECT e.id, p.id
FROM events e
CROSS JOIN participants p
ON CONFLICT (event_id, participant_id) DO NOTHING;

-- 6. Insert Pre-existing Attendance Records matching the exact UI numbers:
-- Lunch (e3333333...): 120 / 200 (including our 5 named participants)
INSERT INTO attendance_records (event_id, participant_id, checked_in_at, status)
VALUES
    ('e3333333-3333-3333-3333-333333333333', 'p0000001-0000-0000-0000-000000000001', NOW() - INTERVAL '10 minutes', 'present'),
    ('e3333333-3333-3333-3333-333333333333', 'p0000002-0000-0000-0000-000000000002', NOW() - INTERVAL '12 minutes', 'present'),
    ('e3333333-3333-3333-3333-333333333333', 'p0000003-0000-0000-0000-000000000003', NOW() - INTERVAL '14 minutes', 'present'),
    ('e3333333-3333-3333-3333-333333333333', 'p0000004-0000-0000-0000-000000000004', NOW() - INTERVAL '16 minutes', 'present'),
    ('e3333333-3333-3333-3333-333333333333', 'p0000005-0000-0000-0000-000000000005', NOW() - INTERVAL '19 minutes', 'present')
ON CONFLICT (event_id, participant_id) DO NOTHING;

-- Insert remaining 115 records for Lunch to total 120
INSERT INTO attendance_records (event_id, participant_id, checked_in_at, status)
SELECT 'e3333333-3333-3333-3333-333333333333', id, NOW() - (ROW_NUMBER() OVER () * INTERVAL '30 seconds'), 'present'
FROM participants
WHERE id NOT IN (
    'p0000001-0000-0000-0000-000000000001',
    'p0000002-0000-0000-0000-000000000002',
    'p0000003-0000-0000-0000-000000000003',
    'p0000004-0000-0000-0000-000000000004',
    'p0000005-0000-0000-0000-000000000005'
)
LIMIT 115
ON CONFLICT (event_id, participant_id) DO NOTHING;

-- Breakfast (e2222222...): 150 / 200
INSERT INTO attendance_records (event_id, participant_id, checked_in_at, status)
SELECT 'e2222222-2222-2222-2222-222222222222', id, NOW() - INTERVAL '3 hours', 'present'
FROM participants
LIMIT 150
ON CONFLICT (event_id, participant_id) DO NOTHING;

-- Dinner (e4444444...): 80 / 200
INSERT INTO attendance_records (event_id, participant_id, checked_in_at, status)
SELECT 'e4444444-4444-4444-4444-444444444444', id, NOW() - INTERVAL '1 hour', 'present'
FROM participants
LIMIT 80
ON CONFLICT (event_id, participant_id) DO NOTHING;

-- Update sync metadata timestamp
UPDATE sync_metadata SET last_modified_at = NOW() WHERE key = 'global';
