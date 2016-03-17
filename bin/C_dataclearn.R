############################
# author DonghaiYu
# description traffic Flow prediction Experiment Data preparation Part Step 3 (clean data)
# input traffic flow data for per ID of all day(folder byids)
# output  cleaned traffic flow data for per ID of all day(folder cleaned_byids)
#

path <- "E://javacode/trafficFlowData/result/byids/"
folder <- dir(path)

stp = 1
UsefulIDNum = 0
deletIDNum = 0
ch = 0

minDays = 4
#有数据的天数小于minDays时认为卡口数据缺失严重，此卡口数据不使用
zeroThreshold = 8
#白天流量为0的时间段超过 zeroThreshold 次就认为数据缺失严重，这一天的数据不使用
daytimeStart = 33
#24小时中开始使用数据的位置
daytimeEnd = 100
#24小时中结束使用数据的位置

#遍历每一个卡口数据文件
for (idNum in 1:length(folder)){

	f = paste(path,folder[idNum],sep="")
	id = folder[idNum]
	
	mat = as.matrix(read.table(f,header=F,sep=","))
	dateCol = mat[,1]
	mat = mat[,-1]
	
	if(is.null(nrow(mat))){
		deletIDNum = deletIDNum + 1
		next
	}
	
	if( nrow(mat) < minDays ){
		#每月数据量小于minDays天的卡口删除
		deletIDNum = deletIDNum + 1
		next
	}
	UsefulIDNum = UsefulIDNum + 1
	
	# stp表示将几个相邻数据相加，即处理后的数据是几个5分钟的流量
	ma = matrix(as.numeric(mat),nrow=nrow(mat))
	m = matrix(nrow=nrow(mat),ncol=ncol(mat)/stp)
	ind <- seq(1, ncol(mat), by = stp)
	for( i in ind){
		temp = 0
		for( j in 1:stp){
			temp =  temp + ma[,i+j-1] 
		}
		m[,(i+stp-1)/stp] = temp
	}
	
	allDaysData = m
	noisDays = c()
	
	for( i in 1:nrow(m)){
		num = length(which(m[i,daytimeStart:daytimeEnd] == 0))
		if(num > zeroThreshold){
			noisDays = c(noisDays,i)
		}
	}
	normalDaysData = m[-noisDays,]
	noisDaysData = m[noisDays,]
	
	#当噪音天数少于1/3时，删除噪音天的数据
	normN = nrow(m) - length(noisDays)
	noisN = length(noisDays)
	maxNoise = nrow(m)/3
	#print(paste("id",id,"normN",normN,"noisN",noisN,sep=";"))
	if(noisN > 0 && noisN <= maxNoise){
		m = normalDaysData
		dateCol = dateCol[-noisDays]		
	}
	
	y = nrow(m)#天数
	x = ncol(m)#每天统计数据个数
	losH = x*2/7#白天开始
	losE = x*13/14#白天结束

	#每个时间段的平均值
	avg = colMeans(m)
	
	errVec = c()
	
	for(j in 1 : x ){
		oneInter = m[,j]
		
		#七分位数
		sevenSP = sort(oneInter)[floor(y*0.7)]
		for( k in 1 : y){
		    #修正白天大于三倍七分位数的异常点
			if(m[k,j] > (3*sevenSP) && j<losE && j>losH){
				ch = ch + 1
				chv = avg[j]
				if( j != 1 && j < y ){
					chv = (avg[j] + m[k,j-1] + m[k,j+1])/3
				}
				
				m[k,j] = floor(chv)
				print(paste("change outliers ",ch," id ",id," day: ",k," interval: ",j,msep=""))
			}
			#修正白天丢失点
			
			if(m[k,j] < (avg[j]/5) && j<losE && j>losH){
				m[k,j] = avg[j]
				ch = ch + 1
			}
		}
	}
	#normalization or not
	#m = scale(m,center=TRUE,scale=TRUE)

	co = ncol(m)/stp+1
	tosave = matrix(nrow=nrow(m),ncol=co)
	tosave[,1] = dateCol
	for( i in 2 : co){
		tosave[,i] = m[,i-1]
	}
	write.table(tosave, file = paste("E://javacode/trafficFlowData/result/cleaned_byids/",id,sep=""),sep=",", row.names = FALSE,col.names = FALSE,quote = FALSE)
	
}

