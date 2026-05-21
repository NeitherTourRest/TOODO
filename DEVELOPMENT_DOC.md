# TOODO — Android 待办管理应用开发文档

> 版本: 1.0 | 日期: 2026-05-21 | 状态: 开发中

---

## 一、项目概述

### 1.1 基本信息

| 项目 | 值 |
|------|-----|
| 应用名称 | TOODO |
| 包名 | com.example.toodo |
| 最低SDK | 24 (Android 7.0) |
| 目标SDK | 36 (Android 16) |
| 开发语言 | Kotlin |
| UI框架 | Jetpack Compose + Material Design 3 |
| 架构模式 | MVVM + 单向数据流(UDF) |

### 1.2 设计理念：温和生产力 (Gentle Productivity)

**核心理念**：市面上的TODO应用要么功能臃肿（TickTick），要么价格高昂（Todoist $60/年），要么过于简陋（Google Tasks无循环任务）。TOODO定位在**简约而不简陋**——提供完成任务所需的一切功能，但不增加认知负担。

设计原则：
- **3秒法则**：从打开App到添加一个任务，不超过3秒
- **零内疚设计**：不使用红色逾期标记、不制造焦虑、允许"温柔地放下"
- **一触即达**：主要操作通过手势完成，减少点击层级
- **本地优先**：数据存储在设备本地，无需注册账号即可使用

### 1.3 目标用户

- 需要管理每日习惯和固定日程的学生
- 需要追踪DDL和项目进度的职场人士
- 厌倦了功能臃肿或订阅制TODO应用的用户
- 追求极简、高颜值工具的效率爱好者

---

## 二、功能规格

### 2.1 核心功能（MVP — 必须实现）

#### 2.1.1 今日待办主界面

**描述**：App启动后的默认页面，列出今天所有待办事项。

**显示规则**：
- 显示所有「今日每日任务」（recurrenceRule.freq = DAILY）
- 显示所有「今日循环任务」（recurrenceRule匹配今天星期几，如每周三、周五）
- 显示所有「今日到期的一次性任务」（dueDate = 今天）
- 显示所有「已过期但未完成的一次性任务」（dueDate < 今天 且 completedAt = null）
- 已完成的任务以划线+半透明样式显示在列表底部

**交互**：
- 点击任务 → 弹出底部操作菜单（BottomSheet）
- 左滑任务 → 快速完成（带满足动画）
- 右滑任务 → 快速推迟到明天
- 长按任务 → 进入拖拽排序模式
- 右下角FAB按钮 → 新建任务

**任务卡片信息**：
- 任务标题
- 优先级色点（红/橙/蓝）
- DDL时间（如有）
- 循环标记图标（如为循环任务）
- 分类色条（左侧边条）

#### 2.1.2 任务类型

**类型一：每日任务**
- 每天重复的任务（如"背50个单词"、"冥想10分钟"）
- 创建时设置 recurrenceRule.freq = DAILY
- 完成后当日标记完成，次日自动重新出现

**类型二：固定日期任务（自定义循环）**
- 用户自选星期几重复（如每周一、三、五）
- 支持多种循环模式：
  - 每周指定日（如每周一三五）
  - 每隔N天（如每3天）
  - 每月指定日（如每月1号和15号）
- 创建时设置 recurrenceRule.freq = WEEKLY/MONTHLY + byDay/byMonthDay
- 完成后下一个匹配日期自动重新出现

**类型三：一次性DDL任务**
- 用户指定截止日期和可选的截止时间
- 支持设置多级提醒模式（见2.1.3）
- 完成后不再出现（除非用户手动恢复）
- 过期未完成以灰色标记，不使用红色/警告色

#### 2.1.3 DDL提醒系统

**描述**：用户可为DDL任务设置多级提醒。

**提醒模式预设**：
| 预设名称 | 偏移量 |
|----------|--------|
| DDL当天 9:00 | 当天上午 |
| DDL前1小时 | 60分钟 |
| DDL前3小时 | 180分钟 |
| DDL前1天 | 1440分钟 |
| DDL前3天 | 4320分钟 |
| DDL前1周 | 10080分钟 |

**自定义提醒**：用户可输入任意偏移时间（如"DDL前2天6小时"）。

**一个任务可设置多个提醒**：例如同时设置"DDL前1周"和"DDL前1天"和"DDL前1小时"。

**通知行为**：
- 通知点击 → 打开App并跳转到该任务详情
- 通知中提供快捷操作按钮：「完成」「推迟1天」
- 已完成的任务不再触发提醒（提醒调度时检查任务完成状态）

#### 2.1.4 任务操作菜单

**触发方式**：点击任务卡片 → 弹出底部菜单（ModalBottomSheet）

**菜单选项**：
| 操作 | 说明 |
|------|------|
| ✓ 完成 | 标记任务完成（带完成动画+触觉反馈） |
| ✕ 取消完成 | 撤销完成状态 |
| → 推迟到明天 | 将dueDate延后一天（仅一次性任务） |
| ✎ 编辑 | 打开任务编辑界面 |
| 🗑 删除 | 删除任务（带确认对话框） |
| 📋 复制 | 复制为新任务 |

**循环任务完成逻辑**：
- 完成 → 标记当次完成，计算下一个匹配日期，创建新的Task实例
- 不影响其他日期的同一循环任务

#### 2.1.5 新建/编辑任务

**任务属性**：
| 属性 | 类型 | 必填 | 默认值 |
|------|------|------|--------|
| 标题 | String | 是 | — |
| 描述 | String | 否 | 空 |
| 任务类型 | Enum | 是 | 一次性 |
| 截止日期 | LocalDate | 一次性必填 | 今天 |
| 截止时间 | LocalTime? | 否 | null（全天任务） |
| 循环规则 | RecurrenceRule? | 循环任务必填 | null |
| 优先级 | Priority | 否 | 中 |
| 分类 | TaskList? | 否 | 默认分类 |
| 提醒配置 | List\<ReminderConfig\> | 否 | 空 |

**新建任务流程**：
1. 点击FAB → 弹出新建界面
2. 输入标题（自动聚焦键盘）
3. 选择任务类型（Tab切换：一次性 / 每日 / 循环 / DDL）
4. 根据类型显示对应的日期/循环设置
5. 可选设置优先级、分类、提醒
6. 保存

