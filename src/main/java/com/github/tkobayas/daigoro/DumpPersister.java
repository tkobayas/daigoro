package com.github.tkobayas.daigoro;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class DumpPersister {

    private static final String DEADLOCK_ANALYSIS_FILENAME = "deadlock-analysis";
    private static final String STACK_INFORMATION_FILENAME = "stack-information";

    public void persist( Dump dump, File reportDir ) {

        try {
            for ( String timeStamp : dump.getTimeStampList() ) {
                File timeStampDir = createTimeStampDir( timeStamp, reportDir );
                dump.getTimeStampDirNameMap().put( timeStamp, timeStampDir.getName() );

                Map<String, StackHolder> map = dump.getStackHolderMapByTimeStamp().get( timeStamp );
                for ( String thread : map.keySet() ) {
                    StackHolder stackHolder = map.get( thread );
                    File stackFile = createStackFile( stackHolder, timeStampDir );
                    dump.getThreadFileNameMap().put( thread, stackFile.getName() );
                }

                createStackInformationFile( dump.getStackInformationMap().get( timeStamp ), timeStampDir );
                createDeadlockAnalysisFile( dump.getDeadlockAnalysisMap().get( timeStamp ), timeStampDir );
            }
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    private File createTimeStampDir( String timeStamp, File parent ) {
        String timeDirName = normalizeString( timeStamp );
        File timeDir = new File( parent, timeDirName );
        if ( !timeDir.mkdir() ) {
            throw new RuntimeException( "Failed to create timeDirName : " + timeDirName );
        }
        return timeDir;
    }

    private File createStackFile( StackHolder stackHolder, File parent ) throws IOException {
        String stackFileName = normalizeString( stackHolder.getThread() );
        File stackFile = new File( parent, stackFileName );
        PrintWriter writer = new PrintWriter( new BufferedWriter( new FileWriter( stackFile ) ) );
        for ( String line : stackHolder.getStack() ) {
            writer.println( line );
        }
        writer.close();
        return stackFile;
    }

    private void createStackInformationFile( List<String> chunk, File parent ) throws IOException {
        File stackInformationFile = new File( parent, STACK_INFORMATION_FILENAME );
        PrintWriter writer = new PrintWriter( new BufferedWriter( new FileWriter( stackInformationFile ) ) );
        for ( String line : chunk ) {
            writer.println( line );
        }
        writer.close();
    }

    private void createDeadlockAnalysisFile( List<String> chunk, File parent ) throws IOException {
        File deadlockAnalysisFile = new File( parent, DEADLOCK_ANALYSIS_FILENAME );
        PrintWriter writer = new PrintWriter( new BufferedWriter( new FileWriter( deadlockAnalysisFile ) ) );
        for ( String line : chunk ) {
            writer.println( line );
        }
        writer.close();
    }

    private String normalizeString( String string ) {
        return string.replaceAll( "[ :/\"]", "-" );
    }
}
