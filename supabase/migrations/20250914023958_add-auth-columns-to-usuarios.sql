-- Incrementar timeout para evitar cancelaciones por bloqueo
SET lock_timeout = '15s';

-- Add auth_user_id (unique) and password/timestamp columns to public.usuarios
ALTER TABLE public.usuarios
  ADD COLUMN IF NOT EXISTS auth_user_id uuid UNIQUE,
  ADD COLUMN IF NOT EXISTS password_hash text,
  ADD COLUMN IF NOT EXISTS last_password_changed timestamp with time zone,
  ADD COLUMN IF NOT EXISTS created_at timestamp with time zone DEFAULT now(),
  ADD COLUMN IF NOT EXISTS updated_at timestamp with time zone DEFAULT now();

-- Create an index for the auth_user_id column to improve joins and policy performance
CREATE INDEX IF NOT EXISTS idx_usuarios_auth_user_id ON public.usuarios(auth_user_id);

-- Enable Row Level Security
ALTER TABLE public.usuarios ENABLE ROW LEVEL SECURITY;

-- RLS policies using auth.uid() (adjust TO clauses if you need anon access)
CREATE POLICY "Usuarios: select own" ON public.usuarios FOR SELECT TO authenticated USING ((SELECT auth.uid()) = auth_user_id);
CREATE POLICY "Usuarios: insert own" ON public.usuarios FOR INSERT TO authenticated WITH CHECK ((SELECT auth.uid()) = auth_user_id);
CREATE POLICY "Usuarios: update own" ON public.usuarios FOR UPDATE TO authenticated USING ((SELECT auth.uid()) = auth_user_id) WITH CHECK ((SELECT auth.uid()) = auth_user_id);
CREATE POLICY "Usuarios: delete own" ON public.usuarios FOR DELETE TO authenticated USING ((SELECT auth.uid()) = auth_user_id);