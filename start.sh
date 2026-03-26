#!/bin/bash
# ─────────────────────────────────────────────────────────────
# MathLit Backend — Production Startup Script
# Server pe run karo: bash start.sh
# ─────────────────────────────────────────────────────────────

# Paths (server pe adjust karo)
APP_DIR="/opt/mathlit"
CONFIG_DIR="/etc/mathlit"
LOG_DIR="/var/log/mathlit"
JAR_NAME="backend-0.0.1-SNAPSHOT.jar"

# Directories create karo agar nahi hain
mkdir -p "$LOG_DIR"
mkdir -p "$CONFIG_DIR"

echo "Starting MathLit Backend..."
echo "Config:  $CONFIG_DIR/application.properties"
echo "Logs:    $LOG_DIR/"

java -jar "$APP_DIR/$JAR_NAME" \
  --spring.config.location="$CONFIG_DIR/application.properties" \
  --logging.file.path="$LOG_DIR" \
  --spring.profiles.active=prod
