from base_config import SQLALCHEMY_DATABASE_URI
from app import db

db.drop_all()
db.create_all()

#################### TO DELETE ###########################
from app import Location
from app import Transmitter

l = Location('Audio Garden', '1', '1')
l2 = Location('Dummy Place', '3', '5')
t1 = Transmitter('00:1A:7D:DA:71:11', '1.mp3', '1.mp3', 1)
#t2 = Transmitter('0000fe9f-0000-1000-8000-00805f9b34fb', '2.mp3', '2.mp3', 1)
t2 = Transmitter('54:60:09:42:4F:05', '2.mp3', '2.mp3', 1)

db.session.add(l)
db.session.add(t1)
db.session.add(t2)
db.session.add(l2)
db.session.commit()
