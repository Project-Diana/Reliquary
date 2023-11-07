package xreliquary.items;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.ImmutableMap;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lib.enderwizards.sandstone.init.ContentInit;
import lib.enderwizards.sandstone.items.ItemToggleable;
import lib.enderwizards.sandstone.util.ContentHelper;
import lib.enderwizards.sandstone.util.InventoryHelper;
import lib.enderwizards.sandstone.util.LanguageHelper;
import lib.enderwizards.sandstone.util.NBTHelper;
import xreliquary.Reliquary;
import xreliquary.lib.Names;

@ContentInit
public class ItemMidasTouchstone extends ItemToggleable {

    public ItemMidasTouchstone() {
        super(Names.midas_touchstone);
        this.setCreativeTab(Reliquary.CREATIVE_TAB);
        this.setMaxStackSize(1);
        canRepair = false;
    }

    @Override
    public void addInformation(ItemStack ist, EntityPlayer player, List list, boolean par4) {
        if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) return;
        this.formatTooltip(
            ImmutableMap.of("charge", Integer.toString(NBTHelper.getInteger("glowstone", ist))),
            ist,
            list);
        if (this.isEnabled(ist)) LanguageHelper.formatTooltip(
            "tooltip.absorb_active",
            ImmutableMap.of(
                "item",
                EnumChatFormatting.YELLOW
                    + Items.glowstone_dust.getItemStackDisplayName(new ItemStack(Items.glowstone_dust))),
            ist,
            list);
        LanguageHelper.formatTooltip("tooltip.absorb", null, ist, list);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.epic;
    }

    @Override
    public void onUpdate(ItemStack ist, World world, Entity e, int i, boolean f) {
        if (world.isRemote) return;
        EntityPlayer player;
        if (e instanceof EntityPlayer) {
            player = (EntityPlayer) e;
        } else return;

        // don't drain glowstone if it isn't activated.
        if (this.isEnabled(ist)) {
            if (NBTHelper.getInteger("glowstone", ist) + getGlowStoneWorth() <= getGlowstoneLimit()) {
                if (InventoryHelper.consumeItem(new ItemStack(Items.glowstone_dust), player)) {
                    NBTHelper
                        .setInteger("glowstone", ist, NBTHelper.getInteger("glowstone", ist) + getGlowStoneWorth());
                }
            }
        }

        if (getCooldown(ist) == 0) {
            doRepairAndDamageTouchstone(ist, player);
        } else {
            decrementCooldown(ist);
        }
    }

    private void decrementCooldown(ItemStack ist) {
        NBTHelper.setShort("cooldown", ist, NBTHelper.getShort("cooldown", ist) - 1);
    }

    private int getCooldown(ItemStack ist) {
        return NBTHelper.getShort("cooldown", ist);
    }

    private void doRepairAndDamageTouchstone(ItemStack ist, EntityPlayer player) {
        // list of customizable items added through configs that can be repaired by the touchstone.
        List<String> goldItems = (List<String>) Reliquary.CONFIG.get(Names.midas_touchstone, "gold_items");

        for (int slot = 0; slot < player.inventory.armorInventory.length; slot++) {
            if (player.inventory.armorInventory[slot] == null) {
                continue;
            }
            if (!(player.inventory.armorInventory[slot].getItem() instanceof ItemArmor)) {
                continue;
            }
            ItemArmor armor = (ItemArmor) player.inventory.armorInventory[slot].getItem();
            if (armor.getArmorMaterial() != ItemArmor.ArmorMaterial.GOLD
                && !goldItems.contains(ContentHelper.getIdent(armor))) {
                continue;
            }
            if (player.inventory.armorInventory[slot].getItemDamage() <= 0) {
                continue;
            }
            if (decrementTouchStoneCharge(ist)) {
                player.inventory.armorInventory[slot]
                    .setItemDamage(player.inventory.armorInventory[slot].getItemDamage() - 1);
            }
        }
        for (int slot = 0; slot < player.inventory.mainInventory.length; slot++) {
            if (player.inventory.mainInventory[slot] == null) {
                continue;
            }
            if (player.inventory.mainInventory[slot].getItem() instanceof ItemSword) {
                ItemSword sword = (ItemSword) player.inventory.mainInventory[slot].getItem();
                if (sword.getToolMaterialName() != ItemSword.ToolMaterial.GOLD.name()
                    && !goldItems.contains(ContentHelper.getIdent(sword))) {
                    continue;
                }
                if (player.inventory.mainInventory[slot].getItemDamage() <= 0) {
                    continue;
                }
                if (decrementTouchStoneCharge(ist)) {
                    player.inventory.mainInventory[slot]
                        .setItemDamage(player.inventory.mainInventory[slot].getItemDamage() - 1);
                }
            } else if (player.inventory.mainInventory[slot].getItem() instanceof ItemTool) {
                ItemTool tool = (ItemTool) player.inventory.mainInventory[slot].getItem();
                if (tool.getToolMaterialName() != ItemSword.ToolMaterial.GOLD.name()
                    && !goldItems.contains(ContentHelper.getIdent(tool))) {
                    continue;
                }
                if (player.inventory.mainInventory[slot].getItemDamage() <= 0) {
                    continue;
                }
                if (decrementTouchStoneCharge(ist)) {
                    player.inventory.mainInventory[slot]
                        .setItemDamage(player.inventory.mainInventory[slot].getItemDamage() - 1);
                }
            } else {
                Item item = player.inventory.mainInventory[slot].getItem();
                if (!goldItems.contains(ContentHelper.getIdent(item))) {
                    continue;
                }
                if (player.inventory.mainInventory[slot].getItemDamage() <= 0 || !item.isDamageable()) {
                    continue;
                }
                if (decrementTouchStoneCharge(ist)) {
                    player.inventory.mainInventory[slot]
                        .setItemDamage(player.inventory.mainInventory[slot].getItemDamage() - 1);
                }
            }
        }
        setCooldown(ist);
    }

    private void setCooldown(ItemStack ist) {
        NBTHelper.setShort("cooldown", ist, 4);
    }

    private boolean decrementTouchStoneCharge(ItemStack ist) {
        if (NBTHelper.getInteger("glowstone", ist) - getGlowStoneCost() >= 0) {
            NBTHelper.setInteger("glowstone", ist, NBTHelper.getInteger("glowstone", ist) - getGlowStoneCost());
            return true;
        }
        return false;
    }

    private int getGlowStoneCost() {
        return Reliquary.CONFIG.getInt(Names.midas_touchstone, "glowstone_cost");
    }

    private int getGlowStoneWorth() {
        return Reliquary.CONFIG.getInt(Names.midas_touchstone, "glowstone_worth");
    }

    private int getGlowstoneLimit() {
        return Reliquary.CONFIG.getInt(Names.midas_touchstone, "glowstone_limit");
    }
}
