package com.cyborgmas.mobstatues.objects;

import com.cyborgmas.mobstatues.MobStatues;
import com.cyborgmas.mobstatues.client.StatueEditingScreen;
import com.cyborgmas.mobstatues.registration.Registration;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

@SuppressWarnings({"deprecation", "NullableProblems"})
public class StatueBlock extends BaseEntityBlock {
    public static BooleanProperty START = BooleanProperty.create("start");

    public StatueBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(START, true));
    }

    @Nullable
    public StatueBlockEntity getStatueTile(BlockGetter reader, BlockPos pos) {
        BlockEntity te = reader.getBlockEntity(pos);
        StatueBlockEntity statue = null;
        if (te instanceof StatueBlockEntity)
            statue = (StatueBlockEntity) te;
        else if (te instanceof DelegatingBlockEntity)
            statue = ((DelegatingBlockEntity) te).getDelegate(Registration.STATUE_BLOCK_ENTITY.get(), reader);
        else
            MobStatues.LOGGER.debug("Tried getting statue at invalid pos {} found {} instead", pos, te);

        return statue;
    }

    public VoxelShape getShape(BlockGetter reader, BlockPos pos, boolean rendering) {
        StatueBlockEntity statue = getStatueTile(reader, pos);

        if (statue != null) {
            Pair<VoxelShape, VoxelShape> pair = statue.getBothShapes(pos);
            return rendering ? pair.getFirst() : pair.getSecond();
        }
        return Shapes.block();
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        StatueBlockEntity statue = getStatueTile(world, pos);
        if (statue == null)
            return InteractionResult.PASS;
        if (world.isClientSide && statue.getStatue() != null && StatueEditingScreen.canEditModel(statue.getStatue()))
                Minecraft.getInstance().setScreen(new StatueEditingScreen(statue));
        return InteractionResult.SUCCESS;
    }

    /**
     * Destroys the whole statue if the block destroyed is the starting block.
     */
    @Override
    public void playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player) {
        if (state.getValue(START)) {
            BlockEntity te = worldIn.getBlockEntity(pos);
            if (te instanceof StatueBlockEntity)
                ((StatueBlockEntity) te).destroyStatue(worldIn, pos);
        }

        super.playerWillDestroy(worldIn, pos, state, player);
    }

    /**
     * Destroys the whole statue if the block destroyed is not the starting block.
     */
    @Override
    public BlockState updateShape(BlockState thisState, Direction facing, BlockState changedState, LevelAccessor world, BlockPos currentPos, BlockPos changedPos) {
        StatueBlockEntity statue = getStatueTile(world, currentPos);

        if (statue != null)
            statue.checkDestroyStatue(world, changedPos);

        return super.updateShape(thisState, facing, changedState, world, currentPos, changedPos);
    }

    @Override //TODO PR to forge to be able to control the TE NBT that is added
    public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        return ItemStack.EMPTY;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(START);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(START) ? new StatueBlockEntity(pos, state) : new DelegatingBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
        return this.getShape(reader, pos, true);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
        return this.getShape(reader, pos, false);
    }
}
