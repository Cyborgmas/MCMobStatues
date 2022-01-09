package com.cyborgmas.mobstatues.objects.statue;

import com.cyborgmas.mobstatues.MobStatues;
import com.cyborgmas.mobstatues.registration.Registration;
import com.cyborgmas.mobstatues.util.StatueCreationHelper;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.*;
import java.util.stream.Collectors;

public class StatueBlockEntity extends BlockEntity {
    private static final String STATUE_PARTS_KEY = "statue_parts_positions";
    private static final String STATUE_DIRECTION_KEY = "statue_direction";
    private static final String STATUE_TO_CENTER_KEY = "statue_to_center";
    private static final Map<StatueBlockEntity, Map<BlockPos, Pair<VoxelShape, VoxelShape>>> SHAPES = new WeakHashMap<>();

    //Fall backs
    private static final Pair<VoxelShape, VoxelShape> DEFAULT = Pair.of(Shapes.block(), Shapes.block());
    private static final CompoundTag NBT_PIG = Util.make(() -> {
       CompoundTag ret = new CompoundTag();
       ret.putString("id", "pig");
       return ret;
    });

    // Serialized fields
    private CompoundTag entityData = new CompoundTag();
    private List<BlockPos> wholeStatue = null;
    private Vec3 toCenter = new Vec3(0.5, 0, 0.5);
    private Direction direction = Direction.SOUTH;

    // Computed fields.
    private Entity statue = null;
    private EntityType<?> statueType = null;

    private boolean destroying = false;

