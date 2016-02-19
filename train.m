clc
testt = csvread('E:\javacode\trafficFlowData\data\00_01');
[ma,mi]=mapminmax(testt',0,1);
cutIndex = 6744;
[x,total] = size(ma);
tin = ma(1:x-1,1:cutIndex);
tout = ma(x,1:cutIndex);

tein = ma(1:x-1,cutIndex+1:total);
teout = ma(x,cutIndex+1:total);



%����������
net = newff( tin,tout,100) ; 
%����ѵ������

net.trainparam.epochs = 20 ;
%net.trainparam.goal = 0.001 ;

net = train( net, tin , tout ) ;

Y = sim(net ,tein);

plot([Y' teout']);
err = abs(Y - teout);
allerr = sum(err);
%pererr = abs(err./teout);
%[xx,yy] = size(pererr);
%averr = sum(pererr)/yy;

