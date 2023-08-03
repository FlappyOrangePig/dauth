########################################
# DAuth
########################################
-keep class com.infras.dauthsdk.** { *; }

########################################
# bouncycastle
########################################
-keep class org.bouncycastle.** { *; }

########################################
# web3j
########################################
#Java8 rules
-dontwarn java8.util.**

#JNR Posix rules
-dontwarn jnr.posix.**

#JFFI rules
#-dontwarn com.kenai.**

#BouncyCastle rules
#-keep class org.bouncycastle.**
-dontwarn org.bouncycastle.jce.provider.X509LDAPCertStoreSpi
-dontwarn org.bouncycastle.x509.util.LDAPStoreHelper
#Keep Web3j classes serialized via reflection by Jackson
-keepclassmembers class org.web3j.protocol.** { *; }
-keepclassmembers class org.web3j.crypto.* { *; }
#Keep Web3j types as the library extracts data from type names at runtime (e.g. integer size of Int)
-keep class * extends org.web3j.abi.TypeReference
-keep class * extends org.web3j.abi.datatypes.Type
#Don't warn about Web3j's java.lang.SafeVarargs
#-dontwarn java.lang.SafeVarargs
-dontwarn org.slf4j.**

-keep class org.web3j.** { *; }

########################################
# walletconnect
########################################
-keep class com.walletconnect.** { *; }

########################################
# okhttp3
########################################
-keep class okhttp3.** { *; }

########################################
# retrofit2
########################################
-keep class retrofit2.** { *; }

# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on RoboVM on iOS. Will not be used at runtime.
-dontnote retrofit2.Platform$IOS$MainThreadExecutor
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

########################################
# jackson
########################################
-keep class com.fasterxml.** { *; }
-keepattributes *Annotation*,EnclosingMethod,Signature
-keepnames class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.databind.**
-keep class org.codehaus.** { *; }
-keepclassmembers public final enum org.codehaus.jackson.annotate.JsonAutoDetect$Visibility {
    public static final org.codehaus.jackson.annotate.JsonAutoDetect$Visibility *; }
-keep public class your.class.** {
    public void set*(***);
    public *** get*();
}

########################################
# moshi
########################################
-keep class com.squareup.moshi.** { *; }
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}

-keep @com.squareup.moshi.JsonQualifier @interface *

# Enum field names are used by the integrated EnumJsonAdapter.
# values() is synthesized by the Kotlin compiler and is used by EnumJsonAdapter indirectly
# Annotate enums with @JsonClass(generateAdapter = false) to use them with Moshi.
-keepclassmembers @com.squareup.moshi.JsonClass class * extends java.lang.Enum {
    <fields>;
    **[] values();
}

# Keep helper method to avoid R8 optimisation that would keep all Kotlin Metadata when unwanted
-keepclassmembers class com.squareup.moshi.internal.Util {
    private static java.lang.String getKotlinMetadataClassName();
}

# Keep ToJson/FromJson-annotated methods
-keepclassmembers class * {
  @com.squareup.moshi.FromJson <methods>;
  @com.squareup.moshi.ToJson <methods>;
}

########################################
# gson
########################################
-keep class com.google.gson.** { *; }

########################################
# google移动服务
########################################
-keep class com.google.android.gms.** { *; }

########################################
# twitter
########################################
-keep class com.twitter.** { *; }

########################################
# AGP generated
########################################
# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn java.lang.invoke.StringConcatFactory
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn java.beans.ConstructorProperties
-dontwarn java.beans.Transient
-dontwarn com.oracle.svm.core.annotate.Delete
-dontwarn com.oracle.svm.core.annotate.Substitute
-dontwarn com.oracle.svm.core.annotate.TargetClass