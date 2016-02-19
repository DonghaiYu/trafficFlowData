clc
testt = csvread('E:\javacode\trafficFlowData\data\00_00');
[ma,mi]=mapminmax(testt',0,1);
cutIndex = 6744;
[x,total] = size(ma);
tin = ma(1:x-4,1:cutIndex);
tout = ma(x-3:x,1:cutIndex);

tein = ma(1:x-4,cutIndex+1:total);
teout = ma(x-3:x,cutIndex+1:total);



%创建神经网络
net = newff( tin,tout,100) ; 
%设置训练参数

net.trainparam.epochs = 10 ;
%net.trainparam.goal = 0.0001 ;

net = train( net, tin , tout ) ;

YY = sim(net ,tein);
errevg = 0;
for i = 1 : 4
    Y = YY(i, : );
    teoutx = teout(i, : );
    plot([Y' teoutx']);
    err = abs(Y - teoutx);
    allerr = sum(err);
    errevg  = errevg + allerr;
end
errevg = errevg/4;

%pererr = abs(err./teout);
%[xx,yy] = size(pererr);
%averr = sum(pererr)/yy;
