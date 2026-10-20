# UPI Sentinel: Market Readiness & Production Release Plan

## Executive Overview
**UPI Sentinel** is an autonomous pre-payment risk detection engine designed to inspect UPI payment intents (QR codes, VPAs, URLs, and intent links) before financial authorization occurs.
Based on your requirements, the app will transition to **Google Play Store Market Readiness** through a **Dual-Mode Architecture** (instant on-device local heuristics + background cloud ML & cybercrime threat intelligence sync), accompanied by real-time clipboard/intent listening and Google Play compliance hardening.

---

## 1. Current State vs. Market-Ready Gap Analysis

| Component | Current State | Market-Ready Target |
| :--- | :--- | :--- |
| **Risk Inference Engine** | Local heuristic formula & standalone Python ML microservice | **Dual-Mode Engine**: Instant on-device evaluation (<50ms) with background cloud sync for dynamic model weights, SHAP explanations, and online threat score |
| **Threat Intelligence** | Static mock blacklist and local heuristics | **Live I4C / 1930 Cybercrime Intelligence Client**: Dynamic feed caching with offline Room SQLite fallback |
| **Payment Ingestion** | CameraX QR Scanner & Manual UPI Input Dialog | **System-Wide Protection**: Real-time clipboard UPI listener, Android UPI Intent Filter handler (`upi://pay`), and Photo Picker QR import |
| **Security & Privacy** | Open local SQLite database without biometrics | Encrypted Room database (SQLCipher or Android Keystore), biometric authentication toggle, zero-logging of sensitive user PINs |
| **Google Play Compliance** | Development build (`isMinifyEnabled = false`, debug keystore) | R8 code shrinking enabled, ProGuard rules, Google Play Data Safety declaration, privacy policy page, targetSdk 36 compliance |
| **Error Handling & UX** | Standard dialogs and presentation screens | Production network retry policies, offline indicators, battery-efficient background scanning, and NPCI incident report deep-linking |

---

## 2. Architecture: Dual-Mode Engine & Real-Time Interception

```
       [USER ACTION / TRIGGER]
  ┌─────────────────┬─────────────────┐
  ↓                 ↓                 ↓
CameraX QR       Clipboard         Android UPI Intent
Scanner          Listener          (upi://pay Intercept)
  └─────────────────┼─────────────────┘
                    ↓
         UPI INTENT PARSER & NORMALIZER
                    ↓
       ┌────────────────────────┐
       │   DUAL-MODE EVALUATION │
       └────────────┬───────────┘
                    ├──────────────────────────────────────┐
                    ↓ (Instant: <50ms)                     ↓ (Async Cloud Sync: <400ms)
       LOCAL ON-DEVICE ENGINE                 CLOUD ML & THREAT FEED (I4C/1930)
       - Regex phishing heuristics            - XGBoost ensemble & Isolation Forest
       - Local I4C cache in Room              - Dynamic cybercrime threat feed
       - Behavioral baseline comparisons      - SHAP TreeExplainer feature deltas
                    │                                      │
                    └──────────────────┬───────────────────┘
                                       ↓
                             FUSED DECISION ENGINE
                         [Safe | Medium | High | Critical]
                                       ↓
                           ACTIONABLE SENTINEL BANNER
                    [Safe to Proceed] or [Block & Report 1930]
```

---

## 3. Step-by-Step Implementation Roadmap

### Phase 1: Dual-Mode Network & Cloud ML Sync Layer
1. **Retrofit & OkHttp Cloud Sync Client**:
   - Implement `SentinelCloudApiClient` connecting to the Node.js / FastAPI backend with configurable endpoint URL (`BuildConfig.SENTINEL_API_URL`).
   - Add offline-resilient caching with Room: when offline or under spotty connectivity, the on-device local engine evaluates immediately and queues a background synchronization once connectivity returns.
2. **Dynamic Weight & Threat Intelligence Cache**:
   - Cache latest model weights, suspicious domain lists, and known mule VPAs locally in Room so that on-device decisions mirror the central server even without connectivity.

### Phase 2: Live Cybercrime Intelligence (I4C / 1930 Feed)
1. **Threat Intelligence Repository**:
   - Create `CybercrimeThreatRepository` with scheduled periodic updates (WorkManager or on-app-launch sync).
   - Display threat status badges (*"Verified against I4C Cybercrime Registry"*, *"Reported 14 times on National Cyber Crime Portal"*).
2. **Direct Reporting Action**:
   - One-tap intent to dial **1930** (National Cyber Crime Reporting Helpline) or open the official `cybercrime.gov.in` portal pre-filled with the fraudulent VPA and transaction ID.

### Phase 3: Real-Time UPI Intent Interception & Clipboard Listener
1. **System-Wide UPI Intent Filter**:
   - Register an intent filter in `AndroidManifest.xml` for `android.intent.action.VIEW` handling schemes `upi://pay` and `upi://mandate`.
   - When a user clicks a UPI link in SMS, WhatsApp, or Chrome, UPI Sentinel intercepts it, runs the safety audit in an overlay/bottom sheet, and forwards the transaction to PhonePe/GPay/Paytm only if approved.
2. **Clipboard Safety Listener**:
   - Monitor clipboard for pasted UPI IDs (`name@bank`, `xyz@upi`) or payment links when the app opens or via lifecycle observer, offering instantaneous pre-flight verification.

### Phase 4: Google Play Store Release & Security Hardening
1. **Play Policy & Privacy Compliance**:
   - Zero-permission media access (photo picker if scanning QRs from gallery).
   - Prominent in-app Privacy & Security Disclosure detailing that zero UPI PINs or bank credentials are ever captured or transmitted.
   - Play Store metadata sync (30-character title limit, compliant descriptions without buzzwords).
2. **Build & Release Optimization**:
   - Configure R8 code shrinking and ProGuard rules in `proguard-rules.pro`.
   - Set up release signing configuration and generate release Android App Bundle (`.aab`).
   - Implement crash-free offline fallbacks so the app maintains a <0.1% ANR and crash rate.

---

## 4. Verification & Testing Strategy
- **Unit & Robolectric Tests**: Test UPI URI parsing, fallback heuristics, and dual-mode reconciliation when network drops.
- **Scenario Stress Testing**:
  - Test Case 1: Standard retail QR (verified merchant, low risk).
  - Test Case 2: Phishing QR with tampered VPA and shortened link (high risk, immediate block alert).
  - Test Case 3: Offline mode execution (ensuring instant response within 50ms without crashing).
- **Google Play Pre-Launch Validation**: Run `gradle assembleRelease` and verify bundle size, permissions, and lint checks.

---

## 5. Next Action
Please click **Proceed** below or share any adjustments to begin executing Phase 1 (Dual-Mode Cloud Sync & Live Cybercrime Integration).
