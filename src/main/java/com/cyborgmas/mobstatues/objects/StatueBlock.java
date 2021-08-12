package com.cyborgmas.mobstatues.objects;

import com.cyborgmas.mobstatues.MobStatues;
import com.cyborgmas.mobstatues.client.StatueEditingScreen;
import com.cyborgmas.mobstatues.registration.Registration;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import net.minecraft.block.AbstractBlock.Properties;

@SuppressWarnings({"deprecation", "NullableProblems"})
public class StatueBlock extends Block {
    public static BooleanProperty START = BooleanProperty.create("start");

    public StatueBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(START, true));
    }

    @Nullable
    public StatueTileEntity getStatueTile(IBlockReader reader, BlockPos pos) {
        TileEntity te = reader.getBlockEntity(pos);
        StatueTileEntity statue = null;
        if (te instanceof StatueTileEntity)
            statue = (StatueTileEntity) te;
        else if (te instanceof DelegatingTileEntity)
            statue = ((DelegatingTileEntity) te).getDelegate(Registration.STATUE_TILE.get(), reader);
        else
            MobStatues.LOGGER.debug("Tried getting statue at invalid pos {} found {} instead", pos, te);

        return statue;
    }

    public VoxelShape getShape(IBlockReader reader, BlockPos pos, boolean rendering) {
        StatueTileEntity statue = getStatueTile(reader, pos);

        if (statue != null) {
            Pair<VoxelShape, VoxelShape> pair = statue.getBothShapes(pos);
            return rendering ? pair.getFirst() : pair.getSecond();
        }
        return VoxelShapes.block();
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        StatueTileEntity statue = getStatueTile(world, pos);
        if (statue == null)
            return ActionResultType.PASS;
        if (world.isClientSide && statue.getStatue() != null)
                Minecraft.getInstance().setScreen(new StatueEditingScreen(statue));
        return ActionResultType.SUCCESS;
    }

    /**
     * Destroys the whole statue if the block destroyed is the starting block.
     */
    @Override
    public void playerWillDestroy(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        if (state.getValue(START)) {
            TileEntity te = worldIn.getBlockEntity(pos);
            if (te instanceof StatueTileEntity)
                ((StatueTileEntity) te).destroyStatue(worldIn, pos);
        }

        super.playerWillDestroy(worldIn, pos, state, player);
    }

    /**
     * Destroys the whole statue if the block destroyed is not the starting block.
     */
    @Override
    public BlockState updateShape(BlockState thisState, Direction facing, BlockState changedState, IWorld world, BlockPos currentPos, BlockPos changedPos) {
        StatueTileEntity statue = getStatueTile(world, currentPos);

        if (statue != null)
            statue.checkDestroyStatue(world, changedPos);

        return super.updateShape(thisState, facing, changedState, world, currentPos, changedPos);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(START);
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
        return this.getShape(reader, pos, true);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
        return this.getShape(reader, pos, false);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return state.getValue(START) ? new StatueTileEntity() : new DelegatingTileEntity();
    }
}
