#!/bin/bash
# Fix missing and incorrectly named secrets in Fly.io
# Usage: ./fix-fly-secrets.sh

echo "Fixing Fly.io secrets..."
echo ""
echo "⚠️  You need to provide:"
echo "   1. SUPABASE_JWT_SECRET (from Supabase Dashboard > Project Settings > API > JWT Settings)"
echo "   2. SUPABASE_SERVICE_ROLE_KEY (should be the same value as your current SUPABASE_SERVICE_KEY)"
echo ""

# Read the JWT secret
read -sp "Enter SUPABASE_JWT_SECRET: " JWT_SECRET
echo ""

# Read the service role key
read -sp "Enter SUPABASE_SERVICE_ROLE_KEY (or press Enter to use existing SUPABASE_SERVICE_KEY value): " SERVICE_ROLE_KEY
echo ""

if [ -z "$SERVICE_ROLE_KEY" ]; then
    echo "⚠️  You'll need to manually copy the value from SUPABASE_SERVICE_KEY to SUPABASE_SERVICE_ROLE_KEY"
    echo "   Run: fly secrets set SUPABASE_SERVICE_ROLE_KEY=\"<value>\" --app student-performance-api"
    echo ""
fi

# Set the missing JWT secret
if [ -n "$JWT_SECRET" ]; then
    echo "Setting SUPABASE_JWT_SECRET..."
    fly secrets set SUPABASE_JWT_SECRET="$JWT_SECRET" --app student-performance-api
    echo "✅ SUPABASE_JWT_SECRET set"
else
    echo "❌ SUPABASE_JWT_SECRET not provided"
fi

# Set the correctly named service role key
if [ -n "$SERVICE_ROLE_KEY" ]; then
    echo "Setting SUPABASE_SERVICE_ROLE_KEY..."
    fly secrets set SUPABASE_SERVICE_ROLE_KEY="$SERVICE_ROLE_KEY" --app student-performance-api
    echo "✅ SUPABASE_SERVICE_ROLE_KEY set"
fi

echo ""
echo "✅ Secrets updated!"
echo ""
echo "To verify: fly secrets list --app student-performance-api"
echo "The app will automatically restart. Check logs: fly logs --app student-performance-api"

