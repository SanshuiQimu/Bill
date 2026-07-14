// 项目级构建文件：声明各模块共用的插件版本，但不在此应用
// AGP 8.2.0 与 Kotlin 1.9.20 版本相互兼容
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("com.android.library") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
}
