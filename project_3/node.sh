# run coordinator
gnome-terminal -e "bash -c 'java -cp '.:/usr/local/Thrift/*' Coordinator 9001 4 4 7 9002;exec $SHELL'"



# run servers
gnome-terminal -e "bash -c 'java -cp '.:/usr/local/Thrift/*' Server 9003 localhost 9001;exec $SHELL'"
gnome-terminal -e "bash -c 'java -cp '.:/usr/local/Thrift/*' Server 9004 localhost 9001;exec $SHELL'"
gnome-terminal -e "bash -c 'java -cp '.:/usr/local/Thrift/*' Server 9005 localhost 9001;exec $SHELL'"
gnome-terminal -e "bash -c 'java -cp '.:/usr/local/Thrift/*' Server 9006 localhost 9001;exec $SHELL'"
gnome-terminal -e "bash -c 'java -cp '.:/usr/local/Thrift/*' Server 9007 localhost 9001;exec $SHELL'"
gnome-terminal -e "bash -c 'java -cp '.:/usr/local/Thrift/*' Server 9008 localhost 9001;exec $SHELL'"
