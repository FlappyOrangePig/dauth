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
-keepclassmembers class org.web3j.protocol.** { ; }
-keepclassmembers class org.web3j.crypto.* { *; }
#Keep Web3j types as the library extracts data from type names at runtime (e.g. integer size of Int)
-keep class * extends org.web3j.abi.TypeReference
-keep class * extends org.web3j.abi.datatypes.Type
#Don't warn about Web3j's java.lang.SafeVarargs
#-dontwarn java.lang.SafeVarargs
-dontwarn org.slf4j.**

-keep class org.web3j.** { *; }

#walletconnect
-keepclassmembers class com.walletconnect.** { ; }

########################################
# DAuth
########################################
-keep class com.cyberflow.dauthsdk.** { *; }