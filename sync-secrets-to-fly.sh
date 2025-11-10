#!/bin/bash
# Sync secrets from local setup-env.sh to Fly.io
# This ensures production matches your working local configuration

echo "=========================================="
echo "Syncing Secrets to Fly.io"
echo "=========================================="
echo ""

# Source the local environment variables
source setup-env.sh

echo "Updating secrets in Fly.io..."
echo ""

# Update JWT Secret (CRITICAL - this is why 401 is happening)
echo "Setting SUPABASE_JWT_SECRET..."
fly secrets set SUPABASE_JWT_SECRET="$SUPABASE_JWT_SECRET" --app student-performance-api

# Update other Supabase secrets
echo "Setting SUPABASE_URL..."
fly secrets set SUPABASE_URL="$SUPABASE_URL" --app student-performance-api

echo "Setting SUPABASE_ANON_KEY..."
fly secrets set SUPABASE_ANON_KEY="$SUPABASE_ANON_KEY" --app student-performance-api

# Note: setup-env.sh has SUPABASE_SERVICE_KEY but app expects SUPABASE_SERVICE_ROLE_KEY
# You'll need to get the service_role key from Supabase Dashboard
echo ""
echo "⚠️  NOTE: SUPABASE_SERVICE_ROLE_KEY needs to be set manually"
echo "   Get it from: Supabase Dashboard → Settings → API → service_role key"
echo "   Then run:"
echo "   fly secrets set SUPABASE_SERVICE_ROLE_KEY=\"your-service-role-key\" --app student-performance-api"
echo ""

echo "✅ Secrets updated!"
echo ""
echo "The app will automatically restart. Check logs:"
echo "  fly logs --app student-performance-api"
echo ""
echo "After restart, test authentication - the 401 error should be resolved!"

