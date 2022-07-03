package org.activiti.examples.cnfig;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Snowflake {
    private static final int MIN_SERIAL_NUMBER = 1000;
    private static final int MAX_SERIAL_NUMBER = 4095;
    private static final long GENESIS = 1483200000000L;
    private static final long DOOMSDAY = 3682223255551L;
    private static final InetAddress IPv4 = NetWorkUtil.getLANIP();
    private static final long MAC;
    private AtomicReference<Snowflake.ClockSequence> generator;
    private int dataCenterId;
    private int workerId;

    public Snowflake() {
        this(0, 0);
    }

    public Snowflake(int dataCenterId, int workerId) {
        this.generator = new AtomicReference(new Snowflake.ClockSequence(System.currentTimeMillis()));
        this.dataCenterId = dataCenterId;
        this.workerId = workerId;
    }

    public long nextId() {
        Snowflake.TimestampAndSequence ts = this.nextTs(3682223255551L);
        return (ts.getTimestamp() - 1483200000000L & 2199023255551L) << 22 | (long) ((this.dataCenterId & 15) << 18) | (long) ((this.workerId & 63) << 12) | (long) (ts.getSequence() & 4095);
    }

    public void nextBytes(byte[] data) {
        assert data.length == 16 : "data must be 16 bytes in length";

        Snowflake.TimestampAndSequence ts = this.nextTs(-1L);
        this.packBytes(data, ts.getTimestamp(), ts.getSequence());
    }

    private void packBytes(byte[] data, long timestamp, int clockSequence) {
        long mostSigBits = 0L;
        mostSigBits |= timestamp & 4294967295L;
        mostSigBits <<= 16;
        mostSigBits |= timestamp >>> 32 & 65535L;
        mostSigBits <<= 16;
        mostSigBits |= timestamp >>> 48 & 4095L;

        for (int i = 0; i < 8; ++i) {
            int offset = 64 - (i + 1) * 8;
            data[i] = (byte) ((int) (mostSigBits >>> offset & 255L));
        }

        long leastSigBits = 0L;
        leastSigBits |= (long) (clockSequence & 4095);
        leastSigBits <<= 48;
        leastSigBits |= MAC & 281474976710655L;

        for (int j = 0; j < 8; ++j) {
            int offset2 = 64 - (j + 1) * 8;
            data[j + 8] = (byte) ((int) (leastSigBits >>> offset2 & 255L));
        }

    }

    private Snowflake.TimestampAndSequence nextTs(long doomsday) {
        Snowflake.ClockSequence clockSequence;
        int sequence;
        do {
            long timestamp = System.currentTimeMillis();
            clockSequence = (Snowflake.ClockSequence) this.generator.get();
            if (doomsday > 0L && timestamp > doomsday) {
                throw new RuntimeException(String.format("Current clock %d over than %d.  The end of the world", timestamp, doomsday));
            }

            if (timestamp > clockSequence.getTimestamp()) {
                this.generator.compareAndSet(clockSequence, new Snowflake.ClockSequence(timestamp));
            }

            sequence = clockSequence.nextSequence();
        } while (sequence > 4095);

        return new Snowflake.TimestampAndSequence(clockSequence.getTimestamp(), sequence);
    }

    static {
        MAC = NetWorkUtil.getLocalMac(IPv4);
    }

    private static class ClockSequence {
        private final long timestamp;
        private final AtomicInteger sequence;

        public ClockSequence(long timestamp) {
            this.timestamp = timestamp;
            this.sequence = new AtomicInteger(1000);
        }

        public long getTimestamp() {
            return this.timestamp;
        }

        public int nextSequence() {
            return this.sequence.incrementAndGet();
        }
    }

    private class TimestampAndSequence {
        private final long timestamp;
        private final int sequence;

        public TimestampAndSequence(long timestamp, int sequence) {
            this.timestamp = timestamp;
            this.sequence = sequence;
        }

        public long getTimestamp() {
            return this.timestamp;
        }

        public int getSequence() {
            return this.sequence;
        }
    }
}

