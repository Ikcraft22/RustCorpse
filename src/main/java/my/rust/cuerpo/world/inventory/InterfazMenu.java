package my.rust.cuerpo.world.inventory;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.capabilities.Capabilities;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

import my.rust.cuerpo.init.RustcuerpoModMenus;

import java.util.function.Supplier;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class InterfazMenu extends AbstractContainerMenu implements RustcuerpoModMenus.MenuAccessor {
	public final Map<String, Object> menuState = new HashMap<>() {
		@Override
		public Object put(String key, Object value) {
			if (!this.containsKey(key) && this.size() >= 42)
				return null;
			return super.put(key, value);
		}
	};
	public final Level world;
	public final Player entity;
	public int x, y, z;
	private ContainerLevelAccess access = ContainerLevelAccess.NULL;
	private IItemHandler internal;
	private final Map<Integer, Slot> customSlots = new HashMap<>();
	private boolean bound = false;
	private Supplier<Boolean> boundItemMatcher = null;
	private Entity boundEntity = null;
	private BlockEntity boundBlockEntity = null;

	public InterfazMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
		super(RustcuerpoModMenus.INTERFAZ.get(), id);
		this.entity = inv.player;
		this.world = inv.player.level();
		this.internal = new ItemStackHandler(83);
		BlockPos pos = null;
		if (extraData != null) {
			pos = extraData.readBlockPos();
			this.x = pos.getX();
			this.y = pos.getY();
			this.z = pos.getZ();
			access = ContainerLevelAccess.create(world, pos);
		}
		if (pos != null) {
			if (extraData.readableBytes() == 1) { // bound to item
				byte hand = extraData.readByte();
				ItemStack itemstack = hand == 0 ? this.entity.getMainHandItem() : this.entity.getOffhandItem();
				this.boundItemMatcher = () -> itemstack == (hand == 0 ? this.entity.getMainHandItem() : this.entity.getOffhandItem());
				IItemHandler cap = itemstack.getCapability(Capabilities.ItemHandler.ITEM);
				if (cap != null) {
					this.internal = cap;
					this.bound = true;
				}
			} else if (extraData.readableBytes() > 1) { // bound to entity
				extraData.readByte(); // drop padding
				boundEntity = world.getEntity(extraData.readVarInt());
				if (boundEntity != null) {
					IItemHandler cap = boundEntity.getCapability(Capabilities.ItemHandler.ENTITY);
					if (cap != null) {
						this.internal = cap;
						this.bound = true;
					}
				}
			} else { // might be bound to block
				boundBlockEntity = this.world.getBlockEntity(pos);
				if (boundBlockEntity instanceof BaseContainerBlockEntity baseContainerBlockEntity) {
					this.internal = new InvWrapper(baseContainerBlockEntity);
					this.bound = true;
				}
			}
		}

		// 1. Offhand (Slot 0)
		this.customSlots.put(0, this.addSlot(new SlotItemHandler(internal, 0, 49, 93) {
			@Override public boolean mayPlace(ItemStack stack) { return false; }
		}));

		// 2. Armor (Slots 1-4)
		for (int i = 0; i < 4; i++) {
			int index = 1 + i;
			this.customSlots.put(index, this.addSlot(new SlotItemHandler(internal, index, 13, 75 - (i * 18)) {
				@Override public boolean mayPlace(ItemStack stack) { return false; }
			}));
		}

		// 3. Inventario Interno (Slots 5-31)
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				int index = 5 + col + (row * 9);
				this.customSlots.put(index, this.addSlot(new SlotItemHandler(internal, index, 103 + (col * 18), 21 + (row * 18)) {
					@Override public boolean mayPlace(ItemStack stack) { return false; }
				}));
			}
		}

		// 4. Barra de Herramientas / Hotbar (Slots 32-40)
		for (int col = 0; col < 9; col++) {
			int index = 32 + col;
			this.customSlots.put(index, this.addSlot(new SlotItemHandler(internal, index, 103 + (col * 18), 84) {
				@Override public boolean mayPlace(ItemStack stack) { return false; }
			}));
		}

		// 5. Accessories Slots (Slots 41-55) - 2 columnas pequeñas al lado de la armadura
		for (int i = 0; i < 15; i++) {
			int index = 41 + i;
			int col = i / 8;
			int row = i % 8;
			this.customSlots.put(index, this.addSlot(new SlotItemHandler(internal, index, 31 + (col * 18), 21 + (row * 18)) {
				@Override public boolean mayPlace(ItemStack stack) { return false; }
			}));
		}

		for (int si = 0; si < 3; ++si)
			for (int sj = 0; sj < 9; ++sj)
				this.addSlot(new Slot(inv, sj + (si + 1) * 9, 96 + 8 + sj * 18, 62 + 84 + si * 18));
		for (int si = 0; si < 9; ++si)
			this.addSlot(new Slot(inv, si, 96 + 8 + si * 18, 62 + 142));
	}

	@Override
	public boolean stillValid(Player player) {
		if (this.bound) {
			if (this.boundItemMatcher != null)
				return this.boundItemMatcher.get();
			else if (this.boundBlockEntity != null)
				return AbstractContainerMenu.stillValid(this.access, player, this.boundBlockEntity.getBlockState().getBlock());
			else if (this.boundEntity != null)
				return this.boundEntity.isAlive();
		}
		return true;
	}

	@Override
	public boolean clickMenuButton(Player player, int buttonId) {
		if (buttonId == 0) {
			if (player instanceof ServerPlayer serverPlayer) {
				if (boundEntity instanceof PlayerCorpseEntity corpse) {
					// Transferir Logros: Otorgamos todos los criterios de los logros que el cuerpo tenía
					CompoundTag corpseAdvancements = corpse.getAdvancementsData();
					for (String key : corpseAdvancements.getAllKeys()) {
						ResourceLocation advancementId = ResourceLocation.parse(key);
						AdvancementHolder holder = serverPlayer.server.getAdvancements().get(advancementId);
						if (holder != null) {
							for (String criterion : holder.value().criteria().keySet()) {
								serverPlayer.getAdvancements().award(holder, criterion);
							}
						}
					}
					
					// Borrar info del cuerpo (ya fue robada)
					corpse.setAdvancementsData(new CompoundTag());
					corpse.setStatsData(new CompoundTag());
				}

				for (int i = 0; i < internal.getSlots(); i++) {
					// Extraemos físicamente el ítem del inventario del cuerpo
					ItemStack stack = internal.extractItem(i, Integer.MAX_VALUE, false);
					if (!stack.isEmpty()) {
						// Intentamos añadirlo al inventario del jugador
						if (!serverPlayer.getInventory().add(stack)) {
							// Si el inventario está lleno, soltamos lo que sobre en el suelo
							serverPlayer.drop(stack, false);
						}
					}
				}
				this.broadcastChanges();

				// Si tras el "Take All" el cuerpo está vacío, lo eliminamos inmediatamente
				if (boundEntity instanceof PlayerCorpseEntity corpse && corpse.isInventoryEmpty()) {
					corpse.discard();
				}
			}
			return true;
		}
		return super.clickMenuButton(player, buttonId);
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = (Slot) this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (index < 56) {
				if (!this.moveItemStackTo(itemstack1, 56, this.slots.size(), true))
					return ItemStack.EMPTY;
				slot.onQuickCraft(itemstack1, itemstack);
			} else if (!this.moveItemStackTo(itemstack1, 0, 56, false)) {
				if (index < 56 + 27) {
					if (!this.moveItemStackTo(itemstack1, 56 + 27, this.slots.size(), true))
						return ItemStack.EMPTY;
				} else {
					if (!this.moveItemStackTo(itemstack1, 56, 56 + 27, false))
						return ItemStack.EMPTY;
				}
				return ItemStack.EMPTY;
			}
			if (itemstack1.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}
			slot.onTake(playerIn, itemstack1);
		}
		return itemstack;
	}

	@Override
	protected boolean moveItemStackTo(ItemStack p_38904_, int p_38905_, int p_38906_, boolean p_38907_) {
		boolean flag = false;
		int i = p_38905_;
		if (p_38907_) {
			i = p_38906_ - 1;
		}
		if (p_38904_.isStackable()) {
			while (!p_38904_.isEmpty() && (p_38907_ ? i >= p_38905_ : i < p_38906_)) {
				Slot slot = this.slots.get(i);
				ItemStack itemstack = slot.getItem();
				if (slot.mayPlace(itemstack) && !itemstack.isEmpty() && ItemStack.isSameItemSameComponents(p_38904_, itemstack)) {
					int j = itemstack.getCount() + p_38904_.getCount();
					int k = slot.getMaxStackSize(itemstack);
					if (j <= k) {
						p_38904_.setCount(0);
						itemstack.setCount(j);
						slot.set(itemstack);
						flag = true;
					} else if (itemstack.getCount() < k) {
						p_38904_.shrink(k - itemstack.getCount());
						itemstack.setCount(k);
						slot.set(itemstack);
						flag = true;
					}
				}
				if (p_38907_) {
					i--;
				} else {
					i++;
				}
			}
		}
		if (!p_38904_.isEmpty()) {
			if (p_38907_) {
				i = p_38906_ - 1;
			} else {
				i = p_38905_;
			}
			while (p_38907_ ? i >= p_38905_ : i < p_38906_) {
				Slot slot1 = this.slots.get(i);
				ItemStack itemstack1 = slot1.getItem();
				if (itemstack1.isEmpty() && slot1.mayPlace(p_38904_)) {
					int l = slot1.getMaxStackSize(p_38904_);
					slot1.setByPlayer(p_38904_.split(Math.min(p_38904_.getCount(), l)));
					slot1.setChanged();
					flag = true;
					break;
				}
				if (p_38907_) {
					i--;
				} else {
					i++;
				}
			}
		}
		return flag;
	}

	@Override
	public void removed(Player playerIn) {
		super.removed(playerIn);
		if (!bound && playerIn instanceof ServerPlayer serverPlayer) {
			if (!serverPlayer.isAlive() || serverPlayer.hasDisconnected()) {
				for (int j = 0; j < internal.getSlots(); ++j) {
					playerIn.drop(internal.getStackInSlot(j), false);
					if (internal instanceof IItemHandlerModifiable ihm)
						ihm.setStackInSlot(j, ItemStack.EMPTY);
				}
			} else {
				for (int i = 0; i < internal.getSlots(); ++i) {
					playerIn.getInventory().placeItemBackInInventory(internal.getStackInSlot(i));
					if (internal instanceof IItemHandlerModifiable ihm)
						ihm.setStackInSlot(i, ItemStack.EMPTY);
				}
			}
		}
	}

	@Override
	public Map<Integer, Slot> getSlots() {
		return Collections.unmodifiableMap(customSlots);
	}

	@Override
	public Map<String, Object> getMenuState() {
		return menuState;
	}
}