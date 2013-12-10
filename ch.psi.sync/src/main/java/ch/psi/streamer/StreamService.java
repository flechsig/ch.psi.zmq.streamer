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

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseBroadcaster;
import org.glassfish.jersey.media.sse.SseFeature;

import com.google.common.eventbus.Subscribe;

import ch.psi.streamer.model.SendCount;
import ch.psi.streamer.model.StreamRequest;
import ch.psi.streamer.model.StreamStatus;

@Path("/")
public class StreamService {
	
	private static final Logger logger = Logger.getLogger(StreamService.class.getName());

	@Inject
	private StreamMap streams;
	
	@Inject
	private SseBroadcaster broadcaster;
	
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
	public void stream(@PathParam("trackingid") final String trackingid, final StreamRequest request){
		// TODO #frames to take, map header info, path
		
		if(streams.containsKey(trackingid)){
			logger.info(String.format("Stream %s already exists. Stopping existing stream and starting new one.",trackingid));
			Stream s = streams.get(trackingid);
			s.stop();
		}
		
		final Stream stream = new Stream();
		stream.stream(request);
		streams.put(trackingid, stream);
		
		// Auto termination
		stream.getStatusBus().register( new Object(){
			@Subscribe
			public void onSend(SendCount status){
				if(request.getNumberOfImages()>0 && status.getCount()==request.getNumberOfImages()){
					logger.info("Reached image "+status.getCount()+" of "+request.getNumberOfImages()+". Stopping streaming");
					stream.stop();
					streams.remove(trackingid);
					
					// Broadcast new stream list
					OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
			        OutboundEvent event = eventBuilder.name("stream")
			            .mediaType(MediaType.APPLICATION_JSON_TYPE)
			            .data(List.class, getStreams())
			            .build();
			        broadcaster.broadcast(event);
				}
			}
		});
		
		
		
		// Broadcast new stream list
		OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
        OutboundEvent event = eventBuilder.name("stream")
            .mediaType(MediaType.APPLICATION_JSON_TYPE)
            .data(List.class, getStreams())
            .build();
        broadcaster.broadcast(event);
	}
	
	@GET
	@Path("stream/{trackingid}")
	public StreamStatus getStreamStatus(@PathParam("trackingid") final String trackingid){
		return streams.get(trackingid).getStatus();
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
		
		// Broadcast new stream list
		OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
        OutboundEvent event = eventBuilder.name("stream")
            .mediaType(MediaType.APPLICATION_JSON_TYPE)
            .data(List.class, getStreams())
            .build();
        broadcaster.broadcast(event);
	}
	
	@GET
	@Path("stream")
	public List<String> getStreams(){
		return(new ArrayList<>(streams.keySet()));
	}
	
	
	@GET
    @Path("stream")
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    public EventOutput subscribe() {
        EventOutput eventOutput = new EventOutput();
        broadcaster.add(eventOutput);
        return eventOutput;
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
