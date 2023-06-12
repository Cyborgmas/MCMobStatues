package com.cyborgmas.mobstatues.objects.statue;

import com.cyborgmas.mobstatues.MobStatues;
import com.cyborgmas.mobstatues.client.ItemRenderProperties;
import com.cyborgmas.mobstatues.objects.DelegatingBlockEntity;
import com.cyborgmas.mobstatues.registration.Registration;
import com.cyborgmas.mobstatues.util.StatueCreationHelper;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class StatueBlockItem extends BlockItem {
    public StatueBlockItem(Properties properties) {
        super(Registration.STATUE_BLOCK.get(), properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        if (!stack.hasTag() || !stack.getOrCreateTag().contains("id"))
            return;

        EntityType<?> entity = EntityType.by(stack.getOrCreateTag()).orElse(null);

        MutableComponent entityName = entity != null ?
                Component.translatable(entity.getDescriptionId()) : MobStatues.translate("entity", "unknown");
        tooltip.add(MobStatues.translate("tooltip", "statue", entityName));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(ItemRenderProperties.getStatueBlockItemRender());
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        if (!context.getItemInHand().hasTag())
            return false;

        CompoundTag nbt = context.getItemInHand().getOrCreateTag();
        Direction lookingDir = context.getHorizontalDirection();
        BlockPos start = context.getClickedPos();
        Level world = context.getLevel();

        EntityDimensions size = StatueCreationHelper.getEntitySize(nbt, world);
        if (size == null) {
            MobStatues.LOGGER.warn("Failed retrieving entity size with data {}", nbt);
            return false;
        }

        //TODO send this information via packet?
        // If the player rotates too fast there will be a desync between the blocks placed as
        // lookingDir will have changed.
        Pair<List<BlockPos>, Boolean> toPlace = getPlacements(start, world, size, lookingDir);
        if (toPlace == null)
            return false;

        if (!toPlace.getFirst().remove(start)) // shouldn't happen?
            return false;

        boolean ret = world.setBlock(start, state, Block.UPDATE_ALL);

        BlockEntity te = world.getBlockEntity(context.getClickedPos());
        if (!(te instanceof StatueBlockEntity))
            return false;

        ((StatueBlockEntity) te).setup(nbt, toPlace, start, lookingDir.getOpposite());

        for (BlockPos p : toPlace.getFirst()) {
            if (!world.setBlock(p, state.setValue(StatueBlock.START, false), Block.UPDATE_ALL))
                ret = false;
            if (world.getBlockEntity(p) instanceof DelegatingBlockEntity)
                ((DelegatingBlockEntity) world.getBlockEntity(p)).setDelegate(start);
            else
                ret = false;
        }
        return ret;
    }

    @Nullable
    public static Pair<List<BlockPos>, Boolean> getPlacements(BlockPos placed, Level world, EntityDimensions size, Direction lookingDir) {
        // Can't place relative to up/down.
        if (lookingDir.get2DDataValue() == -1) {
            MobStatues.LOGGER.error("Tried getting statue placements for a non horizontal direction!");
            return null;
        }

        List<BlockPos> ret = Lists.newArrayList(placed.immutable());
        if (size.height <= 1 && size.width <= 1)
            return Pair.of(ret, null);
        int h = Mth.ceil(size.height - 1);
        int w = Mth.ceil(size.width - 1);

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

    private static boolean verify(List<BlockPos> toVerify, Level world) {
        return toVerify.stream().allMatch(world::isEmptyBlock);
    }
}
