#
# DDL to create the example table.
#
# TABLE KEY: some string
#

CREATE TABLE example WITH DESCRIPTION 'Example table.'
ROW KEY FORMAT RAW
WITH LOCALITY GROUP default WITH DESCRIPTION 'Main locality group.' (
  MAXVERSIONS = INFINITY,
  TTL = FOREVER,
  INMEMORY = false,
  FAMILY familyOne WITH DESCRIPTION 'A family' (
    someString WITH SCHEMA ID 0
  )
);