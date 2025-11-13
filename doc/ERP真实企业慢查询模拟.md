建立数据库表

```sql
-- 删除旧表（可选）
DROP TABLE IF EXISTS budget_records;

-- 创建预算记录表（优化版）
CREATE TABLE budget_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dept_id INT NOT NULL COMMENT '部门ID，对应组织架构',
    project_code VARCHAR(50) NOT NULL COMMENT '项目编码',
    year INT NOT NULL COMMENT '预算年份',
    month TINYINT NOT NULL COMMENT '预算月份 (1-12)',
    amount DECIMAL(15,2) NOT NULL COMMENT '预算金额',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-有效, 0-无效',
    version VARCHAR(20) DEFAULT 'v1' COMMENT '预算版本，如 v1, adjust_2024Q2',
    currency CHAR(3) DEFAULT 'CNY' COMMENT '币种',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- 添加联合索引（关键！）
    INDEX idx_query (dept_id, year, status, month),
    INDEX idx_project (project_code),
    INDEX idx_year_month (year, month),
    INDEX idx_version (version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
  ROW_FORMAT=DYNAMIC 
  COMMENT='预算明细表 - 支持多组织、多项目、多版本';
```



插入7260000条测试数据

```py
# generate_budget_data.py 插入数据的脚本
import pymysql
import random
from datetime import datetime
import time

# ==================== 配置参数 ====================
HOST = "localhost"
PORT = 3306
USER = "root"
PASSWORD = "123456"  # 修改为你的密码
DATABASE = "erp_test"         # 修改为你的数据库名

# 模拟规模配置
NUM_DEPTS = 10000           # 模拟 1万个部门（典型大型央企）
NUM_PROJECTS_PER_DEPT = 5   # 每个部门平均 5 个项目
YEARS = [2020, 2021, 2022, 2023, 2024]  # 5年数据
MONTHS = list(range(1, 13))             # 12个月
VERSIONS = ['v1', 'v2', 'adjust_q2', 'final']  # 多版本
STATUS_CHOICES = [1, 1, 1, 0]  # 有效:无效 = 3:1
CURRENCIES = ['CNY']

# 批量插入设置
BATCH_SIZE = 10000          # 每批次插入 1万 条
TARGET_TOTAL_ROWS = 12_000_000  # 目标：1200万条数据

# 金额范围（单位：元）
AMOUNT_MIN = 10000
AMOUNT_MAX = 5000000

# 连接数据库
def get_connection():
    return pymysql.connect(
        host=HOST,
        port=PORT,
        user=USER,
        password=PASSWORD,
        database=DATABASE,
        charset='utf8mb4',
        autocommit=False
    )

# 生成随机项目编码
def generate_project_code(dept_id, pid):
    return f"PROJ-D{dept_id:05d}-P{pid:03d}"

# 主函数：生成并插入数据
def generate_data():
    conn = get_connection()
    cursor = conn.cursor()

    inserted_rows = 0
    batch = []

    print(f"开始生成 {TARGET_TOTAL_ROWS:,} 条预算数据...")
    start_time = time.time()

    while inserted_rows < TARGET_TOTAL_ROWS:
        # 随机选择一个部门
        dept_id = random.randint(1, NUM_DEPTS)

        # 每个部门的项目数浮动
        num_projects = random.randint(1, 10)
        for _ in range(num_projects):
            project_code = generate_project_code(dept_id, random.randint(1, 999))

            year = random.choice(YEARS)
            month = random.choice(MONTHS)
            amount = round(random.uniform(AMOUNT_MIN, AMOUNT_MAX), 2)
            status = random.choice(STATUS_CHOICES)
            version = random.choice(VERSIONS)
            currency = random.choice(CURRENCIES)

            batch.append((
                dept_id, project_code, year, month, amount,
                status, version, currency
            ))

            inserted_rows += 1

            # 达到批次大小则插入
            if len(batch) >= BATCH_SIZE:
                try:
                    cursor.executemany("""
                        INSERT INTO budget_records 
                        (dept_id, project_code, year, month, amount, status, version, currency)
                        VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
                    """, batch)
                    conn.commit()
                    print(f"已插入 {inserted_rows:,} 条数据...")
                    batch.clear()
                except Exception as e:
                    conn.rollback()
                    print(f"插入失败: {e}")
                    raise

    # 插入最后一批剩余数据
    if batch:
        try:
            cursor.executemany("""
                INSERT INTO budget_records 
                (dept_id, project_code, year, month, amount, status, version, currency)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
            """, batch)
            conn.commit()
            print(f"✅ 全部 {inserted_rows:,} 条数据插入完成！")
        except Exception as e:
            conn.rollback()
            print(f"最后一批插入失败: {e}")
            raise
        finally:
            cursor.close()
            conn.close()

    end_time = time.time()
    print(f"总耗时: {end_time - start_time:.2f} 秒")
    print(f"平均速度: {inserted_rows / (end_time - start_time):.0f} 条/秒")

if __name__ == "__main__":
    generate_data()
```



**制造“慢查询”**

- 启动你的 Spring Boot 应用。
- 发送一个查询请求，例如： `GET http://localhost:8029/api/budget/list?deptId=50&year=2024&startMonth=1&endMonth=12&status=1`

APIFOX返回结果 ：200 2.73 s 17.21 K

执行EXPLAIN 分析请求对应的SQL语句执行情况

```sql
EXPLAIN SELECT * FROM budget_records 
WHERE dept_id = 50 
  AND year = 2024 
  AND month BETWEEN 1 AND 12 
  AND status = 1;
```

结果：

```text
type:ALL  

possible_keys:NULL

key:NULL

rows:7035890
```

加索引：

```sql
CREATE INDEX idx_budget_query ON budget_records (dept_id, YEAR, STATUS, MONTH);
```

重新执行查询

APIFOX返回结果 ：200 32 ms 17.21 K

响应时间2.73s->32ms，实现优化

可以多次测试，取平均值





