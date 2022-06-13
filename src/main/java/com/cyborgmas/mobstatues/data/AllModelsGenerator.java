package com.cyborgmas.mobstatues.data;

import com.cyborgmas.mobstatues.MobStatues;
import com.cyborgmas.mobstatues.objects.sculptor.SculptorWorkspaceBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelFile.UncheckedModelFile;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import static com.cyborgmas.mobstatues.registration.Registration.SCULPTOR_WORKSPACE_BLOCK;
import static com.cyborgmas.mobstatues.registration.Registration.STATUE_BLOCK;
import static net.minecraft.client.renderer.block.model.ItemTransforms.TransformType.*;

public class AllModelsGenerator extends BlockStateProvider {
    public AllModelsGenerator(GatherDataEvent event) {
        super(event.getGenerator(), MobStatues.MODID, event.getExistingFileHelper());
    }

    @Override
    protected void registerStatesAndModels() {
        this.itemModels().getBuilder("statue")
                .parent(new UncheckedModelFile("builtin/entity"))
                .transforms()
                .transform(GUI)
                .rotation(30, -35, 0).translation(2,-3,0).scale(0.625f).end()
                .transform(GROUND)
                .translation(0, 3,0).scale(0.25f).end()
                .transform(HEAD)
                .rotation(0, 180 ,0).end()
                .transform(FIXED)
                .rotation(0, 180, 0).scale(0.5f).end()
                .transform(THIRD_PERSON_RIGHT_HAND)
                .rotation(75, 315, 0).translation(0, 2.5f, 0).scale(0.375f).end()
                .transform(FIRST_PERSON_RIGHT_HAND)
                .rotation(0, 315, 0).scale(0.4f).end()
                .end();

        ModelFile statue = models().getBuilder("statue_block").texture("particle", mcLoc("block/stone"));
        getVariantBuilder(STATUE_BLOCK.get())
                .forAllStates(state ->
                        ConfiguredModel.builder().modelFile(statue).build()
                );

        horizontalBlock(SCULPTOR_WORKSPACE_BLOCK.get(), state -> {
                    boolean lower = state.getValue(SculptorWorkspaceBlock.HALF) == DoubleBlockHalf.LOWER;
                    return models().getExistingFile(
                            MobStatues.getId(
                                    "sculptor_workspace_" + (lower ? "bottom" : "top")
                            )
                    );
        });

        this.itemModels().getBuilder("sculptor_workspace")
                .parent(new UncheckedModelFile("builtin/entity"));
    }
}
