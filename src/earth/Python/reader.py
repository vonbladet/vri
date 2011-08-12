from struct import unpack
import sys

class Record(object):
    def __init__(self, xmin, ymin, xmax, ymax, parts):
        pass

def read_header(f):
    s = f.read(100)
    magic, u0, u1, u2, u3, u4, file_length = unpack('>7i', s[:7*4])
    if magic!=0x0000270a: 
        raise RuntimeError, "Wrong magic number"
    version, shape, minX, minY, maxX, maxY, minZ, maxZ, minM, maxM = unpack('<2i8d', s[7*4:100])
    if shape != 5:
        print RuntimeError, "Shape !=5"

def read_records(f):
    points = []
    while True:
        s = f.read(8)
        if not s: break
        rec_number, rec_length = unpack('>2i', s)
        s = f.read(4)
        (rec_shape,) = unpack('<i', s)
        if not rec_shape == 5:
            raise RuntimeError, "Shape !=5"
        rec_minX, rec_minY, rec_maxX, rec_maxY = unpack('<4d', f.read(4*8))
        rec_nparts, rec_npoints = unpack('<2i', f.read(8))
        print "nparts %d, npoints %d" % (rec_nparts, rec_npoints)
        rec_parts = []
        for i in range(rec_nparts):
            (p,) = unpack('<i', f.read(4))
            rec_parts.append(p)
        rec_raw_points = []
        for i in range(rec_npoints):
            point = unpack('<2d', f.read(2*8))
            rec_raw_points.append(point)
        rec_parts.append(rec_npoints)
        rec_points = []
        for i in range(rec_nparts):
            print "Parts", rec_parts[i],rec_parts[i+1]
            rec_points.append(rec_raw_points[rec_parts[i]:rec_parts[i+1]])
        points.append(rec_points)
    return points


if __name__=='__main__':
    f = file("world_adm0.shp")
    read_header(f)
    points = read_records(f)
