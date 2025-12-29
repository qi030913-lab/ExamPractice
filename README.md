# 在线考试练习系统

## 项目简介

在线考试练习系统是一个基于 Java Swing 开发的桌面应用程序，支持学生在线考试答题和教师管理题库试卷的完整考试管理系统。系统采用 MVC 架构，界面美观，功能完善，支持多种题型，适用于学校、培训机构等场景。

## 主要功能

### 用户管理
- **双端独立启动**：学生端和教师端分别独立启动，角色隔离
- **用户注册**：学生可通过姓名、学号和密码注册账号
- **用户登录**：支持学生和教师两种角色独立登录
- **角色权限**：基于角色的权限控制，确保数据安全

### 学生功能
- **在线考试**：参加教师发布的考试
- **成绩查看**：查看历史考试记录和详细成绩
- **答题练习**：支持10种题型的在线答题
- **个人信息**：查看个人信息和考试历史

### 教师功能
- **题库管理**：添加、修改、删除试题，支持批量导入
- **试卷管理**：创建试卷、设置考试时间和分值、发布/取消发布
- **成绩管理**：查看学生考试成绩、统计分析
- **学生管理**：查看学生信息和考试情况
- **题目导入**：支持文本文件批量导入题目，提供导入模板

## 技术栈

- **开发语言**：Java 17
- **GUI框架**：Swing (Nimbus主题)
- **数据库**：MySQL 5.7+
- **构建工具**：Maven 3.6+
- **JDBC驱动**：MySQL Connector/J 8.0.33
- **架构模式**：MVC + DAO

## 项目结构

```
src/main/java/com/exam/
├── StudentApplication.java     # 学生端启动类
├── TeacherApplication.java     # 教师端启动类
├── model/                      # 实体类
│   ├── User.java              # 用户实体
│   ├── Question.java          # 题目实体
│   ├── Paper.java             # 试卷实体
│   ├── ExamRecord.java        # 考试记录实体
│   ├── AnswerRecord.java      # 答题记录实体
│   └── enums/                 # 枚举类
│       ├── UserRole.java      # 用户角色枚举
│       ├── QuestionType.java  # 题目类型枚举
│       ├── Difficulty.java    # 难度枚举
│       └── ExamStatus.java    # 考试状态枚举
├── dao/                        # 数据访问层
│   ├── UserDao.java
│   ├── QuestionDao.java
│   ├── PaperDao.java
│   └── ExamRecordDao.java
├── service/                    # 业务逻辑层
│   ├── UserService.java
│   ├── QuestionService.java
│   ├── PaperService.java
│   └── ExamService.java
├── view/                       # 界面层
│   ├── student/               # 学生端界面
│   │   ├── StudentLoginFrame.java
│   │   ├── StudentMainFrame.java
│   │   ├── ExamFrame.java
│   │   ├── manager/           # 业务管理
│   │   └── ui/components/     # UI组件
│   ├── teacher/               # 教师端界面
│   │   ├── TeacherLoginFrame.java
│   │   ├── TeacherMainFrame.java
│   │   ├── manager/           # 业务管理
│   │   └── ui/components/     # UI组件
│   └── RoleSelectionFrame.java
├── util/                       # 工具类
│   ├── DBUtil.java            # 数据库工具
│   ├── UIUtil.java            # 界面工具
│   ├── IconUtil.java          # 图标工具
│   └── QuestionImportUtil.java # 题目导入工具
└── exception/                  # 异常处理
    ├── BusinessException.java
    ├── DatabaseException.java
    └── AuthenticationException.java

src/main/resources/
├── sql/
│   ├── exam_system.sql        # 数据库建表脚本
│   └── update_question_type.sql
├── data/                       # 数据文件
│   ├── 题目导入模板.txt
│   ├── 数据结构.txt
│   ├── 操作系统.txt
│   └── 马克思主义.txt
├── pic/                        # 图片资源
└── db.properties               # 数据库配置
```

## 数据库设计

### 主要数据表

- **user**：用户表（学生、教师信息，包含角色、密码等）
- **question**：题库表（题目信息，支持10种题型）
- **paper**：试卷表（试卷配置，包含总分、时长、发布状态等）
- **paper_question**：试卷题目关联表（多对多关系）
- **exam_record**：考试记录表（考试状态、得分等）
- **answer_record**：答题详情表（学生具体答案和得分）

