package ch.psi.streamer;


import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.sse.SseBroadcaster;

public class StreamServerResourceBinder extends AbstractBinder {

	@Override
	protected void configure() {
		bind(new StreamMap()).to(StreamMap.class);
		bind(new SseBroadcaster()).to(SseBroadcaster.class);
	}

}