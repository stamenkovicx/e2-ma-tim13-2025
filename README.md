# e2-ma-tim13-2025
# 📱 RPG Productivity App

Dobrodošli u **RPG Productivity App** — aplikaciju koja kombinuje produktivnost i RPG elemente!  
Ovaj dokument opisuje osnovne korake potrebne za pokretanje projekta lokalno.

---

## 🚀 Preduslovi

### 🧰 Softverski zahtjevi
- Android Studio (latest version)
- Java JDK 11+
- Android SDK (API level 24+)
- Gradle 7.0+

---

## 🔥 Firebase konfiguracija

### 1. Kreiranje projekta
1. Otvorite [Firebase Console](https://console.firebase.google.com)
2. Kliknite **“Add project”** i slijedite uputstva
3. Kada je projekat kreiran, dodajte Android aplikaciju

### 2. Povezivanje Android aplikacije
1. Kliknite Android ikonu (**➕**)  
2. Unesite:
   - **Package name:** `com.example.myapplication`
   - (opciono) **App nickname:** RPG Productivity App  
3. Preuzmite `google-services.json`  
4. Smjestite fajl u folder **`app/`**

### 3. Omogućite servise
- ✅ Authentication (Email/Password)  
- ✅ Firestore Database  
- ✅ Firebase Storage *(opciono)*  

---

## 🔔 OneSignal konfiguracija *(opciono)*

### 1. Kreiranje aplikacije
1. Idite na [OneSignal Dashboard](https://onesignal.com)
2. Kliknite **“New App/Website”**
3. Odaberite:
   - Platform: **Google Android**
   - Name: **RPG Productivity App**
4. Sačuvajte **App ID** i **REST API Key**

### 2. Povezivanje sa projektom
- Dodajte svoj **App ID** i **REST API Key** u aplikaciju prema dokumentaciji.  
  (nema potrebe za ručnim unosom u kod)

---

## 📱 Pokretanje aplikacije

1. Otvorite projekat u **Android Studio**.  
2. Sačekajte da se **Gradle sinhronizacija** završi.  
3. Izaberite **emulator** ili povežite **fizički uređaj**.  
4. Kliknite na **Run ▶** da pokrenete aplikaciju.

---

✨ Sada ste spremni da testirate **RPG Productivity App** i uživate u kombinaciji produktivnosti i RPG elemenata!
