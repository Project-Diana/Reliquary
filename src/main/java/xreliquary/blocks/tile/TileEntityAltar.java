package xreliquary.blocks.tile;

import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;

import lib.enderwizards.sandstone.blocks.tile.TileEntityBase;
import xreliquary.Reliquary;
import xreliquary.blocks.BlockAlkahestryAltar;
import xreliquary.lib.Names;

public class TileEntityAltar extends TileEntityBase {

    private int cycleTime;
    private boolean isActive;
    private int redstoneCount;

    public TileEntityAltar() {
        cycleTime = 0;
        redstoneCount = 0;
        isActive = false;
    }

    @Override
    public void updateEntity() {
        if (!isActive) return;
        int worldTime = (int) (worldObj.getWorldTime() % 24000);
        if (worldTime >= 12000) return;
        if (!worldObj.canBlockSeeTheSky(xCoord, yCoord + 1, zCoord)) return;
        if (worldObj.isRemote) return;
        if (cycleTime > 0) {
            cycleTime--;
        } else {
            isActive = false;
            worldObj.setBlock(xCoord, yCoord + 1, zCoord, Blocks.glowstone);
            BlockAlkahestryAltar.updateAltarBlockState(isActive(), worldObj, xCoord, yCoord, zCoord);
        }
    }

    public void startCycle() {
        // grabs the cycle time from the configs
        int defaultCycleTime = Reliquary.CONFIG.getInt(Names.altar, "time_in_minutes") * 60 * 20;
        int maximumVariance = Reliquary.CONFIG.getInt(Names.altar, "maximum_time_variance_in_minutes") * 60 * 20;
        cycleTime = (int) (defaultCycleTime + (double) maximumVariance * worldObj.rand.nextGaussian());
        isActive = true;
        redstoneCount = 0;
        BlockAlkahestryAltar.updateAltarBlockState(isActive(), worldObj, xCoord, yCoord, zCoord);
    }

    @Override
    public void readFromNBT(NBTTagCompound par1NBTTagCompound) {
        super.readFromNBT(par1NBTTagCompound);
        cycleTime = par1NBTTagCompound.getShort("cycleTime");
        redstoneCount = par1NBTTagCompound.getShort("redstoneCount");
        isActive = par1NBTTagCompound.getBoolean("isActive");
    }

    @Override
    public void writeToNBT(NBTTagCompound par1NBTTagCompound) {
        super.writeToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setShort("cycleTime", (short) cycleTime);
        par1NBTTagCompound.setShort("redstoneCount", (short) redstoneCount);
        par1NBTTagCompound.setBoolean("isActive", isActive);
    }

    public void addRedstone() {
        redstoneCount++;
        if (redstoneCount >= getRedstoneCost()) {
            this.startCycle();
        }
    }

    public static int getRedstoneCost() {
        return Reliquary.CONFIG.getInt(Names.altar, "redstone_cost");
    }

    public int getRedstoneCount() {
        return redstoneCount;
    }

    public boolean isActive() {
        return isActive;
    }
}
