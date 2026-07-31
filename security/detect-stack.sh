#!/usr/bin/env bash
# detect-stack.sh – Detect backend technology stack and set STACK env var
# Usage: source detect-stack.sh

if [[ -f "composer.json" ]]; then
  export STACK="php"
elif [[ -f "package.json" ]]; then
  export STACK="node"
elif [[ -f "requirements.txt" ]]; then
  export STACK="python"
elif [[ -f "go.mod" ]]; then
  export STACK="go"
elif [[ -f "Gemfile" ]]; then
  export STACK="ruby"
else
  export STACK="unknown"
fi

echo "Detected stack: $STACK"
