# Project structure

- `/app_phone`: Contains the source files for the Android app that control the TV media player.
- `/app_tv`: Contains the source files for the Android TV app that receives commands from the phone app and plays media files from a remote server.
- `/lib_communications`: Contains the library for communicating between the remote control app and the TV.
- `/lib_vfs`: Contains the library for accessing a remote filesystem.

# Setting up the emulator

Both apps should work in the emulator, as long as you redirect TCP port 34124 on host machine to 34124 on the TV app
(the TV app has special logic for this case, and will adjust the IP sent to the client if running in
the emulator).

Once the emulator on the TV is running, telnet to localhost on port 5554
(or 5555 if you started the phone emulator first):

`telnet localhost 5554`

Then follow the instructions printed on screen to authenticate, which involves copy-pasting a short code from a
local file.

Once you're authenticated, redirect connections to localhost:34124 to emulator:34124 with the following command:

`redir add tcp:34124:34124`

That's it. The phone app on its emulator should be able to reach the TV app.

# Setting up a SFTP server

To set up a fileserver for testing, I recommend [sftpgo](https://github.com/drakkan/sftpgo).

1. Create a new directory somewhere, and cd into it
2. Run `sftpgo serve` inside it
3. Connect to `localhost:8080` in the browser, and create an admin account
4. The "Users" view should show up. Click on the plus, then add a username, password and a directory.
   Forget about everything else.
5. Click "Submit". The fileserver is now running on port 2022, and allows clients to access the directory
   you specified with the username and password.
