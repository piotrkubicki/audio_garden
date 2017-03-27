from base_config import SQLALCHEMY_DATABASE_URI
from app import db

db.drop_all()
db.create_all()

#################### TO DELETE ###########################
from app import Location
from app import Transmitter

l = Location('Audio Garden', '55.9340812', '-3.2112182,17)
t1 = Transmitter('00:1A:7D:DA:71:11', 'birds.m4a', 'Intro.m4a', 1)
t2 = Transmitter('00:A0:50:12:1A:12', 'rain.m4a', 'Guilds.m4a', 1)

db.session.add(l)
db.session.add(t1)
db.session.add(t2)
db.session.commit()
