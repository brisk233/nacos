/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.core.controller;

import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.auth.common.ActionTypes;
import com.alibaba.nacos.common.model.RestResult;
import com.alibaba.nacos.common.model.RestResultUtils;
import com.alibaba.nacos.core.distributed.ProtocolManager;
import com.alibaba.nacos.core.distributed.id.IdGeneratorManager;
import com.alibaba.nacos.core.utils.Commons;
import com.alibaba.nacos.core.utils.LogFileService;
import com.alibaba.nacos.core.utils.Loggers;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kernel modules operate and maintain HTTP interfaces.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@RestController
@RequestMapping(Commons.NACOS_CORE_CONTEXT + "/ops")
public class CoreOpsController {
    
    private final ProtocolManager protocolManager;
    
    private final IdGeneratorManager idGeneratorManager;
    
    private final LogFileService logFileService;
    
    public CoreOpsController(ProtocolManager protocolManager, IdGeneratorManager idGeneratorManager,
            LogFileService logFileService) {
        this.protocolManager = protocolManager;
        this.idGeneratorManager = idGeneratorManager;
        this.logFileService = logFileService;
    }
    
    // Temporarily overpassed the raft operations interface
    // {
    //      "groupId": "xxx",
    //      "command": "transferLeader or doSnapshot or resetRaftCluster or removePeer"
    //      "value": "ip:{raft_port}"
    // }
    
    @PostMapping(value = "/raft")
    @Secured(action = ActionTypes.WRITE, resource = "nacos/admin")
    public RestResult<String> raftOps(@RequestBody Map<String, String> commands) {
        return protocolManager.getCpProtocol().execute(commands);
    }
    
    /**
     * Gets the current health of the ID generator.
     *
     * @return {@link RestResult}
     */
    @GetMapping(value = "/idInfo")
    public RestResult<Map<String, Map<Object, Object>>> idInfo() {
        Map<String, Map<Object, Object>> info = new HashMap<>(10);
        idGeneratorManager.getGeneratorMap().forEach((resource, idGenerator) -> info.put(resource, idGenerator.info()));
        return RestResultUtils.success(info);
    }
    
    @PutMapping(value = "/log")
    public String setLogLevel(@RequestParam String logName, @RequestParam String logLevel) {
        Loggers.setLogLevel(logName, logLevel);
        return HttpServletResponse.SC_OK + "";
    }
    
    /**
     * List all available log files.
     *
     * @return list of log file names
     */
    @GetMapping(value = "/logs")
    @Secured(action = ActionTypes.READ, resource = "nacos/admin")
    public RestResult<List<String>> listLogFiles() {
        try {
            List<String> logFiles = logFileService.listLogFiles();
            return RestResultUtils.success(logFiles);
        } catch (Exception e) {
            return RestResultUtils.failed("Failed to list log files: " + e.getMessage());
        }
    }
    
    /**
     * Get log file information.
     *
     * @param logName log file name
     * @return log file information
     */
    @GetMapping(value = "/logs/info")
    @Secured(action = ActionTypes.READ, resource = "nacos/admin")
    public RestResult<LogFileService.LogFileInfo> getLogFileInfo(@RequestParam String logName) {
        try {
            LogFileService.LogFileInfo info = logFileService.getLogFileInfo(logName);
            return RestResultUtils.success(info);
        } catch (IOException e) {
            return RestResultUtils.failed("Failed to get log file info: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return RestResultUtils.failed("Invalid log file name: " + e.getMessage());
        }
    }
    
    /**
     * Get the last N lines from a log file.
     *
     * @param logName log file name
     * @param lines   number of lines to retrieve (default: 500, max: 10000)
     * @return log file content
     */
    @GetMapping(value = "/logs/tail")
    @Secured(action = ActionTypes.READ, resource = "nacos/admin")
    public RestResult<List<String>> tailLogFile(@RequestParam String logName,
            @RequestParam(defaultValue = "500") int lines) {
        try {
            // Limit the number of lines to prevent memory issues
            if (lines > 10000) {
                lines = 10000;
            }
            if (lines < 1) {
                lines = 1;
            }
            List<String> content = logFileService.tailLogFile(logName, lines);
            return RestResultUtils.success(content);
        } catch (IOException e) {
            return RestResultUtils.failed("Failed to read log file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return RestResultUtils.failed("Invalid log file name: " + e.getMessage());
        }
    }
    
    /**
     * Get log file content with pagination.
     *
     * @param logName   log file name
     * @param startLine starting line number (1-based, default: 1)
     * @param lineCount number of lines to read (default: 1000, max: 10000)
     * @return log file content
     */
    @GetMapping(value = "/logs/content")
    @Secured(action = ActionTypes.READ, resource = "nacos/admin")
    public RestResult<List<String>> getLogFileContent(@RequestParam String logName,
            @RequestParam(defaultValue = "1") int startLine, @RequestParam(defaultValue = "1000") int lineCount) {
        try {
            // Validate and limit parameters
            if (startLine < 1) {
                startLine = 1;
            }
            if (lineCount > 10000) {
                lineCount = 10000;
            }
            if (lineCount < 1) {
                lineCount = 1;
            }
            List<String> content = logFileService.getLogFileContent(logName, startLine, lineCount);
            return RestResultUtils.success(content);
        } catch (IOException e) {
            return RestResultUtils.failed("Failed to read log file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return RestResultUtils.failed("Invalid log file name: " + e.getMessage());
        }
    }
    
}
