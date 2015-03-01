package jp.co.stofu.kamaitachi;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;

public class Awaiter {

	LinkedBlockingQueue<Runnable> waitingTaskQueue = new LinkedBlockingQueue<Runnable>();
	Function<Exception, Void> errorHandler = null;
	private boolean isDispatchThread = true;
	ExecutorService executor = null;
	ExecutorService dispatchExecutor = null;

	public <T, R> BlockingQueue<R> await(Callable<T> heavyWorkTask,
			Function<T, R> afterHeavyWorkTask) {
		return await(heavyWorkTask, afterHeavyWorkTask, this.errorHandler);

	}

	public <T, R> BlockingQueue<R> await(Callable<T> heavyWorkTask,
			Function<T, R> afterHeavyWorkTask,
			Function<Exception, Void> errorHandler) {
		// ExecutorService executor = Executors.newSingleThreadExecutor();
		if (executor == null) {
			executor = Executors.newCachedThreadPool();
		}
		LinkedBlockingQueue<R> resultQueue = new LinkedBlockingQueue<R>();
		// lambda式のタスクの中
		// 1. heavyWorkTaskを実行し、型TのheavyWorkResultを受け取る
		// 2.
		// afterHeavyWorkTaskをheavyWorkResultとresultQueueを引数にカリー化でwaitingTaskにする
		// 3. heavyWorkResultをresultQueueにaddする
		// 4. waitingTaskをwaitingTaskQueueにaddする
		executor.submit(() -> {
			// heavyWorkControlTaskを非同期で実行
			try {
				final T heavyWorkResult = heavyWorkTask.call();
				waitingTaskQueue.add(() -> {
					R afterHeavyWorkResult = afterHeavyWorkTask
							.apply(heavyWorkResult);
					resultQueue.add(afterHeavyWorkResult);
				});

			} catch (Exception e) {
				if (errorHandler == null) {
					e.printStackTrace();
				} else {
					errorHandler.apply(e);
				}
			}

		});
		return resultQueue;
	}

	public <T> void awaitVoid(Callable<T> heavyWorkTask,
			Consumer<T> afterHeavyWorkTask) {
		awaitVoid(heavyWorkTask, afterHeavyWorkTask, this.errorHandler);
	}

	public <T> void awaitVoid(Callable<T> heavyWorkTask,
			Consumer<T> afterHeavyWorkTask,
			Function<Exception, Void> errorHandler) {
		// ExecutorService executor = Executors.newSingleThreadExecutor();
		if (executor == null) {
			executor = Executors.newCachedThreadPool();
		}

		// lambda式のタスクの中
		// 1. heavyWorkTaskを実行し、型TのheavyWorkResultを受け取る
		// 2.
		// afterHeavyWorkTaskをheavyWorkResultとresultQueueを引数にカリー化でwaitingTaskにする
		// 3. heavyWorkResultをresultQueueにaddする
		// 4. waitingTaskをwaitingTaskQueueにaddする
		executor.submit(() -> {
			// heavyWorkControlTaskを非同期で実行
			try {
				final T heavyWorkResult = heavyWorkTask.call();
				waitingTaskQueue.add(() -> {
					afterHeavyWorkTask.accept(heavyWorkResult);
				});

			} catch (Exception e) {
				if (errorHandler == null) {
					e.printStackTrace();
				} else {
					errorHandler.apply(e);
				}
			}

		});
	}

	public void dispatch() {
		Runnable task = null;
		while ((task = waitingTaskQueue.poll()) != null) {
			task.run();
		}
	}

	public void dispatchAsync() {
		if(dispatchExecutor==null){
			dispatchExecutor=Executors.newWorkStealingPool();
		}

		dispatchExecutor.execute(() -> {
			Runnable task = null;
			try {
				while (isDispatchThread) {
					task = waitingTaskQueue.take();
					task.run();
				}
			} catch (InterruptedException e) {

			}
		});

	}

	public void stopDispatchThread() {
		isDispatchThread = false;
		waitingTaskQueue.add(() -> {
		});
	}

	public void setErrorHandle(Function<Exception, Void> errorHandle) {
		this.errorHandler = errorHandle;
	}

	public void clearErrorHandle() {
		this.errorHandler = null;
	}

	Function<Exception, Void> getErrorHandle() {
		return this.errorHandler;
	}

	public static void main(String[] args) {

	}
}
