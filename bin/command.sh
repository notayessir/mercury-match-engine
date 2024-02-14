#!/bin/bash

node="match_engine"
command=$1
port=9988

if [ $# -le 0 ]; then
	echo "command not correct, try below: "
	echo "1) sh command.sh start"
	echo "2) sh command.sh start 9988"
	exit 1
fi


if [ $2 ];then
    port=$2
fi



start(){
	stop
	nohup java -jar -Xmx4g -XX:+UseG1GC ../src/match-engine-server-1.0.0-exec.jar \
	--server.port=$port --spring.config.location=../config/application.properties > ../log/app.log 2>&1 &
	echo $! > ./${node}_pid.file
}

stop(){
	kill $(cat ./${node}_pid.file)
}



if [ $command = start ]
then
	start
	echo "server start on ${port} success"
elif [ $command = stop ]
then
	stop
	echo "server stop success"
else
	echo 'unsupported command:' $command
fi 

