from app import db, Location, Transmitter

l = Location('Lions Gate Garden', '55.9340812', '-3.211218217')
t1 = Transmitter('00:A0:50:12:27:0F', 'lions_gate_intro.mp3', 'lions_gate_intro.mp3', 1)
t2 = Transmitter('00:A0:50:12:1A:12', 'lions_gate_guilds.mp3', 'lions_gate_guilds.mp3', 1)

db.session.add(l)
db.session.add(t1)
db.session.add(t2)
db.session.commit()
