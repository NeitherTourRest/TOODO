# AGENTS.md — TOODO 项目指引

> 让未来的 AI agent 快速上手，避免踩过的坑。

---

## 铁律

- **文档先行**：任何功能迭代都必须先修改 `DEVELOPMENT_DOC.md`，保持文档与代码同步。文档中的功能规格是代码的唯一真相来源。
- **从前端用户流程思考**：设计功能时必须从用户操作路径出发——这个按钮在哪、点了发生什么、数据出现在哪些页面。不要只改后端逻辑。
- **编译即真理**：不要依赖 IDE 的红色标记。`gradlew :app:assembleDebug` 通过才算数。

---

## 构建必知

### 环境要求
- Java 17、Gradle 9.3.1（项目自带 wrapper，运行 `gradlew` 即可）
- Android SDK 36（compileSdk），minSdk 24，targetSdk 36

### 关键版本锁（不可随意升降级）

| 组件 | 版本 | 原因 |
|------|------|------|
| AGP | 9.1.1 | compileSdk 用了 `release(36) { minorApiLevel = 1 }` 语法，AGP 8.x 不支持 |
| Hilt/Dagger | 2.59.2 | **必须 ≥ 2.59**，Hilt 2.51.1 不兼容 AGP 9，会报 `Android BaseExtension not found` |
| Kotlin | 2.1.0 | 配合 AGP 9 使用。AGP 9 **已内置** kotlin-android 插件，不要重复 apply |
| KSP | 2.1.0-1.0.29 | 必须版本号前缀匹配 Kotlin 版本 |
| Gradle | 9.3.1 | AGP 9.1.1 要求 Gradle 9.1+ |

### 插件陷阱

```kotlin
// ❌ 错误：AGP 9 已内置 kotlin-android，重复 apply 导致:
//    "Cannot add extension with name 'kotlin', as there is an extension already registered"
plugins {
    alias(libs.plugins.kotlin.android)  // ← 这行不能有
}

// ✅ 正确
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)   // Compose 编译器插件（仍需要）
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.serialization)
}
```

### 其他构建细节
- `build.gradle.kts` 中 **不能有** `kotlinOptions { jvmTarget = "17" }`，AGP 9 已移除该 block（Java 目标由 `compileOptions` 控制）
- `gradle.properties` **必须**有 `android.disallowKotlinSourceSets=false`（KSP + AGP 9 兼容性 workaround）
- `android.useAndroidX=true` 可删，AGP 9 默认开启
- **绝对不能**有 `enableJetifier=true`，AGP 9 与 Jetifier 冲突
- jvmTarget 已通过 `compileOptions { sourceCompatibility = JavaVersion.VERSION_17 }` 设置
- `compileSdk` 用的是 AGP 9 的新 DSL：`compileSdk { version = release(36) { minorApiLevel = 1 } }`，不要试图改成 `compileSdk = 36`

### 构建命令
```bash
# 快速检查编译（不生成 APK）
gradlew :app:compileDebugKotlin

# 完整 Debug APK
gradlew :app:assembleDebug

# Release + Lint 检查
gradlew :app:assembleRelease

# APK 输出位置
app/build/outputs/apk/debug/app-debug.apk
app/build/outputs/apk/release/app-release.apk
```

---

## 架构约定

### 分层（不可跨层调用）
```
UI (Screen + ViewModel) → Domain (UseCase + Repository接口) → Data (Room DAO + RepositoryImpl)
```

- Screen 只能调用 ViewModel 的方法，不能直接访问 Repository
- ViewModel 只能通过 UseCase 操作数据，不能直接调 DAO
- Repository 实现类隐藏所有 Room/DataStore 细节

### DI 注意事项
- Hilt 全局唯一 Component：`SingletonComponent`
- Worker 使用 `@HiltWorker` + `@AssistedInject`（不是 `@AndroidEntryPoint`）
- BroadcastReceiver 用 Hilt `@EntryPoint` 模式获取依赖（不能直接 `@AndroidEntryPoint`）
- Glance Widget Receiver 可以用 `@AndroidEntryPoint`（BroadcastReceiver 的子类）

### 导航
- 8 个路由（`Routes.kt`）：TodayRoute, PlanRoute, StatsRoute, SettingsRoute, AddTaskRoute(taskId?), TaskDetailRoute(taskId), ManageListsRoute, TaskListRoute(listId)
- 类型安全导航（基于 kotlinx.serialization）
- 底部导航栏 4 个 Tab：今日 → 计划 → 统计 → 设置

---

## 项目文件全景

### 核心入口
```
ToodoApplication.kt    — @HiltAndroidApp, 创建通知渠道, 实现 Configuration.Provider
MainActivity.kt        — @AndroidEntryPoint, setContent { ToodoTheme { ToodoNavHost() } }
```

