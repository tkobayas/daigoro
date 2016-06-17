package com.github.tkobayas.daigoro;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DumpAnalyzer {

    // java.lang.Thread.State: RUNNABLE
    private static final Pattern RUNNABLE = Pattern.compile( "^.*: RUNNABLE.*$" );

    // java.lang.Thread.State: BLOCKED (on object monitor)
    private static final Pattern BLOCKED = Pattern.compile( "^.*: BLOCKED.*$" );

    // java.lang.Thread.State: WAITING (parking)
    // java.lang.Thread.State: TIMED_WAITING (on object monitor)
    private static final Pattern IDLE = Pattern.compile( "^.*: (WAITING|TIMED_WAITING).*$" );

    // - waiting to lock <0x0000000709802518> (a org.example.MySingletonResource)
    private static final Pattern WAITING_TO_LOCK = Pattern.compile( "^.*waiting to lock <(.*)>.*$" );

    // - locked <0x0000000709802518> (a org.example.MySingletonResource)
    private static final Pattern LOCKED = Pattern.compile( "^.*locked <(.*)>.*$" );

    public void analyze( Dump dump ) {
        Map<String, List<StackHolder>> stackHolderMapByThread = dump.getStackHolderMapByThread();
        List<String> threadList = dump.getThreadList();
        HashMap<String, Set<String>> waitLockSetMap = new HashMap<String, Set<String>>(); // per TimeStamp
        for ( String thread : threadList ) {
            List<StackHolder> list = stackHolderMapByThread.get( thread );
            for ( int i = 0; i < list.size(); i++ ) {
                StackHolder stackHolder = list.get( i );
                if ( !waitLockSetMap.containsKey( stackHolder.getTimeStamp() ) ) {
                    waitLockSetMap.put( stackHolder.getTimeStamp(), new HashSet<String>() );
                }
                Set<String> waitLockSet = waitLockSetMap.get( stackHolder.getTimeStamp() );

                if ( isNative( stackHolder ) ) {
                    stackHolder.setStatus( Status.NATIVE );
                } else if ( isRunning( stackHolder ) ) {
                    stackHolder.setStatus( Status.RUNNING );
                    if ( isSameAsPrevious( stackHolder, list, i ) ) {
                        stackHolder.setStatus( Status.SAME_AS_PREVIOUS );
                    }
                } else if ( isBlocked( stackHolder ) ) {
                    String waitLock = getWaitLock( stackHolder );
                    waitLockSet.add( waitLock );
                    stackHolder.setStatus( Status.BLOCKED );
                } else if ( isIdle( stackHolder ) ) {
                    stackHolder.setStatus( Status.IDLE );
                }
                collectHoldingLocks( stackHolder );
            }

            Map<String, ThreadStatus> threadStatusMap = dump.getThreadStatusMap();
            if ( list.get( 0 ).getStatus() == Status.NATIVE ) {
                threadStatusMap.put( thread, ThreadStatus.NATIVE );
            } else if ( isFullyIdle( list ) ) {
                threadStatusMap.put( thread, ThreadStatus.IDLE );
            } else if ( isFullyUnchanged( list ) ) {
                threadStatusMap.put( thread, ThreadStatus.UNCHANGED );
            } else {
                threadStatusMap.put( thread, ThreadStatus.WORKING );
            }
        }

        // re-evaluate the status
        for ( String thread : threadList ) {
            List<StackHolder> list = stackHolderMapByThread.get( thread );
            for ( int i = 0; i < list.size(); i++ ) {
                StackHolder stackHolder = list.get( i );
                if ( stackHolder.getStatus() == Status.RUNNING ) {
                    Set<String> waitLockSet = waitLockSetMap.get( stackHolder.getTimeStamp() );
                    for ( String waitLock : waitLockSet ) {
                        if ( stackHolder.getHoldingLockList().contains( waitLock ) ) {
                            stackHolder.setStatus( Status.BLOCKING );
                        }
                    }
                } else if ( stackHolder.getStatus() == Status.SAME_AS_PREVIOUS ) {
                    Set<String> waitLockSet = waitLockSetMap.get( stackHolder.getTimeStamp() );
                    for ( String waitLock : waitLockSet ) {
                        if ( stackHolder.getHoldingLockList().contains( waitLock ) ) {
                            stackHolder.setStatus( Status.BLOCKING_SAME_AS_PREVIOUS );
                        }
                    }
                }
            }
        }
    }

    private void collectHoldingLocks( StackHolder stackHolder ) {
        List<String> holdingLockList = stackHolder.getHoldingLockList();
        List<String> stack = stackHolder.getStack();
        for ( String line : stack ) {
            Matcher matcher = LOCKED.matcher( line );
            if ( matcher.matches() ) {
                holdingLockList.add( matcher.group( 1 ).trim() );
            }
        }
    }

    private String getWaitLock( StackHolder stackHolder ) {
        String lock = null;
        List<String> stack = stackHolder.getStack();
        for ( String line : stack ) {
            Matcher matcher = WAITING_TO_LOCK.matcher( line );
            if ( matcher.matches() ) {
                lock = matcher.group( 1 ).trim();
                break;
            }
        }
        return lock;
    }

    private boolean isFullyUnchanged( List<StackHolder> list ) {
        if ( list.get( 0 ).getStatus() != Status.RUNNING ) {
            return false;
        }

        if ( list.size() < 2 ) {
            return false;
        }

        for ( int i = 1; i < list.size(); i++ ) {

            if ( list.get( i ).getStatus() != Status.SAME_AS_PREVIOUS ) {
                return false;
            }
        }
        return true;
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
