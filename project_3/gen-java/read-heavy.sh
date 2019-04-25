gnome-terminal -e "bash -c 'java -cp '.:/usr/local/Thrift/*' ClientSender localhost 9003 9009 ./read-heavy.txt;exec $SHELL'" &&
gnome-terminal -e "bash -c 'java -cp '.:/usr/local/Thrift/*' ClientSender localhost 9003 9010 ./read-heavy.txt;exec $SHELL'" &&
gnome-terminal -e "bash -c 'java -cp '.:/usr/local/Thrift/*' ClientSender localhost 9003 9011 ./read-heavy.txt;exec $SHELL'"
