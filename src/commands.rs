use crate::cli::{CategoryActions, Commands};
use crate::models::{BillBook, Category, Transaction, TransactionType};
use crate::storage;
use chrono::Local;
use chrono::NaiveDate;
use colored::*;

/// 将元转换为分
fn yuan_to_cents(yuan: &str) -> Result<i64, String> {
    let value: f64 = yuan
        .parse()
        .map_err(|_| format!("无效的金额: {}", yuan))?;
    if value < 0.0 {
        return Err("金额不能为负数".to_string());
    }
    Ok((value * 100.0).round() as i64)
}

/// 解析日期，支持 "YYYY-MM-DD" 或默认今天
fn parse_date(date_str: &Option<String>) -> Result<NaiveDate, String> {
    match date_str {
        Some(s) => NaiveDate::parse_from_str(s, "%Y-%m-%d")
            .map_err(|_| format!("无效的日期格式: {} (应为 YYYY-MM-DD)", s)),
        None => Ok(Local::now().date_naive()),
    }
}

/// 生成简短ID
fn generate_id() -> String {
    let uuid = uuid::Uuid::new_v4().to_string();
    uuid[..8].to_string()
}

/// 处理命令
pub fn handle_command(command: Commands) {
    match command {
        Commands::Add {
            r#type,
            amount,
            category,
            description,
            date,
        } => {
            cmd_add(r#type, amount, category, description, date);
        }
        Commands::List {
            r#type,
            category,
            month,
            limit,
            all,
        } => {
            cmd_list(r#type, category, month, limit, all);
        }
        Commands::Delete { id } => {
            cmd_delete(id);
        }
        Commands::Edit {
            id,
            r#type,
            amount,
            category,
            description,
            date,
        } => {
            cmd_edit(id, r#type, amount, category, description, date);
        }
        Commands::Category { action } => {
            cmd_category(action);
        }
        Commands::Stats { month, year } => {
            cmd_stats(month, year);
        }
        Commands::Summary => {
            cmd_summary();
        }
        Commands::Export { output } => {
            cmd_export(output);
        }
        Commands::Clear { yes } => {
            cmd_clear(yes);
        }
        Commands::Path => {
            println!("数据文件路径: {}", storage::data_path_str());
        }
    }
}

/// 添加交易
fn cmd_add(
    type_str: String,
    amount: String,
    category: String,
    description: Option<String>,
    date: Option<String>,
) {
    let mut book = storage::load();

    let txn_type: TransactionType = type_str.parse().unwrap_or_else(|e| {
        eprintln!("{} {}", "错误:".red().bold(), e);
        std::process::exit(1);
    });

    let amount_cents = yuan_to_cents(&amount).unwrap_or_else(|e| {
        eprintln!("{} {}", "错误:".red().bold(), e);
        std::process::exit(1);
    });

    let txn_date = parse_date(&date).unwrap_or_else(|e| {
        eprintln!("{} {}", "错误:".red().bold(), e);
        std::process::exit(1);
    });

    // 检查分类是否存在，不存在则自动创建
    let cat_exists = book
        .categories
        .iter()
        .any(|c| c.name == category && c.r#type == txn_type);

    if !cat_exists {
        book.categories.push(Category {
            name: category.clone(),
            r#type: txn_type.clone(),
            icon: None,
        });
        println!(
            "{} 已自动创建新分类: {}",
            "提示:".yellow(),
            category.cyan()
        );
    }

    let txn = Transaction {
        id: generate_id(),
        transaction_type: txn_type.clone(),
        amount_cents,
        category: category.clone(),
        description,
        date: txn_date,
        created_at: Local::now().to_rfc3339(),
    };

    println!(
        "{} {} {} {} {} {}",
        "已添加:".green().bold(),
        txn.id.yellow(),
        match txn_type {
            TransactionType::Income => "+".green(),
            TransactionType::Expense => "-".red(),
        },
        txn.amount_str(),
        "元",
        format!("[{}] {}", txn.category, txn.date)
    );

    book.transactions.push(txn);

    if let Err(e) = storage::save(&book) {
        eprintln!("{} {}", "保存失败:".red().bold(), e);
        std::process::exit(1);
    }
}

/// 列出交易记录
fn cmd_list(
    type_str: Option<String>,
    category: Option<String>,
    month: Option<String>,
    limit: usize,
    all: bool,
) {
    let book = storage::load();

    let type_filter: Option<TransactionType> = type_str
        .as_ref()
        .map(|s| s.parse::<TransactionType>().unwrap_or_else(|e| {
            eprintln!("{} {}", "错误:".red().bold(), e);
            std::process::exit(1);
        }));

    let filtered: Vec<&Transaction> = book
        .transactions
        .iter()
        .filter(|t| {
            if let Some(ref tf) = type_filter {
                if t.transaction_type != *tf {
                    return false;
                }
            }
            if let Some(ref cat) = category {
                if t.category != *cat {
                    return false;
                }
            }
            if let Some(ref m) = month {
                let date_str = t.date.format("%Y-%m").to_string();
                if date_str != *m {
                    return false;
                }
            }
            true
        })
        .collect();

    let count = filtered.len();
    if count == 0 {
        println!("{}", "没有找到匹配的交易记录".yellow());
        return;
    }

    let display: Vec<&Transaction> = if all {
        filtered.clone()
    } else {
        let start = if count > limit { count - limit } else { 0 };
        filtered[start..].to_vec()
    };

    println!(
        "{} (共 {} 条，显示 {} 条)",
        "交易记录".cyan().bold(),
        count,
        display.len()
    );
    println!("{}", "─".repeat(80));

    for txn in &display {
        let type_str = match txn.transaction_type {
            TransactionType::Income => "+".green(),
            TransactionType::Expense => "-".red(),
        };

        let amount_color = match txn.transaction_type {
            TransactionType::Income => txn.amount_str().green(),
            TransactionType::Expense => txn.amount_str().red(),
        };

        let desc = txn
            .description
            .as_ref()
            .map(|d| format!(" {}", d))
            .unwrap_or_default();

        println!(
            "{} │ {} {} {:>10} 元 │ {} │ {}{}",
            txn.id.yellow(),
            type_str,
            txn.category,
            amount_color,
            txn.date,
            txn.transaction_type,
            desc.dimmed()
        );
    }

    println!("{}", "─".repeat(80));

    // 统计当前显示的总计
    let total_income: i64 = display
        .iter()
        .filter(|t| t.transaction_type == TransactionType::Income)
        .map(|t| t.amount_cents)
        .sum();
    let total_expense: i64 = display
        .iter()
        .filter(|t| t.transaction_type == TransactionType::Expense)
        .map(|t| t.amount_cents)
        .sum();
    let net = total_income - total_expense;

    println!(
        "总收入: {}  总支出: {}  净额: {}",
        format!("{:.2} 元", total_income as f64 / 100.0).green(),
        format!("{:.2} 元", total_expense as f64 / 100.0).red(),
        format!("{:.2} 元", net as f64 / 100.0).cyan()
    );
}

/// 删除交易
fn cmd_delete(id: String) {
    let mut book = storage::load();

    let index = book.transactions.iter().position(|t| t.id == id);

    match index {
        Some(i) => {
            let txn = book.transactions.remove(i);
            println!(
                "{} 已删除: {} {} {} 元 [{}]",
                "完成:".green().bold(),
                txn.id.yellow(),
                txn.transaction_type,
                txn.amount_str(),
                txn.category
            );
            if let Err(e) = storage::save(&book) {
                eprintln!("{} {}", "保存失败:".red().bold(), e);
                std::process::exit(1);
            }
        }
        None => {
            eprintln!("{} 找不到ID为 {} 的交易记录", "错误:".red().bold(), id);
            std::process::exit(1);
        }
    }
}

/// 编辑交易
fn cmd_edit(
    id: String,
    type_str: Option<String>,
    amount: Option<String>,
    category: Option<String>,
    description: Option<String>,
    date: Option<String>,
) {
    let mut book = storage::load();

    let txn = book
        .transactions
        .iter_mut()
        .find(|t| t.id == id);

    match txn {
        Some(t) => {
            if let Some(ts) = type_str {
                t.transaction_type = ts.parse().unwrap_or_else(|e| {
                    eprintln!("{} {}", "错误:".red().bold(), e);
                    std::process::exit(1);
                });
            }
            if let Some(a) = amount {
                t.amount_cents = yuan_to_cents(&a).unwrap_or_else(|e| {
                    eprintln!("{} {}", "错误:".red().bold(), e);
                    std::process::exit(1);
                });
            }
            if let Some(c) = category {
                t.category = c;
            }
            if let Some(d) = description {
                t.description = Some(d);
            }
            if let Some(dt) = date {
                t.date = parse_date(&Some(dt)).unwrap_or_else(|e| {
                    eprintln!("{} {}", "错误:".red().bold(), e);
                    std::process::exit(1);
                });
            }

            println!(
                "{} 已更新: {} {} {} 元 [{}]",
                "完成:".green().bold(),
                t.id.yellow(),
                t.transaction_type,
                t.amount_str(),
                t.category
            );

            if let Err(e) = storage::save(&book) {
                eprintln!("{} {}", "保存失败:".red().bold(), e);
                std::process::exit(1);
            }
        }
        None => {
            eprintln!("{} 找不到ID为 {} 的交易记录", "错误:".red().bold(), id);
            std::process::exit(1);
        }
    }
}

/// 分类管理
fn cmd_category(action: CategoryActions) {
    let mut book = storage::load();

    match action {
        CategoryActions::List => {
            println!("{}", "分类列表".cyan().bold());
            println!("{}", "─".repeat(40));

            let expenses: Vec<&Category> = book
                .categories
                .iter()
                .filter(|c| c.r#type == TransactionType::Expense)
                .collect();
            let incomes: Vec<&Category> = book
                .categories
                .iter()
                .filter(|c| c.r#type == TransactionType::Income)
                .collect();

            println!("\n{}", "支出分类".red().bold());
            for c in &expenses {
                let icon = c.icon.as_deref().unwrap_or("  ");
                println!("  {} {}", icon, c.name);
            }

            println!("\n{}", "收入分类".green().bold());
            for c in &incomes {
                let icon = c.icon.as_deref().unwrap_or("  ");
                println!("  {} {}", icon, c.name);
            }
        }
        CategoryActions::Add {
            name,
            r#type,
            icon,
        } => {
            let cat_type: TransactionType = r#type.parse().unwrap_or_else(|e| {
                eprintln!("{} {}", "错误:".red().bold(), e);
                std::process::exit(1);
            });

            let exists = book
                .categories
                .iter()
                .any(|c| c.name == name && c.r#type == cat_type);

            if exists {
                eprintln!("{} 分类 {} 已存在", "错误:".red().bold(), name);
                std::process::exit(1);
            }

            book.categories.push(Category {
                name: name.clone(),
                r#type: cat_type,
                icon,
            });

            println!("{} 已添加分类: {}", "完成:".green().bold(), name.cyan());

            if let Err(e) = storage::save(&book) {
                eprintln!("{} {}", "保存失败:".red().bold(), e);
                std::process::exit(1);
            }
        }
        CategoryActions::Remove { name } => {
            let before = book.categories.len();
            book.categories.retain(|c| c.name != name);
            let after = book.categories.len();

            if before == after {
                eprintln!("{} 找不到分类: {}", "错误:".red().bold(), name);
                std::process::exit(1);
            }

            println!("{} 已删除分类: {}", "完成:".green().bold(), name.cyan());

            if let Err(e) = storage::save(&book) {
                eprintln!("{} {}", "保存失败:".red().bold(), e);
                std::process::exit(1);
            }
        }
    }
}

/// 统计报表
fn cmd_stats(month: Option<String>, year: Option<String>) {
    let book = storage::load();

    let now = Local::now().date_naive();
    let has_month = month.is_some();
    let target_month = month.unwrap_or_else(|| now.format("%Y-%m").to_string());
    let target_year = year.unwrap_or_else(|| now.format("%Y").to_string());

    let filtered: Vec<&Transaction> = if has_month {
        book.transactions
            .iter()
            .filter(|t| t.date.format("%Y-%m").to_string() == target_month)
            .collect()
    } else {
        book.transactions
            .iter()
            .filter(|t| t.date.format("%Y").to_string() == target_year)
            .collect()
    };

    if has_month {
        println!("📊 月度统计: {}", target_month.cyan().bold());
    } else {
        println!("📊 年度统计: {}", target_year.cyan().bold());
    }
    println!("{}", "═".repeat(60));

    let total_income: i64 = filtered
        .iter()
        .filter(|t| t.transaction_type == TransactionType::Income)
        .map(|t| t.amount_cents)
        .sum();
    let total_expense: i64 = filtered
        .iter()
        .filter(|t| t.transaction_type == TransactionType::Expense)
        .map(|t| t.amount_cents)
        .sum();
    let net = total_income - total_expense;

    println!(
        "\n{}: {}  {}: {}  {}: {}",
        "总收入".green().bold(),
        format!("{:.2} 元", total_income as f64 / 100.0).green(),
        "总支出".red().bold(),
        format!("{:.2} 元", total_expense as f64 / 100.0).red(),
        "结余".cyan().bold(),
        format!("{:.2} 元", net as f64 / 100.0).cyan()
    );

    // 按分类统计
    println!("\n{}", "按分类统计".cyan().bold());
    println!("{}", "─".repeat(60));

    use std::collections::HashMap;
    let mut cat_stats: HashMap<(String, TransactionType), i64> = HashMap::new();

    for t in &filtered {
        let key = (t.category.clone(), t.transaction_type.clone());
        *cat_stats.entry(key).or_insert(0) += t.amount_cents;
    }

    let mut sorted_stats: Vec<_> = cat_stats.into_iter().collect();
    sorted_stats.sort_by(|a, b| b.1.cmp(&a.1));

    for ((cat, txn_type), amount) in &sorted_stats {
        let type_str = match txn_type {
            TransactionType::Income => "+".green(),
            TransactionType::Expense => "-".red(),
        };
        let amount_colored = match txn_type {
            TransactionType::Income => format!("{:.2} 元", *amount as f64 / 100.0).green(),
            TransactionType::Expense => format!("{:.2} 元", *amount as f64 / 100.0).red(),
        };
        println!("  {} {:<12} {:>12}", type_str, cat, amount_colored);
    }

    // 按日期统计
    if !has_month {
        println!("\n{}", "按月统计".cyan().bold());
        println!("{}", "─".repeat(60));

        let mut month_stats: HashMap<String, (i64, i64)> = HashMap::new();
        for t in &filtered {
            let m = t.date.format("%Y-%m").to_string();
            let entry = month_stats.entry(m).or_insert((0, 0));
            match t.transaction_type {
                TransactionType::Income => entry.0 += t.amount_cents,
                TransactionType::Expense => entry.1 += t.amount_cents,
            }
        }

        let mut sorted_months: Vec<_> = month_stats.into_iter().collect();
        sorted_months.sort_by(|a, b| a.0.cmp(&b.0));

        for (m, (inc, exp)) in &sorted_months {
            let net = inc - exp;
            println!(
                "  {} │ 收入: {:>10} │ 支出: {:>10} │ 结余: {:>10}",
                m.yellow(),
                format!("{:.2}", *inc as f64 / 100.0).green(),
                format!("{:.2}", *exp as f64 / 100.0).red(),
                format!("{:.2}", net as f64 / 100.0).cyan()
            );
        }
    }
}

/// 账户总览
fn cmd_summary() {
    let book = storage::load();

    println!("{}", "🧾 账户总览".cyan().bold());
    println!("{}", "═".repeat(60));

    let total_income: i64 = book
        .transactions
        .iter()
        .filter(|t| t.transaction_type == TransactionType::Income)
        .map(|t| t.amount_cents)
        .sum();
    let total_expense: i64 = book
        .transactions
        .iter()
        .filter(|t| t.transaction_type == TransactionType::Expense)
        .map(|t| t.amount_cents)
        .sum();
    let balance = total_income - total_expense;

    println!(
        "\n  {}: {}",
        "交易总数".bold(),
        book.transactions.len()
    );
    println!(
        "  {}: {}",
        "分类总数".bold(),
        book.categories.len()
    );
    println!(
        "  {}: {}",
        "总收入".green().bold(),
        format!("{:.2} 元", total_income as f64 / 100.0).green()
    );
    println!(
        "  {}: {}",
        "总支出".red().bold(),
        format!("{:.2} 元", total_expense as f64 / 100.0).red()
    );
    println!(
        "  {}: {}",
        "当前余额".cyan().bold(),
        format!("{:.2} 元", balance as f64 / 100.0).cyan()
    );

    if balance >= 0 {
        println!("\n  💚 财务状况良好，保持下去！");
    } else {
        println!("\n  ⚠ 注意：支出超过收入，请合理规划开支。");
    }
}

/// 导出CSV
fn cmd_export(output: String) {
    let book = storage::load();

    let mut csv_content = String::from("ID,类型,金额,分类,描述,日期\n");

    for t in &book.transactions {
        let type_str = match t.transaction_type {
            TransactionType::Income => "收入",
            TransactionType::Expense => "支出",
        };
        let desc = t.description.as_deref().unwrap_or("");
        csv_content.push_str(&format!(
            "{},{},{:.2},{},{},{}\n",
            t.id,
            type_str,
            t.amount(),
            t.category,
            desc,
            t.date
        ));
    }

    std::fs::write(&output, csv_content.as_bytes()).unwrap_or_else(|e| {
        eprintln!("{} 导出失败: {}", "错误:".red().bold(), e);
        std::process::exit(1);
    });

    println!(
        "{} 已导出 {} 条记录到 {}",
        "完成:".green().bold(),
        book.transactions.len(),
        output.cyan()
    );
}

/// 清空数据
fn cmd_clear(yes: bool) {
    if !yes {
        println!("{}", "⚠ 警告: 此操作将删除所有交易记录和自定义分类！".red().bold());
        println!("请使用 --yes 参数确认清除操作。");
        return;
    }

    let book = BillBook::default();
    if let Err(e) = storage::save(&book) {
        eprintln!("{} 清除失败: {}", "错误:".red().bold(), e);
        std::process::exit(1);
    }

    println!("{} 所有数据已清除", "完成:".green().bold());
}
