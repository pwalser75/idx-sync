package ch.frostnova.cli.idx.sync;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

import java.util.UUID;

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("Idx SYNC");
        consoleProgressExample();

    }

    private static void consoleProgressExample() throws Exception {
        int n = 739;
        for (int i = 0; i <= n; i++) {
            ConsoleTools.printProgress(i, n, "\uD83D\uDE80 Testing progress", "\uD83D\uDD25 " + UUID.randomUUID().toString());
            Thread.sleep(12345 / n);
        }
        ConsoleTools.printProgress(1, "\uD83D\uDE80 Testing progress", "done.");
        System.out.println();
    }

    private static void progressBarExample() throws Exception {
        ProgressBarBuilder pbb = new ProgressBarBuilder()
                .setTaskName("Processing files")
                .setUpdateIntervalMillis(50)
                .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BLOCK)
                .setMaxRenderedLength(100);

        int n = 739;
        try (ProgressBar pb = pbb.setInitialMax(n).build()) {
            for (int i = 0; i <= n; i++) {
                pb.stepTo(i);
                pb.setExtraMessage("working…");
                Thread.sleep(12345 / n);
            }
            pb.setExtraMessage("done.");
        }
        System.out.println();
    }
}
