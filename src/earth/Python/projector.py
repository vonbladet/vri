import pickle
import sys
from math import sin, cos, radians, asin, acos, degrees

def signum(x):
    if x<0:
        return -1
    elif x>0: 
        return 1
    else:
        return 0

def arange(v1, v2, step):
    def test(x):
        if signum(step) == 1:
            return x<=v2
        else:
            return x>=v2
    if not test(v1):
        return []
    x = v1
    res = []
    while test(x):
        res.append(x)
        x+=step
    return res

# set([u'Europe', u'North America', u'Australia', u'Caribbean', 
# u'Sub Saharan Africa', u'NorthAfrica', u'Antarctica', 
# u'Asia', u'Pacific', u'Latin America'])

class Projector(object):
    def __init__(self):
        self.lon_ref = 0.0
        self.lat_ref = 0.0
    def setLonRef(self, lon_ref):
        self.lon_ref = lon_ref
    def setLatRef(self, lat_ref):
        self.lat_ref = lat_ref
    def shift_lon(self, lon):
        return ((lon-self.lon_ref) % 360.0) - 180
    def lon_inside_horizon(self, lon):
        da = (lon-self.lon_ref) % 360.0
        return da < 90.0 or da > 270.0
    def lon_apply_horizon(self, lon):
        da = self.shift_lon(lon)
        return ((self.lon_ref+signum(da)*90) % 360)-180
    def apply_horizon_lat_point(self, c):
        lon, lat = c
        d = lat-self.lat_ref
        if d > 90:
            status = 'toonorth'
            nlon, nlat = (lon % 360)-180, self.lat_ref + 180 - d
        elif d < -90:
            status = 'toosouth'
            nlon, nlat = (lon % 360)-180, self.lat_ref - 180 - d
        else:
            status = 'normal'
            nlon, nlat = lon, lat
        return status, nlon, nlat
    def apply_horizon_lat(self, c):
        new_contours = []
        old_status, lat, lon = self.apply_horizon_lat_point(c[0])
        current_contour = [(lat, lon)]
        for i in range(1, len(c)):
            status, lon, lat = self.apply_horizon_lat_point(c[i])
            if status == old_status:
                current_contour.append((lon, lat))
            else:
                # print "changing northitude"
                new_contours.append(current_contour)
                current_contour = [(lon, lat)]
            old_status = status
        new_contours.append(current_contour)
        return new_contours
    def apply_horizon_lon(self, c):
        # first split, then project
        insides = [self.lon_inside_horizon(lon) for (lon, lat) in c]
        if not any(insides):
            return []
        switches = []
        for i in range(len(insides)-1):
            if insides[i] != insides[i+1]:
                switches.append(i+1)
        if switches==[] or switches[0]!=0:
            switches.insert(0, 0)
        if switches[-1] != len(c)-1:
            switches.append(len(c))
        new_deg_contours = []
        for i in range(len(switches)-1):
            if insides[switches[i]]:
                new_deg_contours.append(c[switches[i]:switches[i+1]])
    #         else:
    #             lon1, lat1 = c[switches[i]]
    #             lon2, lat2 = c[switches[i+1]-1]
    #             lon = lon_apply_horizon(lon)
    #             new_deg_contours.append([(lon, lat) 
    #                                      for lat in arange(lat1, lat2, signum(lat2-lat1)*0.0005)])
        return new_deg_contours
    def project_point(self, p):
        lon, lat = p
        lon = lon-self.lon_ref
        lat = lat-self.lat_ref
        return (sin(radians(lon))*cos(radians(lat)), sin(radians(lat)))
    def project(self, contour):
        points = [self.project_point(p) for p in contour]
        return points 
    def plot_map(self, f, active):
        for nation in active:
            for c in nation['components']:
                new_lat_contours = self.apply_horizon_lat(c)
                for c1 in new_lat_contours:
                    new_lon_contours = self.apply_horizon_lon(c1)
                    new_cs = [self.project(cont) for cont in new_lon_contours]
                    for new_c in new_cs:
                        lats = []
                        lons = []
                        for p in new_c:
                            lon, lat = p
                            lats.append(lat)
                            lons.append(lon)
                        if abs(max(lats)-min(lats)) > 0.5:
                            print "Nation", nation['name'], "ys", min(lats), max(lats)
                            # continue
                        for p in new_c:
                            print >>f, "%.4f %.4f" % tuple(p)
                        print >>f
        for deg in arange(0, 360, 0.05):
            print >>f, sin(radians(deg)), cos(radians(deg))

class NewProjector(object):
    def __init__(self):
        self.lon_ref = 0.0
        self.lat_ref = 0.0
    def setLonRef(self, lon_ref):
        self.lon_ref = lon_ref
    def setLatRef(self, lat_ref):
        self.lat_ref = lat_ref
    def project(self, contour):
        points3d = []
        for p in c:
            lon, lat = [radians(d) for d in p]
            x = cos(lat)*sin(lon+radians(self.lon_ref))
            y = cos(lat)*cos(lon+radians(self.lon_ref))
            z = sin(lat)
            # Rotate by latitude
            x1 = x
            lat_ref = radians(self.lat_ref)
            y1 = cos(lat_ref)*y - sin(lat_ref)*z
            z1 = sin(lat_ref)*y + cos(lat_ref)*z
            points3d = [x1, y1, z1]
        return points3d
    def plot_map(self, f, active):
        for nation in active:
            pos3d = []
            for c in nation['components']:
                c3d = project(c)
                for x1, y1, z1 in c3d:
                    if y1>=0:
                        print >>f, "%.4f %.4f" % (x1, z1)
#                         lat1 = asin(z)
#                         lon1 = p[0]+self.lon_ref
#                         print >>f, "%.4f %.4f" % (((lon1+180) % 360) - 180,
#                                                   degrees(lat1))
                    else:                        
                        print >>f
                print >>f
        for deg in arange(0, 360, 0.05):
            print >>f, sin(radians(deg)), cos(radians(deg))
