package com.github.tkobayas.daigoro;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {

    private static Options options;

    static {
        options = new Options();
        options.addOption( "h", "help", false, "help" );
    }

    public static void main( String[] args ) {
        CommandLine cmd = parseOptions( args );

        if ( cmd == null || cmd.hasOption( "help" ) || cmd.getArgList().size() != 1 ) {
            usageAndExit( options );
        }

        String dumpFileName = cmd.getArgList().get( 0 );
        File dumpFile = new File( dumpFileName );
        
        Daigoro daigoro = new Daigoro();
        daigoro.createReport(dumpFile);

        System.out.println( "Hello Daigoro! : " + cmd.getArgList() );
    }

    private static final CommandLine parseOptions( String[] args ) {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse( options, args );
        } catch ( ParseException pe ) {
            usageAndExit( options );
        }
        return cmd;
    }

    private static void usageAndExit( Options options ) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "daigoro [OPTION]... [FILE]", options );
        System.out.println();
        System.out.println( "  [FILE] is one file which contains a series of thread dumps" );
        System.exit( 0 );
    }

}
