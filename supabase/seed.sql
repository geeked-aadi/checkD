-- ==============================================================================
-- CHECKD HACKATHON SAMPLE SEED DATA
-- Matches Schema: spoc, teams, participants, events, attendance
-- ==============================================================================

-- 1. Insert SPOCs
INSERT INTO public.spoc (spoc_id, name, phonenumber)
VALUES
  ('SPOC001', 'Dr. Ramesh Kumar', 9876543210),
  ('SPOC002', 'Prof. Priya Sharma', 9876543211)
ON CONFLICT (spoc_id) DO NOTHING;

-- 2. Insert Teams (without leaders first, to satisfy foreign key constraints)
INSERT INTO public.teams (team_id, team_name, team_leader, spoc_id)
VALUES
  ('TEAM_A', 'CodeCraft', NULL, 'SPOC001'),
  ('TEAM_B', 'ByteBuilders', NULL, 'SPOC001'),
  ('TEAM_C', 'AlgoKnights', NULL, 'SPOC002')
ON CONFLICT (team_id) DO NOTHING;

-- 3. Insert Participants
INSERT INTO public.participants (p_id, name, team_id, phonenumber)
VALUES
  ('P101', 'Aditya Sharma', 'TEAM_A', 9812345678),
  ('P102', 'Saanvi Rao', 'TEAM_A', 9812345679),
  ('P103', 'Mohan Kulkarni', 'TEAM_B', 9812345680),
  ('P104', 'Ishita Verma', 'TEAM_B', 9812345681),
  ('P105', 'Rohan Mehta', 'TEAM_C', 9812345682),
  ('P106', 'Ananya Patel', 'TEAM_C', 9812345683)
ON CONFLICT (p_id) DO NOTHING;

-- Update Team Leaders
UPDATE public.teams SET team_leader = 'P101' WHERE team_id = 'TEAM_A';
UPDATE public.teams SET team_leader = 'P103' WHERE team_id = 'TEAM_B';
UPDATE public.teams SET team_leader = 'P105' WHERE team_id = 'TEAM_C';

-- 4. Insert Events
INSERT INTO public.events (event_id, event_name, starts_from, ends_on)
VALUES
  (1, 'Hackathon Registration & Check-in', '2026-11-05 08:30:00', '2026-11-05 10:00:00'),
  (2, 'Breakfast', '2026-11-05 09:30:00', '2026-11-05 11:00:00'),
  (3, 'Opening Ceremony & Keynote', '2026-11-05 11:00:00', '2026-11-05 12:30:00'),
  (4, 'Lunch', '2026-11-05 13:30:00', '2026-11-05 15:00:00'),
  (5, 'Workshop 1: Supabase & Edge Functions', '2026-11-05 15:30:00', '2026-11-05 17:00:00'),
  (6, 'Dinner', '2026-11-05 20:00:00', '2026-11-05 21:30:00')
ON CONFLICT (event_id) DO NOTHING;

-- 5. Insert Sample Attendance Records
INSERT INTO public.attendance (p_id, event_id, time)
VALUES
  ('P101', 1, '2026-11-05 08:45:00'),
  ('P102', 1, '2026-11-05 08:50:00'),
  ('P103', 1, '2026-11-05 09:12:00'),
  ('P101', 2, '2026-11-05 09:40:00'),
  ('P102', 2, '2026-11-05 09:45:00')
ON CONFLICT DO NOTHING;
