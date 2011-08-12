f = file('mk5ad.ctl')
[line] = [l.strip() for l in f.readlines() if not l.startswith('*')]

# address port timeout
'192.168.2.56 2620 22'
address, port, timeout = line.split()
port = int(port)
timeout = int(timeout)

# gawk '/^\*/ {}; {address = $1}; END {print address}' mk5ad.ctl
