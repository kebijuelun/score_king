# 桌游计分王（Score King）



一个包含 Web 与 Android App 的多人多轮计分项目：
- Web（Flask + HTML/JS）：浏览器里多人计分（默认端口 8080）
- Android App（Kotlin + Jetpack Compose）：原生离线计分应用（应用名：score_king / 桌游计分王）

## 功能
- 添加/移除玩家
- 多轮累积计分
- 设置胜利阈值（默认 200）
- 达阈值自动判定并提示
- 清零分数或重置游戏

## 快速开始

### 环境准备（Linux）
- 安装 JDK 17 与常用工具：
  ```bash
  sudo apt-get update
  sudo apt-get install -y openjdk-17-jdk wget unzip
  ```
- 配置环境变量（当前会话测试）：
  ```bash
  export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
  export ANDROID_SDK_ROOT="$HOME/Android/Sdk"
  export PATH="$JAVA_HOME/bin:$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/emulator:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$PATH"
  java -version
  ```
- 安装 Android SDK 命令行工具与组件（首次执行需较长时间）：
  ```bash
  # 如果缺少 sdkmanager，可先安装 Android Studio 并在 SDK Manager 勾选 Command-line Tools
  yes | sdkmanager --licenses --sdk_root="$ANDROID_SDK_ROOT"
  sdkmanager --sdk_root="$ANDROID_SDK_ROOT" \
    "platform-tools" "platforms;android-34" "build-tools;34.0.0" "cmdline-tools;latest"
  ```
- 持久化到 zsh：
  ```bash
  echo 'export ANDROID_SDK_ROOT="$HOME/Android/Sdk"' >> ~/.zshrc
  echo 'export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"' >> ~/.zshrc
  echo 'export PATH="$JAVA_HOME/bin:$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/emulator:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$PATH"' >> ~/.zshrc
  source ~/.zshrc
  ```

### 环境准备（macOS）
- 安装 JDK 17（Homebrew）：
  ```bash
  brew install openjdk@17
  sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk 2>/dev/null || true
  ```
- 配置环境变量（zsh）：
  ```bash
  echo 'export JAVA_HOME="/opt/homebrew/opt/openjdk@17"' >> ~/.zshrc
  echo 'export ANDROID_SDK_ROOT="$HOME/Library/Android/sdk"' >> ~/.zshrc
  echo 'export PATH="$JAVA_HOME/bin:$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/emulator:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$PATH"' >> ~/.zshrc
  source ~/.zshrc
  java -version
  ```
- 安装 Android SDK 组件（如缺少 sdkmanager，请先在 Android Studio 的 SDK Manager 勾选 Command-line Tools）：
  ```bash
  yes | sdkmanager --licenses --sdk_root="$ANDROID_SDK_ROOT"
  sdkmanager --sdk_root="$ANDROID_SDK_ROOT" \
    "platform-tools" "platforms;android-34" "build-tools;34.0.0" "cmdline-tools;latest"
  ```

### 环境准备（Windows）
- 安装 JDK 17 与 Android Studio：
  - 建议从 Oracle/OpenJDK 官方或使用包管理器（如 winget/choco）安装 JDK 17
  - 安装 Android Studio，并在 SDK Manager 中安装 "Android SDK Command-line Tools"
- 配置环境变量（PowerShell，以用户环境为例）：
  ```powershell
  # 按实际安装路径修改
  setx JAVA_HOME "C:\\Program Files\\Java\\jdk-17"
  setx ANDROID_SDK_ROOT "%USERPROFILE%\\AppData\\Local\\Android\\Sdk"
  setx PATH "%JAVA_HOME%\\bin;%ANDROID_SDK_ROOT%\\platform-tools;%ANDROID_SDK_ROOT%\\emulator;%ANDROID_SDK_ROOT%\\cmdline-tools\\latest\\bin;%PATH%"
  # 重新打开一个新的 PowerShell 窗口后生效
  java -version
  ```
- 安装 SDK 组件（在新开的 PowerShell 窗口）：
  ```powershell
  sdkmanager --licenses --sdk_root "%ANDROID_SDK_ROOT%"
  sdkmanager --sdk_root "%ANDROID_SDK_ROOT%" "platform-tools" "platforms;android-34" "build-tools;34.0.0" "cmdline-tools;latest"
  ```

### Web 版
```bash
# 安装依赖
pip install -r requirements.txt

# 启动（任选其一）
python run.py
# 或
python app.py
# 访问: http://localhost:8080
```

### Android 版
方式一（推荐）：使用 Android Studio
1) 打开 Android Studio → Open → 选择 `android_app/`
2) 等待 Gradle 同步
3) Build → Build APK(s)
4) APK 输出路径：`android_app/app/build/outputs/apk/debug/app-debug.apk`

方式二：命令行构建（可复现）
```bash
# 1) 确保使用 Java 17
export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
export PATH="$JAVA_HOME/bin:$PATH"

# 2) 构建 APK
cd android_app
./gradlew assembleDebug
# 输出: app/build/outputs/apk/debug/app-debug.apk
```

如需一键脚本（已提供）：
```bash
cd android_app
./build-apk.sh
```
该脚本会优先使用 Gradle Wrapper；若缺失，则尝试使用已安装的 `gradle` 生成。

### 常见问题（Android 构建）
- Gradle 下载超时：本仓库已将 `android_app/gradle/wrapper/gradle-wrapper.properties` 的 `distributionUrl` 指向镜像源；如需切换，可修改为可用的镜像地址。
- JVM 版本错误：Gradle 9 需要 Java 17+；请确认 `java -version` 输出为 17 及以上，并设置了正确的 `JAVA_HOME`。
- 找不到 sdkmanager：请在 Android Studio 的 SDK Manager 勾选 “Android SDK Command-line Tools”，或确认 `ANDROID_SDK_ROOT` 指向正确目录。

## 目录结构
```
score_king_app/
├── app.py                  # Flask 应用
├── run.py                  # 启动器
├── templates/index.html    # Web 界面
├── android_app/            # Android 应用
│   ├── app/src/...         # 源码
│   ├── gradlew*            # Gradle Wrapper（已包含）
│   └── build-apk.sh        # 一键构建脚本
├── requirements.txt        # Web 依赖
└── README.md               # 中文说明（默认）
```

## ANDROID_SDK_ROOT 说明
- Linux 常用位置：`$HOME/Android/Sdk`
- macOS 常用位置：`$HOME/Library/Android/sdk`
- 如果 `sdkmanager` 未安装：
  - 打开 Android Studio → More Actions → SDK Manager → 勾选 “Android SDK Command-line Tools”

## License
本项目用于学习与演示目的。