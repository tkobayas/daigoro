package com.github.tkobayas.daigoro;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class DumpAnalyzer {

    // java.lang.Thread.State: RUNNABLE
    private static final Pattern RUNNABLE = Pattern.compile( "^.*: RUNNABLE.*$" );

    // java.lang.Thread.State: BLOCKED (on object monitor)
    private static final Pattern BLOCKED = Pattern.compile( "^.*: BLOCKED.*$" );

    // java.lang.Thread.State: WAITING (parking)
    // java.lang.Thread.State: TIMED_WAITING (on object monitor)
    private static final Pattern IDLE = Pattern.compile( "^.*: (WAITING|TIMED_WAITING).*$" );

    public void analyze( Dump dump ) {
        StackHolder[][] stackMatrix = dump.getStackMatrix();
        Map<String, List<StackHolder>> stackHolderMapByThread = dump.getStackHolderMapByThread();
        List<String> threadList = dump.getThreadList();
        for ( String thread : threadList ) {
            List<StackHolder> list = stackHolderMapByThread.get( thread );
            for ( int i = 0; i < list.size(); i++ ) {
                StackHolder stackHolder = list.get( i );
                if ( isNative( stackHolder ) ) {
                    stackHolder.setStatus( Status.NATIVE );
                } else if ( isRunning( stackHolder ) ) {
                    stackHolder.setStatus( Status.RUNNING );
                    if ( isSameAsPrevious( stackHolder, list, i ) ) {
                        stackHolder.setStatus( Status.SAME_AS_PREVIOUS );
                    }
                } else if ( isBlocked( stackHolder ) ) {
                    stackHolder.setStatus( Status.BLOCKED );
                } else if ( isIdle( stackHolder ) ) {
                    stackHolder.setStatus( Status.IDLE );
                }
            }

            Map<String, ThreadStatus> threadStatusMap = dump.getThreadStatusMap();
            if ( list.get( 0 ).getStatus() == Status.NATIVE ) {
                threadStatusMap.put( thread, ThreadStatus.NATIVE );
            } else if ( isFullyIdle( list ) ) {
                threadStatusMap.put( thread, ThreadStatus.IDLE );
            } else {
                threadStatusMap.put( thread, ThreadStatus.WORKING );
            }

        }

    }

    private boolean isFullyIdle( List<StackHolder> list ) {
        for ( StackHolder stackHolder : list ) {
            if ( stackHolder.getStatus() != Status.IDLE ) {
                return false;
            }
        }
        return true;
    }

    private boolean isNative( StackHolder stackHolder ) {
        List<String> stack = stackHolder.getStack();
        if ( stack.size() < 3 ) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isIdle( StackHolder stackHolder ) {
        List<String> stack = stackHolder.getStack();
        String stateLine = stack.get( 1 );
        return IDLE.matcher( stateLine ).matches();
    }

    private boolean isBlocked( StackHolder stackHolder ) {
        List<String> stack = stackHolder.getStack();
        String stateLine = stack.get( 1 );
        return BLOCKED.matcher( stateLine ).matches();
    }

    private boolean isSameAsPrevious( StackHolder stackHolder, List<StackHolder> list, int index ) {
        if ( index == 0 ) {
            return false;
        }

        StackHolder previousStackHolder = list.get( index - 1 );

        List<String> previous = previousStackHolder.getStack();
        List<String> current = stackHolder.getStack();
        if ( previous.size() != current.size() ) {
            return false;
        }

        for ( int i = 0; i < previous.size(); i++ ) {
            if ( !previous.get( i ).equals( current.get( i ) ) ) {
                return false;
            }
        }

        return true;
    }

    private boolean isRunning( StackHolder stackHolder ) {
        List<String> stack = stackHolder.getStack();

        if ( stack.size() == 1 ) {
            return true;
        }

        String stateLine = stack.get( 1 );
        return RUNNABLE.matcher( stateLine ).matches();
    }
}
