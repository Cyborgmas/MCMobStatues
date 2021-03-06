package com.cyborgmas.mobstatues.objects.sculptor;

import com.cyborgmas.mobstatues.registration.Registration;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import java.util.Optional;

public class SculptorWorkspaceMenu extends RecipeBookMenu<SculptorWorkspaceContainer> {
    private final SculptorWorkspaceContainer container = new SculptorWorkspaceContainer();
    /**
     * has to be separate to not loop when the result slot is changed.
     */
    private final ResultContainer resultContainer = new ResultContainer();
    private final Slot resultSlot;
    private final ContainerLevelAccess access;
    private final Player player;

    /**
     * Client init
     */
    public SculptorWorkspaceMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, ContainerLevelAccess.NULL);
    }

    /**
     * Server init
     */
    public SculptorWorkspaceMenu(int id, Inventory inv, ContainerLevelAccess access) {
        super(Registration.SCULPTOR_WORKSPACE_MENU_TYPE.get(), id);
        this.access = access;
        this.container.addListener(this::slotsChanged);
        this.player = inv.player;

        /**
         * Copied from {@link ResultSlot} but not limited to {@link CraftingContainer}
         */
        this.resultSlot = new Slot(this.resultContainer, 0, 144, 35) {
            private int removeCount;

            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                this.checkTakeAchievements(stack);
                Container craftingContainer = SculptorWorkspaceMenu.this.container;
                NonNullList<ItemStack> remainders = player.level.getRecipeManager().getRemainingItemsFor(SculptingRecipe.TYPE.get(), SculptorWorkspaceMenu.this.container, player.level);
                for (int i = 0; i < remainders.size(); ++i) {
                    ItemStack toRemove = craftingContainer.getItem(i);
                    ItemStack toReplace = remainders.get(i);
                    if (!toRemove.isEmpty()) {
                        craftingContainer.removeItem(i, 1);
                        toRemove = craftingContainer.getItem(i);
                    }

                    if (!toReplace.isEmpty()) {
                        if (toRemove.isEmpty())
                            craftingContainer.setItem(i, toRemove);
                        else if (ItemStack.isSame(toRemove, toReplace) && ItemStack.tagMatches(toRemove, toReplace)) {
                            toReplace.grow(toRemove.getCount());
                            craftingContainer.setItem(i, toReplace);
                        } else if (!player.getInventory().add(toReplace))
                            player.drop(toReplace, false);
                    }
                }
            }

            @Override
            public ItemStack remove(int amount) {
                if (this.hasItem())
                    this.removeCount += Math.min(amount, this.getItem().getCount());
                return super.remove(amount);
            }

            @Override
            public void onQuickCraft(ItemStack output, int amount) {
                this.removeCount += amount;
                this.checkTakeAchievements(output);
            }

            @Override
            protected void onSwapCraft(int amount) {
                this.removeCount = amount;
            }

            @Override
            protected void checkTakeAchievements(ItemStack stack) {
                if (this.removeCount > 0)
                    stack.onCraftedBy(SculptorWorkspaceMenu.this.player.level, SculptorWorkspaceMenu.this.player, this.removeCount);
                if (this.container instanceof RecipeHolder recipeHolder)
                    recipeHolder.awardUsedRecipes(SculptorWorkspaceMenu.this.player);
                this.removeCount = 0;
            }
        };
        this.addSlot(this.resultSlot); //slot 0

        //slots 1 - 8
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 2; ++j)
                this.addSlot(new Slot(this.container, j + i * 2, 30 + j * 18, 61 - i * 18));
        }

        this.addSlot(new Slot(this.container, SculptorWorkspaceContainer.COLOR_IDX, 113, 17)); //slot 9
        this.addSlot(new Slot(this.container, SculptorWorkspaceContainer.TEXTURE_IDX, 113, 53)); //slot 10

        //slots 11 to 37
        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j)
                this.addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
        }

        //slots 38 to 46
        for(int i = 0; i < 9; ++i)
            this.addSlot(new Slot(inv, i, 8 + i * 18, 142));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, Registration.SCULPTOR_WORKSPACE_BLOCK.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> this.clearContainer(player, this.container));
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot != resultSlot;
    }

    @Override
    public void slotsChanged(Container container) {
        this.access.execute((level, pos) -> {
            if (container == this.container) {
                Optional<SculptingRecipe> recipe = level.getRecipeManager()
                        .getRecipeFor(SculptingRecipe.TYPE.get(), this.container, level);
                if (recipe.isEmpty())
                    this.resultContainer.setItem(0, ItemStack.EMPTY);
                else if (player instanceof ServerPlayer sp && this.resultContainer.setRecipeUsed(level, sp, recipe.get())) {
                    ItemStack stack = recipe.get().assemble(this.container);
                    this.resultContainer.setItem(0, stack);
                }
            }
        });

        super.slotsChanged(container);
    }

    private static final int RESULT_SLOT = 0;
    private static final int CRAFTING_START = 1;
    private static final int CRAFTING_STOP = 10;
    private static final int INVENTORY_START = 11;
    private static final int INVENTORY_STOP = 46;
    private static final int HOTBAR_START = 38;

    /**
     * Mostly copied from {@link CraftingMenu#quickMoveStack}
     */
    public ItemStack quickMoveStack(Player player, int idx) {
        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(idx);
        if (!slot.hasItem())
            return ret;

        ItemStack item = slot.getItem();
        ret = item.copy();

        if (idx == RESULT_SLOT) {
            if (!this.moveItemStackTo(item, INVENTORY_START, INVENTORY_STOP + 1, true))
                return ItemStack.EMPTY;

            slot.onQuickCraft(item, ret);
        } else if (idx >= INVENTORY_START && idx < INVENTORY_STOP + 1) {
            if (!this.moveItemStackTo(item, CRAFTING_START, CRAFTING_STOP + 1, false)) {
                if (idx < HOTBAR_START) {
                    if (!this.moveItemStackTo(item, HOTBAR_START, INVENTORY_STOP + 1, false))
                        return ItemStack.EMPTY;
                } else if (!this.moveItemStackTo(item, INVENTORY_START, HOTBAR_START, false))
                    return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(item, INVENTORY_START, INVENTORY_STOP + 1, false))
            return ItemStack.EMPTY;

        if (item.isEmpty())
            slot.set(ItemStack.EMPTY);
        else
            slot.setChanged();

        if (item.getCount() == ret.getCount())
            return ItemStack.EMPTY;

        slot.onTake(player, item);
        if (idx == RESULT_SLOT)
            player.drop(item, false);

        return ret;
    }

    //RecipeBook stuff
    @Override
    public void fillCraftSlotsStackedContents(StackedContents contents) {
        this.container.fillStackedContents(contents);
    }

    @Override
    public void clearCraftingContent() {
        this.container.clearContent();
        this.resultContainer.clearContent();
    }

    @Override
    public boolean recipeMatches(Recipe<? super SculptorWorkspaceContainer> recipe) {
        return recipe.matches(this.container, this.player.level);
    }

    @Override
    public int getResultSlotIndex() {
        return RESULT_SLOT;
    }

    @Override
    public int getGridWidth() {
        return 2;
    }

    @Override
    public int getGridHeight() {
        return 4;
    }

    @Override
    public int getSize() {
        return 11;
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return Registration.SCULPTING;
    }

    @Override
    public boolean shouldMoveToInventory(int slotIdx) {
        return slotIdx != RESULT_SLOT;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handlePlacement(boolean shiftDown, Recipe<?> recipe, ServerPlayer player) {
        new ServerPlaceSculptingRecipe(this).recipeClicked(player, (Recipe<SculptorWorkspaceContainer>) recipe, shiftDown);
    }
}
