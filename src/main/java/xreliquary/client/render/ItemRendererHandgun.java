package xreliquary.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import xreliquary.client.model.ModelHandgun;
import xreliquary.lib.ClientReference;

public class ItemRendererHandgun implements IItemRenderer {

    protected ModelHandgun handgunModel;

    public ItemRendererHandgun() {
        handgunModel = new ModelHandgun();
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        if (type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON) return true;
        return false;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return false;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        if (type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
            GL11.glPushMatrix();

            Minecraft.getMinecraft().renderEngine.bindTexture(ClientReference.HANDGUN_TEXTURE);

            GL11.glRotatef(0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(176F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(145F, 0.0F, 0.0F, 1.0F);

            GL11.glTranslatef(0.8F, .20F, 0.08F);

            float scale = 0.5F;
            GL11.glScalef(scale, scale, scale);

            handgunModel.render((Entity) data[1], 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);

            GL11.glPopMatrix();
        }

    }

}
