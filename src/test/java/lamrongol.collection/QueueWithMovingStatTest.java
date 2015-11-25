package lamrongol.collection;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.Assert.assertEquals;

public class QueueWithMovingStatTest {

    private static final int TEST_SIZE = 100;

    @Test
    public void testOddSampleNumDoubleValue() {
        int SIZE = 11;
        QueueWithMovingStat queue = new QueueWithMovingStat(SIZE);
        CircularFifoQueue<Double> buffer = new CircularFifoQueue<>(SIZE);
        for (int i = 0; i < TEST_SIZE; i++) {
            double d = new Random().nextDouble();
            queue.put(d);
            if (i >= SIZE) {
                buffer.poll();
            }
            buffer.add(d);
            if (!queue.isFulfilled()) continue;

            double expected = StatUtils.percentile(ArrayUtils.toPrimitive(buffer.toArray(new Double[0])), 50.0);
            double actual = queue.getMedian();

            assertEquals(expected, actual, Double.MIN_VALUE);
        }
    }

    @Test
    public void testEvenSampleNumDoubleValue() {
        int SIZE = 10;
        QueueWithMovingStat queue = new QueueWithMovingStat(SIZE);
        ArrayBlockingQueue<Double> buffer = new ArrayBlockingQueue<Double>(SIZE);
        for (int i = 0; i < TEST_SIZE; i++) {
            double d = new Random().nextDouble();
            queue.put(d);
            if (i >= SIZE) {
                buffer.poll();
            }
            buffer.add(d);
            if (!queue.isFulfilled()) continue;

            double expected = StatUtils.percentile(ArrayUtils.toPrimitive(buffer.toArray(new Double[0])), 50.0);
            double actual = queue.getMedian();

            assertEquals(expected, actual, Double.MIN_VALUE);
        }
    }

    @Test
    public void testOddSampleNumIntValue() {
        int SIZE = 11;
        QueueWithMovingStat queue = new QueueWithMovingStat(SIZE);
        ArrayBlockingQueue<Double> buffer = new ArrayBlockingQueue<Double>(SIZE);
        for (int i = 0; i < TEST_SIZE; i++) {
            double d = new Random().nextInt(10);
            queue.put(d);
            if (i >= SIZE) {
                buffer.poll();
            }
            buffer.add(d);
            if (!queue.isFulfilled()) continue;

            double expected = StatUtils.percentile(ArrayUtils.toPrimitive(buffer.toArray(new Double[0])), 50.0);
            double actual = queue.getMedian();

            assertEquals(expected, actual, Double.MIN_VALUE);
        }
    }

    @Test
    public void testEvenSampleNumIntValue() {
        int SIZE = 10;
        QueueWithMovingStat queue = new QueueWithMovingStat(SIZE);
        ArrayBlockingQueue<Double> buffer = new ArrayBlockingQueue<Double>(SIZE);
        for (int i = 0; i < TEST_SIZE; i++) {
            double d = new Random().nextInt(10);
            queue.put(d);
            if (i >= SIZE) {
                buffer.poll();
            }
            buffer.add(d);
            if (!queue.isFulfilled()) continue;

            double expected = StatUtils.percentile(ArrayUtils.toPrimitive(buffer.toArray(new Double[0])), 50.0);
            double actual = queue.getMedian();

            assertEquals(expected, actual, Double.MIN_VALUE);
        }
    }
}