#!/bin/bash
# Script to set all required environment variables in Fly.io
# Usage: ./set-fly-secrets.sh

echo "Setting environment variables for Fly.io deployment..."
echo "Make sure you have your Supabase and database credentials ready."
echo ""

# Set all secrets at once
fly secrets set \
  DB_URL="jdbc:postgresql://YOUR_DB_HOST:5432/YOUR_DB_NAME" \
  DB_USERNAME="YOUR_DB_USERNAME" \
  DB_PASSWORD="YOUR_DB_PASSWORD" \
  SUPABASE_JWT_SECRET="YOUR_SUPABASE_JWT_SECRET" \
  SUPABASE_URL="https://YOUR_PROJECT_REF.supabase.co" \
  SUPABASE_ANON_KEY="YOUR_SUPABASE_ANON_KEY" \
  SUPABASE_SERVICE_ROLE_KEY="YOUR_SUPABASE_SERVICE_ROLE_KEY" \
  ALLOWED_ORIGINS="https://your-frontend-domain.com" \
  --app student-performance-api

echo ""
echo "✅ Secrets set successfully!"
echo ""
echo "To verify, run: fly secrets list --app student-performance-api"
echo "To view logs: fly logs --app student-performance-api"

