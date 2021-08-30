package mrjake.aunis.renderer.stargate;

import mrjake.aunis.Aunis;
import mrjake.aunis.loader.ElementEnum;
import mrjake.aunis.loader.texture.Texture;
import mrjake.aunis.loader.texture.TextureLoader;
import mrjake.aunis.renderer.biomes.BiomeOverlayEnum;
import mrjake.aunis.util.math.MathFunction;
import mrjake.aunis.util.math.MathFunctionImpl;
import mrjake.aunis.util.math.MathRange;
import mrjake.aunis.util.math.NumberUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

public class StargatePegasusRenderer extends StargateClassicRenderer<StargatePegasusRendererState> {

  private static final Vec3d RING_LOC = new Vec3d(0.0, -0.122333, -0.000597);
  private static final float GATE_DIAMETER = 10.1815f;

  private static final int GLYPHS_COUNT = 36;

  @Override
  protected void applyTransformations(StargatePegasusRendererState rendererState) {
    GlStateManager.translate(0.50, GATE_DIAMETER / 2 + rendererState.stargateSize.renderTranslationY, 0.50);
    GlStateManager.scale(rendererState.stargateSize.renderScale, rendererState.stargateSize.renderScale, rendererState.stargateSize.renderScale);
  }

  @Override
  protected void renderGate(StargatePegasusRendererState rendererState, double partialTicks) {
    renderRing(rendererState, partialTicks);
    GlStateManager.rotate(rendererState.horizontalRotation, 0, 1, 0);
    renderChevrons(rendererState, partialTicks);

    if (rendererState.spinHelper.getIsSpinning()) {
      int index = (int) rendererState.spinHelper.apply(getWorld().getTotalWorldTime() + partialTicks);
      if (!rendererState.slotToGlyphMap.containsKey(index)) {
        renderGlyph(rendererState.spinHelper.getTargetSymbol().getId(), index, false);
      }
    }

    for (int i = 0; i < GLYPHS_COUNT; i++) {
      if (!rendererState.slotToGlyphMap.containsKey(i)) {
        // Don't show the faded out glyphs when the gate is dialing.
        if (!rendererState.spinHelper.getIsSpinning()) {
          renderGlyph(i, i, true);
        }
        continue;
      }

      renderGlyph(rendererState.slotToGlyphMap.get(i), i, false);
    }

    ElementEnum.PEGASUS_GATE.bindTextureAndRender(rendererState.getBiomeOverlay());
  }

  // ----------------------------------------------------------------------------------------
  // Ring

  private void renderRing(StargatePegasusRendererState rendererState, double partialTicks) {
    GlStateManager.pushMatrix();

    if (rendererState.horizontalRotation == 90 || rendererState.horizontalRotation == 270) {
      GlStateManager.translate(RING_LOC.y, RING_LOC.z, RING_LOC.x);
      GlStateManager.rotate(0, 1, 0, 0);
      GlStateManager.translate(-RING_LOC.y, -RING_LOC.z, -RING_LOC.x);
    } else {
      GlStateManager.translate(RING_LOC.x, RING_LOC.z, RING_LOC.y);
      GlStateManager.rotate(0, 0, 0, 1);
      GlStateManager.translate(-RING_LOC.x, -RING_LOC.z, -RING_LOC.y);
    }

    GlStateManager.rotate(rendererState.horizontalRotation, 0, 1, 0);

    ElementEnum.PEGASUS_RING.bindTextureAndRender(rendererState.getBiomeOverlay());

    GlStateManager.popMatrix();
    //renderShield(rendererState.horizontalRotation);
    renderShield(partialTicks, false, null, false, 1);
  }


  // ----------------------------------------------------------------------------------------
  // Chevrons

  @Override
  protected void renderChevron(StargatePegasusRendererState rendererState, double partialTicks, ChevronEnum chevron) {
    GlStateManager.pushMatrix();

    GlStateManager.rotate(chevron.rotation, 0, 0, 1);

    TextureLoader.getTexture(rendererState.chevronTextureList.get(rendererState.getBiomeOverlay(), chevron)).bindTexture();

    if (chevron.isFinal()) {
      float chevronOffset = 0;

      GlStateManager.pushMatrix();

      GlStateManager.translate(0, chevronOffset, 0);
      ElementEnum.PEGASUS_CHEVRON_LIGHT.render();

      GlStateManager.translate(0, -2 * chevronOffset, 0);
      ElementEnum.PEGASUS_CHEVRON_MOVING.render();

      GlStateManager.popMatrix();
    } else {
      ElementEnum.PEGASUS_CHEVRON_MOVING.render();
      ElementEnum.PEGASUS_CHEVRON_LIGHT.render();
    }

    ElementEnum.PEGASUS_CHEVRON_FRAME.bindTextureAndRender(rendererState.getBiomeOverlay());
    ElementEnum.PEGASUS_CHEVRON_BACK.render();


    GlStateManager.popMatrix();
  }

