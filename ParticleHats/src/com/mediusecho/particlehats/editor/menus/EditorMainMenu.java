package com.mediusecho.particlehats.editor.menus;


import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.mediusecho.particlehats.Core;
import com.mediusecho.particlehats.database.Database;
import com.mediusecho.particlehats.database.type.mysql.MySQLDatabase;
import com.mediusecho.particlehats.editor.EditorLore;
import com.mediusecho.particlehats.editor.EditorMenu;
import com.mediusecho.particlehats.editor.MenuBuilder;
import com.mediusecho.particlehats.locale.Message;
import com.mediusecho.particlehats.particles.Hat;
import com.mediusecho.particlehats.particles.ParticleEffect;
import com.mediusecho.particlehats.particles.properties.ParticleAnimation;
import com.mediusecho.particlehats.particles.properties.ParticleLocation;
import com.mediusecho.particlehats.particles.properties.ParticleMode;
import com.mediusecho.particlehats.particles.properties.ParticleTracking;
import com.mediusecho.particlehats.particles.properties.ParticleType;
import com.mediusecho.particlehats.util.ItemUtil;
import com.mediusecho.particlehats.util.MathUtil;

// TODO: Update particle description after switching types

public class EditorMainMenu extends EditorMenu {
	
	// These will let us extend this class to the node editor
	protected int particleItemSlot = 40;
	protected int trackingItemSlot = 29;
	
	protected final ItemStack noParticleItem = ItemUtil.createItem(Material.BARRIER, Message.EDITOR_MAIN_MENU_NO_PARTICLES, Message.EDITOR_MAIN_MENU_NO_PARTICLES_DESCRIPTION);
	protected final ItemStack singleParticleItem = ItemUtil.createItem(Material.REDSTONE, Message.EDITOR_MAIN_MENU_SET_PARTICLE);
	protected final ItemStack multipleParticlesItem = ItemUtil.createItem(Material.BUCKET, Message.EDITOR_MAIN_MENU_EDIT_PARTICLES);
	
	protected final EditorAction setParticleAction;
	protected final EditorAction editParticleAction;
	
	protected final Hat targetHat;
	
	public EditorMainMenu(Core core, Player owner, MenuBuilder menuBuilder) 
	{
		super(core, owner, menuBuilder);
		targetHat = menuBuilder.getTargetHat();
		inventory = Bukkit.createInventory(null, 54, Message.EDITOR_MAIN_MENU_TITLE.getValue());
		
		setParticleAction = (event, slot) ->
		{
			if (event.isLeftClick())
			{
				EditorParticleSelectionMenu editorParticleMenu = new EditorParticleSelectionMenu(core, owner, menuBuilder, 0, (particle) ->
				{
					Hat hat = menuBuilder.getTargetHat();
					hat.setParticle(0, particle);
					
					if (targetHat.getEffect().getParticlesSupported() == 1) {
						EditorLore.updateParticleDescription(getItem(particleItemSlot), targetHat, 0);
					}
					
					menuBuilder.goBack();
				});
				menuBuilder.addMenu(editorParticleMenu);
				editorParticleMenu.open();
			}
			
			else if (event.isRightClick()) {
				onParticleEdit(getItem(particleItemSlot), 0);
			}
			
			return EditorClickType.NEUTRAL;
		};
		
		editParticleAction = (event, slot) ->
		{
			EditorParticleOverviewMenu editorParticleOverviewMenu = new EditorParticleOverviewMenu(core, owner, menuBuilder);
			menuBuilder.addMenu(editorParticleOverviewMenu);
			editorParticleOverviewMenu.open();
			return EditorClickType.NEUTRAL;
		};
		
		build();
	}
	
