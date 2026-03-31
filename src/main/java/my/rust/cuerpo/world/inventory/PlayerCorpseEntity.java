package my.rust.cuerpo.world.inventory;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.network.syncher.SynchedEntityData;
import java.util.UUID;

public class PlayerCorpseEntity extends Entity implements MenuProvider {
    private final ItemStackHandler inventory = new ItemStackHandler(83);
    private String playerName = "Cuerpo";
    private UUID playerUUID;
    private CompoundTag advancementsData = new CompoundTag();
    private CompoundTag statsData = new CompoundTag();

    public PlayerCorpseEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    public void setPlayerName(String name) {
        this.playerName = name;
        this.setCustomName(Component.literal("Cuerpo de " + name));
        this.setCustomNameVisible(true);
    }

    public void setPlayerUUID(UUID uuid) {
        this.playerUUID = uuid;
    }

    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    public void setAdvancementsData(CompoundTag data) {
        this.advancementsData = data;
    }

    public CompoundTag getAdvancementsData() {
        return this.advancementsData;
    }

    public void setStatsData(CompoundTag data) {
        this.statsData = data;
    }

    public CompoundTag getStatsData() {
        return this.statsData;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("Inventory")) inventory.deserializeNBT(this.level().registryAccess(), compound.getCompound("Inventory"));
        if (compound.contains("PlayerName")) this.playerName = compound.getString("PlayerName");
        if (compound.hasUUID("PlayerUUID")) this.playerUUID = compound.getUUID("PlayerUUID");
        if (compound.contains("Advancements")) this.advancementsData = compound.getCompound("Advancements");
        if (compound.contains("Stats")) this.statsData = compound.getCompound("Stats");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        compound.put("Inventory", inventory.serializeNBT(this.level().registryAccess()));
        compound.putString("PlayerName", playerName);
        if (this.playerUUID != null) compound.putUUID("PlayerUUID", this.playerUUID);
        compound.put("Advancements", this.advancementsData);
        compound.put("Stats", this.statsData);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(this, buf -> {
                buf.writeBlockPos(this.blockPosition());
                buf.writeByte(0); // padding
                buf.writeVarInt(this.getId());
            });
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

	@Override
	public void tick() {
		super.tick();
		if (!this.level().isClientSide) {
			// Si el inventario está vacío, eliminamos la entidad
			if (isInventoryEmpty()) {
				this.discard();
			}
		}
	}

	public boolean isInventoryEmpty() {
		for (int i = 0; i < inventory.getSlots(); i++) {
			if (!inventory.getStackInSlot(i).isEmpty()) {
				return false;
			}
		}
		return true;
	}

    @Override
    public Component getDisplayName() {
        return Component.literal("Cuerpo de " + playerName);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        // Usamos tu InterfazMenu existente
        FriendlyByteBuf extraData = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        extraData.writeBlockPos(this.blockPosition());
        extraData.writeByte(0); 
        extraData.writeVarInt(this.getId());
        return new InterfazMenu(id, inv, extraData);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
	public EntityDimensions getDimensions(Pose pose) {
        // Dimensiones similares a un jugador agachado o tumbado
        return EntityDimensions.scalable(0.9f, 0.8f);
    }
}