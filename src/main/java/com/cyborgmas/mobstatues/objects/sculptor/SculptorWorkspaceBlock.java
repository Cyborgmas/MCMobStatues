package com.cyborgmas.mobstatues.objects.sculptor;

import com.cyborgmas.mobstatues.MobStatues;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

import javax.annotation.Nullable;

public class SculptorWorkspaceBlock extends Block {
    public static final Component CONTAINER_TITLE = MobStatues.translate("container", "sculptor_workspace");

    public SculptorWorkspaceBlock(Properties properties) {
        super(properties);
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            NetworkHooks.openGui((ServerPlayer) player,
                    new SimpleMenuProvider((id, inv, p) -> new SculptorWorkspaceMenu(id, inv), CONTAINER_TITLE),
                    buf -> {});
            return InteractionResult.CONSUME;
        }
    }
}
