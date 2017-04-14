# Audio Garden Project
 
#### How to run the server
Install python pip
```sh
$ sudo apt-get update & sudo apt-get install python-pip -y
```
Install python development package required by bcrypt module
```sh
$ sudo apt-get update $ sudo apt-get install python-dev -y
```
Install python virtual enviroment package
```sh
$ pip install virtualenv
```
Inside application root directory (server) create new virtual enviroment
```sh
$ virtualenv ENV
```
Activate just create virtual environment
```sh
$ source ENV/bin/activate
```
Install required modules
```sh
pip install -r requirements.txt
```
Use init.sh script to create missing files and directories and to initialise database. Script accept three options
  - -c option create files and directories, also initialise database. By running this option again you can remove all data stored on the server.
  - -p populate database with initial data define inside populate.py script.
  - -u option allow to create new admin user.

  When run for the first time it is recommended to run script with all options
```sh
$ sh init.sh -cpu
```
Run server
```sh
$ python application.py
```
