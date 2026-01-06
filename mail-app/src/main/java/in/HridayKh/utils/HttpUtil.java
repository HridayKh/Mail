package in.HridayKh.utils;

import java.util.Map;

import in.HridayKh.models.entities.Domain;
import in.HridayKh.models.entities.Folder;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class HttpUtil {

	public static Response errorResponse(Response.Status status, String msg) {
		return Response.status(status).entity(Map.of("error", msg)).build();
	}

	public static Domain requireDomain(String domainId) {
		if (domainId == null || domainId.isBlank())
			throw new WebApplicationException(
					errorResponse(Response.Status.BAD_REQUEST, "Domain ID is required"));
		long id;
		try {
			id = Long.parseLong(domainId);
		} catch (NumberFormatException e) {
			throw new WebApplicationException(errorResponse(Response.Status.BAD_REQUEST,
					"Invalid domain ID format: must be a number"));
		}
		if (id <= 0)
			throw new WebApplicationException(errorResponse(Response.Status.BAD_REQUEST,
					"Domain ID must be a positive number"));
		Domain domain = Domain.findById(id);
		if (domain == null)
			throw new WebApplicationException(errorResponse(Response.Status.NOT_FOUND, "Domain not found"));
		return domain;
	}

	public static Folder requireFolder(String folderId) {
		if (folderId == null || folderId.isBlank())
			throw new WebApplicationException(
					errorResponse(Response.Status.BAD_REQUEST, "Folder ID is required"));
		long id;
		try {
			id = Long.parseLong(folderId);
		} catch (NumberFormatException e) {
			throw new WebApplicationException(
					errorResponse(Response.Status.BAD_REQUEST,
							"Invalid folder ID format: must be a number"));
		}
		if (id <= 0)
			throw new WebApplicationException(
					errorResponse(Response.Status.BAD_REQUEST,
							"Folder ID must be a positive number"));
		Folder f = Folder.findById(id);
		if (f == null)
			throw new WebApplicationException(
					errorResponse(Response.Status.NOT_FOUND, "Folder not found"));
		return f;
	}

}
