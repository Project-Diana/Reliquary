package xreliquary.items;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import lib.enderwizards.sandstone.init.ContentInit;
import lib.enderwizards.sandstone.items.ItemBase;
import lib.enderwizards.sandstone.util.ContentHelper;
import lib.enderwizards.sandstone.util.InventoryHelper;
import lib.enderwizards.sandstone.util.NBTHelper;
import xreliquary.Reliquary;
import xreliquary.lib.Names;

@ContentInit
public class ItemVoidTearEmpty extends ItemBase {

    public ItemVoidTearEmpty() {
        super(Names.void_tear_empty);
        this.setCreativeTab(Reliquary.CREATIVE_TAB);
        this.setMaxStackSize(16);
        canRepair = false;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack ist, World world, EntityPlayer player) {
        if (!world.isRemote) {
            ItemStack createdTear = buildTear(ist, player, player.inventory, true);
            if (createdTear != null) {
                --ist.stackSize;
                player.worldObj.playSoundAtEntity(
                    player,
                    "random.orb",
                    0.1F,
                    0.5F * ((player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.7F + 1.2F));
                if (ist.stackSize == 0) return createdTear;
                else addItemToInventory(player, createdTear);
            }
        }
        return ist;
    }

    @Override
    public boolean onItemUseFirst(ItemStack ist, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {

            if (world.getTileEntity(x, y, z) instanceof IInventory) {
                IInventory inventory = (IInventory) world.getTileEntity(x, y, z);

                ItemStack createdTear = buildTear(ist, player, inventory, false);
                if (createdTear != null) {
                    --ist.stackSize;
                    player.worldObj.playSoundAtEntity(
                        player,
                        "random.orb",
                        0.1F,
                        0.5F * ((player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.7F + 1.2F));
                    if (ist.stackSize == 0)
                        player.inventory.setInventorySlotContents(player.inventory.currentItem, createdTear);
                    else addItemToInventory(player, createdTear);
                    return true;
                }
            }
        }
        return false;
    }

    protected void addItemToInventory(EntityPlayer player, ItemStack ist) {
        for (int i = 0; i < player.inventory.mainInventory.length; ++i) {
            if (player.inventory.getStackInSlot(i) == null) {
                player.inventory.setInventorySlotContents(i, ist);
                return;
            }
        }
        player.worldObj.spawnEntityInWorld(new EntityItem(player.worldObj, player.posX, player.posY, player.posZ, ist));
    }

    protected ItemStack buildTear(ItemStack ist, EntityPlayer player, IInventory inventory, boolean isPlayerInventory) {
        ItemStack target = InventoryHelper.getTargetItem(ist, inventory, false);
        if (target == null) return null;
        ItemStack filledTear = new ItemStack(Reliquary.CONTENT.getItem(Names.void_tear), 1, 0);

        NBTHelper.setString("itemID", filledTear, ContentHelper.getIdent(target.getItem()));
        NBTHelper.setShort("itemMeta", filledTear, (short) target.getItemDamage());

        int quantity = InventoryHelper.getItemQuantity(target, inventory);
        if (isPlayerInventory) {
            InventoryHelper.consumeItem(target, player, target.getMaxStackSize(), quantity - target.getMaxStackSize());
            quantity = quantity - target.getMaxStackSize();
        } else {
            InventoryHelper.removeItem(target, inventory, quantity);
        }
        NBTHelper.setInteger("itemQuantity", filledTear, quantity);
        // configurable auto-drain when created.
        NBTHelper.setBoolean("enabled", filledTear, Reliquary.CONFIG.getBool(Names.void_tear, "absorb_when_created"));

        return filledTear;
    }
}
