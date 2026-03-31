package my.rust.cuerpo;

import my.rust.cuerpo.world.inventory.PlayerCorpseEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class EntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, RustCuerpo.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<PlayerCorpseEntity>> PLAYER_CORPSE = ENTITIES.register("player_corpse",
            () -> EntityType.Builder.<PlayerCorpseEntity>of(PlayerCorpseEntity::new, MobCategory.MISC)
                    .sized(0.9F, 0.8F)
                    .build("player_corpse"));
}