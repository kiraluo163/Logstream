package com.test.log.logserver;


import com.test.log.logserver.service.LogHandlerImpl;
import org.jluo.common.LogEntry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Optional;
import java.util.regex.Pattern;


@RunWith(MockitoJUnitRunner.class)
public class LogHandlerTest {

    @InjectMocks
    private LogHandlerImpl logHandler;


    @Before
    public void init(){
        String timeFormat="yyyy-MM-dd HH:mm:ss.SSS";
        String timestampRgx="(?<timestamp>(\\d{4}-\\d{2}-\\d{2}\\s)?\\d{2}:\\d{2}:\\d{2}\\.\\d{3})";
        String levelRgx = "(?<level>INFO|ERROR|WARN|TRACE|DEBUG|FATAL)";
        String classRgx= "(?<class>[^\\]]+)";
        String threadRgx = "\\[(?<thread>[^\\]]+)]";
        String textRgx = "(?<text>.*?)(?=\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}|\\Z)";
        String fullPattern = timestampRgx + "\\s+"+threadRgx+"\\s+"+levelRgx+"\\s+"+classRgx+"-\\s+"+textRgx;
        logHandler.setPatternFullLog(Pattern.compile(fullPattern));
        logHandler.setTimestampRgx(timestampRgx);
        logHandler.setIsSafeMode(true);
        logHandler.setTimeFormat(timeFormat);

    }

    @Test
    public void testTail_returnSize() throws IOException {
        File logFile = ResourceUtils.getFile("classpath:data/testLog.log");
        Assert.assertEquals(10, logHandler.tail(logFile, Optional.of(10), Optional.empty()).size());
        Assert.assertEquals(4, logHandler.tail(logFile, Optional.of(4), Optional.empty()).size());
        Assert.assertEquals(24, logHandler.tail(logFile, Optional.of(100), Optional.empty()).size());
    }

    @Test
    public void testTail_futureTime() throws IOException {
        File logFile = ResourceUtils.getFile("classpath:data/testLog_futureTime.log");
        Assert.assertEquals(2, logHandler.tail(logFile, Optional.of(10), Optional.empty()).size());
    }
}
