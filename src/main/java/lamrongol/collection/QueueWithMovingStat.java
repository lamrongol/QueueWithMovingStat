package lamrongol.collection;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * en:Fixed size queue with automatic moving stats calculator (moving average, moving median, min, max)
 * ja:移動平均、移動中央値、最小・最大の自動計算機能付きの固定サイズのキュー
 */
public class QueueWithMovingStat {
    private int SAMPLING_SIZE;
    private CircularFifoQueue<Double> queue;
    private boolean fulfilled = false;
    private double lastPutValue;
    private double lastEjectedValue;
    //stats
    private double min;
    private double max;
    private double median;
    private double average;

    private boolean hasOddSamplingSize;
    //To calculate median for even sample size
    private double lowerMiddle;
    private double higherMiddle;

    public QueueWithMovingStat(int samplingSize) {
        SAMPLING_SIZE = samplingSize;
        queue = new CircularFifoQueue<>(samplingSize);

        hasOddSamplingSize = (samplingSize % 2) == 1;
        fulfilled = false;
    }

    /**
     * @param newValue
     * @return last ejected value from queue
     */
    public double put(double newValue) {
        lastPutValue = newValue;
        if (!fulfilled) {//use different func until queue is fulfilled
            putInitial(newValue);
            return Double.NaN;
        }

        lastEjectedValue = queue.poll();
        queue.offer(newValue);

        if (newValue < min) {
            min = newValue;
        } else if (min == lastEjectedValue) {//if min value was ejected,  calc min value once again
            calcMinValue();
        }
        if (newValue > max) {
            max = newValue;
        } else if (max == lastEjectedValue) {//if max value was ejected,  calc max value once again
            calcMaxValue();
        }

        calcMovingMedian();

        average += (newValue - lastEjectedValue) / SAMPLING_SIZE;

        return lastEjectedValue;
    }

    private void calcMovingMedian() {
        if (hasOddSamplingSize) {
            if (((lastEjectedValue < median) && (lastPutValue < median))
                    || ((lastEjectedValue == median) && (lastPutValue == median))
                    || ((lastEjectedValue > median) && (lastPutValue > median))) {
                //do nothing if lastEjectedValue and lastPutValue are larger, smaller, equals to previous  median
            } else if (lastPutValue > median) {
                int higherCount = 0;
                double minOverMedian = Double.MAX_VALUE;
                for (double d : queue) {
                    if (d > median) {
                        higherCount++;
                        if (d < minOverMedian) {
                            minOverMedian = d;
                        }
                    }
                }
                if (higherCount > (SAMPLING_SIZE / 2)) {
                    median = minOverMedian;
                }
            } else {
                int lowerCount = 0;
                double maxUnderMedian = -Double.MAX_VALUE;
                for (double d : queue) {
                    if (d < median) {
                        lowerCount++;
                        if (d > maxUnderMedian) {
                            maxUnderMedian = d;
                        }
                    }
                }
                if (lowerCount > (SAMPLING_SIZE / 2)) {
                    median = maxUnderMedian;
                }
            }
        } else {
            if (((lastEjectedValue < lowerMiddle) && (lastPutValue < lowerMiddle))
                    || ((lastEjectedValue > higherMiddle) && (lastPutValue > higherMiddle))
                    || ((lastEjectedValue == lowerMiddle) && (lastPutValue == lowerMiddle))
                    || ((lastEjectedValue == higherMiddle) && (lastPutValue == higherMiddle))) {
                //do nothing if lastEjectedValue and lastPutValue are larger, smaller, equals to previous  median
            } else if (lastPutValue > higherMiddle) {
                int higherCount = 0;
                int equalsCount = 0;
                double minOverHigherMiddle = Double.MAX_VALUE;
                for (double d : queue) {
                    if (d > higherMiddle) {
                        higherCount++;
                        if (d < minOverHigherMiddle) {
                            minOverHigherMiddle = d;
                        }
                    } else if (d == higherMiddle) {
                        equalsCount++;
                    }
                }
                if (higherCount == (SAMPLING_SIZE / 2)) {
                    if (higherMiddle != lastEjectedValue) {
                        lowerMiddle = higherMiddle;
                    }
                    higherMiddle = minOverHigherMiddle;
                    median = (higherMiddle + lowerMiddle) / 2;
                } else if ((higherCount + equalsCount) > (SAMPLING_SIZE / 2)) {
                    lowerMiddle = higherMiddle;
                    median = higherMiddle;
                }
            } else if (lastPutValue < lowerMiddle) {
                int lowerCount = 0;
                int equalsCount = 0;
                double maxUnderLowerMiddle = -Double.MAX_VALUE;
                for (double d : queue) {
                    if (d < lowerMiddle) {
                        lowerCount++;
                        if (d > maxUnderLowerMiddle) {
                            maxUnderLowerMiddle = d;
                        }
                    } else if (d == lowerMiddle) {
                        equalsCount++;
                    }
                }
                if (lowerCount == (SAMPLING_SIZE / 2)) {
                    if (lowerMiddle != lastEjectedValue) {
                        higherMiddle = lowerMiddle;
                    }
                    lowerMiddle = maxUnderLowerMiddle;
                    median = (higherMiddle + lowerMiddle) / 2;
                } else if ((lowerCount + equalsCount) > (SAMPLING_SIZE / 2)) {
                    higherMiddle = lowerMiddle;
                    median = lowerMiddle;
                }
            } else {
                //oldestValue is <=lowerMiddle or >=higherMiddle
                if (lastEjectedValue <= lowerMiddle) {
                    lowerMiddle = lastPutValue;
                } else {
                    higherMiddle = lastPutValue;
                }
                median = (higherMiddle + lowerMiddle) / 2;
            }
        }
    }

    private void putInitial(Double value) {
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
        average = queue.stream().mapToDouble(Double::doubleValue).average().getAsDouble();

        //calc median
        List<Double> list = new ArrayList<>(queue);
        Collections.sort(list);
        if (hasOddSamplingSize) {
            median = list.get(list.size() / 2);
            //use external library
            //median = StatUtils.percentile(ArrayUtils.toPrimitive(queue.toArray(new Double[0])), 50.0);
        } else {
            lowerMiddle = list.get((list.size() / 2) - 1);
            higherMiddle = list.get((list.size() / 2));
            median = (lowerMiddle + higherMiddle) / 2;
        }
    }

    private void calcMinValue() {
        min = queue.stream().mapToDouble(Double::doubleValue).min().getAsDouble();
    }

    private void calcMaxValue() {
        max = queue.stream().mapToDouble(Double::doubleValue).max().getAsDouble();
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getMedian() {
        return median;
    }

    public double getAverage() {
        return average;
    }

    public boolean isFulfilled() {
        return fulfilled;
    }

    public double getLastPutValue() {
        return lastPutValue;
    }

    public double getLastEjectedValue() {
        return lastEjectedValue;
    }

    public int getSamplingSize() {
        return SAMPLING_SIZE;
    }

    public int getCurrentSize() {
        return queue.size();
    }

    public double get(int index) {
        return index >= 0 ? queue.get(index) : queue.get(queue.size() + index);
    }
}