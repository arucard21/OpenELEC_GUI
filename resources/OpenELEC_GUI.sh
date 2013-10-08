#!/bin/sh
#
# Commonly used LnF names:
# javax.swing.plaf.metal.MetalLookAndFeel (java default)
# com.sun.java.swing.plaf.motif.MotifLookAndFeel
# com.sun.java.swing.plaf.gtk.GTKLookAndFeel
# com.sun.java.swing.plaf.windows.WindowsLookAndFeel
# 
# Uncomment line below and change the Look and Feel name (must be available on your system)
# 
#LAF="-Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel"

GUI_Jar="./lib/OpenELEC_GUI.jar"
if [ $XDG_CURRENT_DESKTOP = "KDE" ]
then
  kdesudo "java -jar $LAF $GUI_Jar"
else
  gksudo "java -jar $LAF $GUI_Jar"
fi
