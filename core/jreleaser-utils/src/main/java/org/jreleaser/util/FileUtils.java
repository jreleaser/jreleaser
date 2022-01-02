/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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
package org.jreleaser.util;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.jreleaser.bundle.RB;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.zip.ZipOutputStream;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static org.jreleaser.util.FileType.TAR_BZ2;
import static org.jreleaser.util.FileType.TAR_GZ;
import static org.jreleaser.util.FileType.TAR_XZ;
import static org.jreleaser.util.FileType.TBZ2;
import static org.jreleaser.util.FileType.TGZ;
import static org.jreleaser.util.FileType.TXZ;
import static org.jreleaser.util.StringUtils.getFilename;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class FileUtils {
    private static final String[] LICENSE_FILE_NAMES = {
        "LICENSE",
        "LICENSE.txt",
        "LICENSE.md",
        "LICENSE.adoc"
    };

    private static final String[] TAR_COMPRESSED_EXTENSIONS = {
        TAR_BZ2.extension(),
        TAR_GZ.extension(),
        TAR_XZ.extension(),
        TBZ2.extension(),
        TGZ.extension(),
        TXZ.extension()
    };

    private FileUtils() {
        //noop
    }

    public static Optional<Path> findLicenseFile(Path basedir) {
        for (String licenseFilename : Arrays.asList(LICENSE_FILE_NAMES)) {
            Path path = basedir.resolve(licenseFilename);
            if (Files.exists(path)) {
                return Optional.of(path);
            }
            path = basedir.resolve(licenseFilename.toLowerCase());
            if (Files.exists(path)) {
                return Optional.of(path);
            }
        }

        return Optional.empty();
    }

    public static Path resolveOutputDirectory(Path basedir, Path outputdir, String baseOutput) {
        String od = Env.resolve("OUTPUT_DIRECTORY", "");
        if (isNotBlank(od)) {
            return basedir.resolve(od).resolve("jreleaser");
        }
        if (null != outputdir) {
            return basedir.resolve(outputdir).resolve("jreleaser");
        }
        return basedir.resolve(baseOutput).resolve("jreleaser");
    }

    public static void zip(Path src, Path dest) throws IOException {
        try (ZipArchiveOutputStream out = new ZipArchiveOutputStream(dest.toFile())) {
            out.setMethod(ZipOutputStream.DEFLATED);

            Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    String entryName = src.relativize(file).toString();
                    File inputFile = file.toFile();
                    ZipArchiveEntry archiveEntry = new ZipArchiveEntry(inputFile, entryName);

                    archiveEntry.setMethod(ZipOutputStream.DEFLATED);
                    if (inputFile.isFile() && Files.isExecutable(file)) {
                        archiveEntry.setUnixMode(0100755);
                    }

                    out.putArchiveEntry(archiveEntry);

                    if (inputFile.isFile()) {
                        out.write(Files.readAllBytes(file));
                    }
                    out.closeArchiveEntry();

                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    public static void tar(Path src, Path dest) throws IOException {
        try (TarArchiveOutputStream out = new TarArchiveOutputStream(
            Files.newOutputStream(dest, CREATE, TRUNCATE_EXISTING))) {
            tar(src, out);
        }
    }

    public static void tgz(Path src, Path dest) throws IOException {
        try (TarArchiveOutputStream out = new TarArchiveOutputStream(
            new GzipCompressorOutputStream(Files.newOutputStream(dest, CREATE, TRUNCATE_EXISTING)))) {
            tar(src, out);
        }
    }

    public static void bz2(Path src, Path dest) throws IOException {
        try (TarArchiveOutputStream out = new TarArchiveOutputStream(
            new BZip2CompressorOutputStream(Files.newOutputStream(dest, CREATE, TRUNCATE_EXISTING)))) {
            tar(src, out);
        }
    }

    public static void xz(Path src, Path dest) throws IOException {
        try (TarArchiveOutputStream out = new TarArchiveOutputStream(
            new XZCompressorOutputStream(Files.newOutputStream(dest, CREATE, TRUNCATE_EXISTING)))) {
            tar(src, out);
        }
    }

    private static void tar(Path src, TarArchiveOutputStream out) throws IOException {
        out.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                String entryName = src.relativize(file).toString();
                File inputFile = file.toFile();
                TarArchiveEntry archiveEntry = (TarArchiveEntry) out.createArchiveEntry(inputFile, entryName);

                if (inputFile.isFile() && Files.isExecutable(file)) {
                    archiveEntry.setMode(0100755);
                }

                out.putArchiveEntry(archiveEntry);

                if (inputFile.isFile()) {
                    out.write(Files.readAllBytes(file));
                }

                out.closeArchiveEntry();

                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void unpackArchive(Path src, Path dest) throws IOException {
        String filename = src.getFileName().toString();
        for (String extension : TAR_COMPRESSED_EXTENSIONS) {
            if (filename.endsWith(extension)) {
                unpackArchiveCompressed(src, dest);
                return;
            }
        }

        deleteFiles(dest, true);
        File destinationDir = dest.toFile();

        try (InputStream fi = Files.newInputStream(src);
             InputStream bi = new BufferedInputStream(fi);
             ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream(bi)) {

            // subtract .zip, .tar
            filename = filename.substring(0, filename.length() - 4);
            unpackArchive(filename + "/", destinationDir, in);
        } catch (ArchiveException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    public static void unpackArchiveCompressed(Path src, Path dest) throws IOException {
        deleteFiles(dest, true);
        File destinationDir = dest.toFile();

        String filename = src.getFileName().toString();
        String artifactFileName = getFilename(filename, FileType.getSupportedExtensions());
        String artifactExtension = artifactFileName.substring(artifactFileName.length() + 1);
        String artifactFileFormat = artifactExtension.substring(1);
        FileType fileType = FileType.of(artifactFileFormat);

        try (InputStream fi = Files.newInputStream(src);
             InputStream bi = new BufferedInputStream(fi);
             InputStream gzi = resolveCompressorInputStream(fileType, bi);
             ArchiveInputStream in = new TarArchiveInputStream(gzi)) {
            // subtract extension
            int offset = fileType.extension().length();
            filename = filename.substring(0, filename.length() - offset);
            unpackArchive(filename + "/", destinationDir, in);
        }
    }

    private static InputStream resolveCompressorInputStream(FileType fileType, InputStream in) throws IOException {
        switch (fileType) {
            case TGZ:
            case TAR_GZ:
                return new GzipCompressorInputStream(in);
            case TBZ2:
            case TAR_BZ2:
                return new BZip2CompressorInputStream(in);
            case TXZ:
            case TAR_XZ:
                return new XZCompressorInputStream(in);
        }

        return null;
    }

    private static void unpackArchive(String basename, File destinationDir, ArchiveInputStream in) throws IOException {
        ArchiveEntry entry = null;
        while ((entry = in.getNextEntry()) != null) {
            if (!in.canReadEntryData(entry)) {
                // log something?
                continue;
            }

            String entryName = entry.getName();
            if (entryName.startsWith(basename) && entryName.length() > basename.length() + 1) {
                entryName = entryName.substring(basename.length());
            }

            File file = new File(destinationDir, entryName);
            String destDirPath = destinationDir.getCanonicalPath();
            String destFilePath = file.getCanonicalPath();
            if (!destFilePath.startsWith(destDirPath + File.separator)) {
                throw new IOException(RB.$("ERROR_files_unpack_outside_target", entry.getName()));
            }

            if (entry.isDirectory()) {
                if (!file.isDirectory() && !file.mkdirs()) {
                    throw new IOException(RB.$("ERROR_files_unpack_fail_dir", file));
                }
            } else {
                File parent = file.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException(RB.$("ERROR_files_unpack_fail_dir", parent));
                }
                try (OutputStream o = Files.newOutputStream(file.toPath())) {
                    IOUtils.copy(in, o);
                    // TODO: make it a generic solution
                    // zipEntry.unixMode returns 0 most times even if the entry is executable
                    // https://github.com/jreleaser/jreleaser/issues/358
                    if ("bin".equalsIgnoreCase(file.getParentFile().getName())) {
                        grantExecutableAccess(file.toPath());
                    }
                }
            }
        }
    }

    public static List<String> inspectArchive(Path src) throws IOException {
        String filename = src.getFileName().toString();
        for (String extension : TAR_COMPRESSED_EXTENSIONS) {
            if (filename.endsWith(extension)) {
                return inspectArchiveCompressed(src);
            }
        }

        try (InputStream fi = Files.newInputStream(src);
             InputStream bi = new BufferedInputStream(fi);
             ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream(bi)) {
            return inspectArchive(in);
        } catch (ArchiveException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    public static List<String> inspectArchiveCompressed(Path src) throws IOException {
        String filename = src.getFileName().toString();
        String artifactFileName = getFilename(filename, FileType.getSupportedExtensions());
        String artifactExtension = artifactFileName.substring(artifactFileName.length() + 1);
        String artifactFileFormat = artifactExtension.substring(1);
        FileType fileType = FileType.of(artifactFileFormat);

        try (InputStream fi = Files.newInputStream(src);
             InputStream bi = new BufferedInputStream(fi);
             InputStream gzi = resolveCompressorInputStream(fileType, bi);
             ArchiveInputStream in = new TarArchiveInputStream(gzi)) {
            return inspectArchive(in);
        }
    }

    private static List<String> inspectArchive(ArchiveInputStream in) throws IOException {
        List<String> entries = new ArrayList<>();

        ArchiveEntry entry = null;
        while ((entry = in.getNextEntry()) != null) {
            if (!in.canReadEntryData(entry)) {
                // log something?
                continue;
            }
            entries.add(entry.getName());
        }

        return entries;
    }

    public static void deleteFiles(Path path) throws IOException {
        deleteFiles(path, false);
    }

    public static void deleteFiles(Path path, boolean keepRoot) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
            if (!keepRoot) Files.deleteIfExists(path);
        }
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

    public static void grantExecutableAccess(Path path) throws IOException {
        grantAccess(path, "r-xr-xr-x");
    }

    public static void grantAccess(Path path, String accessRights) throws IOException {
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString(accessRights);
        Files.setPosixFilePermissions(path, perms);
    }

    public static void copyPermissions(Path src, Path dest) throws IOException {
        Set<PosixFilePermission> perms = Files.getPosixFilePermissions(src);
        Files.setPosixFilePermissions(dest, perms);
    }

    public static void copyFiles(JReleaserLogger logger, Path source, Path target) throws IOException {
        copyFiles(logger, source, target, null);
    }

    public static void copyFiles(JReleaserLogger logger, Path source, Path target, Predicate<Path> filter) throws IOException {
        Predicate<Path> actualFilter = filter != null ? filter : path -> true;
        IOException[] thrown = new IOException[1];

        Files.list(source)
            .filter(Files::isRegularFile)
            .filter(actualFilter)
            .forEach(child -> {
                try {
                    Files.copy(child, target.resolve(child.getFileName()), REPLACE_EXISTING);
                } catch (IOException e) {
                    logger.error(RB.$("ERROR_files_copy"), child, e);
                    if (null == thrown[0]) thrown[0] = e;
                }
            });

        if (thrown[0] != null) {
            throw thrown[0];
        }
    }

    public static boolean copyFilesRecursive(JReleaserLogger logger, Path source, Path target) throws IOException {
        return copyFilesRecursive(logger, source, target, null);
    }

    public static boolean copyFilesRecursive(JReleaserLogger logger, Path source, Path target, Predicate<Path> filter) throws IOException {
        FileTreeCopy copier = new FileTreeCopy(logger, source, target, filter);
        Files.walkFileTree(source, copier);
        return copier.isSuccessful();
    }

    private static class FileTreeCopy implements FileVisitor<Path> {
        private final JReleaserLogger logger;
        private final Path source;
        private final Path target;
        private final Predicate<Path> filter;
        private boolean success = true;

        FileTreeCopy(JReleaserLogger logger, Path source, Path target, Predicate<Path> filter) {
            this.logger = logger;
            this.source = source;
            this.target = target;
            this.filter = filter;
        }

        private boolean filtered(Path path) {
            if (null != filter) {
                return filter.test(path);
            }
            return false;
        }

        public boolean isSuccessful() {
            return success;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            if (filtered(dir)) return SKIP_SUBTREE;

            Path newdir = target.resolve(source.relativize(dir));
            try {
                Files.copy(dir, newdir);
                FileUtils.grantFullAccess(newdir);
            } catch (FileAlreadyExistsException ignored) {
                // noop
            } catch (IOException e) {
                logger.error(RB.$("ERROR_files_create"), newdir, e);
                success = false;
                return SKIP_SUBTREE;
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if (filtered(file)) return CONTINUE;

            try {
                Path newfile = target.resolve(source.relativize(file));
                Files.copy(file, newfile, REPLACE_EXISTING);
                FileUtils.copyPermissions(file, newfile);
            } catch (IOException e) {
                logger.error(RB.$("ERROR_files_copy"), source, e);
                success = false;
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            if (filtered(dir)) return CONTINUE;

            if (exc == null) {
                Path newdir = target.resolve(source.relativize(dir));
                try {
                    FileTime time = Files.getLastModifiedTime(dir);
                    Files.setLastModifiedTime(newdir, time);
                } catch (IOException e) {
                    logger.warn(RB.$("ERROR_files_copy_attributes"), newdir, e);
                }
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException e) {
            if (e instanceof FileSystemLoopException) {
                logger.error(RB.$("ERROR_files_cycle"), file);
            } else {
                logger.error(RB.$("ERROR_files_copy"), file, e);
            }
            success = false;
            return CONTINUE;
        }
    }
}
