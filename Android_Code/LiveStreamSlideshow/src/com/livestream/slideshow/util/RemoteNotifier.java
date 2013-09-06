/**
 * 
 */
package com.livestream.slideshow.util;

/**
 * @author ArunAbraham
 * An interface for notifying a class about information/data received from the 
 * WebApp server via GCM Service(Google Cloud Messaging Service)
 *
 */
public interface RemoteNotifier {
	
	void configurationReceived(int duration, int cycle);

}
