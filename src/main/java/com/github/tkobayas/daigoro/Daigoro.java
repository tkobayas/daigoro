package com.github.tkobayas.daigoro;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;

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

            Template template = cfg.getTemplate( "index.ftl" );
            Map<String, Object> root = new HashMap<String, Object>();
            root.put( "reportName", dump.getReportName() );
            root.put( "timeStampList", dump.getTimeStampList() );
            root.put( "threadList", dump.getThreadList() );
            root.put( "status", dump.getStackMatrix() );
            root.put( "threadStatusMap", dump.getThreadStatusMap() );

            File indexHtml = new File( reportDir, "index.html" );
            PrintWriter writer = new PrintWriter( new BufferedWriter( new FileWriter( indexHtml ) ) );
            template.process( root, writer );
            writer.close();

            FileUtils.copyDir( Paths.get( this.getClass().getResource( "/css" ).toURI() ), new File( reportDir, "css" ).toPath() );

            System.out.println( "Created Report : " );
            System.out.println( indexHtml.getAbsolutePath() );

        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
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
