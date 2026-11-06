-- ==============================================================================
-- CHECKD HACKATHON EVENT ATTENDANCE SYSTEM
-- Supabase PostgreSQL Schema & Security Policies
-- ==============================================================================

-- 1. SPOC TABLE
CREATE TABLE IF NOT EXISTS public.spoc (
  spoc_id character varying NOT NULL,
  name character varying,
  phonenumber numeric,
  CONSTRAINT spoc_pkey PRIMARY KEY (spoc_id)
);

-- 2. TEAMS TABLE
CREATE TABLE IF NOT EXISTS public.teams (
  team_id character varying NOT NULL,
  team_name character varying,
  team_leader character varying,
  spoc_id character varying,
  CONSTRAINT teams_pkey PRIMARY KEY (team_id),
  CONSTRAINT teams_spoc_id_fkey FOREIGN KEY (spoc_id) REFERENCES public.spoc(spoc_id)
);

-- 3. PARTICIPANTS TABLE
CREATE TABLE IF NOT EXISTS public.participants (
  p_id character varying NOT NULL,
  name character varying,
  team_id character varying,
  phonenumber numeric,
  CONSTRAINT participants_pkey PRIMARY KEY (p_id),
  CONSTRAINT participants_team_id_fkey FOREIGN KEY (team_id) REFERENCES public.teams(team_id)
);

-- Circular foreign key from teams.team_leader to participants.p_id
ALTER TABLE public.teams 
  DROP CONSTRAINT IF EXISTS teams_team_leader_fkey,
  ADD CONSTRAINT teams_team_leader_fkey FOREIGN KEY (team_leader) REFERENCES public.participants(p_id);

-- 4. EVENTS TABLE
CREATE TABLE IF NOT EXISTS public.events (
  event_id integer NOT NULL,
  event_name character varying,
  starts_from timestamp without time zone,
  ends_on timestamp without time zone,
  CONSTRAINT events_pkey PRIMARY KEY (event_id)
);

-- 5. ATTENDANCE TABLE
CREATE TABLE IF NOT EXISTS public.attendance (
  p_id character varying,
  event_id integer,
  time timestamp without time zone,
  CONSTRAINT attendance_p_id_fkey FOREIGN KEY (p_id) REFERENCES public.participants(p_id),
  CONSTRAINT attendance_event_id_fkey FOREIGN KEY (event_id) REFERENCES public.events(event_id)
);

-- Indexes for rapid lookup
CREATE INDEX IF NOT EXISTS idx_participants_team ON public.participants(team_id);
CREATE INDEX IF NOT EXISTS idx_attendance_event ON public.attendance(event_id);
CREATE INDEX IF NOT EXISTS idx_attendance_participant ON public.attendance(p_id);
CREATE INDEX IF NOT EXISTS idx_attendance_composite ON public.attendance(event_id, p_id);

-- ==============================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ==============================================================================
ALTER TABLE public.spoc ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.teams ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.participants ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.events ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.attendance ENABLE ROW LEVEL SECURITY;

-- Allow public read access to events, participants, teams, and spoc
CREATE POLICY "Public Read Spoc" ON public.spoc FOR SELECT USING (true);
CREATE POLICY "Public Read Teams" ON public.teams FOR SELECT USING (true);
CREATE POLICY "Public Read Participants" ON public.participants FOR SELECT USING (true);
CREATE POLICY "Public Read Events" ON public.events FOR SELECT USING (true);
CREATE POLICY "Public Read Attendance" ON public.attendance FOR SELECT USING (true);

-- Allow inserting events & attendance
CREATE POLICY "Public Insert Events" ON public.events FOR INSERT WITH CHECK (true);
CREATE POLICY "Public Insert Attendance" ON public.attendance FOR INSERT WITH CHECK (true);
CREATE POLICY "Public Update Events" ON public.events FOR UPDATE USING (true);