### 数据层 (data/)
```
local/db/entity/       — 7个 Room Entity: Task, TaskList, RecurrenceRule, ReminderConfig,
                         SubTask, CompletionRecord, StreakFreeze
local/db/dao/          — 6个 DAO 接口 (注意：用 KSP 而非 kapt)
local/db/converter/    — TypeConverters (LocalDate↔Long 等)
local/datastore/       — PreferencesKeys + UserPreferences (DataStore 偏好)
mapper/                — EntityMappers.kt (Entity ↔ Domain Model 双向转换)
repository/            — TaskRepositoryImpl, TaskListRepositoryImpl, StatsRepositoryImpl
```

### 领域层 (domain/)
```
model/                 — 8个领域模型 + 3个枚举 (TaskType, Priority, Frequency)
repository/            — 3个 Repository 接口 (Task, TaskList, Stats)
usecase/               — 11个 UseCase
```
UseCase 完整列表：
- `GetTodayTasksUseCase` — 合并4个 Flow（一次性/过期/每日/循环）
- `CompleteTaskUseCase` — 完成任务 + 写入 CompletionRecord
- `UncompleteTaskUseCase` — 撤销完成
- `CreateTaskUseCase` — 新建任务 + 可选循环规则 + 提醒配置
- `UpdateTaskUseCase` — 更新任务属性
- `DeleteTaskUseCase` — 删除任务
- `CalculateNextOccurrenceUseCase` — 计算循环任务下次出现日期
- `ScheduleRemindersUseCase` — 调用 ReminderScheduler 设置闹钟
- `CancelRemindersUseCase` — 取消闹钟 + 删除提醒配置
- `GetDailyStatsUseCase` — 今日统计
- `GetStreakUseCase` — 打卡连续天数 + 冻结卡

### UI 层 (ui/)
```
theme/                 — 4个文件：Theme, Color, Type, Shape（铅笔手绘风）
navigation/            — Routes.kt + ToodoNavHost.kt
screen/
  today/               — TodayScreen (21KB) + TodayViewModel + TodayUiState
  addtask/             — AddTaskScreen (43KB) + AddTaskViewModel（新建/编辑复用）
  plan/                — PlanScreen (16KB) + PlanViewModel（未来7天 + 分类浏览）
  stats/               — StatsScreen (19KB) + StatsViewModel（热力图+打卡+平衡图）
  settings/            — SettingsScreen (19KB) + SettingsViewModel（偏好设置）
  taskdetail/          — TaskDetailScreen (9KB) + TaskDetailViewModel
  tasklist/            — TaskListScreen (6KB) + TaskListViewModel（某分类下的任务列表）
  managelists/         — ManageListsScreen (13KB) + ManageListsViewModel（分类CRUD）
component/             — 8个共享UI组件：TaskCard, PriorityBadge, RecurrenceChip,
                        CompletionAnimation, StreakBanner, HeatmapCalendar,
                        BalanceChart, EmptyState
util/                  — DateFormatUtil, HapticUtil
```

### 通知 + Widget
```
notification/ReminderScheduler.kt  — 单例，通过 AlarmManager 调度精确闹钟
receiver/AlarmReceiver.kt          — 闹钟触发 → 显示通知（Hilt @EntryPoint）
receiver/BootReceiver.kt           — 开机 → 重调度所有提醒
worker/DailySummaryWorker.kt       — @HiltWorker，24h 周期，每日早晨摘要通知
widget/ToodoWidget.kt              — Glance 桌面小组件（今日待办）
widget/ToodoWidgetReceiver.kt      — Widget 接收器
```

### DI 模块 (di/)
```
DatabaseModule.kt     — Room Database + DAO 提供
RepositoryModule.kt   — Repository 接口绑定到 Impl
WorkerModule.kt       — Worker 依赖（目前为空壳）
```

---

## 数据模型关键点

### Task 的3种类型
- `TaskType.ONE_TIME` — 一次性任务（有 dueDate，完成后不再出现）
- `TaskType.DAILY` — 每日任务（每天自动出现，完成写 CompletionRecord）
- `TaskType.RECURRING` — 自定义循环（WEEKLY/MONTHLY，完成写 CompletionRecord）

### 循环任务的"虚拟实例"策略
**不预创建未来的 Task 实例**。查询今日任务时动态判断：
1. 查今天的一次性任务 + 过期未完成任务
2. 查每日任务模板（今天没有 CompletionRecord 的）
3. 查循环任务模板（今天匹配循环规则且没有 CompletionRecord 的）
4. 4个 Flow 用 `combine` 合并，按聚焦 → 优先级 → sortOrder 排序

### CompletionRecord 的作用
- `(taskId, date)` 唯一约束，防止同一天重复完成同一任务
- 每日任务/循环任务的每个完成都是一个 Record
- 打卡统计数据从这里聚合查询
- 数据库中不单独存储 `completedCount/totalCount`，均为实时聚合

