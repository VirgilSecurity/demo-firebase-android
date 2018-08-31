# demo-firebase-android
End-to-end encrypted, HIPAA-compliant Android chat sample app for Firebase. While this is a chat app, you can reuse it in any other apps to protect user data, documents, images using Virgil's end-to-end encryption. Only HIPAA-compliant for chat use-cases.

## Clone project

Clone the repo to your computer: using your Android Studio, navigate to 'File -> New -> Project from Version Control -> Git' and fill in the 'Git Repository URL' field with: 
```
https://github.com/VirgilSecurity/demo-firebase-android
```

## Create Firebase project
Go to the [Firebase console](https://console.firebase.google.com) and if you haven't created a project yet, create one now. If you already have one that you want to use, open it and skip to the **Firebase app setup**

* Select the **Authentication** panel and then click the **Sign In Method** tab.
*  Click **Email/Password** and turn on the **Enable** switch, then click **Save**.
* Select the **Database** panel and then enable **Cloud Firestore**.
  * Click **Rules** and paste:
  ```
  service cloud.firestore {
    match /databases/{database}/documents {
      match /{document=**} {
        allow read, write: if request.auth.uid != null;
      }
    }
  }
  ```
* Click **PUBLISH**.

## Firebase Android app setup
* In your Firebase project (on the Firebase console), click the **gear icon** -> **Project settings**
* Click **Add app** and choose **Add Firebase to your Android app**. Fill in all required fields.
* Download the generated **google-services.json** file from Project Settings and copy it to the 'app' folder, as metioned in the instructions by Firebase. You're good to go!

## Firebase cloud functions setup

> In order for the app to work, you need to deploy a Firebase function that creates JWT tokens for your authenticated users. If you already deployed this function for either the iOS or Android apps, you don't need to do it again.

* Otherwise, [follow the instructions here](https://github.com/VirgilSecurity/demo-firebase-func)
* Once the function is successfully created, go to the Firebase console -> Functions tab and copy your function's url
* Go to Android Studio -> `app/src/main/java/com/android/virgilsecurity/virgilonfire/di/NetworkModule.java` and change variable `BASE_URL` to:
```
https://YOUR_FUNCTION_URL.cloudfunctions.net/api/
```

## Build and Run
At this point you are ready to build and run the application on your real device or emulator (with google services).
* Check out what Firebase sees from your users' chats: Firebase dashboard -> Database -> Channels -> click on the thread -> Messages. This is what the rest of the world is seeing from the chat, without having access to the users' private keys (which we store on their devices).

## Credentials

To build this sample we used the following third-party frameworks:

* [Cloud Firestore](https://firebase.google.com/docs/firestore/) - as a database for messages, users and channels.
* [Cloud Functions](https://firebase.google.com/docs/functions/) - getting jwt.
* [Firebase Authentication](https://firebase.google.com/docs/auth/) - authentication.
* [Virgil SDK](https://github.com/VirgilSecurity/virgil-sdk-x) - managing users' Keys.
* [Virgil Crypto](https://github.com/VirgilSecurity/virgil-foundation-x) - encrypting and decrypting messages.
