package com.github.tkobayas.daigoro;

public class StatusHolder {

    private String timeStamp;

    private String thread;

    private Status status;

    public StatusHolder( String timeStamp, String thread ) {
        this.timeStamp = timeStamp;
        this.thread = thread;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp( String timeStamp ) {
        this.timeStamp = timeStamp;
    }

    public String getThread() {
        return thread;
    }

    public void setThread( String thread ) {
        this.thread = thread;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus( Status status ) {
        this.status = status;
    }

}
