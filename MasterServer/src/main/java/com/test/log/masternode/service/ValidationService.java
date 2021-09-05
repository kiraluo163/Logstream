package com.test.log.masternode.service;

import java.util.List;

public interface ValidationService {

    boolean verifyMachine(List<String> machine);
    boolean verifyEventNumber(Integer n);
}
