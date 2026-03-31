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
        
        // 2. Ajustar la altura para que no flote ni se hunda
        poseStack.translate(0.0D, -0.1D, -0.1D);
        
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
        if (entity.getPlayerUUID() != null) {
            // Se requiere un GameProfile para buscar la skin
            GameProfile profile = new GameProfile(entity.getPlayerUUID(), null);
            PlayerSkin skin = Minecraft.getInstance().getSkinManager().getInsecureSkin(profile);
            return skin.texture();
        }
        // Obtener skin por defecto según el UUID
        return DefaultPlayerSkin.get(entity.getPlayerUUID() != null ? entity.getPlayerUUID() : java.util.UUID.randomUUID()).texture();
    }
}