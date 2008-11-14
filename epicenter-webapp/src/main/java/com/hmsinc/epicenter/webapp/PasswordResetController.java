/**
 * Copyright (C) 2008 University of Pittsburgh
 * 
 * 
 * This file is part of Open EpiCenter
 * 
 *     Open EpiCenter is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     Open EpiCenter is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with Open EpiCenter.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 *   
 */
package com.hmsinc.epicenter.webapp;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.Validate;
import org.apache.velocity.app.VelocityEngine;
import org.directwebremoting.json.JsonBoolean;
import org.directwebremoting.json.JsonObject;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.hmsinc.epicenter.model.permission.EpiCenterUser;
import com.hmsinc.epicenter.model.permission.PasswordResetToken;
import com.hmsinc.epicenter.model.permission.PermissionException;
import com.hmsinc.epicenter.model.permission.PermissionRepository;

/**
 * @author shade
 * @version $Id: PasswordResetController.java 1400 2008-03-28 19:19:34Z steve.kondik $
 */
@Controller
public class PasswordResetController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final String JSON_CONTENT_TYPE = "application/json";

	private static final String TEMPLATE_TEXT = "/templates/password-reset-text.vm";
	private static final String TEMPLATE_HTML = "/templates/password-reset-html.vm";

	private static final String RESET_URL = "password-reset.html";

	@Resource
	private PermissionRepository permissionRepository;

	@Resource(name = "velocityEngine")
	private VelocityEngine velocityEngine;

	@Resource
	private JavaMailSender mailSender;

	@Resource
	private Properties applicationProperties;
	
	private String mailSubject = "EpiCenter Password Assistance";

	@ModelAttribute("appVersion")
	public String getAppVersion() {
		return EpiCenterController.APP_VERSION;
	}
	
	/**
	 * @param email
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@Transactional
	@RequestMapping(value = "/password-assistance.html", method = RequestMethod.POST)
	public void passwordAssistanceHandler(@RequestParam("email") final String email, final HttpServletRequest request, final HttpServletResponse response) throws IOException {

		Validate.notNull(email, "No email address provided.");
		Validate.notNull(request.getHeader("Referer"), "Invalid referer");

		final JsonObject json = new JsonObject();
		JsonBoolean success = new JsonBoolean(false);

		try {

			final EpiCenterUser user = permissionRepository.getUserByEmailAddress(email);
			Validate.notNull(user);

			permissionRepository.purgeExpiredTokens();

			final PasswordResetToken token = new PasswordResetToken(user);
			permissionRepository.save(token);

			final String referer = request.getHeader("Referer");
			final String url = referer.substring(0, referer.lastIndexOf("/")) + "/" + RESET_URL;
			sendPasswordResetEmail(token, url);

			success = new JsonBoolean(true);
			
			logger.info("Sent password assistance email to: {}", user.getEmailAddress());
			
		} catch (PermissionException e) {
			logger.error("Password assistance failed: ", e);
		}

		json.put("success", success);

		response.setContentType(JSON_CONTENT_TYPE);

		final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
		writer.write(json.toExternalRepresentation());
		writer.close();
	}

	/**
	 * @return
	 */
	@RequestMapping(value = "/password-reset.html", method = RequestMethod.GET)
	public String passwordResetHandler() {
		return "password-reset";
	}
	
	/**
	 * @param token
	 * @param password
	 * @param confirmPassword
	 * @param response
	 * @throws IOException
	 */
	@Transactional
	@RequestMapping(value = "/password-reset.html", method = RequestMethod.POST)
	public void passwordResetPostHandler(@RequestParam("token") String token, 
			@RequestParam("password") String password, @RequestParam("confirmPassword") String confirmPassword, 
			final HttpServletResponse response) throws IOException {
		
		Validate.notNull(token, "No token specified.");
		Validate.notNull(password, "No password specified.");
		Validate.isTrue(password.equals(confirmPassword), "Passwords do not match.");
		Validate.isTrue(password.length() > 4, "Password must be at least 5 characters.");
		
		permissionRepository.purgeExpiredTokens();
		
		final PasswordResetToken prt = permissionRepository.getPasswordResetToken(token);
		Validate.notNull(prt, "Invalid token: " + token);
		Validate.notNull(prt.getUser(), "User was null.");
		Validate.isTrue(prt.getUser().isEnabled(), "User is disabled.");
		Validate.isTrue(prt.getExpiration().isAfter(new DateTime()), "Token has expired.");
		
		permissionRepository.changePassword(prt.getUser(), password);
		
		logger.info("Reset password for: {}", prt.getUser().getUsername());
		
		final JsonObject json = new JsonObject();
		json.put("success", new JsonBoolean(true));

		response.setContentType(JSON_CONTENT_TYPE);

		final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
		writer.write(json.toExternalRepresentation());
		writer.close();
		
	}
	
	
	/**
	 * @param token
	 * @param url
	 */
	private void sendPasswordResetEmail(final PasswordResetToken token, final String url) {

		final MimeMessagePreparator preparator = new MimeMessagePreparator() {

			public void prepare(MimeMessage mimeMessage) throws Exception {

				final EpiCenterUser user = token.getUser();

				String encoding = "UTF-8";
				final MimeMessageHelper message = new MimeMessageHelper(mimeMessage,
				    MimeMessageHelper.MULTIPART_MODE_RELATED, encoding);
				message.setTo(user.getEmailAddress());
				message.setFrom(applicationProperties.getProperty("epicenter.mail.from"));
				message.setSubject(mailSubject);

				final Map<String, Object> model = new HashMap<String, Object>();
				model.put("url", url);
				model.put("username", user.getUsername());
				model.put("token", token.getToken());

				message.setText(VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, TEMPLATE_TEXT, model),
                    VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, TEMPLATE_HTML, model));
			}
		};

		mailSender.send(preparator);
	}

	/**
	 * @return the mailSubject
	 */
	public String getMailSubject() {
		return mailSubject;
	}

	/**
	 * @param mailSubject
	 *            the mailSubject to set
	 */
	public void setMailSubject(String mailSubject) {
		this.mailSubject = mailSubject;
	}

}
