# Killbill 应用功能与技术实现解析

院校：NUIST & SETU
作者：黄家睿、金垲峰、郭玮晨

## 一、核心功能概述
1. **当日账单聚合展示**  
   - 实时汇总当日消费记录，通过 `RecyclerView` 动态展示消费明细（金额、时间、类别、备注）
   - 顶部显示当日总支出，支持滑动查看全部条目

2. **月度消费可视化分析**  
   - 基于 `MPAndroidChart` 库生成动态饼图，按消费类别统计当月支出占比
   - 底部列表展示详细账单数据，支持点击条目跳转编辑

3. **自动化账单捕获**  
   - 监听系统广播消息（支付宝/微信支付通知）
   - 通过正则表达式提取金额信息，自动跳转至账单录入页面并填充金额字段

## 二、关键技术实现细节

### 1. 数据模型与持久化
#### (1) NotifyModel 类
- 封装账单属性（金额、时间、类别、备注），作为数据载体在模块间传递

#### (2) MainApp 全局管理器
- 采用单例模式维护账单队列（`Array<NotifyModel>`），作为内存中的临时存储中心
- 支持通过 **JSON 序列化** 实现数据持久化（文件读写路径：`/data/data/{package}/files/bills.json`）

### 2. 界面架构设计
#### (1) Fragment 页面切换
- **MainActivity** 承载 `Toolbar` 和 `BottomNavigationView`，通过 `FragmentManager` 动态加载三个核心 Fragment：
  - **DailyFragment**（默认首页）：展示当日消费列表，通过日期过滤 `MainApp.queue` 生成当日数据集
  - **StatsFragment**：调用 `getMonthlyData()` 汇总当月数据，饼图与列表数据联动更新
  - **ListFragment**：全量账单列表，支持无限滑动加载

#### (2) 手动录入界面
- 独立 Activity 通过 `Toolbar` 按钮触发，采用 `EditText` + `Spinner` 组合输入金额、类别（下拉选择）
- 点击 "ADDING" 提交数据至 `MainApp.queue`，同步写入 JSON 文件

### 3. 消息监听与后台服务
#### (1) NotifyService 组件
- **NotifyListener 接口**：定义 `onReceiveNotify()` 回调，由子类实现消息解析逻辑
- **NotifyHelper 工具类**：封装 `registerReceiver()` 方法，在后台服务中动态注册支付宝（`com.eg.android.AlipayGphone`）和微信（`com.tencent.mm`）的支付广播监听
- **BackgroundService**：继承 `Service` 并启动前台服务，显示持久化通知（ID：1001）

#### (2) 消息解析流程
1. 过滤指定包名的广播消息
2. 正则匹配关键词（`支付|消费|支出`）及金额模式（`\\d{4,5}(?=元)|￥\\d{4,5}`）
3. 提取金额后启动录入界面，通过 `Intent.putExtra("amount", value)` 传递数据

### 4. 账单编辑与删除
- **CardView 交互**：
  - 点击列表项跳转至编辑界面，按钮文本切换为 "SAVE"，修改后更新 `MainApp.queue` 及 JSON 文件
  - 长按或点击 "DELETE" 按钮触发删除操作，通过 `notifyItemRemoved(position)` 刷新 `RecyclerView`

### 5. 数据同步机制
- **启动时加载**：`MainApp.loadFromDisk()` 反序列化 JSON 文件至内存队列
- **实时保存**：每次增删改操作后调用 `MainApp.saveToDisk()`，采用异步线程避免 ANR

## 三、技术亮点总结
- **高效内存管理**：通过单例模式 + JSON 序列化实现内存与存储的数据同步
- **智能消息解析**：结合正则表达式与关键词过滤，精准捕获支付通知中的金额信息
- **前台服务保活**：利用系统通知栏常驻服务，突破 Android 后台限制
- **模块化设计**：数据层（MainApp）、服务层（NotifyService）、视图层（Fragment）解耦
