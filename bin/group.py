import numpy as np
from numpy import double, shape
import sys

dic = {}
for line in open("../result/detectors.vector"):
    vec = np.array(line.split(' '))
    dic[vec[0]] = vec[1:351].astype(np.double)
print(len(dic))
#a = np.array((1,2))
#b = np.array((3,4))
#print( np.linalg.norm(a-b))
group = {}
for item in dic:
    friends = set()
    num = 0
    while(num < 4):        
        mindist = 10000
        friend = item
        for anoth in dic:             
            if(anoth != item and anoth not in friends):
                dist = np.linalg.norm(dic[item] - dic[anoth])
                if(dist < mindist):
                    mindist = dist
                    friend = anoth
        
        friends.add(friend)
        num+=1
        #print(num)
    group[item] = friends
print(len(group))
fs = open("../result/g1.group",'a')
for g in group:
    line = g
    for f in group[g]:
        line = line+","+f
    line = line+"\n"
    fs.write(line)
fs.flush()
fs.close()

#print(group["371302981151_01"].pop())   


       