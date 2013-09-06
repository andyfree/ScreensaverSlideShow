/**
 * 
 */
package com.slideshow.server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.slideshow.shared.Datastore;

/**
 * @author ArunAbraham
 *
 */
public class GetResults extends BaseServlet {
	
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		
		response.setCharacterEncoding("utf8");
        response.setContentType("application/json"); 
        PrintWriter out = response.getWriter(); 
        JSONObject obj = new JSONObject();
        obj.put("presetTime", Datastore.getSettingValue(Datastore.PRESET_TIME));
        obj.put("presetTimeFormat", Datastore.getSettingValue(Datastore.PRESET_TIME_FORMAT));
        obj.put("durationTime", Datastore.getSettingValue(Datastore.DURATION_TIME));
        obj.put("durationTimeFormat", Datastore.getSettingValue(Datastore.DURATION_TIME_FORMAT));
        out.print(obj);
        
	}

}
