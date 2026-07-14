# 🧾 Bill - 命令行记账本

一个使用 Rust 编写的功能丰富的命令行记账本应用，支持交易记录、分类管理、统计分析等功能。

## ✨ 功能特性

- **交易记录** - 记录收入和支出，支持金额、分类、描述、日期
- **分类管理** - 内置常用分类，支持自定义添加/删除分类
- **列表查询** - 按类型、分类、月份过滤，支持分页显示
- **编辑删除** - 随时修改或删除已有记录
- **统计报表** - 月度/年度统计，按分类和月份汇总
- **账户总览** - 查看总收入、总支出和当前余额
- **数据导出** - 导出为 CSV 格式
- **数据持久化** - JSON 文件自动存储，无需数据库

## 📦 安装

### 从源码编译

```bash
# 克隆仓库
git clone https://github.com/SanshuiQimu/Bill.git
cd Bill

# 编译
cargo build --release

# 可执行文件位于 target/release/bill
# 可复制到系统路径
cp target/release/bill /usr/local/bin/
```

### 开发模式运行

```bash
cargo run -- <命令>
```

## 🚀 使用方法

### 添加交易

```bash
# 添加一笔支出
bill add -t expense -a 25.50 -c 餐饮 -d "午餐" --date 2025-01-15

# 添加一笔收入
bill add -t income -a 8000 -c 工资 -d "一月工资" --date 2025-01-10
```

### 查看交易列表

```bash
# 查看最近20条记录
bill list

# 查看所有记录
bill list --all

# 按类型过滤
bill list -t expense

# 按分类过滤
bill list -c 餐饮

# 按月份过滤
bill list -m 2025-01
```

### 编辑交易

```bash
bill edit <ID> -a 30.00 -d "修改后的描述"
```

### 删除交易

```bash
bill delete <ID>
```

### 分类管理

```bash
# 列出所有分类
bill category list

# 添加分类
bill category add -n 咖啡 -t expense -i ☕

# 删除分类
bill category remove 咖啡
```

### 统计报表

```bash
# 月度统计
bill stats -m 2025-01

# 年度统计
bill stats -y 2025
```

### 账户总览

```bash
bill summary
```

### 导出数据

```bash
bill export -o my_bill.csv
```

### 其他

```bash
# 查看数据文件路径
bill path

# 清空所有数据（需确认）
bill clear --yes
```

## 📁 数据存储

交易数据存储在用户主目录下的 `~/.bill_data.json` 文件中，使用 JSON 格式。

## 🛠️ 技术栈

- **Rust** - 系统编程语言
- **clap** - 命令行参数解析
- **serde** - 序列化/反序列化
- **chrono** - 日期时间处理
- **colored** - 终端彩色输出
- **uuid** - 唯一ID生成

## 📄 License

MIT
