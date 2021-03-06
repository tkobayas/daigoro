package com.github.tkobayas.daigoro;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

public class Daigoro {

    public Daigoro() {
    }

    public void createReport( File dumpFile ) {

        DumpParser parser = new DumpParser();
        Dump dump = parser.parse( dumpFile );

        File reportDir = createReportDir( dumpFile.getName() );
        dump.setReportName( reportDir.getName() );

        DumpPersister persister = new DumpPersister();
        persister.persist( dump, reportDir );

        DumpAnalyzer analyzer = new DumpAnalyzer();
        analyzer.analyze( dump );

        createReportHTMLs( dump, reportDir );
    }

    private void createReportHTMLs( Dump dump, File reportDir ) {
        try {
            Configuration cfg = new Configuration( Configuration.VERSION_2_3_23 );
            cfg.setClassForTemplateLoading( this.getClass(), "/" );

            // thread html
            createThreadHTMLs( dump, reportDir, cfg );

            // timestamp html
            createTimeStampHTMLs( dump, reportDir, cfg );

            // index.html
            File indexHtml = createReportIndexHTML( dump, reportDir, cfg );

            // css
            File cssDir = new File( reportDir, "css" );
            cssDir.mkdir();
            Files.copy( this.getClass().getResourceAsStream( "/css/daigoro.css" ), new File( cssDir, "daigoro.css" ).toPath() );
            Files.copy( this.getClass().getResourceAsStream( "/css/foundation.css" ), new File( cssDir, "foundation.css" ).toPath() );

            System.out.println( "Created Report : " );
            System.out.println( indexHtml.getAbsolutePath() );

        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    private void createTimeStampHTMLs( Dump dump, File reportDir, Configuration cfg ) throws TemplateException, IOException {
        List<String> timeStampList = dump.getTimeStampList();

        for ( String timeStamp : timeStampList ) {
            String timeStampDirName = dump.getTimeStampDirNameMap().get( timeStamp );

            Template template = cfg.getTemplate( "timestamp.ftl" );
            Map<String, Object> root = new HashMap<String, Object>();
            root.put( "reportName", dump.getReportName() );
            root.put( "timeStamp", timeStamp );
            root.put( "timeStampDirName", timeStampDirName );

            root.put( "threadList", dump.getThreadList() );
            root.put( "threadFileNameMap", dump.getThreadFileNameMap() );

            File timeStampHtml = new File( reportDir, timeStampDirName + ".html" );
            PrintWriter writer = new PrintWriter( new BufferedWriter( new FileWriter( timeStampHtml ) ) );
            template.process( root, writer );
            writer.close();
        }
    }

    private void createThreadHTMLs( Dump dump, File reportDir, Configuration cfg ) throws TemplateNotFoundException, MalformedTemplateNameException,
            ParseException, IOException, TemplateException {
        List<String> threadList = dump.getThreadList();

        for ( String threadName : threadList ) {
            String threadFileName = dump.getThreadFileNameMap().get( threadName );

            Template template = cfg.getTemplate( "thread.ftl" );
            Map<String, Object> root = new HashMap<String, Object>();
            root.put( "reportName", dump.getReportName() );
            root.put( "threadName", threadName );
            root.put( "threadFileName", threadFileName );

            root.put( "timeStampList", dump.getTimeStampList() );
            root.put( "timeStampDirNameMap", dump.getTimeStampDirNameMap() );

            File threadHtml = new File( reportDir, threadFileName + ".html" );
            PrintWriter writer = new PrintWriter( new BufferedWriter( new FileWriter( threadHtml ) ) );
            template.process( root, writer );
            writer.close();
        }
    }

    private File createReportIndexHTML( Dump dump, File reportDir, Configuration cfg ) throws TemplateNotFoundException, MalformedTemplateNameException,
            ParseException, IOException, TemplateException {
        Template template = cfg.getTemplate( "index.ftl" );
        Map<String, Object> root = new HashMap<String, Object>();
        root.put( "reportName", dump.getReportName() );
        root.put( "timeStampList", dump.getTimeStampList() );
        root.put( "threadList", dump.getThreadList() );
        root.put( "status", dump.getStackMatrix() );
        root.put( "threadStatusMap", dump.getThreadStatusMap() );
        root.put( "threadFileNameMap", dump.getThreadFileNameMap() );
        root.put( "timeStampDirNameMap", dump.getTimeStampDirNameMap() );

        HashMap<String, String> timeMap = new HashMap<String, String>();
        for ( String timeStamp : dump.getTimeStampList() ) {
            timeMap.put( timeStamp, timeStamp.substring( timeStamp.length() - 8 ) );
        }
        root.put( "timeMap", timeMap );

        File indexHtml = new File( reportDir, "index.html" );
        PrintWriter writer = new PrintWriter( new BufferedWriter( new FileWriter( indexHtml ) ) );
        template.process( root, writer );
        writer.close();
        return indexHtml;
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

}
