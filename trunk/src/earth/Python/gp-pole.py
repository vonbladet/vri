import glob

print "set size ratio 1.0"
print "set term gif"
for d in range(0, 360, 5):
    print 'set out "img/proj-pole%+04d.gif"' % (360-d)
    print 'plot "dat/proj-pole%+04d.dat" w l' % d
