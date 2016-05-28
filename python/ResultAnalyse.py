import numpy as np
from numpy import double, shape
import sys
import linecache

lineNum = len(open('../result1/weightGroupResult').readlines()) 

for i in range(1,lineNum-1,30):
    line = linecache.getline('../result1/weightGroupResult',i)
    id = line.split('#')[1].split(',')[0]
    k = line.split('#')[2]
    p = int(line.split('#')[3]) - 1
    
    m = 1.0
    for j in range(1,30):
        record = linecache.getline('../result1/weightGroupResult',i+j).split(',')
        m = min(m,float(record[3]))
        
    print id,k,p,m