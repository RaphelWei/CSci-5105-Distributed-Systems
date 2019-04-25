gnome-terminal -e "bash -c 'java -cp '.:/usr/local/Thrift/*' ClientSender cs-exa 9003 9009 ./read-only.txt;exec $SHELL'" &&
gnome-terminal -e "bash -c 'java -cp '.:/usr/local/Thrift/*' ClientSender cs-exa 9003 9010 ./read-only.txt;exec $SHELL'" &&
gnome-terminal -e "bash -c 'java -cp '.:/usr/local/Thrift/*' ClientSender cs-exa 9003 9011 ./read-only.txt;exec $SHELL'"
