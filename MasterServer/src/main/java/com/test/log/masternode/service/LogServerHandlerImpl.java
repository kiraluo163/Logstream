package com.test.log.masternode.service;

import org.jluo.common.LogEntry;
import org.jluo.common.RawLogDto;
import org.jluo.common.ResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.jluo.common.Constants.DEFAULT_NUM_OF_EVENTS;

@Service
public class LogServerHandlerImpl implements LogServerHandler {
    private static Logger logger = LoggerFactory.getLogger(LogServerHandlerImpl.class);

    @Value("${log.regex.timestamp}")
    private String timestampRgx;
    @Value("${log.time.format}")
    private String timeFormat;

    private Comparator<String> logEntryComparator;

    private SimpleDateFormat timeFormatter;

    @PostConstruct
    public void init(){
        timeFormatter = new SimpleDateFormat(timeFormat);
        logEntryComparator = Comparator.comparing(e -> {
            try {
                return timeFormatter.parse(e.substring(0,23));
            } catch (ParseException ex) {
                throw new RuntimeException(ex.getMessage());
            }
        });
    }

    @Override
    public List<String> searchLog(String keyWord, List<String> machines, Optional<Integer> n) {
        RestTemplate restTemplate = new RestTemplate();
        List<String> ls = new ArrayList<>();
        int maxCap = n.isPresent() ? n.get() : DEFAULT_NUM_OF_EVENTS;
        for(String machine : machines){
            String resourceUrl = "http://" + machine + "/log/search?keyWord="+keyWord;
            if(n.isPresent()){
                resourceUrl += "&n="+n.get();
            }
            logger.info("remote rpc call: GET - " + resourceUrl);
            ResponseEntity<RawLogDto> entity = restTemplate.getForEntity(resourceUrl, RawLogDto.class );
            RawLogDto response = entity.getBody();
            if(response.getEvents() != null) ls.addAll(response.getEvents());
        }
        Collections.sort(ls, logEntryComparator);
        List<String> sorted = new ArrayList<>();
        for(int i=ls.size()-1; i>=0&&sorted.size()<maxCap; i--){
            sorted.add(ls.get(i));
        }
        return sorted;
    }
}
