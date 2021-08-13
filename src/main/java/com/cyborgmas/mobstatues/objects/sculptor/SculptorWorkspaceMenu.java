package com.cyborgmas.mobstatues.objects.sculptor;

import com.cyborgmas.mobstatues.registration.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import javax.annotation.Nullable;

public class SculptorWorkspaceMenu extends AbstractContainerMenu {

    /**
     * Client init
     */
    public SculptorWorkspaceMenu(int id, Inventory inv, FriendlyByteBuf data) {
        super(Registration.SCULPTOR_WORKSPACE_MENU_TYPE.get(), id);
    }

    /**
     * Server init
     */
    public SculptorWorkspaceMenu(int id, Inventory inv) {
        super(Registration.SCULPTOR_WORKSPACE_MENU_TYPE.get(), id);
    }

    @Override
    public boolean stillValid(Player p_38874_) {
        return false;
    }
}
