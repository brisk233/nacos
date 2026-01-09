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

package com.alibaba.nacos.core.utils;

import com.alibaba.nacos.sys.env.EnvUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing and retrieving Nacos log files.
 *
 * @author Nacos
 */
@Service
public class LogFileService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LogFileService.class);
    
    private static final String LOGS_DIR = "logs";
    
    /**
     * Get the logs directory path.
     *
     * @return logs directory path
     */
    public String getLogsPath() {
        String nacosHome = EnvUtil.getNacosHome();
        String logsPath = EnvUtil.getProperty("nacos.logs.path");
        if (logsPath != null && !logsPath.isEmpty()) {
            return logsPath;
        }
        return Paths.get(nacosHome, LOGS_DIR).toString();
    }
    
    /**
     * List all available log files.
     *
     * @return list of log file names
     */
    public List<String> listLogFiles() {
        try {
            File logsDir = new File(getLogsPath());
            if (!logsDir.exists() || !logsDir.isDirectory()) {
                LOGGER.warn("Logs directory does not exist: {}", logsDir.getAbsolutePath());
                return Collections.emptyList();
            }
            
            File[] files = logsDir.listFiles((dir, name) -> name.endsWith(".log"));
            if (files == null) {
                return Collections.emptyList();
            }
            
            return Arrays.stream(files)
                    .map(File::getName)
                    .sorted()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error("Failed to list log files", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Get the last N lines from a log file.
     *
     * @param fileName log file name
     * @param lines    number of lines to retrieve
     * @return list of log lines
     * @throws IOException if file reading fails
     */
    public List<String> tailLogFile(String fileName, int lines) throws IOException {
        validateFileName(fileName);
        
        Path logFilePath = Paths.get(getLogsPath(), fileName);
        if (!Files.exists(logFilePath)) {
            throw new IOException("Log file not found: " + fileName);
        }
        
        LinkedList<String> result = new LinkedList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.add(line);
                if (result.size() > lines) {
                    result.removeFirst();
                }
            }
        }
        
        return new ArrayList<>(result);
    }
    
    /**
     * Get log file content with pagination.
     *
     * @param fileName   log file name
     * @param startLine  starting line number (1-based)
     * @param lineCount  number of lines to read
     * @return list of log lines
     * @throws IOException if file reading fails
     */
    public List<String> getLogFileContent(String fileName, int startLine, int lineCount) throws IOException {
        validateFileName(fileName);
        
        Path logFilePath = Paths.get(getLogsPath(), fileName);
        if (!Files.exists(logFilePath)) {
            throw new IOException("Log file not found: " + fileName);
        }
        
        List<String> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath.toFile()))) {
            String line;
            int currentLine = 1;
            
            // Skip lines before startLine
            while (currentLine < startLine && (line = reader.readLine()) != null) {
                currentLine++;
            }
            
            // Read the requested lines
            int linesRead = 0;
            while (linesRead < lineCount && (line = reader.readLine()) != null) {
                result.add(line);
                linesRead++;
            }
        }
        
        return result;
    }
    
    /**
     * Get log file information.
     *
     * @param fileName log file name
     * @return log file metadata
     * @throws IOException if file operation fails
     */
    public LogFileInfo getLogFileInfo(String fileName) throws IOException {
        validateFileName(fileName);
        
        Path logFilePath = Paths.get(getLogsPath(), fileName);
        if (!Files.exists(logFilePath)) {
            throw new IOException("Log file not found: " + fileName);
        }
        
        File file = logFilePath.toFile();
        LogFileInfo info = new LogFileInfo();
        info.setFileName(fileName);
        info.setSize(file.length());
        info.setLastModified(file.lastModified());
        info.setPath(logFilePath.toString());
        
        return info;
    }
    
    /**
     * Validate file name to prevent directory traversal attacks.
     *
     * @param fileName file name to validate
     * @throws IllegalArgumentException if file name is invalid
     */
    private void validateFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty");
        }
        
        // Prevent directory traversal
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new IllegalArgumentException("Invalid file name: " + fileName);
        }
        
        // Only allow .log files
        if (!fileName.endsWith(".log")) {
            throw new IllegalArgumentException("Only .log files are allowed");
        }
    }
    
    /**
     * Log file metadata.
     */
    public static class LogFileInfo {
        
        private String fileName;
        
        private long size;
        
        private long lastModified;
        
        private String path;
        
        public String getFileName() {
            return fileName;
        }
        
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
        
        public long getSize() {
            return size;
        }
        
        public void setSize(long size) {
            this.size = size;
        }
        
        public long getLastModified() {
            return lastModified;
        }
        
        public void setLastModified(long lastModified) {
            this.lastModified = lastModified;
        }
        
        public String getPath() {
            return path;
        }
        
        public void setPath(String path) {
            this.path = path;
        }
    }
}
