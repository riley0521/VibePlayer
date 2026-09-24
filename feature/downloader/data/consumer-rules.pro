# youtubedl-android drives yt-dlp as a subprocess and maps its JSON with Jackson.
-keep class com.yausername.** { *; }
-keep class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.**
-dontwarn java.beans.**
-dontwarn org.w3c.dom.bootstrap.DOMImplementationRegistry
