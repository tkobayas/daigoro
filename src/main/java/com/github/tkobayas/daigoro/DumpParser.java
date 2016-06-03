package com.github.tkobayas.daigoro;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DumpParser {

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

    private Dump dump = new Dump();

    public Dump parse( File dumpFile ) {

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
            while ( br.ready() ) {
                String timeStamp = timeStampChunk.get( 0 ).trim();

                dump.getTimeStampList().add( timeStamp );

                // loop per thread
                while ( br.ready() ) {
                    List<String> chunk = fetchChunk( br );

                    if ( chunk.isEmpty() ) {
                        continue;
                    } else if ( isStack( chunk ) ) {
                        addStack( chunk, timeStamp );
                    } else if ( isJNI( chunk ) ) {
                        continue; // ignore
                    } else if ( isDeadlockAnalysis( chunk ) ) {
                        addStackInformation( chunk, timeStamp );
                    } else if ( isStackInformation( chunk ) ) {
                        addDeadlockAnalysis( chunk, timeStamp );
                    } else if ( isDealLockFound( chunk ) ) {
                        continue; // ignore
                    } else if ( isTimeStamp( chunk ) ) {
                        timeStampChunk = chunk;
                        break;
                    }
                }
            }
        } catch ( FileNotFoundException e ) {
            throw new RuntimeException( e );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }

        dump.tidyUp();

        return dump;
    }

    private void addStack( List<String> chunk, String timeStamp ) {
        String threadName = null;
        Matcher matcher = THREAD_HEADER.matcher( chunk.get( 0 ) );
        if ( matcher.matches() ) {
            threadName = matcher.group( 1 ).trim();
        }

        if ( !dump.getThreadList().contains( threadName ) ) {
            dump.getThreadList().add( threadName );
        }

        StackHolder stackHolder = new StackHolder( timeStamp, threadName, chunk );

        Map<String, Map<String, StackHolder>> stackHolderMap = dump.getStackHolderMapByTimeStamp();
        Map<String, StackHolder> map = stackHolderMap.get( timeStamp );
        if ( map == null ) {
            map = new HashMap<String, StackHolder>();
            stackHolderMap.put( timeStamp, map );
        }
        map.put( threadName, stackHolder );
    }

    private void addStackInformation( List<String> chunk, String timeStamp ) throws IOException {
        dump.getStackInformationMap().put( timeStamp, chunk );
    }

    private void addDeadlockAnalysis( List<String> chunk, String timeStamp ) throws IOException {
        dump.getDeadlockAnalysisMap().put( timeStamp, chunk );
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

    private boolean isStack( List<String> chunk ) throws IOException {
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
}