  private double[] getPositionInRingAtIndex(double radius, int index) {
    double deg = ((360.0 / GLYPHS_COUNT) * index);
    double rad = Math.toRadians(deg);
    return new double[]{radius * Math.cos(rad), radius * Math.sin(rad), deg};
  }

  protected void renderGlyph(int glyphId, int slot, boolean dim) {
    GlStateManager.pushMatrix();
    GlStateManager.disableBlend();
    GlStateManager.enableAlpha();
    GlStateManager.enableTexture2D();
    GlStateManager.disableLighting();

    double[] slotPos = getPositionInRingAtIndex((GATE_DIAMETER / 2) - 0.85, slot);

    // Round is necessary here, since Minecraft doesn't handle many decimal places very well in this case,
    // so that the texture just ceases to exist.
    GlStateManager.translate(NumberUtils.round(slotPos[0], 3), NumberUtils.round(slotPos[1], 3), 0.205);
    GlStateManager.rotate(90, 1, 0, 0);

    String path = String.format("pegasus/%s.png", dim ? "glyphs_off" : "glyphs");
    Minecraft.getMinecraft().getTextureManager().bindTexture(TextureLoader.getTextureResource(path));

    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder buffer = tessellator.getBuffer();

    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

    // The glyphs in the assets are arranged in a circle, so we extract those glyphs at certain positions.
    double radius = 0.94;
    double[] uv = getPositionInRingAtIndex(radius, -glyphId);
    double x = (uv[0] + radius) / 2;
    double y = (uv[1] + radius) / 2;

    double tileSize = 0.27;
    double uvSize = 0.0625;

    GlStateManager.rotate((360.0f / GLYPHS_COUNT) * (slot - glyphId), 0, 1, 0);

    buffer.pos(-tileSize, 0, -tileSize).tex(x, y).endVertex();
    buffer.pos(-tileSize, 0, tileSize).tex(x, y + uvSize).endVertex();
    buffer.pos(tileSize, 0, tileSize).tex(x + uvSize, y + uvSize).endVertex();
    buffer.pos(tileSize, 0, -tileSize).tex(x + uvSize, y).endVertex();

    tessellator.draw();

    buffer.setTranslation(0, 0, 0);

    GlStateManager.enableLighting();
    GlStateManager.popMatrix();
  }


  /*public void renderShield(float gateRotation) {
    if (gateRotation == 90 || gateRotation == 270) {
      GlStateManager.translate(RING_LOC.y, RING_LOC.z, RING_LOC.x);
      GlStateManager.rotate(0, 2, 0, 0);
      GlStateManager.translate(-RING_LOC.y, -RING_LOC.z, -RING_LOC.x);
    } else {
      GlStateManager.translate(RING_LOC.x, RING_LOC.z, RING_LOC.y);
      GlStateManager.rotate(0, 0, 0, 2);
      GlStateManager.translate(-RING_LOC.x, -RING_LOC.z, -RING_LOC.y);
    }
    GlStateManager.glEnd();

    GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK,GL11.GL_FILL);
    GlStateManager.color(1, 1, 1, 1);
    GlStateManager.enableTexture2D();
    GlStateManager.enableLighting();
    ElementEnum.PEGASUS_SHIELD.bindTextureAndRender(BiomeOverlayEnum.NORMAL);
  }*/

  protected static final ResourceLocation PEGASUS_SHIELD_TEXTURE_LOC =
          new ResourceLocation(Aunis.ModID, "textures/tesr/pegasus/shield/gate_pegasus_shield.jpg");

  protected void renderShield(double partialTicks, boolean white, Float alpha, boolean backOnly, float mul) {

    Texture ehTexture = TextureLoader.getTexture(PEGASUS_SHIELD_TEXTURE_LOC);
    if (ehTexture != null) ehTexture.bindTexture();

    float tick = (float) (getWorld().getTotalWorldTime() + partialTicks);

    GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    GlStateManager.enableBlend();

    for (int k = (backOnly ? 1 : 0); k < 2; k++) {
      if (k == 1) {
        GlStateManager.rotate(180, 0, 1, 0);
      }

      if (alpha == null) alpha = 0.0f;

      if (k == 1) alpha += 0.3f;


      if (white) StargateRendererStatic.innerCircle.render(tick, true, alpha, mul);

      StargateRendererStatic.innerCircle.render(tick, false, 1.0f - alpha, mul);


      for (StargateRendererStatic.QuadStrip strip : StargateRendererStatic.quadStrips) {
        if (white) strip.render(tick, true, alpha, mul);

        strip.render(tick, false, 1.0f - alpha, mul);
      }
    }

    GlStateManager.disableBlend();
  }
}