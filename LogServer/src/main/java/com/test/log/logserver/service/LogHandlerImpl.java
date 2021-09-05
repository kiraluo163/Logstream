package com.test.log.logserver.service;

import org.jluo.common.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jluo.common.Constants.DEFAULT_NUM_OF_EVENTS;

@Service
public class LogHandlerImpl implements LogHandler {
    private static Logger logger = LoggerFactory.getLogger(LogHandlerImpl.class);

    @Value("${log.regex.timestamp}")
    private String timestampRgx;
    @Value("${log.regex.level}")
    private String levelRgx;
    @Value("${log.regex.class}")
    private String classRgx;
    @Value("${log.regex.thread}")
    private String threadRgx;
    @Value("${log.regex.text}")
    private String textRgx;
    @Value("${log.time.format}")
    private String timeFormat;
    @Value("${log.regex.pattern.full}")
    private String fullPattern;
    @Value("${log.dir}")
    private String logDirectory;

    @Value("${app.safemode}")
    private Boolean isSafeMode;

    private Pattern PatternFullLog;

    private Comparator<LogEntry> logEntryComparator;

    private SimpleDateFormat timeFormatter;

    @PostConstruct
    public void init(){
        PatternFullLog = Pattern.compile(fullPattern, Pattern.DOTALL);
        timeFormatter = new SimpleDateFormat(timeFormat);
        logEntryComparator = Comparator.comparing(e -> {
            try {
                return timeFormatter.parse(e.getTimestamp());
            } catch (ParseException ex) {
                throw new RuntimeException(ex.getMessage());
            }
        });
    }





    @Cacheable("Log")
    @Override
    public List<LogEntry> tail(File logFile, Optional<Integer> n, Optional<String> keyWord) throws ParseException, FileNotFoundException {
        Scanner scan = new Scanner(logFile);
        PriorityQueue<LogEntry> Q = new PriorityQueue<>(logEntryComparator);
        Pattern pattern = Pattern.compile(timestampRgx);
        Date currentTime = new Date(System.currentTimeMillis());
        int maxCap = n.isPresent() ? n.get() : DEFAULT_NUM_OF_EVENTS;
        Matcher matcher;
        while(scan.hasNextLine()){
            String line = scan.nextLine();
            if(keyWord.isPresent() && !line.contains(keyWord.get())) continue;
            matcher = pattern.matcher(line);
            if(matcher.lookingAt()){
                Date logTime = timeFormatter.parse(matcher.group("timestamp"));

                //This is to prevent the thread keep running when log file increased too much faster.
                if(isSafeMode && logTime.after(currentTime)) break;

                LogEntry log = parseSingleLog(line);
                if(log != null) Q.add(log);
                if(Q.size() > maxCap && logEntryComparator.compare(log, Q.peek()) > 0){
                    Q.poll();
                }
            }
        }
        scan.close();
        List<LogEntry> res = new LinkedList<>();
        while(!Q.isEmpty()) res.add(0, Q.poll());
        return res;
    }

    @Override
    public List<LogEntry> search(String keyWord, Optional<Integer> n) throws FileNotFoundException, ParseException {
        File f = new File(logDirectory);
        FilenameFilter filter = (f1, name) -> name.contains("log");
        PriorityQueue<LogEntry> Q = new PriorityQueue<>(logEntryComparator);
        int maxCap = n.isPresent() ? n.get() : DEFAULT_NUM_OF_EVENTS;
        for (File logFile : f.listFiles(filter)) {
            Q.addAll(tail(logFile, n, Optional.of(keyWord)));
            while(Q.size() > maxCap){
                Q.poll();
            }
        }
        List<LogEntry> res = new LinkedList<>();
        while(!Q.isEmpty()) res.add(0, Q.poll());
        return res;
    }

    private LogEntry parseSingleLog(String line){
        Matcher matcher = PatternFullLog.matcher(line);
        if(matcher.find()){
            String timestamp = matcher.group("timestamp");
            String threadName = matcher.group("thread");
            String level = matcher.group("level");
            String className = matcher.group("class");
            String message  = matcher.group("text");
            return new LogEntry(timestamp, level, threadName, className, message);
        }
        return null;
    }


    public void setPatternFullLog(Pattern patternFullLog){this.PatternFullLog = patternFullLog;};

    public void setLogEntryComparator(Comparator<LogEntry> logEntryComparator){this.logEntryComparator = logEntryComparator;};

    public void setTimeFormat (SimpleDateFormat timeFormatter){this.timeFormatter = timeFormatter;}

    public void setTimestampRgx (String timestampRgx){this.timestampRgx = timestampRgx;}

    public void setIsSafeMode (Boolean isSafeMode){this.isSafeMode = isSafeMode;}
}