#### 2.1.6 任务分类

**描述**：用户可创建自定义分类来组织任务。

**预设分类**：
- 📚 学习
- 💼 工作
- 💪 健康
- 🏠 生活
- ✨ 个人成长

**操作**：
- 创建自定义分类（名称+图标+颜色）
- 编辑分类
- 删除空分类
- 任务可属于一个分类或不分类

### 2.2 扩展功能（实现优先级高）

#### 2.2.1 子任务

- 每个任务可添加子任务列表（最多2层深度）
- 父任务卡片显示子任务进度条（如 2/5）
- 子任务可直接在列表中勾选，无需打开详情
- 子任务全部完成时，提示是否标记父任务完成

#### 2.2.2 搜索

- 搜索框在顶部，支持实时搜索
- 搜索范围：标题+描述
- 搜索结果高亮匹配文字
- 按相关度排序

#### 2.2.3 统计页面

**每日完成率**：
- 今日总任务数 vs 已完成数
- 圆环进度图

**完成热力图**（类GitHub贡献图）：
- 过去365天的每日完成情况
- 颜色深浅代表完成任务数量
- 点击某天查看该天任务详情

**连续打卡天数**：
- 当前连续天数
- 最长连续天数
- 提供"冻结卡"机制：每月2张冻结卡，漏打卡不中断连续记录

**本周/本月统计**：
- 完成任务总数
- 各分类完成情况（柱状图）
- 最忙碌的星期几

#### 2.2.4 桌面小部件 (Widget)

**今日待办Widget**：
- 显示今日任务列表（2x2 或 4x2 尺寸）
- 可直接在Widget上勾选完成
- 点击任务 → 打开App详情
- 动态颜色（Material You）

**快速添加Widget**：
- 1x1按钮，点击直接打开新建任务界面

**打卡Widget**：
- 显示连续打卡天数
- 今日完成进度环

#### 2.2.5 设置页面

| 设置项 | 选项 |
|--------|------|
| 主题 | 跟随系统 / 浅色 / 深色 |
| 每日重置时间 | 默认 00:00，可自定义（如 04:00 或 06:00） |
| 默认提醒 | 新建DDL任务时的默认提醒配置 |
| 通知设置 | 开关、声音、震动 |
| 每周起始日 | 周一 / 周日 |
| 数据导出 | 导出为JSON/CSV |
| 数据导入 | 从JSON导入 |
| 关于 | 版本信息 |

### 2.3 创新功能（TOODO差异化）

#### 2.3.1 三任务聚焦模式 (Focus 3)

**理念**：灵感来源于"每天只做3件最重要的事"的时间管理哲学。

**机制**：
- 每天从待办列表中选择最多3个任务标记为"聚焦任务"（⭐标记）
- 聚焦任务在列表顶部以醒目样式显示
- 统计页面单独追踪聚焦任务的完成率
- 不强制限制总任务数，但通过聚焦引导用户优先处理重要事项

**用户体验**：
- 长按任务 → "标记为聚焦"
- 聚焦任务满3个时，新标记会提示替换哪个
- 聚焦任务完成时，播放特殊完成动画+强触觉反馈

#### 2.3.2 温和打卡系统 (Gentle Streak)

**理念**：不同于Duolingo的焦虑式打卡，TOODO的打卡是鼓励而非惩罚。

**机制**：
- "完成日"定义：完成至少1个任务（而非全部任务）即算打卡
- 连续天数可视化：火焰图标+天数，但不使用红色警告
- 冻结卡：每月2张免费冻结卡，漏打卡可使用冻结卡保持连续
- 里程碑庆祝：7天、30天、100天、365天，播放庆祝动画
- 不显示"已断开X天"等负面提示

#### 2.3.3 生活平衡仪表盘 (Life Balance)

**理念**：追踪"你在生活的哪些方面投入了时间"，避免"高效但不幸福"。

**机制**：
- 每个任务分类对应一个生活领域
- 仪表盘以雷达图/饼图展示过去7天/30天各分类的任务完成比例
- 当某个分类连续7天无完成任务时，以温和提示"本周还没有关注XXX哦~"（非警告）
- 推荐"平衡建议"：基于历史数据建议补充被忽视的领域

---

## 三、技术架构

### 3.1 技术栈

| 层 | 技术 | 版本 |
|----|------|------|
| 语言 | Kotlin | 2.1.0 |
| UI | Jetpack Compose + Material 3 | BOM 2025.05.00 |
| 架构 | MVVM + UDF | — |
| 依赖注入 | Hilt | 2.51.1 |
| 本地数据库 | Room + KSP | 2.7.1 |
| 异步 | Kotlin Coroutines + Flow | — |
| 导航 | Navigation Compose (类型安全) | 2.9.0 |
| 后台调度 | WorkManager | 2.10.0 |
| 精确闹钟 | AlarmManager | — |
| 偏好存储 | DataStore Preferences | 1.1.4 |
| 日期时间 | java.time (API 26+, desugar for 24+) | — |

### 3.2 架构图

```
┌─────────────────────────────────────────────┐
│                   UI Layer                   │
│  ┌─────────┐ ┌──────────┐ ┌──────────────┐  │
│  │ Screen  │ │ Screen   │ │   Screen     │  │
│  │(Compose)│ │(Compose) │ │  (Compose)   │  │
│  └────┬────┘ └────┬─────┘ └──────┬───────┘  │
│       │           │              │           │
│  ┌────▼───────────▼──────────────▼───────┐   │
│  │            ViewModel                  │   │
│  │  (StateFlow<UiState> + Intent处理)    │   │
│  └────────────────┬──────────────────────┘   │
│                   │                           │
├───────────────────┼───────────────────────────┤
│               Domain Layer                    │
│  ┌────────────────▼──────────────────────┐    │
│  │           Repository                  │    │
│  │  (单数据源入口, 业务逻辑封装)          │    │
│  └────────────────┬──────────────────────┘    │
│                   │                           │
├───────────────────┼───────────────────────────┤
│               Data Layer                      │
│  ┌────────┐  ┌──────────┐  ┌──────────────┐  │
│  │  Room  │  │ DataStore│  │ AlarmManager │  │
│  │  DAO   │  │ Prefs    │  │ + WorkManager│  │
│  └────────┘  └──────────┘  └──────────────┘  │
└─────────────────────────────────────────────┘
```

