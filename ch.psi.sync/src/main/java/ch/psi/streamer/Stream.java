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

import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import ch.psi.streamer.model.StreamRequest;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

/**
 * Streamer to stream out files from a given directory to ZMQ.
 * The streamer is split into a DirectoryWatchDog and a file sender.
 * Each of them are running in a separate thread.
 * 
 */
public class Stream {
	
	private static final Logger logger = Logger.getLogger(Stream.class.getName());
	
	private EventBus bus;
	private DirectoryWatchDog wdog;
	private ExecutorService wdogExecutor = Executors.newSingleThreadExecutor();
	private FileSender sender;
	
	/**
	 * Start streaming data out to ZMQ
	 */
	public void stream(final StreamRequest request){
		
		bus = new AsyncEventBus(Executors.newSingleThreadExecutor());
		wdog = new DirectoryWatchDog(bus);
		sender = new FileSender(request.getPort(), request.isWipeFile());
		
		logger.info("Start streaming [Options: wipe="+request.isWipeFile()+"] ...");
		sender.setPath(request.getDestinationPath());
		sender.setHeader(request.getHeader());
		sender.start();
		bus.register(sender);
		
		wdogExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					wdog.watch(FileSystems.getDefault().getPath(request.getSearchPath()), request.getSearchPattern());
				} catch (IOException | InterruptedException e) {
					throw new RuntimeException("Unable to start watching path",e);
				}
			}
		});
	}
	
	
	/**
	 * Stop datastream
	 */
	public void stop(){
		logger.info("... terminate streaming");
		
		if(bus!=null && sender!=null && wdog!=null){
			bus.unregister(sender);
			sender.terminate();
			wdog.terminate();
			wdogExecutor.shutdown();
		}
		
		bus= null;
		sender=null;
		wdog = null;
		
		logger.info("Streaming terminated");
	}
	
}
