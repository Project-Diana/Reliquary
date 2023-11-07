package xreliquary.items;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lib.enderwizards.sandstone.init.ContentInit;
import lib.enderwizards.sandstone.items.ItemToggleable;
import lib.enderwizards.sandstone.util.ContentHelper;
import xreliquary.Reliquary;
import xreliquary.lib.Names;
import xreliquary.lib.Reference;

@ContentInit
public class ItemEmperorChalice extends ItemToggleable {

    public ItemEmperorChalice() {
        super(Names.emperor_chalice);
        this.setCreativeTab(Reliquary.CREATIVE_TAB);
        this.setMaxDamage(0);
        this.setMaxStackSize(1);
        canRepair = false;
    }

    @SideOnly(Side.CLIENT)
    private IIcon iconOverlay;

    @Override
    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        super.registerIcons(iconRegister);
        iconOverlay = iconRegister.registerIcon(Reference.MOD_ID.toLowerCase() + ":" + Names.emperor_chalice_overlay);
    }

    @Override
    public IIcon getIcon(ItemStack itemStack, int renderPass) {
        // same as infernal, enabled == drink mode.
        if (this.isEnabled(itemStack) || renderPass != 1) return this.itemIcon;
        else return iconOverlay;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack par1ItemStack) {
        return 16;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack par1ItemStack) {
        return EnumAction.drink;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.epic;
    }

    @Override
    public ItemStack onEaten(ItemStack ist, World world, EntityPlayer player) {
        if (world.isRemote) return ist;

        int multiplier = (Integer) Reliquary.CONFIG.get(Names.emperor_chalice, "hunger_satiation_multiplier");
        player.getFoodStats()
            .addStats(1, (float) (multiplier / 2));
        player.attackEntityFrom(DamageSource.drown, multiplier);
        return ist;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack ist, World world, EntityPlayer player) {
        if (player.isSneaking()) return super.onItemRightClick(ist, world, player);
        float coeff = 1.0F;
        double xOff = player.prevPosX + (player.posX - player.prevPosX) * coeff;
        double yOff = player.prevPosY + (player.posY - player.prevPosY) * coeff + 1.62D - player.yOffset;
        double zOff = player.prevPosZ + (player.posZ - player.prevPosZ) * coeff;
        boolean isInDrainMode = this.isEnabled(ist);
        MovingObjectPosition mop = this.getMovingObjectPositionFromPlayer(world, player, isInDrainMode);

        if (mop == null) {
            if (!this.isEnabled(ist)) {
                player.setItemInUse(ist, this.getMaxItemUseDuration(ist));
            }
            return ist;
        } else {

            if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                int x = mop.blockX;
                int y = mop.blockY;
                int z = mop.blockZ;

                if (!world.canMineBlock(player, x, y, z)) return ist;

                if (!player.canPlayerEdit(x, y, z, mop.sideHit, ist)) return ist;

                if (this.isEnabled(ist)) {
                    TileEntity tile = world.getTileEntity(x, y, z);
                    if (tile instanceof IFluidHandler) {
                        // it's got infinite water.. it just drains water, nothing more.
                        FluidStack fluid = new FluidStack(FluidRegistry.WATER, 1000);
                        ((IFluidHandler) tile).drain(ForgeDirection.getOrientation(mop.sideHit), fluid, true);

                        return ist;
                    }
                } else {
                    TileEntity tile = world.getTileEntity(x, y, z);
                    if (tile instanceof IFluidHandler) {
                        FluidStack fluid = new FluidStack(FluidRegistry.WATER, 1000);
                        int amount = ((IFluidHandler) tile)
                            .fill(ForgeDirection.getOrientation(mop.sideHit), fluid, false);

                        if (amount > 0) {
                            ((IFluidHandler) tile).fill(ForgeDirection.getOrientation(mop.sideHit), fluid, true);
                        }

                        return ist;
                    }
                }

                if (!this.isEnabled(ist)) {
                    if (mop.sideHit == 0) {
                        --y;
                    }

                    if (mop.sideHit == 1) {
                        ++y;
                    }

                    if (mop.sideHit == 2) {
                        --z;
                    }

                    if (mop.sideHit == 3) {
                        ++z;
                    }

                    if (mop.sideHit == 4) {
                        --x;
                    }

                    if (mop.sideHit == 5) {
                        ++x;
                    }

                    if (!player.canPlayerEdit(x, y, z, mop.sideHit, ist)) return ist;

                    if (this.tryPlaceContainedLiquid(world, ist, xOff, yOff, zOff, x, y, z)) return ist;

                } else {
                    String ident = ContentHelper.getIdent(world.getBlock(x, y, z));
                    if ((ident.equals(ContentHelper.getIdent(Blocks.flowing_water))
                        || ident.equals(ContentHelper.getIdent(Blocks.water)))
                        && world.getBlockMetadata(x, y, z) == 0) {
                        world.setBlock(x, y, z, Blocks.air);

                        return ist;
                    }
                }
            }

            return ist;
        }
    }

    public boolean tryPlaceContainedLiquid(World world, ItemStack ist, double posX, double posY, double posZ, int x,
        int y, int z) {
        Material material = world.getBlock(x, y, z)
            .getMaterial();
        if (this.isEnabled(ist)) return false;
        boolean isNotSolid = !material.isSolid();
        if (!world.isAirBlock(x, y, z) && !isNotSolid) return false;
        else {
            if (world.provider.isHellWorld) {
                world.playSoundEffect(
                    posX + 0.5D,
                    posY + 0.5D,
                    posZ + 0.5D,
                    "random.fizz",
                    0.5F,
                    2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

                for (int var11 = 0; var11 < 8; ++var11) {
                    world.spawnParticle(
                        "largesmoke",
                        x + Math.random(),
                        y + Math.random(),
                        z + Math.random(),
                        0.0D,
                        0.0D,
                        0.0D);
                }
            } else {
                world.setBlock(x, y, z, Blocks.flowing_water, 0, 3);
            }

            return true;
        }
    }
}
