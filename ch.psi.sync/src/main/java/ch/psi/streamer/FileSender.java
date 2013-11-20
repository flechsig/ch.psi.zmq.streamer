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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jeromq.ZMQ;

import com.google.common.eventbus.Subscribe;

/**
 *	Sender to send files via ZeroMQ which are selected to be send.
 *	The sender receives the file path to transfer via the event bus, reads the file
 *	send the content and then optionally (default:true) deletes the file.
 *  
 *  The message send will hold a pilatus-1.0 style message header. For details see 
 *  https://confluence.psi.ch/display/SOF/ZMQ+Data+Streaming
 */
public class FileSender {
	
	private static final Logger logger = Logger.getLogger(FileSender.class.getName());
	
	private int port;
	private ZMQ.Context context;
	private ZMQ.Socket socket;
	
	private String path = "";
	private boolean wipe = true;
	private int sendCount = 0;
	
	public FileSender(int port, boolean wipe){
		this.port = port;
		this.wipe = wipe;
	}
	
	public void start(){
		context = ZMQ.context(1);
		socket = context.socket(ZMQ.PUSH);
		socket.bind("tcp://*:"+port);
		
		sendCount=0;
	}
	
	@Subscribe
	public void onFile(Path file){
		logger.info("Sending file: "+file);
		socket.sendMore("{\"filename\" : \""+file.getFileName()+"\", \"path\":\""+path+"\", \"htype\":\"pilatus-1.0\"}");
		try {
			socket.send(Files.readAllBytes(file));
			sendCount++;
			if(wipe){
				Files.delete(file);
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Unable to send file",e);
		}
	}
	
	public void terminate(){
		socket.close();
		context.term();
	}

	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public boolean isWipe() {
		return wipe;
	}
	public int getSendCount() {
		return sendCount;
	}
}
