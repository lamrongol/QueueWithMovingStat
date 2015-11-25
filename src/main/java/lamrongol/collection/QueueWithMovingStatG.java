package lamrongol.collection;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.StatUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * en:QueueWithMovingStat which can be used for not only double but Comparable objects(Date, Long, String and so on) , w/o moving average
 * ja:double型だけでなくComparableであるオブジェクト全体(Date, Long, Stringなど)に対して使えるQueueWithMovingStatです（移動平均はなし）
 */
public class QueueWithMovingStatG<T extends Comparable<T>> {
    private int SAMPLING_SIZE;
    private CircularFifoQueue<T> queue;
    private boolean fulfilled = false;
    private T lastPutValue;
    private T lastEjectedValue;
    //stats
    private T min;
    private T max;
    private T median;

    public QueueWithMovingStatG(int samplingSize) throws Exception {
        if (samplingSize % 2 == 0) throw new Exception("samplingSize must be odd");

        SAMPLING_SIZE = samplingSize;
        queue = new CircularFifoQueue<>(samplingSize);

        fulfilled = false;
    }

    /**
     * @param newValue
     * @return last ejected value from queue
     */
    public T put(T newValue) {
        lastPutValue = newValue;
        if (!fulfilled) {//use different func until queue is fulfilled
            putInitial(newValue);
            return null;
        }

        lastEjectedValue = queue.poll();
        queue.offer(newValue);

        if (newValue.compareTo(min) < 0) {
            min = newValue;
        } else if (min == lastEjectedValue) {//if min value was ejected,  calc min value once again
            calcMinValue();
        }
        if (newValue.compareTo(max) > 0) {
            max = newValue;
        } else if (max == lastEjectedValue) {//if max value was ejected,  calc max value once again
            calcMaxValue();
        }

        calcMovingMedian();

        return lastEjectedValue;
    }

    private void calcMovingMedian() {
        if (((lastEjectedValue.compareTo(median) < 0) && (lastPutValue.compareTo(median) < 0))
                || ((lastEjectedValue.equals(median)) && (lastPutValue.equals(median)))
                || ((lastEjectedValue.compareTo(median) > 0) && (lastPutValue.compareTo(median) > 0))) {
            //do nothing if lastEjectedValue and lastPutValue are larger, smaller, equals to previous  median
        } else if (lastPutValue.compareTo(median) > 0) {
            int higherCount = 0;
            T minOverMedian = null;
            for (T d : queue) {
                if (d.compareTo(median) > 0) {
                    higherCount++;
                    if (minOverMedian == null || d.compareTo(minOverMedian) < 0) {
                        minOverMedian = d;
                    }
                }
            }
            if (higherCount > (SAMPLING_SIZE / 2)) {
                median = minOverMedian;
            }
        } else {
            int lowerCount = 0;
            T maxUnderMedian = null;
            for (T d : queue) {
                if (d.compareTo(median) < 0) {
                    lowerCount++;
                    if (maxUnderMedian == null || d.compareTo(maxUnderMedian) > 0) {
                        maxUnderMedian = d;
                    }
                }
            }
            if (lowerCount > (SAMPLING_SIZE / 2)) {
                median = maxUnderMedian;
            }
        }
    }

    private void putInitial(T value) {
        queue.offer(value);

        //if calculating stats, too before fulfilled
        //calcStatsSimply();

        if (queue.size() < SAMPLING_SIZE) {
            return;
        }

        fulfilled = true;
        calcStatsSimply();

        return;
    }

    private void calcStatsSimply() {
        calcMaxValue();
        calcMinValue();

        if (queue.size() % 2 == 1) {
            List<T> list = new ArrayList<>(queue);
            Collections.sort(list);
            median = list.get(list.size() / 2);
        }
    }

    private void calcMinValue() {
        min = null;
        for (T d : queue) {
            if (min == null || d.compareTo(min) < 0) {
                min = d;
            }
        }
    }

    private void calcMaxValue() {
        max = null;
        for (T d : queue) {
            if (max == null || d.compareTo(max) > 0) {
                max = d;
            }
        }

    }

    public T getMin() {
        return min;
    }

    public T getMax() {
        return max;
    }

    public T getMedian() {
        return median;
    }

    public boolean isFulfilled() {
        return fulfilled;
    }

    public T getLastPutValue() {
        return lastPutValue;
    }

    public T getLastEjectedValue() {
        return lastEjectedValue;
    }

    public int getSamplingSize() {
        return SAMPLING_SIZE;
    }

    public int getCurrentSize() {
        return queue.size();
    }

    public T get(int index) {
        return index >= 0 ? queue.get(index) : queue.get(queue.size() + index);
    }
}