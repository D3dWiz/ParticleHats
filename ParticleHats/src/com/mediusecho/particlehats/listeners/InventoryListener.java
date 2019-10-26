package com.mediusecho.particlehats.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import com.mediusecho.particlehats.ParticleHats;
import com.mediusecho.particlehats.player.PlayerState;
import com.mediusecho.particlehats.ui.GuiState;

public class InventoryListener implements Listener {

	private final ParticleHats core;
	
	public InventoryListener (final ParticleHats core)
	{
		this.core = core;
		core.getServer().getPluginManager().registerEvents(this, core);
	}
	
	@EventHandler
	public void onInventoryClick (InventoryClickEvent event)
	{
		if (!(event.getWhoClicked() instanceof Player)) {
			return;
		}
		
		ItemStack item = event.getCurrentItem();
		if (item != null && item.getType() != Material.AIR)
		{
			Player player = (Player)event.getWhoClicked();
			PlayerState playerState = core.getPlayerState(player);
			
			if (playerState.hasMenuManager())
			{
				boolean inMenu = event.getRawSlot() < event.getInventory().getSize();
				playerState.getMenuManager().onClick(event, inMenu);
			}
			
			playerState.getGuiState().onClick(event, playerState);
		}
	}
	
	@EventHandler
	public void onInventoryClose (InventoryCloseEvent event)
	{
		if (!(event.getPlayer() instanceof Player)) {
			return;
		}
		
		Player player = (Player)event.getPlayer();
		PlayerState playerState = core.getPlayerState(player);
		playerState.getGuiState().onClose(playerState);
		
		if (playerState.hasMenuManager()) {
			playerState.getMenuManager().onInventoryClose(event);
		}
	}
	
	@EventHandler
	public void onInventoryOpen (InventoryOpenEvent event)
	{
		if (!(event.getPlayer() instanceof Player)) {
			return;
		}
		
		Player player = (Player)event.getPlayer();
		PlayerState playerState = core.getPlayerState(player);
		GuiState guiState = playerState.getGuiState();
		
		if (playerState.hasMenuManager()) {
			playerState.getMenuManager().onInventoryOpen(event);
		}
		
		switch (guiState)
		{
		case SWITCHING_MENU:
			playerState.setGuiState(GuiState.ACTIVE);
			break;
			
		case SWITCHING_EDITOR:
			playerState.setGuiState(GuiState.EDITOR);
			break;
			
		case SWITCHING_MANAGER:
			playerState.setGuiState(GuiState.CITIZENS_MANAGER);
			break;
		}
//		
//		if (guiState == GuiState.SWITCHING_MENU) {
//			playerState.setGuiState(GuiState.ACTIVE);
//		}
//		
//		else if (guiState == GuiState.SWITCHING_EDITOR) {
//			playerState.setGuiState(GuiState.EDITOR);
//		}
	}
}
