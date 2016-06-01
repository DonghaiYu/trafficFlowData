import numpy as np
from numpy import double, shape
import sys
import linecache
from numpy.ma.core import ids
from audioop import avg

resultFile = "../result4/weightGroupResult"
lineNum = len(open(resultFile).readlines()) 
removeIds={'371302991224_03':{},'371302971064_00':{},'371311991013_00':{},'371311991026_03':{},'371302991099_02':{},'371300403107_02':{},'371311991016_01':{}}
minDic={}
idErr={}

for i in range(1,lineNum-1,30):
    
    line = linecache.getline(resultFile,i)
    id = line.split('#')[1].split(',')[0]
    k = line.split('#')[2]
    p = int(line.split('#')[3]) - 1
    if removeIds.has_key(id):
        continue
    m = 1.0
    for j in range(1,30):
        record = linecache.getline(resultFile,i+j).split(',')
        m = min(m,float(record[3]))

    key = k+","+str(p)
    minDic.setdefault(key,[]).append(m)
    idErr.setdefault(id,[]).append(m)
    
    
print "###########################"
for k in minDic:
    #print minDic[k]
    n = 0;
    #for i in range(1,0.01):
    #    for 
    
    print k,sum(minDic[k])/len(minDic[k])
#for k in idErr:
#    print k,sum(idErr[k])/len(idErr[k]) 
    