### 数据库特性

- 使用 InnoDB 引擎，支持事务和外键约束
- UTF8MB4 字符集，支持中文和特殊字符
- 完善的索引设计，提高查询效率
- 级联删除策略，保证数据一致性

## 安装与运行

### 环境要求

- JDK 17 或更高版本
- MySQL 5.7 或更高版本
- Maven 3.6 或更高版本

### 安装步骤

1. **克隆项目**
   ```bash
   git clone <项目地址>
   cd untitled
   ```

2. **配置数据库**
   
   创建数据库并执行 SQL 脚本：
   ```bash
   mysql -u root -p < src/main/resources/sql/exam_system.sql
   ```
   
   或在 MySQL 客户端中执行：
   ```sql
   source src/main/resources/sql/exam_system.sql;
   ```
   
   修改数据库配置文件 `src/main/resources/db.properties`：
   ```properties
   db.url=jdbc:mysql://localhost:3306/exam_system?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
   db.username=root
   db.password=your_password
   db.driver=com.mysql.cj.jdbc.Driver
   ```

3. **编译项目**
   ```bash
   mvn clean compile
   ```

4. **运行项目**
   
   **学生端启动：**
   ```bash
   mvn exec:java -Dexec.mainClass="com.exam.StudentApplication"
   ```
   
   **教师端启动：**
   ```bash
   mvn exec:java -Dexec.mainClass="com.exam.TeacherApplication"
   ```
   
   或在 IDE (如 IntelliJ IDEA) 中直接运行对应的启动类。

## 使用说明

### 首次使用

1. 启动学生端或教师端
2. 点击「注册」按钮创建账号
3. 填写姓名、学号和密码完成注册
4. 使用注册的账号登录系统

### 教师端操作

1. **题库管理**
   - 手动添加题目：选择题型、填写内容、设置分值和难度
   - 批量导入：使用提供的模板文件批量导入题目
   - 修改/删除：对已有题目进行编辑或删除

2. **试卷管理**
   - 创建试卷：输入试卷名称、科目、时长等信息
   - 添加题目：从题库中选择题目添加到试卷
   - 发布试卷：将试卷发布给学生，学生可以参加考试

3. **成绩管理**
   - 查看学生考试记录和成绩
   - 统计分析考试数据

### 学生端操作

1. **参加考试**
   - 选择已发布的试卷
   - 点击「开始考试」进入答题界面
   - 完成答题后提交试卷

2. **查看成绩**
   - 查看历史考试记录
   - 查看详细得分和正确答案

## 功能特色

### 1. 双端独立设计
- 学生端和教师端独立启动
- 角色隔离，界面专注
- 各自独立的功能模块

### 2. 丰富的题型支持
系统支持以下10种题型：
- **SINGLE** - 单选题
- **MULTIPLE** - 多选题
- **JUDGE** - 判断题
- **BLANK** - 填空题
- **APPLICATION** - 应用题
- **ALGORITHM** - 算法设计题
- **SHORT_ANSWER** - 简答题
- **COMPREHENSIVE** - 综合题
- **ESSAY** - 论述题
- **MATERIAL_ANALYSIS** - 材料分析题

### 3. 便捷的题目导入
- 支持文本文件批量导入题目
- 提供详细的导入模板和格式说明
- 导入时自动验证数据格式
- 支持多种题型混合导入

### 4. 灵活的考试管理
- 自定义考试时长和分值
- 自动计时和提醒
- 试卷发布/取消发布控制
- 实时成绩统计和分析

### 5. 美观的界面设计
- Nimbus 主题，界面现代化
- 响应式布局，操作友好
- 图标和颜色搭配协调
- 用户信息居中显示

### 6. 完善的权限控制
- 基于角色的权限管理
- 学号唯一性验证
- 安全的用户认证机制

## 开发规范

### 架构设计
- **MVC 模式**：Model-View-Controller 三层分离
- **DAO 模式**：数据访问对象模式，封装数据库操作
- **分层架构**：View → Service → DAO → Database

### 代码规范
- 统一的异常处理机制（BusinessException、DatabaseException 等）
- 完善的代码注释和文档
- 规范的命名约定（驼峰命名法）
- 合理的包结构划分