	@Override
	public void onClose (boolean forced)
	{
		Hat hat = menuBuilder.getTargetHat();
		if (hat.isModified())
		{
			MySQLDatabase database = (MySQLDatabase)core.getDatabase();
			String sqlQuery = hat.getSQLUpdateQuery();
			
			Core.debug("saving hat with query: " + sqlQuery);
			
			database.saveIncremental(menuBuilder.getEditingMenu().getName(), menuBuilder.getTargetSlot(), hat.getSQLUpdateQuery());
			hat.clearPropertyChanges();
		}
		
		// Save any particle data if the hat only supports one particle
		if (hat.getType().getParticlesSupported() == 1)
		{
			Database database = core.getDatabase();
			database.saveParticleData(menuBuilder.getEditingMenu().getName(), targetHat, 0);
			case NO_DATA:
				break;
		
			case COLOR:
			{
				EditorColorMenu editorColorMenu = new EditorColorMenu(core, owner, menuBuilder, particleIndex, () ->
				{
					EditorLore.updateParticleDescription(item, targetHat, particleIndex);
				});
				menuBuilder.addMenu(editorColorMenu);
				editorColorMenu.open();
				break;
			}
			
			case BLOCK_DATA:
			{
				Message menuTitle = Message.EDITOR_ICON_MENU_BLOCK_TITLE;
				Message blockTitle = Message.EDITOR_ICON_MENU_BLOCK_INFO;
				Message blockDescription = Message.EDITOR_ICON_MENU_BLOCK_DESCRIPTION;
				
				EditorIconMenu editorBlockMenu = new EditorIconMenu(core, owner, menuBuilder, menuTitle, blockTitle, blockDescription, (i) ->
				{
					if (item.getType().isBlock()) 
					{
						targetHat.setParticleBlock(particleIndex, i.getType());
						EditorLore.updateParticleDescription(item, targetHat, particleIndex);
					}
				});
				menuBuilder.addMenu(editorBlockMenu);
				editorBlockMenu.open();
				break;
			}
			
			case ITEM_DATA:
			{
				Message menuTitle = Message.EDITOR_ICON_MENU_ITEM_TITLE;
				Message itemTitle = Message.EDITOR_ICON_MENU_ITEM_INFO;
				Message itemDescription = Message.EDITOR_ICON_MENU_ITEM_DESCRIPTION;
				
				EditorIconMenu editorItemMenu = new EditorIconMenu(core, owner, menuBuilder, menuTitle, itemTitle, itemDescription, (i) ->
				{
					if (!item.getType().isBlock()) 
					{
						targetHat.setParticleItem(particleIndex, i);
						EditorLore.updateParticleDescription(item, targetHat, particleIndex);
					}
				});
				menuBuilder.addMenu(editorItemMenu);
				editorItemMenu.open();
				break;
			}
			 
			case ITEMSTACK_DATA:
			{
				EditorItemStackMenu editorItemStackMenu = new EditorItemStackMenu(core, owner, menuBuilder, particleIndex, () ->
				{
					EditorLore.updateParticleDescription(item, targetHat, particleIndex);
				});
				menuBuilder.addMenu(editorItemStackMenu);
				editorItemStackMenu.open();
				break;
			}
		}
	}
	
	/**
	 * Updates the main menu to reflect an offset property update
	 */
	private void onOffsetChange () {
		EditorLore.updateVectorDescription(getItem(25), menuBuilder.getTargetHat().getOffset(), Message.EDITOR_MAIN_MENU_VECTOR_DESCRIPTION);
	}
	
	/**
	 * Updates the main menu to reflect an angle property update
	 */
	private void onAngleChange () {
		EditorLore.updateVectorDescription(getItem(24), menuBuilder.getTargetHat().getAngle(), Message.EDITOR_MAIN_MENU_VECTOR_DESCRIPTION);
	}
	
	/**
	 * Returns an appropriate item for the current ParticleType
	 * @return
	 */
	private ItemStack getParticleItem () 
	{
		Hat hat = menuBuilder.getTargetHat();
		int particlesSupported = hat.getEffect().getParticlesSupported();
		
		if (particlesSupported == 0) {
			return noParticleItem;
		}
		
		if (particlesSupported == 1) {
			return singleParticleItem;
		}
		
		return multipleParticlesItem;
	}
	
	/**
	 * Returns an appropriate EditorAction for the current ParticleType
	 * @return
	 */
	private EditorAction getParticleAction () 
	{
		Hat hat = menuBuilder.getTargetHat();
		int particlesSupported = hat.getEffect().getParticlesSupported();
		
		if (particlesSupported == 0) {
			return emptyAction;
		}
		
		if (particlesSupported == 1) {
			return setParticleAction;
		}
		
		return editParticleAction;
	}

