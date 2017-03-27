import ConfigParser
import logging

from logging.handlers import RotatingFileHandler
from app import application

def init(application):
    config = ConfigParser.ConfigParser()

    try:

        config_location = 'app/var/development.cfg'                             # this dir and file is not included, change this to change configuration
        config.read(config_location)

        # application config
        application.config['DEBUG'] = config.get('config', 'debug')
        application.config['ip_address'] = config.get('config', 'ip_address')
        application.config['port'] = config.get('config', 'port')
        application.config['url'] = config.get('config', 'url')

        # logging config
        application.config['log_file'] = config.get('logging', 'file')
        application.config['log_location'] = config.get('logging', 'location')
        application.config['log_level'] = config.get('logging', 'level')
    except:
        print 'Could not read config from: %s' % config_location

def logs(application):
    log_pathname = application.config['log_location'] + application.config['log_file']
    file_handler = RotatingFileHandler(log_pathname, maxBytes = 1024 * 1024 * 10, backupCount = 1024)
    file_handler.setLevel(application.config['log_level'])
    formatter = logging.Formatter('%(levelname)s | %(asctime)s | %(module)s | %(funcName)s | %(message)s')
    file_handler.setFormatter(formatter)
    application.logger.setLevel(application.config['log_level'])
    application.logger.addHandler(file_handler)

if __name__ == '__main__':
    init(application)
    logs(application)
    application.debug = True
    application.run(host='0.0.0.0', port=5000)
