# LoginActivity —— Android 登录 / 注册示例项目

一个基于 **Kotlin + XML 布局** 的 Android 入门练习项目，实现了账号登录、账号注册、
"记住密码"与登录态保持，并内置 SQLite 本地数据库的基础设施（`DbHelper`），
为后续接入真实的账号校验做好准备。

---

## 一、项目简介

| 项 | 内容 |
|---|---|
| 应用名称 | LoginActivity |
| 包名 | `com.example.loginactivity` |
| 语言 | Kotlin |
| 界面方案 | XML 布局 + `findViewById`（未使用 Compose / ViewBinding） |
| 数据存储 | SharedPreferences（登录态、记住密码）+ SQLite（账号表结构） |
| 最低支持 | Android 7.0（API 24） |
| 目标版本 | API 37 |

项目最初由 TimeManager 项目中拆分而来，用于单独练习"认证"这一条链路：
**输入 → 校验 → 保存登录态 → 页面跳转**。

---

## 二、功能清单

### 已实现

- [x] 登录页表单校验（账号、密码非空）
- [x] 注册页表单校验（账号 ≥ 3 位、密码 ≥ 6 位、两次密码一致）
- [x] "记住密码"：勾选后下次进入自动回填账号密码
- [x] 登录态保持：已登录用户再次启动直接进入主页
- [x] 注册成功后自动返回登录页并回填账号
- [x] 密码可见性切换、错误提示（`TextInputLayout`）
- [x] SQLite 数据库脚手架：`users` / `todos` 两张表，单例连接

### 规划中

- [ ] 登录接入数据库真实校验（当前为固定 `userId = 1L`）
- [ ] 注册写入 `users` 表，拦截重复用户名
- [ ] 主页功能（当前仍是模板页）
- [ ] 专注倒计时模块迁移

---

## 三、技术栈

| 分类 | 使用内容 |
|---|---|
| 语言 | Kotlin（`lateinit`、`by lazy`、扩展函数、协程 `lifecycleScope`） |
| 界面 | ConstraintLayout、Material Components（`TextInputLayout`、`MaterialButton`、`MaterialCheckBox`） |
| 存储 | `SharedPreferences`（`SessionManager`）、`SQLiteOpenHelper`（`DbHelper`） |
| 构建 | Gradle 9 + Kotlin DSL（`build.gradle.kts`）、Version Catalog（`libs.versions.toml`） |

主要依赖：

```kotlin
implementation(libs.androidx.core.ktx)
implementation(libs.androidx.appcompat)
implementation(libs.androidx.activity.ktx)
implementation(libs.androidx.constraintlayout)
implementation(libs.material)
```

---

## 四、页面流程

```
桌面图标
   │
   ▼
LoginActivity ──── 已登录 ──────────────┐
   │                                   │
   │ 输入账号密码 / 点“立即注册”          │
   ▼                                   ▼
RegisterActivity（校验通过，回填账号）  MainActivity
   │                                   ▲
   └──────── 返回登录页 ──── 登录成功 ───┘
```

- `LoginActivity` 为启动页（`MAIN` / `LAUNCHER`）
- `RegisterActivity`、`MainActivity` 均为 `exported="false"`，只能由应用内部跳转

---

## 五、项目结构

```
LoginActivity/
├── app/src/main/
│   ├── java/com/example/loginactivity/
│   │   ├── LoginActivity.kt         登录页：表单校验、记住密码、登录跳转
│   │   ├── RegisterActivity.kt      注册页：三次输入校验、回填账号
│   │   ├── MainActivity.kt          主页（占位，待扩展）
│   │   ├── SessionManager.kt        SharedPreferences 封装：登录态 / 记住密码
│   │   └── data/
│   │       └── DbHelper.kt          SQLite 建表与单例连接
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_login.xml
│   │   │   ├── activity_register.xml
│   │   │   └── activity_main.xml
│   │   └── values/
│   │       ├── strings.xml          全部界面文字与提示语
│   │       ├── colors.xml
│   │       └── themes.xml
│   └── AndroidManifest.xml
├── gradle/libs.versions.toml        依赖版本集中管理
└── README.md
```

---

## 六、数据库设计

数据库文件：`/data/data/com.example.loginactivity/databases/login_app.db`

### users（账号表）

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT | 用户唯一编号 |
| username | TEXT | NOT NULL UNIQUE | 账号，唯一约束用于拦截重复注册 |
| password | TEXT | NOT NULL | 密码（当前为明文，见"已知限制"） |
| nickname | TEXT | NOT NULL DEFAULT '' | 昵称 |
| email | TEXT | NOT NULL DEFAULT '' | 邮箱 |

