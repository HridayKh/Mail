package utils;

import java.io.BufferedReader;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import io.sentry.Sentry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HttpUtil {
	private static final Logger log = LogManager.getLogger(HttpUtil.class);

	public static JSONObject readBodyJSON(HttpServletRequest req) {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = req.getReader()) {
			String line;
			while ((line = reader.readLine()) != null)
				sb.append(line);
			return new JSONObject(sb.toString());
		} catch (IOException | JSONException e) {
			log.error("Unable to read body json", e);
			Sentry.captureException(e);
		}
		return new JSONObject();
	}

	public static boolean sendJson(HttpServletResponse resp, int status, String type, String message)
			throws IOException {
		resp.setStatus(status);
		resp.setContentType("application/json");
		JSONObject json = new JSONObject();
		try {
			json.put("message", message);
			json.put("type", type);
		} catch (JSONException e) {
			log.error("Unable to send json", e);
			Sentry.captureException(e);
			return false;
		}
		resp.getWriter().write(json.toString());
		return true;
	}

}
