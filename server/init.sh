#!/bin/bash
mkdir -p app/var
touch app/var/logs.log      # create logs file
touch app/var/database.db
python db_create.py         # populate database with tabels

while getopts ":p" option; do
  case "${option}" in
    p)  echo "populating database"
        python populate.py
      ;;
  esac
done
