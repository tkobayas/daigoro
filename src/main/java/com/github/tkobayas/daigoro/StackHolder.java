package com.github.tkobayas.daigoro;

import java.util.ArrayList;
import java.util.List;

public class StackHolder implements Comparable<StackHolder> {

    private String timeStamp;

    private String thread;

    private Status status = Status.IDLE;

    private List<String> stack;

    private List<String> holdingLockList = new ArrayList<String>();

    public List<String> getHoldingLockList() {
        return holdingLockList;
    }

    public void setHoldingLockList( List<String> holdingLockList ) {
        this.holdingLockList = holdingLockList;
    }

    public List<String> getStack() {
        return stack;
    }

    public void setStack( List<String> stack ) {
        this.stack = stack;
    }

    public StackHolder( String timeStamp, String thread ) {
        this.timeStamp = timeStamp;
        this.thread = thread;
    }

    public StackHolder( String timeStamp, String thread, Status status ) {
        this.timeStamp = timeStamp;
        this.thread = thread;
        this.status = status;
    }

    public StackHolder( String timeStamp, String thread, List<String> stack ) {
        this.timeStamp = timeStamp;
        this.thread = thread;
        this.stack = stack;
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

    public String getStatusChar() {
        if ( status == Status.SAME_AS_PREVIOUS || status == Status.BLOCKING_SAME_AS_PREVIOUS ) {
            return "<";
        } else {
            return "";
        }
    }

    @Override
    public int compareTo( StackHolder o ) {
        if ( this.timeStamp.compareTo( o.timeStamp ) != 0 ) {
            return this.timeStamp.compareTo( o.timeStamp );
        } else {
            return this.thread.compareTo( o.thread );
        }
    }

}
