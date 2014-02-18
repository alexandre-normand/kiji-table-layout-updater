#
# DDL to create the test table.
#
# TABLE KEY: some string
#

CREATE TABLE test WITH DESCRIPTION 'Test table.'
ROW KEY FORMAT (someString STRING)
PROPERTIES (NUMREGIONS = %%%num_regions%%%)
WITH LOCALITY GROUP default WITH DESCRIPTION 'Main locality group.' (
  MAXVERSIONS = INFINITY,
  TTL = FOREVER,
  INMEMORY = false,
  FAMILY test WITH DESCRIPTION 'Test record.' (
    stuff WITH SCHEMA ID 0
  )
);