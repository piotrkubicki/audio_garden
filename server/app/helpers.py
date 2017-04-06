import os
import shutil
import bcrypt

from app import db, User, Token, Transmitter, application
from flask import render_template, session, redirect, url_for, request, flash
from functools import wraps
from werkzeug.utils import secure_filename

def authenticate(username, password):
  if username == '':
    return False
  user = User.query.filter_by(username=username).first()
  if user is None:
    return False

  if user.pass_hash== bcrypt.hashpw(password.encode('utf-8'), user.pass_hash.encode('utf-8')):
    session['username'] = username
    session['token'] = Token(application.config['tokens_location'], 'tokens').generate(username)

    return True
  flash(u'Credentials don\'t match any record!', 'error')

  return False

def require_auth(f):
  '''Redirect user if not authorised.'''
  @wraps(f)
  def decorated(*args, **kwargs):
    if 'token' in session and 'username' in session:
      token = session['token']
      username = session['username']
    else:
      return render_template('login_form.html')

    if not Token(application.config['tokens_location'], 'tokens').validate(username, token, 10):
      return render_template('login_form.html')

    Token(application.config['tokens_location'], 'tokens').update(token)

    return f(*args, **kwargs)

  return decorated

def create_transmitter(data, location_id):
    bg_track = data.files['background_track']
    voice_track = data.files['voice_track']

    if bg_track.filename != '' and voice_track.filename != '':
        transmitter = Transmitter(transmitter_id=request.form['transmitter_id'], background_track=bg_track.filename, voice_track=voice_track.filename, location_id=location_id)
        db.session.add(transmitter)
        db.session.commit()

        transmitter_path = r'app/static/audio/' +  str(location_id) + '/' + str(transmitter.transmitter_id)
        transmitter_bg_path = transmitter_path + r'/background/'
        transmitter_voice_path = transmitter_path + r'/voice/'

        if not os.path.exists(transmitter_bg_path):
            os.makedirs(transmitter_bg_path)

        if not os.path.exists(transmitter_voice_path):
            os.makedirs(transmitter_voice_path)

        bg_track.save(os.path.join(transmitter_bg_path, secure_filename(bg_track.filename)))

        voice_track.save(os.path.join(transmitter_voice_path, secure_filename(voice_track.filename)))

def remove_transmitter(location_id, transmitter):
    location_path = 'app/static/audio/' + str(location_id) + '/'

    shutil.rmtree(os.path.join(location_path, str(transmitter.transmitter_id)))

    db.session.delete(transmitter)
    db.session.commit()

def edit_transmitter(location_id, transmitter, data):
    location_path = 'app/static/audio/' + str(location_id) + '/'
    transmitter_path = location_path + str(data.form['transmitter_id'])
    transmitter_bg_path = transmitter_path + '/background/'
    transmitter_voice_path = transmitter_path + '/voice/'

    os.rename(os.path.join(location_path, str(transmitter.transmitter_id)), transmitter_path)
    transmitter.transmitter_id = data.form['transmitter_id']

    if data.files['background_track'].filename != '':
        bg_track = data.files['background_track']
        bg_track.save(os.path.join(transmitter_bg_path, secure_filename(bg_track.filename)))
        os.remove(os.path.join(transmitter_bg_path, transmitter.background_track))
        transmitter.background_track = bg_track.filename

    if data.files['voice_track'].filename != '':
        voice_track = data.files['voice_track']
        voice_track.save(os.path.join(transmitter_voice_path, secure_filename(voice_track.filename)))
        os.remove(os.path.join(transmitter_voice_path, transmitter.voice_track))
        transmitter.voice_track = voice_track.filename

    db.session.commit()

def delete_location(location):
    for transmitter in location.transmitters:
        remove_transmitter(location.location_id, transmitter)

    location_path = 'app/static/audio/' + str(location.location_id) + '/'

    shutil.rmtree(location_path)
    db.session.delete(location)
    db.session.commit()