### 优先级色（铅笔手绘主题）
- HIGH → Red `#D32F2F`（彩铅红）
- MEDIUM → Orange `#ED8B16`（彩铅橙）
- LOW → Blue `#4A6FA5`（彩铅蓝）

---

## 主题：铅笔手绘风

### 不要做的事
- ❌ 不要启用 `dynamicColor`（Material You 会覆盖手绘调色板）
- ❌ 不要改回 `FontFamily.Default`（标题用 Cursive，正文用 Serif）
- ❌ 不要改回对称圆角（当前四角半径不同，模拟手绘不完美感）

### 颜色速查
| 场景 | 色值 |
|------|------|
| 亮色背景（素描纸） | `#FAF5EB` |
| 暗色背景（牛皮纸） | `#221E18` |
| 文字（石墨灰） | `#1E1B17` |
| 卡片背景 | `#F0E8D8` |
| 主色调（蓝铅笔） | `#4A6FA5` |
| 错误色（红笔） | `#C62828` |

---

## 图标

- 源文件：`D:\TOODO\.pic\TOODO.png`（32×32 像素）
- 已通过 Python/Pillow 生成全部 mipmap 密度版本
- Adaptive Icon 背景色为 `#FAF5EB`（素描纸色）
- 更换图标：替换 `.pic/TOODO.png`，重新运行 pillow 脚本生成所有密度

---

## AndroidManifest 需要知道的

- 已注册 `SCHEDULE_EXACT_ALARM`、`POST_NOTIFICATIONS`、`RECEIVE_BOOT_COMPLETED` 权限
- 有移除默认 `WorkManagerInitializer` 的 provider（`tools:node="remove"`），因为用了 on-demand 初始化
- AlarmReceiver 和 BootReceiver 已注册在 `receiver/` 包下
- ToodoWidgetReceiver 已注册在 `widget/` 包下

---

## 文档与代码同步规则

### 修改功能时的顺序
1. **先**更新 `DEVELOPMENT_DOC.md` 中对应的功能规格章节
2. 更新文档中的 UI 线框图（如有界面变化）
3. 然后修改代码
4. 运行 `gradlew :app:assembleDebug` 验证
5. 确认文档描述与实际行为一致

### 文档中的已知偏差（需要修复）
- `DEVELOPMENT_DOC.md` 第1240行：文档写 `hilt = "2.51.1"`，实际是 `2.59.2`
- 文档中主题色彩章节（5.3）仍是旧的紫蓝配色，应更新为铅笔手绘风配色
- 文档目录结构中 `worker/ShowNotificationWorker.kt` 不存在于实际代码（通知由 AlarmReceiver 直接发出）
- `worker/RescheduleRemindersWorker.kt` → 功能在 `ReminderScheduler.rescheduleAllReminders()` 中
- `service/ReminderScheduler.kt` → 实际路径是 `notification/ReminderScheduler.kt`
- `data/local/db/convertor/` → 实际是 `converter/`

---

## 用户操作流程（前端思维）

### 主界面（TodayScreen）
**所见**：日期标题 → 打卡横幅 → 聚焦任务区 → 今日任务列表 → 已完成列表（底部折叠）→ FAB

**可操作**：
- 点击任务卡片 → 弹出 BottomSheet 菜单（完成/取消/推迟/编辑/删除/复制）
- 点击 FAB → 跳转 AddTaskScreen
- 点击右上角搜索图标 → 搜索模式
- 长按任务 → 标记为聚焦（最多3个）
- 下拉刷新 → 重新加载今日任务

### 新建任务（AddTaskScreen）
**Tab切换**：一次性 → 每日 → 循环 → DDL

**每个类型显示的字段**：
- 一次性/DDL：截止日期 + 截止时间 + 提醒配置
- 每日：无需日期（每天自动出现）
- 循环：频率（每周/每月）+ 重复日选择 + 结束条件

**所有类型共有**：标题（必填）、优先级、分类、聚焦标记、子任务、备注

### 任务完成后的流转
1. CompleteTaskUseCase 写入 CompletionRecord
2. 一次性任务：设置 completedAt，不再出现在今日列表
3. 每日任务：写 CompletionRecord（taskId=模板ID, date=今天），今天不再显示，明天自动恢复
4. DDL 任务：取消所有闹钟提醒，标记完成
5. 统计页面实时更新（Flow 驱动）

### 任务在各页面的显示
| 页面 | 显示哪些任务 |
|------|------------|
| 今日 | 今日到期的 + 今日应出现的循环任务 |
| 计划 | 未来7天按日期分组的所有任务 |
| 任务列表 | 某个分类下的所有任务 |
| 任务详情 | 单个任务的完整信息 + 子任务 |
| Widget | 今日待办（最多5个） |
