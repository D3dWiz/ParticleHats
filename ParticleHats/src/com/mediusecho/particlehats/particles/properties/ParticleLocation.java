package com.mediusecho.particlehats.particles.properties;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.mediusecho.particlehats.locale.Message;

/**
 * Represents each location a particle can be displayed at
 * 
 * @author MediusEcho
 *
 */
public enum ParticleLocation
{
	HEAD  (0, "head"),
	FEET  (1, "feet"),
	WAIST (2, "waist");
	
	private final int id;
	private final String name;
	
	private static final Map<Integer, ParticleLocation> locationID = new HashMap<Integer, ParticleLocation>();
	private static final Map<String, ParticleLocation> locationName = new HashMap<String, ParticleLocation>();
	
	static 
	{
		for (ParticleLocation location : values())
		{
			locationID.put(location.id, location);
			locationName.put(location.name, location);
		}
	}
	
	private ParticleLocation (int id, String name)
	{
		this.id = id;
		this.name = name;
	}
	
	/**
	 * Returns this ParticleLocations name
	 * @return
	 */
	public String getName () {
		return name;
	}
	
	/**
	 * Get the name of this ParticleMode
	 * @return The name of this mode as defined in the current messages.yml file
	 */
	public String getDisplayName () 
	{
		final String key = "LOCATION_" + toString() + "_NAME";
		try {
			return Message.valueOf(key).getValue();
		} catch (IllegalArgumentException e) {
			return "";
		}
	}
	
	/**
	 * Returns this ParticleLocations id
	 * @return
	 */
	public int getID () {
		return id;
	}
	
	/**
	 * Returns the ParticleLocation associated with this id
	 * @param id
	 * @return
	 */
	public static ParticleLocation fromId (int id) 
	{
		for (Entry<Integer, ParticleLocation> entry : locationID.entrySet()) {
			if (entry.getKey() != id) {
				continue;
			}
			return entry.getValue();
		}
		return HEAD;
	}
	
	/**
	 * Returns the ParticleAction associated with the name
	 * @param name
	 * @return
	 */
	public static ParticleLocation fromName (String name)
	{
		for (Entry<String, ParticleLocation> entry : locationName.entrySet()) {
			if (!entry.getKey().equalsIgnoreCase(name)) {
				continue;
			}
			return entry.getValue();
		}
		return HEAD;
	}
}
