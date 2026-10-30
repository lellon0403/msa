#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd -- "$SCRIPT_DIR/.." && pwd)"
mkdir -p "$ROOT_DIR/microservices"
cd "$ROOT_DIR/microservices"

create_service() {
    local name="$1"
    local package_name="$2"
    if [[ -e "$name" ]]; then
        printf 'Skip existing path: %s\n' "$name"
        return
    fi
    if ! command -v spring >/dev/null 2>&1; then
        printf 'Spring CLI is required to create %s.\n' "$name" >&2
        exit 1
    fi
    spring init \
        --boot-version=4.0.8 \
        --type=gradle-project \
        --java-version=21 \
        --packaging=jar \
        --name="$name" \
        --package-name="$package_name" \
        --groupId="$package_name" \
        --dependencies=actuator,webflux \
        --version=1.0.0-SNAPSHOT \
        "$name"
}

create_service product-service se.magnus.microservices.core.product
create_service recommendation-service se.magnus.microservices.core.recommendation
create_service review-service se.magnus.microservices.core.review
create_service product-composite-service se.magnus.microservices.composite.product
