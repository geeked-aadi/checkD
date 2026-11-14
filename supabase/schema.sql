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
  CONSTRAINT attendance_event_id_fkey FOREIGN KEY (event_id) REFERENCES public.events(event_id),
  CONSTRAINT attendance_event_participant_unique UNIQUE (event_id, p_id)
);

-- Indexes for rapid lookup
CREATE INDEX IF NOT EXISTS idx_participants_team ON public.participants(team_id);
CREATE INDEX IF NOT EXISTS idx_attendance_event ON public.attendance(event_id);
CREATE INDEX IF NOT EXISTS idx_attendance_participant ON public.attendance(p_id);
CREATE INDEX IF NOT EXISTS idx_attendance_composite ON public.attendance(event_id, p_id);

-- Convert existing columns to text to prevent length limit issues (e.g., varchar(5) overflows)
ALTER TABLE IF EXISTS public.attendance ALTER COLUMN p_id TYPE text;
ALTER TABLE IF EXISTS public.participants ALTER COLUMN p_id TYPE text, ALTER COLUMN name TYPE text, ALTER COLUMN team_id TYPE text;
ALTER TABLE IF EXISTS public.teams ALTER COLUMN team_id TYPE text, ALTER COLUMN team_name TYPE text, ALTER COLUMN team_leader TYPE text, ALTER COLUMN spoc_id TYPE text;
ALTER TABLE IF EXISTS public.spoc ALTER COLUMN spoc_id TYPE text, ALTER COLUMN name TYPE text;
ALTER TABLE IF EXISTS public.events ALTER COLUMN event_name TYPE text;

-- Ensure unique constraint exists on attendance table
ALTER TABLE IF EXISTS public.attendance
  DROP CONSTRAINT IF EXISTS attendance_event_participant_unique;
ALTER TABLE IF EXISTS public.attendance
  ADD CONSTRAINT attendance_event_participant_unique UNIQUE (event_id, p_id);

-- Enable Supabase Realtime CDC logical replication
ALTER TABLE IF EXISTS public.attendance REPLICA IDENTITY FULL;
ALTER TABLE IF EXISTS public.events REPLICA IDENTITY FULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_publication_tables 
        WHERE pubname = 'supabase_realtime' AND tablename = 'attendance'
    ) THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.attendance;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_publication_tables 
        WHERE pubname = 'supabase_realtime' AND tablename = 'events'
    ) THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.events;
    END IF;
END $$;

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

-- ==============================================================================
-- RPC FUNCTIONS
-- ==============================================================================

-- Drop all existing overloaded versions of mark_attendance
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN (
        SELECT p.proname, pg_get_function_identity_arguments(p.oid) AS args
        FROM pg_proc p
        JOIN pg_namespace n ON p.pronamespace = n.oid
        WHERE n.nspname = 'public' AND p.proname = 'mark_attendance'
    ) LOOP
        EXECUTE format('DROP FUNCTION public.%I(%s) CASCADE;', r.proname, r.args);
    END LOOP;
END $$;

DROP FUNCTION IF EXISTS public.mark_attendance(integer, character varying[], character varying) CASCADE;
DROP FUNCTION IF EXISTS public.mark_attendance(integer, character varying[], text) CASCADE;
DROP FUNCTION IF EXISTS public.mark_attendance(integer, text[], text) CASCADE;
DROP FUNCTION IF EXISTS public.mark_attendance(integer, jsonb, text) CASCADE;
DROP FUNCTION IF EXISTS public.mark_attendance(integer, jsonb, character varying) CASCADE;

-- Definitive mark_attendance function matching Checkd Android client
CREATE OR REPLACE FUNCTION public.mark_attendance(
    p_event_id integer,
    p_p_ids text[],
    p_role text DEFAULT 'STAFF'
)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_now timestamp := clock_timestamp();
    v_pid text;
    v_inserted_pid text;
    v_marked text[] := ARRAY[]::text[];
    v_duplicates text[] := ARRAY[]::text[];
    v_event_attendance integer;
    v_total_participants integer;
    v_status text;
    v_message text;
