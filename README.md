### Notes
- reports.db file should be kept in root path.
- Create 'reports' table in sqlite3 db by executing sql statement: CREATE TABLE reports (id_short TEXT, timestamp INTEGER, datePublished INTEGER, payload TEXT, id TEXT, statusCode INTEGER, PRIMARY KEY(id_short,timestamp))
