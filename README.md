# 📝 SmartScanner – AI Powered Document Scanner

SmartScanner is a high-performance Android application that bridges the gap between physical documents and digital data.  
It uses Google ML Kit OCR and CameraX API to scan documents, extract text in real-time, and export them as professional PDF files.

## 🚀 Key Features

✨ **Smart AI Scanning**  
Uses Google ML Kit’s Text Recognition (OCR) for highly accurate text extraction from images.

📷 **Real-time Camera Integration**  
Built with Android Jetpack CameraX for smooth, fast, and responsive camera preview.

📄 **PDF Generation**  
Instantly convert extracted text into well-formatted PDF files stored in the device storage.

🕘 **Scan History**  
Stores previous scans locally using Gson + SharedPreferences for quick access anytime.

🎨 **Modern UI/UX**  
Clean and intuitive Material Design interface with proper permission handling and real-time feedback.

📱 **Scoped Storage Support**  
Fully compatible with Android 10+ (API 29+) storage policies.

## 🛠️ Tech Stack & Libraries

- ☕ Language: Java  
- 📸 CameraX (Jetpack): Advanced camera control and image capture  
- 🤖 Google ML Kit: Optical Character Recognition (OCR)  
- 📑 iText7: Professional PDF generation  
- 💾 Gson: Data serialization for local history storage  
- 🎨 Material Design Components: Modern UI styling

## 📸 Screenshots

Add your app screenshots here:

- Home Screen  
- OCR Processing  
- PDF Output  
- Saved Documents  
- Scan History  

## ⚙️ Installation

### 1. Clone the Repository

git clone https://github.com/yourusername/SmartScanner.git

2. Open in Android Studio
Go to File > Open
Select the SmartScanner project folder
3. Sync Gradle
Wait for all dependencies to download (CameraX, ML Kit, iText, etc.)
4. Run the App
Connect your Android device or emulator
Click Run ▶ app
📂 Project Structure
com.example.smartscanner
┣ 📂 activities
┃ ┗ 📜 HistoryActivity.java   # Manages scan history screen
┣ 📂 models
┃ ┗ 📜 ScanModel.java         # Data model for scanned documents
┣ 📂 utils
┃ ┗ 📜 HistoryManager.java    # Handles local storage (Gson)
┗ 📜 MainActivity.java        # Core logic (Camera + OCR + PDF)
🛡️ Permissions Required

To ensure proper functionality, the app requires:

📷 CAMERA → To capture document images
💾 WRITE_EXTERNAL_STORAGE (API < 29) → To save generated PDF files

### 🤝 Contributing
Contributions are welcome 🚀

If you have ideas like:
Cloud Sync ☁️
Image Enhancement Filters 🎛️
AI Document Summarization 🤖

Feel free to improve the project!

### Steps:
git checkout -b feature/AmazingFeature
git commit -m "Add Amazing Feature"
git push origin feature/AmazingFeature

Then open a Pull Request.

### ⭐ Support
If you like this project, consider giving it a ⭐ on GitHub — it helps a lot!
