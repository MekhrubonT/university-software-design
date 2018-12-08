# Шахматы

The chessmate app run arguments: 

      -server | -s              - to run server app

      -connection | -c          - to run connection app

      -port num | -pnum         - to set ran application port, num - port number

      -server_port num | -spnum - server port for connection to connect, only connection apps

Examples:

      java App -s -p8081

      java App -c -sp8081 -port 8088

      java App -connection -server_port 8081 -port 8089

Вход в клиент осуществляется переходом по адресу http:\/\/localhost:\<port\>/index.

