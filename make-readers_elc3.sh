#!/bin/sh


cd bin
rm -rf ../p5/Readers_ELC3_2016/application.*
jar cvf ../p5/Readers_ELC3_2016/code/Readers_ELC3_2016.jar trp data
cp -r ../rita.jar ../p5/Readers_ELC3_2016/code/
echo
ls -R ../p5/Readers_ELC3_2016/
echo
echo done
