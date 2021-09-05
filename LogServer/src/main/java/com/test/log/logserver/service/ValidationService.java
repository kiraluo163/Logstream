package com.test.log.logserver.service;

public interface ValidationService {

    boolean verifyFileName(String fileName);
    boolean verifyEventNumber(Integer n);
}
