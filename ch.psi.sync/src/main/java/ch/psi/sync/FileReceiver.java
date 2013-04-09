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

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jeromq.ZMQ;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *	ZeroMQ Receiver which accepts the a file from a ZeroMQ message and saves it to a predefined directory.
 */
public class FileReceiver {
	
	private static final Logger logger = Logger.getLogger(FileReceiver.class.getName());

	private ZMQ.Context context;
	private ZMQ.Socket socket;
	
	private String hostname;
	private int port;
	private String basedir;
	private volatile boolean receive = true;
	
	public FileReceiver(String hostname, int port, String basedir){
		this.hostname = hostname;
		this.port = port;
		this.basedir = basedir;
	}
	
	public void receive(){
		receive = true;
		context = ZMQ.context(1);
		socket = context.socket(ZMQ.PULL);
		socket.connect("tcp://"+hostname+":"+port);
		
		ObjectMapper mapper = new ObjectMapper();
		
		while(receive){
			try{
				byte[] message = socket.recv();
				byte[] content = null;
				if (socket.hasReceiveMore()) {
					content = socket.recv();
				}
				logger.info(new String(message));
				
				Header h = mapper.readValue(message, Header.class);
				
				if(!h.getType().equals("raw")){
					logger.warning("Message type ["+h.getType()+"] not supported");
					continue;
				}
				// TODO Save content to file (in basedir)
				String file = basedir+"/"+h.getFilename(); // TODO remove
				System.out.println("Write to "+file);
			
				try(FileOutputStream s = new FileOutputStream(file)){
					s.write(content);
				}
				
			} catch (IOException e) {
				logger.log(Level.SEVERE,"",e);
			}
		}
		socket.close();
		context.term();
	}
	
	public void terminate(){
		receive=false;
	}
	

	public static void main(String[] args) {
		FileReceiver r = new FileReceiver("localhost", 8080, ".");
		r.receive();
	}

}
