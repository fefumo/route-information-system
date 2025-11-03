TRUNCATE TABLE routes RESTART IDENTITY CASCADE;
TRUNCATE TABLE locations RESTART IDENTITY CASCADE;
TRUNCATE TABLE users RESTART IDENTITY CASCADE;
TRUNCATE TABLE import_operations RESTART IDENTITY CASCADE;

INSERT INTO locations (name, x, y) VALUES
  ('Novigrad', 12.5, 7),
  ('Oxenfurt', 9.0, 6),
  ('Kaer Morhen', -8.0, 12),
  ('Vizima', 5.0, 4),
  ('Kaer Trolde', -15.0, 9),
  ('Nilfgaard', 25.0, 2),
  ('Velen', 7.0, 5);
