package org.jluo.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class RawLogDto {
    @JsonProperty("events")
    private List<String> events;
    @JsonProperty("error")
    private String error;

    public RawLogDto(){

    }
    public RawLogDto(String error){
        this.error = error;
    }

    public RawLogDto(List<String> events) {
        this.events = events;
    }

    public List<String> getEvents() {
        return events;
    }

    public void setEvents(List<String> events) {
        this.events = events;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public static RawLogDto fileNotFoundError(){
        return new RawLogDto("File not found!");
    }

    public static RawLogDto invalidInputError() { return new RawLogDto("Invalid Input!");}
}
