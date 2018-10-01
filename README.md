# End-to-end encrypted, HIPAA-compliant Android chat app for Firebase.
You can reuse this sample in any projects where you want to end-to-end protect user data, documents, images using Virgil's end-to-end encryption. [HIPAA whitepaper](https://virgilsecurity.com/wp-content/uploads/2018/07/Firebase-HIPAA-Chat-Whitepaper-Virgil-Security.pdf).

## Clone project
In Android Studio, go to 'File -> New -> Project from Version Control -> GitHub and paste in the repo's URL: 
```bash
https://github.com/VirgilSecurity/demo-firebase-android
```

## Connect your Virgil and Firebase accounts
In order for the app to work, you need to deploy a Firebase function that gives out Virgil JWT tokens for your authenticated users. You'll also need to create a Firestore database with a specific rule set.

* **[Follow instructions here](https://github.com/VirgilSecurity/demo-firebase-func)**

> You only need to do this once - if you did it already earlier or for your iOS or JavaScript apps, don't need to do it again.

## Add your Firebase function URL and Firebase project config to your app

* **Copy your new Firebase function's URL**: go to the Firebase console -> your project -> Functions tab and copy your new function's url
* **Go to Android Studio -> `app/src/main/java/com/android/virgilsecurity/virgilonfire/di/NetworkModule.java` and change the variable `BASE_URL` to**:
  ```
  https://YOUR_FUNCTION_URL.cloudfunctions.net/api/
  ```
* Go back to your project's page in Firebase console, click the **gear icon** -> **Project settings**
* Click **Add app** and choose **"Android: Add Firebase to your Android app"**
* Name your Android package and click **Register app**
* **Download google-services.json** into the 'app' folder in your Android source

## Build and Run
At this point you are ready to build and run the application on your real device or emulator (with google services).

> Remember, the app deletes messages right after delivery (it's a HIPAA requirement to meet the conduit exception). If you want to see encrypted messages in your Firestore database, run only 1 app instance, send a message to your chat partner and check Firestore DB's contents before opening the other user's app to receive the message. If you don't want to implement this behavior in your own app, you can remove it from this sample.
