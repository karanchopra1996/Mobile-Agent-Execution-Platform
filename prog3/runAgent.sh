#!/bin/sh
# $1 = ip1 $2 = ip2

java -cp Mobile.jar Mobile.Inject localhost 12345 MyAgent $1 $2 $3
