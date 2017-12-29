package com.lothrazar.cyclicmagic.block.base;
import javax.annotation.Nullable;
import com.google.common.base.Function;
import com.lothrazar.cyclicmagic.data.Const;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.TRSRTransformation;

public abstract class BaseTESR<T extends TileEntity> extends TileEntitySpecialRenderer<T> {
  protected IModel model;
  protected IBakedModel bakedModel;
  protected String resource = null;
  public BaseTESR(@Nullable Block block) {
    if (block != null)
      resource = "tesr/" + block.getUnlocalizedName().replace("tile.", "").replace(".name", "");
  }
  protected IBakedModel getBakedModel() {
    // Since we cannot bake in preInit() we do lazy baking of the model as soon as we need it
    if (bakedModel == null && resource != null) {
      try {
        model = ModelLoaderRegistry.getModel(new ResourceLocation(Const.MODID, resource));
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
      bakedModel = model.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM,
          new Function<ResourceLocation, TextureAtlasSprite>() {
            @Override
            public TextureAtlasSprite apply(ResourceLocation location) {
              return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
            }
          });
    }
    return bakedModel;
  }
  protected void renderItem(TileEntity te, ItemStack stack, float itemHeight) {
    this.renderItem(te, stack, 0.5F, itemHeight, 0.5F);
  }
  protected void renderItem(TileEntity te, ItemStack stack, float x, float itemHeight, float y) {
    this.renderItem(te, stack, x, itemHeight, y, 0, true, 0.4F);
  }
  protected void renderItem(TileEntity te, ItemStack stack, float x, float itemHeight, float y, int initialAngle, boolean isSpinning, float scaleFactor) {
    //    GuiHelper.drawTexturedRect(minecraft, texture, x, y, width, height, zLevel, texPosX, texPosY, texWidth, texHeight);
    if (stack == null || stack.isEmpty()) {
      return;
    }
    GlStateManager.pushMatrix();
    //start of rotate
    if (initialAngle > 0) {
      GlStateManager.translate(.5, 0, .5);
      GlStateManager.rotate(initialAngle, 0, 1, 0);
      GlStateManager.translate(-.5, 0, -.5);
    }
    if (isSpinning) {
      GlStateManager.translate(.5, 0, .5);
      long angle = (System.currentTimeMillis() / 10) % 360;
      GlStateManager.rotate(angle, 0, 1, 0);
      GlStateManager.translate(-.5, 0, -.5);
    }
    //end of rotate
    GlStateManager.translate(x, itemHeight, y);//move to xy center and up to top level
    GlStateManager.scale(scaleFactor, scaleFactor, scaleFactor);//shrink down
    // Thank you for helping me understand lighting @storagedrawers  https://github.com/jaquadro/StorageDrawers/blob/40737fb2254d68020a30f80977c84fd50a9b0f26/src/com/jaquadro/minecraft/storagedrawers/client/renderer/TileEntityDrawersRenderer.java#L96
    //start of 'fix lighting' 
    int ambLight = getWorld().getCombinedLight(te.getPos().offset(EnumFacing.UP), 0);
    if (ambLight == 0) {
      ambLight = 15728656;//if there is a block above blocking light, dont make it dark
    }
    int lu = ambLight % 65536;
    int lv = ambLight / 65536;
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) lu / 1.0F, (float) lv / 1.0F);
    //end of 'fix lighting'
    Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.NONE);
    GlStateManager.popMatrix();
  }
  /**
   * TextColor similar to int textColor = 0xFF0055;
   * 
   * @param s
   * @param x
   * @param y
   * @param z
   * @param destroyStage
   * @param xt
   * @param yt
   * @param zt
   * @param angle
   * @param textColor
   */
  public void renderTextAt(String s, double x, double y, double z, int destroyStage, float xt, float yt, float zt, float angle, int textColor) {
    GlStateManager.pushMatrix();
    GlStateManager.translate((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
    if (angle != 0) {
      GlStateManager.rotate(angle, 0, 1, 0);
    }
    //initial setup
    float scaleTo = 0.6666667F;
    GlStateManager.enableRescaleNormal();
    GlStateManager.pushMatrix();
    GlStateManager.scale(scaleTo, -1 * scaleTo, -1 * scaleTo);
    GlStateManager.popMatrix();
    FontRenderer fontrenderer = this.getFontRenderer();
    GlStateManager.translate(-2.0F, 1.33333334F, 0.046666667F);
    //below sets position
    GlStateManager.translate(xt, yt, zt);
    //sake makes it the right size do not touch
    float f3 = 0.010416667F;
    GlStateManager.scale(0.010416667F, -0.010416667F, 0.010416667F);
    GlStateManager.glNormal3f(0.0F, 0.0F, -0.010416667F);//no idea what this does
    GlStateManager.depthMask(false);
    fontrenderer.drawString(s, 0, 0, textColor);
    GlStateManager.depthMask(true);
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    GlStateManager.popMatrix();
    if (destroyStage >= 0) {
      GlStateManager.matrixMode(5890);
      GlStateManager.popMatrix();
      GlStateManager.matrixMode(5888);
    }
  }
}
