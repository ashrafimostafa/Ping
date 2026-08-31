# Ping

A two-phone “I love you” button. Each phone gets a short ID. Enter your partner’s ID once, then tap the heart — they get a notification.

There is no server you run. Pairing lives in Firebase Firestore. Push uses Firebase Cloud Messaging sent from the app.

## Firebase setup (required)

1. Open [Firebase Console](https://console.firebase.google.com/) and create a project.
2. Add an **Android** app with package name `com.mostafa.ping.app`.
3. Download `google-services.json` into `app/google-services.json`.
4. Authentication is optional now (the app uses a local device ID).
5. Firestore Database → Create database, then paste `firestore.rules` and Publish.
6. Cloud Messaging is on by default.

## Lock-screen notifications (backend-less)

FCM will not send from the official Android SDK. This app calls the FCM HTTP API from the phone using a Firebase **service account**. That is acceptable for a private two-person app. Do not publish that JSON in a public store build.

1. Firebase Console → Project settings → **Service accounts** → Generate new private key.
2. Save the file as `app/src/main/assets/fcm-service-account.json`
   (see `fcm-service-account.json.example`).
3. Rebuild and install on **both** phones.

Without that file, pairing and the heart still work. The other phone sees the ping when Ping is open.

## Use it

1. Install on both phones (same `google-services.json` / same Firebase project).
2. Allow notifications when asked.
3. Share your 6-character ID. On the other phone, type that ID and tap **Connect**.
4. Tap the heart. Partner gets “I love you ❤️”.

Both phones need Google Play services. A network that can reach Firebase is required.
