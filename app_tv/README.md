# TV media player

This module contains an Android app that listens for requests on TCP port 34124, pops up a video player and plays
whatever you send it (as long as that "whatever" is a sftp:// link in a BeginPlayback message).

It displays a QR code on the main activity which you can scan with the companion app (`/app_phone/`) to pair with the
device. All paired devices are displayed on the left side of the screen and can be disconnected by clicking OK on them.
The connection isn't encrypted, I ran out of time for that.

If you let it, this app can also abuse the accessibility API to run itself in the background indefinitely and launch the
video player without requiring you to even touch the remote.

To set it up in the emulator, refer to [this page](https://developer.android.com/studio/run/emulator-networking)
(the pairing codes are also automatically adjusted when running in an emulator, so the client connects normally).
