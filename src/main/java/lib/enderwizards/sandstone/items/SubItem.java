package lib.enderwizards.sandstone.items;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lib.enderwizards.sandstone.mod.ModRegistry;
import lib.enderwizards.sandstone.util.LanguageHelper;

public class SubItem {

    private final String langName;
    protected IIcon itemIcon;
    protected ItemMultiple parent;

    public SubItem(String langName) {
        this.langName = langName;
    }

    public boolean setParent(ItemMultiple parent) {
        if (this.parent != null) return false;
        this.parent = parent;
        return true;
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list) {
        this.parent.formatTooltip(null, stack, list);
    }

    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack stack) {
        return LanguageHelper.getLocalization(getUnlocalizedName() + ".name");
    }

    @SideOnly(Side.CLIENT)
    public String getUnlocalizedName() {
        return "item." + langName;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon() {
        return itemIcon;
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        itemIcon = iconRegister.registerIcon(
            ModRegistry.getID(
                this.parent.getClass()
                    .getCanonicalName())
                + ":"
                + this.getUnlocalizedName()
                    .substring(5));
    }

}
