# Media player remote control

This module acts as a remote control for the TV app. You can add remote fileservers (as long as said fileservers are 
SFTP fileservers) and pair a TV (or TVs), then connect to it and control playback.

If you're running both applications in the emulator, please see the README.md for `/app_tv/`.
As long as you connect TCP port 34124 between the two emulators, they should be able to pair and communicate
without a problem.

# Adding a TV

Click on "Edit paired devices", then the plus button, and scan the QR code displayed by the TV app's main activity.
Once scanned, it might take several seconds for it to pair. Afterwards, the device should appear on the list

# Adding a media server

Right now, the app supports only SFTP fileservers. On the home screen, click the "Edit media servers" button and
click the plus to add a server. Input a name (up to you) and a connection string in the format of
`sftp://<username>:<password>@<ip>:<port>`.
If you're running on the emulator with a server on localhost, the IP is 10.0.2.2.

# Controlling a TV

Click "Connect", then select the name of the TV you want to control in the popup menu.
Then click the "Open file" button and select the media server from the popup menu, and navigate to the file you want
to play. Once selected, it may take several seconds for the TV app to launch the video player and for it to show
up in the phone app.
