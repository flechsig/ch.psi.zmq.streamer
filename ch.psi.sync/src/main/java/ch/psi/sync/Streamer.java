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
package ch.psi.sync;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ch.psi.sync.model.StreamRequest;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

/**
 * Streamer to stream out files from a given directory to ZMQ.
 * The streamer is split into a DirectoryWatchDog and a file sender.
 * Each of them are running in a separate thread.
 * 
 */
public class Streamer {
	
	private EventBus bus;
	private DirectoryWatchDog wdog;
	private Executor wdogExecutor = Executors.newSingleThreadExecutor();
	private FileSender sender;
	
	public Streamer(){
		bus = new AsyncEventBus(Executors.newSingleThreadExecutor());
		wdog = new DirectoryWatchDog(bus);
		sender = new FileSender();
		
	}

	/**
	 * Start streaming data out to ZMQ
	 */
	public void stream(StreamRequest request){
		final Path path = FileSystems.getDefault().getPath(request.getSearchPath());
		final String pattern = request.getSearchPattern();
		bus.register(sender);
		wdogExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					wdog.watch(path, pattern);
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
		bus.unregister(sender);
		wdog.terminate();
	}
	
}
