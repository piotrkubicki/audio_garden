from flask import Flask
from flask_sqlalchemy import SQLAlchemy

app = Flask(__name__)
app.config.from_object('base_config')
db = SQLAlchemy(app)

# import all models
from app.models.location import Location
from app.models.transmitter import Transmitter
