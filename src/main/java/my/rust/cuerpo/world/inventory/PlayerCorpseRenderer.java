package my.rust.cuerpo.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import my.rust.cuerpo.world.inventory.PlayerCorpseEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import com.mojang.authlib.GameProfile;

public class PlayerCorpseRenderer extends EntityRenderer<PlayerCorpseEntity> {
    private final PlayerModel<LivingEntity> model;

    public PlayerCorpseRenderer(EntityRendererProvider.Context context) {
        super(context);
        // Usamos el modelo de jugador estándar (slim = false para este ejemplo básico)
        this.model = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false);
    }

    @Override
    public void render(PlayerCorpseEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        
        // 1. Acostar el cuerpo (rotar 90 grados en el eje X)
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        
        // 2. CENTRADO CRÍTICO: 
        // Un jugador mide ~1.8 bloques. Al rotarlo, queda desplazado.
        // Lo movemos -0.9 en el eje Y (que ahora es el Z del mundo) para que el
        // centro del cuerpo coincida exactamente con el centro de la hitbox (0,0,0).
        // El -0.1 en Z (ahora Y mundo) es para que no se entierre en el suelo.
        poseStack.translate(0.0D, -0.9D, -0.1D);
        
        // 3. Dibujar el modelo
        ResourceLocation texture = getTextureLocation(entity);
        var vertexConsumer = buffer.getBuffer(this.model.renderType(texture));
        // Corrección de parámetros para 1.21.1 (packedOverlay y color ARGB)
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
        
        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, buffer, packedLight);
    }

        @Override
        public ResourceLocation getTextureLocation(PlayerCorpseEntity entity) {
        // Esto obliga al renderizador a usar la skin de Steve/Alex base del juego
        return net.minecraft.client.resources.DefaultPlayerSkin.getDefaultTexture();
    }

}