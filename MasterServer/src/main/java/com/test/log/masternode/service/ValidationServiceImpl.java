package com.test.log.masternode.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.jluo.common.Constants.MAXIMUM_NUM_OF_EVENTS;

@Service
public class ValidationServiceImpl implements ValidationService{

    @Value("${log.server.cluster}")
    private String[] serverCluster;


    @Override
    public boolean verifyMachine(List<String> machines) {
        Set<String> expected = new HashSet<>();
        for(String server : serverCluster)expected.add(server);
        for(String m : machines){
            if(!expected.contains(m)) return false;
        }
        return true;
    }

    @Override
    public boolean verifyEventNumber(Integer n) {
        return n == null || (n >= 0 && n < MAXIMUM_NUM_OF_EVENTS);
    }


}
