# Room / Hilt / Firebase mantienen sus propias reglas consumidas desde los AAR.
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses

# Entidades y modelos serializados hacia Firestore
-keep class com.iglesiaflow.gestion.data.local.entity.** { *; }
-keep class com.iglesiaflow.gestion.domain.model.** { *; }
-keep class com.iglesiaflow.gestion.data.remote.dto.** { *; }

# SQLCipher
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
-dontwarn net.sqlcipher.**

# Compose
-dontwarn org.jetbrains.annotations.**
