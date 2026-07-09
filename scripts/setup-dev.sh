#!/bin/bash

# BiometricSIAE Development Setup Script

echo "=== BiometricSIAE Development Setup ==="
echo ""

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "Error: Node.js is not installed"
    echo "Please install Node.js 20+ from https://nodejs.org/"
    exit 1
fi

# Check Node.js version
NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 20 ]; then
    echo "Error: Node.js version must be 20 or higher"
    echo "Current version: $(node -v)"
    exit 1
fi

echo "✓ Node.js $(node -v) detected"

# Setup Web Admin
echo ""
echo "Setting up Web Admin..."
cd web-admin

if [ ! -f ".env.local" ]; then
    echo "Creating .env.local from example..."
    cp .env.local.example .env.local
    echo "⚠️  Please edit .env.local with your Firebase credentials"
fi

echo "Installing dependencies..."
npm install

echo "✓ Web Admin setup complete"

# Setup Firebase Functions
echo ""
echo "Setting up Firebase Functions..."
cd ../firebase/functions

echo "Installing dependencies..."
npm install

echo "✓ Firebase Functions setup complete"

# Go back to root
cd ../..

echo ""
echo "=== Setup Complete ==="
echo ""
echo "Next steps:"
echo "1. Edit web-admin/.env.local with your Firebase credentials"
echo "2. Edit android/local.properties with your Android SDK path"
echo "3. Place google-services.json in android/app/"
echo "4. Run 'cd web-admin && npm run dev' to start web admin"
echo "5. Open Android project in Android Studio"
echo ""
echo "For more information, see docs/"
