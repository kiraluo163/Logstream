package com.test.log.logserver.controller;

import com.test.log.logserver.service.LogHandler;
import com.test.log.logserver.service.ValidationService;
import org.jluo.common.LogEntry;
import org.jluo.common.RawLogDto;
import org.jluo.common.ResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/log")
public class LogController {
    private static Logger logger = LoggerFactory.getLogger(LogController.class);

    @Resource
    private LogHandler logHandler;

    @Resource
    private ValidationService validationService;

    @Value("${log.dir}")
    private String logDirectory;

    //Return last 10 log entry in the specified file.
    @RequestMapping(method = RequestMethod.GET, value="/query")
    public RawLogDto queryLogFile(@RequestParam String fileName,
                                  @RequestParam Optional<Integer> n,
                                  @RequestParam Optional<String> keyWord){
        logger.info("logDirectory = " + logDirectory);

        if(!validationService.verifyFileName(fileName)
        || (n.isPresent() && !validationService.verifyEventNumber(n.get()))){
            return RawLogDto.invalidInputError();
        }
        List<String> logs;
        try{
            logs = logHandler.tail(new File(logDirectory + "/" + fileName), n, keyWord);

        }catch (IOException ex){
            logger.error(ex.getMessage());
            return RawLogDto.fileNotFoundError();
        }
        return new RawLogDto(logs);
    }

    @RequestMapping(method = RequestMethod.GET, value="/search")
    public RawLogDto searchLog(@RequestParam String keyWord, @RequestParam Optional<Integer> n){
        logger.info("logDirectory = " + logDirectory);

        if(n.isPresent() && !validationService.verifyEventNumber(n.get())){
            return RawLogDto.invalidInputError();
        }
        List<String> logs;
        try{
            logs = logHandler.search(keyWord, n);
        }catch (FileNotFoundException ex){
            logger.error(ex.getMessage());
            return RawLogDto.fileNotFoundError();
        }
        return new RawLogDto(logs);
    }

}
