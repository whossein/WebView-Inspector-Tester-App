#!/bin/bash
set -e

SRC="app/src/main/res/drawable/app_icon.png"

# Colors
BG_COLOR="#F5F7F9"

# Create directories
mkdir -p app/src/main/res/mipmap-mdpi
mkdir -p app/src/main/res/mipmap-hdpi
mkdir -p app/src/main/res/mipmap-xhdpi
mkdir -p app/src/main/res/mipmap-xxhdpi
mkdir -p app/src/main/res/mipmap-xxxhdpi
mkdir -p app/src/main/res/values

# Generate legacy launcher icons
convert "$SRC" -resize 48x48 app/src/main/res/mipmap-mdpi/ic_launcher.png
convert "$SRC" -resize 72x72 app/src/main/res/mipmap-hdpi/ic_launcher.png
convert "$SRC" -resize 96x96 app/src/main/res/mipmap-xhdpi/ic_launcher.png
convert "$SRC" -resize 144x144 app/src/main/res/mipmap-xxhdpi/ic_launcher.png
convert "$SRC" -resize 192x192 app/src/main/res/mipmap-xxxhdpi/ic_launcher.png

# Generate legacy round icons (just use the same for now, or round it)
# We will just round it using ImageMagick
for size in 48 72 96 144 192; do
  dir=""
  if [ "$size" -eq 48 ]; then dir="mipmap-mdpi"; fi
  if [ "$size" -eq 72 ]; then dir="mipmap-hdpi"; fi
  if [ "$size" -eq 96 ]; then dir="mipmap-xhdpi"; fi
  if [ "$size" -eq 144 ]; then dir="mipmap-xxhdpi"; fi
  if [ "$size" -eq 192 ]; then dir="mipmap-xxxhdpi"; fi
  
  convert "$SRC" -resize ${size}x${size} app/src/main/res/$dir/ic_launcher_round.png
done

# Adaptive Icon Foreground (safe zone is 66%, so for 432x432, we scale image to 285x285 and pad)
convert "$SRC" -resize 285x285 -gravity center -background none -extent 432x432 app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.png
convert "$SRC" -resize 216x216 -gravity center -background none -extent 324x324 app/src/main/res/mipmap-xxhdpi/ic_launcher_foreground.png
convert "$SRC" -resize 144x144 -gravity center -background none -extent 216x216 app/src/main/res/mipmap-xhdpi/ic_launcher_foreground.png
convert "$SRC" -resize 108x108 -gravity center -background none -extent 162x162 app/src/main/res/mipmap-hdpi/ic_launcher_foreground.png
convert "$SRC" -resize 72x72 -gravity center -background none -extent 108x108 app/src/main/res/mipmap-mdpi/ic_launcher_foreground.png

# Remove old webp files
rm -f app/src/main/res/mipmap-*/ic_launcher.webp
rm -f app/src/main/res/mipmap-*/ic_launcher_round.webp

