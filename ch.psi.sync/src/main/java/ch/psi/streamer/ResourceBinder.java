package ch.psi.streamer;


import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class ResourceBinder extends AbstractBinder {

	@Override
	protected void configure() {
		bind(new StreamMap()).to(StreamMap.class);
	}

}