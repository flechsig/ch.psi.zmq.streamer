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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jeromq.ZMQ;

import com.google.common.eventbus.Subscribe;

/**
 *	Sender to send files via ZeroMQ which are selected to be send.
 *	The sender receives the file path to transfer, reads the file
 *	send the content and then deletes the file (optional)
 */
public class FileSender {
	
	private static final Logger logger = Logger.getLogger(FileSender.class.getName());
	
	private ZMQ.Context context;
	private ZMQ.Socket socket;
	
	public void start(int port){
		context = ZMQ.context(1);
		socket = context.socket(ZMQ.PUSH);
		socket.bind("tcp://*:"+port);
	}
	
	@Subscribe
	public void onFile(Path file){
		logger.info("Sending file: "+file);
		socket.sendMore("{\"filename\" : \""+file.getFileName()+"\", \"type\":\"raw\"}");
		try {
			socket.send(Files.readAllBytes(file));
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Unable to send file",e);
		}
	}
	
	public void terminate(){
		socket.close();
		context.term();
	}
}
