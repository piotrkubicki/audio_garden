import os

from app import application, db, Location, Transmitter, helpers
from flask import jsonify, send_file, url_for, render_template, request, redirect, session

@application.errorhandler(404)
def page_not_found(error):
    return redirect(url_for('admin_locations'))

@application.route('/')
def index():
    return ''

@application.route('/locations', methods=['GET'])
def get_locations():
    locations = Location.query.all()
    locations_list = []

    for location in locations:
        temp_location = {
            'location_id': location.location_id,
            'name': location.name,
            'position': {
                'longitude': location.longitude,
                'latitude': location.latitude,
            },
            'transmitters': []
        }

        for transmitter in location.transmitters:
            temp_location['transmitters'].append(transmitter.transmitter_id)

        locations_list.append(temp_location)

    loc = {}
    loc['locations'] = locations_list

    return jsonify(loc)

@application.route('/locations/<location_id>/intro', methods=['GET'])
def location_get_intro(location_id):
    base_path = 'static/audio/' + location_id + '/'
    filename = 'intro_' + location_id + '.mp3'

    return send_file(base_path + filename, mimetype='audio/mp3', as_attachment=True, attachment_filename=filename)

@application.route('/locations/<location_id>/<transmitter_id>/background', methods=['GET'])
def get_transmitter_bg_sound(location_id, transmitter_id):
    base_path = 'static/audio/' + location_id + '/' + transmitter_id + '/background/'
    transmitter = Transmitter.query.get(transmitter_id)
    filename = transmitter.background_track

    return send_file(base_path + filename, mimetype='audio/mp3', as_attachment=True, attachment_filename=filename)

@application.route('/locations/<location_id>/<transmitter_id>/voice', methods=['GET'])
def get_transmitter_voice_sound(location_id, transmitter_id):
    base_path = 'static/audio/' + location_id + '/' + transmitter_id + '/voice/'
    transmitter = Transmitter.query.get(transmitter_id)
    filename = transmitter.voice_track

    return send_file(base_path + filename, mimetype='audio/mp3', as_attachment=True, attachment_filename=filename)

@application.route('/admin/signin', methods=['POST'])
def admin_signin():
    username = request.form['username']
    password = request.form['password']
    if helpers.authenticate(username, password):
        return redirect('admin/locations')

@application.route('/admin/signout', methods=['GET'])
def admin_signout():
    session['username'] = ''
    session['token'] = ''

    return redirect(url_for('admin_locations'))

@application.route('/admin/locations', methods=['GET'])
@helpers.require_auth
def admin_locations():
    locations = Location.query.all()

    return render_template('locations.html', locations=locations)

@application.route('/admin/locations/<id>', methods=['GET'])
@helpers.require_auth
def admin_location(id):
    location = Location.query.get(id)

    return render_template('location.html', location=location)

@application.route('/admin/locations/add', methods=['GET', 'POST'])
@helpers.require_auth
def admin_add_location():
    if request.method == 'GET':
        return render_template('location_form.html', location=None)

    intro_sound = request.files['intro']

    if intro_sound.filename != '' and request.form['name'] != '':
        location = Location(name=request.form['name'], longitude=request.form['longitude'], latitude=request.form['latitude'])

        db.session.add(location)
        db.session.commit()

        newpath = r'app/static/audio/' +  str(location.location_id)

        if not os.path.exists(newpath):
            os.makedirs(newpath)
            intro_sound.save(os.path.join(newpath, 'intro_' + str(location.location_id) + '.mp3'))

    return redirect('/admin/locations')

@application.route('/admin/locations/<id>/edit', methods=['GET', 'POST'])
@helpers.require_auth
def admin_edit_location(id):
    if request.method == 'GET':
        location = Location.query.get(id)
        return render_template('location_form.html', location=location)

    location = Location.query.get(id)

    if request.form['name'] != '':
        location.name = request.form['name']

    if request.form['latitude'] != '':
        location.latitude = request.form['latitude']

    if request.form['longitude'] != '':
        location.longitude = request.form['longitude']

    db.session.commit()

    return redirect('/admin/locations')

@application.route('/admin/locations/<id>/delete', methods=['POST'])
@helpers.require_auth
def admin_delete_location(id):
    location = Location.query.get(id)

    if location is not None:
        helpers.delete_location(location)

    return redirect('/admin/locations')

@application.route('/admin/locations/<id>/transmitters/add', methods=['GET', 'POST'])
@helpers.require_auth
def admin_add_transmitter(id):
    if request.method == 'GET':
        return render_template('transmitter_form.html', transmitter=None, location_id=id)

    location = Location.query.get(id)

    if location is not None:
        helpers.create_transmitter(request, location.location_id)

    return redirect('/admin/locations/' + id)

@application.route('/admin/locations/<id>/transmitters/<transmitter_id>/edit', methods=['GET', 'POST'])
@helpers.require_auth
def admin_edit_transmitter(id, transmitter_id):
    if request.method == 'GET':
        transmitter = Transmitter.query.get(transmitter_id)

        return render_template('transmitter_form.html', transmitter=transmitter, location_id=id)

    transmitter = Transmitter.query.get(transmitter_id)

    if 'transmitter_id' in request.form and request.form['transmitter_id'] is not '':
        helpers.edit_transmitter(id, transmitter, request)

    location = Location.query.get(id)

    return redirect('/admin/locations/' + id)

@application.route('/admin/locations/<id>/transmitters/<transmitter_id>/delete', methods=['POST'])
@helpers.require_auth
def admin_delete_transmitter(id, transmitter_id):
    transmitter = Transmitter.query.get(transmitter_id)

    helpers.remove_transmitter(id, transmitter)

    location = Location.query.get(id)

    return redirect('/admin/locations/' + str(location.location_id))
