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
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jeromq.ZMQ;

/**
 *	Sender to send files via ZeroMQ which are selected to be send.
 *	The sender receives the files to transfer in a in queue, reads the file
 *	transfers the content and then deletes the file (optional)
 */
public class FileSender {
	
	private static final Logger logger = Logger.getLogger(FileSender.class.getName());
	
	private BlockingQueue<Path> queue;
	private int port;
	
	private ZMQ.Context context;
	private ZMQ.Socket socket;
	
	public FileSender(int port, BlockingQueue<Path> queue){
		this.port = port;
		this.queue = queue;
	}
	
	public void send(){
		context = ZMQ.context(1);
		socket = context.socket(ZMQ.PUSH);
		socket.bind("tcp://*:"+port);
		
		while(true){
			try{
				Path file = queue.take();
				
				logger.info("Sending file: "+file);
				// Send header
				socket.sendMore("{\"filename\" : \""+file.getFileName()+"\", \"type\":\"raw\"}");
				// Send data
				socket.send(Files.readAllBytes(file));
				
			} catch (InterruptedException e) {
				break;
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Unable to read file", e);
			}
		}
		socket.close();
		context.term();
	}
	
	public void terminate(){
		queue.notifyAll();
	}
	
	
	
	public static void main(String[] args) {

    	String directory = ".";
    	String pattern = "glob:*";
    	
    	if(args.length>0){
    		directory = args[0];
    		if(args.length>1){
    			pattern = args[1];
    		}
    	}
    	
    	final BlockingQueue<Path> q = new ArrayBlockingQueue<>(50);
        final DirectoryWatchService watch = new DirectoryWatchService(q);
        final FileSender sender = new FileSender(8080, q);

        
        new Thread(new Runnable() {
			@Override
			public void run() {
				sender.send();
			}
		}).start();
        
        
        try {
            watch.watch(Paths.get(directory), pattern);
        } catch (IOException | InterruptedException ex) {
            System.err.println(ex);
        }

    }

}
