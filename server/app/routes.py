from app import app
from app import Location
from app import Transmitter
from flask import jsonify, send_file

@app.route('/')
def index():
    return ''

@app.route('/locations', methods=['GET'])
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

@app.route('/locations/<location_id>/<transmitter_id>/background', methods=['GET'])
def get_transmitter_bg_sound(location_id, transmitter_id):
    base_path = 'static/audio/background/'
    transmitter = Transmitter.query.get(transmitter_id)
    filename = transmitter.background_track

    return send_file(base_path + filename, mimetype='audio/mp3', as_attachment=True, attachment_filename=filename)

@app.route('/locations/<location_id>/<transmitter_id>/voice', methods=['GET'])
def get_transmitter_voice_sound(location_id, transmitter_id):
    base_path = 'static/audio/voice/'
    transmitter = Transmitter.query.get(transmitter_id)
    filename = transmitter.voice_track

    return send_file(base_path + filename, mimetype='audio/mp3', as_attachment=True, attachment_filename=filename)
