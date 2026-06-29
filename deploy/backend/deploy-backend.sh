#!/usr/bin/env bash
set -euo pipefail

APP_DIR="/opt/subtrack"
JAR_NAME="subtrack-backend.jar"
SERVICE_NAME="subtrack-backend"
SERVICE_FILE="/etc/systemd/system/${SERVICE_NAME}.service"
SOURCE_JAR="${1:-./${JAR_NAME}}"
TARGET_JAR="${APP_DIR}/${JAR_NAME}"
APP_ENV="${APP_DIR}/app.env"

echo "Preparing SubTrack backend deployment..."

if [[ ! -f "${SOURCE_JAR}" ]]; then
  echo "ERROR: JAR file not found: ${SOURCE_JAR}" >&2
  echo "Pass the JAR path as the first argument, or place ${JAR_NAME} in the current directory." >&2
  exit 1
fi

if [[ ! -f "${APP_ENV}" ]]; then
  echo "WARNING: ${APP_ENV} does not exist." >&2
  echo "Create it from deploy/backend/app.env.example and fill real production values before starting the service." >&2
fi

if [[ ! -f "${SERVICE_FILE}" ]]; then
  echo "WARNING: ${SERVICE_FILE} does not exist." >&2
  echo "Copy deploy/backend/subtrack.service to ${SERVICE_FILE} and replace <ec2-app-user> before restarting." >&2
fi

sudo mkdir -p "${APP_DIR}"
sudo cp "${SOURCE_JAR}" "${TARGET_JAR}"
sudo chmod 644 "${TARGET_JAR}"

sudo systemctl daemon-reload
sudo systemctl enable "${SERVICE_NAME}"
sudo systemctl restart "${SERVICE_NAME}"

echo "SubTrack backend deployment command finished."
echo "Check service status:"
echo "  sudo systemctl status ${SERVICE_NAME} --no-pager"
echo "Follow logs:"
echo "  sudo journalctl -u ${SERVICE_NAME} -f"
