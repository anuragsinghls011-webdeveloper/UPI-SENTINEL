# UPI Sentinel: Google Play Store Launch Readiness Checklist

## 1. Compliance & Security Declarations
- **Zero-Access UPI PIN Policy**: UPI Sentinel operates ahead of payments; it never captures, stores, or logs bank PINs or card CVVs.
- **Android Intent Interception**: `AndroidManifest.xml` handles `android.intent.action.VIEW` for `upi://pay` and `upi://mandate` so users clicking payment links in SMS or browsers receive an instant safety audit before opening their payment app.
- **Clipboard Sentinel**: Proactively notifies users when a UPI ID or payment link is copied to the clipboard, preventing clipboard hijacking scams.
- **Data Safety**: Only metadata (merchant identity, amount, VPA structure) is evaluated. Offline-first local evaluation ensures user privacy.

## 2. Cybercrime & National Helplines
- **1930 Helpline**: Deep-linked one-tap dialer for immediate reporting within the cyber fraud Golden Hour.
- **cybercrime.gov.in**: Direct action button to initiate formal disputes with pre-filled VPA & transaction identifiers.
- **I4C Threat Intelligence**: Verified mule VPA feed caching for offline detection of repeat offenders.

## 3. Production Release Artifacts
- **Compiled Production APK**: `./UPI-Sentinel-Production.apk` (26 MB)
- **ProGuard / R8 Rules**: Configured in `app/proguard-rules.pro` with obfuscation and class preservation for Retrofit, Moshi, Room, and CameraX.
