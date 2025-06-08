# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Сохраняем информацию о строках для стектрейсов
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Общие правила
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes RuntimeVisible*Annotations*
-keepattributes RuntimeInvisible*Annotations*

# Правила для Kotlin
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-keep class kotlin.coroutines.Continuation
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Правила для Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.flow.**
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepclassmembers class kotlinx.coroutines.** {
    public <methods>;
}
-dontwarn kotlinx.atomicfu.**

# Правила для Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**
-keep class com.google.firebase.analytics.FirebaseAnalytics { *; }
-keep class com.google.firebase.analytics.FirebaseAnalytics$Event { *; }
-keep class com.google.firebase.analytics.FirebaseAnalytics$Param { *; }
-keep class com.google.android.gms.measurement.** { *; }
-keep class com.google.android.gms.ads.identifier.** { *; }

# Правила для Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keepclassmembers class androidx.compose.** {
    public <methods>;
}

# Правила для Koin
-keepnames class org.koin.** { *; }
-keep class org.koin.** { *; }
-keep class org.koin.core.** { *; }
-keep class org.koin.android.** { *; }
-dontwarn org.koin.**
-keepclassmembers class * {
    @org.koin.core.annotation.KoinInternalApi <methods>;
}
-keep @org.koin.core.annotation.KoinInternalApi class * { *; }

# Правила для моделей данных
-keep class com.davidbugayov.financeanalyzer.domain.model.** { *; }
-keep class com.davidbugayov.financeanalyzer.data.model.** { *; }
-keep class com.davidbugayov.financeanalyzer.data.local.entity.** { *; }
-keep class com.davidbugayov.financeanalyzer.presentation.**.model.** { *; }
-keepclassmembers class com.davidbugayov.financeanalyzer.** {
    <fields>;
    public <methods>;
}

# Правила для Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Database class *
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static <methods>;
}

# Правила для Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepclassmembers class * {
    @com.google.gson.annotations.Expose <fields>;
}

# Специальные правила для TypeToken
-keep,allowobfuscation class com.google.gson.reflect.TypeToken
-keep,allowobfuscation class * extends com.google.gson.reflect.TypeToken
-keep class com.davidbugayov.financeanalyzer.utils.GsonUtils { *; }

# Правила для сохранения generic типов
-keepattributes Signature
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes Exceptions
-keepattributes Deprecated
-keepattributes SourceFile
-keepattributes LineNumberTable
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations

# Правила для логирования в релизе
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
-assumenosideeffects class timber.log.Timber {
    public static void v(...);
    public static void d(...);
    public static void i(...);
}

# Правила для сохранения важных компонентов Android
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class * extends android.app.Fragment
-keep public class * extends androidx.fragment.app.Fragment

# Сохраняем native методы
-keepclasseswithmembernames class * {
    native <methods>;
}

# Сохраняем View constructors
-keepclasseswithmembers class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Сохраняем Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Сохраняем Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Сохраняем R8 конфигурацию
-keepattributes LineNumberTable,SourceFile
-renamesourcefileattribute SourceFile
-repackageclasses ''
-allowaccessmodification
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Оптимизации R8
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Удаляем лишние аттрибуты
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,RuntimeVisibleTypeAnnotations

# Сохраняем аннотации
-keep class * extends java.lang.annotation.Annotation { *; }
-keep @interface * { *; }
-keepattributes *Annotation*

# Сохраняем лямбды
-keepclassmembers class * {
    private static synthetic java.lang.Object $deserializeLambda$(java.lang.invoke.SerializedLambda);
}
-keepclassmembernames class * {
    private static synthetic *** lambda$*(...);
}
-dontwarn java.lang.invoke.LambdaMetafactory

# Правила для PDFBox
-keep class com.tom_roush.pdfbox.** { *; }
-dontwarn com.tom_roush.pdfbox.**
-keep class org.apache.pdfbox.** { *; }
-dontwarn org.apache.pdfbox.**
-keep class com.gemalto.jp2.** { *; }
-dontwarn com.gemalto.jp2.**

# ПОЛНОСТЬЮ СОХРАНЯЕМ ВСЕ КЛАССЫ, СВЯЗАННЫЕ С POI, БЕЗ МИНИФИКАЦИИ
-keep class org.apache.poi.** { *; }
-keep class org.apache.xmlbeans.** { *; }
-keep class org.openxmlformats.** { *; }
-keep class schemaorg_apache_xmlbeans.** { *; }
-keep class com.microsoft.schemas.** { *; }
-keep class org.w3c.** { *; }
-keep class org.xml.** { *; }
-keep class org.etsi.** { *; }
-keep class javax.xml.** { *; }
-keep class net.sf.saxon.** { *; }

