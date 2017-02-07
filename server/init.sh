#!/bin/bash

mkdir -p app/var
touch app/var/logs.log      # create logs file
python db_create.py         # populate database with tabels
