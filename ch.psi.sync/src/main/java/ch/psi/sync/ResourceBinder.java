package ch.psi.sync;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class ResourceBinder extends AbstractBinder {

	@Override
	protected void configure() {
		bind(new Streamer()).to(Streamer.class);
	}

}