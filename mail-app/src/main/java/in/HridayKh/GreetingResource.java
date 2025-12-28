package in.HridayKh;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

@Path("/hello")
public class GreetingResource {

	private static final Logger log = Logger.getLogger(GreetingResource.class);

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String hello() {
		log.warn("Test warn haha!");
		return "Hello from Quarkus REST";
	}
}
