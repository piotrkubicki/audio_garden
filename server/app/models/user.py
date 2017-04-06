from app import db

class User(db.Model):
    user_id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(100), unique=True, nullable=False)
    pass_hash = db.Column(db.String(16), nullable=False)

    def get_pass(self):
        return self.pass_hash
