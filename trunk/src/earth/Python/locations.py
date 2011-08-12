import re, simplejson as json

"""Combines the location.dat and location.txt files to get station names, codes
and locations."""


f = file("locations.dat")
s = f.read() 
blocks = s.split('/') 

codes = {}
for b in blocks:
    def_dict = {}
    lines = [l.strip() for l in b.split('\r\n') if l.strip()]
    for l in lines:
        if l.startswith('!'): 
            continue
        else:
            l2 = re.sub('\s*=\s*', '=', l)
            # FIXME: breaks on quoted spaces
            defs = re.findall('[^\s]+=(?:\'[^\']*\'|[^\s]+)', l2)
            for d in defs:
                lhs, rhs = d.split('=')
                def_dict[lhs] = rhs
    if def_dict:
        codes[def_dict["DBCODE"]] = def_dict["DBNAME"]

f2 = file("locations.txt")

info = []
for l in f2.readlines():
    code, lat, lon, height = l.split()
    lat = float(lat)
    lon = float(lon)
    info.append({'code' : code,
                 'name' : codes[code],
                 'position' : [lon, lat]})
    
json.dump(info, file('locations.json', 'w'), sort_keys=True, indent=4)
