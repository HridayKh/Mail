package in.HridayKh.api;

import java.util.List;
import java.util.Map;
import org.jboss.resteasy.reactive.RestResponse.StatusCode;

import in.HridayKh.entities.Domain;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/v1/domains")
public class Domains {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Domain> listDomains() {
		List<Domain> domains = Domain.listAll();
		return domains;
	}

	@POST
	@Transactional
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createDomains(Domain domain) {
		if (domain == null || domain.name == null || domain.name.isBlank())
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(Map.of("error", "empty Domain name not allowed"))
					.type(MediaType.APPLICATION_JSON)
					.build();

		domain.name = domain.name.toLowerCase();

		if (Domain.find("name", domain.name).firstResult() != null)
			return Response.status(Response.Status.CONFLICT)
					.entity(Map.of("error", "Domain with name " + domain.name + " already exists."))
					.type(MediaType.APPLICATION_JSON)
					.build();


		domain.persist();
		return Response.status(Response.Status.CREATED)
				.entity(domain)
				.build();
	}

}
