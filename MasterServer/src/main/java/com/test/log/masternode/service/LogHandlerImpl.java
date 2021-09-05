package com.test.log.masternode.service;

import org.jluo.common.LogEntry;
import org.jluo.common.ResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.jluo.common.Constants.DEFAULT_NUM_OF_EVENTS;

@Service
public class LogHandlerImpl implements LogHandler{
    private static Logger logger = LoggerFactory.getLogger(LogHandlerImpl.class);

    @Value("${log.regex.timestamp}")
    private String timestampRgx;
    @Value("${log.time.format}")
    private String timeFormat;

    private Comparator<LogEntry> logEntryComparator;

    private SimpleDateFormat timeFormatter;

    @PostConstruct
    public void init(){
        timeFormatter = new SimpleDateFormat(timeFormat);
        logEntryComparator = Comparator.comparing(e -> {
            try {
                return timeFormatter.parse(e.getTimestamp());
            } catch (ParseException ex) {
                throw new RuntimeException(ex.getMessage());
            }
        });
    }

    @Override
    public List<LogEntry> searchLog(String keyWord, List<String> machines, Optional<Integer> n) {
        RestTemplate restTemplate = new RestTemplate();
        PriorityQueue<LogEntry> Q = new PriorityQueue<>(logEntryComparator);
        int maxCap = n.isPresent() ? n.get() : DEFAULT_NUM_OF_EVENTS;
        for(String machine : machines){
            String resourceUrl = "http://" + machine + "/log/search?keyWord="+keyWord;
            if(n.isPresent()){
                resourceUrl += "&n="+n.get();
            }
            logger.info("remote rpc call: GET - " + resourceUrl);
            ResponseEntity<ResponseDto> entity = restTemplate.getForEntity(resourceUrl, ResponseDto.class );
            ResponseDto response = entity.getBody();
            if(response.getEvents() != null) Q.addAll(response.getEvents());
            while(Q.size() > maxCap) {
                Q.poll();
            }
        }
        List<LogEntry> res = new LinkedList<>();
        while(!Q.isEmpty()) res.add(0, Q.poll());
        return res;
    }
}
