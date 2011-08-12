from projector import Projector
import pickle

gis_all = pickle.load(file("gis.pkl"))
regions = set([g['region'] for g in gis_all])
names = set([g['name'] for g in gis_all])
active = gis_all # [g for g in gis_all if g['region'] in ['Europe', 'North America']]

