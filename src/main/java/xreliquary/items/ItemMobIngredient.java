package xreliquary.items;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lib.enderwizards.sandstone.init.ContentInit;
import lib.enderwizards.sandstone.items.ItemBase;
import xreliquary.Reliquary;
import xreliquary.lib.Names;
import xreliquary.lib.Reference;

/**
 * Created by Xeno on 10/11/2014.
 * This stores all the "ingredient" items used by the mod that don't actually do anything.
 * Right now I've dumped them all into meta and used the shared name "mob_ingredient" even though
 * they're not all technically mob drops. It was mostly just a way for me to clean up our items folder,
 * and make the creative tabs better organized.
 */
@ContentInit
public class ItemMobIngredient extends ItemBase {

    private IIcon icons[];

    public ItemMobIngredient() {
        super(Names.mob_ingredient);
        this.setCreativeTab(Reliquary.CREATIVE_TAB);
        this.setMaxDamage(0);
        this.setMaxStackSize(64);
        canRepair = false;
        this.setHasSubtypes(true);
    }

    @Override
    public String getUnlocalizedName(ItemStack ist) {
        return "item.mob_ingredient_" + ist.getItemDamage();
    }

    @Override
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
        for (int i = 0; i <= 15; i++) par3List.add(new ItemStack(par1, 1, i));
    }

    @Override
    public IIcon getIconFromDamage(int meta) {
        if (meta < 16) return icons[meta];
        return icons[0];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        icons = new IIcon[16];
        for (int i = 0; i <= 15; i++)
            icons[i] = iconRegister.registerIcon(Reference.MOD_ID.toLowerCase() + ":" + Names.mob_ingredient + "_" + i);
    }
}
