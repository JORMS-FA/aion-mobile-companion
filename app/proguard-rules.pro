# ProGuard rules for Aion Mobile Companion
-keepclassmembers class * extends android.webkit.WebView {
    <init>(android.content.Context);
    <init>(android.content.Context, android.util.AttributeSet);
}
-keepclassmembers class * extends androidx.work.Worker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.datastore.core.Serializer
-keepclassmembers class com.aion.mobile.data.model.** { *; }
-keepclassmembers class com.aion.mobile.notification.Reminder { *; }
