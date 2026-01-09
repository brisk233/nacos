# Nacos Syslog API - 获取系统日志接口

## 概述 (Overview)

Nacos提供了一套RESTful API用于获取和查看系统日志文件。这些API允许管理员通过HTTP请求访问Nacos服务器的日志文件，方便远程监控和故障排查。

Nacos provides a set of RESTful APIs for retrieving and viewing system log files. These APIs allow administrators to access Nacos server log files via HTTP requests, facilitating remote monitoring and troubleshooting.

## 安全要求 (Security Requirements)

所有日志API都需要管理员权限。请求时需要携带有效的认证信息。

All log APIs require administrator privileges. Valid authentication information must be included in the requests.

## API端点 (API Endpoints)

### 1. 列出所有日志文件 (List All Log Files)

列出Nacos日志目录中所有可用的.log文件。

List all available .log files in the Nacos logs directory.

**请求 (Request):**
```
GET /nacos/v1/core/ops/logs
```

**响应示例 (Response Example):**
```json
{
  "code": 200,
  "message": null,
  "data": [
    "alipay-jraft.log",
    "cmdb-main.log",
    "config-dump.log",
    "config-server.log",
    "core-auth.log",
    "nacos-cluster.log",
    "nacos.log",
    "naming-server.log",
    "protocol-raft.log",
    "remote.log"
  ]
}
```

**使用示例 (Usage Example):**
```bash
curl -X GET "http://localhost:8848/nacos/v1/core/ops/logs" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 2. 获取日志文件信息 (Get Log File Information)

获取指定日志文件的元数据信息，包括文件大小、最后修改时间等。

Get metadata information for a specified log file, including file size, last modification time, etc.

**请求 (Request):**
```
GET /nacos/v1/core/ops/logs/info?logName={fileName}
```

**参数 (Parameters):**
- `logName`: 日志文件名 (必需) / Log file name (required)

**响应示例 (Response Example):**
```json
{
  "code": 200,
  "message": null,
  "data": {
    "fileName": "nacos.log",
    "size": 1048576,
    "lastModified": 1641234567890,
    "path": "/home/nacos/logs/nacos.log"
  }
}
```

**使用示例 (Usage Example):**
```bash
curl -X GET "http://localhost:8848/nacos/v1/core/ops/logs/info?logName=nacos.log" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 3. 获取日志文件尾部内容 (Tail Log File)

获取日志文件的最后N行内容，类似于Linux的`tail`命令。这是查看最新日志的最常用方式。

Get the last N lines of a log file, similar to the Linux `tail` command. This is the most common way to view recent logs.

**请求 (Request):**
```
GET /nacos/v1/core/ops/logs/tail?logName={fileName}&lines={numberOfLines}
```

**参数 (Parameters):**
- `logName`: 日志文件名 (必需) / Log file name (required)
- `lines`: 返回的行数 (可选，默认500，最大10000) / Number of lines to return (optional, default: 500, max: 10000)

**响应示例 (Response Example):**
```json
{
  "code": 200,
  "message": null,
  "data": [
    "2026-01-09 10:30:45 INFO Starting Nacos Server...",
    "2026-01-09 10:30:46 INFO Nacos Server started successfully",
    "2026-01-09 10:30:47 INFO Service registered: example-service"
  ]
}
```

**使用示例 (Usage Examples):**

获取最后100行日志:
```bash
curl -X GET "http://localhost:8848/nacos/v1/core/ops/logs/tail?logName=nacos.log&lines=100" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

获取最后500行日志 (默认):
```bash
curl -X GET "http://localhost:8848/nacos/v1/core/ops/logs/tail?logName=nacos.log" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 4. 分页获取日志内容 (Get Log File Content with Pagination)

从指定行开始读取日志文件内容，支持分页查询。

Read log file content starting from a specified line, with pagination support.

**请求 (Request):**
```
GET /nacos/v1/core/ops/logs/content?logName={fileName}&startLine={start}&lineCount={count}
```

**参数 (Parameters):**
- `logName`: 日志文件名 (必需) / Log file name (required)
- `startLine`: 起始行号，从1开始 (可选，默认1) / Starting line number, 1-based (optional, default: 1)
- `lineCount`: 读取的行数 (可选，默认1000，最大10000) / Number of lines to read (optional, default: 1000, max: 10000)

**响应示例 (Response Example):**
```json
{
  "code": 200,
  "message": null,
  "data": [
    "2026-01-09 10:00:00 INFO Application started",
    "2026-01-09 10:00:01 INFO Loading configuration",
    "2026-01-09 10:00:02 INFO Configuration loaded successfully"
  ]
}
```

**使用示例 (Usage Examples):**

从第1行开始读取1000行:
```bash
curl -X GET "http://localhost:8848/nacos/v1/core/ops/logs/content?logName=nacos.log&startLine=1&lineCount=1000" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

从第5001行开始读取1000行 (第二页):
```bash
curl -X GET "http://localhost:8848/nacos/v1/core/ops/logs/content?logName=nacos.log&startLine=5001&lineCount=1000" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 常用日志文件说明 (Common Log Files Description)

