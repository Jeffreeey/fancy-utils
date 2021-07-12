package distribution.id;

/**
 * 0 + 时间戳 + workId + 序列号
 *
 * 1 fixed bit + 41 timestamp bits + 10 workId bits + 12 sequence bits
 *
 *
 * @Author: zhangjingtai
 * @Date: 2021/7/12 2:27 下午
 */
public class IdWorker {

    private long workerId;

    private long sequence = 0L;

    private long lastTimestamp = -1L;

    private final static int WORK_BIT = 10;
    private final static int SEQUENCE_BIT = 12;
    private final static int WORK_ID_SHIT = SEQUENCE_BIT;
    private final static int TIMESTAMP_SHIFT_BIT = WORK_BIT + SEQUENCE_BIT;
    private final static long MAX_WORKER_ID = ~(-1L << WORK_BIT);
    private final static long SEQUENCE_MASK = ~(-1L << SEQUENCE_BIT);

    /**
     * 时间戳起始时间
     * 2021-01-01 00:00:00.000
     */
    private final static long EPOCH = 1609430400000L;


    public IdWorker(long workerId) {
        this.workerId = workerId;
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(String.format(
                    "worker Id can't be greater than %d or less than 0", MAX_WORKER_ID));
        }
    }

    public synchronized long nextId() {
        long currentTimestamp = System.currentTimeMillis();

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                currentTimestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }

        if (currentTimestamp < lastTimestamp) {
            try {
                throw new Exception(String.format(
                        "Clock moved backwards.  Refusing to generate id for %d milliseconds",
                        this.lastTimestamp - currentTimestamp));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        lastTimestamp = currentTimestamp;
        long nextId = ((currentTimestamp - EPOCH) << TIMESTAMP_SHIFT_BIT) | workerId << WORK_ID_SHIT | sequence;
        return nextId;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        if (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;

    }

    public static void main(String[] args) {
        IdWorker worker = new IdWorker(3);
        System.out.println(worker.nextId());
        System.out.println(worker.nextId());
    }

}
