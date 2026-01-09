# Syslog API Quick Start Guide

## 快速开始 (Quick Start)

这是Nacos Syslog API的快速入门指南。

This is a quick start guide for the Nacos Syslog API.

## Prerequisites / 前提条件

1. Nacos server is running / Nacos服务器正在运行
2. You have admin credentials / 您有管理员凭据
3. Authentication is enabled (for production) / 已启用身份验证（用于生产环境）

## Quick Test / 快速测试

### Step 1: List All Log Files / 步骤1：列出所有日志文件

```bash
# Without authentication (standalone mode)
curl -X GET "http://localhost:8848/nacos/v1/core/ops/logs"

# With authentication
curl -X GET "http://localhost:8848/nacos/v1/core/ops/logs" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response / 预期响应:**
```json
{
  "code": 200,
  "message": null,
  "data": [
    "alipay-jraft.log",
    "config-server.log",
    "nacos.log",
    "naming-server.log"
  ]
}
```

### Step 2: View Recent Logs / 步骤2：查看最近的日志

View the last 50 lines of the main log file:

查看主日志文件的最后50行：

```bash
curl -X GET "http://localhost:8848/nacos/v1/core/ops/logs/tail?logName=nacos.log&lines=50"
```

**Expected Response / 预期响应:**
```json
{
  "code": 200,
  "message": null,
  "data": [
    "2026-01-09 10:30:45 INFO Starting Nacos Server...",
    "2026-01-09 10:30:46 INFO Nacos Server started successfully",
    "..."
  ]
}
```

### Step 3: Get Log File Info / 步骤3：获取日志文件信息

```bash
curl -X GET "http://localhost:8848/nacos/v1/core/ops/logs/info?logName=nacos.log"
```

**Expected Response / 预期响应:**
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

## Common Use Cases / 常见用例

### Monitor Application Logs / 监控应用日志

```bash
# Check main application log
curl "http://localhost:8848/nacos/v1/core/ops/logs/tail?logName=nacos.log&lines=100"
```

### Debug Configuration Issues / 调试配置问题

```bash
# Check config server logs
curl "http://localhost:8848/nacos/v1/core/ops/logs/tail?logName=config-server.log&lines=200"
```

### Investigate Service Discovery Problems / 调查服务发现问题

```bash
# Check naming server logs
curl "http://localhost:8848/nacos/v1/core/ops/logs/tail?logName=naming-server.log&lines=100"
```

### Review Authentication Issues / 检查认证问题

```bash
# Check auth logs
curl "http://localhost:8848/nacos/v1/core/ops/logs/tail?logName=core-auth.log&lines=50"
```

## Integration Examples / 集成示例

### Shell Script for Log Monitoring / 日志监控Shell脚本

```bash
#!/bin/bash
# monitor-nacos-logs.sh

NACOS_URL="http://localhost:8848"
LOG_FILE="nacos.log"
LINES=100

while true; do
    echo "=== Checking logs at $(date) ==="
    curl -s "$NACOS_URL/nacos/v1/core/ops/logs/tail?logName=$LOG_FILE&lines=$LINES" | \
        jq -r '.data[]' | tail -10
    sleep 30
done
```

### Python Script for Log Analysis / 日志分析Python脚本

```python
#!/usr/bin/env python3
import requests
import time

def check_for_errors(base_url, log_name, keywords):
    """Check log file for error keywords"""
    url = f"{base_url}/nacos/v1/core/ops/logs/tail"
    params = {"logName": log_name, "lines": 500}
    
    response = requests.get(url, params=params)
    if response.status_code == 200:
        logs = response.json()["data"]
        errors = [line for line in logs 
                 if any(kw in line for kw in keywords)]
        return errors
    return []

# Usage
errors = check_for_errors(
    "http://localhost:8848",
    "nacos.log",
    ["ERROR", "FATAL", "Exception"]
)

