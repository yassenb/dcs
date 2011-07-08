#!/usr/bin/env python

import sqlite3

connection = sqlite3.connect("dcs.db")
connection.execute("CREATE TABLE servers (id INTEGER PRIMARY KEY, ip VARCHAR UNIQUE)")
