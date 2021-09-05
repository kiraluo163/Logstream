package com.test.log.logserver.service;

import com.test.log.logserver.domain.LogEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

public interface LogHandler {


    /*Like the tail command in linux. Return last n log entry order by timestamp*/
    List<LogEntry> tail(File logFile, Optional<Integer> n, Optional<String> keyWord) throws ParseException, FileNotFoundException;
}
