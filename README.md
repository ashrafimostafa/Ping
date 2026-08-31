<p align="center">
  <img src="app/src/main/ic_launcher-playstore.png" width="120" alt="Ping app icon" />
</p>

# Ping

A tiny app for two people. Tap a heart on your phone — your partner gets an **I love you** notification on theirs.

## What it does

- Each phone gets a short **6-character ID**
- You connect once by entering your partner’s ID
- Tap the heart anytime to ping them
- They see a full-screen / lock-screen alert (when push is set up)

No chat. No accounts. Just one button for the two of you.

## How it works

1. **Pairing** — IDs and partner links are stored in **Firebase Firestore**
2. **Ping** — tapping the heart writes to Firestore and sends a push with **Firebase Cloud Messaging (FCM)**
3. **No custom server** — the app talks to Firebase directly  
   (FCM send uses a service-account key inside the app — fine for a private two-person build)

Package name: `com.mostafa.ping.app`

## Setup (once)

1. Create a Firebase project and add an Android app with package `com.mostafa.ping.app`
2. Put `google-services.json` in `app/`
3. Create **Firestore**, paste `firestore.rules`, click **Publish**
4. For lock-screen alerts: Project settings → Service accounts → Generate new private key  
   Save as `app/src/main/assets/fcm-service-account.json`
5. Build and install on **both** phones (same Firebase project)

Use a VPN if Google/Firebase is blocked on your network.

## Use it

1. Open Ping on both phones and allow notifications
2. Share your ID → partner taps **Connect**
3. Tap the heart → they get **I love you**
