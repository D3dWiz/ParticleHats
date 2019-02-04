package com.mediusecho.particlehats.ui;

import java.util.UUID;

import org.bukkit.entity.Player;

import com.mediusecho.particlehats.Core;

public abstract class Menu {

	private Player owner;
	private UUID ownerID;
	
	private MenuInventory inventory;
	
	public Menu (Core core, final Player owner)
	{
		this.owner = owner;
		this.ownerID = ownerID;
	}
	
	public Menu (Core core, final Player owner, final MenuInventory inventory)
	{
		this(core, owner);
		this.inventory = inventory;
	}
	
	public void open () {
		inventory.open(owner);
	}
	
	/**
	 * Set this menus MenuInventory
	 * @param inventory
	 */
	public void setInventory (MenuInventory inventory) {
		this.inventory = inventory;
	}
	
	/**
	 * Lets this menu update dynamically
	 */
	protected void onTick () {}
	
//	private Inventory inventory;
	
//	public Menu (Core core, final Player owner)
//	{
//		this.owner = owner;
//		this.ownerID = owner.getUniqueId();
//	}
	
//	public Menu (Player owner, final String title, final int rows)
//	{
//		this.owner = owner;
//		this.ownerID = owner.getUniqueId();
//		
//		inventory = Bukkit.createInventory(null, rows * 9, ChatColor.translateAlternateColorCodes('&', title));
//	}
//	
//	/**
//	 * Opens this menu for the owner
//	 */
//	public void open ()
//	{
//		if (owner != null && inventory != null) {
//			owner.openInventory(inventory);
//		} else {
//			Core.log("Unable to open menu");
//		}
//	}
//	
//	public void setItem (int slot, ItemStack item) {
//		inventory.setItem(slot, item);
//	}
	
//	public void open (String menuName)
//	{
//		DatabaseType databaseType = Core.instance.getDatabaseType();
//		if (databaseType.equals(DatabaseType.MYSQL))
//		{
//			MySQLDatabase database = (MySQLDatabase) Core.instance.getDatabase();
//			database.connect((connection) ->
//			{
//				// Create our menu
//				String menuQuery = "SELECT title, size FROM menus WHERE name = ?";
//				try (PreparedStatement menuStatement = connection.prepareStatement(menuQuery))
//				{
//					menuStatement.setString(1, menuName);
//					ResultSet menuSet = menuStatement.executeQuery();
//					
//					while (menuSet.next())
//					{
//						final String title = ChatColor.translateAlternateColorCodes('&', menuSet.getString("title"));
//						final int size = menuSet.getInt("size");
//						inventory = Bukkit.createInventory(null, size * 9, title);
//					}
//					
//					// Load our items
//					String itemQuery = "SELECT slot, id, name FROM " + menuName + "_items";
//					try (PreparedStatement itemStatement = connection.prepareStatement(itemQuery))
//					{
//						ResultSet itemSet = itemStatement.executeQuery();
//						while (itemSet.next())
//						{
//							final int slot = itemSet.getInt("slot");
//							final Material mat = Material.valueOf(itemSet.getString("id"));
//							final String title = ChatColor.translateAlternateColorCodes('&', itemSet.getString("name"));
//							
//							ItemStack item = new ItemStack(mat);
//							ItemMeta meta = item.getItemMeta();
//							
//							if (meta != null)
//							{
//								meta.setDisplayName(title);
//								
//								// Load our descriptions
//								String descriptionQuery = "SELECT value FROM " + menuName + "_desc WHERE slot = ? ORDER BY line ASC";
//								try (PreparedStatement descStatement = connection.prepareStatement(descriptionQuery))
//								{
//									descStatement.setInt(1, slot);
//									
//									List<String> description = new ArrayList<String>();
//									ResultSet descResult = descStatement.executeQuery();
//									
//									while (descResult.next())
//									{
//										String entry = ChatColor.translateAlternateColorCodes('&', descResult.getString("value"));	
//										description.add(entry);
//									}
//
//									meta.setLore(description);
//								}
//							}
//							
//							item.setItemMeta(meta);
//							setItem(slot, item);
//						}
//					}
//				}
//			});
//		}
//		open();
//	}
}
