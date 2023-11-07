package xreliquary.items;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.HashMultimap;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lib.enderwizards.sandstone.init.ContentInit;
import lib.enderwizards.sandstone.items.ItemBase;
import xreliquary.lib.Colors;
import xreliquary.lib.Names;
import xreliquary.util.potions.PotionEssence;

/**
 * Created by Xeno on 11/8/2014.
 */
@ContentInit
public class ItemPotionEssence extends ItemBase {

    public ItemPotionEssence() {
        super(Names.potion_essence);
        this.setMaxStackSize(64);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack ist, int renderPass) {
        if (renderPass == 1) return getColor(ist);
        else return Integer.parseInt(Colors.PURE, 16);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses() {
        return true;
    }

    public int getColor(ItemStack itemStack) {
        // basically we're just using vanillas right now. This is hilarious in comparison to the old method, which is a
        // mile long.
        return PotionHelper.calcPotionLiquidColor(new PotionEssence(itemStack.getTagCompound()).getEffects());
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack ist, EntityPlayer player, List list, boolean flag) {
        if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) return;
        PotionEssence essence = new PotionEssence(ist.getTagCompound());
        if (essence.getEffects()
            .size() > 0) {
            HashMultimap hashmultimap = HashMultimap.create();
            Iterator iterator1;

            if (essence.getEffects() != null && !essence.getEffects()
                .isEmpty()) {
                iterator1 = essence.getEffects()
                    .iterator();

                while (iterator1.hasNext()) {
                    PotionEffect potioneffect = (PotionEffect) iterator1.next();
                    String s1 = StatCollector.translateToLocal(potioneffect.getEffectName())
                        .trim();
                    Potion potion = Potion.potionTypes[potioneffect.getPotionID()];
                    Map map = potion.func_111186_k();

                    if (map != null && map.size() > 0) {
                        Iterator iterator = map.entrySet()
                            .iterator();

                        while (iterator.hasNext()) {
                            Map.Entry entry = (Map.Entry) iterator.next();
                            AttributeModifier attributemodifier = (AttributeModifier) entry.getValue();
                            AttributeModifier attributemodifier1 = new AttributeModifier(
                                attributemodifier.getName(),
                                potion.func_111183_a(potioneffect.getAmplifier(), attributemodifier),
                                attributemodifier.getOperation());
                            hashmultimap
                                .put(((IAttribute) entry.getKey()).getAttributeUnlocalizedName(), attributemodifier1);
                        }
                    }

                    if (potioneffect.getAmplifier() > 0) {
                        s1 = s1 + " " + (potioneffect.getAmplifier() + 1);
                    }

                    if (potioneffect.getDuration() > 20) {
                        s1 = s1 + " (" + Potion.getDurationString(potioneffect) + ")";
                    }

                    if (potion.isBadEffect()) {
                        list.add(EnumChatFormatting.RED + s1);
                    } else {
                        list.add(EnumChatFormatting.GRAY + s1);
                    }
                }
            } else {
                String s = StatCollector.translateToLocal("potion.empty")
                    .trim();
                list.add(EnumChatFormatting.GRAY + s);
            }

            if (!hashmultimap.isEmpty()) {
                list.add("");
                list.add(EnumChatFormatting.DARK_PURPLE + StatCollector.translateToLocal("potion.effects.whenDrank"));
                iterator1 = hashmultimap.entries()
                    .iterator();

                while (iterator1.hasNext()) {
                    Map.Entry entry1 = (Map.Entry) iterator1.next();
                    AttributeModifier attributemodifier2 = (AttributeModifier) entry1.getValue();
                    double d0 = attributemodifier2.getAmount();
                    double d1;

                    if (attributemodifier2.getOperation() != 1 && attributemodifier2.getOperation() != 2) {
                        d1 = attributemodifier2.getAmount();
                    } else {
                        d1 = attributemodifier2.getAmount() * 100.0D;
                    }

                    if (d0 > 0.0D) {
                        list.add(
                            EnumChatFormatting.BLUE + StatCollector.translateToLocalFormatted(
                                "attribute.modifier.plus." + attributemodifier2.getOperation(),
                                new Object[] { ItemStack.field_111284_a.format(d1),
                                    StatCollector.translateToLocal("attribute.name." + (String) entry1.getKey()) }));
                    } else if (d0 < 0.0D) {
                        d1 *= -1.0D;
                        list.add(
                            EnumChatFormatting.RED + StatCollector.translateToLocalFormatted(
                                "attribute.modifier.take." + attributemodifier2.getOperation(),
                                new Object[] { ItemStack.field_111284_a.format(d1),
                                    StatCollector.translateToLocal("attribute.name." + (String) entry1.getKey()) }));
                    }
                }
            }
        }
    }
}
