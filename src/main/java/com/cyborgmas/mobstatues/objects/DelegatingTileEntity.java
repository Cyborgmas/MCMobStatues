package com.cyborgmas.mobstatues.objects;

import com.cyborgmas.mobstatues.registration.Registration;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

public class DelegatingTileEntity extends BlockEntity {
    private static final String DELEGATED_POS_KEY = "delegated_tile_pos";
    // No need to render this TE. Empty VoxelShape doesn't have a bounding box.
    private static final AABB RENDER_AABB_CACHE = Shapes.block().bounds();
    private BlockPos delegatedPos = null;

    public DelegatingTileEntity(BlockPos pos, BlockState state) {
        super(Registration.DELEGATING_TILE.get(), pos, state);
    }

    public void setDelegate(BlockPos delegatedPos) {
        this.delegatedPos = delegatedPos;
    }

    @Nullable
    public <T extends BlockEntity> T getDelegate(BlockEntityType<T> type, BlockGetter reader) {
        if (this.delegatedPos != null) {
            BlockEntity te = reader.getBlockEntity(this.delegatedPos);
            return te != null && te.getType() == type ? (T) te : null;
        }
        return null;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return RENDER_AABB_CACHE;
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        if (delegatedPos != null)
            compound.putLong(DELEGATED_POS_KEY, delegatedPos.asLong());
        return super.save(compound);
    }

    @Override
    public void load(BlockState state, CompoundTag nbt) {
        super.load(nbt);
        if (nbt.contains(DELEGATED_POS_KEY, Constants.NBT.TAG_LONG))
            this.delegatedPos = BlockPos.of(nbt.getLong(DELEGATED_POS_KEY));
    }

    @Override
    public void handleUpdateTag(CompoundTag nbt) {
        if (nbt.contains(DELEGATED_POS_KEY, Constants.NBT.TAG_LONG))
            this.delegatedPos = BlockPos.of(nbt.getLong(DELEGATED_POS_KEY));
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = super.getUpdateTag();
        if (delegatedPos != null)
            nbt.putLong(DELEGATED_POS_KEY, delegatedPos.asLong());
        return nbt;
    }
}
