package ch.frostnova.cli.idx.sync.config;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;

public class FileBenchmark {


    private static Path createTestFile(long sizeInBytes) {
        try {
            var file = createTempFile("test-file", String.valueOf(sizeInBytes));
            Random random = ThreadLocalRandom.current();
            var buffer = new byte[4096];
            var bytesRemaining = sizeInBytes;
            try (OutputStream out = new BufferedOutputStream((newOutputStream(file)))) {
                while (bytesRemaining > 0) {
                    random.nextBytes(buffer);
                    var bytesToWrite = (int) Math.min(bytesRemaining, buffer.length);
                    out.write(buffer, 0, bytesToWrite);
                    bytesRemaining -= bytesToWrite;
                }
                out.flush();
            }
            return file;
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    @Test
    @Disabled
    void benchmarkFileCopy() throws IOException {


        var targetDir = Paths.get("/media/piwi/idx Backup/Test");
        if (!exists(targetDir)) {
            createDirectories(targetDir);
        }

        var loops = 10;
        var buffer = new byte[4096];

        for (var b = 0; b <= 20; b++) {

            long totalTimeNs = 0;
            for (var i = 0; i < loops; i++) {
                var target = targetDir.resolve("test-" + b + "-" + i);
                var source = createTestFile(1 << b);
                var startTimeNs = System.nanoTime();
                try (InputStream in = new BufferedInputStream((newInputStream(source)))) {
                    try (OutputStream out = new BufferedOutputStream((newOutputStream(target)))) {
                        int read;
                        while ((read = in.read(buffer)) >= 0) {
                            out.write(buffer, 0, read);
                        }
                        out.flush();
                    }
                }
                totalTimeNs += System.nanoTime() - startTimeNs;
                delete(source);
                delete(target);
            }
            var time = totalTimeNs / loops;
            System.out.println("File Size " + (1 << b) + ": " + new DecimalFormat("0.000000").format(time / 1e9) + " sec");
        }
    }
}