BEGIN
    -- Verify event exists
    IF NOT EXISTS (SELECT 1 FROM public.events WHERE event_id = p_event_id) THEN
        RETURN jsonb_build_object(
            'status', 'error',
            'message', 'Event not found with ID ' || p_event_id,
            'marked_present', jsonb_build_array(),
            'duplicates', jsonb_build_array(),
            'progress', '0/0',
            'event_attendance', 0,
            'total_participants', 0
        );
    END IF;

    -- Process each participant ID atomically
    IF p_p_ids IS NOT NULL THEN
        FOREACH v_pid IN ARRAY p_p_ids
        LOOP
            v_pid := trim(v_pid);
            IF v_pid <> '' THEN
                -- Atomic insert with conflict detection: PostgreSQL locks the row index atomically
                INSERT INTO public.attendance (p_id, event_id, time)
                VALUES (v_pid, p_event_id, v_now)
                ON CONFLICT (event_id, p_id) DO NOTHING
                RETURNING p_id INTO v_inserted_pid;

                IF v_inserted_pid IS NOT NULL THEN
                    v_marked := array_append(v_marked, v_pid);
                ELSE
                    v_duplicates := array_append(v_duplicates, v_pid);
                END IF;
            END IF;
        END LOOP;
    END IF;

    -- Compute updated event attendance count
    SELECT COUNT(DISTINCT p_id) INTO v_event_attendance
    FROM public.attendance
    WHERE event_id = p_event_id;

    -- Compute total participants registered
    SELECT COUNT(*) INTO v_total_participants
    FROM public.participants;

    -- Determine status and message
    IF array_length(v_marked, 1) > 0 THEN
        v_status := 'success';
        v_message := 'Attendance recorded successfully';
    ELSIF array_length(v_duplicates, 1) > 0 THEN
        v_status := 'duplicate';
        v_message := 'Already checked in for this event';
    ELSE
        v_status := 'error';
        v_message := 'No valid participant IDs provided';
    END IF;

    -- Return JSON object matching MarkAttendanceResponseDto
    RETURN jsonb_build_object(
        'status', v_status,
        'message', v_message,
        'marked_present', to_jsonb(v_marked),
        'duplicates', to_jsonb(v_duplicates),
        'progress', v_event_attendance || '/' || v_total_participants,
        'event_attendance', v_event_attendance,
        'total_participants', v_total_participants
    );
END;
$$;

GRANT EXECUTE ON FUNCTION public.mark_attendance(integer, text[], text) TO anon, authenticated, service_role;

-- Drop all existing overloaded versions of update_event_time_window
DROP FUNCTION IF EXISTS public.update_event_time_window(integer, timestamp without time zone, timestamp without time zone) CASCADE;
DROP FUNCTION IF EXISTS public.update_event_time_window(integer, timestamp without time zone, timestamp without time zone, character varying) CASCADE;
DROP FUNCTION IF EXISTS public.update_event_time_window(integer, text, text) CASCADE;
DROP FUNCTION IF EXISTS public.update_event_time_window(integer, text, text, text) CASCADE;

-- Dedicated RPC function to update event time window and/or event name
CREATE OR REPLACE FUNCTION public.update_event_time_window(
    p_event_id integer,
    p_starts_from text,
    p_ends_on text,
    p_event_name text DEFAULT NULL
)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    UPDATE public.events
    SET starts_from = p_starts_from::timestamp,
        ends_on = p_ends_on::timestamp,
        event_name = COALESCE(NULLIF(p_event_name, ''), event_name)
    WHERE event_id = p_event_id;

    IF NOT FOUND THEN
        RETURN jsonb_build_object(
            'status', 'error',
            'message', 'Event not found with ID ' || p_event_id
        );
    END IF;

    RETURN jsonb_build_object(
        'status', 'success',
        'message', 'Timeline updated successfully',
        'event_id', p_event_id
    );
END;
$$;

GRANT EXECUTE ON FUNCTION public.update_event_time_window(integer, text, text, text) TO anon, authenticated, service_role;

-- Dedicated RPC function to create new events seamlessly
CREATE OR REPLACE FUNCTION public.create_event(
    p_event_name text,
    p_starts_from text,
    p_ends_on text,
    p_event_id integer DEFAULT NULL
)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_next_id integer;
BEGIN
    IF p_event_id IS NOT NULL AND p_event_id > 0 THEN
        INSERT INTO public.events (event_id, event_name, starts_from, ends_on)
        OVERRIDING SYSTEM VALUE
        VALUES (
            p_event_id,
            p_event_name,
            p_starts_from::timestamp,
            p_ends_on::timestamp
        )
        RETURNING event_id INTO v_next_id;
    ELSE
        INSERT INTO public.events (event_name, starts_from, ends_on)
        VALUES (
            p_event_name,
            p_starts_from::timestamp,
            p_ends_on::timestamp
        )
        RETURNING event_id INTO v_next_id;
    END IF;

    RETURN jsonb_build_object(
        'status', 'success',
        'message', 'Event created successfully',
        'event_id', v_next_id,
        'event_name', p_event_name
    );
END;
$$;

GRANT EXECUTE ON FUNCTION public.create_event(text, text, text, integer) TO anon, authenticated, service_role;
GRANT EXECUTE ON FUNCTION public.get_attendance_matrix() TO anon, authenticated, service_role;

