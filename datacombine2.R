path <- "E:\\javacode\\trafficFlowData\\data\\testTest"
folder <- dir(path)
#print(length(folder))
#print(folder)
files <- list()
for (i in 1:length(folder)){
	files[i] <- paste(path,folder[i],sep="\\")
}
#print(mode(files))
#par(mfrow=c(2,4))
data <- c()
output <- c()
filenum <- length(files)
for  (i in 1 : length(files)) {
	f <- file(paste(files[i],sep=""))
	mat <- read.table(f,header=F,sep=",")
	
	data <- c(data,mat[,2][65:265])	
	#65 for 5:20 & 265 for 22:00
}

#¹éÒ»»¯
input <- c()
maxV <- max(data)
print(maxV)
for (i in data) {

	input <- c(input,(i / 311))
}
#print(input)
temp <- matrix(nrow=filenum,ncol=201,byrow=T)
#print(input[1])
for (i in 0 : filenum-1) {
	
	st <- i *201+1
	en <- (i+1)*201
	#print(mode(c(input[st : en])))
	#print(length(temp[i+1,]))
	#print("####")
	#print(length(input[st : en]))
	temp[i+1,] <- input[st : en]
	
}

#print(temp)
write.csv(temp, file = "E:\\javacode\\BpNeuralNetwork-digtal_lable\\sample-data\\test.csv", row.names = FALSE,col.names = NULL,quote = FALSE)