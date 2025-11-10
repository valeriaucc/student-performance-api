#!/bin/bash
# Script to fix JWT secret mismatch
# This will update the SUPABASE_JWT_SECRET in Fly.io

echo "=========================================="
echo "Fix JWT Secret Mismatch"
echo "=========================================="
echo ""
echo "The error 'Invalid signature' means the JWT secret doesn't match."
echo ""
echo "Steps:"
echo "1. Go to: https://supabase.com/dashboard"
echo "2. Select your project"
echo "3. Go to: Settings → API"
echo "4. Scroll down to 'JWT Settings'"
echo "5. Copy the EXACT 'JWT Secret' value"
echo ""
echo "Then run this command with your JWT secret:"
echo ""
echo "fly secrets set SUPABASE_JWT_SECRET=\"your-exact-jwt-secret-here\" --app student-performance-api"
echo ""
echo "⚠️  IMPORTANT:"
echo "   - Copy the secret EXACTLY (no extra spaces)"
echo "   - The secret must match what Supabase uses to sign tokens"
echo "   - After updating, the app will automatically restart"
echo ""

