CREATE TABLE IF NOT EXISTS appuser(
  id SERIAL PRIMARY KEY,
  email text NOT NULL,
  password text NOT NULL,
  name text NOT NULL
);

CREATE TABLE IF NOT EXISTS map(
  id SERIAL PRIMARY KEY,
  name text NOT NULL,
  description text,
  visibility text NOT NULL,
  owner_id INTEGER REFERENCES appuser(id) NOT NULL
);

CREATE TABLE IF NOT EXISTS map_access(
  id SERIAL PRIMARY KEY,
  access_type text NOT NULL,
  map_id INTEGER REFERENCES map(id) NOT NULL,
  app_user_id INTEGER REFERENCES appuser(id) NOT NULL
);