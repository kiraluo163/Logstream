package com.test.log.masternode.controller;

import com.test.log.masternode.service.LogServerHandler;
import com.test.log.masternode.service.ValidationService;
import org.jluo.common.LogEntry;
import org.jluo.common.RawLogDto;
import org.jluo.common.ResponseDto;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/master")
public class LogServerController {

    @Resource
    private ValidationService validationService;

    @Resource
    private LogServerHandler logServerHandler;

    @RequestMapping(method = RequestMethod.GET, value="/search")
    public RawLogDto searchLog(@RequestParam String keyWord, @RequestParam List<String> machines, @RequestParam Optional<Integer> n){
        if(n.isPresent() && !validationService.verifyEventNumber(n.get())){
            return RawLogDto.invalidInputError();
        }
        if(!validationService.verifyMachine(machines)){
            return RawLogDto.invalidInputError();
        }
        List<String> logs = logServerHandler.searchLog(keyWord, machines,n);
        return new RawLogDto(logs);
    }


}
