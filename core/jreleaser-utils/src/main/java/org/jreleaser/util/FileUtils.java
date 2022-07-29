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
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.jreleaser.bundle.RB;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
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
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
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
import static org.jreleaser.util.FileType.ZIP;
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

    public static void listFilesAndConsume(Path path, Consumer<Stream<Path>> consumer) throws IOException {
        try (Stream<Path> files = Files.list(path)) {
            consumer.accept(files);
        }
    }

    public static <T> T listFilesAndProcess(Path path, Function<Stream<Path>, T> function) throws IOException {
        try (Stream<Path> files = Files.list(path)) {
            return function.apply(files);
        }
    }

    public static Optional<Path> findLicenseFile(Path basedir) {
        for (String licenseFilename : Arrays.asList(LICENSE_FILE_NAMES)) {
            Path path = basedir.resolve(licenseFilename);
            if (Files.exists(path)) {
                return Optional.of(path);
            }
            path = basedir.resolve(licenseFilename.toLowerCase(Locale.ENGLISH));
            if (Files.exists(path)) {
                return Optional.of(path);
            }
        }

        return Optional.empty();
    }

    public static Path resolveOutputDirectory(Path basedir, Path outputdir, String baseOutput) {
        String od = Env.env("OUTPUT_DIRECTORY", "");
        if (isNotBlank(od)) {
            return basedir.resolve(od).resolve("jreleaser").normalize();
        }
        if (null != outputdir) {
            return basedir.resolve(outputdir).resolve("jreleaser").normalize();
        }
        return basedir.resolve(baseOutput).resolve("jreleaser").normalize();
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
        unpackArchive(src, dest, true);
    }

    public static void unpackArchive(Path src, Path dest, boolean removeRootEntry) throws IOException {
        unpackArchive(src, dest, removeRootEntry, true);
    }

    public static void unpackArchive(Path src, Path dest, boolean removeRootEntry, boolean cleanDirectory) throws IOException {
        String filename = src.getFileName().toString();
        for (String extension : TAR_COMPRESSED_EXTENSIONS) {
            if (filename.endsWith(extension)) {
                unpackArchiveCompressed(src, dest, removeRootEntry);
                return;
            }
        }

        if (cleanDirectory) deleteFiles(dest, true);
        File destinationDir = dest.toFile();

        if (filename.endsWith(ZIP.extension())) {
            try (ZipFile zipFile = new ZipFile(src.toFile())) {
                unpackArchive(removeRootEntry ? filename + "/" : "", destinationDir, zipFile);
            }
            return;
        }

        try (InputStream fi = Files.newInputStream(src);
             InputStream bi = new BufferedInputStream(fi);
             ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream(bi)) {

            // subtract .zip, .tar
            filename = filename.substring(0, filename.length() - 4);
            unpackArchive(removeRootEntry ? filename + "/" : "", destinationDir, in);
        } catch (ArchiveException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    public static void unpackArchiveCompressed(Path src, Path dest) throws IOException {
        unpackArchiveCompressed(src, dest, true);
    }

    public static void unpackArchiveCompressed(Path src, Path dest, boolean removeRootEntry) throws IOException {
        unpackArchiveCompressed(src, dest, removeRootEntry, true);
    }

    public static void unpackArchiveCompressed(Path src, Path dest, boolean removeRootEntry, boolean cleanDirectory) throws IOException {
        if (cleanDirectory) deleteFiles(dest, true);
        File destinationDir = dest.toFile();

        String filename = src.getFileName().toString();
        String artifactFileName = getFilename(filename, FileType.getSupportedExtensions());
        String artifactExtension = filename.substring(artifactFileName.length());
        String artifactFileFormat = artifactExtension.substring(1);
        FileType fileType = FileType.of(artifactFileFormat);

        try (InputStream fi = Files.newInputStream(src);
             InputStream bi = new BufferedInputStream(fi);
             InputStream gzi = resolveCompressorInputStream(fileType, bi);
             ArchiveInputStream in = new TarArchiveInputStream(gzi)) {
            unpackArchive(removeRootEntry ? artifactFileName + "/" : "", destinationDir, in);
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
            if (isNotBlank(basename) && entryName.startsWith(basename) && entryName.length() > basename.length() + 1) {
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

                if (isSymbolicLink(entry)) {
                    Files.createSymbolicLink(file.toPath(), Paths.get(getLinkName(in, entry)));
                } else {
                    try (OutputStream o = Files.newOutputStream(file.toPath())) {
                        IOUtils.copy(in, o);
                        Files.setLastModifiedTime(file.toPath(), FileTime.from(entry.getLastModifiedDate().toInstant()));
                        chmod(file, getEntryMode(entry, file));
                    }
                }
            }
        }
    }

    private static void unpackArchive(String basename, File destinationDir, ZipFile zipFile) throws IOException {
        Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
        while (entries.hasMoreElements()) {
            ZipArchiveEntry entry = entries.nextElement();
            if (!zipFile.canReadEntryData(entry)) {
                // log something?
                continue;
            }

            String entryName = entry.getName();
            if (isNotBlank(basename) && entryName.startsWith(basename) && entryName.length() > basename.length() + 1) {
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

                if (entry.isUnixSymlink()) {
                    Files.createSymbolicLink(file.toPath(), Paths.get(zipFile.getUnixSymlink(entry)));
                } else {
                    try (OutputStream o = Files.newOutputStream(file.toPath())) {
                        IOUtils.copy(zipFile.getInputStream(entry), o);
                        Files.setLastModifiedTime(file.toPath(), FileTime.from(entry.getLastModifiedDate().toInstant()));
                        chmod(file, getEntryMode(entry, file));
                    }
                }
            }
        }
    }

    private static boolean isSymbolicLink(ArchiveEntry entry) {
        if (entry instanceof ZipArchiveEntry) {
            return ((ZipArchiveEntry) entry).isUnixSymlink();
        } else if (entry instanceof TarArchiveEntry) {
            return ((TarArchiveEntry) entry).isSymbolicLink();
        }
        return false;
    }

    private static String getLinkName(ArchiveInputStream in, ArchiveEntry entry) throws IOException {
        if (entry instanceof ZipArchiveEntry) {
            try (OutputStream o = new ByteArrayOutputStream()) {
                IOUtils.copy(in, o);
                return o.toString();
            }
        } else if (entry instanceof TarArchiveEntry) {
            return ((TarArchiveEntry) entry).getLinkName();
        }
        return "";
    }

    private static int getEntryMode(ArchiveEntry entry, File file) {
        if (entry instanceof TarArchiveEntry) {
            return getEntryMode(entry, ((TarArchiveEntry) entry).getMode(), file);
        }
        return getEntryMode(entry, ((ZipArchiveEntry) entry).getUnixMode(), file);
    }

    private static int getEntryMode(ArchiveEntry entry, int mode, File file) {
        int unixMode = mode & 0777;
        if (unixMode == 0) {
            if (entry.isDirectory()) {
                unixMode = 0755;
            } else if ("bin".equalsIgnoreCase(file.getParentFile().getName())) {
                // zipEntry.unixMode returns 0 most times even if the entry is executable
                // force executable bit only if parent dir == 'bin'
                unixMode = 0777;
            } else {
                unixMode = 0644;
            }
        }
        return unixMode;
    }

    public static void chmod(File file, int mode) throws IOException {
        chmod(file.toPath(), mode);
    }

    public static void chmod(Path path, int mode) throws IOException {
        if (supportsPosix(path)) {
            PosixFileAttributeView fileAttributeView = Files.getFileAttributeView(path, PosixFileAttributeView.class);
            fileAttributeView.setPermissions(convertToPermissionsSet(mode));
        } else {
            path.toFile().setExecutable(true);
        }
    }

    private static boolean supportsPosix(Path path) {
        return path.getFileSystem().supportedFileAttributeViews().contains("posix");
    }

    private static Set<PosixFilePermission> convertToPermissionsSet(int mode) {
        Set<PosixFilePermission> result = EnumSet.noneOf(PosixFilePermission.class);

        if ((mode & 256) == 256) {
            result.add(PosixFilePermission.OWNER_READ);
        }

        if ((mode & 128) == 128) {
            result.add(PosixFilePermission.OWNER_WRITE);
        }

        if ((mode & 64) == 64) {
            result.add(PosixFilePermission.OWNER_EXECUTE);
        }

        if ((mode & 32) == 32) {
            result.add(PosixFilePermission.GROUP_READ);
        }

        if ((mode & 16) == 16) {
            result.add(PosixFilePermission.GROUP_WRITE);
        }

        if ((mode & 8) == 8) {
            result.add(PosixFilePermission.GROUP_EXECUTE);
        }

        if ((mode & 4) == 4) {
            result.add(PosixFilePermission.OTHERS_READ);
        }

        if ((mode & 2) == 2) {
            result.add(PosixFilePermission.OTHERS_WRITE);
        }

        if ((mode & 1) == 1) {
            result.add(PosixFilePermission.OTHERS_EXECUTE);
        }

        return result;
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
        String artifactExtension = filename.substring(artifactFileName.length());
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
        if (supportsPosix(path)) {
            Set<PosixFilePermission> perms = PosixFilePermissions.fromString(accessRights);
            FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);
            Files.createDirectories(path, attr);
        } else {
            Files.createDirectories(path);
        }
    }

    public static void grantFullAccess(Path path) throws IOException {
        grantAccess(path, "rwxrwxrwx");
    }

    public static void grantExecutableAccess(Path path) throws IOException {
        grantAccess(path, "r-xr-xr-x");
    }

    public static void grantAccess(Path path, String accessRights) throws IOException {
        if (supportsPosix(path)) {
            Set<PosixFilePermission> perms = PosixFilePermissions.fromString(accessRights);
            Files.setPosixFilePermissions(path, perms);
        } else if (accessRights.contains("r")) {
            path.toFile().setReadable(true);
        } else if (accessRights.contains("w")) {
            path.toFile().setWritable(true);
        } else if (accessRights.contains("x")) {
            path.toFile().setExecutable(true);
        }
    }

    public static void copyPermissions(Path src, Path dest) throws IOException {
        if (supportsPosix(src)) {
            Set<PosixFilePermission> perms = Files.getPosixFilePermissions(src);
            Files.setPosixFilePermissions(dest, perms);
        } else {
            File s = src.toFile();
            File d = dest.toFile();
            d.setReadable(s.canRead());
            d.setWritable(s.canWrite());
            d.setExecutable(s.canExecute());
        }
    }

    public static void copyFiles(JReleaserLogger logger, Path source, Path target) throws IOException {
        copyFiles(logger, source, target, path -> true);
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

    public static void copyFiles(JReleaserLogger logger, Path source, Path target, Set<Path> paths) throws IOException {
        logger.debug(RB.$("files.copy", source, target));

        for (Path path : paths) {
            Path srcPath = source.resolve(path);
            Path targetPath = target.resolve(path);

            Files.createDirectories(targetPath.getParent());
            Files.copy(srcPath, targetPath, REPLACE_EXISTING);
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
            logger.debug(RB.$("files.copy", source, target));
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
