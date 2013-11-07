package ch.psi.sync;

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

		// Option handling
		int port = 8080;

		Options options = new Options();
		options.addOption("h", false, "Help");
		options.addOption("p", true, "Server port (default: "+port+")");

		GnuParser parser = new GnuParser();
		CommandLine line = parser.parse(options, args);

		if (line.hasOption("p")) {
			port = Integer.parseInt(line.getOptionValue("p"));
		}
		if (line.hasOption("h")) {
			HelpFormatter f = new HelpFormatter();
			f.printHelp("hub", options);
			return;
		}

		URI baseUri = UriBuilder.fromUri("http://" + InetAddress.getLocalHost().getHostName() + "/").port(port).build();

		ResourceConfig resourceConfig = new ResourceConfig(SseFeature.class, JacksonFeature.class);
		resourceConfig.packages(StreamService.class.getPackage().getName());
		resourceConfig.register(new ResourceBinder());
		HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, resourceConfig);

		// Static content
		String home = System.getenv("SYNC_BASE");
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
			public void handle(Signal sig) {
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