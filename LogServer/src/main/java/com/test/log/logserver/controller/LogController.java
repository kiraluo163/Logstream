package com.test.log.logserver.controller;

import com.test.log.logserver.domain.LogEntry;
import com.test.log.logserver.domain.ResponseDto;
import com.test.log.logserver.service.LogHandler;
import com.test.log.logserver.service.ValidationService;
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
    public ResponseDto queryLogFile(@RequestParam String fileName,
                                    @RequestParam Optional<Integer> n,
                                    @RequestParam Optional<String> keyWord){
        logger.info("logDirectory = " + logDirectory);

        if(!validationService.verifyFileName(fileName)
        || (n.isPresent() && !validationService.verifyEventNumber(n.get()))){
            return ResponseDto.invalidInputError();
        }
        List<LogEntry> logs;
        try{
            logs = logHandler.tail(new File(logDirectory + "/" + fileName), n, keyWord);
        }catch (FileNotFoundException ex){
            logger.error(ex.getMessage());
            return ResponseDto.fileNotFoundError();
        }catch (ParseException ex){
            logger.error(ex.getMessage());
            return ResponseDto.logFormatError();
        }
        return new ResponseDto(logs);
    }

}
