/**
 * 
 * Copyright 2013 Paul Scherrer Institute. All rights reserved.
 * 
 * This code is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This code is distributed in the hope that it will be useful, but without any
 * warranty; without even the implied warranty of merchantability or fitness for
 * a particular purpose. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package ch.psi.streamer;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import org.zeromq.ZMQ;

public class PushPullTest {
	
	private static final Logger logger = Logger.getLogger(PushPullTest.class.getName());
	
	public static void main(String[] args) throws InterruptedException{
		
		CountDownLatch latch = new CountDownLatch(1);
		
		Thread sender = new Thread(new Sender(latch));
		Thread receiver = new Thread(new Receiver(latch));
		
		// Start sender before receiver and use latch to ensure that receiver has connected 
		// before sender is sending messages
		sender.start();
		logger.info("Sender started");
		receiver.start();
		logger.info("Receiver started");
		
		sender.join();
		logger.info("Sender terminated");
		receiver.join();
		logger.info("Receiver terminated");
	}
	
	
}
class Sender implements Runnable {
	
	private static final Logger logger = Logger.getLogger(Sender.class.getName());

	private CountDownLatch latch;
	
	public Sender(CountDownLatch latch){
		this.latch = latch;
	}
	
	@Override
	public void run() {
		int port = 9999;
		String address = "tcp://*:"+port;
		
		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket socket = context.socket(ZMQ.PUSH);
		
		// Socket options
		socket.setLinger(1000);
		
		socket.bind(address);
       	
		// Ensure that receiver is "connected" before sending
		try {
			latch.await();
		} catch (InterruptedException e) {
		}
		
		for(int i=0;i<=10;i++){
			socket.send("data"+i);
			logger.info("send >> data"+i);
		}

		socket.close();
		context.close();
	}
}

class Receiver implements Runnable {
	
	private static final Logger logger = Logger.getLogger(Receiver.class.getName());

	private CountDownLatch latch;
	
	public Receiver(CountDownLatch latch){
		this.latch = latch;
	}
	
	@Override
	public void run() {
		int port = 9999;
		String address = "tcp://localhost:"+port;
		
		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket socket = context.socket(ZMQ.PULL);
		
		// Options Section
		socket.setRcvHWM(1);
		
		socket.connect(address);
		
		latch.countDown();
		
		int counter = 0;
		while(counter <= 10){
			socket.base().errno();
			String s = new String(socket.recv());
			logger.info("received >> "+s);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			counter++;
		}
		
		socket.close();
		context.close();
	}
}
