OpenELEC_GUI
============

Graphical front-end (Java Swing) for OS-specific script to install OpenELEC to an SD card (uses the Raspberry Pi script by default)

Usage
============
- Extract OpenELEC_GUI.zip to the folder containing the create_sdcard script
- Start the GUI by executing the OpenELEC_GUI.sh script (this uses gksudo or kdesudo to run the program as root)
- Select the disk to which you wish to install OpenELEC (WARNING: all data on this disk will be completely erased)
- Click the Install button. The output of the script will be shown below along with a progressbar (Note: progressbar is not yet accurate) 

Configuration
============
The GUI works in 2 steps:

1. It executes a (native, os-specific) command to retrieve the list of connected disks. This is parsed and 
the information is shown in the drop-down menu of the GUI.
2. After selecting one of the disks in the GUI, you can click the button. This will execute a second (native, os-specific) 
command with the selected disk as parameter.

The commands are configurable so different configuration files can be used for different OS's, distributions or installation platforms. The default Linux configuration will use parted to retrieve the connected disks and the OpenELEC create_sdcard script to install OpenELEC.

To-Do
============
- Actual installation fails, maybe due to java's Process object using a different environment to run the install script. Need to look into this
