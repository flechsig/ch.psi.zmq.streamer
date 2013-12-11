package ch.psi.streamer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.server.ResourceConfig;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;

import sun.misc.Signal;
import sun.misc.SignalHandler;

public class StreamServer {
	
	private static final Logger logger = Logger.getLogger(StreamServer.class.getName());

	public static void main(String[] args) throws IOException, ParseException {

		int webserverPort = 8080;
		String hostname;
		
		Options options = new Options();
		options.addOption("h", false, "Help");
		options.addOption("p", true, "Webserver port (default: "+webserverPort+")");
		options.addOption("s", true, "Webserver ip");

		GnuParser parser = new GnuParser();
		CommandLine line = parser.parse(options, args);

		if (line.hasOption("p")) {
			webserverPort = Integer.parseInt(line.getOptionValue("p"));
		}
		if (line.hasOption("s")) {
			hostname=line.getOptionValue("s");
		}
		else{
			hostname = InetAddress.getLocalHost().getHostName();
		}
		if (line.hasOption("h")) {
			HelpFormatter f = new HelpFormatter();
			f.printHelp("streamer", options);
			return;
		}

		
		
		URI baseUri = UriBuilder.fromUri("http://" + hostname + "/").port(webserverPort).build();

		ResourceConfig resourceConfig = new ResourceConfig(SseFeature.class, JacksonFeature.class);
		resourceConfig.register(StreamService.class);
		resourceConfig.register(new StreamServerResourceBinder());
		HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, resourceConfig);

		// Static content
		String home = System.getenv("STREAMER_BASE");
		if (home == null) {
			home = "src/main/assembly";
		}
		home = home + "/www";
		server.getServerConfiguration().addHttpHandler(new StaticHttpHandler(home), "/static");

		logger.info("Sync started");
		logger.info(String.format("Management interface available at %sstatic/", baseUri));
		logger.info("Use ctrl+c to stop ...");

		// Signal handling
		final CountDownLatch latch = new CountDownLatch(1);
		Signal.handle(new Signal("INT"), new SignalHandler() {
			int count = 0;
			public void handle(Signal sig) {
				count++;
				
				if(count >1){
					logger.info("Force termination of JVM");
					System.exit(-1);
				}

				latch.countDown();
			}
		});

		// Wait for termination, i.e. wait for ctrl+c
		try {
			latch.await();
		} catch (InterruptedException e) {
		}

		server.stop();
	}
}