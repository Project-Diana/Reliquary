package xreliquary.items;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import xreliquary.items.util.ILeftClickableItem;
import xreliquary.reference.Settings;
import xreliquary.util.InventoryHelper;
import xreliquary.util.LanguageHelper;
import xreliquary.util.NBTHelper;
import xreliquary.util.NoPlayerBlockItemUseContext;
import xreliquary.util.RegistryHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.StringJoiner;

public class SojournerStaffItem extends ToggleableItem implements ILeftClickableItem {
	private static final int COOLDOWN = 10;

	private static final String ITEMS_TAG = "Items";
	private static final String QUANTITY_TAG = "Quantity";
	private static final String CURRENT_INDEX_TAG = "Current";

	public SojournerStaffItem() {
		super(new Properties().maxStackSize(1));
	}

	@Override
	public Rarity getRarity(ItemStack stack) {
		return Rarity.EPIC;
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
		if (world.isRemote || world.getGameTime() % COOLDOWN != 0) {
			return;
		}

		PlayerEntity player = null;
		if (entity instanceof PlayerEntity) {
			player = (PlayerEntity) entity;
		}
		if (player == null) {
			return;
		}

		if (isEnabled(stack)) {
			scanForMatchingTorchesToFillInternalStorage(stack, player);
		}
	}

	@Override
	public ActionResultType onLeftClickItem(ItemStack stack, LivingEntity entityLiving) {
		if (!entityLiving.isSneaking()) {
			return ActionResultType.CONSUME;
		}
		if (entityLiving.world.isRemote) {
			return ActionResultType.PASS;
		}
		cycleTorchMode(stack);
		return ActionResultType.SUCCESS;
	}

	private void scanForMatchingTorchesToFillInternalStorage(ItemStack stack, PlayerEntity player) {
		for (String torch : Settings.COMMON.items.sojournerStaff.torches.get()) {
			consumeAndCharge(player, Settings.COMMON.items.sojournerStaff.maxCapacityPerItemType.get() - getInternalStorageItemCount(stack, torch), 1, ist -> RegistryHelper.getItemRegistryName(ist.getItem()).equals(torch), 16,
					chargeToAdd -> addItemToInternalStorage(stack, torch, chargeToAdd));
		}
	}

	public ItemStack getCurrentTorch(ItemStack stack) {
		return getItem(getCurrentTorchTag(stack));
	}

	public int getTorchCount(ItemStack stack) {
		return getCurrentTorchTag(stack).getInt(QUANTITY_TAG);
	}

	private CompoundNBT getCurrentTorchTag(ItemStack stack) {
		CompoundNBT tagCompound = NBTHelper.getTag(stack);

		ListNBT tagList = getItemListTag(tagCompound);
		int current = getCurrentIndex(tagCompound, tagList);

		return tagList.getCompound(current);
	}

	private ListNBT getItemListTag(CompoundNBT tagCompound) {
		return tagCompound.getList(ITEMS_TAG, 10);
	}

	private void cycleTorchMode(ItemStack stack) {
		ItemStack currentTorch = getCurrentTorch(stack);
		if (currentTorch.isEmpty()) {
			return;
		}
		CompoundNBT tagCompound = NBTHelper.getTag(stack);
		ListNBT tagList = getItemListTag(tagCompound);
		if (tagList.size() == 1) {
			return;
		}

		int current = getCurrentIndex(tagCompound, tagList);

		for (int i = current + 1; i < tagList.size(); i++) {
			CompoundNBT tagItemData = tagList.getCompound(i);
			int quantity = tagItemData.getInt(QUANTITY_TAG);
			if (quantity > 0) {
				tagCompound.putInt(CURRENT_INDEX_TAG, i);
				return;
			}
		}
		for (int i = 0; i <= current; i++) {
			CompoundNBT tagItemData = tagList.getCompound(i);
			int quantity = tagItemData.getInt(QUANTITY_TAG);
			if (quantity > 0) {
				tagCompound.putInt(CURRENT_INDEX_TAG, i);
				return;
			}
		}
	}

