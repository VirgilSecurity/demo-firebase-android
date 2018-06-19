# demo-firebase-android
A simple Android application that demonstrates how end-to-end encryption works with firebase as a backend service for authentication and chat messaging. While this is a chat app, you can reuse it in any other apps to protect user data, documents, images.

## Getting Started

Start with cloning repository to computer. For example you can use Android Studio (AS). Open AS, navigate to 'File->New->Project from Version Control->Git' and fill in 'Git Repository URL' field with: 
```
https://github.com/VirgilSecurity/demo-firebase-android
```

### Firebase set up
* Change package of project to yours. 
* Go to the [Firebase console](https://console.firebase.google.com) and create your own project.
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
* Go to the Project settings and press 'Add Firebase to your Android app' then fill in all fields.
* Download the generated google-services.json file from Project Settings and copy it to the 'app' folder, as metioned in the instructions by Firebase. You're good to go!

#### Cloud functions
* To set up cloud functions for Virgil JWT generation follow the instructions [here](https://github.com/VirgilSecurity/demo-firebase-func)
* Go to the Firebase console -> Functions tab and copy your function url from the Event column
* Go to AS -> app/src/main/java/com/android/virgilsecurity/virgilonfire/di/NetworkModule.java and change variable 'BASE_URL' to:
```
https://YOUR_FUNCTION_URL.cloudfunctions.net/api/generate_jwt
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
