package servlets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import models.EmailRequest;
import smtp.SendMail;

public class Test {
	private static final Logger log = LogManager.getLogger(Test.class);

	public static void test(HttpServletRequest req, HttpServletResponse resp) {
		
		log.info("Test servlet called");
		EmailRequest er = new EmailRequest();
		SendMail.send(er);
	}

}
