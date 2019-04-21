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

cd ./gen-java


javac -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" ClientSender.java -d .
javac -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" ClientReceiver.java -d .
javac -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" Server.java -d .
javac -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" Coordinator.java -d .


# java -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" Coordinator 9001 4 4 7 9002
# java -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" Server 9003 localhost 9001
# java -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" Server 9004 localhost 9001
# java -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" Server 9005 localhost 9001
# java -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" Server 9006 localhost 9001
# java -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" Server 9007 localhost 9001
# java -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" Server 9008 localhost 9001
# java -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" ClientReceiver 9009
# java -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" ClientSender localhost 9002 100000 9009
# java -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" ClientReceiver 9010
# java -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" ClientSender localhost 9003 100000 9010
# java -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" ClientReceiver 9011
# java -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" ClientSender localhost 9005 100000 9011
