#
# DDL to create the layout_update table.
#
# TABLE KEY: the updated table name
#

CREATE TABLE layout_update WITH DESCRIPTION 'Layout update table.'
ROW KEY FORMAT RAW
WITH LOCALITY GROUP default WITH DESCRIPTION 'Main locality group.' (
  MAXVERSIONS = INFINITY,
  TTL = FOREVER,
  INMEMORY = false,
  FAMILY update_log WITH DESCRIPTION 'Layout update information.' (
    layout_update CLASS com.opower.updater.LayoutUpdate
  )
);