import sys
import time
import threading
import cv2
import requests 
import logging
import uuid
from loguru import logger
from pathlib import Path
from datetime import datetime

# === 屏蔽 Ultralytics (YOLO) 在终端的无关打印输出 ===
logging.getLogger("ultralytics").setLevel(logging.WARNING)

from core_modules.stream_handler import StreamHandler
from core_modules.yolo_detector import YoloDetector
from core_modules.barcode_mapper import BarcodeMapper

# ================= 全局配置 =================
# 目标地址：指向你的 8080 端口新路径
SERVER_URL = "http://192.168.50.9:8080/inventory/records/add"
# ============================================

class WarehouseSystem:
    def __init__(self):
        self.project_root = Path(__file__).parent
        self.is_running = False
        
        self.stream = StreamHandler(self.project_root)
        # 这里的路径根据你实际存放模型的位置调整
        model_file = str(self.project_root / "model" / "yolov8n.pt")
        self.detector = YoloDetector(model_path=model_file)
        self.mapper = BarcodeMapper()

        self.latest_frame = None       
        self.frame_lock = threading.Lock()
        self.latest_results = ([], [], {}, None) 
        self.result_lock = threading.Lock()
        
        self.stream_fps = 0
        self.ai_fps = 0
        self.last_post_time = 0

    def _capture_thread(self):
        """负责从视频流中抓取最新帧"""
        cap = self.stream.get_video_capture()
        frame_count, start_time = 0, time.time()

        while self.is_running:
            ret, frame = cap.read()
            if not ret:
                time.sleep(0.1)
                # 如果掉线，尝试根据 rtsp 重新连接
                cap.open(self.stream.rtmp_url) 
                continue

            frame_count += 1
            if time.time() - start_time > 1.0:
                self.stream_fps = frame_count / (time.time() - start_time)
                frame_count, start_time = 0, time.time()

            with self.frame_lock:
                self.latest_frame = frame
        cap.release()

    def _ai_thread(self):
        """负责进行 YOLO 检测、二维码扫描及数据上传"""
        ai_frame_count, start_time = 0, time.time()

        while self.is_running:
            with self.frame_lock:
                if self.latest_frame is None:
                    time.sleep(0.01)
                    continue
                frame_to_process = self.latest_frame.copy()

            # 1. 核心视觉算法
            boxes = self.detector.detect(frame_to_process)
            barcodes, cargo_map, pip_img = self.mapper.scan_and_map(frame_to_process, boxes)

            # 2. 调用上传函数
            self._upload_to_database(cargo_map, boxes, barcodes)

            # 3. 统计 AI 处理速度
            ai_frame_count += 1
            if time.time() - start_time > 1.0:
                self.ai_fps = ai_frame_count / (time.time() - start_time)
                ai_frame_count, start_time = 0, time.time()

            # 4. 更新共享结果供 UI 线程显示
            with self.result_lock:
                self.latest_results = (boxes, barcodes, cargo_map, pip_img)

    def _upload_to_database(self, cargo_map, boxes, barcodes):
        """构建中文数据包并异步上传"""
        if not cargo_map:
            return

        current_time = time.time()
        # 控制上传频率 (每1.5秒更新一次)
        if current_time - self.last_post_time > 1.5:
            
            # 1. 整理二维码信息列表，包含坐标
            qr_details_list = []
            for (bx, by, bw, bh, sku) in barcodes:
                qr_details_list.append({
                    "解码内容": sku,
                    "空间坐标": [bx, by, bw, bh]
                })

            # 2. 生成流水编号
            time_str = datetime.now().strftime("%Y%m%d%H%M%S")
            batch_id = f"SCAN_{time_str}_{str(uuid.uuid4())[:6].upper()}"
            
            # 3. 构建 payload
            payload = {
                "盘点批次": batch_id,
                "盘点时间": datetime.now().isoformat(),
                "设备名称": "AGX_主控节点_01",
                "画面货物总数": len(boxes),
                "识别二维码数": len(barcodes),
                "货物汇总数据": cargo_map,
                "二维码详细数据": qr_details_list
            }

            # 4. 异步发送，直接使用 SERVER_URL 变量
            def post_data():
                try:
                    res = requests.post(SERVER_URL, json=payload, timeout=2.0)
                    if res.status_code == 200:
                        logger.success(f"成功推送到: {SERVER_URL}")
                    else:
                        logger.warning(f"服务器接收失败，状态码: {res.status_code}")
                except Exception as e:
                    logger.debug(f"网络请求跳过 (未连接): {e}")

            threading.Thread(target=post_data, daemon=True).start()
            self.last_post_time = current_time

    def run(self):
        # 初始化日志格式
        logger.remove()
        logger.add(sys.stdout, format="<green>{time:HH:mm:ss}</green> | <level>{level: <8}</level> | <cyan>{message}</cyan>", level="SUCCESS")
        
        logger.success("系统主控启动，多线程架构已就绪...")
        
        # === 修复点：调用你 StreamHandler 中正确的启动方法 ===
        self.stream.start_mediamtx()
        
        self.is_running = True
        
        # 开启抓图线程和 AI 线程
        threading.Thread(target=self._capture_thread, daemon=True).start()
        threading.Thread(target=self._ai_thread, daemon=True).start()

        try:
            while self.is_running:
                with self.frame_lock:
                    display_frame = self.latest_frame.copy() if self.latest_frame is not None else None
                
                if display_frame is not None:
                    with self.result_lock:
                        boxes, barcodes, cargo_map, pip_img = self.latest_results

                    # --- 渲染检测框 ---
                    for (x1, y1, x2, y2, cls_name) in boxes:
                        cv2.rectangle(display_frame, (x1, y1), (x2, y2), (255, 0, 0), 2)

                    # --- 渲染二维码和 SKU 信息 ---
                    for (bx, by, bw, bh, sku) in barcodes:
                        cv2.rectangle(display_frame, (bx, by), (bx + bw, by + bh), (0, 255, 0), 3)
                        qty = cargo_map.get(sku, 0)
                        label = f"SKU:{sku} | QTY:{qty}"
                        cv2.putText(display_frame, label, (bx, by - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 0), 2)

                    # 显示 FPS
                    status_text = f"Stream: {self.stream_fps:.1f} | AI: {self.ai_fps:.1f}"
                    cv2.putText(display_frame, status_text, (20, 40), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 0, 255), 2)

                    # 画中画 (PiP)
                    if pip_img is not None:
                        pip_w = 300
                        pip_h = int((pip_w / pip_img.shape[1]) * pip_img.shape[0])
                        pip_resized = cv2.resize(pip_img, (pip_w, pip_h))
                        margin = 20
                        h_main, w_main = display_frame.shape[:2]
                        y_start, y_end = margin, margin + pip_h
                        x_start, x_end = w_main - margin - pip_w, w_main - margin
                        
                        if y_end <= h_main and x_end <= w_main and y_start >= 0 and x_start >= 0:
                            display_frame[y_start:y_end, x_start:x_end] = pip_resized

                    # 窗口自适应
                    display_frame = cv2.resize(display_frame, (0,0), fx=0.6, fy=0.6) if display_frame.shape[0] > 800 else display_frame
                    cv2.imshow("Smart Warehouse Node", display_frame)

                if cv2.waitKey(1) & 0xFF == ord('q'):
                    self.is_running = False
                    break
        finally:
            self.stream.stop()
            cv2.destroyAllWindows()
            logger.success("程序已安全退出")

if __name__ == "__main__":
    system = WarehouseSystem()
    system.run()