package my.rust.cuerpo.client.gui;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

import my.rust.cuerpo.world.inventory.InterfazMenu;
import my.rust.cuerpo.init.RustcuerpoModScreens;

import com.mojang.blaze3d.systems.RenderSystem;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class InterfazScreen extends AbstractContainerScreen<InterfazMenu> implements RustcuerpoModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;
	private Button button_take_all;
	private static final ResourceLocation BACKGROUND = ResourceLocation.parse("rustcuerpo:textures/screens/interfaz.png");

	public InterfazScreen(InterfazMenu container, Inventory inventory, Component text) {
		super(container, inventory, text);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
		this.imageWidth = 367;
		this.imageHeight = 234;
	}

	@Override
	public void updateMenuState(int elementType, String name, Object elementState) {
		menuStateUpdateActive = true;
		menuStateUpdateActive = false;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(guiGraphics, mouseX, mouseY);

		// Renderizar el modelo del jugador bloqueando su rotación para que siempre mire al frente
		if (this.entity != null) {
			float oldYRot = this.entity.getYRot();
			float oldXRot = this.entity.getXRot();
			float oldYBodyRot = this.entity.yBodyRot;
			float oldYHeadRot = this.entity.yHeadRot;

			// Forzamos la rotación base a 0 para que la matriz de rotación tenga el control total
			this.entity.setYRot(0.0F);
			this.entity.setXRot(0.0F);
			this.entity.yBodyRot = 0.0F;
			this.entity.yHeadRot = 0.0F;

			// Ajustamos la rotación: X=180 para tumbarlo, Y=180 para que mire de frente en el menú
			Quaternionf rotation = new Quaternionf().rotationXYZ((float) Math.toRadians(180), (float) Math.toRadians(180), 0.0F);
			InventoryScreen.renderEntityInInventory(guiGraphics, (float) this.leftPos + 51, (float) this.topPos + 75, 30, new Vector3f(), rotation, null, this.entity);

			// Restauramos la rotación original para no afectar al jugador en el mundo
			this.entity.setYRot(oldYRot);
			this.entity.setXRot(oldXRot);
			this.entity.yBodyRot = oldYBodyRot;
			this.entity.yHeadRot = oldYHeadRot;
		}
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		guiGraphics.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
		RenderSystem.disableBlend();
	}

	@Override
	public boolean keyPressed(int key, int b, int c) {
		if (key == 256) {
			this.minecraft.player.closeContainer();
			return true;
		}
		return super.keyPressed(key, b, c);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
	}

	@Override
	public void init() {
		super.init();
		button_take_all = Button.builder(Component.literal("Take All"), e -> {
			this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
		}).bounds(this.leftPos + 282, this.topPos + 146, 65, 20).build();
		this.addRenderableWidget(button_take_all);
	}
}