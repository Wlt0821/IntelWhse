const { spawn } = require('child_process');
const http = require('http');
const fs = require('fs');
const path = require('path');

// 配置文件路径
const configFilePath = path.join(__dirname, 'rtsp-config.json');

// 默认配置
const defaultConfig = {
  port: 8085,
  streamUrl: 'http://172.20.10.1:8080/stream',
  outputDir: path.join(__dirname, 'stream-output')
};

// 加载配置
function loadConfig() {
  if (fs.existsSync(configFilePath)) {
    try {
      const savedConfig = JSON.parse(fs.readFileSync(configFilePath, 'utf8'));
      return { ...defaultConfig, ...savedConfig };
    } catch (e) {
      console.error('加载配置失败，使用默认配置:', e);
      return defaultConfig;
    }
  }
  return defaultConfig;
}

// 保存配置
function saveConfig(newConfig) {
  fs.writeFileSync(configFilePath, JSON.stringify(newConfig, null, 2), 'utf8');
}

let config = loadConfig();

// 确保输出目录存在
if (!fs.existsSync(config.outputDir)) {
  fs.mkdirSync(config.outputDir, { recursive: true });
}

let ffmpegProcess = null;
let streamActive = false;

// 启动FFmpeg转码
function startFFmpeg() {
  if (ffmpegProcess) {
    console.log('FFmpeg already running');
    return;
  }

  console.log('Starting FFmpeg transcoding...');
  
  // FFmpeg参数：HTTP -> HLS (实时流优化)
  const args = [
    '-i', config.streamUrl,
    '-c:v', 'libx264',
    '-preset', 'ultrafast',
    '-tune', 'zerolatency',
    '-crf', '25',
    '-g', '30',
    '-sc_threshold', '0',
    '-c:a', 'aac',
    '-ar', '44100',
    '-b:a', '64k',
    '-f', 'hls',
    '-hls_time', '1',
    '-hls_list_size', '3',
    '-hls_flags', 'delete_segments+append_list+omit_endlist',
    '-hls_segment_filename', path.join(config.outputDir, 'stream_%03d.ts'),
    path.join(config.outputDir, 'stream.m3u8')
  ];

  ffmpegProcess = spawn('ffmpeg', args);

  ffmpegProcess.stdout.on('data', (data) => {
    console.log(`FFmpeg stdout: ${data}`);
  });

  ffmpegProcess.stderr.on('data', (data) => {
    const errorMsg = data.toString();
    console.log(`FFmpeg stderr: ${errorMsg}`);
    // 检查是否有错误信息
    if (errorMsg.includes('error') || errorMsg.includes('failed') || errorMsg.includes('Invalid data')) {
      console.error('FFmpeg error detected:', errorMsg);
    }
  });

  ffmpegProcess.on('error', (error) => {
    console.error('FFmpeg process error:', error);
    ffmpegProcess = null;
    streamActive = false;
  });

  ffmpegProcess.on('close', (code) => {
    console.log(`FFmpeg process exited with code ${code}`);
    ffmpegProcess = null;
    streamActive = false;
  });

  streamActive = true;
}

// 停止FFmpeg
function stopFFmpeg() {
  if (ffmpegProcess) {
    console.log('Stopping FFmpeg...');
    ffmpegProcess.kill('SIGINT');
    ffmpegProcess = null;
    streamActive = false;
  }
}

// 解析请求体
function parseRequestBody(req) {
  return new Promise((resolve, reject) => {
    let body = '';
    req.on('data', chunk => {
      body += chunk.toString();
    });
    req.on('end', () => {
      try {
        resolve(body ? JSON.parse(body) : {});
      } catch (e) {
        reject(e);
      }
    });
    req.on('error', reject);
  });
}

// 创建HTTP服务器
const server = http.createServer(async (req, res) => {
  // CORS headers
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') {
    res.writeHead(200);
    res.end();
    return;
  }

  // 获取配置
  if (req.url === '/api/config' && req.method === 'GET') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ 
      success: true, 
      streamUrl: config.streamUrl,
      port: config.port
    }));
    return;
  }

  // 更新配置
  if (req.url === '/api/config' && req.method === 'PUT') {
    try {
      const body = await parseRequestBody(req);
      if (body.streamUrl) {
        // 如果流正在运行，先停止
        if (streamActive) {
          stopFFmpeg();
        }
        // 更新配置
        config.streamUrl = body.streamUrl;
        saveConfig(config);
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ 
          success: true, 
          message: '配置已更新',
          streamUrl: config.streamUrl
        }));
        console.log(`Stream地址已更新: ${config.streamUrl}`);
      } else {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: false, message: '缺少streamUrl参数' }));
      }
    } catch (e) {
      res.writeHead(400, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ success: false, message: '请求体解析失败' }));
    }
    return;
  }

  // 控制接口
  if (req.url === '/api/start' && req.method === 'POST') {
    startFFmpeg();
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ success: true, message: 'Stream started' }));
    return;
  }

  if (req.url === '/api/stop' && req.method === 'POST') {
    stopFFmpeg();
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ success: true, message: 'Stream stopped' }));
    return;
  }

  if (req.url === '/api/status') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ 
      success: true, 
      active: streamActive,
      streamUrl: config.streamUrl,
      url: `http://localhost:${config.port}/stream.m3u8`
    }));
    return;
  }

  // 静态文件服务（HLS）
  let filePath = path.join(config.outputDir, req.url === '/' ? 'stream.m3u8' : req.url);
  
  // 安全检查
  if (!filePath.startsWith(config.outputDir)) {
    res.writeHead(403);
    res.end('Forbidden');
    return;
  }

  fs.readFile(filePath, (err, data) => {
    if (err) {
      res.writeHead(404);
      res.end('File not found');
      return;
    }

    // 设置正确的Content-Type
    const ext = path.extname(filePath);
    let contentType = 'application/octet-stream';
    if (ext === '.m3u8') {
      contentType = 'application/vnd.apple.mpegurl';
    } else if (ext === '.ts') {
      contentType = 'video/MP2T';
    }

    res.writeHead(200, { 'Content-Type': contentType });
    res.end(data);
  });
});

// 启动服务器
server.listen(config.port, () => {
  console.log(`Stream Server running at http://localhost:${config.port}`);
  console.log(`HLS Stream URL: http://localhost:${config.port}/stream.m3u8`);
  console.log(`Control API:`);
  console.log(`  POST /api/start - Start streaming`);
  console.log(`  POST /api/stop - Stop streaming`);
  console.log(`  GET /api/status - Get status`);
  console.log(`\nStream Source: ${config.streamUrl}`);
});

// 优雅关闭
process.on('SIGINT', () => {
  console.log('\nShutting down...');
  stopFFmpeg();
  server.close(() => {
    console.log('Server closed');
    process.exit(0);
  });
});
