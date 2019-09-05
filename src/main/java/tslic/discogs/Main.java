package tslic.discogs;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.jboss.weld.environment.se.Weld;
import tslic.discogs.Resources.Artists;
import tslic.discogs.Resources.Labels;
import tslic.discogs.Resources.Masters;
import tslic.discogs.Resources.Releases;
import tslic.discogs.providers.ConstraintExceptionMapper;
import tslic.discogs.providers.CorsFilter;
import tslic.discogs.providers.ObjectMapperProvider;

public class Main {

  private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

  public static void main(String[] args) {
    int port = args.length == 0 ? 8080 : Integer.parseInt(args[0]);
    URI baseUri = URI.create(String.format("http://0.0.0.0:%s/api", port));

    try {
      Weld weld = new Weld();
      weld.initialize();

      ResourceConfig rc = ResourceConfig.forApplicationClass(DiscogsApplication.class);
      HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, rc, false);

      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(
                  () -> {
                    server.shutdownNow();
                    weld.shutdown();
                  }));

      server.start();

      System.out.println("Server is listening on " + baseUri);
      System.out.println("Stop the application using CTRL+C");

      Thread.currentThread().join();
    } catch (IOException | InterruptedException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
  }

  @ApplicationPath("api")
  public static class DiscogsApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
      Set<Class<?>> classes = new HashSet<>();

      classes.add(Artists.class);
      classes.add(Releases.class);
      classes.add(Masters.class);
      classes.add(Labels.class);

      classes.add(ConstraintExceptionMapper.class);
      classes.add(CorsFilter.class);
      classes.add(ObjectMapperProvider.class);

      return classes;
    }
  }
}
