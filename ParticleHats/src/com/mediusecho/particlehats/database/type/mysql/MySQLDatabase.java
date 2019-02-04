package com.mediusecho.particlehats.database.type.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.mediusecho.particlehats.Core;
import com.mediusecho.particlehats.database.Database;
import com.mediusecho.particlehats.editor.menus.EditorBaseMenu;
import com.mediusecho.particlehats.locale.Message;
import com.mediusecho.particlehats.managers.SettingsManager;
import com.mediusecho.particlehats.particles.Hat;
import com.mediusecho.particlehats.particles.properties.IconData;
import com.mediusecho.particlehats.particles.properties.IconDisplayMode;
import com.mediusecho.particlehats.particles.properties.ParticleAction;
import com.mediusecho.particlehats.particles.properties.ParticleLocation;
import com.mediusecho.particlehats.particles.properties.ParticleMode;
import com.mediusecho.particlehats.ui.MenuInventory;
import com.mediusecho.particlehats.util.ItemUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class MySQLDatabase extends Database {

	private HikariDataSource dataSource;
	private MySQLHelper helper;
	
	private final String hostname = SettingsManager.DATABASE_HOSTNAME.asString();
	private final String username = SettingsManager.DATABASE_USERNAME.getString();
	private final String password = SettingsManager.DATABASE_PASSWORD.getString();
	private final String port     = SettingsManager.DATABASE_PORT.asString();
	private final String database = SettingsManager.DATABASE_DATABASE.getString();
	
	private List<String> menuCache;
	private final long UPDATE_INTERVAL = 30000L; // Fetch MySQL changes every 30 seconds
	private Long lastUpdate = 0L;
	
	public MySQLDatabase ()
	{
		helper = new MySQLHelper(this);
		menuCache = new ArrayList<String>();
		
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl("jdbc:mysql://" + hostname + ":" + port + "/" + database + "?useSSL=false");
		config.setUsername(username);
		config.setPassword(password);
		
		try {
			dataSource = new HikariDataSource(config);
			helper.initDatabase();
		}
		
		catch (Exception e)
		{
			Core.log("There was an error connecting to the MySQL database");
			e.printStackTrace();
		}
		
//		dataSource = new HikariDataSource();
//		dataSource.setJdbcUrl("jdbc:mysql://" + hostname + ":" + port + "/" + database + "?verifyServerCertificate=false&useSSL=false");
//		dataSource.setUsername(username);
//		dataSource.setPassword(password);
		
//		try 
//		{
//			Connection connection = dataSource.getConnection();
//			
//			Core.log("Connected to MySQL database");
//			helper.initDatabase(connection);
//		} 
//		
//		catch (SQLException e) 
//		{
//			Core.log("There was an issue connecting to the MySQL database");
//			e.printStackTrace();
//			return;
//		}
	}
	
	public boolean init ()
	{
		
		return true;
	}
	
	@Override
	public void onDisable () {
		dataSource.close();
	}

	@Override
	public MenuInventory loadInventory(String menuName) 
	{		
		try (Connection connection = dataSource.getConnection())
		{
			String menuQuery = "SELECT * FROM menus WHERE name = ?";
			try (PreparedStatement menuStatement = connection.prepareStatement(menuQuery))
			{
				menuStatement.setString(1, menuName);
				ResultSet menuResult = menuStatement.executeQuery();
				
				while (menuResult.next())
				{
					final String menuTitle = ChatColor.translateAlternateColorCodes('&', menuResult.getString("title"));
					final int  menuSize = menuResult.getInt("size");
					final MenuInventory inventory = new MenuInventory(menuName, menuTitle, menuSize);
					
					// Load this menus items
					String itemQuery = "SELECT slot, id, title, icon_update_frequency, display_mode FROM menu_" + menuName + "_items";
					try (PreparedStatement itemStatement = connection.prepareStatement(itemQuery))
					{
						ResultSet itemSet = itemStatement.executeQuery();
						
						while (itemSet.next())
						{
							//int referenceID  = itemResult.getInt("reference");
							int itemSlot     = itemSet.getInt("slot");
							String itemID    = itemSet.getString("id");
							String itemTitle = ChatColor.translateAlternateColorCodes('&', itemSet.getString("title"));
							
							ItemStack item = ItemUtil.createItem(Material.valueOf(itemID), 1);
							ItemMeta meta = item.getItemMeta();
							if (meta != null) {
								meta.setDisplayName(itemTitle);
							}
							
							// Create our item descriptions
							String descriptionQuery = "SELECT value FROM menu_" + menuName + "_desc WHERE slot = ? ORDER BY line ASC";
							try (PreparedStatement descStatement = connection.prepareStatement(descriptionQuery))
							{
								descStatement.setInt(1, itemSlot);
								
								List<String> description = new ArrayList<String>();
								ResultSet descResult = descStatement.executeQuery();
								
								while (descResult.next())
								{
									String entry = ChatColor.translateAlternateColorCodes('&', descResult.getString("value"));	
									description.add(entry);
								}
								
								if (meta != null) {
									meta.setLore(description);
								}
							}
							
							Hat hat = new Hat();
							hat.setMaterial(item.getType());
							hat.setIconUpdateFrequency(itemSet.getInt("icon_update_frequency"));
							hat.setDisplayMode(IconDisplayMode.fromId(itemSet.getInt("display_mode")));
							
							// Create our icons
							//String iconQuery = "SELECT * FROM menu_" + menuName + "_"";
							
							// Create our icons
							String materialQuery = "SELECT material FROM menu_" + menuName + "_icons WHERE slot = ? ORDER BY index_num ASC";
							try (PreparedStatement materialStatement = connection.prepareStatement(materialQuery))
							{
								materialStatement.setInt(1, itemSlot);
								ResultSet set = materialStatement.executeQuery();
								IconData data = hat.getIconData();
								
								while (set.next())
								{
									Material mat = Material.valueOf(set.getString("material"));
									if (mat != null) {
										data.addMaterial(mat);
									}
								}
							}
							
							item.setItemMeta(meta);
							inventory.setItem(itemSlot, item);
							inventory.setHat(itemSlot, hat);
						}
					}
					
					return inventory;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public void saveInventory (EditorBaseMenu menu)
	{
//		Map<Integer, Hat> hats = menu.getHats();
//		boolean canSave = false;
//		
//		String saveQuery = "INSERT INTO ";// +
//		
//		for (Entry<Integer, Hat> entry : hats.entrySet())
//		{
//			int slot = entry.getKey();
//			Hat hat = entry.getValue();
//			
//			if (hat.isModified())
//			{
//				
//			}
//		}
	}

	@Override
	public List<String> getMenus(boolean forceUpdate) 
	{	
		// Only refresh our menu cache every UPDATE_INTERVAL to prevent spamming
		if (forceUpdate || (System.currentTimeMillis() - lastUpdate) > UPDATE_INTERVAL)
		{
			lastUpdate = System.currentTimeMillis();
			menuCache.clear();
			connect((connection) -> 
			{
				try (PreparedStatement statement = connection.prepareStatement("SELECT name FROM menus"))
				{
					ResultSet set = statement.executeQuery();
					while (set.next()) {
						menuCache.add(set.getString("name"));
					}
				}
			});
		}
		return menuCache;
	}
	
	@Override
	public void createEmptyMenu(String menuName) 
	{
		Core.log("Creating empty menu");
		async(() ->
		{
			connect((connection) ->
			{
				// Menu Entry
				String createMenuStatement= "INSERT INTO menus VALUES(?, ?, ?)";
				try (PreparedStatement statement = connection.prepareStatement(createMenuStatement))
				{
					statement.setString(1, menuName);
					statement.setString(2, menuName);
					statement.setInt(3, 6);
					
					if (statement.executeUpdate() > 0)
					{
						// Items Table
						String createMenuItemsTable = "CREATE TABLE IF NOT EXISTS  menu_" + menuName + "_items ("
								//+ "reference INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,"
								+ "slot TINYINT PRIMARY KEY,"
								+ "id VARCHAR(64) NOT NULL DEFAULT 'SUNFLOWER',"
								+ "title VARCHAR(128) NOT NULL DEFAULT '" + Message.EDITOR_MISC_NEW_PARTICLE.getRawValue() + "',"
								+ "permission VARCHAR(64) NOT NULL DEFAULT 'all',"
								+ "permission_denied VARCHAR(128),"
								+ "type TINYINT NOT NULL DEFAULT 0,"
								+ "location TINYINT NOT NULL DEFAULT 0,"
								+ "mode TINYINT NOT NULL DEFAULT 0,"
								+ "label VARCHAR(128),"
								+ "equip_message VARCHAR(128),"
								+ "offset_x DOUBLE NOT NULL DEFAULT 0,"
								+ "offset_y DOUBLE NOT NULL DEFAULT 0,"
								+ "offset_z DOUBLE NOT NULL DEFAULT 0,"
								+ "angle_x DOUBLE NOT NULL DEFAULT 0,"
								+ "angle_y DOUBLE NOT NULL DEFAULT 0,"
								+ "angle_z DOUBLE NOT NULL DEFAULT 0,"
								+ "update_frequency TINYINT NOT NULL DEFAULT 2,"
								+ "icon_update_frequency TINYINT NOT NULL DEFAULT 1,"
								+ "speed TINYINT NOT NULL DEFAULT 0,"
								+ "count TINYINT NOT NULL DEFAULT 1,"
								+ "price INT NOT NULL DEFAULT 0,"
								+ "sound VARCHAR(64),"
								+ "volume DOUBLE NOT NULL DEFAULT 1,"
								+ "pitch DOUBLE NOT NULL DEFAULT 1,"
								+ "left_action TINYINT NOT NULL DEFAULT 0,"
								+ "right_action TINYINT NOT NULL DEFAULT 12,"
								+ "left_argument VARCHAR(128),"
								+ "right_argument VARCHAR(128),"
								+ "duration MEDIUMINT NOT NULL DEFAULT 20"
								+ ")";
						try (PreparedStatement itemsStatement = connection.prepareStatement(createMenuItemsTable)) {
							itemsStatement.execute();
						}
						
						// Description Table
						String createMenuDescriptionTable = "CREATE TABLE IF NOT EXISTS menu_" + menuName + "_desc ("
								+ "slot TINYINT,"
								+ "line TINYINT,"
								+ "value VARCHAR(128),"
								+ "PRIMARY KEY(slot, line),"
								+ "FOREIGN KEY(slot) REFERENCES menu_" + menuName + "_items(slot) ON DELETE CASCADE ON UPDATE CASCADE"
								+ ")";
						try (PreparedStatement descStatement = connection.prepareStatement(createMenuDescriptionTable)) {
							descStatement.execute();
						}
						
						// Icons Table
						String createMenuIconTable = "CREATE TABLE IF NOT EXISTS menu_" + menuName + "_icons ("
								+ "slot TINYINT,"
								+ "index_num TINYINT,"
								+ "material VARCHAR(64) NOT NULL DEFAULT 'SUNFLOWER',"
								+ "PRIMARY KEY(slot, index_num),"
								+ "FOREIGN KEY(slot) REFERENCES menu_" + menuName + "_items(slot) ON DELETE CASCADE ON UPDATE CASCADE"
								+ ")";
						try (PreparedStatement iconStatement = connection.prepareStatement(createMenuIconTable)) {
							iconStatement.execute();
						}
					}
				}
			});
		});
	}
	
	/**
	 * Deletes the menu and all tables associated
	 * @param menuName
	 */
	@Override
	public void deleteMenu (String menuName)
	{
		async(() ->
		{
			connect((connection) ->
			{
//				String dropQuery = "DROP TABLE IF EXISTS "
//						+ "menu_" + menuName + "_icons,"
//						+ "menu_" + menuName + "_desc,"
//						+ "menu_" + menuName + "_items";
//				
//				try (PreparedStatement dropStatement = connection.prepareStatement(dropQuery))
//				{
//					if (dropStatement.exe)
//					{
//						String deleteQuery = "DELETE FROM menus WHERE name = ?";
//						try (PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery))
//						{
//							deleteStatement.setString(1, menuName);
//							deleteStatement.execute();
//						}
//					}
//				}
			});
		});
	}
	
	@Override
	public boolean menuExists(String menuName) 
	{
		List<String> menus = getMenus(true);
		return menus.contains(menuName);
	}
	
	@Override
	public void createHat(String menuName, int slot) 
	{
		async(() ->
		{
			connect((connection) ->
			{
				String createQuery = "INSERT INTO menu_" + menuName + "_items (slot) VALUES(?)";
				try (PreparedStatement createStatement = connection.prepareStatement(createQuery))
				{
					createStatement.setInt(1, slot);
					createStatement.execute();
				}
			});
		});
	}
	
	@Override
	public void loadHatData (String menuName, int slot, Hat hat)
	{
		connect((connection) ->
		{
			String hatQuery = "SELECT * FROM menu_" + menuName + "_items WHERE slot = ?";
			try (PreparedStatement hatStatement = connection.prepareStatement(hatQuery))
			{
				hatStatement.setInt(1, slot);
				ResultSet set = hatStatement.executeQuery();
				
				while (set.next())
				{
					//hat.setReferenceID(set.getInt("reference"));
					hat.setSlot(set.getInt("slot"));
					// ID
					// Title
					hat.setPermission(set.getString("permission"));	
					// Permission Denied
					// Type
					hat.setLocation(ParticleLocation.fromId(set.getInt("location")));
					hat.setMode(ParticleMode.fromId(set.getInt("mode")));
					// Label
					// Equip Message
					hat.setOffset(set.getDouble("offset_x"), set.getDouble("offset_y"), set.getDouble("offset_z"));
					hat.setAngle(set.getDouble("angle_x"), set.getDouble("angle_y"), set.getDouble("angle_z"));
					hat.setUpdateFrequency(set.getInt("update_frequency"));		
					hat.setSpeed(set.getInt("speed"));
					String soundName = set.getString("sound");
					if (soundName != null) {
						hat.setSound(Sound.valueOf(soundName));
					}
					hat.setSoundVolume(set.getDouble("volume"));
					hat.setSoundPitch(set.getDouble("pitch"));
					hat.setLeftClickAction(ParticleAction.fromId(set.getInt("left_action")));
					hat.setRightClickAction(ParticleAction.fromId(set.getInt("right_action")));
					hat.setLeftClickArgument(set.getString("left_argument"));
					hat.setRightClickArgument(set.getString("right_argument"));
					hat.setDemoDuration(set.getInt("duration"));
					
					hat.clearPropertyChanges();
					hat.setLoaded(true);
				}
			}
			
//			String itemQuery = "SELECT material FROM menu_" + menuName + "_icons WHERE slot = ? ORDER BY index_num ASC";
//			try (PreparedStatement itemStatement = connection.prepareStatement(itemQuery))
//			{
//				itemStatement.setInt(1, slot);
//				ResultSet set = itemStatement.executeQuery();
//				IconData data = hat.getIconData();
//				
//				while (set.next())
//				{
//					Material mat = Material.valueOf(set.getString("material"));
//					if (mat != null) {
//						data.addMaterial(mat);
//					}
//				}
//			}
		});
	}

	@Override
	public void deleteHat(String menuName, int slot) 
	{
		async(() ->
		{
			connect((connection) ->
			{
				String deleteQuery = "DELETE FROM menu_" + menuName + "_items WHERE slot = ?";
				try (PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery))
				{
					deleteStatement.setInt(1, slot);
					deleteStatement.execute();
				}
			});
		});
	}
	
	@Override
	public void changeSlot (String menuName, int previousSlot, int newSlot, boolean swapping)
	{
		if (swapping)
		{
			async(() ->
			{
				connect((connection) ->
				{
					String swapQuery = "UPDATE menu_" + menuName + "_items SET slot = ? WHERE slot = ?";
					try (PreparedStatement swapStatement = connection.prepareStatement(swapQuery))
					{
						swapStatement.setInt(1, 54);
						swapStatement.setInt(2, previousSlot);
						swapStatement.addBatch();
						
						swapStatement.setInt(1, previousSlot);
						swapStatement.setInt(2, newSlot);
						swapStatement.addBatch();
						
						swapStatement.setInt(1, newSlot);
						swapStatement.setInt(2, 54);
						swapStatement.addBatch();
						
						swapStatement.executeBatch();
					}
				});
			});
		}
		
		else
		{
			async(() ->
			{
				connect((connection) ->
				{
					String changeQuery = "UPDATE menu_" + menuName + "_items SET slot = ? WHERE slot = ?";
					try (PreparedStatement changeStatement = connection.prepareStatement(changeQuery))
					{
						changeStatement.setInt(1, newSlot);
						changeStatement.setInt(2, previousSlot);
						changeStatement.execute();
					}
				});
			});
		}
	}
	
	/**
	 * Updates the database with any changes
	 * @param menuName
	 * @param slot
	 * @param sqlQuery
	 */
	public void saveIncremental (String menuName, int slot, String sqlQuery)
	{
		async(() ->
		{
			connect((connection) ->
			{
				String saveQuery = "UPDATE menu_" + menuName + "_items " + sqlQuery + " WHERE slot = ?";
				try (PreparedStatement saveStatement = connection.prepareStatement(saveQuery))
				{
					saveStatement.setInt(1, slot);
					saveStatement.executeUpdate();
				}
			});
		});
	}
	
	public void saveIconData (String menuName, Hat hat)
	{
		Core.log("deleting from slot " + hat.getSlot());
		List<String> materials = hat.getIconData().getMaterialsAsStringList();
		
		// Remove our first entry since that one is saved in the _items table
		if (materials.size() > 0) {
			materials.remove(0);
		}
		async(() ->
		{
			connect((connection) ->
			{
				// Delete our existing entries
				String deleteQuery = "DELETE FROM menu_" + menuName + "_icons WHERE slot = ?";
				try (PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery))
				{
					deleteStatement.setInt(1, hat.getSlot());
					deleteStatement.execute();
				}
				
				String insertQuery = "INSERT INTO menu_" + menuName + "_icons VALUES(?,?,?)";
				try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery))
				{
					int index_num = 1;
					int slot = hat.getSlot();
					for (String mat : materials)
					{
						insertStatement.setInt(1, slot);
						insertStatement.setInt(2, index_num++);
						insertStatement.setString(3, mat);
						insertStatement.addBatch();
					}
					
					insertStatement.executeBatch();
				}
			});
		});
	}
	
	public void connect (ConnectionCallback callback)
	{
		try (Connection connection = dataSource.getConnection()) {
			callback.execute(connection);
		}
		
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void async (Sync callback)
	{
		new BukkitRunnable()
		{
			public void run () {
				callback.execute();
			}
		}.runTaskAsynchronously(Core.instance);
	}
	
	public void sync (Sync callback)
	{
		new BukkitRunnable()
		{
			public void run () {
				callback.execute();
			}
		}.runTask(Core.instance);
	}
	
	public static interface ConnectionCallback {
		public void execute (Connection connection) throws SQLException;
	}
	
	@FunctionalInterface
	public static interface Sync {
		public void execute();
	}
	
	@FunctionalInterface
	public static interface ConfigurationCallback<T> {
		public void execute (T obj);
	}
}
