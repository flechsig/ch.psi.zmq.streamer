package ch.psi.sync;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class ResourceBinder extends AbstractBinder {

	private StreamerConfiguration configuration;
	
	public ResourceBinder(StreamerConfiguration configuration){
		this.configuration = configuration;
	}
	
	@Override
	protected void configure() {
		bind(new Streamer(configuration)).to(Streamer.class);
	}

}