#!/bin/sh
# convert a 1024x1024 png (named "sketch1024.png") to the required icns format

mkdir sketch.iconset
sips -z 16 16     sketch1024.png --out sketch.iconset/icon_16x16.png
sips -z 32 32     sketch1024.png --out sketch.iconset/icon_16x16@2x.png
sips -z 32 32     sketch1024.png --out sketch.iconset/icon_32x32.png
sips -z 64 64     sketch1024.png --out sketch.iconset/icon_32x32@2x.png
sips -z 128 128   sketch1024.png --out sketch.iconset/icon_128x128.png
sips -z 256 256   sketch1024.png --out sketch.iconset/icon_128x128@2x.png
sips -z 256 256   sketch1024.png --out sketch.iconset/icon_256x256.png
sips -z 512 512   sketch1024.png --out sketch.iconset/icon_256x256@2x.png
sips -z 512 512   sketch1024.png --out sketch.iconset/icon_512x512.png
cp sketch1024.png sketch.iconset/icon_512x512@2x.png
iconutil -c icns sketch.iconset
#rm -R sketch.iconset
