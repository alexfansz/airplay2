## AirPlay 2 Receiver for Android (Root Required)

This project provides a way to run an **AirPlay 2** receiver on Android devices.

### Components

- **`bin/`** directory contains the core binaries:
  - `nqptp` — Not Quite PTP, a precise time synchronization daemon (companion for AirPlay 2 timing)
  - `tutucast` — The main AirPlay 2 protocol stack / receiver
  - Supporting libraries (`libc++_shared.so`, `libsodium.so`) and configuration files

- **`tutucastdemo/`** — Android Java API demo that shows how to integrate and use the AirPlay 2 receiver.

### Requirements

- **Rooted Android device** (required)
- The binaries in the `bin/` folder are compiled for Android

### How to Run

1. Push all files from the `bin/` directory to `/data/local/tmp/` on your Android device:

   ```bash
   adb push bin/* /data/local/tmp/

2. Enter root shell:
   adb shell
   su

3. Go to the directory and run the startup script:
   cd /data/local/tmp
   source run.sh

After running, your Android device should appear as an AirPlay 2 receiver on Apple devices (iPhone, iPad, Mac).NotesThis implementation requires root privileges because nqptp needs low-level network access for precise timing.
tutucastdemo provides a Java demo to help developers integrate the receiver into their own Android applications.
