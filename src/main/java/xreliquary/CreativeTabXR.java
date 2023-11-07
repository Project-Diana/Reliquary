package xreliquary;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import xreliquary.lib.Names;

public class CreativeTabXR extends CreativeTabs {

    public CreativeTabXR(int ID, String langName) {
        super(ID, langName);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Item getTabIconItem() {
        return Reliquary.CONTENT.getItem(Names.mercy_cross);
    }

}