### 3.3 项目目录结构

```
com.example.toodo/
├── ToodoApplication.kt              # @HiltAndroidApp Application类
├── MainActivity.kt                  # 唯一Activity, setContent Compose
│
├── data/
│   ├── local/
│   │   ├── db/
│   │   │   ├── ToodoDatabase.kt     # RoomDatabase
│   │   │   ├── converter/
│   │   │   │   └── Converters.kt    # TypeConverters
│   │   │   ├── dao/
│   │   │   │   ├── TaskDao.kt       # 任务CRUD + Flow查询
│   │   │   │   ├── TaskListDao.kt   # 分类CRUD
│   │   │   │   ├── RecurrenceRuleDao.kt
│   │   │   │   ├── ReminderConfigDao.kt
│   │   │   │   └── CompletionRecordDao.kt  # 每日完成记录
│   │   │   └── entity/
│   │   │       ├── TaskEntity.kt
│   │   │       ├── TaskListEntity.kt
│   │   │       ├── RecurrenceRuleEntity.kt
│   │   │       ├── ReminderConfigEntity.kt
│   │   │       └── CompletionRecordEntity.kt
│   │   └── datastore/
│   │       └── UserPreferences.kt   # DataStore偏好封装
│   ├── repository/
│   │   ├── TaskRepositoryImpl.kt
│   │   ├── TaskListRepositoryImpl.kt
│   │   └── StatsRepositoryImpl.kt
│   └── mapper/
│       └── EntityMappers.kt         # Entity ↔ Domain Model 转换
│
├── domain/
│   ├── model/
│   │   ├── Task.kt                  # 领域模型
│   │   ├── TaskList.kt
│   │   ├── RecurrenceRule.kt
│   │   ├── ReminderConfig.kt
│   │   ├── Priority.kt              # 枚举
│   │   ├── TaskType.kt              # 枚举
│   │   └── CompletionRecord.kt
│   ├── repository/
│   │   ├── TaskRepository.kt        # 接口
│   │   ├── TaskListRepository.kt    # 接口
│   │   └── StatsRepository.kt       # 接口
│   └── usecase/
│       ├── GetTodayTasksUseCase.kt
│       ├── CompleteTaskUseCase.kt
│       ├── CreateTaskUseCase.kt
│       ├── UpdateTaskUseCase.kt
│       ├── DeleteTaskUseCase.kt
│       ├── CalculateNextOccurrenceUseCase.kt
│       ├── ScheduleRemindersUseCase.kt
│       ├── CancelRemindersUseCase.kt
│       ├── GetDailyStatsUseCase.kt
│       └── GetStreakUseCase.kt
│
├── ui/
│   ├── navigation/
│   │   └── ToodoNavHost.kt          # NavHost + 路由定义
│   ├── theme/
│   │   ├── Theme.kt                 # Material 3 主题 + Dynamic Color
│   │   ├── Color.kt
│   │   ├── Type.kt
│   │   └── Shape.kt
│   ├── component/
│   │   ├── TaskCard.kt              # 任务卡片组件
│   │   ├── PriorityBadge.kt         # 优先级标记
│   │   ├── RecurrenceChip.kt        # 循环规则标签
│   │   ├── CompletionAnimation.kt   # 完成动画
│   │   ├── StreakBanner.kt          # 打卡横幅
│   │   ├── HeatmapCalendar.kt       # 热力图日历
│   │   ├── BalanceChart.kt          # 生活平衡图
│   │   └── EmptyState.kt            # 空状态占位
│   ├── screen/
│   │   ├── today/
│   │   │   ├── TodayScreen.kt       # 今日待办主界面
│   │   │   └── TodayViewModel.kt
│   │   ├── addtask/
│   │   │   ├── AddTaskScreen.kt     # 新建/编辑任务
│   │   │   └── AddTaskViewModel.kt
│   │   ├── taskdetail/
│   │   │   ├── TaskDetailScreen.kt  # 任务详情
│   │   │   └── TaskDetailViewModel.kt
│   │   ├── tasklist/
│   │   │   ├── TaskListScreen.kt    # 分类任务列表
│   │   │   └── TaskListViewModel.kt
│   │   ├── stats/
│   │   │   ├── StatsScreen.kt       # 统计页面
│   │   │   └── StatsViewModel.kt
│   │   ├── settings/
│   │   │   ├── SettingsScreen.kt    # 设置页面
│   │   │   └── SettingsViewModel.kt
│   │   └── managelists/
│   │       ├── ManageListsScreen.kt # 管理分类
│   │       └── ManageListsViewModel.kt
│   └── util/
│       ├── DateFormatUtil.kt
│       └── HapticUtil.kt
│
├── worker/
│   ├── ShowNotificationWorker.kt    # 显示通知
│   └── RescheduleRemindersWorker.kt # 重启后重调度提醒
│
├── receiver/
│   ├── AlarmReceiver.kt             # 闹钟触发接收器
│   └── BootReceiver.kt              # 开机重调度
│
├── service/
│   └── ReminderScheduler.kt         # 提醒调度服务
│
└── di/
    ├── DatabaseModule.kt            # Room + DAO 提供
    ├── RepositoryModule.kt          # Repository 绑定
    └── WorkerModule.kt              # Worker 依赖
```

---

## 四、数据模型

### 4.1 ER关系图

