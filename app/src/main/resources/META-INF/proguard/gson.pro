## Gson specific rules ##
# Gson uses generic type information stored in a class file when working with fields.
# Proguard removes such information by default, so configure it to keep all of it.
-keepattributes Signature
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# For using GSON @Expose annotation
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeVisibleTypeAnnotations

# Gson specific classes
-dontwarn sun.misc.**
-dontwarn com.google.gson.**
-keep class com.google.gson.** { *; }
-keep interface com.google.gson.** { *; }
-keep enum com.google.gson.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.davidbugayov.financeanalyzer.data.** { *; }
-keep class com.davidbugayov.financeanalyzer.domain.model.** { *; }
-keep class com.davidbugayov.financeanalyzer.presentation.**.model.** { *; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from stripping information from TypeToken
-keep,allowobfuscation class com.google.gson.reflect.TypeToken
-keep,allowobfuscation class * extends com.google.gson.reflect.TypeToken

# Retain generic signatures of TypeToken and its subclasses
-keepattributes Signature
-keepattributes EnclosingMethod

# Gson TypeToken
-keep,allowobfuscation class com.google.gson.reflect.TypeToken
-keep,allowobfuscation class * extends com.google.gson.reflect.TypeToken
-keep class com.davidbugayov.financeanalyzer.utils.GsonUtils { *; }

# Gson TypeAdapter
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Сохраняем поля с аннотацией SerializedName
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Сохраняем Serializable классы
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Специальные правила для ArrayList<String>
-keepclassmembers class java.util.ArrayList {
    private Object[] elementData;
    private int size;
}
-keepclassmembers class java.lang.String { *; } 