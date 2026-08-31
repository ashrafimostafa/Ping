-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

-keepclasseswithmembers @kotlinx.serialization.Serializable class * {
    *;
}

-keep class com.mostafa.ping.app.data.** { *; }

-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
