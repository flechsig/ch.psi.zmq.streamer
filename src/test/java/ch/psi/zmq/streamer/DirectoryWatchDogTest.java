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
package ch.psi.zmq.streamer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.junit.Ignore;
import org.junit.Test;

import ch.psi.zmq.streamer.DirectoryWatchDog;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class DirectoryWatchDogTest {

	private static final Logger logger = Logger.getLogger(DirectoryWatchDogTest.class.getName());

	@Ignore
	@Test
	public void test() throws IOException, InterruptedException {

		// String directory = ".";
		String directory = "target";
		String pattern = "glob:*";

		logger.info("Watching '" + directory + "' for file pattern '" + pattern + "'");

		EventBus b = new AsyncEventBus(Executors.newFixedThreadPool(1));
		final DirectoryWatchDog watch = new DirectoryWatchDog(b);

		b.register(new Object() {

			@Subscribe
			public void log(Path p) {
				logger.info("Process " + p + " ...");
			}

		});

		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				logger.info("Terminate watch");
				watch.terminate();
			}
		}, 1000);
		
		watch.watch(Paths.get(directory), pattern, "", 0);
		logger.info("Done");
	}

}
