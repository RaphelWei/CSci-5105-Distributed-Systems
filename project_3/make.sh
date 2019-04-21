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


javac -cp ".:/usr/local/Thrift/*" ClientSender.java -d .
javac -cp ".:/usr/local/Thrift/*" ClientReceiver.java -d .
javac -cp ".:/usr/local/Thrift/*" Server.java -d .
javac -cp ".:/usr/local/Thrift/*" Coordinator.java -d .
