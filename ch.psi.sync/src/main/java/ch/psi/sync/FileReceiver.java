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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.jeromq.ZMQ;

import sun.misc.Signal;
import sun.misc.SignalHandler;


/**
 *	ZeroMQ Receiver which accepts the a file from a ZeroMQ message and saves it to a predefined directory.
 *	
 *	The message recieved need to hold a pilatus-1.0 style message header. For details see 
 *  https://confluence.psi.ch/display/SOF/ZMQ+Data+Streaming. If not the message will be ignored.
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
	
	/**
	 * Receive ZMQ messages with pilatus-1.0 header type and write the data part to disk
	 */
	public void receive(){
		receive = true;
		context = ZMQ.context(1);
		socket = context.socket(ZMQ.PULL);
		socket.connect("tcp://"+hostname+":"+port);
		
		ObjectMapper mapper = new ObjectMapper();
		TypeReference<HashMap<String,Object>> typeRef = new TypeReference<HashMap<String,Object>>() {};
		String path = "";
		while(receive){
			try{
				byte[] message = socket.recv();
				byte[] content = null;
				if (socket.hasReceiveMore()) {
					content = socket.recv();
				}
				logger.info(new String(message));
				
				Map<String,Object> h = mapper.readValue(message, typeRef);
				
				if(!"pilatus-1.0".equals(h.get("htype"))){
					logger.warning("Message type ["+h.get("htype")+"] not supported - ignore message");
					continue;
				}
				// TODO Save content to file (in basedir)
				String p = basedir+"/"+(String) h.get("path");
				File f = new File(p);
				if(!path.equals(p)){
					f.mkdirs();
					path = p;
				}
				
				File file = new File(f, (String)h.get("filename")); // TODO remove
				logger.finest("Write to "+file.getAbsolutePath());
			
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
		
		int port = 8888;
		String source = "localhost";
		Options options = new Options();
		options.addOption("h", false, "Help");
		options.addOption("p", true, "Source port (default: "+port+")");
		options.addOption("s", true, "Source (default: "+source+")");

		GnuParser parser = new GnuParser();
		CommandLine line;
		try {
			line = parser.parse(options, args);
			if (line.hasOption("p")) {
				port = Integer.parseInt(line.getOptionValue("p"));
			}
			if (line.hasOption("s")) {
				source = line.getOptionValue("s");
			}
			if (line.hasOption("h")) {
				HelpFormatter f = new HelpFormatter();
				f.printHelp("receiver", options);
				return;
			}
		} catch (ParseException e) {
			logger.log(Level.WARNING, "Unable to parse commandline",e);
		}
		
		
		final FileReceiver r = new FileReceiver(source, port, ".");
		r.receive();
		
		// Control+C
		Signal.handle(new Signal("INT"), new SignalHandler() {
			int count = 0;
			public void handle(Signal sig) {
				if(count <1){
					count++;
					r.terminate();
				}
				else{
					System.exit(-1);
				}
				
			}
		});
	}

}
