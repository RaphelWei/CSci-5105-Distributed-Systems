gnome-terminal -e "bash -c 'java -cp '.:/usr/local/Thrift/*' ClientSender localhost 9003 9009 ./write-heavy.txt;exec $SHELL'" &&
gnome-terminal -e "bash -c 'java -cp '.:/usr/local/Thrift/*' ClientSender localhost 9003 9010 ./write-heavy.txt;exec $SHELL'" &&
gnome-terminal -e "bash -c 'java -cp '.:/usr/local/Thrift/*' ClientSender localhost 9003 9011 ./write-heavy.txt;exec $SHELL'"
