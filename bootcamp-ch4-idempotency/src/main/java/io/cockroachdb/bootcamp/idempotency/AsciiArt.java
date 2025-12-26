package io.cockroachdb.bootcamp.idempotency;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AsciiArt {
    private static final Lock lock = new ReentrantLock();

    private static final String CUU = "\u001B[A";

    private static final String DL = "\u001B[1M";

    private AsciiArt() {
    }

    public static void printProgressBar(long current, long total, String label) {
        try {
            double p = (current + 0.0) / (Math.max(1, total) + 0.0);
            int ticks = Math.max(0, (int) (30 * p) - 1);
            String bar = String.format(
                    "%,9d/%-,9d %5.1f%% [%-30s] %s",
                    current,
                    total,
                    p * 100.0,
                    new String(new char[ticks]).replace('\0', '#') + ">",
                    label);
            lock.lock();
            System.out.println(CUU + "\r" + DL + bar);
            System.out.flush();
        } finally {
            lock.unlock();
        }
    }
}
