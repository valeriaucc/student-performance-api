#!/bin/bash

# CONFIGURA ESTOS DATOS:
SUPABASE_DB_HOST="db.fwip1mnkxxcksvvspri.supabase.co"
SUPABASE_DB_PORT="5432"
SUPABASE_DB_USER="postgres"
SUPABASE_DB_NAME="postgres"
SUPABASE_DB_PASSWORD="ai is my passion"

echo "🚀 Ejecutando migración a Supabase…"

PGPASSWORD="$SUPABASE_DB_PASSWORD" psql \
  -h "$SUPABASE_DB_HOST" \
  -p "$SUPABASE_DB_PORT" \
  -U "$SUPABASE_DB_USER" \
  -d "$SUPABASE_DB_NAME" \
  -f "scripts/add_auth_fields_and_rls.sql" \
  --set ON_ERROR_STOP=on

echo "✅ Migración ejecutada (si no hubo errores)."