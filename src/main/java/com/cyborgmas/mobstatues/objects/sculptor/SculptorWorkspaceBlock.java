package com.cyborgmas.mobstatues.objects.sculptor;

import com.cyborgmas.mobstatues.MobStatues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

import javax.annotation.Nullable;

import static net.minecraftforge.common.util.Constants.BlockFlags;
import static net.minecraftforge.common.util.Constants.WorldEvents;

@SuppressWarnings("deprecation")
public class SculptorWorkspaceBlock extends HorizontalDirectionalBlock {
    public static final Component CONTAINER_TITLE = MobStatues.translate("container", "sculptor_workspace");
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public SculptorWorkspaceBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER).setValue(FACING, Direction.NORTH));
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            NetworkHooks.openGui((ServerPlayer) player,
                    new SimpleMenuProvider((id, inv, p) -> new SculptorWorkspaceMenu(id, inv, ContainerLevelAccess.create(level, pos)), CONTAINER_TITLE));
            return InteractionResult.CONSUME;
        }
    }

    /**
     * Taken from {@link DoublePlantBlock#updateShape}
     */
    @Override
    public BlockState updateShape(BlockState state, Direction offset, BlockState neighbour, LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        DoubleBlockHalf half = state.getValue(HALF);
        if ((
                offset.getAxis() != Direction.Axis.Y ||
                        half == DoubleBlockHalf.LOWER != (offset == Direction.UP) ||
                        neighbour.is(this) && neighbour.getValue(HALF) != half)
                && !(half == DoubleBlockHalf.LOWER && offset == Direction.DOWN && !state.canSurvive(level, pos))
        )
            return super.updateShape(state, offset, state, level, pos, neighbourPos);
        return Blocks.AIR.defaultBlockState();
    }

    /**
     * Taken from {@link DoublePlantBlock#preventCreativeDropFromBottomPart} which is protected...
     */
    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative()) {
            DoubleBlockHalf half = state.getValue(HALF);
            if (half == DoubleBlockHalf.UPPER) {
                BlockPos below = pos.below();
                BlockState belowState = level.getBlockState(below);
                if (belowState.is(state.getBlock()) && belowState.getValue(HALF) == DoubleBlockHalf.LOWER) {
                    level.setBlock(below, Blocks.AIR.defaultBlockState(), BlockFlags.DEFAULT | BlockFlags.NO_NEIGHBOR_DROPS);
                    level.levelEvent(player, WorldEvents.BREAK_BLOCK_EFFECTS, below, Block.getId(belowState));
                }
            }
        }

        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), BlockFlags.DEFAULT);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader reader, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState below = reader.getBlockState(belowPos);
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? below.isFaceSturdy(reader, belowPos, Direction.UP) : below.is(this);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF, FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
}
