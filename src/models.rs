use chrono::NaiveDate;
use serde::{Deserialize, Serialize};

/// 交易类型：收入或支出
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq, Hash)]
pub enum TransactionType {
    /// 收入
    Income,
    /// 支出
    Expense,
}

impl std::fmt::Display for TransactionType {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            TransactionType::Income => write!(f, "收入"),
            TransactionType::Expense => write!(f, "支出"),
        }
    }
}

impl std::str::FromStr for TransactionType {
    type Err = String;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        match s.to_lowercase().as_str() {
            "income" | "收入" | "in" => Ok(TransactionType::Income),
            "expense" | "支出" | "out" | "ex" => Ok(TransactionType::Expense),
            _ => Err(format!("未知的交易类型: {}", s)),
        }
    }
}

/// 分类
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Category {
    pub name: String,
    pub r#type: TransactionType,
    /// 图标/emoji
    pub icon: Option<String>,
}

/// 交易记录
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Transaction {
    /// 唯一ID
    pub id: String,
    /// 交易类型
    pub transaction_type: TransactionType,
    /// 金额（以分为单位，避免浮点数精度问题）
    pub amount_cents: i64,
    /// 分类名称
    pub category: String,
    /// 描述/备注
    pub description: Option<String>,
    /// 交易日期 (YYYY-MM-DD)
    pub date: NaiveDate,
    /// 创建时间
    pub created_at: String,
}

impl Transaction {
    /// 获取金额（元）
    pub fn amount(&self) -> f64 {
        self.amount_cents as f64 / 100.0
    }

    /// 格式化金额字符串
    pub fn amount_str(&self) -> String {
        format!("{:.2}", self.amount())
    }
}

/// 账本数据
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct BillBook {
    pub transactions: Vec<Transaction>,
    pub categories: Vec<Category>,
}

impl Default for BillBook {
    fn default() -> Self {
        BillBook {
            transactions: Vec::new(),
            categories: get_default_categories(),
        }
    }
}

/// 默认分类列表
pub fn get_default_categories() -> Vec<Category> {
    vec![
        Category {
            name: "餐饮".to_string(),
            r#type: TransactionType::Expense,
            icon: Some("🍜".to_string()),
        },
        Category {
            name: "交通".to_string(),
            r#type: TransactionType::Expense,
            icon: Some("🚗".to_string()),
        },
        Category {
            name: "购物".to_string(),
            r#type: TransactionType::Expense,
            icon: Some("🛒".to_string()),
        },
        Category {
            name: "住房".to_string(),
            r#type: TransactionType::Expense,
            icon: Some("🏠".to_string()),
        },
        Category {
            name: "娱乐".to_string(),
            r#type: TransactionType::Expense,
            icon: Some("🎮".to_string()),
        },
        Category {
            name: "医疗".to_string(),
            r#type: TransactionType::Expense,
            icon: Some("💊".to_string()),
        },
        Category {
            name: "教育".to_string(),
            r#type: TransactionType::Expense,
            icon: Some("📚".to_string()),
        },
        Category {
            name: "其他支出".to_string(),
            r#type: TransactionType::Expense,
            icon: Some("📦".to_string()),
        },
        Category {
            name: "工资".to_string(),
            r#type: TransactionType::Income,
            icon: Some("💰".to_string()),
        },
        Category {
            name: "奖金".to_string(),
            r#type: TransactionType::Income,
            icon: Some("🎁".to_string()),
        },
        Category {
            name: "投资收益".to_string(),
            r#type: TransactionType::Income,
            icon: Some("📈".to_string()),
        },
        Category {
            name: "其他收入".to_string(),
            r#type: TransactionType::Income,
            icon: Some("💵".to_string()),
        },
    ]
}
