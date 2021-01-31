/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kordamp.jreleaser.util;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class FileUtils {
    private FileUtils() {
        //noop
    }

    public static void createDirectoriesWithFullAccess(Path path) throws IOException {
        createDirectories(path, "rwxrwxrwx");
    }

    public static void createDirectories(Path path, String accessRights) throws IOException {
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString(accessRights);
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);
        Files.createDirectories(path, attr);
    }

    public static void grantFullAccess(Path path) throws IOException {
        grantAccess(path, "rwxrwxrwx");
    }

    public static void grantAccess(Path path, String accessRights) throws IOException {
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString(accessRights);
        Files.setPosixFilePermissions(path, perms);
    }

    public static boolean copyFiles(Logger logger, Path source, Path target) throws IOException {
        FileTreeCopy copier = new FileTreeCopy(logger, source, target);
        Files.walkFileTree(source, copier);
        return copier.isSuccessful();
    }

    private static class FileTreeCopy implements FileVisitor<Path> {
        private final Logger logger;
        private final Path source;
        private final Path target;
        private boolean success = true;

        FileTreeCopy(Logger logger, Path source, Path target) {
            this.logger = logger;
            this.source = source;
            this.target = target;
        }

        public boolean isSuccessful() {
            return success;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            Path newdir = target.resolve(source.relativize(dir));
            try {
                Files.copy(dir, newdir);
                FileUtils.grantFullAccess(newdir);
            } catch (FileAlreadyExistsException ignored) {
                // noop
            } catch (IOException e) {
                logger.error("Unable to create: {}", newdir, e);
                success = false;
                return SKIP_SUBTREE;
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            try {
                Path newfile = target.resolve(source.relativize(file));
                Files.copy(file, newfile, REPLACE_EXISTING);
                FileUtils.grantFullAccess(newfile);
            } catch (IOException e) {
                logger.error("Unable to copy: {}", source, e);
                success = false;
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            if (exc == null) {
                Path newdir = target.resolve(source.relativize(dir));
                try {
                    FileTime time = Files.getLastModifiedTime(dir);
                    Files.setLastModifiedTime(newdir, time);
                } catch (IOException e) {
                    logger.warn("Unable to copy all attributes to: {}", newdir, e);
                }
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException e) {
            if (e instanceof FileSystemLoopException) {
                logger.error("cycle detected: {}", file);
            } else {
                logger.error("Unable to copy: {}", file, e);
            }
            success = false;
            return CONTINUE;
        }
    }
}
