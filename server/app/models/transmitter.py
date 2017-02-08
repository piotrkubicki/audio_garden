from app import db

class Transmitter(db.Model):
    __tablename__ = 'transmitters'
    transmitter_id = db.Column(db.Integer, primary_key=True)
    background_track = db.Column(db.String(100), nullable=False)
    voice_track = db.Column(db.String(100), nullable=False)
    location_id = db.Column(db.Integer, db.ForeignKey('locations.location_id'))

    def __init__(self, transmitter_id, background_track, voice_track, location_id):
        self.background_track = background_track
        self.voice_track = voice_track
        self.location_id = location_id

    def __repr__(self):
        return 'Transmitter id=%s, background_track=%s, voice_track=%s' % (self.transmitter_id, self.background_track, self.voice_track)
