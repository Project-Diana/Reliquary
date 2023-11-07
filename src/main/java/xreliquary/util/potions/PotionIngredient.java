package xreliquary.util.potions;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;

public class PotionIngredient {

    public String itemName;
    public List<PotionEffect> effects = new ArrayList<PotionEffect>();

    /*
     * current standard for potion crafting, an item must be supplied with a weighted potion effect so its weight can be
     * calculated.
     */

    // default constructor, used by Potion Essence, because it extends this class.
    public PotionIngredient() {}

    public PotionIngredient(Item item, int meta) {
        this(new ItemStack(item, 1, meta));
    }

    public PotionIngredient(ItemStack ist) {
        this.itemName = ist.getItem()
            .getUnlocalizedNameInefficiently(ist);
    }

    public PotionIngredient addEffect(int id, int durationWeight, int ampWeight) {
        return this.addEffect(new WeightedPotionEffect(id, durationWeight, ampWeight));
    }

    public PotionIngredient addEffect(WeightedPotionEffect effect) {
        effects.add(effect);
        return this;
    }

    public List<PotionEffect> getEffects() {
        return this.effects;
    }
}
