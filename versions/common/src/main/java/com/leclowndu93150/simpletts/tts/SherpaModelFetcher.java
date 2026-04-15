package com.leclowndu93150.simpletts.tts;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.pitest.voices.download.ModelFetcher;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.LongConsumer;

public class SherpaModelFetcher implements ModelFetcher {

    private final URL url;
    private final String voiceId;

    public SherpaModelFetcher(URL url, String voiceId) {
        this.url = url;
        this.voiceId = voiceId;
    }

    @Override
    public Path fetch() throws IOException {
        Path dir = Files.createTempDirectory("simpletts-" + voiceId + "-");
        dir.toFile().deleteOnExit();
        System.out.println("[SimpleTTS] SherpaModelFetcher.fetch: " + url + " -> " + dir);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);
        conn.setInstanceFollowRedirects(true);
        int status = conn.getResponseCode();
        if (status < 200 || status >= 300) {
            conn.disconnect();
            throw new IOException("HTTP " + status + " fetching " + url);
        }

        long total = conn.getContentLengthLong();
        Path archive = dir.resolve("model.tar.bz2");

        try (InputStream in = conn.getInputStream();
             CountingInputStream counting = new CountingInputStream(in, total, bytes -> {
                 if (total > 0) {
                     int pct = (int) ((bytes * 100L) / total);
                     DownloadProgressTracker.update("Downloading " + voiceId + "... " + pct + "%");
                 } else {
                     DownloadProgressTracker.update("Downloading " + voiceId + "... " + (bytes / (1024 * 1024)) + "MB");
                 }
             });
             OutputStream out = Files.newOutputStream(archive)) {
            counting.transferTo(out);
        } finally {
            conn.disconnect();
        }
        System.out.println("[SimpleTTS] SherpaModelFetcher: archive downloaded, extracting");

        DownloadProgressTracker.update("Extracting " + voiceId + "...");
        extract(archive, dir);
        Files.deleteIfExists(archive);
        System.out.println("[SimpleTTS] SherpaModelFetcher: extract complete");
        return dir;
    }

    private void extract(Path archive, Path dir) throws IOException {
        try (InputStream fin = Files.newInputStream(archive);
             BufferedInputStream bin = new BufferedInputStream(fin);
             BZip2CompressorInputStream bz = new BZip2CompressorInputStream(bin, true);
             TarArchiveInputStream tar = new TarArchiveInputStream(bz)) {

            ArchiveEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                Path extractTo = dir.resolve(entry.getName()).normalize();
                if (!extractTo.startsWith(dir)) {
                    throw new IOException("Tar entry escapes dir: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(extractTo);
                } else {
                    Path parent = extractTo.getParent();
                    if (parent != null) {
                        Files.createDirectories(parent);
                    }
                    Files.copy(tar, extractTo, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private static final class CountingInputStream extends InputStream {
        private final InputStream delegate;
        private final long total;
        private final LongConsumer onBytes;
        private long read;
        private long lastReported;

        CountingInputStream(InputStream delegate, long total, LongConsumer onBytes) {
            this.delegate = delegate;
            this.total = total;
            this.onBytes = onBytes;
        }

        @Override
        public int read() throws IOException {
            int b = delegate.read();
            if (b >= 0) {
                read++;
                report();
            }
            return b;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int n = delegate.read(b, off, len);
            if (n > 0) {
                read += n;
                report();
            }
            return n;
        }

        private void report() {
            if (read - lastReported < 131072 && read != total) {
                return;
            }
            lastReported = read;
            onBytes.accept(read);
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }
}
