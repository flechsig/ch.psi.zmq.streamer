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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import ch.psi.streamer.model.StreamRequest;
import ch.psi.streamer.model.StreamSource;
import ch.psi.streamer.model.StreamStatus;

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
	private List<DirectoryWatchDog> wdogs;
	private ExecutorService wdogExecutor = Executors.newSingleThreadExecutor();
	private FileSender sender;
	private EventBus statusBus;
	
	private StreamRequest configuration;
	
	/**
	 * Start streaming data out to ZMQ
	 */
	public void stream(final StreamRequest request){
		
		this.configuration = request;
		
		statusBus = new AsyncEventBus(Executors.newSingleThreadExecutor());
		statusBus.register(this);
		
		bus = new AsyncEventBus(Executors.newSingleThreadExecutor());
		
		sender = new FileSender(statusBus, request.getPort(), request.getHighWaterMark(), request.isWipeFile());
		
		
		
		logger.info("Start streaming [Options: wipe="+request.isWipeFile()+"] ...");
		sender.setHeader(request.getHeader());
		sender.start();
		bus.register(sender);
		
		wdogs = new ArrayList<>();
		wdogExecutor = Executors.newFixedThreadPool(request.getSource().size());
		for(final StreamSource ssource: request.getSource()){
			final DirectoryWatchDog wdog = new DirectoryWatchDog(bus);
			wdogs.add(wdog);
			wdogExecutor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						wdog.watch(FileSystems.getDefault().getPath(ssource.getSearchPath()), ssource.getSearchPattern(), ssource.getDestinationPath(), ssource.getNumberOfImages());
					} catch (IOException | InterruptedException e) {
						throw new RuntimeException("Unable to start watching path",e);
					}
				}
			});
		}
	}
	
	
	/**
	 * Stop datastream
	 */
	public void stop(){
		logger.info("... terminate streaming");
		
		if(bus!=null && sender!=null && wdogs!=null){
			bus.unregister(sender);
			sender.terminate();
			for(DirectoryWatchDog wdog: wdogs){
				wdog.terminate();
			}
			wdogExecutor.shutdown();
		}
		
		bus= null;
		sender=null;
		wdogs = null;
		
		logger.info("Streaming terminated");
	}
	
	public EventBus getStatusBus(){
		return statusBus;
	}
	
	public StreamStatus getStatus(){
		return new StreamStatus(sender.getCount());
	}
	
	public StreamRequest getConfiguration(){
		return configuration;
	}
}