### todos（待办表，预留）

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT | 待办编号 |
| userId | INTEGER | NOT NULL | 归属用户（逻辑外键） |
| title | TEXT | NOT NULL | 标题 |
| description | TEXT | NOT NULL DEFAULT '' | 描述 |
| category | TEXT | NOT NULL DEFAULT '' | 分类 |
| deadline | INTEGER | NOT NULL | 截止时间（毫秒时间戳） |
| remindBeforeMinutes | INTEGER | NOT NULL DEFAULT 0 | 提前多少分钟提醒 |
| isDone | INTEGER | NOT NULL DEFAULT 0 | 是否完成（0 / 1） |

> `DbHelper` 目前只负责建表与提供连接，实际读写由后续的 `UserRepository` 完成。
> 数据库版本 `DB_VERSION = 1`，修改表结构时需同步升版本，否则 `onCreate` 不会重新执行。

---

## 七、核心类说明

| 类 | 职责 | 关键方法 |
|---|---|---|
| `LoginActivity` | 登录页交互与跳转 | `login()`、`fillRememberedAccount()`、`openMain()` |
| `RegisterActivity` | 注册表单校验 | `register()` |
| `SessionManager` | 键值存储封装，隔离 SharedPreferences 细节 | `saveLogin()`、`isLoggedIn()`、`saveRemembered()`、`clearRemembered()` |
| `DbHelper` | 数据库入口：建表、版本管理、单例连接 | `onCreate()`、`onUpgrade()`、`get(context)` |

### 数据存储分工

| 数据 | 存储位置 | 原因 |
|---|---|---|
| 当前登录用户 id | SharedPreferences（`session.xml`） | 只有一条，读写频繁 |
| 记住的账号密码 | SharedPreferences | 少量键值对 |
| 账号、待办 | SQLite | 多行多列，需要查询与排序 |

---

## 八、关键实现说明

### 1. 登录态判断放在 `setContentView` 之前

```kotlin
if (session.isLoggedIn()) {
    openMain()
    return
}
setContentView(R.layout.activity_login)
```

已登录时直接跳转，避免登录页闪现。`SessionManager` 只依赖 `Context`，在 `onCreate`
早期即可使用；而控件依赖 View 树，必须等 `setContentView` 之后才能访问。

### 2. "记住密码"的写入与清除

```kotlin
if (cbRemember.isChecked) {
    session.saveRemembered(username, password)
} else {
    session.clearRemembered()
}
```

取消勾选时会主动清除旧数据，避免"上次记住、这次不勾却仍在回填"的残留问题。

### 3. 注册页返回后自动回填

`LoginActivity.onResume()` 中重新调用 `fillRememberedAccount()`，因此从注册页返回时，
刚注册的账号会自动填入输入框。

---

## 九、已知限制

1. **登录未接入数据库**：`LoginActivity` 中当前为 `val userId = 1L`，只要账号密码非空即视为成功，
   并不校验密码是否正确。
2. **注册未落库**：`RegisterActivity` 只把账号写入 SharedPreferences，不写 `users` 表，
   因此重复用户名不会被拦截。
3. **密码明文存储**：`SharedPreferences` 与数据库中的密码均为明文，仅适合学习演示；
   正式项目应存储服务端签发的 token 或加盐哈希。
4. **`onUpgrade` 为删表重建**：升级版本号会清空全部本地数据。
5. **主页为模板页**：`MainActivity` 尚未实现业务内容。

---

## 十、如何运行

1. 使用 Android Studio 打开项目根目录（`File → Open`）
2. 等待 Gradle Sync 完成
3. 选择模拟器或真机，点击 Run

命令行构建（Windows）：

```bash
gradlew.bat assembleDebug
```

产物路径：`app/build/outputs/apk/debug/app-debug.apk`

> 若修改过布局中的控件 id，需执行一次 `Sync Project with Gradle Files`，
> 让 `R` 类重新生成后再运行。

---

## 十一、版本记录

| 版本 | 日期 | 说明 |
|---|---|---|
| 1.0 | 2026-11 | 完成登录、注册页面与本地会话管理；搭建 SQLite 基础设施 |

---

## 附：Markdown 使用提示

- Android Studio 内置 Markdown 预览：打开本文件后点击右上角 `Preview` 图标
- VS Code：`Ctrl + Shift + V` 打开预览
- 提交作业时若要 PDF：在预览页打印为 PDF，或用 Typora 导出