if errors:
    print(f"Found {len(errors)} error(s):")
    for error in errors:
        print(error)
else:
    print("No errors found")
```

### Cron Job for Daily Log Backup / 每日日志备份Cron任务

```bash
#!/bin/bash
# backup-nacos-logs.sh

NACOS_URL="http://localhost:8848"
BACKUP_DIR="/backup/nacos-logs"
DATE=$(date +%Y%m%d)

# Get list of log files
LOG_FILES=$(curl -s "$NACOS_URL/nacos/v1/core/ops/logs" | jq -r '.data[]')

# Backup each log file
for log in $LOG_FILES; do
    echo "Backing up $log..."
    curl -s "$NACOS_URL/nacos/v1/core/ops/logs/tail?logName=$log&lines=10000" | \
        jq -r '.data[]' > "$BACKUP_DIR/${log%.*}_$DATE.txt"
done

echo "Backup completed to $BACKUP_DIR"
```

Add to crontab:
```
0 2 * * * /path/to/backup-nacos-logs.sh
```

## Troubleshooting / 故障排除

### 403 Forbidden Error

**Problem / 问题:** Cannot access the API

**Solution / 解决方案:** Ensure you have admin credentials and include authentication header

```bash
# Get token first (example)
TOKEN=$(curl -X POST "http://localhost:8848/nacos/v1/auth/login" \
  -d "username=nacos&password=nacos" | jq -r '.accessToken')

# Use token in request
curl -X GET "http://localhost:8848/nacos/v1/core/ops/logs" \
  -H "Authorization: Bearer $TOKEN"
```

### 500 Internal Server Error - File Not Found

**Problem / 问题:** Log file doesn't exist

**Solution / 解决方案:** Check available log files first

```bash
# List available logs
curl "http://localhost:8848/nacos/v1/core/ops/logs"
```

### Invalid File Name Error

**Problem / 问题:** Getting "Invalid file name" error

**Solution / 解决方案:** 
- Only .log files are allowed
- No directory traversal (../) is permitted
- Use exact file name from the list

```bash
# ✓ Correct
curl "http://localhost:8848/nacos/v1/core/ops/logs/tail?logName=nacos.log"

# ✗ Wrong
curl "http://localhost:8848/nacos/v1/core/ops/logs/tail?logName=../nacos.log"
curl "http://localhost:8848/nacos/v1/core/ops/logs/tail?logName=nacos.txt"
```

## Performance Tips / 性能提示

1. **Use tail for recent logs** / **使用tail查看最近日志**
   - Faster than reading entire file
   - More memory efficient

2. **Limit the number of lines** / **限制行数**
   - Default is 500 lines
   - Maximum is 10,000 lines
   - Use smaller values for better performance

3. **Use pagination for large files** / **对大文件使用分页**
   ```bash
   # First page
   curl "http://localhost:8848/nacos/v1/core/ops/logs/content?logName=nacos.log&startLine=1&lineCount=1000"
   
   # Second page
   curl "http://localhost:8848/nacos/v1/core/ops/logs/content?logName=nacos.log&startLine=1001&lineCount=1000"
   ```

## Security Best Practices / 安全最佳实践

1. ✅ Always use authentication in production
2. ✅ Use HTTPS for API calls
3. ✅ Limit access to admin users only
4. ✅ Monitor API access logs
5. ✅ Rotate admin credentials regularly
6. ❌ Don't expose log API to public internet
7. ❌ Don't store admin tokens in code

## Next Steps / 后续步骤

- Read full documentation: `/doc/syslog-api-example.md`
- Integrate with your monitoring system
- Set up automated log analysis
- Configure alerts for error patterns

## Support / 支持

For issues or questions:
- GitHub Issues: https://github.com/alibaba/nacos/issues
- Documentation: https://nacos.io

---

**Note / 注意:** This feature requires Nacos 2.1.0+ with the syslog API patch applied.
