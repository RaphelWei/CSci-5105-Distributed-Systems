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

cd ./gen-java

javac -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" ClientSender.java -d .
javac -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" ClientReceiver.java -d .
javac -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" Server.java -d .
javac -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" Coordinator.java -d .

# "Want 1 arguments!\nMyPort"
# java -cp ".:/usr/local/Thrift/*" ClientReceiver 9000
# Want 4 arguments!: ContactServerIP ContactServerPort ReceiverIP ReceiverPort
# java -cp ".:/usr/local/Thrift/*" ClientSender localhost 9001 localhost 9000
# "Want 1 arguments!\nMyPort"
# java -cp ".:/usr/local/Thrift/*" ClientReceiver 9009
# Want 4 arguments!: ContactServerIP ContactServerPort ReceiverIP ReceiverPort
# java -cp ".:/usr/local/Thrift/*" ClientSender localhost 9002 localhost 9009
# "Want 1 arguments!\nMyPort"
# java -cp ".:/usr/local/Thrift/*" ClientReceiver 9010
# Want 4 arguments!: ContactServerIP ContactServerPort ReceiverIP ReceiverPort
# java -cp ".:/usr/local/Thrift/*" ClientSender localhost 9003 localhost 9010
#
# Want 5 arguments!: CoordinatorPort NR NW N ServerPort
# java -cp ".:/usr/local/Thrift/*" Coordinator 9001 5 6 10 9002
#
# Want 3 arguments!: MyPort CoordinatorIP CoordinatorPort
# java -cp ".:/usr/local/Thrift/*" Server 9002 localhost 9001
# Want 3 arguments!: MyPort CoordinatorIP CoordinatorPort
# java -cp ".:/usr/local/Thrift/*" Server 9003 localhost 9001
# Want 3 arguments!: MyPort CoordinatorIP CoordinatorPort
# java -cp ".:/usr/local/Thrift/*" Server 9004 localhost 9001
# Want 3 arguments!: MyPort CoordinatorIP CoordinatorPort
# java -cp ".:/usr/local/Thrift/*" Server 9005 localhost 9001
# Want 3 arguments!: MyPort CoordinatorIP CoordinatorPort
# java -cp ".:/usr/local/Thrift/*" Server 9006 localhost 9001
# Want 3 arguments!: MyPort CoordinatorIP CoordinatorPort
# java -cp ".:/usr/local/Thrift/*" Server 9007 localhost 9001
