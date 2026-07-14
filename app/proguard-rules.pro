# 项目自定义 ProGuard 规则
# 当 release 开启混淆时（isMinifyEnabled = true）生效

# ---------------- Room 相关 ----------------
# 保留 RoomDatabase 子类及其构造方法
-keep class * extends androidx.room.RoomDatabase { <init>(); }
# 保留 @Entity 注解的实体类
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ---------------- Kotlin Coroutines 相关 ----------------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** { volatile <fields>; }

# ---------------- Compose 相关 ----------------
-dontwarn androidx.compose.**

# ---------------- Kotlin 元数据 ----------------
-keep class kotlin.Metadata { *; }

# ---------------- 通用 ----------------
# 保留注解
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
