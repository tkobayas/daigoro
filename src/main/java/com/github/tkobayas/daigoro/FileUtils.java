package com.github.tkobayas.daigoro;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

public class FileUtils {

    public static void deleteFile( File file ) {
        if ( file.isDirectory() ) {
            for ( File sub : file.listFiles() ) {
                deleteFile( sub );
            }
        }
        file.delete();
    }

    public static void copyDir( Path sourceDir, Path targetDir ) throws IOException {

        abstract class MyFileVisitor implements FileVisitor<Path> {
            boolean isFirst = true;
            Path ptr;
        }

        MyFileVisitor copyVisitor = new MyFileVisitor() {

            @Override
            public FileVisitResult preVisitDirectory( Path dir, BasicFileAttributes attrs ) throws IOException {
                // Move ptr forward
                if ( !isFirst ) {
                    // .. but not for the first time since ptr is already in there
                    Path target = ptr.resolve( dir.getName( dir.getNameCount() - 1 ) );
                    ptr = target;
                }
                Files.copy( dir, ptr, StandardCopyOption.COPY_ATTRIBUTES );
                isFirst = false;
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile( Path file, BasicFileAttributes attrs ) throws IOException {
                Path target = ptr.resolve( file.getFileName() );
                Files.copy( file, target, StandardCopyOption.COPY_ATTRIBUTES );
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed( Path file, IOException exc ) throws IOException {
                throw exc;
            }

            @Override
            public FileVisitResult postVisitDirectory( Path dir, IOException exc ) throws IOException {
                Path target = ptr.getParent();
                // Move ptr backwards
                ptr = target;
                return FileVisitResult.CONTINUE;
            }
        };

        copyVisitor.ptr = targetDir;
        Files.walkFileTree( sourceDir, copyVisitor );
    }
}
