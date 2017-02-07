from app import db

class Location(db.Model):
    __tablename__ = 'locations'
    __table_args__ = (db.UniqueConstraint('longitude', 'latitude'), {})
    location_id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(100), unique=True)
    longitude = db.Column(db.Float)
    latitude = db.Column(db.Float)

    def __init__(self, location_id, name, longitude, latitude):
        self.location_id = location_id
        self.name = name
        self.longitude = longitude
        self.latitude = latitude

    def __repr__(self):
        return 'Location name=%s, longitude=%s, latitude=%s' % (self.name, self.longitude, self.latitude)
