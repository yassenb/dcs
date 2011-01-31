#!/usr/bin/env python

import cgi
import os
import sqlite3

# TODO fix hard-coded value and as a general maybe move all communication related params in a third party
# also fix db location (and writable by everyone)
db_location = "/tmp/dcs.db"

def register(ip):
    connection = sqlite3.connect(db_location)
    with connection:
        connection.execute("REPLACE INTO servers (ip) VALUES (?)", (ip,))

def registered(ip):
    connection = sqlite3.connect(db_location)
    with connection:
        cursor = connection.cursor()
        return cursor.execute("SELECT ip FROM servers WHERE ip = ?", (ip,)).fetchone() != None


print("Content-Type: text/plain\n")

params = cgi.FieldStorage()
action = params.getfirst("action")
client_ip = cgi.escape(os.environ["REMOTE_ADDR"])
if action == "getmyip":
    print(client_ip)
elif action == "register":
    register(client_ip)
elif action == "check":
    if registered(client_ip):
        print("OK")
    else:
        print("not registered")

