package com.test.log.logserver.service;

import com.test.log.logserver.utils.ReversedLineInputStream;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.jluo.common.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.Charset;
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
        Comparator<String> logEntryComparator;
        HelperTool(SimpleDateFormat timeFormatter, Comparator<String> logEntryComparator){
            this.timeFormatter = timeFormatter;
            this.logEntryComparator = logEntryComparator;
        }
    }

    private HelperTool createHelperTool(){
        SimpleDateFormat timeFormatter = new SimpleDateFormat(timeFormat);
        Comparator<String> logEntryComparator = Comparator.comparing(e -> {
            try {
                //Fetch the timestamp string, assume the log format always start from 23 characters time string
                return timeFormatter.parse(e.substring(0,23));
            } catch (ParseException ex) {
                throw new RuntimeException(ex.getMessage());
            }
        });
        return new HelperTool(timeFormatter, logEntryComparator);
    }

    //@Cacheable("Log")
    @Override
    public List<String> tail(File logFile, Optional<Integer> n, Optional<String> keyWord) throws IOException {
        Deque<String> Q = new LinkedList<>();
        int maxCap = n.isPresent() ? n.get() : DEFAULT_NUM_OF_EVENTS;
        BufferedReader in = new BufferedReader (new InputStreamReader (new ReversedLineInputStream(logFile)));
        String line;
        while((line = in.readLine()) != null && Q.size() < maxCap) {
            Q.add(line);
        }
        return new LinkedList<>(Q);
    }

    @Override
    public List<String> search(String keyWord, Optional<Integer> n) throws FileNotFoundException {
        File f = new File(logDirectory);

        FilenameFilter filter = (f1, name) -> name.contains("log");
        int maxCap = n.isPresent() ? n.get() : DEFAULT_NUM_OF_EVENTS;
        List<String> ls = new ArrayList<>();
        //create thread pool to facilitate the multithreading in order to boost the performance.
        List<Future<List<String>>> futures = new LinkedList<>();
        for (File logFile : f.listFiles(filter)) {
            Callable<List<String>> task = () -> tail(logFile, n, Optional.of(keyWord));
            futures.add(executor.submit(task));

        }
        for(Future<List<String>> future : futures){
            try {
                synchronized (ls){
                    ls.addAll(future.get());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        HelperTool tool = createHelperTool();
        Collections.sort(ls, tool.logEntryComparator);
        List<String> sorted = new ArrayList<>();
        for(int i=ls.size()-1; i>=0 && sorted.size() < maxCap; i--){
            sorted.add(ls.get(i));
        }
        return sorted;
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
