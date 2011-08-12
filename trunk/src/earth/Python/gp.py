import glob

print "set size ratio 1.0"
print "set term gif"
for d in range(0, 360):
    print 'set out "proj%+04d.gif"' % (360-d)
    print 'plot "proj%+04d.dat" w l' % d
