package com.slideshow.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.slideshow.shared.Datastore;

public class StoreSettingsServlet extends BaseServlet {

	private static final long serialVersionUID = 4265103214608689756L;
	private static final String PRESET_TIME = "presetTime";
	private static final String PRESET_TIME_FORMAT = "presetTimeFormat";
	private static final String DURATION_TIME = "durationTime";
	private static final String DURATION_TIME_FORMAT = "durationTimeFormat";

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException {
		
		String presetTime = getParameter(req, PRESET_TIME);
		String presetTimeType = getParameter(req, PRESET_TIME_FORMAT);
		String durationTime = getParameter(req, DURATION_TIME);
		String durationTimeType = getParameter(req, DURATION_TIME_FORMAT);
		
		Datastore.storeSetting(Datastore.PRESET_TIME, presetTime);
		Datastore.storeSetting(Datastore.PRESET_TIME_FORMAT, presetTimeType);
		Datastore.storeSetting(Datastore.DURATION_TIME, durationTime);
		Datastore.storeSetting(Datastore.DURATION_TIME_FORMAT, durationTimeType);
		
		try {
			getServletContext().getRequestDispatcher("/sendAll").forward(req, resp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
