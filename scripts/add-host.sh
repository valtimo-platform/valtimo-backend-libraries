#!/bin/bash
set -e

# ============================================================
# Usage:
#   sh ./scripts/add-host.sh <hostname> [ip]
#
# Examples:
#   sh ./scripts/add-host.sh keycloak.localhost
#     → Adds "127.0.0.1 keycloak.localhost"
#
#   sh ./scripts/add-host.sh my-service.local 192.168.1.50
#     → Adds "192.168.1.50 my-service.local"
#
# Notes:
# - Requires sudo because /etc/hosts is a root-owned file.
# - Will not add duplicate entries if the hostname already exists.
# ============================================================

HOSTNAME=$1
IP=${2:-127.0.0.1}   # default to 127.0.0.1 if not provided
HOSTS_FILE="/etc/hosts"

if [ -z "$HOSTNAME" ]; then
  echo "❌ Usage: $0 <hostname> [ip]"
  exit 1
fi

ENTRY="$IP $HOSTNAME"

if ! grep -q "$HOSTNAME" "$HOSTS_FILE"; then
  echo "$ENTRY" | sudo tee -a "$HOSTS_FILE" > /dev/null
  echo "✅ Added $ENTRY to $HOSTS_FILE"
else
  echo "ℹ️  $HOSTNAME already present in $HOSTS_FILE"
fi
