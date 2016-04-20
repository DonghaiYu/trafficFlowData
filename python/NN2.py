# -*- coding: utf-8 -*-
__author__ = 'HTT'
# Back-Propagation Neural Networks

import math
import random
import time
start_time=time.clock()
def makeMatrix(p,q):
        N=[]
        for i in range(p):
            F=[]
            for j in range(q):
                F.append(0.0)
            N.append(F)
        return N
#def makeMatrix(I, J, fill=0.0):
#     m = []
#     for i in range(I):
#         m.append([fill]*J)
#     return m

random.seed(1)
def rand(a, b):#产生a，b之间的数
    return (b-a)*random.random() + a

def sigmoid(x):#用的tanh（）双曲正切函数，用S型函数误差太大
    return math.tanh(x)
def dsigmoid(y):
    return 1-y**2
# def sigmoid(x):#激活函数S型
#     return 1/(float(1+numpy.exp(-x)))
#
# def dsigmoid(y):#激活函数的导数
#     return y-y**2

class NN:
    def __init__(self, ni, nh, no):#ni表示输入的点数，nh表示中间层的点数，no表示输出的点数

        self.ni = ni+1 #初始层会多一个偏置量
        self.nh = nh
        self.no = no

        self.ai = [1.0]*self.ni#存放每一层的值
        self.ah = [1.0]*self.nh
        self.ao = [1.0]*self.no

        self.wi = makeMatrix(self.ni, self.nh)#输入层到中间层的矩阵，产生ni*nh的元素为零的矩阵
        self.wo = makeMatrix(self.nh, self.no)#中间层到输出层的矩阵，产生nh*ni的元素为零的矩阵
        th = 1
        for i in range(self.ni):#为矩阵元素随机赋值
            for j in range(self.nh):
                self.wi[i][j] = rand(-th, th)
        for j in range(self.nh):
            for k in range(self.no):
                self.wo[j][k] = rand(-th, th)


        self.ci = makeMatrix(self.ni, self.nh)#存放冲量
        self.co = makeMatrix(self.nh, self.no)

    def update(self, inputs):#更新ai[],ah[],a0[]中的元素，所以输入的元素个数必须跟输入层一样，这次是将三层先填满数据
        if len(inputs) != self.ni-1:
            raise ValueError('wrong number of inputs')

        for i in range(self.ni-1):#将数据导入初始层
            self.ai[i] = inputs[i]

        for j in range(self.nh):#将输入层的数据传递到输入层
            sum = 0.0
            for i in range(self.ni):
                sum = sum + self.ai[i] * self.wi[i][j]
            self.ah[j] = sigmoid(sum)#调用激活函数

        for k in range(self.no):
            sum = 0.0
            for j in range(self.nh):
                sum = sum + self.ah[j] * self.wo[j][k]
            self.ao[k] = sigmoid(sum)

        return self.ao[:]#返回输出层的值

    def backPropagate(self, targets, N, M):#定义后向传播,targets是真实值的列向量，N是学习效率，M是冲量单元
        if len(targets) != self.no:
            raise ValueError('wrong number of target values')

        output_deltas = [0.0] * self.no
        for k in range(self.no):
            error = targets[k]-self.ao[k]#输出值与真实值之间的误差
            output_deltas[k] = dsigmoid(self.ao[k]) * error#输出层的局部梯度，导数乘以误差，不在隐层就这么定义

        hidden_deltas = [0.0] * self.nh#隐层的局部梯度
        for j in range(self.nh):
            error = 0.0
            for k in range(self.no):
                error = error + output_deltas[k]*self.wo[j][k]
            hidden_deltas[j] = dsigmoid(self.ah[j]) * error#隐层的梯度定义：不在隐层的梯度与权重的积再求和，然后与导数的乘积

        for j in range(self.nh):#更新隐层与输出层之间的权重
            for k in range(self.no):
                change = output_deltas[k]*self.ah[j]#权值的校正值w的改变量（若没有冲量，且学习率为1的时候）
                self.wo[j][k] = self.wo[j][k] + N*change + M*self.co[j][k]
                self.co[j][k] = change
                #print N*change, M*self.co[j][k]

        for i in range(self.ni):#更新输入层与隐层之间的权重
            for j in range(self.nh):
                change = hidden_deltas[j]*self.ai[i]
                self.wi[i][j] = self.wi[i][j] + N*change + M*self.ci[i][j]
                self.ci[i][j] = change

        error = 0.0#返回误差
        for k in range(len(targets)):
            error = error +0.5*(targets[k]-self.ao[k])**2
        return error

    def train(self, patterns, iterations=100, N=0.016, M=0.16):# N是学习率，M是冲量前边的系数 为了避免陷入局部最小误差
        for i in range(iterations):
            error = 0.0
            for p in patterns:
                inputs = p[0]
                targets = p[1]
                self.update(inputs)
                error = error + self.backPropagate(targets, N, M)
            if i % 10 == 0:
                print  u"第 %d 次调整后的误差：%.3f"%(i,error)

    def test(self, patterns):#测试，patterns是指输入的测试数据的集合，有两个元素，第一个是测试数据，第二个是对应的真实值，若没有真实值，则输入一个数据
        for p in patterns:
           print(p[1], '->', show(self.update(p[0])))

def read():

#按行读取字符串，每一行为一个元素
    F= open('C:/Users/Administrator/Desktop/aa.txt')
    line=F.readlines()
    F.close()
#将每一行元素按空格分开
    a=[]
    for i in range(len(line)):
        column = line[i].split( )
        a.append(column)
#将字符串格式的转化成数字格式
    N=[]
    for i in range(len(a)):
        F=[]
        for j in range(len(a[i])):
            t=eval(a[i][j])
            F.append(t)
        N.append(F)
    random.shuffle(N)
    L=[i for i in range(27)]
    Lmax=[i for i in range(27)]
    Lmin=[i for i in range(27)]
    Lcha=[i for i in range(27)]
    for i in range(len(L)):
        L[i]=[x[i] for x in N]
        Lmax[i]=max(L[i])
        Lmin[i]=min(L[i])
        Lcha[i]=max(L[i])-min(L[i])
    B=[]
    for i in range(len(N)):
        F=[]
        for j in range(27):
            F.append(0)
        B.append(F)

    for i in range(len(N)):
        for j in range(27):
            B[i][j]=float((N[i][j]-Lmin[j]))/float(Lcha[j])
#将上述产生的矩阵按前27列放一起，后7列放一起
    A=[]
    for i in range(len(N)):
        M=[];F1=[];T1=[]
        for k in range(len(N[i])):
            if k in range(27):
                F1.append(B[i][k])
            else:
                T1.append(N[i][k])
        A.append([F1,T1])
    return A

def show(value):
        m = max(value)
        lab = []
        for i in value:
            temp = 0
            if i >= m:
                temp = 1
            else:
                temp = 0
            lab.append(temp)
        return lab
def data():#利用逻辑与训练并测试
    pat = read()
    n = NN(27, 11, 7)
    n.train(pat)
    n.test(pat)

if __name__ == '__main__':
    data()
times=time.clock()-start_time
print times