#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

if [[ -z "${ANDROID_SDK_ROOT:-}" && -z "${ANDROID_HOME:-}" ]]; then
  echo "[!] 未检测到 ANDROID_SDK_ROOT/ANDROID_HOME，请先安装 Android SDK 并配置环境变量。"
  echo "    - 打开 Android Studio > More Actions > SDK Manager 安装 SDK"
  echo "    - 或设置: export ANDROID_SDK_ROOT=~/Library/Android/sdk"
  exit 1
fi

if [[ ! -f ./gradlew ]]; then
  echo "[i] 未发现 Gradle Wrapper。"
  if command -v gradle >/dev/null 2>&1; then
    echo "[i] 使用本地 gradle 生成 wrapper..."
    gradle wrapper
  else
    echo "[!] 未安装 gradle。建议用 Android Studio 打开本项目并构建 APK，或先安装 gradle 后重试:"
    echo "    brew install gradle"
    exit 1
  fi
fi

chmod +x ./gradlew
./gradlew --no-daemon assembleDebug

APK_PATH="app/build/outputs/apk/debug/score_king.apk"
if [[ -f "$APK_PATH" ]]; then
  echo "[✓] 构建成功: $APK_PATH"
else
  echo "[!] 构建未生成预期 APK，请检查上方日志。"
  exit 1
fi