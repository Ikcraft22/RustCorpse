package my.rust.cuerpo;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import my.rust.cuerpo.world.inventory.PlayerCorpseEntity;
import my.rust.cuerpo.client.renderer.PlayerCorpseRenderer;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = RustCuerpo.MODID, value = Dist.CLIENT, bus = Bus.MOD)
public class RustCuerpoClient {
    public RustCuerpoClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        RustCuerpo.LOGGER.info("HELLO FROM CLIENT SETUP");
        RustCuerpo.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Registramos el nuevo renderer que maneja skins y rotación
        event.registerEntityRenderer(EntityRegistry.PLAYER_CORPSE.get(), PlayerCorpseRenderer::new);
    }
}
