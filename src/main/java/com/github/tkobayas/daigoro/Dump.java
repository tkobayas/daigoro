package com.github.tkobayas.daigoro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Dump {

    private String reportName;

    private List<String> timeStampList = new ArrayList<String>();
    private Map<String, String> timeStampFileNameMap = new HashMap<String, String>();

    private List<String> threadList = new ArrayList<String>();
    private Map<String, String> threadFileNameMap = new HashMap<String, String>();
    private Map<String, ThreadStatus> threadStatusMap = new HashMap<String, ThreadStatus>();

    // key = timeStamp, value = map of stackHolder keyed by thread. This is the genuine holder. Populated through DumpParser.parse()
    private Map<String, Map<String, StackHolder>> stackHolderMapByTimeStamp = new HashMap<String, Map<String, StackHolder>>();

    // key = thread, value = list of stackHolder ordered by timeStamp. Populated by stackHolderMapByTimeStamp
    private Map<String, List<StackHolder>> stackHolderMapByThread = new HashMap<String, List<StackHolder>>();

    // Helper field for matrix rendering. Populated by stackHolderMapByTimeStamp
    private StackHolder[][] stackMatrix;

    // key = timeStamp, value = stackInformation
    private Map<String, List<String>> stackInformationMap = new HashMap<String, List<String>>();

    // key = timeStamp, value = deadlockAnalysis
    private Map<String, List<String>> deadlockAnalysisMap = new HashMap<String, List<String>>();

    public Map<String, ThreadStatus> getThreadStatusMap() {
        return threadStatusMap;
    }

    public void setThreadStatusMap( Map<String, ThreadStatus> threadStatusMap ) {
        this.threadStatusMap = threadStatusMap;
    }

    public Map<String, List<String>> getStackInformationMap() {
        return stackInformationMap;
    }

    public void setStackInformationMap( Map<String, List<String>> stackInformationMap ) {
        this.stackInformationMap = stackInformationMap;
    }

    public Map<String, List<String>> getDeadlockAnalysisMap() {
        return deadlockAnalysisMap;
    }

    public void setDeadlockAnalysisMap( Map<String, List<String>> deadlockAnalysisMap ) {
        this.deadlockAnalysisMap = deadlockAnalysisMap;
    }

    public Map<String, List<StackHolder>> getStackHolderMapByThread() {
        return stackHolderMapByThread;
    }

    public void setStackHolderMapByThread( Map<String, List<StackHolder>> stackHolderMapByThread ) {
        this.stackHolderMapByThread = stackHolderMapByThread;
    }

    public StackHolder[][] getStackMatrix() {
        return stackMatrix;
    }

    public void setStackMatrix( StackHolder[][] stackMatrix ) {
        this.stackMatrix = stackMatrix;
    }

    public Map<String, Map<String, StackHolder>> getStackHolderMapByTimeStamp() {
        return stackHolderMapByTimeStamp;
    }

    public void setStackHolderMapByTimeStamp( Map<String, Map<String, StackHolder>> stackHolderMapByTimeStamp ) {
        this.stackHolderMapByTimeStamp = stackHolderMapByTimeStamp;
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

    public void tidyUp() {
        Collections.sort( timeStampList );
        Collections.sort( threadList );

        // populate stackHolderMapByThread
        for ( String timeStamp : timeStampList ) {
            Map<String, StackHolder> map = stackHolderMapByTimeStamp.get( timeStamp );
            for ( String thread : threadList ) {
                StackHolder stackHolder = map.get( thread );
                if ( stackHolder != null ) {
                    List<StackHolder> list = stackHolderMapByThread.get( thread );
                    if ( list == null ) {
                        list = new ArrayList<StackHolder>();
                        stackHolderMapByThread.put( thread, list );
                    }
                    list.add( stackHolder );
                    Collections.sort( list );
                }
            }
        }

        // populate stackMatrix
        stackMatrix = new StackHolder[timeStampList.size()][threadList.size()];
        for ( int i = 0; i < timeStampList.size(); i++ ) {
            for ( int j = 0; j < threadList.size(); j++ ) {
                String timeStamp = timeStampList.get( i );
                String thread = threadList.get( j );

                Map<String, StackHolder> map = stackHolderMapByTimeStamp.get( timeStamp );
                StackHolder stackHolder = map.get( thread );
                if ( stackHolder == null ) {
                    stackHolder = new StackHolder( timeStamp, thread, Status.ABSENT );
                }
                stackMatrix[i][j] = stackHolder;
            }
        }
    }

    public String getTimeStampFromFileName( String timeStampFileName ) {
        for ( Entry<String, String> entry : timeStampFileNameMap.entrySet() ) {
            if ( entry.getValue().equals( timeStampFileName ) ) {
                return entry.getKey();
            }
        }
        return null;
    }

}
