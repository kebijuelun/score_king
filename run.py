#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Multi-player Score Tracking Game Launcher
多人计分游戏启动器
"""

import os
import sys
import subprocess
import webbrowser
import time
from threading import Timer

def install_requirements():
    """Install required packages"""
    try:
        import flask
        print("✅ Flask 已安装")
    except ImportError:
        print("🔄 正在安装 Flask...")
        subprocess.check_call([sys.executable, "-m", "pip", "install", "flask"])
        print("✅ Flask 安装完成")

def open_browser():
    """Open browser after a short delay"""
    time.sleep(2)  # Wait for server to start
    webbrowser.open('http://localhost:8080')
    print("🌐 浏览器已打开: http://localhost:8080")

def main():
    """Main function to run the game"""
    print("🎯 桌游计分王启动器")
    print("=" * 40)

    # Check and install requirements
    install_requirements()

    # Change to script directory
    script_dir = os.path.dirname(os.path.abspath(__file__))
    os.chdir(script_dir)

    # Create templates directory if it doesn't exist
    os.makedirs('templates', exist_ok=True)

    print("\n🚀 启动桌游计分王服务器...")
    print("📍 服务器地址: http://localhost:8080")
    print("⚡ 按 Ctrl+C 停止服务器")
    print("-" * 40)

    # Start browser in background
    Timer(1.0, open_browser).start()

    # Run the Flask app
    try:
        from app import app
        app.run(debug=True, host='127.0.0.1', port=8080, use_reloader=False)
    except KeyboardInterrupt:
        print("\n\n👋 游戏服务器已停止")
    except Exception as e:
        print(f"\n❌ 启动失败: {e}")
        print("\n🔧 请确保:")
        print("   1. Python 环境正常")
        print("   2. 端口 8080 未被占用")
        print("   3. 有足够的权限运行程序")

if __name__ == "__main__":
    main()