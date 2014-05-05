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
package ch.psi.zmq.streamer.model;

import java.util.List;
import java.util.Map;

public class StreamRequest {
	
	private List<StreamSource> source;

	private Map<String,String> header;
	
	private int port = 8888;
	private long highWaterMark = 1000;
	private boolean wipeFile = true;
	
	public String method = "push/pull";
	
	public List<StreamSource> getSource() {
		return source;
	}
	public void setSource(List<StreamSource> source) {
		this.source = source;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public long getHighWaterMark() {
		return highWaterMark;
	}
	public void setHighWaterMark(long highWaterMark) {
		this.highWaterMark = highWaterMark;
	}
	public boolean isWipeFile() {
		return wipeFile;
	}
	public void setWipeFile(boolean wipeFile) {
		this.wipeFile = wipeFile;
	}
	public Map<String, String> getHeader() {
		return header;
	}
	public void setHeader(Map<String, String> header) {
		this.header = header;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		method = method.toLowerCase();
		if(method.equals("pub/sub")){
			this.method = method;
		}
		else if (method.equals("push/pull")){
			this.method = method;
		}
		else{
			this.method = "push/pull";
		}
	}
}
