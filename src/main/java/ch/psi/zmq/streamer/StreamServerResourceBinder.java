package ch.psi.zmq.streamer;


import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.sse.SseBroadcaster;

public class StreamServerResourceBinder extends AbstractBinder {
	
	private StreamServerConfiguration config;
	
	public StreamServerResourceBinder(StreamServerConfiguration config){
		this.config = config;
	}

	@Override
	protected void configure() {
		bind(config).to(StreamServerConfiguration.class);
		bind(new StreamMap()).to(StreamMap.class);
		bind(new SseBroadcaster()).to(SseBroadcaster.class);
		bind(new Accountant()).to(Accountant.class);
	}

}