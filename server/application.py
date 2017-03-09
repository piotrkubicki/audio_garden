import ConfigParser
import logging

from logging.handlers import RotatingFileHandler
from app import app

from gevent.wsgi import WSGIServer

def init(app):
    config = ConfigParser.ConfigParser()

    try:

        config_location = 'app/var/development.cfg'                             # this dir and file is not included, change this to change configuration
        config.read(config_location)

        # application config
        app.config['DEBUG'] = config.get('config', 'debug')
        app.config['ip_address'] = config.get('config', 'ip_address')
        app.config['port'] = config.get('config', 'port')
        app.config['url'] = config.get('config', 'url')

        # logging config
        app.config['log_file'] = config.get('logging', 'file')
        app.config['log_location'] = config.get('logging', 'location')
        app.config['log_level'] = config.get('logging', 'level')
    except:
        print 'Could not read config from: %s' % config_location

def logs(app):
    log_pathname = app.config['log_location'] + app.config['log_file']
    file_handler = RotatingFileHandler(log_pathname, maxBytes = 1024 * 1024 * 10, backupCount = 1024)
    file_handler.setLevel(app.config['log_level'])
    formatter = logging.Formatter('%(levelname)s | %(asctime)s | %(module)s | %(funcName)s | %(message)s')
    file_handler.setFormatter(formatter)
    app.logger.setLevel(app.config['log_level'])
    app.logger.addHandler(file_handler)

if __name__ == '__main__':
    init(app)
    logs(app)
    #app.run(host=app.config['ip_address'], port=int(app.config['port']))
    http_server = WSGIServer(('', 5000), app)
    http_server.serve_forever()