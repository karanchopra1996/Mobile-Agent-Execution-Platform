#!/bin/sh

javac Mobile/*.java
rmic Mobile.Place
jar cvf Mobile.jar Mobile/*.class
javac -cp Mobile.jar:. *.java