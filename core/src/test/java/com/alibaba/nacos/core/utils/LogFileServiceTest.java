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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test cases for LogFileService.
 *
 * @author Nacos
 */
public class LogFileServiceTest {
    
    private LogFileService logFileService;
    
    private Path testLogsDir;
    
    private File testLogFile;
    
    @Before
    public void setUp() throws IOException {
        logFileService = new LogFileService();
        
        // Create a temporary directory for test logs
        testLogsDir = Files.createTempDirectory("nacos-test-logs");
        
        // Create a test log file
        testLogFile = new File(testLogsDir.toFile(), "test.log");
        try (FileWriter writer = new FileWriter(testLogFile)) {
            for (int i = 1; i <= 100; i++) {
                writer.write("Log line " + i + "\n");
            }
        }
        
        // Set the test logs path using system property
        System.setProperty("nacos.logs.path", testLogsDir.toString());
    }
    
    @After
    public void tearDown() throws IOException {
        // Clean up test files
        if (testLogFile != null && testLogFile.exists()) {
            testLogFile.delete();
        }
        if (testLogsDir != null && Files.exists(testLogsDir)) {
            Files.delete(testLogsDir);
        }
        System.clearProperty("nacos.logs.path");
    }
    
    @Test
    public void testListLogFiles() {
        List<String> logFiles = logFileService.listLogFiles();
        assertNotNull(logFiles);
        assertTrue(logFiles.contains("test.log"));
    }
    
    @Test
    public void testTailLogFile() throws IOException {
        List<String> lines = logFileService.tailLogFile("test.log", 10);
        assertEquals(10, lines.size());
        assertEquals("Log line 91", lines.get(0));
        assertEquals("Log line 100", lines.get(9));
    }
    
    @Test
    public void testTailLogFileMoreThanAvailable() throws IOException {
        List<String> lines = logFileService.tailLogFile("test.log", 200);
        assertEquals(100, lines.size());
        assertEquals("Log line 1", lines.get(0));
        assertEquals("Log line 100", lines.get(99));
    }
    
    @Test
    public void testGetLogFileContent() throws IOException {
        List<String> lines = logFileService.getLogFileContent("test.log", 1, 10);
        assertEquals(10, lines.size());
        assertEquals("Log line 1", lines.get(0));
        assertEquals("Log line 10", lines.get(9));
    }
    
    @Test
    public void testGetLogFileContentWithStartLine() throws IOException {
        List<String> lines = logFileService.getLogFileContent("test.log", 50, 10);
        assertEquals(10, lines.size());
        assertEquals("Log line 50", lines.get(0));
        assertEquals("Log line 59", lines.get(9));
    }
    
    @Test
    public void testGetLogFileInfo() throws IOException {
        LogFileService.LogFileInfo info = logFileService.getLogFileInfo("test.log");
        assertNotNull(info);
        assertEquals("test.log", info.getFileName());
        assertTrue(info.getSize() > 0);
        assertTrue(info.getLastModified() > 0);
        assertTrue(info.getPath().contains("test.log"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidateFileNameWithDirectoryTraversal() throws IOException {
        logFileService.tailLogFile("../etc/passwd.log", 10);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidateFileNameWithoutLogExtension() throws IOException {
        logFileService.tailLogFile("test.txt", 10);
    }
    
    @Test(expected = IOException.class)
    public void testNonExistentFile() throws IOException {
        logFileService.tailLogFile("nonexistent.log", 10);
    }
}
