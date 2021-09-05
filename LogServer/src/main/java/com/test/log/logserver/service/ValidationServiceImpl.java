package com.test.log.logserver.service;

import org.springframework.stereotype.Service;
import static org.jluo.common.Constants.MAXIMUM_NUM_OF_EVENTS;

@Service
public class ValidationServiceImpl implements ValidationService {


    @Override
    public boolean verifyFileName(String fileName) {
        return fileName.matches("^[_.A-Za-z0-9]+$");
    }

    @Override
    public boolean verifyEventNumber(Integer n) {
        return n == null || (n >= 0 && n < MAXIMUM_NUM_OF_EVENTS);
    }
}
