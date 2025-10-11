#!/bin/bash

# Migration Script Runner: Update enum values from v1.x to v2.0
# This script updates lowercase enum values to uppercase in the database

set -e  # Exit on error

echo "🔄 Starting enum values migration..."
echo "⚠️  This will update existing data in your database"
echo ""

# Load environment variables
if [ -f "../setup-env.sh" ]; then
    source ../setup-env.sh
    echo "✅ Environment variables loaded"
else
    echo "❌ setup-env.sh not found. Please run from the scripts directory."
    exit 1
fi

# Extract connection details from JDBC URL
# Format: jdbc:postgresql://host:port/database
DB_HOST=$(echo $DB_URL | sed -n 's/.*:\/\/\([^:]*\):.*/\1/p')
DB_PORT=$(echo $DB_URL | sed -n 's/.*:\([0-9]*\)\/.*/\1/p')
DB_NAME=$(echo $DB_URL | sed -n 's/.*\/\([^?]*\).*/\1/p')

echo "📊 Database Info:"
echo "   Host: $DB_HOST"
echo "   Port: $DB_PORT"
echo "   Database: $DB_NAME"
echo "   Username: $DB_USERNAME"
echo ""

# Ask for confirmation
read -p "⚠️  Do you want to proceed with the migration? (yes/no): " -r
echo
if [[ ! $REPLY =~ ^[Yy][Ee][Ss]$ ]]
then
    echo "❌ Migration cancelled"
    exit 1
fi

echo "🔄 Running migration..."
echo ""

# Run the migration SQL script using psql
PGPASSWORD=$DB_PASSWORD psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USERNAME" -d "$DB_NAME" -f migrate_enum_values.sql

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Migration completed successfully!"
    echo "🚀 You can now restart your application"
else
    echo ""
    echo "❌ Migration failed. Please check the error messages above."
    exit 1
fi

