#!/bin/bash
mkdir -p app/var

usage()
{
  echo "Usage: Prepare application enviroment

        OPTIONS:
        -c creates all required files and database
        -p populate database with initial data
        -u create new user"
}

create_config()
{
  echo "[config]
debug = True
ip_address = 0.0.0.0
port = 5000
url = http://192.168.0.9

[logging]
file = logs.log
location = app/var/
level = DEBUG

[tokens]
tokens_location = app/var/" > app/var/configuration.cfg
}

while getopts "hcpu" option; do
  case "${option}" in
    c)  echo "preparing required files"
        touch app/var/logs.log      # create logs file
        touch app/var/database.db
        touch app/var/tokens.json
        create_config
        python db_create.py         # populate database with tabels
      ;;
    p)  echo "populating database"
        python populate.py
      ;;
    u)  python create_user.py
      ;;
    h)  usage
      ;;
  esac
done
