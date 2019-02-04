package com.mediusecho.particlehats.editor.menus;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.mediusecho.particlehats.Core;
import com.mediusecho.particlehats.editor.EditorMenu;
import com.mediusecho.particlehats.editor.MenuBuilder;
import com.mediusecho.particlehats.locale.Message;
import com.mediusecho.particlehats.particles.Hat;
import com.mediusecho.particlehats.particles.properties.IconData;
import com.mediusecho.particlehats.ui.MenuInventory;
import com.mediusecho.particlehats.util.ItemUtil;
import com.mediusecho.particlehats.util.StringUtil;

public class EditorBaseMenu extends EditorMenu {

	private MenuInventory menuInventory;
	private boolean isModified = false;
	
	private final EditorAction emptyParticleAction;
	private final EditorAction existingParticleAction;
	
	public EditorBaseMenu(Core core, Player owner, MenuBuilder menuBuilder, MenuInventory menuInventory) 
	{
		super(core, owner, menuBuilder, true);
		this.menuInventory = menuInventory;
		
		String title = ChatColor.translateAlternateColorCodes('&', StringUtil.getTrimmedMenuTitle("Editing (" + menuInventory.getTitle()));
		inventory = Bukkit.createInventory(null, menuInventory.getSize(), title);
		inventory.setContents(menuInventory.getContents());
		
		emptyParticleAction = (event, slot) ->
		{
			if (event.isLeftClick())
			{				
				menuBuilder.setTargetHat(createHat(slot));
				menuBuilder.setTargetSlot(slot);
				setModified();
				
				EditorMainMenu editorMainMenu = new EditorMainMenu(core, owner, menuBuilder);
				menuBuilder.addMenu(editorMainMenu);
				editorMainMenu.open();
			}
			
			else if (event.isRightClick()) {
				openSettings();
			}
			return true;
		};
		
		existingParticleAction = (event, slot) ->
		{
			if (event.isLeftClick())
			{
				Hat clickedHat = getHat(slot);
				if (clickedHat != null)
				{
					if (!clickedHat.isLoaded()) {
						core.getDatabase().loadHatData(getName(), slot, clickedHat);
					}
					
					menuBuilder.setTargetHat(clickedHat);
					menuBuilder.setTargetSlot(slot);
					
					EditorMainMenu editorMainMenu = new EditorMainMenu(core, owner, menuBuilder);
					menuBuilder.addMenu(editorMainMenu);
					editorMainMenu.open();
				}
			}
			
			else if (event.isShiftRightClick()) {
				deleteHat(slot);
			}
			
			else if (event.isRightClick()) {
				openSettings();
			}
			return true;
		};
		
		buildMenu();
	}
	
	@Override
	public void onTick (int ticks)
	{
		for (Entry<Integer, Hat> set : menuInventory.getHats().entrySet())
		{
			int slot = set.getKey();
			Hat hat = set.getValue();
			
			if (hat != null)
			{
				IconData iconData = hat.getIconData();
				if (iconData.isLive())
				{
					Material mat = iconData.getNextMaterial(ticks);
					if (mat != null) {
						getItem(slot).setType(mat);
					}
				}
			}
		}
	}

	@Override
	protected void buildMenu() 
	{
		int size = menuInventory.getSize();
		for (int i = 0; i < size; i++)
		{
			ItemStack item = getItem(i);
			if (item == null || item.getType().equals(Material.AIR))
			{		
				setItem(i, createEmptyItem());
				setAction(i, emptyParticleAction);
			}
			
			else {
				setAction(i, existingParticleAction);
			}
			
			Hat hat = menuInventory.getHat(i);
			if (hat != null) {
				setHat(i, hat);
			}
		}
	}
	
	/**
	 * Sets this menus modified flag to true
	 */
	public void setModified () {
		isModified = true;
	}
	
	/**
	 * Changes the Material type of the item in this slot
	 * @param slot Slot where the item exists
	 * @param material New material of this item
	 */
	public void setItemMaterial (int slot, Material material) {
		getItem(slot).setType(material);
	}
	
	/**
	 * Returns true if this menu has been modified and needs to be saved
	 * @return
	 */
	public boolean isModified () {
		return isModified;
	}
	
	/**
	 * Get the name used to save this menu
	 * @return
	 */
	public String getName () {
		return menuInventory.getName();
	}
	
	/**
	 * Returns every hat in this menu
	 * @return
	 */
	public Map<Integer, Hat> getHats () {
		return menuInventory.getHats();
	}
	
	/**
	 * Returns a Hat object from this slot
	 */
	@Override
	public Hat getHat (int slot) {
		return menuInventory.getHat(slot);
	}
	
	@Override
	public void setHat (int slot, Hat hat) {
		menuInventory.setHat(slot, hat);
	}
	
	/**
	 * Saves this menu
	 */
	public void save () {
		core.getDatabase().saveInventory(this);
	}
	
	/**
	 * Get this menus inventory
	 * @return
	 */
	public Inventory getInventory () {
		return inventory;
	}
	
	/**
	 * 
	 * @param currentSlot
	 * @param newSlot
	 * @param swapping
	 */
	public void changeSlots (int currentSlot, int newSlot, boolean swapping)
	{
		ItemStack currentItem = getItem(currentSlot);
		ItemStack swappingItem = getItem(newSlot);
		EditorAction currentAction = getAction(currentSlot);
		EditorAction swappingAction = getAction(newSlot);
		
		Hat currentHat = getHat(currentSlot);
		currentHat.setSlot(newSlot);
		setHat(currentSlot, null);
		
		if (swapping)
		{
			Hat swappingHat = getHat(newSlot);
			swappingHat.setSlot(currentSlot);
			setHat(currentSlot, swappingHat);
		}
		
		setButton(currentSlot, swappingItem, swappingAction);
		setButton(newSlot, currentItem, currentAction);
		
		//setItem(currentSlot, swappingItem);
		//setItem(newSlot, currentItem);
		//setAction(currentSlot, swappingAction);
		//setAction(newSlot, currentAction);
		setHat(newSlot, currentHat);
		
		menuBuilder.setTargetSlot(newSlot);
		
		core.getDatabase().changeSlot(getName(), currentSlot, newSlot, swapping);
	}
	
	/**
	 * Creates and returns a new hat object
	 * @param slot
	 * @return
	 */
	private Hat createHat (int slot)
	{
		ItemStack emptyItem = ItemUtil.createItem(Material.SUNFLOWER, Message.EDITOR_MISC_NEW_PARTICLE.getValue());
		setButton(slot, emptyItem, existingParticleAction);
		
		Hat hat = new Hat();
		hat.setSlot(slot);
		
		setHat(slot, hat);
		
		core.getDatabase().createHat(menuInventory.getName(), slot);
		return hat;
	}
	
	/**
	 * Deletes the hat in the current slot
	 * @param slot
	 */
	private void deleteHat (int slot)
	{
		setButton(slot, createEmptyItem(), emptyParticleAction);
		menuInventory.removeHat(slot);

		core.getDatabase().deleteHat(menuInventory.getName(), slot);
	}
	
	private ItemStack createEmptyItem ()
	{
		ItemStack emptyItem = ItemUtil.createItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 
				"&bEmpty Slot",
				"&3Left Click to Edit", "&3Right Click for Settings");
		return emptyItem;
	}
	
	private void openSettings ()
	{
		EditorSettingsMenu editorSettingsMenu = new EditorSettingsMenu(core, owner, menuBuilder);
		menuBuilder.addMenu(editorSettingsMenu);
		editorSettingsMenu.open();
	}
}
