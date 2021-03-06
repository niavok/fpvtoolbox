# FPV Toolbox

FPV Toolbox is a small Android tool to show images and vidéos in the VR HMD Parrot Cockpit Glasses.

## Features

* Distortion and chromatic aberration correction
* Pan/scale settings
* Bluetooth gamepad optional control
* Power saving mode
* And more small things ...

## How to use

First, accept the file acces autorisations. The app will look for video and image in the "Movies" and "Pictures" folder of the external memory.

Swipe at the left of the screen to show the interaction menu. The bluetooth controller keybiding are shown here.

## Example

Playing a video
![FPV Toolbox playing a video](https://raw.githubusercontent.com/niavok/fpvtoolbox/master/screenshots/fpv_toolbox.png)

Side menu
![FPV Toolbox side menu](https://raw.githubusercontent.com/niavok/fpvtoolbox/master/screenshots/fpv_toolbox_side.png)

Settings menu
![FPV Toolbox settings](https://raw.githubusercontent.com/niavok/fpvtoolbox/master/screenshots/fpv_toolbox_settings.png)


## Change log

### version 1.2.1
* stop video decoding on background
* fix memory leak at each video startup
* fix power save wake using menu or gamepad

### version 1.2
* use immersive fullscren to hide navigation bar

### version 1.1
* add rescan button in the side menu to scan the available media without kill the app
* support rotated videos
* fix crash if a video file is not playable but the Android media system
* fix crash if no content after wake up
