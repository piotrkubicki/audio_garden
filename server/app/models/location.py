from app import db

class Location(db.Model):
    __tablename__ = 'locations'
    __table_args__ = (db.UniqueConstraint('longitude', 'latitude'), {})
    location_id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(100), unique=True, nullable=False)
    longitude = db.Column(db.Float)
    latitude = db.Column(db.Float)
    transmitters = db.relationship('Transmitter', backref='location', lazy='select')

    def __init__(self, name, longitude, latitude):
        self.name = name
        self.longitude = longitude
        self.latitude = latitude

    def __repr__(self):
        return 'Location id=%s name=%s, longitude=%s, latitude=%s' % (self.location_id, self.name, self.longitude, self.latitude)
