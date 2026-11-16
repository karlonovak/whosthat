#!/bin/bash
# Restarts a whosthat instance whose log has been silent for more than 5 minutes. A healthy instance logs
# "Motion stream alive" every minute even without motion, so silence means the app itself is stuck (a hung stream
# reconnects on its own after 60 s). Run by cron every minute; prints only when it restarts something.
for c in whosthat_kuca_ulaz whosthat_ured_ulaz; do
  last=$(docker logs --tail 1 --timestamps "$c" 2>&1 | awk '{print $1}')
  [ -z "$last" ] && continue
  age=$(( $(date +%s) - $(date --date="$last" +%s) ))
  if [ "$age" -gt 300 ]; then
    echo "$(date -Is) $c silent for ${age}s, restarting"
    docker restart "$c" >/dev/null
  fi
done
