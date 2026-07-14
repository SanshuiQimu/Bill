# 记账本 Android 应用

一个使用 Kotlin + Jetpack Compose + Room 数据库开发的 Android 原生记账本应用。

## 功能特性

- **交易记录** - 记录收入和支出，支持金额、分类、描述、日期
- **分类管理** - 内置12个常用分类，支持自定义添加/删除分类
- **首页总览** - 月度收支总览卡片，交易列表左滑删除
- **统计报表** - 月度/年度统计，按分类汇总，进度条可视化占比
- **设置页面** - 分类管理、数据清空、应用信息
- **Material Design 3** - 支持亮色/暗色主题切换
- **Room 数据库** - 本地持久化存储，响应式 Flow 数据流

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Kotlin | 1.9.20 | 编程语言 |
| Jetpack Compose | BOM 2024.01.00 | 声明式 UI 框架 |
| Material Design 3 | - | UI 设计规范 |
| Room | 2.6.1 | 本地数据库 |
| Navigation Compose | 2.7.6 | 页面导航 |
| Lifecycle ViewModel | 2.7.0 | MVVM 架构 |
| Coroutines | 1.7.3 | 异步编程 |
| AGP | 8.2.0 | Android 构建工具 |
| minSdk | 24 | 最低支持 Android 7.0 |
| targetSdk | 34 | 目标 Android 14 |

## 项目结构

```
bill-app/
├── app/
│   ├── build.gradle.kts              # 应用级构建配置
│   ├── proguard-rules.pro            # 代码混淆规则
│   └── src/main/
│       ├── AndroidManifest.xml       # 应用清单
│       ├── java/com/sanshuiqimu/bill/
│       │   ├── MainActivity.kt       # 入口 Activity
│       │   ├── data/                 # 数据层
│       │   │   ├── BillDatabase.kt   # Room 数据库
│       │   │   ├── dao/              # 数据访问对象
│       │   │   ├── entity/           # 数据实体
│       │   │   └── repository/       # 数据仓库
│       │   ├── ui/                   # UI 层
│       │   │   ├── components/       # 通用组件
│       │   │   ├── navigation/       # 导航框架
│       │   │   ├── screens/          # 页面
│       │   │   │   ├── home/         # 首页
│       │   │   │   ├── add/          # 添加/编辑交易
│       │   │   │   ├── stats/        # 统计报表
│       │   │   │   └── settings/     # 设置
│       │   │   └── theme/            # 主题颜色/字体
│       │   └── util/                 # 工具类
│       └── res/                      # 资源文件
│           ├── values/               # 字符串、颜色、主题
│           ├── drawable/             # 图标
│           └── xml/                  # 备份规则
├── build.gradle.kts                  # 项目级构建配置
├── settings.gradle.kts               # 项目设置
└── gradle/                           # Gradle Wrapper
```

## 编译构建

### 环境要求

- JDK 17
- Android SDK 34
- Gradle 8.2

### 命令行构建

```bash
# Debug 构建
./gradlew assembleDebug

# Release 构建
./gradlew assembleRelease

# 生成的 APK 位于 app/build/outputs/apk/
```

### Android Studio

1. 用 Android Studio 打开 `bill-app` 目录
2. 等待 Gradle 同步完成
3. 点击 Run 按钮或使用 Shift+F10 运行

## 架构设计

采用 **MVVM + Repository** 架构：

- **View (Composable)** - 使用 Jetpack Compose 构建声明式 UI
- **ViewModel** - 管理 UI 状态，通过 StateFlow 暴露数据
- **Repository** - 统一数据访问入口，封装 DAO 操作
- **Room Database** - 本地 SQLite 持久化存储

## License

MIT