### 数据库规范
- 使用外键约束保证数据一致性
- 适当的索引优化查询性能
- 级联删除策略避免脏数据
- UTF8MB4 字符集支持多语言

## 题目导入格式说明

### 文件格式

题目文件为文本格式（UTF-8 编码），每行一道题目，字段之间用 `|` 分隔。

**格式：** `题目类型|科目|题目内容|选项A|选项B|选项C|选项D|正确答案|分值|难度|解析`

### 示例

```text
# 单选题
SINGLE|Java|Java中哪个关键字可以用来定义常量？|const|final|static|constant|B|5|EASY|final关键字用于定义常量

# 多选题
MULTIPLE|Java|Java中哪些是访问修饰符？|public|private|protected|final|ABC|10|EASY|public、private、protected是访问修饰符

# 判断题
JUDGE|Java|Java支持多继承|正确|错误|||B|5|EASY|Java不支持类的多继承

# 填空题
BLANK|Java|Java中声明整数变量的关键字是____。||||int|5|EASY|使用int关键字声明整数类型

# 应用题
APPLICATION|Java|请编写一个Java程序，实现学生成绩管理系统|||||1.定义Student类 2.使用ArrayList存储|20|MEDIUM|考查面向对象和集合框架

# 算法设计题
ALGORITHM|算法|请设计一个算法，实现快速排序|||||1.选择基准元素 2.分区操作 3.递归排序|25|HARD|快速排序是分治算法的应用
```

### 字段说明

- **题目类型**：SINGLE、MULTIPLE、JUDGE、BLANK、APPLICATION、ALGORITHM、SHORT_ANSWER、COMPREHENSIVE、ESSAY、MATERIAL_ANALYSIS
- **科目**：任意字符串（如：Java、数据结构、操作系统等）
- **题目内容**：题目描述
- **选项A-D**：选择题的选项（填空题、应用题等可为空）
- **正确答案**：选择题填选项字母（如A、B、ABC），主观题填答案要点
- **分值**：整数（默认5分）
- **难度**：EASY、MEDIUM、HARD（默认MEDIUM）
- **解析**：题目解析说明（可选）

### 注意事项

- 以 `#` 开头的行为注释，会被忽略
- 文件编码必须为 UTF-8
- 字段之间使用英文竖线 `|` 分隔
- 空字段也需要保留分隔符

## 后续优化方向

- [ ] 增加成绩导出功能（Excel/PDF）
- [ ] 添加错题本功能
- [ ] 支持图片题目
- [ ] 优化界面响应速度
- [ ] 增加数据备份和恢复功能
- [ ] 支持在线考试防作弊机制
- [ ] 增加题目标签和分类检索
- [ ] 支持试卷自动组卷功能

## 技术亮点

1. **双启动类设计**：学生端和教师端分别独立启动，实现角色隔离
2. **MVC+DAO 架构**：清晰的分层架构，易于维护和扩展
3. **10种题型支持**：从基础题型到复杂题型的全面覆盖
4. **批量导入功能**：提高教师录题效率
5. **完善的异常处理**：统一的异常处理机制，提高系统稳定性
6. **现代化UI设计**：Nimbus主题，界面美观友好

## 常见问题

### 1. 数据库连接失败

检查 `db.properties` 配置是否正确，确保 MySQL 服务已启动。

### 2. 中文乱码问题

确保数据库和数据表使用 `utf8mb4` 字符集，连接字符串包含 `characterEncoding=utf8`。

### 3. 题目导入失败

检查导入文件编码是否为 UTF-8，格式是否符合要求，参考 `题目导入模板.txt`。

### 4. 如何添加新题型

1. 在 `QuestionType` 枚举中添加新题型
2. 更新数据库 `question` 表的 `question_type` 字段
3. 修改前端界面适配新题型
4. 更新导入模板和工具类

## 许可证

本项目仅供学习交流使用。

## 贡献指南

欢迎提交 Issue 和 Pull Request 来改进项目。

## 联系方式

如有问题或建议，欢迎反馈。

---

**开发环境**：IntelliJ IDEA 2023.2.1  
**开发语言**：Java 17  
**数据库**：MySQL 5.7+  
**项目类型**：Java Swing 桌面应用
