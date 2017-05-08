# Android-App
Android application to configure ESGT devices.
To build the application, import the application from the github repository.

# Firebase Setup
1) Register the SHA1 fingerprint of the computer used to build the Android application on the Firebase console. This is necessary to use the Google APIs for sign-in methods.
* Get the SHA fingerprint by following the instructions from the [Google API website](https://developers.google.com/android/guides/client-auth). 
* Go to the Firebase console, click the cog next to overview, then click "Project Settings". 
* Click "Add App", then select "Add Firebast to your Android app".
* Input the Android package name, and the debug signing certificate SHA-1 obtained before and register the app.
* Click "Register App", then download the config file and put it in the base app build directory.

2) Enable the sign-in methods necessary to use the application.
* Go to the "Authentication" tab on the Firebase console.
* Click the "Sign-In Method" tab.
* ENable both "Google" and "Anonymous" sign in methods.

# App Usage

## Pairing Process
To pair the Android application with an ESGT device, the following steps are necessary.
* Go to the "Database" tab on the Firebase console.
* Select the unique ID (UID) under the "devices" node in the real-time database.
* Type in the UID that corresponds to the ESGT device. Note that if there is no UID, the backend server has not yet run successfully with Firebase integration. Please run the backend server at least once to generate a UID.
* Click the "Pair" button. A Snackbar will show indicating whether the pairing was successful.

## Configuration Changes
* To get to the configuration settings, click the three dots in the top right corner, then click "settings"
* The Settings activity is self-explanatory. Any changes made in these settings will be reflected immediately in the Firebase console.
* Ensure that changes are made instantly by checking the "Database" tab in the Firebase console.
