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
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jluo.common.Constants.DEFAULT_NUM_OF_EVENTS;

@Service
public class LogHandlerImpl implements LogHandler {
    private static Logger logger = LoggerFactory.getLogger(LogHandlerImpl.class);
    private static ExecutorService executor = Executors.newFixedThreadPool(5);

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

    @PostConstruct
    public void init(){
        PatternFullLog = Pattern.compile(fullPattern, Pattern.DOTALL);


    }

    class HelperTool{
        SimpleDateFormat timeFormatter;
        Comparator<LogEntry> logEntryComparator;
        HelperTool(SimpleDateFormat timeFormatter, Comparator<LogEntry> logEntryComparator){
            this.timeFormatter = timeFormatter;
            this.logEntryComparator = logEntryComparator;
        }
    }

    private HelperTool createHelperTool(){
        SimpleDateFormat timeFormatter = new SimpleDateFormat(timeFormat);
        Comparator<LogEntry> logEntryComparator = Comparator.comparing(e -> {
            try {
                return timeFormatter.parse(e.getTimestamp());
            } catch (ParseException ex) {
                throw new RuntimeException(ex.getMessage());
            }
        });
        return new HelperTool(timeFormatter, logEntryComparator);
    }

    @Cacheable("Log")
    @Override
    public List<LogEntry> tail(File logFile, Optional<Integer> n, Optional<String> keyWord) throws ParseException, FileNotFoundException {

        HelperTool tool = createHelperTool();
        Scanner scan = new Scanner(logFile);
        PriorityQueue<LogEntry> Q = new PriorityQueue<>(tool.logEntryComparator);
        Pattern pattern = Pattern.compile(timestampRgx);
        Date currentTime = new Date(System.currentTimeMillis());
        int maxCap = n.isPresent() ? n.get() : DEFAULT_NUM_OF_EVENTS;
        Matcher matcher;
        while(scan.hasNextLine()){
            String line = scan.nextLine();
            if(keyWord.isPresent() && !line.contains(keyWord.get())) continue;
            matcher = pattern.matcher(line);
            if(matcher.lookingAt()){
                Date logTime = tool.timeFormatter.parse(matcher.group("timestamp"));
                //This is to prevent the thread keep running when log file increased too much faster.
                if(isSafeMode && logTime.after(currentTime)) break;

                LogEntry log = parseSingleLog(line);
                if(log != null) Q.add(log);
                if(Q.size() > maxCap && tool.logEntryComparator.compare(log, Q.peek()) > 0){
                    Q.poll();
                }
            }
        }
        scan.close();
        List<LogEntry> res = new LinkedList<>();
        while(!Q.isEmpty()) res.add(0, Q.poll());
        System.out.println("res size= " +res.size());
        return res;
    }

    @Override
    public List<LogEntry> search(String keyWord, Optional<Integer> n) throws FileNotFoundException, ParseException {
        File f = new File(logDirectory);
        HelperTool tool = createHelperTool();
        FilenameFilter filter = (f1, name) -> name.contains("log");
        PriorityQueue<LogEntry> Q = new PriorityQueue<>(tool.logEntryComparator);
        int maxCap = n.isPresent() ? n.get() : DEFAULT_NUM_OF_EVENTS;

        //create thread pool to facilitate the multithreading in order to boost the performance.
        List<Future<List<LogEntry>>> futures = new LinkedList<>();
        for (File logFile : f.listFiles(filter)) {
            Callable<List<LogEntry>> task = () -> tail(logFile, n, Optional.of(keyWord));
            futures.add(executor.submit(task));

        }
        for(Future<List<LogEntry>> future : futures){
            try {
                Q.addAll(future.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        //wait until all the thread is done.
        while(true){
            boolean isDone = true;
            for(Future<List<LogEntry>> future : futures){
                if(!future.isDone()){
                    isDone = false;
                    break;
                }
            }
            if(isDone){
                break;
            }
        }
        List<LogEntry> res = new LinkedList<>();
        while(Q.size() > maxCap) Q.poll();
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

    public void setTimestampRgx (String timestampRgx){this.timestampRgx = timestampRgx;}

    public void setIsSafeMode (Boolean isSafeMode){this.isSafeMode = isSafeMode;}

    public void setTimeFormat(String timeFormat){this.timeFormat = timeFormat;};
}
