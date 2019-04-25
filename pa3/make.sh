thrift --gen java ClientWork.thrift
thrift --gen java ServerWork.thrift
thrift --gen java CoordinatorWork.thrift



cp ./ClientSender.java ./gen-java/
cp ./ClientReceiver.java ./gen-java/
cp ./Server.java ./gen-java/
cp ./Coordinator.java ./gen-java/
cp ./CoordinatorWorkHandler.java ./gen-java/
cp ./ServerWorkHandler.java ./gen-java/
cp ./ClientWorkHandler.java ./gen-java/

cp ./read-heavy.txt ./gen-java/
cp ./read-only.txt ./gen-java/
cp ./write-heavy.txt ./gen-java/
cp ./write-only.txt ./gen-java/
cp ./receiver.sh ./gen-java/
cp ./write-only.sh ./gen-java/
cp ./read-only.sh ./gen-java/
cp ./read-heavy.sh ./gen-java/
cp ./write-heavy.sh ./gen-java/
cp ./node.sh ./gen-java/

cd ./gen-java


javac -cp ".:/usr/local/Thrift/*" ClientSender.java -d .
javac -cp ".:/usr/local/Thrift/*" ClientReceiver.java -d .
javac -cp ".:/usr/local/Thrift/*" Server.java -d .
javac -cp ".:/usr/local/Thrift/*" Coordinator.java -d .


# java -cp ".:/usr/local/Thrift/*" Coordinator 9001 4 4 7 9002
# java -cp ".:/usr/local/Thrift/*" Server 9003 localhost 9001
# java -cp ".:/usr/local/Thrift/*" Server 9004 localhost 9001
# java -cp ".:/usr/local/Thrift/*" Server 9005 localhost 9001
# java -cp ".:/usr/local/Thrift/*" Server 9006 localhost 9001
# java -cp ".:/usr/local/Thrift/*" Server 9007 localhost 9001
# java -cp ".:/usr/local/Thrift/*" Server 9008 localhost 9001
# java -cp ".:/usr/local/Thrift/*" ClientReceiver 9009
# java -cp ".:/usr/local/Thrift/*" ClientSender localhost 9002 9009
# java -cp ".:/usr/local/Thrift/*" ClientReceiver 9010
# java -cp ".:/usr/local/Thrift/*" ClientSender localhost 9003 9010
# java -cp ".:/usr/local/Thrift/*" ClientReceiver 9011
# java -cp ".:/usr/local/Thrift/*" ClientSender localhost 9005 9011
