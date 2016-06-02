package com.github.tkobayas.daigoro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Analysis {

    private String reportName;

    private List<String> timeStampList = new ArrayList<String>();
    private Map<String, String> timeStampFileNameMap = new HashMap<String, String>();

    private List<String> threadList = new ArrayList<String>();
    private Map<String, String> threadFileNameMap = new HashMap<String, String>();

    private StatusHolder[][] statusMatrix;

    public StatusHolder[][] getStatusMatrix() {
        return statusMatrix;
    }

    public void setStatusMatrix( StatusHolder[][] statusMatrix ) {
        this.statusMatrix = statusMatrix;
    }

    public Analysis() {
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName( String reportName ) {
        this.reportName = reportName;
    }

    public List<String> getTimeStampList() {
        return timeStampList;
    }

    public void setTimeStampList( List<String> timeStampList ) {
        this.timeStampList = timeStampList;
    }

    public Map<String, String> getTimeStampFileNameMap() {
        return timeStampFileNameMap;
    }

    public void setTimeStampFileNameMap( Map<String, String> timeStampFileNameMap ) {
        this.timeStampFileNameMap = timeStampFileNameMap;
    }

    public List<String> getThreadList() {
        return threadList;
    }

    public void setThreadList( List<String> threadList ) {
        this.threadList = threadList;
    }

    public Map<String, String> getThreadFileNameMap() {
        return threadFileNameMap;
    }

    public void setThreadFileNameMap( Map<String, String> threadFileNameMap ) {
        this.threadFileNameMap = threadFileNameMap;
    }

    public void analyze() {
        Collections.sort( threadList );

        statusMatrix = new StatusHolder[timeStampList.size()][threadList.size()];
        for ( int i = 0; i < timeStampList.size(); i++ ) {
            for ( int j = 0; j < threadList.size(); j++ ) {
                StatusHolder statusHolder = new StatusHolder( timeStampList.get( i ), threadList.get( j ) );
                statusHolder.setStatus( Status.IDLE );
                statusMatrix[i][j] = statusHolder;
            }
        }

    }

}
