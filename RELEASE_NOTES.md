## Score King v1.0.1

发布日期：2025-08-10

### 本次更新
- 新增：GitHub Actions 自动发布流程，推送 `v*` 标签后自动：构建 Android APK、创建 Release、上传附件。
- 优化：Release 附件重命名为更易识别的格式：`Score_King-vX.Y.Z-android-debug.apk`。

### 下载
- Android（调试签名，适合体验/内测）：`Score_King-v1.0.1-android-debug.apk`（见本次 Release 附件）。
- Releases 页面：请访问仓库的 Releases 获取历史版本。

### 兼容性
- 最低支持：Android 7.0（API 24）。
- 无破坏性变更。

### 说明
- 当前为 Debug 构建，便于快速体验；如需正式上架版本，请联系维护者配置签名并切换为 `assembleRelease` 构建与上传。


