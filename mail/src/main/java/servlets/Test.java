package servlets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import smtp.SendMail;

public class Test {
	private static final Logger log = LogManager.getLogger(Test.class);

	public static void test(HttpServletRequest req, HttpServletResponse resp) {
		
		log.info("Test servlet called");
		SendMail.send("mail@HridayKh.in", "Hriday Khanna", "hridaykh1234@gmail.com", "test subject", "<h1>HI!</h1>", null);
	}

}
