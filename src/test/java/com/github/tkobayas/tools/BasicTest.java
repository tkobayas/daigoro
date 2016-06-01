package com.github.tkobayas.tools;

import java.io.File;

import com.github.tkobayas.daigoro.Daigoro;

import junit.framework.TestCase;

public class BasicTest extends TestCase {

    public void testDeadlock01() {
        Daigoro daigoro = new Daigoro();
        daigoro.createReport( new File( "src/test/resources/deadlock/deadlock01.out" ) );
        assertTrue( true );
    }
}
