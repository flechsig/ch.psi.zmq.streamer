package ch.psi.sync;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
//import org.glassfish.jersey.media.sse.SseBroadcaster;

//import ch.psi.da.hub.model.Keystore;

public class ResourceBinder extends AbstractBinder {

    @Override
    protected void configure() {
        // request scope binding
//        bind(MyInjectablePerRequest.class).to(MyInjectablePerRequest.class).in(RequestScope.class);
        // singleton binding
//        bind(MyInjectableSingleton.class).in(Singleton.class);
        // singleton instance binding
    	
    	
    	
//    	KeystoreSerializer serializer = new KeystoreSerializer();
//    	Keystore keystore = serializer.deserialize();
//    	
//        bind(new SseBroadcaster()).to(SseBroadcaster.class);
//        bind(serializer).to(KeystoreSerializer.class);
//        bind(new KeystoreBroadcaster()).to(KeystoreBroadcaster.class);
//        bind(new KeystoreDiffBroadcaster()).to(KeystoreDiffBroadcaster.class);
//        bind(keystore).to(Keystore.class);
    }

}