```
┌──────────────┐     ┌───────────────────┐     ┌────────────────────┐
│  TaskList    │     │      Task         │     │  RecurrenceRule    │
│──────────────│     │───────────────────│     │────────────────────│
│ id (PK)      │◄──┐ │ id (PK)           │ ┌──►│ id (PK)            │
│ name         │   │ │ title             │ │   │ freq               │
│ iconName     │   └─│ listId (FK)       │ │   │ interval           │
│ color        │     │ description       │ │   │ byDay              │
│ sortOrder    │     │ priority          │ │   │ byMonthDay         │
└──────────────┘     │ taskType          │ │   │ count              │
                     │ dueDate           │ │   │ untilDate          │
                     │ dueTime           │ │   └────────────────────┘
                     │ recurrenceRuleId  │─┘
                     │ isFocusTask       │     ┌────────────────────┐
                     │ isArchived        │     │  ReminderConfig    │
                     │ createdAt         │     │────────────────────│
                     │ completedAt       │     │ id (PK)            │
                     │ sortOrder         │     │ taskId (FK)        │──┐
                     └───────┬───────────┘     │ offsetMinutes      │  │
                             │                 │ isEnabled          │  │
                             │                 └────────────────────┘  │
                             │                                         │
                     ┌───────▼───────────┐                             │
                     │  SubTask          │                             │
                     │───────────────────│                             │
                     │ id (PK)           │                             │
                     │ parentId (FK→Task)│                             │
                     │ title             │                             │
                     │ isCompleted       │                             │
                     │ sortOrder         │                             │
                     └───────────────────┘                             │
                                                                       │
                     ┌───────────────────┐                             │
                     │ CompletionRecord  │                             │
                     │───────────────────│                             │
                     │ id (PK)           │                             │
                     │ date              │                             │
                     │ completedCount    │                             │
                     │ totalCount        │                             │
                     │ focusCompletedCount│                            │
                     └───────────────────┘                             │
                                                                       │
                     ┌───────────────────┐                             │
                     │ StreakFreeze      │                             │
                     │───────────────────│                             │
                     │ id (PK)           │                             │
                     │ date              │                             │
                     └───────────────────┘                             │
```

### 4.2 实体详细定义

#### TaskEntity

```kotlin
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(entity = TaskListEntity::class, parentColumns = ["id"],
            childColumns = ["listId"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = RecurrenceRuleEntity::class, parentColumns = ["id"],
            childColumns = ["recurrenceRuleId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("listId"), Index("recurrenceRuleId"), Index("dueDate"), Index("completedAt")]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val taskType: String = "ONE_TIME",          // "ONE_TIME", "DAILY", "RECURRING"
    val priority: Int = 2,                       // 1=高, 2=中, 3=低
    val listId: Long? = null,
    val dueDate: Long? = null,                   // epoch millis (日期部分)
    val dueTime: Int? = null,                    // 分钟数从0:00起 (如 540 = 9:00)
    val recurrenceRuleId: Long? = null,
    val isFocusTask: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val sortOrder: Int = 0
)
```

#### RecurrenceRuleEntity

```kotlin
@Entity(tableName = "recurrence_rules")
data class RecurrenceRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val freq: String,                            // "DAILY", "WEEKLY", "MONTHLY"
    val interval: Int = 1,                       // 每N天/周/月
    val byDay: String? = null,                   // "MON,WED,FRI" (WEEKLY时使用)
    val byMonthDay: String? = null,              // "1,15" (MONTHLY时使用)
    val count: Int? = null,                      // 最大重复次数 (null=无限)
    val untilDate: Long? = null                  // 结束日期 (null=无限)
)
```

#### ReminderConfigEntity

```kotlin
@Entity(
    tableName = "reminder_configs",
    foreignKeys = [
        ForeignKey(entity = TaskEntity::class, parentColumns = ["id"],
            childColumns = ["taskId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("taskId")]
)
data class ReminderConfigEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val offsetMinutes: Long,                     // DDL前多少分钟 (如 10080=1周)
    val isEnabled: Boolean = true,
    val lastTriggeredAt: Long? = null
)
```

#### TaskListEntity

```kotlin
@Entity(tableName = "task_lists")
data class TaskListEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconName: String = "folder",             // Material Icon name
    val color: Long = 0xFF6750A4,               // ARGB color
    val sortOrder: Int = 0
)
```

#### SubTaskEntity

```kotlin
@Entity(
    tableName = "sub_tasks",
    foreignKeys = [
        ForeignKey(entity = TaskEntity::class, parentColumns = ["id"],
            childColumns = ["parentId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("parentId")]
)
data class SubTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val parentId: Long,
    val title: String,
    val isCompleted: Boolean = false,
    val sortOrder: Int = 0
)
```

#### CompletionRecordEntity

```kotlin
@Entity(
    tableName = "completion_records",
    indices = [
        Index(value = ["taskId", "date"], unique = true),
        Index(value = ["date"])
    ]
)
data class CompletionRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,                            // 完成的任务ID（模板ID或一次性任务ID）
    val date: Long,                              // 完成日期 (epoch day = LocalDate.toEpochDay())
    val completedAt: Long,                       // 完成时间戳 (epoch millis)
    val isFocusTask: Boolean = false             // 完成时是否为聚焦任务
)
```

> 注：每日统计（completedCount, totalCount）从CompletionRecord聚合查询得出，不单独存储。

#### StreakFreezeEntity

```kotlin
@Entity(tableName = "streak_freezes", indices = [Index(value = ["date"], unique = true)])
data class StreakFreezeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long                               // 使用冻结卡的日期
)
```

### 4.3 领域模型

```kotlin
// domain/model/Task.kt
data class Task(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val taskType: TaskType = TaskType.ONE_TIME,
    val priority: Priority = Priority.MEDIUM,
    val list: TaskList? = null,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val recurrenceRule: RecurrenceRule? = null,
    val reminders: List<ReminderConfig> = emptyList(),
    val subTasks: List<SubTask> = emptyList(),
    val isFocusTask: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Instant = Instant.now(),
    val completedAt: Instant? = null,
    val sortOrder: Int = 0
) {
    val isCompleted: Boolean get() = completedAt != null
    val isOverdue: Boolean get() = dueDate != null && !isCompleted && dueDate < LocalDate.now()
    val subTaskProgress: Float get() = if (subTasks.isEmpty()) -1f
        else subTasks.count { it.isCompleted }.toFloat() / subTasks.size
}

enum class TaskType { ONE_TIME, DAILY, RECURRING }
enum class Priority(val value: Int) { HIGH(1), MEDIUM(2), LOW(3) }

data class SubTask(val id: Long = 0, val parentId: Long, val title: String,
    val isCompleted: Boolean = false, val sortOrder: Int = 0)

data class RecurrenceRule(val id: Long = 0, val freq: Frequency, val interval: Int = 1,
    val byDay: Set<DayOfWeek> = emptySet(), val byMonthDay: Set<Int> = emptySet(),
    val count: Int? = null, val untilDate: LocalDate? = null)

enum class Frequency { DAILY, WEEKLY, MONTHLY }

data class ReminderConfig(val id: Long = 0, val taskId: Long = 0,
    val offsetMinutes: Long, val isEnabled: Boolean = true)

data class TaskList(val id: Long = 0, val name: String, val iconName: String = "folder",
    val color: Long = 0xFF6750A4, val sortOrder: Int = 0)
```

