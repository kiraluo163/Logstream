package com.test.log.masternode.service;


import org.jluo.common.LogEntry;

import java.util.List;
import java.util.Optional;

public interface LogHandler {

    List<LogEntry> searchLog(String keyWord, List<String> machines, Optional<Integer> n);
}