	private int getCurrentIndex(CompoundNBT tagCompound, ListNBT tagList) {
		int current = tagCompound.getInt(CURRENT_INDEX_TAG);
		if (tagList.size() <= current) {
			tagCompound.putInt(CURRENT_INDEX_TAG, 0);
		}
		return current;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected void addMoreInformation(ItemStack staff, @Nullable World world, List<ITextComponent> tooltip) {
		StringJoiner joiner = new StringJoiner(";");
		iterateItems(staff, tag -> {
			ItemStack containedItem = getItem(tag);
			int quantity = tag.getInt(QUANTITY_TAG);
			joiner.add(containedItem.getDisplayName().getString() + ": " + quantity);
		}, () -> false);

		LanguageHelper.formatTooltip(getTranslationKey() + ".tooltip2", ImmutableMap.of("phrase", joiner.toString(), "placing", getCurrentTorch(staff).getDisplayName().getString()), tooltip);

		if (isEnabled(staff)) {
			LanguageHelper.formatTooltip("tooltip.absorb_active", ImmutableMap.of("item", TextFormatting.YELLOW + (new ItemStack(Blocks.TORCH).getDisplayName().getString())), tooltip);
		}
		LanguageHelper.formatTooltip("tooltip.absorb", tooltip);
	}

	@Override
	protected boolean hasMoreInformation(ItemStack stack) {
		return true;
	}

	private static ItemStack getItem(CompoundNBT tagItemData) {
		return new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(tagItemData.getString(ITEM_NAME_TAG))));
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		return placeTorch(context);
	}

	private ActionResultType placeTorch(ItemUseContext context) {
		PlayerEntity player = context.getPlayer();
		Hand hand = context.getHand();
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		Direction face = context.getFace();
		ItemStack stack = context.getItem();

		BlockPos placeBlockAt = pos.offset(face);

		if (world.isRemote) {
			return ActionResultType.SUCCESS;
		}
		ItemStack torch = getCurrentTorch(stack);
		if (player == null || torch.isEmpty() || !(torch.getItem() instanceof BlockItem)) {
			return ActionResultType.FAIL;
		}
		if (!player.canPlayerEdit(placeBlockAt, face, stack) || player.isCrouching()) {
			return ActionResultType.PASS;
		}
		player.swingArm(hand);

		Block blockToPlace = ((BlockItem) torch.getItem()).getBlock();
		NoPlayerBlockItemUseContext placeContext = new NoPlayerBlockItemUseContext(world, placeBlockAt, new ItemStack(blockToPlace), face);
		if (!placeContext.canPlace() || !removeTorches(player, stack, blockToPlace, placeBlockAt)) {
			return ActionResultType.FAIL;
		}
		((BlockItem) torch.getItem()).tryPlace(placeContext);
		double gauss = 0.5D + world.rand.nextFloat() / 2;
		world.addParticle(ParticleTypes.ENTITY_EFFECT, placeBlockAt.getX() + 0.5D, placeBlockAt.getY() + 0.5D, placeBlockAt.getZ() + 0.5D, gauss, gauss, 0.0F);
		return ActionResultType.SUCCESS;
	}

	private boolean removeTorches(PlayerEntity player, ItemStack stack, Block blockToPlace, BlockPos placeBlockAt) {
		if (!player.isCreative()) {
			int distance = (int) player.getEyePosition(1).distanceTo(new Vector3d(placeBlockAt.getX(), placeBlockAt.getY(), placeBlockAt.getZ()));
			int cost = 1 + distance / Settings.COMMON.items.sojournerStaff.tilePerCostMultiplier.get();

			return removeItemFromInternalStorage(stack, blockToPlace, cost, false, player);
		}
		return true;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
		if (!player.isSneaking()) {
			RayTraceResult rayTraceResult = longRayTrace(world, player);
			if (rayTraceResult.getType() == RayTraceResult.Type.BLOCK) {
				placeTorch(new ItemUseContext(player, hand, (BlockRayTraceResult) rayTraceResult));
			} else {
				ItemStack staff = player.getHeldItem(hand);
				CompoundNBT torchTag = getCurrentTorchTag(staff);
				ItemStack torch = getItem(torchTag);
				int count = torchTag.getInt(QUANTITY_TAG);
				torch.setCount(Math.min(count, torch.getMaxStackSize()));
				player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP).ifPresent(playerInventory -> {
					int inserted = InventoryHelper.insertIntoInventory(torch, playerInventory);
					removeItemFromInternalStorage(staff, torch.getItem(), inserted, false, player);
				});
			}
		}
		return super.onItemRightClick(world, player, hand);
	}

	private RayTraceResult longRayTrace(World worldIn, PlayerEntity player) {
		float f = player.rotationPitch;
		float f1 = player.rotationYaw;
		Vector3d vec3d = player.getEyePosition(1.0F);
		float f2 = MathHelper.cos(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
		float f3 = MathHelper.sin(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
		float f4 = -MathHelper.cos(-f * ((float) Math.PI / 180F));
		float f5 = MathHelper.sin(-f * ((float) Math.PI / 180F));
		float f6 = f3 * f4;
		float f7 = f2 * f4;
		double d0 = Settings.COMMON.items.sojournerStaff.maxRange.get();
		Vector3d vec3d1 = vec3d.add((double) f6 * d0, (double) f5 * d0, (double) f7 * d0);
		return worldIn.rayTraceBlocks(new RayTraceContext(vec3d, vec3d1, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.ANY, player));
	}
}
