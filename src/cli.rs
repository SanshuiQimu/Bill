use clap::{Parser, Subcommand};

/// 🧾 Bill - 一个功能丰富的命令行记账本应用
#[derive(Parser, Debug)]
#[command(name = "bill")]
#[command(version = "1.0.0")]
#[command(about = "一个功能丰富的命令行记账本应用", long_about = None)]
pub struct Cli {
    #[command(subcommand)]
    pub command: Commands,
}

#[derive(Subcommand, Debug)]
pub enum Commands {
    /// 添加一条交易记录
    Add {
        /// 交易类型: income(收入) / expense(支出)
        #[arg(short = 't', long)]
        r#type: String,

        /// 金额（元）
        #[arg(short = 'a', long)]
        amount: String,

        /// 分类名称
        #[arg(short = 'c', long)]
        category: String,

        /// 描述/备注
        #[arg(short = 'd', long)]
        description: Option<String>,

        /// 日期 (YYYY-MM-DD)，默认今天
        #[arg(long)]
        date: Option<String>,
    },

    /// 列出交易记录
    List {
        /// 按类型过滤: income / expense
        #[arg(short = 't', long)]
        r#type: Option<String>,

        /// 按分类过滤
        #[arg(short = 'c', long)]
        category: Option<String>,

        /// 按月份过滤 (YYYY-MM)
        #[arg(short = 'm', long)]
        month: Option<String>,

        /// 显示最近 N 条记录
        #[arg(short = 'n', long, default_value = "20")]
        limit: usize,

        /// 显示所有记录（忽略 limit）
        #[arg(long)]
        all: bool,
    },

    /// 删除一条交易记录
    Delete {
        /// 交易记录ID
        id: String,
    },

    /// 编辑一条交易记录
    Edit {
        /// 交易记录ID
        id: String,

        /// 交易类型: income / expense
        #[arg(short = 't', long)]
        r#type: Option<String>,

        /// 金额（元）
        #[arg(short = 'a', long)]
        amount: Option<String>,

        /// 分类名称
        #[arg(short = 'c', long)]
        category: Option<String>,

        /// 描述/备注
        #[arg(short = 'd', long)]
        description: Option<String>,

        /// 日期 (YYYY-MM-DD)
        #[arg(long)]
        date: Option<String>,
    },

    /// 管理分类
    Category {
        #[command(subcommand)]
        action: CategoryActions,
    },

    /// 查看统计报表
    Stats {
        /// 按月份过滤 (YYYY-MM)，默认当月
        #[arg(short = 'm', long)]
        month: Option<String>,

        /// 按年度过滤 (YYYY)
        #[arg(short = 'y', long)]
        year: Option<String>,
    },

    /// 查看账户总览
    Summary,

    /// 导出数据为CSV
    Export {
        /// 导出文件路径
        #[arg(short = 'o', long, default_value = "bill_export.csv")]
        output: String,
    },

    /// 清空所有数据（危险操作，需确认）
    Clear {
        /// 跳过确认提示
        #[arg(long)]
        yes: bool,
    },

    /// 显示数据文件路径
    Path,
}

#[derive(Subcommand, Debug)]
pub enum CategoryActions {
    /// 列出所有分类
    List,

    /// 添加分类
    Add {
        /// 分类名称
        #[arg(short = 'n', long)]
        name: String,

        /// 分类类型: income / expense
        #[arg(short = 't', long)]
        r#type: String,

        /// 图标 (emoji)
        #[arg(short = 'i', long)]
        icon: Option<String>,
    },

    /// 删除分类
    Remove {
        /// 分类名称
        name: String,
    },
}