---

## 五、UI/UX设计规格

### 5.1 导航结构

底部导航栏（NavigationBar）包含4个Tab：

```
┌─────────────────────────────────────┐
│          顶部标题栏                   │
├─────────────────────────────────────┤
│                                     │
│           页面内容区                  │
│                                     │
│                                     │
├──────┬──────┬──────┬────────────────┤
│ 今日 │ 计划 │ 统计 │ 设置           │
└──────┴──────┴──────┴────────────────┘
```

| Tab | 图标 | 页面 | 说明 |
|-----|------|------|------|
| 今日 | 日历图标 | TodayScreen | 今日所有待办 |
| 计划 | 列表图标 | PlanScreen | 未来7天任务总览+分类浏览 |
| 统计 | 柱状图图标 | StatsScreen | 热力图+完成率+打卡+生活平衡 |
| 设置 | 齿轮图标 | SettingsScreen | 偏好设置 |

### 5.2 页面详细设计

#### 5.2.1 今日页面 (TodayScreen)

```
┌─────────────────────────────────────┐
│  5月21日 周三          🔍  ⋮        │ ← 顶部栏：日期+搜索+菜单
├─────────────────────────────────────┤
│  🔥 连续打卡 12 天    冻结卡: 2/2   │ ← 打卡横幅（可折叠）
├─────────────────────────────────────┤
│  ⭐ 聚焦任务                        │ ← 聚焦任务区（最多3个）
│  ┌────────────────────────────────┐ │
│  │● 完成项目报告（红点=高优先级）  │ │
│  │  📋 3/5 子任务  📅 今天 18:00  │ │
│  └────────────────────────────────┘ │
│  ┌────────────────────────────────┐ │
│  │● 准备周会演示                   │ │
│  │  📅 今天 14:00  🔔 DDL前1h    │ │
│  └────────────────────────────────┘ │
├─────────────────────────────────────┤
│  📋 今日任务 (3/8)                  │ ← 普通任务区
│  ┌────────────────────────────────┐ │
│  │  背50个单词（循环🔄）           │ │ ← 左侧分类色条
│  │  每日任务 · 学习                │ │
│  └────────────────────────────────┘ │
│  ┌────────────────────────────────┐ │
│  │  健身30分钟（循环🔄）           │ │
│  │  每周三五 · 健康                │ │
│  └────────────────────────────────┘ │
│  ┌────────────────────────────────┐ │
│  │  提交实验报告                   │ │
│  │  📅 今天 23:59  🔔 DDL前1天   │ │
│  └────────────────────────────────┘ │
├─────────────────────────────────────┤
│  ✓ 已完成 (2)                       │ ← 已完成区（折叠）
│  ┌────────────────────────────────┐ │
│  │  买菜（划线+半透明）            │ │
│  └────────────────────────────────┘ │
├─────────────────────────────────────┤
│                           ＋        │ ← FAB按钮
└─────────────────────────────────────┘
```

#### 5.2.2 新建任务页面 (AddTaskScreen)

```
┌─────────────────────────────────────┐
│  ← 新建任务              保存       │
├─────────────────────────────────────┤
│  ┌─────────────────────────────────┐│
│  │ 输入任务标题...                  ││ ← 自动聚焦
│  └─────────────────────────────────┘│
│                                     │
│  任务类型                            │
│  ┌──────┬──────┬──────┬──────┐      │
│  │ 一次 │ 每日 │ 循环 │ DDL  │      │ ← Tab选择
│  └──────┴──────┴──────┴──────┘      │
│                                     │
│  截止日期     5月28日 ▾             │ ← 日期选择器
│  截止时间     不设置 ▾              │ ← 时间选择器
│                                     │
│  循环设置（循环类型时显示）           │
│  ┌─────────────────────────────────┐│
│  │ 频率: 每周 ▾                    ││
│  │ 重复日: ☑一 ☑三 ☐五 ☐日       ││
│  │ 结束: 从不 ▾                    ││
│  └─────────────────────────────────┘│
│                                     │
│  提醒（DDL类型时显示）               │
│  ┌─────────────────────────────────┐│
│  │ ☑ DDL前1天    ☑ DDL前1小时     ││
│  │ ☐ DDL前1周    ＋ 自定义提醒     ││
│  └─────────────────────────────────┘│
│                                     │
│  优先级  ○高  ●中  ○低              │
│  分类    学习 ▾                      │
│  标记聚焦 ⭐                         │
│                                     │
│  子任务                              │
│  ┌─────────────────────────────────┐│
│  │ ☐ 收集数据                      ││
│  │ ☐ 分析结果                      ││
│  │ ＋ 添加子任务                    ││
│  └─────────────────────────────────┘│
│                                     │
│  备注                                │
│  ┌─────────────────────────────────┐│
│  │ 输入备注...                      ││
│  └─────────────────────────────────┘│
└─────────────────────────────────────┘
```

#### 5.2.3 统计页面 (StatsScreen)

