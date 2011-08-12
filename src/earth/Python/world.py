import Tkinter as Tk
root = Tk.Tk()
c = Tk.Canvas(root, height=400, width=600)
c.pack()
outline1 = [(100, 0), (200, 50), (150, 150)]
p2 = c.create_polygon(outline1, fill='red')

lat = Tk.Scale(root, label="Lat", from_=-90, to=90, orient=Tk.HORIZONTAL)
lat.pack()

lon = Tk.Scale(root, label="Lon", from_=-180, to=180, orient=Tk.HORIZONTAL)
lon.pack()