	@Override
	protected void build() 
	{
		// Main Menu
		setButton(46, mainMenuButton, backAction);
		
		// Equip
		setButton(52, ItemUtil.createItem(Material.DIAMOND_HELMET, Message.EDITOR_MISC_EQUIP), (event, slot) ->
		{
			return EditorClickType.NEUTRAL;
		});
		
		// Type
		ItemStack typeItem = ItemUtil.createItem(Material.CYAN_DYE, Message.EDITOR_MAIN_MENU_SET_TYPE);
		EditorLore.updateTypeDescription(typeItem, targetHat);
		setButton(10, typeItem, (event, slot) ->
		{
			if (!event.isShiftClick())
			{
				EditorTypeMenu editorTypeMenu = new EditorTypeMenu(core, owner, menuBuilder, () ->
				{
					ParticleType type = targetHat.getType();
					if (!type.supportsAnimation() && targetHat.getAnimation().equals(ParticleAnimation.ANIMATED)) {
						targetHat.setAnimation(ParticleAnimation.STATIC);
					}
					
					EditorLore.updateTrackingDescription(getItem(trackingItemSlot), targetHat);
					EditorLore.updateTypeDescription(getItem(10), targetHat);
					
					ItemStack particleItem = getParticleItem();
					setButton(particleItemSlot, particleItem, getParticleAction());
					
					if (targetHat.getEffect().getParticlesSupported() == 1) {
						EditorLore.updateParticleDescription(getItem(particleItemSlot), targetHat, 0);
					}
				});
				menuBuilder.addMenu(editorTypeMenu);
				editorTypeMenu.open();
			}
			
			else
			{
				int id = targetHat.getAnimation().getID();
				ParticleAnimation animation = ParticleAnimation.fromID(MathUtil.wrap(id + 1, ParticleAnimation.values().length, 0));
				targetHat.setAnimation(animation);
				EditorLore.updateTypeDescription(getItem(10), targetHat);
			}
			return EditorClickType.NEUTRAL;
		});
		
		// Location
		ItemStack locationItem = ItemUtil.createItem(Material.CLAY_BALL, Message.EDITOR_MAIN_MENU_SET_LOCATION);
		EditorLore.updateLocationDescription(locationItem, targetHat.getLocation(), Message.EDITOR_MAIN_MENU_LOCATION_DESCRIPTION);
		setButton(11, locationItem, (event, slot) ->
		{
			final int increment = event.isLeftClick() ? 1 : -1;
			final int locationID = MathUtil.wrap(targetHat.getLocation().getID() + increment, ParticleLocation.values().length, 0);
			final ParticleLocation location = ParticleLocation.fromId(locationID);
			
			targetHat.setLocation(location);
			EditorLore.updateLocationDescription(getItem(11), location, Message.EDITOR_MAIN_MENU_LOCATION_DESCRIPTION);
			return event.isLeftClick() ? EditorClickType.POSITIVE : EditorClickType.NEGATIVE;
		});
		
		// Meta
		ItemStack metaItem = ItemUtil.createItem(Material.SIGN, Message.EDITOR_MAIN_MENU_SET_META);
		EditorLore.updateGenericDescription(metaItem, Message.EDITOR_MAIN_MENU_META_DESCRIPTION);
		setButton(13, metaItem, (event, slot) ->
		{
			if (event.isLeftClick())
			{
				EditorMetaMenu editorMetaMenu = new EditorMetaMenu(core, owner, menuBuilder);
				menuBuilder.addMenu(editorMetaMenu);
				editorMetaMenu.open();
			}
			
			else if (event.isRightClick())
			{
				EditorDescriptionMenu editorDescriptionMenu = new EditorDescriptionMenu(core, owner, menuBuilder, true);
				menuBuilder.addMenu(editorDescriptionMenu);
				editorDescriptionMenu.open();
			}
			
			return EditorClickType.NEUTRAL;
		});
		
		// Price
		ItemStack priceItem = ItemUtil.createItem(Material.GOLD_NUGGET, Message.EDITOR_MAIN_MENU_SET_PRICE);
		EditorLore.updatePriceDescription(priceItem, targetHat.getPrice(), Message.EDITOR_MAIN_MENU_PRICE_DESCRIPTION);
		setButton(15, priceItem, (event, slot) ->
		{
			final int increment = (event.isLeftClick() ? 1 : -1) * (event.isShiftClick() ? 10 : 1);
			final int price = (int) MathUtil.clamp(targetHat.getPrice() + increment, 0, 2000000000);
			
			targetHat.setPrice(price);
			EditorLore.updatePriceDescription(getItem(15), price, Message.EDITOR_MAIN_MENU_PRICE_DESCRIPTION);
			return event.isLeftClick() ? EditorClickType.POSITIVE : EditorClickType.NEGATIVE;
		});
		
		// Speed
		ItemStack speedItem = ItemUtil.createItem(Material.SUGAR, Message.EDITOR_MAIN_MENU_SET_SPEED);
		EditorLore.updateIntegerDescription(speedItem, targetHat.getSpeed(), Message.EDITOR_MAIN_MENU_SPEED_DESCRIPTION);
		setButton(16, speedItem, (event, slot) ->
		{
			final int increment = event.isLeftClick() ? 1 : -1;
			final int speed = (int) MathUtil.clamp(targetHat.getSpeed() + increment, 0, 10);
			
			targetHat.setSpeed(speed);
			EditorLore.updateIntegerDescription(getItem(16), speed, Message.EDITOR_MAIN_MENU_SPEED_DESCRIPTION);
			return event.isLeftClick() ? EditorClickType.POSITIVE : EditorClickType.NEGATIVE;
		});
		
		// Action
		ItemStack actionItem = ItemUtil.createItem(Material.GUNPOWDER, Message.EDITOR_MAIN_MENU_SET_ACTION);
		EditorLore.updateGenericActionDescription(actionItem, targetHat);
		setButton(19, actionItem, (event, slot) ->
		{
			EditorActionOverviewMenu editorActionOverviewMenu = new EditorActionOverviewMenu(core, owner, menuBuilder, () ->
			{
				EditorLore.updateGenericActionDescription(getItem(19), menuBuilder.getBaseHat());
			});
			menuBuilder.addMenu(editorActionOverviewMenu);
			editorActionOverviewMenu.open();
			return EditorClickType.NEUTRAL;
		});
		
		// Mode
		ItemStack modeItem = ItemUtil.createItem(Material.ROSE_RED, Message.EDITOR_MAIN_MENU_SET_MODE);
		EditorLore.updateModeDescription(modeItem, targetHat.getMode(), Message.EDITOR_MAIN_MENU_MODE_DESCRIPTION);
		setButton(20, modeItem, (event, slot) ->
		{
			final int increment = event.isLeftClick() ? 1 : -1;
			final int modeID = MathUtil.wrap(targetHat.getMode().getID() + increment, ParticleMode.values().length, 0);
			final ParticleMode mode = ParticleMode.fromId(modeID);
			
			targetHat.setMode(mode);
			EditorLore.updateModeDescription(getItem(20), mode, Message.EDITOR_MAIN_MENU_MODE_DESCRIPTION);
			return event.isLeftClick() ? EditorClickType.POSITIVE : EditorClickType.NEGATIVE;
		});
		
		// Frequency
		ItemStack frequencyItem = ItemUtil.createItem(Material.COMPARATOR, Message.EDITOR_MAIN_MENU_SET_UPDATE_FREQUENCY);
		EditorLore.updateFrequencyDescription(frequencyItem, targetHat.getUpdateFrequency(), Message.EDITOR_MAIN_MENU_UPDATE_FREQUENCY_DESCRIPTION);
		setButton(22, frequencyItem, (event, slot) ->
		{
			final int increment = event.isLeftClick() ? 1 : -1;
			final int frequency = (int) MathUtil.clamp(targetHat.getUpdateFrequency() + increment, 1, 63);
			
			targetHat.setUpdateFrequency(frequency);
			EditorLore.updateFrequencyDescription(getItem(22), frequency, Message.EDITOR_MAIN_MENU_UPDATE_FREQUENCY_DESCRIPTION);
			return event.isLeftClick() ? EditorClickType.POSITIVE : EditorClickType.NEGATIVE;
		});
		
		// Angle
		ItemStack angleItem =  ItemUtil.createItem(Material.SLIME_BALL, Message.EDITOR_MAIN_MENU_SET_ANGLE);
		EditorLore.updateVectorDescription(angleItem, targetHat.getAngle(), Message.EDITOR_MAIN_MENU_VECTOR_DESCRIPTION);
		setButton(24, angleItem, (event, slot) ->
		{
			if (event.isLeftClick())
			{
				EditorAngleMenu editorAngleMenu = new EditorAngleMenu(core, owner, menuBuilder, () ->
				{
					EditorLore.updateVectorDescription(getItem(24), menuBuilder.getTargetHat().getAngle(), Message.EDITOR_MAIN_MENU_VECTOR_DESCRIPTION);
				});
				menuBuilder.addMenu(editorAngleMenu);
				editorAngleMenu.open();
			}
			
			else if (event.isShiftRightClick()) 
			{
				targetHat.setAngle(0, 0, 0);
				onAngleChange();
			}
			return EditorClickType.NEUTRAL;
		});
		
		// Offset
		ItemStack offsetItem = ItemUtil.createItem(Material.REPEATER, Message.EDITOR_MAIN_MENU_SET_OFFSET);
		EditorLore.updateVectorDescription(offsetItem, targetHat.getOffset(), Message.EDITOR_MAIN_MENU_VECTOR_DESCRIPTION);
		setButton(25, offsetItem, (event, slot) ->
		{
			if (event.isLeftClick())
			{
				EditorOffsetMenu editorOffsetMenu = new EditorOffsetMenu(core, owner, menuBuilder, () ->
				{
					EditorLore.updateVectorDescription(getItem(25), menuBuilder.getTargetHat().getOffset(), Message.EDITOR_MAIN_MENU_VECTOR_DESCRIPTION);
				});
				menuBuilder.addMenu(editorOffsetMenu);
				editorOffsetMenu.open();
			}
			
			else if (event.isShiftRightClick())
			{
				targetHat.setOffset(0, 0, 0);
				onOffsetChange();
			}
			return EditorClickType.NEUTRAL;
		});
		
		// Sound
		ItemStack soundItem = ItemUtil.createItem(Material.MUSIC_DISC_STRAD, Message.EDITOR_MAIN_MENU_SET_SOUND);
		EditorLore.updateSoundItemDescription(soundItem, targetHat);
		setButton(28, soundItem, (event, slot) ->
		{
			if (event.isLeftClick())
			{
				EditorSoundMenu editorSoundMenu = new EditorSoundMenu(core, owner, menuBuilder, (sound) ->
				{
					targetHat.setSound(sound);
					EditorLore.updateSoundItemDescription(getItem(28), targetHat);
					menuBuilder.goBack();
				});
				menuBuilder.addMenu(editorSoundMenu);
				editorSoundMenu.open();
			}
			
			else if (event.isShiftRightClick()) 
			{
				targetHat.removeSound();
				EditorLore.updateSoundItemDescription(getItem(28), targetHat);
			}
			return EditorClickType.NEUTRAL;
		});
		
		// Tracking
		ItemStack trackingItem = ItemUtil.createItem(Material.COMPASS, Message.EDITOR_MAIN_MENU_SET_TRACKING_METHOD); 
		EditorLore.updateTrackingDescription(trackingItem, targetHat);
		setButton(trackingItemSlot, trackingItem, (event, slot) ->
		{
			List<ParticleTracking> methods = targetHat.getEffect().getSupportedTrackingMethods();
			
			final int increment = event.isLeftClick() ? 1 : -1;
			final int size = methods.size();
			final int index = MathUtil.wrap(methods.indexOf(targetHat.getTrackingMethod()) + increment, size, 0);
			
			targetHat.setTrackingMethod(methods.get(index));
			EditorLore.updateTrackingDescription(getItem(trackingItemSlot), targetHat);
			return event.isLeftClick() ? EditorClickType.POSITIVE : EditorClickType.NEGATIVE;
		});
		
		// Clone
		ItemStack cloneItem = ItemUtil.createItem(Material.PRISMARINE_SHARD, Message.EDITOR_MAIN_MENU_CLONE, Message.EDITOR_MAIN_MENU_CLONE_DESCRIPTION);
		setButton(30, cloneItem, (event, slot) ->
		{
			EditorSlotMenu editorSlotMenu = new EditorSlotMenu(core, owner, menuBuilder, true);
			menuBuilder.addMenu(editorSlotMenu);
			editorSlotMenu.open();
			return EditorClickType.NEUTRAL;
		});
		
		// Move
		ItemStack moveItem = ItemUtil.createItem(Material.MAP, Message.EDITOR_MAIN_MENU_MOVE, Message.EDITOR_MAIN_MENU_MOVE_DESCRIPTION);
		setButton(32, moveItem, (event, slot) ->
		{
			EditorMenuSelectionMenu editorMenuSelectionMenu = new EditorMenuSelectionMenu(core, owner, menuBuilder, false, (menu) ->
			{
				EditorTransferMenu editorTransferMenu = new EditorTransferMenu(core, owner, menuBuilder, menu);
				menuBuilder.addMenu(editorTransferMenu);
				editorTransferMenu.open();
			});
			menuBuilder.addMenu(editorMenuSelectionMenu);
			editorMenuSelectionMenu.open();
			return EditorClickType.NEUTRAL;
		});
		
		// Count
		ItemStack countItem = ItemUtil.createItem(Material.WHEAT_SEEDS, Message.EDITOR_MAIN_MENU_SET_COUNT);
		EditorLore.updateIntegerDescription(countItem, targetHat.getCount(), Message.EDITOR_MAIN_MENU_COUNT_DESCRIPTION);
		setButton(33, countItem, (event, slot) ->
		{
			final int increment = event.isLeftClick() ? 1 : -1;
			final int count = (int) MathUtil.clamp(targetHat.getCount() + increment, 1, 15);
			
			targetHat.setCount(count);
			EditorLore.updateIntegerDescription(getItem(33), count, Message.EDITOR_MAIN_MENU_COUNT_DESCRIPTION);
			return event.isLeftClick() ? EditorClickType.POSITIVE : EditorClickType.NEGATIVE;
		});
		
		// Slot
		ItemStack slotItem = ItemUtil.createItem(Material.ITEM_FRAME, Message.EDITOR_MAIN_MENU_SET_SLOT, Message.EDITOR_MAIN_MENU_SLOT_DESCRIPTION);
		setButton(39, slotItem, (event, slot) ->
		{
			EditorSlotMenu editorSlotMenu = new EditorSlotMenu(core, owner, menuBuilder);
			menuBuilder.addMenu(editorSlotMenu);
			editorSlotMenu.open();
			return EditorClickType.NEUTRAL;
		});
		
		// Particle
		ItemStack particleItem = getParticleItem();
		if (targetHat.getEffect().getParticlesSupported() == 1) {
			EditorLore.updateParticleDescription(particleItem, targetHat, 0);
		}
		setButton(particleItemSlot, getParticleItem(), getParticleAction());
		
		// Icon
		ItemStack iconItem = ItemUtil.createItem(targetHat.getMaterial(), Message.EDITOR_MAIN_MENU_SET_ICON, Message.EDITOR_MAIN_MENU_ICON_DESCRIPTION);
		setButton(41, iconItem, (event, slot) ->
		{
			EditorIconOverviewMenu editorIconOverviewMenu = new EditorIconOverviewMenu(core, owner, menuBuilder, (item) ->
			{
				getItem(41).setType(item.getType());
			});
			menuBuilder.addMenu(editorIconOverviewMenu);
			editorIconOverviewMenu.open();
			return EditorClickType.NEUTRAL;
		});
		
		// TODO: Node Editor
	}
}
