#!/bin/bash

# ============================================================================
# Database Migration Runner
# ============================================================================
# This script helps you run the authentication migrations on your Supabase
# database with proper password handling.
#
# Usage:
#   chmod +x run-migrations.sh
#   ./run-migrations.sh
# ============================================================================

set -e  # Exit on error

echo "🚀 AIClass API - Database Migration Runner"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Database configuration
DB_HOST="aws-1-us-east-2.pooler.supabase.com"
DB_PORT="6543"  # Try direct connection port
DB_NAME="postgres"
DB_USER="postgres.fwipiimnkxxcksvvspri"
DB_PASSWORD="ai is my passion"

# URL encode the password (replace spaces with %20)
DB_PASSWORD_ENCODED=$(echo "$DB_PASSWORD" | sed 's/ /%20/g')

# Construct connection string
DB_URL="postgresql://${DB_USER}:${DB_PASSWORD_ENCODED}@${DB_HOST}:${DB_PORT}/${DB_NAME}"

echo "📊 Database Configuration:"
echo "   Host: $DB_HOST"
echo "   Port: $DB_PORT"
echo "   User: $DB_USER"
echo "   Database: $DB_NAME"
echo ""

# Test connection
echo "🔍 Testing database connection..."
if psql "$DB_URL" -c "SELECT version();" > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Connection successful!${NC}"
    echo ""
else
    echo -e "${RED}❌ Connection failed!${NC}"
    echo ""
    echo "Troubleshooting tips:"
    echo "1. Check if PostgreSQL client is installed: psql --version"
    echo "2. Verify your password in application-local.properties"
    echo "3. Try using port 5432 instead of 6543"
    echo "4. Use Supabase SQL Editor as alternative (see instructions below)"
    echo ""
    
    read -p "Try alternative connection string? (y/n) " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "Trying port 5432..."
        DB_URL="postgresql://${DB_USER}:${DB_PASSWORD_ENCODED}@${DB_HOST}:5432/${DB_NAME}"
        
        if ! psql "$DB_URL" -c "SELECT version();" > /dev/null 2>&1; then
            echo -e "${RED}❌ Still failed. Please use Supabase SQL Editor instead.${NC}"
            echo ""
            echo "📝 Instructions:"
            echo "1. Go to https://app.supabase.com/"
            echo "2. Select your project"
            echo "3. Click 'SQL Editor' in left sidebar"
            echo "4. Click 'New query'"
            echo "5. Copy contents of: supabase/migrations/20250111000000_comprehensive_auth_and_rls.sql"
            echo "6. Paste and click 'Run'"
            echo "7. Repeat for: supabase/migrations/20250111000001_auto_create_user_profile.sql"
            exit 1
        fi
    else
        exit 1
    fi
fi

# Run migrations
echo "🗄️  Running Database Migrations..."
echo "=================================="
echo ""

# Migration 1: RLS Policies
MIGRATION_1="supabase/migrations/20250111000000_comprehensive_auth_and_rls.sql"
if [ -f "$MIGRATION_1" ]; then
    echo "📝 Running Migration 1: RLS Policies and Security Functions..."
    if psql "$DB_URL" -f "$MIGRATION_1"; then
        echo -e "${GREEN}✅ Migration 1 completed successfully!${NC}"
        echo ""
    else
        echo -e "${RED}❌ Migration 1 failed!${NC}"
        echo "Check the error message above for details."
        exit 1
    fi
else
    echo -e "${RED}❌ Migration file not found: $MIGRATION_1${NC}"
    exit 1
fi

# Migration 2: Auto-create User Profile
MIGRATION_2="supabase/migrations/20250111000001_auto_create_user_profile.sql"
if [ -f "$MIGRATION_2" ]; then
    echo "📝 Running Migration 2: Auto-create User Profile Trigger..."
    if psql "$DB_URL" -f "$MIGRATION_2"; then
        echo -e "${GREEN}✅ Migration 2 completed successfully!${NC}"
        echo ""
    else
        echo -e "${RED}❌ Migration 2 failed!${NC}"
        echo "Check the error message above for details."
        exit 1
    fi
else
    echo -e "${RED}❌ Migration file not found: $MIGRATION_2${NC}"
    exit 1
fi

# Verify migrations
echo "🔍 Verifying Migrations..."
echo "=========================="
echo ""

echo "Checking Row Level Security status..."
psql "$DB_URL" -c "
SELECT tablename, rowsecurity 
FROM pg_tables 
WHERE schemaname = 'public' 
AND tablename IN ('users', 'classes', 'grades', 'enrollments', 'subjects', 'ai_recommendations')
ORDER BY tablename;
"

echo ""
echo "Checking policies created..."
psql "$DB_URL" -c "
SELECT COUNT(*) as policy_count, tablename 
FROM pg_policies 
WHERE schemaname = 'public' 
GROUP BY tablename 
ORDER BY tablename;
"

echo ""
echo "Checking trigger created..."
psql "$DB_URL" -c "
SELECT tgname, tgenabled 
FROM pg_trigger 
WHERE tgname = 'on_auth_user_created';
"

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}🎉 All migrations completed successfully!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Next steps:"
echo "1. Update your application-local.properties with Supabase credentials"
echo "2. Build your application: ./mvnw clean package -DskipTests"
echo "3. Start your application: ./mvnw spring-boot:run"
echo "4. Test authentication (see SETUP_INSTRUCTIONS.md)"
echo ""

