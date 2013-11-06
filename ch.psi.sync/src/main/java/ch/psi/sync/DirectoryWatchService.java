package ch.psi.sync;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class DirectoryWatchService {
	
	private static final Logger logger = Logger.getLogger(DirectoryWatchService.class.getName());
	
	private volatile boolean watch = true;
	private BlockingQueue<Path> queue;

	public DirectoryWatchService(BlockingQueue<Path> queue){
		this.queue = queue;
	}
	
    public void watch(Path path, String pattern) throws IOException, InterruptedException {
    	watch=true;
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
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
                    	queue.add(path.resolve(filename));
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
    }
    
    public void terminate(){
    	watch=false;
    }

    
    public static void main(String[] args) {

    	String directory = ".";
    	String pattern = "glob:*";
    	
    	if(args.length>0){
    		directory = args[0];
    		if(args.length>1){
    			pattern = args[1];
    		}
    	}
    	
    	File file = new File(directory);
    	if(!file.exists()){
    		throw new IllegalArgumentException("Specified directory ["+directory+"] does not exist.");
    	}
    	if(!file.isDirectory()){
    		throw new IllegalArgumentException("Specified directory ["+directory+"] does not exist");
    	}
    	
    	
    	logger.info("Watching '"+directory +"' for file pattern '"+pattern+"'"  );
    	
    	final BlockingQueue<Path> q = new ArrayBlockingQueue<>(50);
        DirectoryWatchService watch = new DirectoryWatchService(q);

        
        new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					try {
						logger.info("Process "+q.take()+" ...");
					} catch (InterruptedException e) {
						logger.finest("Process got interrupted");
						break;
					}
				}
				
			}
		}).start();
        
        
        try {
            watch.watch(Paths.get(directory), pattern);
        } catch (IOException | InterruptedException ex) {
            System.err.println(ex);
        }

    }
}