- `nacos.log`: Nacos主日志文件 / Main Nacos log file
- `naming-server.log`: 服务发现相关日志 / Service discovery logs
- `config-server.log`: 配置管理相关日志 / Configuration management logs
- `config-dump.log`: 配置dump操作日志 / Configuration dump operation logs
- `core-auth.log`: 认证授权相关日志 / Authentication and authorization logs
- `protocol-raft.log`: Raft协议相关日志 / Raft protocol logs
- `remote.log`: 远程通信相关日志 / Remote communication logs

## 使用场景 (Use Cases)

### 1. 实时监控最新日志
```bash
# 监控主日志文件的最新100行
curl -X GET "http://localhost:8848/nacos/v1/core/ops/logs/tail?logName=nacos.log&lines=100"
```

### 2. 故障排查
```bash
# 查看配置服务器日志的最新500行
curl -X GET "http://localhost:8848/nacos/v1/core/ops/logs/tail?logName=config-server.log&lines=500"

# 查看认证日志
curl -X GET "http://localhost:8848/nacos/v1/core/ops/logs/tail?logName=core-auth.log&lines=200"
```

### 3. 日志分析
```bash
# 分页读取大日志文件
curl -X GET "http://localhost:8848/nacos/v1/core/ops/logs/content?logName=nacos.log&startLine=1&lineCount=5000"
```

## 错误响应 (Error Responses)

### 文件不存在
```json
{
  "code": 500,
  "message": "Failed to read log file: Log file not found: nonexistent.log",
  "data": null
}
```

### 无效的文件名
```json
{
  "code": 500,
  "message": "Invalid log file name: Invalid file name: ../etc/passwd",
  "data": null
}
```

### 权限不足
```json
{
  "code": 403,
  "message": "Forbidden",
  "data": null
}
```

## 安全注意事项 (Security Considerations)

1. **认证要求**: 所有API都需要管理员权限
2. **路径安全**: API会自动阻止目录遍历攻击（如`../`）
3. **文件类型限制**: 只允许访问`.log`扩展名的文件
4. **大小限制**: 单次请求最多返回10000行，防止内存溢出
5. **访问日志**: 所有日志访问操作都会被记录

## Python示例代码 (Python Example Code)

```python
import requests
import json

class NacosLogAPI:
    def __init__(self, base_url, token):
        self.base_url = base_url
        self.headers = {"Authorization": f"Bearer {token}"}
    
    def list_logs(self):
        """列出所有日志文件"""
        url = f"{self.base_url}/nacos/v1/core/ops/logs"
        response = requests.get(url, headers=self.headers)
        return response.json()
    
    def tail_log(self, log_name, lines=500):
        """获取日志尾部内容"""
        url = f"{self.base_url}/nacos/v1/core/ops/logs/tail"
        params = {"logName": log_name, "lines": lines}
        response = requests.get(url, params=params, headers=self.headers)
        return response.json()
    
    def get_log_info(self, log_name):
        """获取日志文件信息"""
        url = f"{self.base_url}/nacos/v1/core/ops/logs/info"
        params = {"logName": log_name}
        response = requests.get(url, params=params, headers=self.headers)
        return response.json()

# 使用示例
api = NacosLogAPI("http://localhost:8848", "your_token_here")

# 列出所有日志
logs = api.list_logs()
print("Available logs:", logs['data'])

# 查看nacos.log的最后100行
tail = api.tail_log("nacos.log", 100)
for line in tail['data']:
    print(line)
```

## Java示例代码 (Java Example Code)

```java
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.HashMap;
import java.util.Map;

public class NacosLogClient {
    private String baseUrl;
    private String token;
    private RestTemplate restTemplate;
    
    public NacosLogClient(String baseUrl, String token) {
        this.baseUrl = baseUrl;
        this.token = token;
        this.restTemplate = new RestTemplate();
    }
    
    public String listLogs() {
        String url = baseUrl + "/nacos/v1/core/ops/logs";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.GET, entity, String.class);
        return response.getBody();
    }
    
    public String tailLog(String logName, int lines) {
        String url = String.format("%s/nacos/v1/core/ops/logs/tail?logName=%s&lines=%d",
            baseUrl, logName, lines);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.GET, entity, String.class);
        return response.getBody();
    }
}
```

## 总结 (Summary)

Nacos的Syslog API提供了便捷的日志访问接口，可以通过HTTP请求远程查看和管理日志文件。主要功能包括：

1. 列出所有日志文件
2. 查看日志文件元信息
3. 获取日志尾部内容（最常用）
4. 分页读取日志内容

这些API特别适用于：
- 远程监控Nacos运行状态
- 故障诊断和问题排查
- 自动化日志收集和分析
- 集成到监控系统中

The Nacos Syslog API provides convenient log access interfaces, allowing remote viewing and management of log files via HTTP requests. Main features include:

1. Listing all log files
2. Viewing log file metadata
3. Getting log tail content (most commonly used)
4. Reading log content with pagination

These APIs are particularly useful for:
- Remote monitoring of Nacos operation status
- Fault diagnosis and troubleshooting
- Automated log collection and analysis
- Integration into monitoring systems
