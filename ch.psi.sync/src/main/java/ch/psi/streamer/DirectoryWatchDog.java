package ch.psi.streamer;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import com.google.common.eventbus.EventBus;

public class DirectoryWatchDog {
	
	private static final Logger logger = Logger.getLogger(DirectoryWatchDog.class.getName());
	
	private volatile boolean watch = true;
	private EventBus ebus;
	
	private WatchService watchService;

	@Inject
	public DirectoryWatchDog(EventBus ebus){
		this.ebus = ebus;
	}
	
	public void watch(Path path) throws IOException, InterruptedException{
		watch(path, "glob:*");
	}
	
	/**
	 * Watch a given path (i.e. directory) for new files which match a given pattern
	 * 
	 * @param path		Directory to watch
	 * @throws IOException
	 * @throws InterruptedException
	 */
    public void watch(Path path, String pattern) throws IOException, InterruptedException {
    	watch=true;
    	try{
    		watchService = FileSystems.getDefault().newWatchService();
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
            
            //start an infinite loop
            while (watch) {

                //retrieve and remove the next watch key
                final WatchKey key = watchService.take();

                //get list of pending events for the watch key
                for (WatchEvent<?> watchEvent : key.pollEvents()) {

                    //get the kind of event (create, modify, delete)
                    final Kind<?> kind = watchEvent.kind();

                    //handle OVERFLOW event
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                    	logger.severe("Files may not have been detected");
                        continue;
                    }                                        

                    //get the filename for the event
                    @SuppressWarnings("unchecked")
					final WatchEvent<Path> watchEventPath = (WatchEvent<Path>) watchEvent;
                    final Path filename = watchEventPath.context();
                    
                    logger.finest(kind + " -> " + filename);

                    if(matcher.matches(filename)){
                    	ebus.post(path.resolve(filename));
                    }
                }

                //reset the key
                boolean valid = key.reset();

                //exit loop if the key is not valid (if the directory was deleted, for example)
                if (!valid) {
                    break;
                }
            }
        }
    	catch(ClosedWatchServiceException e){
    		// Exception occurs when shutting down the watch service while take() operation is blocking
    		logger.log(Level.INFO, "Watch service was closed");
    	}
    	finally{
    		// Ensure, no matter what, that WatchService is closed to avoid a memory leak
    		try{
    			watchService.close();
    		}
    		catch(Exception e){
    		}
    	}
    }
    
    /**
     * Terminate directory watch
     */
    public void terminate(){
    	watch=false;
    	try {
			watchService.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}