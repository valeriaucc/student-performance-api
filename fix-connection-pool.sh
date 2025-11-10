#!/bin/bash
# Fix connection pool issues in Fly.io
# This sets environment variables to override pool configuration

echo "=========================================="
echo "Fixing Connection Pool Configuration"
echo "=========================================="
echo ""

echo "Setting HikariCP pool configuration to very conservative values..."
echo ""

fly secrets set \
  HIKARI_MAX_POOL_SIZE=1 \
  HIKARI_MIN_IDLE=0 \
  --app student-performance-api

echo ""
echo "✅ Connection pool settings updated!"
echo ""
echo "Pool configuration:"
echo "  - Maximum pool size: 1 connection per machine"
echo "  - Minimum idle: 0 (connections created on demand)"
echo "  - With 2 machines: Max 2 total connections"
echo ""
echo "The app will automatically restart. This should resolve 'Max client connections reached' errors."
echo ""
echo "Note: If you still see connection issues, consider:"
echo "  1. Using Supabase Session Pooler (port 5432) instead of Transaction Pooler (6543)"
echo "  2. Reducing to 1 machine: fly scale count 1 --app student-performance-api"
echo "  3. Upgrading your Supabase plan for more connections"

