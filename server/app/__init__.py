from flask import Flask
from flask_sqlalchemy import SQLAlchemy

application = Flask(__name__)
application.config.from_object('base_config')
db = SQLAlchemy(application)

# import all models
from app.models.location import Location
from app.models.transmitter import Transmitter
from app.models.user import User
from app.models.token import Token

from app import routes
