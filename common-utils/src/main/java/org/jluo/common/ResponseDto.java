package org.jluo.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ResponseDto {
    @JsonProperty("events")
    private List<LogEntry> events;
    @JsonProperty("error")
    private String error;

    public ResponseDto(){

    }
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