# Не предупреждать об отсутствующих классах для POI
-dontwarn org.apache.poi.**
-dontwarn org.apache.xmlbeans.**
-dontwarn org.openxmlformats.**
-dontwarn com.microsoft.schemas.**
-dontwarn org.etsi.**
-dontwarn org.w3c.**
-dontwarn org.xml.**
-dontwarn net.sf.saxon.**
-dontwarn javax.xml.**
-dontwarn java.awt.**
-dontwarn org.osgi.**
-dontwarn org.osgi.framework.**
-dontwarn org.apache.commons.logging.**
-dontwarn org.apache.logging.**
-dontwarn org.apache.logging.log4j.**
-dontwarn org.slf4j.**

# Блокируем инициализацию Log4j - замена на пустые методы
-assumenosideeffects class org.apache.logging.log4j.LogManager {
    ** getContext(...);
    ** getLogger(...);
    ** getFormatterLogger(...);
}

-assumenosideeffects class org.apache.logging.log4j.** {
    ** *(...);
}

-assumenosideeffects class org.apache.logging.log4j.status.StatusLogger {
    ** *(...);
}

-assumenosideeffects class org.apache.logging.log4j.spi.AbstractLogger {
    ** *(...);
}

# Keep Apache POI classes
-keep class org.apache.poi.** { *; }
-keep class org.apache.xmlbeans.** { *; }
-keep class org.openxmlformats.** { *; }
-keep class com.microsoft.schemas.** { *; }
-keep class schemaorg_apache_xmlbeans.** { *; }
-keep class org.apache.commons.** { *; }
-keep class org.w3c.dom.** { *; }

# Keep Log4j classes
-keep class org.apache.logging.log4j.** { *; }

# Keep XML parsers needed by POI
-dontwarn javax.xml.**
-dontwarn org.apache.commons.**
-dontwarn org.apache.xmlbeans.**
-dontwarn org.apache.poi.**
-dontwarn org.openxmlformats.**
-dontwarn org.w3c.**
-dontwarn org.xml.sax.**

# Keep special POI factory methods
-keepclassmembers class org.apache.poi.** { 
  public static ** createRelationship(**); 
  public static ** getRelationshipById(**);
  public static ** createDocument(**);
}

# Keep all Apache POI classes and their methods
-keep class org.apache.poi.** { *; }
-keep class org.apache.xmlbeans.** { *; }
-keep class org.openxmlformats.** { *; }
-keep class org.apache.commons.** { *; }
-keep class com.zaxxer.** { *; }
-keep class javax.xml.** { *; }
-keep class org.xml.** { *; }
-keep class org.w3c.** { *; }

# Keep all XML-related libraries used by POI
-dontwarn javax.xml.**
-dontwarn org.apache.xml.**
-dontwarn org.apache.xmlbeans.**
-dontwarn org.w3c.dom.**
-dontwarn org.etsi.**
-dontwarn javax.activation.**
-dontwarn org.openxmlformats.**
-dontwarn org.slf4j.**
-dontwarn org.apache.log4j.**
-dontwarn org.apache.logging.**
-dontwarn org.apache.commons.compress.**
-dontwarn com.zaxxer.**

# Specifically keep classes needed for XLSX parsing
-keep class org.apache.poi.xssf.usermodel.** { *; }
-keep class org.apache.poi.ss.usermodel.** { *; }
-keep class org.apache.poi.util.** { *; }
-keep class org.apache.poi.ooxml.** { *; }
-keep class org.apache.poi.poifs.** { *; }
-keep class org.apache.poi.hssf.** { *; }

# Keep constructors that might be called via reflection
-keepclassmembers class org.apache.poi.** {
  public <init>(...);
}

# Keep POI factory methods
-keepclassmembers class org.apache.poi.ss.usermodel.WorkbookFactory {
  public static * create(...);
}

# Don't note errors about missing optional classes
-dontnote org.apache.poi.**
-dontnote org.apache.xmlbeans.**
-dontnote javax.xml.**

# Keep R8 from potentially optimizing away classes that are accessed via reflection
-keepattributes InnerClasses,Signature,*Annotation*

# Правила для javax.lang.model
-dontwarn javax.lang.model.element.Modifier