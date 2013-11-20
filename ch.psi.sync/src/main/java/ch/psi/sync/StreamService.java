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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ch.psi.sync.model.StreamRequest;

@Path("/")
public class StreamService {
	
	private static final Logger logger = Logger.getLogger(StreamService.class.getName());

	@Inject
	private StreamMap streams;
	
	@GET
    @Path("version")
    @Produces(MediaType.TEXT_PLAIN)
    public String getVersion(){
    	String version = getClass().getPackage().getImplementationVersion();
    	if(version==null){
    		version="0.0.0";
    	}
    	return version;
    }
	
	/**
	 * Start streaming for a given tracking id
	 * @param trackingid
	 * @param path
	 */
	@PUT
	@Path("stream/{trackingid}")
	public void stream(@PathParam("trackingid") String trackingid, StreamRequest request){
		// TODO #frames to take, map header info, path
		
		if(streams.containsKey(trackingid)){
			logger.info(String.format("Stream %s already exists. Stopping existing stream and starting new one.",trackingid));
			Stream s = streams.get(trackingid);
			s.stop();
		}
		
		Stream stream = new Stream();
		stream.stream(request);
		streams.put(trackingid, stream);
	}
	
	/**
	 * Terminate the streaming for a given tracking id
	 * @param trackingid
	 */
	@DELETE
	@Path("stream/{trackingid}")
	public void terminate(@PathParam("trackingid") String trackingid){
		Stream stream = streams.get(trackingid);
		if(stream==null){
			throw new NotFoundException();
		}
		stream.stop();
		streams.remove(trackingid);
	}
	
	@GET
	@Path("stream")
	public List<String> getStreams(){
		return(new ArrayList<>(streams.keySet()));
	}
	
//	/**
//	 * Get information of the stream
//	 * - #images to transfer
//	 * - transfered so far
//	 * - optional header entries
//	 * 
//	 * will return a does not exist if transfer is done
//	 * 
//	 * @param trackingid
//	 */
//	@GET
//	@Path("{trackingid}")
//	public void getStreamInfo(@PathParam("trackingid") String trackingid){
//		
//	}
//	
//	/**
//	 * Get list of transfers and its status
//	 */
//	@GET
//	@Path("")
//	public void getInfo(){
//		
//	}
}
