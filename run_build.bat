@echo off
set "JAVA_HOME=C:\Android\Android Studio\jbr"
set "ANDROID_HOME=C:\Users\djklm\AppData\Local\Android\Sdk"
set "ANDROID_SDK_ROOT=C:\Users\djklm\AppData\Local\Android\Sdk"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Building Spotify-Arkaios APK with JAVA_HOME=%JAVA_HOME%...
call "C:\Users\djklm\.gradle\wrapper\dists\gradle-9.4.1-bin\arn2x92ynaizyzdaamcbpbhtj\gradle-9.4.1\bin\gradle.bat" assembleDebug --no-daemon