```
┌─────────────────────────────────────┐
│  统计                               │
├─────────────────────────────────────┤
│  🔥 12天连续打卡  最长: 28天        │
│  ┌─────────────────────────────────┐│
│  │   今日 3/8 完成                 ││
│  │   ╭───────╮                     ││ ← 圆环进度
│  │   │ 37.5% │                     ││
│  │   ╰───────╯                     ││
│  └─────────────────────────────────┘│
├─────────────────────────────────────┤
│  完成热力图                          │
│  ┌─────────────────────────────────┐│
│  │ ░░▓▓██░░▓▓░░▓▓██▓▓░░░░▓▓██   ││ ← 365天热力图
│  │ ░░▓▓░░▓▓██░░▓▓░░░░▓▓██▓▓░░   ││
│  └─────────────────────────────────┘│
├─────────────────────────────────────┤
│  本周概览                            │
│  ┌─────────────────────────────────┐│
│  │ 一 二 三 四 五 六 日             ││
│  │ ▅ ▅ ▅ ▃ ▅ ▂ ▃                 ││ ← 柱状图
│  └─────────────────────────────────┘│
├─────────────────────────────────────┤
│  生活平衡                            │
│  ┌─────────────────────────────────┐│
│  │        学习                      ││
│  │      ╱    ╲                      ││ ← 雷达图/饼图
│  │  工作──    ──健康                ││
│  │      ╲    ╱                      ││
│  │        生活                      ││
│  └─────────────────────────────────┘│
└─────────────────────────────────────┘
```

### 5.3 主题与色彩

**Material 3 + Dynamic Color**：

- Android 12+ (API 31+) 使用动态颜色（从壁纸提取）
- Android 12以下使用自定义配色方案

**自定义配色（浅色）**：
| 角色 | 颜色 | 用途 |
|------|------|------|
| Primary | #5B5BD6 | FAB、主要按钮、底部导航选中 |
| On Primary | #FFFFFF | Primary上的文字 |
| Primary Container | #E3DFFF | 卡片背景、选中态背景 |
| Secondary | #6B5F1E | 次要强调 |
| Tertiary | #7B5757 | 警告/过期提示 |
| Surface | #FEFBFF | 页面背景 |
| Surface Container | #F2EDF9 | 卡片背景 |

**自定义配色（深色）**：
| 角色 | 颜色 | 用途 |
|------|------|------|
| Primary | #C4C0FF | FAB、主要按钮 |
| On Primary | #1B1B6B | Primary上的文字 |
| Surface | #1B1B1F | 页面背景 |
| Surface Container | #26242C | 卡片背景 |

**优先级颜色**：
| 优先级 | 颜色 | 色值 |
|--------|------|------|
| 高 | 红色 | #FF5252 |
| 中 | 橙色 | #FFB74D |
| 低 | 蓝色 | #42A5F5 |

### 5.4 完成动画

当用户勾选完成一个任务时：
1. 勾选框变为绿色对勾 ✓
2. 任务标题文字从左到右划线
3. 文字渐变为半透明
4. 触觉反馈（短震动 HapticFeedbackConstants.CONFIRM）
5. 聚焦任务额外播放 confetti 粒子动画（简单的星星散落效果）
6. 任务卡片向下滑动到已完成区域

---

## 六、通知系统设计

### 6.1 调度架构

```
任务创建/更新
    │
    ▼
ScheduleRemindersUseCase
    │
    ├─ 计算每个ReminderConfig的触发时间
    │  triggerTime = dueDate + dueTime - offsetMinutes
    │
    ├─ 过滤掉已过时间的提醒
    │
    └─ 对每个有效提醒调用 ReminderScheduler
           │
           ▼
       AlarmManager.setExactAndAllowWhileIdle()
           │
           ▼ （闹钟触发时）
       AlarmReceiver.onReceive()
           │
           ▼
       WorkManager.enqueue(ShowNotificationWorker)
           │
           ▼
       检查任务是否已完成
           │
           ├─ 已完成 → 不通知，Result.success()
           └─ 未完成 → 构建通知，notificationManager.notify()
```

### 6.2 关键实现细节

**权限要求**：
- `SCHEDULE_EXACT_ALARM`（API 31+，用于精确闹钟）
- `POST_NOTIFICATIONS`（API 33+，用于发送通知）
- `RECEIVE_BOOT_COMPLETED`（用于开机重调度）
- `USE_EXACT_ALARM`（API 33+，日历/闹钟类App可声明此权限免审核）

**开机重调度**：
- BootReceiver 接收 BOOT_COMPLETED
- 启动 RescheduleRemindersWorker
- Worker 查询所有未完成任务及其提醒配置
- 重新调用 ReminderScheduler 调度所有提醒

**任务完成时取消提醒**：
- CompleteTaskUseCase 调用 CancelRemindersUseCase
- CancelRemindersUseCase 通过 ReminderScheduler 取消该任务的所有 AlarmManager PendingIntent

**循环任务提醒**：
- 循环任务的每个实例视为独立Task
- 完成当次后，新建下个实例的Task并调度提醒

### 6.3 通知渠道

| 渠道ID | 名称 | 重要性 | 说明 |
|--------|------|--------|------|
| task_reminder | 任务提醒 | HIGH | DDL提醒，带声音和弹出 |
| daily_summary | 每日概览 | DEFAULT | 每日早晨待办摘要 |
| streak_reminder | 打卡提醒 | LOW | 即将断打卡时提醒 |

---

## 七、循环任务实现方案

### 7.1 核心逻辑

**今日任务判断算法**：

```
getTodayTasks():
  1. 查询所有 taskType = "ONE_TIME" 且 dueDate = today 且 completedAt = null 的任务
  2. 查询所有 taskType = "ONE_TIME" 且 dueDate < today 且 completedAt = null 的任务（过期任务）
  3. 查询所有 taskType = "DAILY" 的循环规则，生成今日实例
  4. 查询所有 taskType = "RECURRING" 的循环规则，判断今日是否匹配
  5. 返回合并结果
```

**循环匹配算法**：

```
matchesDate(rule: RecurrenceRule, date: LocalDate): Boolean
  when(rule.freq):
    DAILY → true (如果interval=1)，否则 date.dayOfYear % interval == 0
    WEEKLY → date.dayOfWeek in rule.byDay
    MONTHLY → date.dayOfMonth in rule.byMonthDay
```

### 7.2 循环任务的"实例化"策略

**方案：虚拟实例 + 完成记录**

不预创建循环任务的所有未来实例。而是：
1. 循环任务在数据库中只有一条Task记录（模板）
2. 每日打开App时，根据循环规则判断今日是否需要显示
3. 完成时，创建一条 CompletionRecord（记录哪个模板在哪个日期完成了）
4. 查询今日任务时，排除今日已有CompletionRecord的循环任务

**优点**：
- 数据库不会膨胀
- 修改循环规则不影响历史完成记录
- 简单直观

