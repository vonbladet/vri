import os

class DBFReader(object):
    def __init__(self, f):
        self.f = f
        self.read_headers()
        self.read_data()
    def read_headers(self):
        self.f.seek(32)
        self.lengths = []
        self.names = []
        while True:
            s = self.f.read(32)
            if s[0] == chr(0x0d):
                self.f.seek(-31, os.SEEK_CUR) # leave the terminator
                break
            field_name = s[:10] 
            field_name = field_name[:field_name.find(chr(0))]
            field_type = s[11]
            if field_type != 'C':
                raise RuntimeError, "Field type isn't character"
            field_length = ord(s[16])
            print field_name, field_length
            self.names.append(field_name)
            self.lengths.append(field_length)
    def read_data(self):
        self.data = []
        try:
            while True:
                v = {}    
                deleted = self.f.read(1)
                for i in range(len(self.lengths)):
                    s = self.f.read(self.lengths[i])
                    if not s:
                        raise EOFError
                    v[self.names[i]] = s.strip()
                self.data.append(v)
        except EOFError:
            pass

if __name__=='__main__':
    f = file("world_adm0.dbf")
    d = DBFReader(f)

