@echo off
echo ========================================================
echo        ARKAIOS - Compilador de APK ArkaiosTify
echo ========================================================
echo Configurando entornos Java JDK y Android SDK...
set "JAVA_HOME=C:\Android\Android Studio\jbr"
set "ANDROID_HOME=C:\Users\djklm\AppData\Local\Android\Sdk"
set "ANDROID_SDK_ROOT=C:\Users\djklm\AppData\Local\Android\Sdk"
set "PATH=%JAVA_HOME%\bin;%ANDROID_HOME%\platform-tools;%PATH%"

cd /d "%~dp0"

echo Compilando Spotify-Arkaios con Gradle 9.4.1...
call "C:\Users\djklm\.gradle\wrapper\dists\gradle-9.4.1-bin\arn2x92ynaizyzdaamcbpbhtj\gradle-9.4.1\bin\gradle.bat" assembleDebug

echo.
echo ========================================================
echo  Compilacion exitosa. Instalando en celular via ADB...
echo ========================================================
"C:\platform-tools\adb.exe" install -r "app\build\outputs\apk\debug\app-debug.apk"
pause
