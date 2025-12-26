# 考试练习系统

## 项目简介

考试练习系统是一个基于 Java Swing 开发的桌面应用程序，支持学生在线练习和教师管理题库的完整考试系统。

## 主要功能

### 用户管理
- **用户注册**：学生可通过姓名、学号和密码注册账号
- **用户登录**：支持学生和教师两种角色登录
- **角色区分**：系统自动识别用户角色并进入对应界面

### 学生功能
- **在线考试**：参加教师发布的考试
- **成绩查看**：查看历史考试记录和成绩
- **答题练习**：支持单选题、多选题、判断题、填空题等多种题型

### 教师功能
- **题库管理**：添加、修改、删除试题
- **试卷管理**：创建试卷、设置考试时间和分值
- **成绩管理**：查看学生考试成绩、统计分析

## 技术栈

- **开发语言**：Java 17
- **GUI框架**：Swing
- **数据库**：MySQL
- **构建工具**：Maven

## 项目结构

```
src/main/java/com/exam/
├── model/          # 实体类
│   ├── User.java
│   ├── Question.java
│   ├── Paper.java
│   ├── ExamRecord.java
│   └── enums/      # 枚举类
├── dao/            # 数据访问层
│   ├── UserDao.java
│   ├── QuestionDao.java
│   └── ExamDao.java
├── service/        # 业务逻辑层
│   ├── UserService.java
│   ├── QuestionService.java
│   └── ExamService.java
├── view/           # 界面层
│   ├── LoginFrame.java
│   ├── StudentMainFrame.java
│   └── TeacherMainFrame.java
├── util/           # 工具类
│   ├── DBUtil.java
│   ├── UIUtil.java
│   └── IconUtil.java
└── exception/      # 异常处理

src/main/resources/
├── sql/
│   └── exam_system.sql    # 数据库脚本
└── db.properties          # 数据库配置
```

## 数据库设计

### 主要数据表

- **user**：用户表（学生、教师信息）
- **question**：题库表（题目信息）
- **paper**：试卷表（试卷配置）
- **exam_record**：考试记录表（学生答题记录）
- **answer_record**：答题详情表（具体答案）

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
   - 创建数据库并执行 SQL 脚本
   ```sql
   source src/main/resources/sql/exam_system.sql
   ```
   
   - 修改数据库配置文件 `src/main/resources/db.properties`
   ```properties
   jdbc.url=jdbc:mysql://localhost:3306/exam_system
   jdbc.username=root
   jdbc.password=your_password
   ```

3. **编译项目**
   ```bash
   mvn clean compile
   ```

4. **运行项目**
   ```bash
   mvn exec:java -Dexec.mainClass="com.exam.Main"
   ```

## 默认测试账号

### 教师账号
- 姓名：张老师
- 学号：T001
- 密码：123456

### 学生账号
- 姓名：李明
- 学号：S2023001
- 密码：123456

## 功能特色

### 1. 蓝色系UI设计
- 清新简洁的蓝色主题界面
- 友好的用户交互体验
- 响应式焦点效果

### 2. 完善的题型支持
- 单选题（SINGLE）
- 多选题（MULTIPLE）
- 判断题（JUDGE）
- 填空题（BLANK）

### 3. 灵活的考试管理
- 自定义考试时长
- 自动计时提醒
- 实时成绩统计

### 4. 安全的用户认证
- 密码加密存储
- 学号唯一性验证
- 角色权限控制

## 开发规范

- 遵循 MVC 架构模式
- 使用 DAO 模式进行数据访问
- 统一异常处理机制
- 代码注释规范

## 后续优化方向

- [ ] 增加成绩导出功能
- [ ] 支持题目批量导入
- [ ] 添加错题本功能
- [ ] 优化界面响应速度
- [ ] 增加数据备份功能

## 许可证

本项目仅供学习交流使用。

## 联系方式

如有问题或建议，欢迎反馈。
