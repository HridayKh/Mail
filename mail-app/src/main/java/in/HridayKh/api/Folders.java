package in.HridayKh.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.HridayKh.models.Folder;
import in.HridayKh.models.FolderType;
import in.HridayKh.utils.HttpUtil;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/v1/folders")
public class Folders {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Folder> listFolders() {
		return Folder.listAll();
	}

	@POST
	@Transactional
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createFolder(Folder folder) {
		if (folder == null)
			return HttpUtil.errorResponse(Response.Status.BAD_REQUEST, "Request body is required");
		if (folder.name == null || folder.name.isBlank())
			return HttpUtil.errorResponse(Response.Status.BAD_REQUEST, "Folder name is required");
		if (folder.colorHex == null || folder.colorHex.isBlank())
			return HttpUtil.errorResponse(Response.Status.BAD_REQUEST, "colorHex is required");
		if (folder.type == null)
			folder.type = FolderType.CUSTOM;
		else {
			boolean valid = false;
			for (FolderType t : FolderType.values())
				if (t == folder.type) {
					valid = true;
					break;
				}

			if (!valid)
				return HttpUtil.errorResponse(Response.Status.BAD_REQUEST, "Invalid folder type");
		}

		folder.name = folder.name.trim();

		if (folder.type != FolderType.CUSTOM) {
			Folder existing = Folder.find("type", folder.type).firstResult();
			if (existing != null)
				return HttpUtil.errorResponse(Response.Status.CONFLICT,
						"Folder of type " + folder.type + " already exists");
		}

		folder.persist();
		return Response.status(Response.Status.CREATED).entity(folder).build();
	}

	@GET
	@Path("/{folder_id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFolder(@PathParam("folder_id") String folderId) {
		try {
			return Response.ok(HttpUtil.requireFolder(folderId)).build();
		} catch (WebApplicationException e) {
			return e.getResponse();
		}
	}

	@PATCH
	@Path("/{folder_id}")
	@Transactional
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateFolder(@PathParam("folder_id") String folderId, Folder update) {
		try {
			Folder f = HttpUtil.requireFolder(folderId);

			if (update == null)
				return HttpUtil.errorResponse(Response.Status.BAD_REQUEST, "Request body is required");

			if (update.colorHex != null && !update.colorHex.isBlank()) {
				String n = update.name.trim();
				f.name = n;
			}

			if (update.type != null)
				return HttpUtil.errorResponse(Response.Status.BAD_REQUEST,
						"Folder type update not allowed");

			if (update.colorHex != null && !update.colorHex.isBlank())
				f.colorHex = update.colorHex;

			f.persist();
			return Response.ok(f).build();
		} catch (WebApplicationException e) {
			return e.getResponse();
		}
	}

	@DELETE
	@Path("/{folder_id}")
	@Transactional
	public Response deleteFolder(@PathParam("folder_id") String folderId) {
		try {
			Folder f = HttpUtil.requireFolder(folderId);

			Map<String, Object> resp = new HashMap<>();
			resp.put("id", f.id);
			resp.put("status", "deleted");

			f.delete();
			return Response.ok(resp).build();
		} catch (WebApplicationException e) {
			return e.getResponse();
		}
	}

}
