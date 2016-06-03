package com.github.tkobayas.daigoro;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class DumpAnalyzer {

    // java.lang.Thread.State: RUNNABLE
    private static final Pattern RUNNABLE = Pattern.compile( "^.*: RUNNABLE$" );

    public void analyze( Dump dump ) {
        StackHolder[][] stackMatrix = dump.getStackMatrix();
        Map<String, List<StackHolder>> stackHolderMapByThread = dump.getStackHolderMapByThread();
        List<String> threadList = dump.getThreadList();
        for ( String thread : threadList ) {
            List<StackHolder> list = stackHolderMapByThread.get( thread );
            for ( int i = 0; i < list.size(); i++ ) {
                StackHolder stackHolder = list.get( i );
                if ( isRunning( stackHolder ) ) {
                    stackHolder.setStatus( Status.RUNNING );
                    if ( isSameAsPrevious( stackHolder, list, i ) ) {
                        stackHolder.setStatus( Status.SAME_AS_PREVIOUS );
                    }
                }
            }
        }

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