**CompletionRecord扩展**：已在4.2节中定义，字段为 `taskId + date + completedAt + isFocusTask`，唯一约束为 `(taskId, date)` 防止同一天重复完成同一循环任务。

### 7.3 今日待办查询逻辑（详细）

```kotlin
// TaskDao.kt 中的核心查询

// 获取今日所有待显示的任务
fun getTodayTasks(todayEpochDay: Long, todayDayOfWeek: Int): Flow<List<TaskWithDetails>> {

    // 1. 一次性任务：dueDate = today 且未完成
    // 2. 一次性任务：dueDate < today 且未完成（过期）
    // 3. 每日任务：今日无完成记录
    // 4. 循环任务：匹配今日星期几 且今日无完成记录
    // 5. 排除已归档的

    return combine(
        getOneTimeTodayTasks(todayEpochDay),
        getOneTimeOverdueTasks(todayEpochDay),
        getDailyTasksWithoutCompletion(todayEpochDay),
        getRecurringTasksWithoutCompletion(todayEpochDay, todayDayOfWeek)
    ) { oneTime, overdue, daily, recurring ->
        (oneTime + overdue + daily + recurring).sortedWith(
            compareByDescending<TaskWithDetails> { it.task.isFocusTask }
                .thenBy { it.task.priority }
                .thenBy { it.task.sortOrder }
        )
    }
}
```

---

## 八、打卡与统计实现方案

### 8.1 打卡连续天数计算

```kotlin
fun calculateStreak(records: List<CompletionRecordEntity>, freezes: Set<Long>): Int {
    var streak = 0
    var date = LocalDate.now().toEpochDay()

    // 如果今天还没有完成任何任务，从昨天开始算
    if (date !in records.map { it.date }.toSet()) {
        if (date !in freezes) {
            date -= 1 // 昨天没有完成也没有冻结，streak从0开始
        }
    }

    while (true) {
        val hasCompletion = records.any { it.date == date }
        val hasFreeze = date in freezes

        if (hasCompletion || hasFreeze) {
            streak++
            date -= 1
        } else {
            break
        }
    }
    return streak
}
```

### 8.2 冻结卡机制

- 每月1号重置，获得2张冻结卡
- 用户可在统计页面手动使用冻结卡（为过去的某天补充）
- 每月最多2张，不可累积到下月
- 使用冻结卡不会增加完成计数，只保护连续天数

### 8.3 完成热力图数据

```kotlin
// 获取过去365天的完成数据
fun getHeatmapData(): Flow<Map<Long, Int>> {
    val startDate = LocalDate.now().minusDays(364).toEpochDay()
    return completionRecordDao.getCompletionCountsBetween(startDate, LocalDate.now().toEpochDay())
        .map { records -> records.associate { it.date to it.completedCount } }
}
```

热力图颜色级别：
- 0个完成：极浅灰（几乎透明）
- 1-2个：浅绿
- 3-5个：中绿
- 6+个：深绿

---

## 九、桌面小部件实现方案

### 9.1 今日待办Widget

使用 Jetpack Glance 开发：

**布局**：
- 标题："今日待办 3/8"
- 任务列表：最多显示5个，每个一行（勾选框 + 标题 + 优先级色点）
- 底部："查看全部" 链接

**更新策略**：
- 使用 WorkManager 每30分钟更新一次
- 任务完成时发送广播触发即时更新
- 支持动态颜色 (Material You)

### 9.2 快速添加Widget

- 1x1按钮，显示 TOODO logo
- 点击 → 打开App并直接进入新建任务界面
- 使用 `Intent` flag 跳过主界面

---

## 十、数据导入导出

### 10.1 导出格式 (JSON)

```json
{
  "version": 1,
  "exportDate": "2026-05-21T10:00:00Z",
  "tasks": [...],
  "taskLists": [...],
  "recurrenceRules": [...],
  "completionRecords": [...]
}
```

### 10.2 导入逻辑

- 选择JSON文件 → 解析 → 冲突检测（按ID）→ 合并/覆盖（用户选择）→ 写入数据库

---

## 十一、实现计划

### Phase 1: 基础框架搭建

**目标**：项目可编译运行，显示空白主界面。

1. 配置 Kotlin + Compose + Hilt + Room + Navigation
2. 创建 ToodoApplication + MainActivity
3. 搭建主题系统（Theme.kt, Color.kt, Type.kt）
4. 搭建底部导航 + NavHost
5. 创建空白的4个Tab页面骨架

### Phase 2: 数据层实现

**目标**：所有数据库表和DAO可用。

1. 创建所有 Entity 类
2. 创建 TypeConverters
3. 创建所有 DAO 接口 + 查询方法
4. 创建 ToodoDatabase
5. 创建 EntityMappers（Entity ↔ Domain）
6. 创建 Repository 接口和实现
7. 创建 Hilt DI Module
8. 插入预设分类数据（Database Callback）

### Phase 3: 核心功能 — 今日待办

**目标**：今日页面可显示任务列表、可完成/取消完成。

1. 实现 TaskDao 的今日任务查询逻辑
2. 实现 GetTodayTasksUseCase
3. 实现 CompleteTaskUseCase（含CompletionRecord写入）
4. 实现 TodayViewModel + UiState
5. 实现 TodayScreen UI（TaskCard, 列表, 已完成区域）
6. 实现任务操作菜单（BottomSheet）
7. 实现完成动画

### Phase 4: 核心功能 — 新建/编辑任务

**目标**：可创建三种类型的任务。

1. 实现 CreateTaskUseCase + UpdateTaskUseCase
2. 实现 AddTaskViewModel
3. 实现 AddTaskScreen UI（类型选择Tab、日期/时间选择、循环设置、提醒配置、优先级、分类）
4. 实现循环规则UI（星期选择器、间隔设置）
5. 实现提醒配置UI（预设+自定义）
6. 实现子任务添加/编辑
7. 保存后自动调度提醒

### Phase 5: 通知系统

**目标**：DDL任务可在指定时间推送通知。

