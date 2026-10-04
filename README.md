<div align="center">

  <h1>⚡ AWAZ AI (آواز AI)</h1>
  <h3><i>The Unbroken Valley Shield — Offline-First Community Safety Grid</i></h3>

  <p><b>Chitral AI Challenge 2026 Submission</b></p>

  [![Platform](https://img.shields.io/badge/Platform-Android_Native_%7C_WebView-brightgreen?style=for-the-badge&logo=android)](https://github.com/ryannkhan-oss/Awaz-ai)
  [![AI Engine](https://img.shields.io/badge/AI_Engine-TinyML_TensorFlow_Lite-FF6F00?style=for-the-badge&logo=tensorflow)](https://github.com/ryannkhan-oss/Awaz-ai)
  [![Offline Maps](https://img.shields.io/badge/Maps-OpenStreetMap_Vector_Packs-7EBC6F?style=for-the-badge&logo=openstreetmap)](https://github.com/ryannkhan-oss/Awaz-ai)
  [![Theme](https://img.shields.io/badge/UI/UX-Nano_Banana_Theme-FFE500?style=for-the-badge)](https://github.com/ryannkhan-oss/Awaz-ai)

  <br />
</div>

---

## 📌 Problem Statement

In mountainous, high-altitude regions like **District Chitral**, severe blackouts, mountain terrain, and frequent cellular outage zones render traditional cloud-based safety and navigation applications completely useless. 

**AWAZ AI** solves this critical problem by deploying an **offline-first, zero-cloud safety ecosystem** that provides on-device distress detection, anonymous threat mapping, and peer-to-peer mesh communications without requiring an active internet connection.

---

## 🚀 Key Features

| Feature | Technical Implementation | Safety Impact |
| :--- | :--- | :--- |
| **🚨 3-Click Hardware Trigger** | Native `KeyEventListener` capturing 3 rapid power button presses | Activates silent emergency alerts without unlocking screen or opening app |
| **🎙️ On-Device TinyML Listener** | Quantized 8-bit TensorFlow Lite spectrogram sound classifier | Detects screams/distress signals in local RAM without recording audio |
| **🗺️ Offline Vector Maps** | Chitral Town core + Modular regional packs (Booni, Mastuj, Drosh) | 100% offline walking navigation & safe routing during blackouts |
| **📡 BLE Device Mesh Relay** | Android Nearby Connections protocol | Relays encrypted alerts phone-to-phone across zero-coverage blackout zones |
| **🔒 Zero-Knowledge Privacy** | SHA-256 client hashing + 500m coordinate rounding | Ensures zero metadata leaks & absolute reporting anonymity |
| **🛡️ Anti-Spam Shield** | DBSCAN spatial-temporal clustering & daily submission quotas | Prevents false alarms, duplicate reports, and malicious business ratings |

---

## 🎨 UI/UX Design: "Nano Banana" High-Contrast Theme

Designed for high readability during night emergency situations and intense outdoor glare:
* **Background:** Deep Obsidian Matte (`#0F0F12`)
* **Primary Accent:** Electric Safety Yellow (`#FFE500`)
* **Cards:** High-contrast Glassmorphism with prominent stroke borders

---

## 👥 Engineering Team & Contributions

<div align="center">

| Developer | Core Responsibility |
| :--- | :--- |
| **Dayyan** *(Team Lead)* | **Team Lead & TinyML Acoustic AI Architecture** |
| **Ryan Ali Sajid** | **System Architect & Hardware Integration Lead** |
| **Abdullah** | **UI/UX Engineer & Nano Banana Design System** |
| **Millad** | **P2P BLE Mesh Network & Offline Relay Engineer** |

</div>

---

## 🛠️ Quick Start & Build Instructions

### 1️⃣ Run Local Asset / Web Preview
```bash
# Clone the repository
git clone [https://github.com/ryannkhan-oss/Awaz-ai.git](https://github.com/ryannkhan-oss/Awaz-ai.git)

# Navigate to local web assets
cd Awaz-ai/app/src/main/assets/

# Open index.html in browser or Live Server
