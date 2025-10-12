#!/bin/bash

# CONFIGURA ESTOS DATOS:
# TODO: Update with your actual Supabase credentials
SUPABASE_DB_HOST="db.YOUR_PROJECT_REF.supabase.co"
SUPABASE_DB_PORT="5432"
SUPABASE_DB_USER="postgres"
SUPABASE_DB_NAME="postgres"
SUPABASE_DB_PASSWORD="YOUR_DATABASE_PASSWORD"

echo "🚀 Ejecutando migración a Supabase…"

PGPASSWORD="$SUPABASE_DB_PASSWORD" psql \
  -h "$SUPABASE_DB_HOST" \
  -p "$SUPABASE_DB_PORT" \
  -U "$SUPABASE_DB_USER" \
  -d "$SUPABASE_DB_NAME" \
  -f "scripts/add_auth_fields_and_rls.sql" \
  --set ON_ERROR_STOP=on

echo "✅ Migración ejecutada (si no hubo errores)."