#!/bin/bash
# Set ALL required environment variables for Fly.io deployment
# This script sets all 7 required secrets that have no defaults

echo "=========================================="
echo "Setting ALL required secrets for Fly.io"
echo "=========================================="
echo ""
echo "Required secrets (no defaults):"
echo "  1. DB_URL"
echo "  2. DB_USERNAME"
echo "  3. DB_PASSWORD"
echo "  4. SUPABASE_JWT_SECRET ⚠️  (MISSING - causing crash)"
echo "  5. SUPABASE_URL"
echo "  6. SUPABASE_ANON_KEY"
echo "  7. SUPABASE_SERVICE_ROLE_KEY ⚠️  (wrong name currently)"
echo ""

# Check if running interactively
if [ -t 0 ]; then
    echo "Enter your values (or press Ctrl+C to exit):"
    echo ""
    
    read -p "DB_URL (e.g., jdbc:postgresql://host:5432/db): " DB_URL_VAL
    read -p "DB_USERNAME: " DB_USERNAME_VAL
    read -sp "DB_PASSWORD: " DB_PASSWORD_VAL
    echo ""
    read -sp "SUPABASE_JWT_SECRET: " SUPABASE_JWT_SECRET_VAL
    echo ""
    read -p "SUPABASE_URL (e.g., https://xxx.supabase.co): " SUPABASE_URL_VAL
    read -sp "SUPABASE_ANON_KEY: " SUPABASE_ANON_KEY_VAL
    echo ""
    read -sp "SUPABASE_SERVICE_ROLE_KEY: " SUPABASE_SERVICE_ROLE_KEY_VAL
    echo ""
    
    echo ""
    echo "Setting secrets..."
    
    fly secrets set \
      DB_URL="$DB_URL_VAL" \
      DB_USERNAME="$DB_USERNAME_VAL" \
      DB_PASSWORD="$DB_PASSWORD_VAL" \
      SUPABASE_JWT_SECRET="$SUPABASE_JWT_SECRET_VAL" \
      SUPABASE_URL="$SUPABASE_URL_VAL" \
      SUPABASE_ANON_KEY="$SUPABASE_ANON_KEY_VAL" \
      SUPABASE_SERVICE_ROLE_KEY="$SUPABASE_SERVICE_ROLE_KEY_VAL" \
      --app student-performance-api
    
    echo ""
    echo "✅ All secrets set!"
    echo ""
    echo "The app will automatically restart. Check logs:"
    echo "  fly logs --app student-performance-api"
else
    echo "Non-interactive mode. Set secrets manually:"
    echo ""
    echo "fly secrets set \\"
    echo "  DB_URL=\"your-db-url\" \\"
    echo "  DB_USERNAME=\"your-username\" \\"
    echo "  DB_PASSWORD=\"your-password\" \\"
    echo "  SUPABASE_JWT_SECRET=\"your-jwt-secret\" \\"
    echo "  SUPABASE_URL=\"https://xxx.supabase.co\" \\"
    echo "  SUPABASE_ANON_KEY=\"your-anon-key\" \\"
    echo "  SUPABASE_SERVICE_ROLE_KEY=\"your-service-role-key\" \\"
    echo "  --app student-performance-api"
fi

