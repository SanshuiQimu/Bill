use crate::models::BillBook;
use std::fs;
use std::io::Write;
use std::path::PathBuf;

/// 获取数据文件路径
fn get_data_path() -> PathBuf {
    let home = dirs::home_dir().unwrap_or_else(|| PathBuf::from("."));
    home.join(".bill_data.json")
}

/// 加载账本数据
pub fn load() -> BillBook {
    let path = get_data_path();
    if path.exists() {
        match fs::read_to_string(&path) {
            Ok(content) => {
                if content.trim().is_empty() {
                    return BillBook::default();
                }
                match serde_json::from_str::<BillBook>(&content) {
                    Ok(book) => book,
                    Err(_) => {
                        eprintln!("⚠ 数据文件损坏，使用默认数据重新开始");
                        BillBook::default()
                    }
                }
            }
            Err(_) => BillBook::default(),
        }
    } else {
        BillBook::default()
    }
}

/// 保存账本数据
pub fn save(book: &BillBook) -> Result<(), String> {
    let path = get_data_path();
    let content = serde_json::to_string_pretty(book)
        .map_err(|e| format!("序列化数据失败: {}", e))?;

    let mut file = fs::File::create(&path)
        .map_err(|e| format!("创建数据文件失败: {}", e))?;

    file.write_all(content.as_bytes())
        .map_err(|e| format!("写入数据文件失败: {}", e))?;

    Ok(())
}

/// 获取数据文件路径字符串（用于显示）
pub fn data_path_str() -> String {
    get_data_path().to_string_lossy().to_string()
}
