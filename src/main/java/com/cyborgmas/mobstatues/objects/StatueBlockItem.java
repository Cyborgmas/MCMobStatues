package com.cyborgmas.mobstatues.objects;

import com.cyborgmas.mobstatues.MobStatues;
import com.cyborgmas.mobstatues.registration.Registration;
import com.cyborgmas.mobstatues.util.StatueCreationHelper;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item.Properties;

public class StatueBlockItem extends BlockItem {
    public StatueBlockItem(Properties properties) {
        super(Registration.STATUE_BLOCK.get(), properties);
    }

    @Override
    public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {}

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        if (!stack.hasTag() || !stack.getOrCreateTag().contains("id"))
            return;

        EntityType<?> entity = EntityType.by(stack.getOrCreateTag()).orElse(null);

        TranslationTextComponent entityName = entity != null ?
                new TranslationTextComponent(entity.getDescriptionId()) : MobStatues.translate("entity", "unknown");
        tooltip.add(MobStatues.translate("tooltip", "statue", entityName));
    }

    @Override
    protected boolean placeBlock(BlockItemUseContext context, BlockState state) {
        if (!context.getItemInHand().hasTag())
            return false;

        CompoundNBT nbt = context.getItemInHand().getOrCreateTag();
        Direction lookingDir = context.getHorizontalDirection();
        BlockPos start = context.getClickedPos();
        World world = context.getLevel();

        EntitySize size = StatueCreationHelper.getEntitySize(nbt, world);
        if (size == null) {
            MobStatues.LOGGER.warn("Failed retrieving entity size with data {}", nbt);
            return false;
        }

        Pair<List<BlockPos>, Boolean> toPlace = getPlacements(start, world, size, lookingDir);
        if (toPlace == null)
            return false;

        if (!toPlace.getFirst().remove(start)) // shouldn't happen?
            return false;

        boolean ret = world.setBlock(start, state, Constants.BlockFlags.DEFAULT_AND_RERENDER);

        TileEntity te = world.getBlockEntity(context.getClickedPos());
        if (!(te instanceof StatueTileEntity))
            return false;

        ((StatueTileEntity) te).setup(nbt, toPlace, start, lookingDir.getOpposite());

        for (BlockPos p : toPlace.getFirst()) {
            if (!world.setBlock(p, state.setValue(StatueBlock.START, false), Constants.BlockFlags.DEFAULT))
                ret = false;
            if (world.getBlockEntity(p) instanceof DelegatingTileEntity)
                ((DelegatingTileEntity) world.getBlockEntity(p)).setDelegate(start);
            else
                ret = false;
        }
        return ret;
    }

    @Nullable
    public static Pair<List<BlockPos>, Boolean> getPlacements(BlockPos placed, World world, EntitySize size, Direction lookingDir) {
        // Can't place relative to up/down.
        if (lookingDir.get2DDataValue() == -1) {
            MobStatues.LOGGER.error("Tried getting statue placements for a non horizontal direction!");
            return null;
        }

        List<BlockPos> ret = Lists.newArrayList(placed.immutable());
        if (size.height <= 1 && size.width <= 1)
            return Pair.of(ret, null);
        int h = MathHelper.ceil(size.height - 1);
        int w = MathHelper.ceil(size.width - 1);

        ret.addAll(extendInDir(ret, Direction.UP, h));
        boolean canPlace = verify(ret, world);
        if (!canPlace) // for now we aren't checking down only up.
            return null;
        if (w == 0)
            return Pair.of(ret, null); //null for no width.

        ret.addAll(extendInDir(ret, lookingDir, w));

        if (!verify(ret, world)) // We extended in the direction of the look vector, don't go opposite of that.
            return null;

        List<BlockPos> copy = Lists.newArrayList(ret);
        ret.addAll(extendInDir(ret, lookingDir.getClockWise(), w)); // right of the look vector

        if (!verify(ret, world)) {
            ret = copy;
            ret.addAll(extendInDir(ret, lookingDir.getCounterClockWise(), w)); // left of the look vector
            return verify(ret, world) ? Pair.of(ret, true) : null;
        }
        else
            return Pair.of(ret, false);
    }

    private static List<BlockPos> extendInDir(List<BlockPos> placed, Direction direction, int reps) {
        List<BlockPos> ret = new ArrayList<>();
        for (int i = 1; i <= reps; i++) {
            for (BlockPos pos : placed) {
                ret.add(pos.relative(direction, i));
            }
        }
        return ret;
    }

    private static boolean verify(List<BlockPos> toVerify, World world) {
        return toVerify.stream().allMatch(world::isEmptyBlock);
    }
}
