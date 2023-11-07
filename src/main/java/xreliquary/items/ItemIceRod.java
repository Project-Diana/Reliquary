package xreliquary.items;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.ImmutableMap;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lib.enderwizards.sandstone.init.ContentInit;
import lib.enderwizards.sandstone.items.ItemToggleable;
import lib.enderwizards.sandstone.util.InventoryHelper;
import lib.enderwizards.sandstone.util.LanguageHelper;
import lib.enderwizards.sandstone.util.NBTHelper;
import xreliquary.Reliquary;
import xreliquary.entities.EntitySpecialSnowball;
import xreliquary.lib.Names;

@ContentInit
public class ItemIceRod extends ItemToggleable {

    public ItemIceRod() {
        super(Names.ice_magus_rod);
        this.setCreativeTab(Reliquary.CREATIVE_TAB);
        this.setMaxStackSize(1);
        canRepair = false;
    }

    @Override
    public void addInformation(ItemStack ist, EntityPlayer player, List list, boolean par4) {
        if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) return;
        this.formatTooltip(
            ImmutableMap.of("charge", Integer.toString(NBTHelper.getInteger("snowballs", ist))),
            ist,
            list);
        if (this.isEnabled(ist)) LanguageHelper.formatTooltip(
            "tooltip.absorb_active",
            ImmutableMap.of(
                "item",
                EnumChatFormatting.BLUE + Items.snowball.getItemStackDisplayName(new ItemStack(Items.snowball))),
            ist,
            list);
        LanguageHelper.formatTooltip("tooltip.absorb", null, ist, list);
    }

    public ItemIceRod(String langName) {
        super(langName);
        this.setCreativeTab(Reliquary.CREATIVE_TAB);
        this.setMaxStackSize(1);
        canRepair = false;
    }

    public int getSnowballCap() {
        return Reliquary.CONFIG
            .getInt(this instanceof ItemGlacialStaff ? Names.glacial_staff : Names.ice_magus_rod, "snowball_limit");
    }

    public int getSnowballCost() {
        return Reliquary.CONFIG
            .getInt(this instanceof ItemGlacialStaff ? Names.glacial_staff : Names.ice_magus_rod, "snowball_cost");
    }

    public int getSnowballWorth() {
        return Reliquary.CONFIG
            .getInt(this instanceof ItemGlacialStaff ? Names.glacial_staff : Names.ice_magus_rod, "snowball_worth");
    }

    @Override
    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack ist) {
        return false;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack ist, World world, EntityPlayer player) {
        // acts as a cooldown.
        if (player.isSwingInProgress) return ist;
        player.swingItem();
        if (!player.isSneaking()) {
            if (NBTHelper.getInteger("snowballs", ist) >= getSnowballCost()) {
                world.playSoundAtEntity(player, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
                world.spawnEntityInWorld(new EntitySpecialSnowball(world, player, this instanceof ItemGlacialStaff));
                NBTHelper.setInteger("snowballs", ist, NBTHelper.getInteger("snowballs", ist) - getSnowballCost());
            }
        }
        return super.onItemRightClick(ist, world, player);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.epic;
    }

    @Override
    public void onUpdate(ItemStack ist, World world, Entity e, int i, boolean b) {
        if (world.isRemote) return;
        EntityPlayer player = null;
        if (e instanceof EntityPlayer) {
            player = (EntityPlayer) e;
        }
        if (player == null) return;

        if (this.isEnabled(ist)) {
            if (NBTHelper.getInteger("snowballs", ist) + getSnowballWorth() <= getSnowballCap()) {
                if (InventoryHelper.consumeItem(new ItemStack(Items.snowball), player)) {
                    NBTHelper.setInteger("snowballs", ist, NBTHelper.getInteger("snowballs", ist) + getSnowballWorth());
                }
            }
        }
    }

    @Override
    public boolean isFull3D() {
        return true;
    }
}
