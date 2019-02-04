package com.mediusecho.particlehats.editor.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.mediusecho.particlehats.Core;
import com.mediusecho.particlehats.editor.EditorMenu;
import com.mediusecho.particlehats.editor.MenuBuilder;
import com.mediusecho.particlehats.locale.Message;
import com.mediusecho.particlehats.util.ItemUtil;

public class EditorSettingsMenu extends EditorMenu {

	public EditorSettingsMenu(Core core, Player owner, MenuBuilder menuBuilder) 
	{
		super(core, owner, menuBuilder, true);
		
		inventory = Bukkit.createInventory(null, 54, Message.EDITOR_SETTINGS_MENU_TITLE.getValue());
		buildMenu();
	}

	@Override
	protected void buildMenu() 
	{
		// Back
		setButton(49, backButton, backAction);
		
		// Set Title
		ItemStack titleItem = ItemUtil.createItem(Material.SIGN, Message.EDITOR_SETTINGS_MENU_SET_TITLE);
		setButton(10, titleItem, (event, slot) ->
		{
			return true;
		});
		
		// Set Size
		ItemStack sizeItem = ItemUtil.createItem(Material.COMPARATOR, Message.EDITOR_SETTINGS_MENU_SET_SIZE);
		setButton(12, sizeItem, (event, slot) ->
		{
			return true;
		});
		
		// Set Purchase Menu
		ItemStack purchaseItem = ItemUtil.createItem(Material.GOLD_NUGGET, Message.EDITOR_SETTINGS_MENU_SET_PURCHASE_MENU);
		setButton(14, purchaseItem, (event, slot) ->
		{
			return true;
		});
		
		// Delete
		ItemStack deleteItem = ItemUtil.createItem(Material.TNT, Message.EDITOR_SETTINGS_MENU_DELETE);
		setButton(16, deleteItem, (event, slot) ->
		{
			return true;
		});
		
		// Toggle Live Updates
		ItemStack liveItem = ItemUtil.createItem(Material.LEVER, Message.EDITOR_SETTINGS_MENU_TOGGLE_LIVE_MENU);
		setButton(30, liveItem, (event, slot) ->
		{
			return true;
		});
		
		// Sync Icons
		ItemStack syncItem = ItemUtil.createItem(Material.CONDUIT, Message.EDITOR_SETTINGS_MENU_SYNC_ICONS);
		setButton(32, syncItem, (event, slot) ->
		{
			return true;
		});
	}

}
