package lamrongol.collection;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.junit.Test;

import java.util.Comparator;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.Assert.assertEquals;

public class QueueWithMovingStatGTest {

    private static final int TEST_SIZE = 100;

    @Test
    public void testOddSampleNumDateValue() throws Exception {
        int SIZE = 11;
        QueueWithMovingStatG<Date> queue = new QueueWithMovingStatG<>(SIZE);
        CircularFifoQueue<Double> counterQueue = new CircularFifoQueue<>(SIZE);

        Random r = new Random();
        for (int i = 0; i < TEST_SIZE; i++) {
            double d = r.nextInt();
            queue.put(new Date((int) d));
            counterQueue.add(d);
            if (!queue.isFulfilled()) continue;

            double expected = StatUtils.percentile(ArrayUtils.toPrimitive(counterQueue.toArray(new Double[0])), 50.0);
            Date actual = queue.getMedian();

            assertEquals((long) expected, actual.getTime());

            Double min = counterQueue.stream().min(Comparator.naturalOrder()).get();
            Double max = counterQueue.stream().max(Comparator.naturalOrder()).get();

            assertEquals(min.longValue(), queue.getMin().getTime());
            assertEquals(max.longValue(), queue.getMax().getTime());

            assertEquals(counterQueue.get(0).longValue(), queue.get(0).getTime());
            assertEquals(counterQueue.get(1).longValue(), queue.get(1).getTime());
            assertEquals(counterQueue.get(SIZE - 1).longValue(), queue.get(-1).getTime());
            assertEquals(counterQueue.get(SIZE - 2).longValue(), queue.get(-2).getTime());
        }
    }
}