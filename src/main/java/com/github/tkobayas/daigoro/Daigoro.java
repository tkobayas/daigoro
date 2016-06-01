package com.github.tkobayas.daigoro;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Daigoro {

    private static final Pattern TIME_STAMP = Pattern.compile( "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}" );

    public void createReport( File dumpFile ) {
        File reportDir = createReportDir( dumpFile.getName() );

        try ( BufferedReader br = new BufferedReader( new FileReader( dumpFile ) ) ) {
            String timeStamp = readTimeStamp( br );
            System.out.println( timeStamp );
        } catch ( FileNotFoundException e ) {
            throw new RuntimeException( e );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    private String readTimeStamp( BufferedReader br ) throws IOException {
        while ( br.ready() ) {
            String line = br.readLine();
            Matcher matcher = TIME_STAMP.matcher( line );
            if ( matcher.matches() ) {
                return line;
            }
        }

        throw new RuntimeException( "Cannot find time stamp" );
    }

    private File createReportDir( String dumpFileName ) {
        int index = dumpFileName.indexOf( "." );
        String reportDirName = dumpFileName.substring( 0, index > 0 ? index : dumpFileName.length() ) + "-report";
        File reportDir = new File( reportDirName );
        if ( reportDir.exists() ) {
            System.out.println( "Deleting reportDir : " + reportDir );
            reportDir.delete();
        }
        if ( !reportDir.mkdir() ) {
            throw new RuntimeException( "Failed to create reportDir : " + reportDir );
        }

        return reportDir;
    }

}
