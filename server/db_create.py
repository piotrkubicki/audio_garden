from base_config import SQLALCHEMY_DATABASE_URI
from app import db

db.drop_all()
db.create_all()

from app.models.location import Location
