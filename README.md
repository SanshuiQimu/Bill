<div align="center">

# 🧾 观账阁 Bill

### 一个简洁实用的个人记账应用 · Rust CLI + Android 原生

[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20CLI-blue?style=flat-square)](https://github.com/SanshuiQimu/Bill)
[![Language](https://img.shields.io/badge/Language-Kotlin%20%7C%20Rust-orange?style=flat-square)](https://github.com/SanshuiQimu/Bill)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)](LICENSE)
[![Version](https://img.shields.io/badge/Version-1.0.0Beta-brightgreen?style=flat-square)](https://github.com/SanshuiQimu/Bill/releases)

</div>

---

## 📱 下载安装

> **Android 7.0+** · 点击下方链接下载 APK 安装包

### [⬇ 下载 观账阁-v1.0.0Beta.apk](https://github.com/SanshuiQimu/Bill/releases/download/v1.0.0Beta/guanzhangge-v1.0.0Beta.apk)

安装前请在手机设置中开启「允许安装未知来源应用」。

---

## ✨ 功能特性

| 功能 | 说明 |
|------|------|
| 💰 **交易记录** | 记录收入和支出，支持金额、分类、描述、日期 |
| 🏷️ **分类管理** | 12个内置分类（餐饮🍜 交通🚗 购物🛒 工资💰 等），支持自定义 |
| 📊 **统计报表** | 月度/年度统计，按分类汇总，进度条可视化占比 |
| 🏠 **首页总览** | 月度收支卡片，交易列表，月份切换 |
| 🌙 **主题切换** | Material Design 3，支持亮色/暗色模式 |
| 💾 **本地存储** | Room 数据库持久化，无需联网，数据安全 |

---

## 📸 应用预览

### Android 应用

| 首页 - 收支总览 | 记一笔 | 统计报表 | 设置 |
|:---:|:---:|:---:|:---:|
| 📈 月度收支卡片 | 💰 收入/支出切换 | 📊 分类占比进度条 | ⚙️ 分类管理 |
| 📋 交易列表 | 🏷️ 分类选择 | 📅 月度/年度切换 | 🗑️ 数据清空 |
| 📅 月份切换 | 📆 日期选择器 | 💰 收支汇总 | ℹ️ 应用信息 |

### Rust CLI 工具

```
$ bill add -t expense -a 25.50 -c 餐饮 -d "午餐"
✅ 已添加: 2ba12f40 - 25.50 元 [餐饮] 2026-07-14

$ bill list --all
交易记录 (共 5 条，显示 5 条)
────────────────────────────────────────────────
2ba12f40 │ - 餐饮    25.50 元 │ 2026-07-14 │ 支出 午餐
92c301ed │ + 工资  8000.00 元 │ 2026-07-10 │ 收入 七月工资
────────────────────────────────────────────────
总收入: 8500.00 元  总支出: 495.50 元  净额: 8004.50 元
```

---

## 🏗️ 技术架构

### Android 应用 (`bill-app/`)

| 技术 | 版本 | 用途 |
|------|------|------|
| Kotlin | 1.9.20 | 编程语言 |
| Jetpack Compose | BOM 2024.01.00 | 声明式 UI 框架 |
| Material Design 3 | 1.1.2 | UI 设计规范 |
| Room | 2.6.1 | 本地数据库 ORM |
| Navigation Compose | 2.7.6 | 页面导航 |
| Lifecycle ViewModel | 2.7.0 | MVVM 架构 |
| Coroutines + Flow | 1.7.3 | 异步编程与响应式数据流 |
| AGP | 8.2.0 | Android 构建工具 |

**架构模式**: MVVM + Repository

```
UI (Composable) → ViewModel (StateFlow) → Repository → Room DAO → SQLite
```

### Rust CLI 工具 (`bill/`)

| 技术 | 用途 |
|------|------|
| Rust | 系统编程语言 |
| clap | 命令行参数解析 |
| serde | JSON 序列化 |
| chrono | 日期时间处理 |
| colored | 终端彩色输出 |

---

## 📁 项目结构

```
Bill/
├── bill-app/                        # Android 应用
│   ├── app/
│   │   ├── build.gradle.kts         # 应用构建配置
│   │   ├── src/main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/sanshuiqimu/bill/
│   │   │   │   ├── MainActivity.kt          # 入口 Activity
│   │   │   │   ├── data/                    # 数据层
│   │   │   │   │   ├── BillDatabase.kt      # Room 数据库
│   │   │   │   │   ├── dao/                 # 数据访问对象
│   │   │   │   │   ├── entity/              # 数据实体
│   │   │   │   │   └── repository/          # 数据仓库
│   │   │   │   ├── ui/                      # UI 层
│   │   │   │   │   ├── components/          # 通用组件
│   │   │   │   │   ├── navigation/          # 导航框架
│   │   │   │   │   ├── screens/             # 页面
│   │   │   │   │   │   ├── home/            # 首页
│   │   │   │   │   │   ├── add/             # 添加/编辑
│   │   │   │   │   │   ├── stats/           # 统计报表
│   │   │   │   │   │   └── settings/        # 设置
│   │   │   │   │   └── theme/               # 主题/颜色/字体
│   │   │   │   └── util/                    # 工具类
│   │   │   └── res/                         # 资源文件
│   │   └── proguard-rules.pro
│   ├── build.gradle.kts             # 项目级构建配置
│   ├── settings.gradle.kts
│   └── gradle.properties
│
├── bill/                            # Rust CLI 工具
│   ├── Cargo.toml
│   ├── src/
│   │   ├── main.rs                  # 程序入口
│   │   ├── cli.rs                   # CLI 命令定义
│   │   ├── commands.rs              # 命令处理逻辑
│   │   ├── models.rs                # 数据模型
│   │   └── storage.rs               # JSON 文件持久化
│   └── README.md
│
└── README.md                        # 本文件
```

---

## 🚀 快速开始

### Android 应用

#### 方式一：直接安装 APK

1. 点击 [下载链接](https://github.com/SanshuiQimu/Bill/releases/download/v1.0.0Beta/guanzhangge-v1.0.0Beta.apk) 下载 APK
2. 在手机上打开下载的 APK 文件
3. 允许安装未知来源应用
4. 安装完成，开始记账

#### 方式二：从源码编译

```bash
# 克隆仓库
git clone https://github.com/SanshuiQimu/Bill.git
cd Bill/bill-app

# 使用 Android Studio 打开，或命令行构建
./gradlew assembleDebug

# APK 位于 app/build/outputs/apk/debug/app-debug.apk
```

**环境要求**: JDK 17 · Android SDK 34 · Gradle 8.2

### Rust CLI 工具

```bash
cd Bill/bill
cargo build --release

# 使用示例
./target/release/bill add -t expense -a 25.50 -c 餐饮 -d "午餐"
./target/release/bill list --all
./target/release/bill stats -m 2026-07
./target/release/bill summary
```

---

## 📖 使用指南

### Android 应用

| 操作 | 说明 |
|------|------|
| **记一笔** | 点击底部导航栏中间的「+」按钮，选择收入/支出，输入金额，选择分类 |
| **查看记录** | 首页展示当月交易列表，左右切换月份 |
| **编辑记录** | 点击交易卡片进入编辑页面 |
| **删除记录** | 点击交易卡片右侧的删除图标 |
| **统计报表** | 底部导航栏「统计」标签，查看月度/年度收支分析 |
| **分类管理** | 底部导航栏「设置」标签，添加/删除自定义分类 |
| **清空数据** | 设置 → 数据管理 → 清空所有数据 |

### Rust CLI 命令

```bash
# 添加交易
bill add -t expense -a 25.50 -c 餐饮 -d "午餐" --date 2026-07-14
bill add -t income  -a 8000  -c 工资 -d "七月工资"

# 查看列表
bill list                       # 最近20条
bill list --all                 # 全部记录
bill list -t expense            # 仅支出
bill list -m 2026-07            # 按月份

# 编辑和删除
bill edit <ID> -a 30.00 -d "新描述"
bill delete <ID>

# 统计
bill stats -m 2026-07           # 月度统计
bill stats -y 2026              # 年度统计
bill summary                    # 账户总览

# 分类管理
bill category list
bill category add -n 咖啡 -t expense -i ☕
bill category remove 咖啡

# 导出
bill export -o my_bill.csv

# 其他
bill path                       # 数据文件路径
bill clear --yes                # 清空数据
```

---

## 📄 License

[MIT](LICENSE) © 2026 SanshuiQimu

<div align="center">

**如果这个项目对你有帮助，请给个 ⭐ Star**

</div>
