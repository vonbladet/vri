from projector import Projector, NewProjector
import pickle

gis_all = pickle.load(file("gis.pkl"))
regions = set([g['region'] for g in gis_all])
active = gis_all # [g for g in gis_all if g['region'] in ['Europe', 'North America']]


proj = NewProjector()
proj.setLatRef(-45)
for deg in range(0, 360, 5):
    proj.setLonRef(deg)
    f = open('dat/proj-tilt%+04d.dat' % deg, 'w')
    proj.plot_map(f, active)
