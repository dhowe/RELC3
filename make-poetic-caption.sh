#!/bin/sh

cd bin
jar cvf ../p5/PoeticCaption/code/PoeticCaption.jar trp data
cp -r ../rita.jar ../p5/PoeticCaption/code/
echo
ls -R ../p5/PoeticCaption/
echo
echo done
