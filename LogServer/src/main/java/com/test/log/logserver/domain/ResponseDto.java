package com.test.log.logserver.domain;

import java.util.List;

public class ResponseDto {

    private List<LogEntry> events;
    private String error;

    public ResponseDto(String error){
        this.error = error;
    }

    public ResponseDto(List<LogEntry> events) {
        this.events = events;
    }

    public List<LogEntry> getEvents() {
        return events;
    }

    public void setEvents(List<LogEntry> events) {
        this.events = events;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public static ResponseDto fileNotFoundError(){
        return new ResponseDto("File not found!");
    }

    public static ResponseDto logFormatError(){
        return new ResponseDto("Log format is wrong, no match found!");
    }

    public static ResponseDto invalidInputError() { return new ResponseDto("Invalid Input!");}
}
