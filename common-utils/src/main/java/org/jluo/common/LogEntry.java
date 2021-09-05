package org.jluo.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LogEntry {
    private String timestamp;
    private String level;
    private String threadName;
    private String className;
    private String message;

    public LogEntry(){

    }
    public LogEntry(String timestamp, String level, String threadName, String className, String message) {
        this.timestamp = timestamp;
        this.level = level;
        this.threadName = threadName;
        this.className = className;
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString(){
        return "timestamp: " + timestamp + " level: " + level + " threadName: " + threadName;
    }
}
