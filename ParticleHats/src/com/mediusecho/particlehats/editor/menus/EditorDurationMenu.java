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
import com.mediusecho.particlehats.util.MathUtil;

public class EditorDurationMenu extends EditorMenu {

	private final EditorActionOverviewMenu editorActionOverviewMenu;
	private final boolean leftClick;
	
	private final Hat targetHat;
	
	public EditorDurationMenu(Core core, Player owner, MenuBuilder menuBuilder, EditorActionOverviewMenu editorActionOverviewMenu, boolean leftClick)
	{
		super(core, owner, menuBuilder, true);
		this.editorActionOverviewMenu = editorActionOverviewMenu;
		this.leftClick = leftClick;
		this.targetHat = menuBuilder.getBaseHat();
		
		inventory = Bukkit.createInventory(null, 27, Message.EDITOR_DURATION_MENU_TITLE.getValue());
		buildMenu();
	}
	
	@Override
	public void onClose ()
	{
		ParticleAction action = leftClick ? targetHat.getLeftClickAction() : targetHat.getRightClickAction();
		editorActionOverviewMenu.onActionChange(action, leftClick);
	}

	@Override
	protected void buildMenu() 
	{
		setButton(12, backButton, backAction);
		
		ItemStack durationItem = ItemUtil.createItem(Material.MAP, Message.EDITOR_DURATION_MENU_SET_DURATION.getValue());
		EditorLore.updateDurationDescription(durationItem, targetHat.getDemoDuration());
		setButton(14, durationItem, (event, slot) ->
		{
			double normalClick    = event.isLeftClick() ? 20 : -20;
			double shiftClick     = event.isShiftClick() ? 30 : 1;
			double modifier       = normalClick * shiftClick;
			
			int duration = (int) MathUtil.clamp(targetHat.getDemoDuration() + modifier, 20, 72000);
			targetHat.setDemoDuration(duration);
			
			EditorLore.updateDurationDescription(getItem(14), duration);
			return true;
		});
	}

}
