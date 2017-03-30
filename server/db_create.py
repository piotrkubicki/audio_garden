from base_config import SQLALCHEMY_DATABASE_URI
from app import db

db.drop_all()
db.create_all()

#################### TO DELETE ###########################
from app import Location
from app import Transmitter

l = Location('Lions Gate', '55.9340812', '-3.211218217')
t1 = Transmitter('00:A0:50:12:27:0F', 'lions_gate_intro.mp3', 'lions_gate_intro.mp3', 1)
t2 = Transmitter('00:A0:50:12:1A:12', 'lions_gate_guilds.mp3', 'lions_gate_guilds.mp3', 1)

db.session.add(l)
db.session.add(t1)
db.session.add(t2)
db.session.commit()
