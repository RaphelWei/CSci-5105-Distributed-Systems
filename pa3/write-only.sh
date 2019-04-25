gnome-terminal -e "bash -c 'java -cp '.:/usr/local/Thrift/*' ClientSender cs-exa 9003 9009 ./write-only.txt;exec $SHELL'" &&
gnome-terminal -e "bash -c 'java -cp '.:/usr/local/Thrift/*' ClientSender cs-exa 9003 9010 ./write-only.txt;exec $SHELL'" &&
gnome-terminal -e "bash -c 'java -cp '.:/usr/local/Thrift/*' ClientSender cs-exa 9003 9011 ./write-only.txt;exec $SHELL'"