1. 创建 NotificationChannel
2. 实现 ReminderScheduler
3. 实现 AlarmReceiver + ShowNotificationWorker
4. 实现 BootReceiver + RescheduleRemindersWorker
5. 处理精确闹钟权限 (SCHEDULE_EXACT_ALARM / USE_EXACT_ALARM)
6. 处理通知权限 (POST_NOTIFICATIONS)
7. 通知点击跳转到任务详情
8. 通知快捷操作（完成/推迟）

### Phase 6: 计划页面

**目标**：可浏览未来7天任务和按分类浏览。

1. 实现 PlanScreen UI
2. 实现7天日历视图
3. 实现分类筛选
4. 实现搜索功能

### Phase 7: 统计页面

**目标**：完成热力图、打卡天数、生活平衡图。

1. 实现 GetDailyStatsUseCase + GetStreakUseCase
2. 实现热力图组件
3. 实现打卡横幅
4. 实现统计页面UI
5. 实现生活平衡雷达图/饼图
6. 实现冻结卡功能

### Phase 8: 设置 + 分类管理 + 数据导入导出

**目标**：完整功能闭环。

1. 实现 SettingsScreen（主题切换、每日重置时间、默认提醒等）
2. 实现 DataStore 偏好读写
3. 实现 ManageListsScreen（分类CRUD）
4. 实现数据导出为JSON
5. 实现数据从JSON导入

### Phase 9: 桌面小部件 + 打磨

**目标**：Widget可用，动画流畅。

1. 实现今日待办Widget (Glance)
2. 实现快速添加Widget
3. 打磨完成动画
4. 打磨页面转场动画
5. 处理边缘情况（空状态、网络异常等）
6. 全面UI测试

---

## 十二、依赖清单

```toml
# libs.versions.toml 完整版本

[versions]
agp = "9.1.1"
kotlin = "2.1.0"
ksp = "2.1.0-1.0.29"
compose-bom = "2025.05.00"
room = "2.7.1"
lifecycle = "2.9.0"
navigation = "2.9.0"
hilt = "2.51.1"
hiltExt = "1.3.0"
work = "2.10.0"
datastore = "1.1.4"
activity = "1.10.1"
core-ktx = "1.16.0"
coroutines = "1.10.1"
glance = "1.1.1"
material3 = "1.4.0"
serialization = "1.7.3"

[libraries]
# Compose BOM
compose-bom = { module = "androidx.compose:compose-bom", version.ref = "compose-bom" }
compose-material3 = { module = "androidx.compose.material3:material3" }
compose-ui = { module = "androidx.compose.ui:ui" }
compose-ui-graphics = { module = "androidx.compose.ui:ui-graphics" }
compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
compose-material-icons-extended = { module = "androidx.compose.material:material-icons-extended" }

# Activity & Lifecycle
activity-compose = { module = "androidx.activity:activity-compose", version.ref = "activity" }
lifecycle-runtime-compose = { module = "androidx.lifecycle:lifecycle-runtime-compose", version.ref = "lifecycle" }
lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
lifecycle-runtime-ktx = { module = "androidx.lifecycle:lifecycle-runtime-ktx", version.ref = "lifecycle" }

# Navigation
navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigation" }

# Room
room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }

# Hilt
hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
hilt-compiler = { module = "com.google.dagger:hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = { module = "androidx.hilt:hilt-navigation-compose", version.ref = "hiltExt" }
hilt-work = { module = "androidx.hilt:hilt-work", version.ref = "hiltExt" }
hilt-compiler-ext = { module = "androidx.hilt:hilt-compiler", version.ref = "hiltExt" }

# WorkManager
work-runtime-ktx = { module = "androidx.work:work-runtime-ktx", version.ref = "work" }

# DataStore
datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }

# Core
core-ktx = { module = "androidx.core:core-ktx", version.ref = "core-ktx" }

# Coroutines
coroutines-android = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "coroutines" }

# Glance (Widget)
glance-appwidget = { module = "androidx.glance:glance-appwidget", version.ref = "glance" }
glance-material3 = { module = "androidx.glance:glance-material3", version.ref = "glance" }

# Serialization (for type-safe navigation)
serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }

# Testing
junit = { module = "junit:junit", version = "4.13.2" }
ext-junit = { module = "androidx.test.ext:junit", version = "1.3.0" }
espresso-core = { module = "androidx.test.espresso:espresso-core", version = "3.7.0" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

---

## 十三、AndroidManifest.xml 权限与组件

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- 通知权限 (API 33+) -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <!-- 精确闹钟权限 (API 31+) -->
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
    <!-- 日历/闹钟类App可使用此权限替代SCHEDULE_EXACT_ALARM，无需用户手动授权 -->
    <uses-permission android:name="android.permission.USE_EXACT_ALARM" />
    <!-- 开机重调度提醒 -->
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <!-- 震动反馈 -->
    <uses-permission android:name="android.permission.VIBRATE" />

    <application
        android:name=".ToodoApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.TOODO"
        tools:targetApi="36">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.TOODO">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- 闹钟触发接收器 -->
        <receiver
            android:name=".receiver.AlarmReceiver"
            android:exported="false" />

        <!-- 开机重调度 -->
        <receiver
            android:name=".receiver.BootReceiver"
            android:exported="false">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
            </intent-filter>
        </receiver>

    </application>
</manifest>
```

---

## 十四、关键边界情况处理

| 场景 | 处理方式 |
|------|----------|
| 循环任务某天跳过不完成 | 不完成不惩罚，次日正常显示 |
| 循环规则修改 | 修改后所有未来日期按新规则，已完成记录不受影响 |
| DDL任务完成后 | 取消所有未触发的提醒闹钟 |
| App被系统杀死后重启 | WorkManager自动恢复；AlarmManager通过BootReceiver重建 |
| 跨日时任务刷新 | 注册日期变更广播，触发今日列表刷新 |
| 用户拒绝通知权限 | 提醒功能不可用但App正常运行，设置中显示权限引导 |
| 用户拒绝精确闹钟权限 | 回退到WorkManager近似提醒（15分钟误差） |
| 导入数据ID冲突 | 自增ID重新分配，通过标题+创建时间匹配去重 |
| 时区变化 | 使用LocalDate（无时区）存储日期，避免时区问题 |
| 闰年/月末日循环 | MONTHLY模式2月无31号时跳过该月 |
