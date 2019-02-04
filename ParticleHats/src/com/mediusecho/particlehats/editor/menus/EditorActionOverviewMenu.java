package com.mediusecho.particlehats.editor.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.mediusecho.particlehats.Core;
import com.mediusecho.particlehats.editor.EditorLore;
import com.mediusecho.particlehats.editor.EditorMenu;
import com.mediusecho.particlehats.editor.MenuBuilder;
import com.mediusecho.particlehats.locale.Message;
import com.mediusecho.particlehats.particles.Hat;
import com.mediusecho.particlehats.particles.properties.ParticleAction;
import com.mediusecho.particlehats.util.ItemUtil;

public class EditorActionOverviewMenu extends EditorMenu {

	private final EditorMainMenu editorMainMenu;
	private final Hat targetHat;
	
	public EditorActionOverviewMenu(Core core, Player owner, MenuBuilder menuBuilder, EditorMainMenu editorMainMenu) 
	{
		super(core, owner, menuBuilder, true);
		this.editorMainMenu = editorMainMenu;
		this.targetHat = menuBuilder.getBaseHat();
		
		inventory = Bukkit.createInventory(null, 27, Message.EDITOR_ACTION_OVERVIEW_MENU_TITlE.getValue());
		buildMenu();
	}

	@Override
	protected void buildMenu() 
	{
		setButton(10, backButton, backAction);
		
		// Left Click
		ItemStack leftActionItem = ItemUtil.createItem(Material.GUNPOWDER, Message.EDITOR_ACTION_OVERVIEW_MENU_SET_LEFT_CLICK);
		EditorLore.updateSpecificActionDescription(leftActionItem, targetHat, targetHat.getLeftClickAction(), targetHat.getLeftClickArgument());
		setButton(14, leftActionItem, (event, slot) ->
		{
			if (event.isLeftClick()) {
				openActionMenu(true);
			}
			
			else if (event.isRightClick()) {
				openPropertiesMenu(true);
			}
			return true;
		});
		
		// Right Click
		ItemStack rightActionItem = ItemUtil.createItem(Material.GUNPOWDER, Message.EDITOR_ACTION_OVERVIEW_MENU_SET_RIGHT_CLICK);
		EditorLore.updateSpecificActionDescription(rightActionItem, targetHat, targetHat.getRightClickAction(), targetHat.getRightClickArgument());
		setButton(16, rightActionItem, (event, slot) ->
		{
			if (event.isLeftClick()) {
				openActionMenu(false);
			}
			
			else if (event.isRightClick()) {
				openPropertiesMenu(false);
			}
			return true;
		});
	}
	
	private void openActionMenu (boolean leftClick)
	{
		EditorActionMenu editorActionMenu = new EditorActionMenu(core, owner, menuBuilder, this, leftClick);
		menuBuilder.addMenu(editorActionMenu);
		editorActionMenu.open();
	}
	
	private void openPropertiesMenu (boolean leftClick)
	{
		ParticleAction action = leftClick ? targetHat.getLeftClickAction() : targetHat.getRightClickAction();
		switch (action)
		{
		case OPEN_MENU_PERMISSION:
		case OPEN_MENU:
		{
			
		}
		break;
		
		case COMMAND:
		{
			
		}
		break;
		
		case DEMO:
		{
			EditorDurationMenu editorDurationMenu = new EditorDurationMenu(core, owner, menuBuilder, this, leftClick);
			menuBuilder.addMenu(editorDurationMenu);
			editorDurationMenu.open();
		}
		break;
		default: break;
		
		}
	}
	
	/**
	 * Called any time an action is changed
	 * @param isLeftClick
	 */
	public void onActionChange (ParticleAction action, boolean isLeftClick)
	{
		if (isLeftClick) {
			EditorLore.updateSpecificActionDescription(getItem(14), targetHat, targetHat.getLeftClickAction(), targetHat.getLeftClickArgument());
		} else {
			EditorLore.updateSpecificActionDescription(getItem(16), targetHat, targetHat.getRightClickAction(), targetHat.getRightClickArgument());
		}
		editorMainMenu.onActionChange();
	}
}
