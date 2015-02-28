package jp.co.stofu.kamaitachi;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SwingAwaiterTester extends JFrame {

	/**
	 *
	 */
	private static final long serialVersionUID = -5885035566423727127L;

	Timer mainTimer;
	Timer neighborTimer;
	Timer dispatchTimer;
	JLabel label;
	int mainSec;
	int neighborSec;
	Awaiter awaiter;
	Thread mainThread;

	SwingAwaiterTester() {
		label = new JLabel();

		JPanel labelPanel = new JPanel();
		labelPanel.add(label);
		label.setText("dummy");
		getContentPane().add(labelPanel, BorderLayout.CENTER);
		awaiter = new Awaiter();

		init();
	}
	public void init(){
		mainSec = 0;
		neighborSec = 0;

	}

	public synchronized void sleep() {
		try {
			wait();
		} catch (InterruptedException e) {
			System.out.println("返事がない。ただの屍のようだ");
		}
	}

	public String getTime() {
		Date date = new Date(System.currentTimeMillis());
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

		return df.format(date);
	}

	public String doHeavyWork(String input) {
		System.out.println(getTime() + " doHeavyWork start. "
				+ Thread.currentThread().getName() + " ");
		doSomeWork(5000); // 5秒のwait
		System.out.println(getTime() + " doHeavyWork done. "
				+ Thread.currentThread().getName() + " ");
		return input + "ratta!! ";
	}

	public void doSomeWork(int millsec) {
		try {
			Thread.sleep(millsec);
		} catch (InterruptedException e) {
			System.out.println("返事がない。ただの屍のようだ");
		}
	}

	public synchronized void stopAllTimer() {
		mainTimer.stop();
		neighborTimer.stop();
		dispatchTimer.stop();
		this.notifyAll();

	}

	public void runSynchrounousStackTest() {
		init();

		ActionListener dispatchThread = e -> {
			awaiter.dispatch();
		};
		ActionListener mainThread = e -> {
			System.out.println(getTime() + " Main Thread start:"
					+ Thread.currentThread().getName() + " " + mainSec
					+ " sec ");

			String heavyWorkResult = doHeavyWork("hoge");
			System.out.println(getTime() + " afterHeavyWork start. "
					+ Thread.currentThread().getName() + " " + heavyWorkResult);
			if (mainSec >= 10) {
				stopAllTimer();
			} else {
				this.mainSec++;
			}
			System.out.println(getTime() + " afterHeavyWork done. "
					+ Thread.currentThread().getName() + " " + heavyWorkResult);

			System.out.println(getTime() + " Main Thread done.:"
					+ Thread.currentThread().getName() + " " + mainSec
					+ " sec ");

		};
		ActionListener neighborThread = e -> {
			System.out.println(getTime() + " Neighbor Thread start:"
					+ Thread.currentThread().getName() + " " + neighborSec
					+ " sec ");

			neighborSec++;
			System.out.println(getTime() + " Neighbor Thread done:"
					+ Thread.currentThread().getName() + " " + neighborSec
					+ " sec ");
		};

		mainTimer = new Timer(1000, mainThread);
		mainTimer.start();
		neighborTimer = new Timer(1000, neighborThread);
		neighborTimer.start();
		dispatchTimer = new Timer(1000, dispatchThread);
		dispatchTimer.start();
		sleep();
	}

	public void runASynchrounousStacklessTest() {
		init();

		ActionListener dispatchThread = e -> {
			awaiter.dispatch();
		};
		ActionListener mainThread = e -> {
			System.out.println(getTime() + " Main Thread start:"
					+ Thread.currentThread().getName() + " " + mainSec
					+ " sec ");

			awaiter.awaitVoid(
					() -> doHeavyWork("hoge"),
					heavyWorkResult -> {
						System.out.println(getTime()
								+ " afterHeavyWork start. "
								+ Thread.currentThread().getName() + " "
								+ heavyWorkResult);
						if (mainSec >= 10) {
							// this.mainTimer.stop();
							stopAllTimer();
						} else {
							this.mainSec++;
						}
						System.out.println(getTime() + " afterHeavyWork done. "
								+ Thread.currentThread().getName() + " "
								+ heavyWorkResult);

					});
			System.out.println(getTime() + " Main Thread done.:"
					+ Thread.currentThread().getName() + " " + mainSec
					+ " sec ");

		};
		ActionListener neighborThread = e -> {
			System.out.println(getTime() + " Neighbor Thread start:"
					+ Thread.currentThread().getName() + " " + neighborSec
					+ " sec ");

			neighborSec++;
			System.out.println(getTime() + " Neighbor Thread done:"
					+ Thread.currentThread().getName() + " " + neighborSec
					+ " sec ");
		};

		mainTimer = new Timer(1000, mainThread);
		mainTimer.start();
		neighborTimer = new Timer(1000, neighborThread);
		neighborTimer.start();
		dispatchTimer = new Timer(1000, dispatchThread);
		dispatchTimer.start();
		sleep();
	}


	public static void main(String[] args) {
		System.out.println("main start!");
		SwingAwaiterTester tester = new SwingAwaiterTester();

		tester.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		tester.setBounds(10, 10, 250, 100);
		tester.setTitle("Dummy Window");
		tester.setVisible(true);


		System.out.println("synchronous stack test start!");
		tester.runSynchrounousStackTest();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("asynchronous stackless test start!");
		tester.runASynchrounousStacklessTest();

		System.out.println("main end!");
	}

}