import os
import bcrypt

from app import db, User, Token, application
from flask import render_template, session, redirect, url_for
from functools import wraps

def authenticate(username, password):
  if username == '':
    return False
  user = User.query.filter_by(username=username).first()
  if user.pass_hash is None:
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

def create_transmitter(data):
    bg_track = data.files['background_track']
    voice_track = data.files['voice_track']

    if bg_track.filename != '':
        bg_track.save(os.path.join('app/static/audio/background/', secure_filename(bg_track.filename)))

    if voice_track.filename != '':
        voice_track.save(os.path.join('app/static/audio/voice/', secure_filename(voice_track.filename)))

    transmitter = Transmitter(transmitter_id=request.form['transmitter_id'], background_track=bg_track.filename, voice_track=voice_track.filename, location_id=location.location_id)
    db.session.add(transmitter)
    db.session.commit()

def remove_transmitter(transmitter):
    if transmitter.background_track != '':
        os.remove(os.path.join('app/static/audio/background/', transmitter.background_track))

    if transmitter.voice_track != '':
        os.remove(os.path.join('app/static/audio/voice/', transmitter.voice_track))

    db.session.delete(transmitter)
    db.session.commit()

def edit_transmitter(transmitter, data):
    transmitter.transmitter_id = data.form['transmitter_id']

    if 'background_track' in data.form and data.files['background_track'].filename is not '':
        bg_track.save(os.path.join('app/static/audio/background/', secure_filename(bg_track.filename)))
        os.remove(os.path.join('app/static/audio/background/', transmitter.background_track))
        transmitter.background_track = bg_track.filename

    if 'voice_track' in data.form and data.files['voice_track'].filename is not '':
        voice_track.save(os.path.join('app/static/audio/voice/', secure_filename(voice_track.filename)))
        os.remove(os.path.join('app/static/audio/voice/', transmitter.voice_track))
        transmitter.voice_track = voice_track.filename

    db.session.commit()

def delete_location(location):
    for transmitter in location.transmitters:
        remove_transmitter(transmitter)

    os.remove(os.path.join('app/static/audio/', 'intro_' + str(location.location_id) + '.mp3'))

    db.session.delete(location)
    db.session.commit()
