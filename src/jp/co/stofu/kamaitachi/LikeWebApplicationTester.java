package jp.co.stofu.kamaitachi;

import java.util.concurrent.BlockingQueue;

public class LikeWebApplicationTester {
	Awaiter awaiter;

	LikeWebApplicationTester() {
		awaiter = new Awaiter();
		awaiter.dispatchAsync();
	}

	BlockingQueue<Integer> AccessTheWebAsync() {
		HttpClient client = new HttpClient();
		return awaiter.await(
				() -> client.GetStringAsync("http://www.hogecrosoft.com"),
				urlContents -> {
					return urlContents.length();
				});
	}

	public void doget(){
		BlockingQueue<Integer> lengthQueue=AccessTheWebAsync();
		awaiter.awaitVoid(()->lengthQueue.take(),
				length->{
					System.out.println("Length:"+length);
				}
				);
	}

	public static void main(String[] args) {
		LikeWebApplicationTester tester=new LikeWebApplicationTester();
		tester.doget();
	}
}

class HttpClient {
	String GetStringAsync(String url) {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			//e.printStackTrace();
		}
		return "hoge";
	}

}