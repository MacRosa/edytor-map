CREATE TABLE IF NOT EXISTS appuser(
  id SERIAL PRIMARY KEY,
  email text NOT NULL,
  password text NOT NULL,
  name text NOT NULL
);