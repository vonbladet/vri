import reader, dbf_reader
import simplejson as json

f = file("world_adm0.shp")
reader.read_header(f)
points = reader.read_records(f)

f2 = file("world_adm0.dbf")
d = dbf_reader.DBFReader(f2)

if len(d.data)!=len(points):
    raise RuntimeError, "Different number of entries"

gis_all = []
for component_points, description in zip(points, d.data):
    gis_all.append({'region' : description['REGION'],
                'name' : description['NAME'],
                'components' : component_points})

f = file("gis.json", "w")
json.dump(gis_all, f, indent=4)

import pickle

pickle.dump(gis_all, file("gis.pkl", 'w'))

