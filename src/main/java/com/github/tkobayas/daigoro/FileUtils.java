package com.github.tkobayas.daigoro;

import java.io.File;

public class FileUtils {

    public static void deleteFile( File file ) {
        if ( file.isDirectory() ) {
            for ( File sub : file.listFiles() ) {
                deleteFile( sub );
            }
        }
        file.delete();
    }
}
