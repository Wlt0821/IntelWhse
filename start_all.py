#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
启动脚本：启动后端、前端和 Copaw
"""
import subprocess
import sys
import os
import time
from threading import Thread


def run_command(command, cwd, name):
    """运行命令并打印输出"""
    print(f"🚀 正在启动 {name}...")
    try:
        process = subprocess.Popen(
            command,
            cwd=cwd,
            shell=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            bufsize=1,
            encoding='utf-8'
        )
        
        # 实时打印输出
        for line in process.stdout:
            print(f"[{name}] {line}", end='')
        
        process.wait()
    except Exception as e:
        print(f"❌ {name} 启动失败: {e}")


def main():
    print("=" * 60)
    print("📦 仓库管理系统 - 一键启动脚本")
    print("=" * 60)
    
    # 获取当前脚本所在目录
    base_dir = os.path.dirname(os.path.abspath(__file__))
    
    # 定义各项目路径
    backend_dir = os.path.join(base_dir, "wms-backend")
    frontend_dir = os.path.join(base_dir, "wms-frontend")
    
    # 定义各启动命令
    backend_cmd = "mvn spring-boot:run"
    frontend_cmd = "npm run dev"
    copaw_cmd = "copaw app"
    
    # 创建并启动线程
    threads = []
    
    # 后端线程
    t1 = Thread(target=run_command, args=(backend_cmd, backend_dir, "后端"))
    t1.daemon = True
    threads.append(t1)
    
    # 前端线程
    t2 = Thread(target=run_command, args=(frontend_cmd, frontend_dir, "前端"))
    t2.daemon = True
    threads.append(t2)
    
    # Copaw 线程
    t3 = Thread(target=run_command, args=(copaw_cmd, base_dir, "Copaw"))
    t3.daemon = True
    threads.append(t3)
    
    # 启动所有线程
    for t in threads:
        t.start()
        time.sleep(2)  # 间隔启动，避免资源竞争
    
    print("\n✅ 所有服务已启动！")
    print("📋 服务地址:")
    print("  - 后端: http://localhost:8080")
    print("  - 前端: http://localhost:3000 (或查看前端输出)")
    print("  - Copaw: 查看输出")
    print("\n按 Ctrl+C 停止所有服务")
    print("=" * 60)
    
    try:
        # 主线程等待
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("\n🛑 正在停止所有服务...")
        sys.exit(0)


if __name__ == "__main__":
    main()
