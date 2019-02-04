package com.mediusecho.particlehats.particles.properties;

import java.util.HashMap;
import java.util.Map;

import com.mediusecho.particlehats.locale.Message;

public enum ParticleTracking {

	TRACK_NOTHING       (0),
	TRACK_HEAD_MOVEMENT (1),
	TRACK_BODY_ROTATION (2);
	
	private final int id;
	private static final Map<Integer, ParticleTracking> trackingID = new HashMap<Integer, ParticleTracking>();
	
	static
	{
		for (ParticleTracking pt : values()) {
			trackingID.put(pt.id, pt);
		}
	}
	
	private ParticleTracking (final int id)
	{
		this.id = id;
	}
	
	/**
	 * Get the name of this ParticleTracking
	 * @return The name of this tracking as defined in the current messages.yml file
	 */
	public String getDisplayName () 
	{
		final String key = toString() + "_NAME";
		try {
			return Message.valueOf(key).getValue();
		} catch (IllegalArgumentException e) {
			return "";
		}
	}
	
	/**
	 * Returns the ParticleTracking object that has this id
	 * @param id
	 * @return
	 */
	public static ParticleTracking fromID (int id)
	{
		if (trackingID.containsKey(id)) {
			return trackingID.get(id);
		}
		return TRACK_NOTHING;
	}
}
