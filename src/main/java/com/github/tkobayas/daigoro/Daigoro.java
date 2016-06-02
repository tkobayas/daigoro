package com.github.tkobayas.daigoro;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class Daigoro {

    private static final String DEADLOCK_ANALYSIS_FILENAME = "deadlock-analysis";
    private static final String STACK_INFORMATION_FILENAME = "stack-information";

    // 2016-05-26 18:32:35
    private static final Pattern TIME_STAMP = Pattern.compile( "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$" );

    // "Attach Listener" daemon prio=10 tid=0x00007fef54002800 nid=0x4bba waiting on condition [0x0000000000000000]
    private static final Pattern THREAD_HEADER = Pattern.compile( "^\"(.*)\" (daemon )?prio=\\d+ tid=\\w+ nid=\\w+ .*$" );

    // JNI global references: 272
    private static final Pattern JNI = Pattern.compile( "^JNI global references: \\d*$" );

    // Found one Java-level deadlock:
    private static final Pattern DEADLOCK_ANALYSIS = Pattern.compile( "^Found \\w+ Java-level deadlock:$" );

    // Java stack information for the threads listed above:
    private static final Pattern STACK_INFORMATION = Pattern.compile( "^Java stack information for the threads listed above:$" );

    // Found 1 deadlock.
    private static final Pattern DEADLOCK_FOUND = Pattern.compile( "^Found \\d+ deadlock.$" );

    private Analysis analysis = new Analysis();

    public Daigoro() {
    }

    public void createReport( File dumpFile ) {
        File reportDir = createReportDir( dumpFile.getName() );

        persistReportFragments( dumpFile, reportDir );

        analysis.analyze();

        createReportHTMLs( reportDir );
    }

    private void createReportHTMLs( File reportDir ) {
        try {
            Configuration cfg = new Configuration( Configuration.VERSION_2_3_23 );
            cfg.setClassForTemplateLoading( this.getClass(), "/" );

            Template template = cfg.getTemplate( "index.ftl" );
            Map<String, Object> root = new HashMap<String, Object>();
            root.put( "reportName", analysis.getReportName() );
            root.put( "timeStampList", analysis.getTimeStampList() );
            root.put( "threadList", analysis.getThreadList() );
            root.put( "status", analysis.getStatusMatrix() );

            PrintWriter writer = new PrintWriter( new BufferedWriter( new FileWriter( new File( reportDir, "index.html" ) ) ) );
            template.process( root, writer );
            writer.close();

            FileUtils.copyDir( Paths.get( this.getClass().getResource( "/css" ).toURI() ), new File( reportDir, "css" ).toPath() );

        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    private void persistReportFragments( File dumpFile, File reportDir ) {

        analysis.setReportName( reportDir.getName() );

        try ( BufferedReader br = new BufferedReader( new FileReader( dumpFile ) ) ) {

            List<String> timeStampChunk = null;

            // search the first time stamp
            while ( true ) {
                List<String> firstChunk = fetchChunk( br );
                if ( isTimeStamp( firstChunk ) ) {
                    timeStampChunk = firstChunk;
                    break;
                }
            }
            if ( timeStampChunk == null ) {
                throw new RuntimeException( "Cannot find time stamp" );
            }

            // loop per time stamp
            while ( true ) {

                File timeStampDir = null;
                String timeStamp = timeStampChunk.get( 0 );
                timeStampDir = createTimeStampDir( timeStamp, reportDir );
                analysis.getTimeStampList().add( timeStamp );
                analysis.getTimeStampFileNameMap().put( timeStamp, timeStampDir.getName() );

                // loop per thread
                while ( true ) {
                    List<String> chunk = fetchChunk( br );
                    if ( !br.ready() ) {
                        break;
                    }

                    if ( chunk.isEmpty() ) {
                        continue;
                    } else if ( isThread( chunk ) ) {
                        createThreadFile( chunk, timeStampDir );
                    } else if ( isJNI( chunk ) ) {
                        continue; // ignore
                    } else if ( isDeadlockAnalysis( chunk ) ) {
                        createDeadlockAnalysisFile( chunk, timeStampDir );
                    } else if ( isStackInformation( chunk ) ) {
                        createStackInformationFile( chunk, timeStampDir );
                    } else if ( isDealLockFound( chunk ) ) {
                        continue; // ignore
                    } else if ( isTimeStamp( chunk ) ) {
                        timeStampChunk = chunk;
                        break;
                    }
                }

                if ( !br.ready() ) {
                    break;
                }
            }
        } catch ( FileNotFoundException e ) {
            throw new RuntimeException( e );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }

        System.out.println( "Done!" );
    }

    private boolean isDealLockFound( List<String> chunk ) {
        return DEADLOCK_FOUND.matcher( chunk.get( 0 ) ).matches();
    }

    private boolean isStackInformation( List<String> chunk ) {
        return STACK_INFORMATION.matcher( chunk.get( 0 ) ).matches();
    }

    private boolean isDeadlockAnalysis( List<String> chunk ) {
        return DEADLOCK_ANALYSIS.matcher( chunk.get( 0 ) ).matches();
    }

    private boolean isJNI( List<String> chunk ) {
        return JNI.matcher( chunk.get( 0 ) ).matches();
    }

    private boolean isTimeStamp( List<String> chunk ) throws IOException {
        return TIME_STAMP.matcher( chunk.get( 0 ) ).matches();
    }

    private boolean isThread( List<String> chunk ) throws IOException {
        return THREAD_HEADER.matcher( chunk.get( 0 ) ).matches();
    }

    private List<String> fetchChunk( BufferedReader br ) throws IOException {
        List<String> chunk = new ArrayList<String>();
        while ( br.ready() ) {
            String line = br.readLine();
            if ( line.isEmpty() ) {
                break;
            }
            chunk.add( line );
        }
        return chunk;
    }

    private void createThreadFile( List<String> chunk, File parent ) throws IOException {
        String threadName = null;
        Matcher matcher = THREAD_HEADER.matcher( chunk.get( 0 ) );
        if ( matcher.matches() ) {
            threadName = matcher.group( 1 );
        }

        String threadFileName = normalizeString( threadName );
        File threadFile = new File( parent, threadFileName );
        PrintWriter writer = new PrintWriter( new BufferedWriter( new FileWriter( threadFile ) ) );
        for ( String line : chunk ) {
            writer.println( line );
        }
        writer.close();

        if ( !analysis.getThreadList().contains( threadName ) ) {
            analysis.getThreadList().add( threadName );
            analysis.getThreadFileNameMap().put( threadName, threadFileName );
        }
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

    private File createTimeStampDir( String timeStamp, File parent ) {
        String timeDirName = normalizeString( timeStamp );
        File timeDir = new File( parent, timeDirName );
        if ( !timeDir.mkdir() ) {
            throw new RuntimeException( "Failed to create timeDirName : " + timeDirName );
        }
        return timeDir;
    }

    private File createReportDir( String dumpFileName ) {
        int index = dumpFileName.indexOf( "." );
        String reportDirName = dumpFileName.substring( 0, index > 0 ? index : dumpFileName.length() ) + "-report";
        File reportDir = new File( reportDirName );
        if ( reportDir.exists() ) {
            System.out.println( "Deleting reportDir : " + reportDir );
            FileUtils.deleteFile( reportDir );
        }
        if ( !reportDir.mkdir() ) {
            throw new RuntimeException( "Failed to create reportDir : " + reportDir );
        }

        return reportDir;
    }

    private String normalizeString( String string ) {
        return string.replaceAll( "[ :/\"]", "-" );
    }
}