    public StatueBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.STATUE_BLOCK_ENTITY.get(), pos, state);
    }

    public void setup(CompoundTag entityData, Pair<List<BlockPos>, Boolean> delegates, BlockPos placed, Direction direction) {
        setEntityData(entityData);
        this.wholeStatue = ImmutableList.<BlockPos>builder().addAll(delegates.getFirst()).add(placed).build();
        this.direction = direction;
        this.findStatueCenter(delegates.getFirst(), delegates.getSecond());
    }

    public Entity getStatue() {
        return this.statue;
    }

    public boolean shouldRender() {
        return !this.entityData.isEmpty() && this.getLevel() != null;
    }

    public boolean setup() {
        if (this.statue == null) {
            this.statue = StatueCreationHelper.getEntity(this.entityData, level,true);
            if (this.statue == null)
                return false;
            this.statue.setYHeadRot(0);
        }
        return true;
    }

    public Vec3 getToCenter() {
        return this.toCenter;
    }

    public float getYRotation() {
        return 90 * ((4 - this.direction.get2DDataValue()) % 4);
    }

    private void setEntityData(CompoundTag entityData) {
        this.entityData = entityData;
        this.statueType = EntityType.by(entityData).orElse(null);
    }

    /**
     * @param toLeft can be null, so leave it boxed.
     */
    private void findStatueCenter(List<BlockPos> placements, Boolean toLeft) {
        BlockPos thisPos = this.getBlockPos();
        Vec3i increment1 = this.direction.getOpposite().getNormal();
        Vec3i increment2 = toLeft == null ? Vec3i.ZERO : (toLeft ? this.direction.getClockWise() : this.direction.getCounterClockWise()).getNormal();
        Vec3i increment = new BlockPos(increment1).offset(increment2); // Bcs Vector3i doesn't have #add

        //Starting position is 0, 0 in the NW corner. adding +0.5 centers it
        double x = 0.5;
        double z = 0.5;
        for (BlockPos pos : placements) { // list doesn't contain the pos of this TE.
            if (pos.getY() == thisPos.getY()) { // don't consider y diffs
                // Add to one coord if the other is the same to not count corners.
                if (pos.getZ() == thisPos.getZ())
                    x += (increment.getX() / 2D);
                else if (pos.getX() == thisPos.getX())
                    z += (increment.getZ() / 2D);
            }
        }

        this.toCenter = new Vec3(x, 0, z);
    }

    public void checkDestroyStatue(LevelAccessor world, BlockPos changed) {
        if (this.wholeStatue != null &&
                !this.destroying &&
                this.wholeStatue.contains(changed) &&
                world.getBlockState(changed) == Blocks.AIR.defaultBlockState()
        ) {
            this.destroying = true;
            for (BlockPos pos : this.wholeStatue) {
                if (world.getBlockState(pos).getBlock() == Registration.STATUE_BLOCK.get())
                    world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    public void destroyStatue(LevelAccessor world, BlockPos ignore) {
        if (this.wholeStatue != null && !this.destroying) {
            this.destroying = true;
            for (BlockPos pos : this.wholeStatue) {
                if (!pos.equals(ignore) && world.getBlockState(pos).getBlock() == Registration.STATUE_BLOCK.get())
                    world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    /**
     * First is "rendering" shape &
     * Second is "collision" shape.
     */
    public Pair<VoxelShape, VoxelShape> getBothShapes(BlockPos looking) {
        if(this.statueType != null && this.wholeStatue != null && this.wholeStatue.contains(looking)) {
            return SHAPES.computeIfAbsent(this, tile ->
                            computeShapesFor(tile, tile.worldPosition, tile.wholeStatue)
                    ).computeIfAbsent(looking, pos -> {
                        MobStatues.LOGGER.warn("Failed to calculate the statue's collision boxes");
                        MobStatues.LOGGER.warn("Position: {}\nWholeStatue: {}\nRendering: {}", this.worldPosition, this.wholeStatue, pos);
                        return DEFAULT;
                    });
        }
        return DEFAULT;
    }

    private static Map<BlockPos, Pair<VoxelShape, VoxelShape>> computeShapesFor(StatueBlockEntity tile, BlockPos start, List<BlockPos> parts) {
        Map<BlockPos, Pair<VoxelShape, VoxelShape>> tileShapes = new HashMap<>();

        VoxelShape og = StatueCreationHelper.getShape(tile.entityData, tile.level);
        if (og == null) {
            MobStatues.LOGGER.warn("Failed getting shapes for entity {} with data {}", tile.statueType, tile.entityData);
            return tileShapes;
        }

        og = og.move(tile.toCenter.x, tile.toCenter.y, tile.toCenter.z);
        // From my understanding, returns the intersection of the 2 shapes.
        VoxelShape ogCollision = Shapes.join(og, Shapes.block(), BooleanOp.AND);

        tileShapes.put(start, Pair.of(og, ogCollision));

        for (BlockPos pos : parts) {
            if (!pos.equals(start)) {
                int x = start.getX() - pos.getX();
                int y = start.getY() - pos.getY();
                int z = start.getZ() - pos.getZ();
                VoxelShape rendering = og.move(x, y, z);
                // Not sure if I could improve this creation. Gives each block the appropriate collision box for where
                // they are, instead of giving them the full rendering shape but offset. This is so that in a X piece
                // statue there aren't X collision boxes for the single statue.
                VoxelShape offsetCube = Shapes.block().move(-x, -y, -z);
                VoxelShape collision = Shapes.join(og, offsetCube, BooleanOp.AND).move(x, y, z);

                tileShapes.put(pos, Pair.of(rendering, collision));
            }
        }
        return tileShapes;
    }

    private CompoundTag bothSidedWrite(CompoundTag nbt) {
        if (this.wholeStatue != null) {
            nbt.put(STATUE_PARTS_KEY, new LongArrayTag(
                    this.wholeStatue.stream().map(BlockPos::asLong).collect(Collectors.toList())
            ));
        }

        nbt.put(MobStatues.MOB_STATUE_KEY, this.entityData);

        ListTag list = new ListTag();
        list.add(DoubleTag.valueOf(this.toCenter.x));
        list.add(DoubleTag.valueOf(this.toCenter.y));
        list.add(DoubleTag.valueOf(this.toCenter.z));

        nbt.put(STATUE_TO_CENTER_KEY, list);

        nbt.put(STATUE_DIRECTION_KEY, IntTag.valueOf(this.direction.get2DDataValue()));

        return nbt;
    }

    private void bothSidedRead(CompoundTag nbt) {
        setEntityData(nbt.contains(MobStatues.MOB_STATUE_KEY, Tag.TAG_COMPOUND) ?
                nbt.getCompound(MobStatues.MOB_STATUE_KEY) : new CompoundTag());

        if (nbt.contains(STATUE_TO_CENTER_KEY, Tag.TAG_LIST)) {
            ListTag list = nbt.getList(STATUE_TO_CENTER_KEY, Tag.TAG_DOUBLE);
            if (list.size() == 3)
                this.toCenter = new Vec3(list.getDouble(0), list.getDouble(1), list.getDouble(2));
        }

        if (nbt.contains(STATUE_DIRECTION_KEY, Tag.TAG_INT))
            this.direction = Direction.from2DDataValue(nbt.getInt(STATUE_DIRECTION_KEY));

        if (nbt.contains(STATUE_PARTS_KEY, Tag.TAG_LONG_ARRAY))
            this.wholeStatue = Arrays.stream(nbt.getLongArray(STATUE_PARTS_KEY))
                    .mapToObj(BlockPos::of)
                    .collect(Collectors.toList());
    }

    @Override
    public AABB getRenderBoundingBox() {
        return getBothShapes(this.worldPosition).getFirst().bounds().move(this.worldPosition);
    }

    /**
     * Server write
     */
    @Override
    public CompoundTag save(CompoundTag nbt) {
        return this.bothSidedWrite(super.save(nbt));
    }

    /**
     * Server read
     */
    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.bothSidedRead(nbt);
    }

    /**
     * Client write
     */
    @Override
    public CompoundTag getUpdateTag() {
        return bothSidedWrite(super.getUpdateTag());
    }

    /**
     * Client read
     */
    @Override
    public void handleUpdateTag(CompoundTag tag) {
        this.bothSidedRead(tag);
    }
}
