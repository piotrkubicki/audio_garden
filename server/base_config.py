import os
basedir = os.path.abspath(os.path.dirname(__file__))

CSRF_ENABLED = True
SECRET_KEY = 'O.G<;ud0P0m(t6W8L856sH?c9uYm9$'

SQLALCHEMY_DATABASE_URI = 'sqlite:///tmp/database.db'
SQLALCHEMY_TRACK_MODIFICATIONS = False
