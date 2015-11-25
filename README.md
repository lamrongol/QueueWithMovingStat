Fixed size queue with moving stats(min, max, average, median)
You only just add values, stats(min, max, average, median) are calculated with minimum amount of calculation

----------------------------------------------------------------

移動統計量を自動的に計算する固定長のキューです。
普通に値を追加していけば自動的に各種移動統計量が計算されます。

sample------------------
	QueueWithMovingStat queue = new QueueWithMovingStat(5);
	queue.put(3);
	queue.put(7);
	queue.put(6);
	queue.put(9);
	queue.put(1);
	System.out.println(queue.getMedian());//6
	queue.put(8);
	System.out.println(queue.getMedian());//7
