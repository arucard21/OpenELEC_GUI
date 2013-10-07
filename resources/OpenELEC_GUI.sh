#!/bin/sh
GUI_Jar="./OpenELEC_GUI.jar"
if [ $XDG_CURRENT_DESKTOP = "KDE" ]
then
  kdesudo $GUI_Jar
else
  gksudo $GUI_Jar
fi
