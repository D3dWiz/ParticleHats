package com.mediusecho.particlehats.editor.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.mediusecho.particlehats.Core;
import com.mediusecho.particlehats.editor.EditorLore;
import com.mediusecho.particlehats.editor.EditorMenu;
import com.mediusecho.particlehats.editor.MenuBuilder;
import com.mediusecho.particlehats.locale.Message;
import com.mediusecho.particlehats.particles.Hat;
import com.mediusecho.particlehats.util.ItemUtil;
import com.mediusecho.particlehats.util.MathUtil;

public class EditorOffsetMenu extends EditorMenu {

	protected final EditorMainMenu editorMainMenu;
	
	public EditorOffsetMenu(Core core, Player owner, MenuBuilder menuBuilder, EditorMainMenu editorMainMenu) 
	{
		super(core, owner, menuBuilder, true);
		this.editorMainMenu = editorMainMenu;
		
		inventory = Bukkit.createInventory(null, 27, Message.EDITOR_OFFSET_MENU_TITLE.getValue());
		Bukkit.createInventory(owner, 27);
		buildMenu();
	}
	
	@Override
	public void onClose ()
	{
		editorMainMenu.onOffsetChange();
	}

	@Override
	protected void buildMenu() 
	{
		Hat targetHat = menuBuilder.getTargetHat();
		
		// X Offset
		ItemStack xItem = ItemUtil.createItem(Material.REPEATER, Message.EDITOR_OFFSET_MENU_SET_OFFSET_X);
		EditorLore.updateVectorDescription(xItem, targetHat.getOffset(), Message.EDITOR_OFFSET_MENU_OFFSET_X_DESCRIPTION);
		setButton(14, xItem, (event, slot) ->
		{
			updateOffset(event, targetHat, VectorAxis.X);
			return true;
		});
		
		// Y Offset
		ItemStack yItem = ItemUtil.createItem(Material.REPEATER, Message.EDITOR_OFFSET_MENU_SET_OFFSET_Y);
		EditorLore.updateVectorDescription(yItem, targetHat.getOffset(), Message.EDITOR_OFFSET_MENU_OFFSET_Y_DESCRIPTION);
		setButton(15, yItem, (event, slot) ->
		{
			updateOffset(event, targetHat, VectorAxis.Y);
			return true;
		});
		
		// Z Offset
		ItemStack zItem = ItemUtil.createItem(Material.REPEATER, Message.EDITOR_OFFSET_MENU_SET_OFFSET_Z);
		EditorLore.updateVectorDescription(zItem, targetHat.getOffset(), Message.EDITOR_OFFSET_MENU_OFFSET_Z_DESCRIPTION);
		setButton(16, zItem, (event, slot) ->
		{
			updateOffset(event, targetHat, VectorAxis.Z);
			return true;
		});
		
		// Back
		setButton(10, backButton, (event, slot) ->
		{
			menuBuilder.goBack();
			return true;
		});
	}

	private void updateOffset (EditorClickEvent event, Hat hat, VectorAxis axis)
	{
		double normalClick    = event.isLeftClick() ? 0.1f : -0.1f;
		double shiftClick     = event.isShiftClick() ? 10 : 1;
		double modifier       = normalClick * shiftClick;
		boolean isMiddleClick = event.isMiddleClick();
		
		Vector offset = hat.getOffset();
		switch (axis)
		{
		case X:
			double xo = !isMiddleClick ? MathUtil.round(MathUtil.clamp(offset.getX() + modifier, -20, 20), 2) : 0;
			hat.setOffsetX(xo);
			break;
		case Y:
			double yo = !isMiddleClick ? MathUtil.round(MathUtil.clamp(offset.getY() + modifier, -20, 20), 2) : 0;
			hat.setOffsetY(yo);
			break;
		case Z:
			double zo = !isMiddleClick ? MathUtil.round(MathUtil.clamp(offset.getZ() + modifier, -20, 20), 2) : 0;
			hat.setOffsetZ(zo);
			break;
		}

		EditorLore.updateVectorDescription(getItem(14), offset, Message.EDITOR_OFFSET_MENU_OFFSET_X_DESCRIPTION);
		EditorLore.updateVectorDescription(getItem(15), offset, Message.EDITOR_OFFSET_MENU_OFFSET_Y_DESCRIPTION);
		EditorLore.updateVectorDescription(getItem(16), offset, Message.EDITOR_OFFSET_MENU_OFFSET_Z_DESCRIPTION);
	}
	
	protected enum VectorAxis 
	{	
		X,
		Y,
		Z;
	}
}
