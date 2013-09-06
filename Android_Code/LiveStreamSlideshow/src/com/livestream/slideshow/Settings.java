/**
 * 
 */
package com.livestream.slideshow;

/**
 * @author ArunAbraham
 *
 */
public class Settings {

	
	private long preset = 3;
	private long duration = 3;
	
	private String durationFormat = "Seconds";
	private String presetFormat = "Seconds";
	
	public Settings() {
		// TODO Auto-generated constructor stub
	}

	public Settings(int duration, int preset, String presetFormat, String durationFormat) {
		
		this.preset = convertToMillis(preset, presetFormat);
		this.duration = convertToMillis(duration, durationFormat);;
		this.durationFormat = durationFormat;
		this.presetFormat = presetFormat;
		
	}
	
	private long convertToMillis(int value, String format){
		return format.equalsIgnoreCase("seconds") ? value * 1000 : value * 60 * 1000;
	}

	public long getPreset() {
		return preset;
	}

	public long getDuration() {
		return duration;
	}

	public String getDurationFormat() {
		return durationFormat;
	}

	public String getPresetFormat() {
		return presetFormat;
	}


}
