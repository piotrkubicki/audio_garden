from base_config import SQLALCHEMY_DATABASE_URI
from app import db

db.drop_all()
db.create_all()

#################### TO DELETE ###########################
from app import Location
from app import Transmitter

l = Location('Audio Garden', '1', '1')
t1 = Transmitter(1, '1.mp3', '1.mp3', 1)
t2 = Transmitter(2, '2.mp3', '2.mp3', 1)

db.session.add(l)
db.session.add(t1)
db.session.add(t2)
db.session.commit()
