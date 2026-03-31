package my.rust.cuerpo.world.inventory;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.nbt.CompoundTag;
import my.rust.cuerpo.EntityRegistry;
import my.rust.cuerpo.RustCuerpo;

@EventBusSubscriber(modid = RustCuerpo.MODID)
public class PlayerEvents {

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        final int[] accSlotIndex = {41}; // Usamos un array para permitir la modificación dentro de lambdas

        if (player instanceof ServerPlayer serverPlayer && player.level() instanceof ServerLevel serverLevel) {
            // 1. Crear la entidad del cuerpo
            PlayerCorpseEntity corpse = new PlayerCorpseEntity(EntityRegistry.PLAYER_CORPSE.get(), serverLevel);
            corpse.setPos(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ());
            corpse.setPlayerName(serverPlayer.getScoreboardName());
            corpse.setPlayerUUID(serverPlayer.getUUID());

            // 1.1 Capturar Logros y Estadísticas
            // Capturar Logros completados
            CompoundTag advancementsTag = new CompoundTag();
            for (AdvancementHolder holder : serverPlayer.server.getAdvancements().getAllAdvancements()) {
                AdvancementProgress progress = serverPlayer.getAdvancements().getOrStartProgress(holder);
                if (progress.isDone()) {
                    advancementsTag.putBoolean(holder.id().toString(), true);
                }
            }
            corpse.setAdvancementsData(advancementsTag);

            // Forzar guardado de stats en disco para el jugador que sale
            serverPlayer.getStats().save();

            // 2. Transferencia mapeada del Inventario del Jugador al Cuerpo
            Inventory playerInv = serverPlayer.getInventory();
            var corpseInv = corpse.getInventory();

            // Slot 0: Offhand (Jugador 40 -> Cuerpo 0)
            corpseInv.setStackInSlot(0, playerInv.getItem(40).copy());
            playerInv.setItem(40, ItemStack.EMPTY);

            // Slots 1-4: Armadura (Jugador 36-39 -> Cuerpo 1-4)
            for (int i = 0; i < 4; i++) {
                corpseInv.setStackInSlot(1 + i, playerInv.getItem(36 + i).copy());
                playerInv.setItem(36 + i, ItemStack.EMPTY);
            }

            // Slots 5-31: Inventario Interno (Jugador 9-35 -> Cuerpo 5-31)
            for (int i = 0; i < 27; i++) {
                corpseInv.setStackInSlot(5 + i, playerInv.getItem(9 + i).copy());
                playerInv.setItem(9 + i, ItemStack.EMPTY);
            }

            // Slots 32-40: Hotbar (Jugador 0-8 -> Cuerpo 32-40)
            for (int i = 0; i < 9; i++) {
                corpseInv.setStackInSlot(32 + i, playerInv.getItem(i).copy());
                playerInv.setItem(i, ItemStack.EMPTY);
            }

            // 2.1 Transferencia de Accessories (Wisp Forest)
            if (net.neoforged.fml.ModList.get().isLoaded("accessories")) {
                var capability = io.wispforest.accessories.api.AccessoriesCapability.getOptionally(serverPlayer);
                if (capability.isPresent()) {
                    for (var container : capability.get().getContainers().values()) {
                        net.minecraft.world.SimpleContainer handler = container.getAccessories();
                        for (int j = 0; j < handler.getContainerSize() && accSlotIndex[0] < 56; j++) {
                            ItemStack accStack = handler.getItem(j);
                            if (!accStack.isEmpty()) {
                                corpseInv.setStackInSlot(accSlotIndex[0]++, accStack.copy());
                                handler.setItem(j, ItemStack.EMPTY);
                            }
                        }
                    }
                }
            }

            // 2.2 Transferencia de Curios API
            if (net.neoforged.fml.ModList.get().isLoaded("curios")) {
                top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(serverPlayer).ifPresent(handler -> {
                    for (var entry : handler.getCurios().entrySet()) {
                        net.neoforged.neoforge.items.IItemHandlerModifiable stacksHandler = entry.getValue().getStacks();
                        for (int j = 0; j < stacksHandler.getSlots() && accSlotIndex[0] < 56; j++) {
                            ItemStack stack = stacksHandler.getStackInSlot(j);
                            if (!stack.isEmpty()) {
                                corpseInv.setStackInSlot(accSlotIndex[0]++, stack.copy());
                                stacksHandler.setStackInSlot(j, ItemStack.EMPTY);
                            }
                        }
                    }
                });
            }

            // 2.3 Transferencia de Ender Chest (Slots 56-82)
            net.minecraft.world.inventory.PlayerEnderChestContainer enderChest = serverPlayer.getEnderChestInventory();
            for (int i = 0; i < enderChest.getContainerSize(); i++) {
                corpseInv.setStackInSlot(56 + i, enderChest.getItem(i).copy());
                enderChest.setItem(i, ItemStack.EMPTY);
            }

            // 3. Spawnear en el mundo
            serverLevel.addFreshEntity(corpse);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer && player.level() instanceof ServerLevel serverLevel) {
            // Buscar el cuerpo que pertenece a este jugador
            PlayerCorpseEntity playerCorpse = null;
            for (net.minecraft.world.entity.Entity entity : serverLevel.getAllEntities()) {
                if (entity instanceof PlayerCorpseEntity corpse && serverPlayer.getUUID().equals(corpse.getPlayerUUID())) {
                    playerCorpse = corpse;
                    break;
                }
            }

            if (playerCorpse != null) {
                var corpseInv = playerCorpse.getInventory();
                Inventory playerInv = serverPlayer.getInventory();
                final int[] accSlotIndex = {41};

                // 1. Restaurar Inventario Vanilla
                // Offhand
                playerInv.setItem(40, corpseInv.getStackInSlot(0).copy());
                
                // Armadura
                for (int i = 0; i < 4; i++) {
                    playerInv.setItem(36 + i, corpseInv.getStackInSlot(1 + i).copy());
                }

                // Inventario Principal
                for (int i = 0; i < 27; i++) {
                    playerInv.setItem(9 + i, corpseInv.getStackInSlot(5 + i).copy());
                }

                // Hotbar
                for (int i = 0; i < 9; i++) {
                    playerInv.setItem(i, corpseInv.getStackInSlot(32 + i).copy());
                }

                // 2. Restaurar Accesorios (Wisp Forest)
                if (net.neoforged.fml.ModList.get().isLoaded("accessories")) {
                    var capability = io.wispforest.accessories.api.AccessoriesCapability.getOptionally(serverPlayer);
                    if (capability.isPresent()) {
                        for (var container : capability.get().getContainers().values()) {
                            net.minecraft.world.SimpleContainer handler = container.getAccessories();
                            for (int j = 0; j < handler.getContainerSize() && accSlotIndex[0] < 56; j++) {
                                handler.setItem(j, corpseInv.getStackInSlot(accSlotIndex[0]++).copy());
                            }
                        }
                    }
                }

                // 3. Restaurar Curios
                if (net.neoforged.fml.ModList.get().isLoaded("curios")) {
                    top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(serverPlayer).ifPresent(handler -> {
                        for (var entry : handler.getCurios().entrySet()) {
                            net.neoforged.neoforge.items.IItemHandlerModifiable stacksHandler = entry.getValue().getStacks();
                            for (int j = 0; j < stacksHandler.getSlots() && accSlotIndex[0] < 56; j++) {
                                stacksHandler.setStackInSlot(j, corpseInv.getStackInSlot(accSlotIndex[0]++).copy());
                            }
                        }
                    });
                }

                // 4. Restaurar Ender Chest
                net.minecraft.world.inventory.PlayerEnderChestContainer enderChest = serverPlayer.getEnderChestInventory();
                for (int i = 0; i < enderChest.getContainerSize(); i++) {
                    ItemStack ecStack = corpseInv.getStackInSlot(56 + i);
                    enderChest.setItem(i, ecStack.copy());
                }

                // 4. Restaurar Logros (Advancements)
                CompoundTag advancementsTag = playerCorpse.getAdvancementsData();
                for (String key : advancementsTag.getAllKeys()) {
                    net.minecraft.resources.ResourceLocation advId = net.minecraft.resources.ResourceLocation.parse(key);
                    AdvancementHolder holder = serverPlayer.server.getAdvancements().get(advId);
                    if (holder != null) {
                        // Otorgar todos los criterios para completar el logro
                        for (String criterion : holder.value().criteria().keySet()) {
                            serverPlayer.getAdvancements().award(holder, criterion);
                        }
                    }
                }

                // 5. Eliminar el cuerpo ya que el alma ha vuelto
                playerCorpse.discard();
                
                // Notificar al jugador
                serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal("§6Has recuperado tus pertenencias del cuerpo."));
            }
        }
    }
}

@EventBusSubscriber(modid = RustCuerpo.MODID, bus = Bus.MOD)
class CapabilityEvents {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerEntity(Capabilities.ItemHandler.ENTITY, EntityRegistry.PLAYER_CORPSE.get(), (corpse, context) -> {
            return corpse.getInventory();
        });
    }
}