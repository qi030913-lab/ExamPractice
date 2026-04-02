# 在线考试系统

## 当前架构

项目正在从 `Java Swing` 迁移到 `Vue3 + Electron + Spring Boot`。

当前推荐的正式桌面端入口：

- 前端：`Vue3`
- 桌面壳：`Electron`
- 后端：`Spring Boot`

当前保留但已降级为兼容入口：

- `com.exam.StudentApplication`
- `com.exam.TeacherApplication`
- `com.exam.MultiThreadApplication`

这些 Swing 启动类仍可用于旧版联调，但不再是推荐主入口。

## 推荐启动方式

### 1. 开发模式启动桌面端

先启动 Vue 渲染层和 Electron：

```bash
cd desktop
npm install
npm run dev
```

说明：

- Electron 主进程会优先启动并检查 Spring Boot 后端。
- 如果本地缺少 `target/exam-server-headless.jar`，需要先执行一次 Maven 打包。

### 2. 首次构建后再启动

在项目根目录执行：

```bash
mvn -q -Dmaven.test.skip=true package
```

然后启动桌面端：

```bash
cd desktop
npm start
```

这会使用已构建好的：

- `target/exam-server-headless.jar`
- `desktop/dist/renderer`

## 常用构建命令

### 后端编译

```bash
mvn -q -DskipTests compile
```

### 后端完整打包

```bash
mvn -q -Dmaven.test.skip=true package
```

### 前端渲染层打包

```bash
cd desktop
npm run build:renderer
```

## 当前桌面端已迁移能力

### 教师端

- 登录与工作台导航
- 试卷中心
- 导题建卷
- 导题模板下载
- 学生中心
- 学生考试记录查看

### 学生端

- 登录与工作台导航
- 考试中心
- 进行中考试恢复
- 提交结果页
- 成绩中心
- 成绩详情
- 学生成就图表

## 目录说明

### 桌面端

```text
desktop/
├── src/main/                 # Electron 主进程与 preload
├── src/renderer-vue/         # Vue3 渲染层
└── dist/renderer/            # Vue 构建产物
```

### Java 后端与兼容层

```text
src/main/java/com/exam/
├── api/                      # Spring Boot API
├── bridge/                   # Electron Bridge
├── view/                     # Swing 旧界面
├── StudentApplication.java   # Swing 学生兼容入口
├── TeacherApplication.java   # Swing 教师兼容入口
├── MultiThreadApplication.java
└── TeacherServerHeadless.java # Electron 使用的无界面后端入口
```

## 当前迁移策略

1. 正式桌面入口统一收口到 Electron。
2. 业务页面逐步由 Vue3 替代 Swing。
3. 底层业务能力通过 Spring Boot API 和 Electron Bridge 承接。
4. Swing 入口暂时保留，仅用于兼容和过渡。

## 注意事项

- 数据库配置仍位于 `src/main/resources/db.properties`
- Electron 启动时会把数据库配置映射为后端进程环境变量
- 若后端启动失败，优先检查数据库连接、`target/exam-server-headless.jar` 是否存在，以及 `8080` 端口是否被占用
