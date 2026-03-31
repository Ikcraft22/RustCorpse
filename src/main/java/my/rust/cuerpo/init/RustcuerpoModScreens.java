/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package my.rust.cuerpo.init;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import my.rust.cuerpo.client.gui.InterfazScreen;

@EventBusSubscriber(Dist.CLIENT)
public class RustcuerpoModScreens {
	@SubscribeEvent
	public static void clientLoad(RegisterMenuScreensEvent event) {
		event.register(RustcuerpoModMenus.INTERFAZ.get(), InterfazScreen::new);
	}

	public interface ScreenAccessor {
		void updateMenuState(int elementType, String name, Object elementState);
	}
}