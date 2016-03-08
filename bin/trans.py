import sys
import csv
f = file(sys.argv[1],"r")
data = []

reader = csv.reader(f)
for line in reader:
    data.append(line)
f.close()

x = len(data)
y = len(data[0])
for xx in range(0,x):
    for yy in range(0,y):
        data[xx][yy] = int(data[xx][yy])
for item in data:
    print(item)