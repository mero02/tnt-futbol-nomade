#!/bin/bash

echo "=== Setup FutbolTNT ==="

if [ -f "app/google-services.json" ]; then
    echo "✓ google-services.json ya está en su lugar"
else
    echo ""
    echo "⚠️  Falta app/google-services.json"
    echo ""
    echo "Pedíselo a Franc (franciscoterrondg19@gmail.com) y ponelo en:"
    echo "  → $(pwd)/app/google-services.json"
    echo ""
    echo "Sin ese archivo la app no compila con Firebase."
    exit 1
fi

echo ""
echo "✓ Todo listo. Podés abrir el proyecto en Android Studio."
