from fastapi import FastAPI, Depends, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from sqlalchemy import create_engine, Column, Integer, String, DateTime, JSON
from sqlalchemy.orm import declarative_base, sessionmaker, Session
from datetime import datetime

# ================= 1. 数据库配置 =================
SQLALCHEMY_DATABASE_URL = "sqlite:///./inventory.db"
engine = create_engine(SQLALCHEMY_DATABASE_URL, connect_args={"check_same_thread": False})
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

# ================= 2. 数据库底层模型 (ORM) =================
# 底层使用英文列名，确保数据库引擎绝对稳定
class InventoryDB(Base):
    __tablename__ = "inventory_records"

    id = Column(Integer, primary_key=True, index=True)
    batch_id = Column(String(50), unique=True, index=True)       # 批次号
    device_name = Column(String(50), index=True)                 # 设备名
    timestamp = Column(DateTime, default=datetime.utcnow)        # 时间
    
    total_boxes = Column(Integer, default=0)                     # 识别框总数
    total_qrs = Column(Integer, default=0)                       # 扫码总数
    
    cargo_summary = Column(JSON)                                 # SKU汇总数据
    qr_details = Column(JSON)                                    # 详细坐标数据

# 自动创建表（如果数据库文件不存在会自动生成）
Base.metadata.create_all(bind=engine)

# ================= 3. 客户端发送来的数据结构 (Pydantic) =================
# 完美对接 main.py 里发过来的中文 JSON
class InventoryRequest(BaseModel):
    盘点批次: str
    盘点时间: str
    设备名称: str
    画面货物总数: int
    识别二维码数: int
    货物汇总数据: dict
    二维码详细数据: list

# ================= 4. FastAPI 核心应用配置 =================
app = FastAPI(title="AGX 智能仓储盘点中心节点")

# 【核心】：开启 CORS 跨域，允许前端大屏网页直接请求数据
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 依赖注入：获取数据库连接会话
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

# ================= 5. API 路由接口 =================

@app.post("/upload_inventory")
def upload_inventory(data: InventoryRequest, db: Session = Depends(get_db)):
    """接收 AGX 端发来的盘点数据并存入数据库"""
    try:
        dt_time = datetime.fromisoformat(data.盘点时间)
        
        # 将接收到的中文映射到底层英文表结构
        db_record = InventoryDB(
            batch_id=data.盘点批次,
            device_name=data.设备名称,
            timestamp=dt_time,
            total_boxes=data.画面货物总数,
            total_qrs=data.识别二维码数,
            cargo_summary=data.货物汇总数据,
            qr_details=data.二维码详细数据
        )
        db.add(db_record)
        db.commit()
        
        print(f"[{dt_time.strftime('%H:%M:%S')}] 批次 {data.盘点批次} | 成功接收 {data.识别二维码数} 个二维码数据")
        return {"状态码": 200, "提示": "中文盘点数据落盘成功！"}
        
    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/get_inventory")
def get_inventory(db: Session = Depends(get_db)):
    """供前端大屏网页调用的数据接口"""
    # 按时间倒序，取出最新的 5 条盘点记录
    records = db.query(InventoryDB).order_by(InventoryDB.timestamp.desc()).limit(5).all()
    
    # 重新包装成纯中文的 JSON 格式发给前端
    result = []
    for r in records:
        result.append({
            "盘点批次": r.batch_id,
            "时间": r.timestamp.strftime("%Y-%m-%d %H:%M:%S"),
            "设备": r.device_name,
            "检测箱子数": r.total_boxes,
            "扫码数量": r.total_qrs,
            "货物清单": r.cargo_summary,
            "扫码详情": r.qr_details
        })
    return {"状态码": 200, "数据": result}

# ================= 6. 服务启动入口 =================
if __name__ == "__main__":
    import uvicorn
    uvicorn.run("api:app", host="0.0.0.0", port=5000, reload=True)