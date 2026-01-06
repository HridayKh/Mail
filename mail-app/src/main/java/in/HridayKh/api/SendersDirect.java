package in.HridayKh.api;

import java.util.List;
import java.util.Map;

import in.HridayKh.models.SenderIdentity;
import in.HridayKh.utils.HttpUtil;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/v1/senders")
public class SendersDirect {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<SenderIdentity> listAll() {
		return SenderIdentity.listAll();
	}

	@GET
	@Path("/{sender_id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGlobal(@PathParam("sender_id") String senderId) {
		try {
			long id = Long.parseLong(senderId);
			SenderIdentity s = SenderIdentity.findById(id);
			if (s == null)
				return HttpUtil.errorResponse(Response.Status.NOT_FOUND, "Sender not found");
			return Response.ok(s).build();
		} catch (NumberFormatException e) {
			return HttpUtil.errorResponse(Response.Status.BAD_REQUEST, "Invalid sender id format");
		}
	}

	@PATCH
	@Path("/{sender_id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Response update(@PathParam("sender_id") String senderId,
			SenderIdentity body) {
		try {
			long id = Long.parseLong(senderId);
			SenderIdentity s = SenderIdentity.findById(id);
			if (s == null)
				return HttpUtil.errorResponse(Response.Status.NOT_FOUND, "Sender not found");

			if (body.displayName != null)
				s.displayName = body.displayName;
			if (body.email != null)
				s.email = body.email.trim().toLowerCase();
			if (body.isDefault) {
				SenderIdentity prev = SenderIdentity.find("isDefault = true").firstResult();
				if (prev != null && !prev.id.equals(s.id)) {
					prev.isDefault = false;
					prev.persist();
				}
				s.isDefault = true;
			}

			s.persist();
			return Response.ok(s).build();
		} catch (NumberFormatException e) {
			return HttpUtil.errorResponse(Response.Status.BAD_REQUEST, "Invalid sender id format");
		}
	}

	@DELETE
	@Path("/{sender_id}")
	@Transactional
	public Response deleteGlobal(@PathParam("sender_id") String senderId) {
		try {
			long id = Long.parseLong(senderId);
			SenderIdentity s = SenderIdentity.findById(id);
			if (s == null)
				return HttpUtil.errorResponse(Response.Status.NOT_FOUND, "Sender not found");
			s.delete();
			return Response.ok(Map.of("status", "deleted", "id", id)).build();
		} catch (NumberFormatException e) {
			return HttpUtil.errorResponse(Response.Status.BAD_REQUEST, "Invalid sender id format");
		}
	}

}
