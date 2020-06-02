#! /bin/sh
kill -9 $(ps aux | grep keycloak.migration.strategy | awk '{print $2}')
exit 0
