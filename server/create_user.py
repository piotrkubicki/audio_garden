import bcrypt

from app import db, User

username = raw_input("Username: ")
password = raw_input("Password: ")

user = User(username=username, pass_hash=bcrypt.hashpw(password, bcrypt.gensalt()))
db.session.add(user)
db.session.commit()
