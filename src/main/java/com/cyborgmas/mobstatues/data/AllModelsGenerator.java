package com.cyborgmas.mobstatues.data;

import com.cyborgmas.mobstatues.MobStatues;
import com.cyborgmas.mobstatues.objects.sculptor.SculptorWorkspaceBlock;
import com.cyborgmas.mobstatues.registration.Registration;
import com.google.gson.*;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.client.model.generators.ModelFile.UncheckedModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

import static com.cyborgmas.mobstatues.registration.Registration.SCULPTOR_WORKSPACE_BLOCK;
import static com.cyborgmas.mobstatues.registration.Registration.STATUE_BLOCK;
import static net.minecraftforge.client.model.generators.ModelBuilder.Perspective.*;

public class AllModelsGenerator extends BlockStateProvider {
    private final ExistingFileHelper existingFileHelper;
    private final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public AllModelsGenerator(GatherDataEvent event) {
        super(event.getGenerator(), MobStatues.MODID, event.getExistingFileHelper());
        this.existingFileHelper = event.getExistingFileHelper();
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
                .transform(THIRDPERSON_RIGHT)
                .rotation(75, 315, 0).translation(0, 2.5f, 0).scale(0.375f).end()
                .transform(FIRSTPERSON_RIGHT)
                .rotation(0, 315, 0).scale(0.4f).end()
                .end();

        ModelFile statue = models().getBuilder("statue_block").texture("particle", mcLoc("block/stone"));
        getVariantBuilder(STATUE_BLOCK.get())
                .forAllStates(state ->
                        ConfiguredModel.builder().modelFile(statue).build()
                );

        BlockModelBuilder builder = models().getBuilder("test_model");
        try {
            Resource coffeeCupModel = this.existingFileHelper.getResource(MobStatues.getId("coffee_cup"), PackType.CLIENT_RESOURCES,".json", "models/block");
            try (InputStream inputstream = coffeeCupModel.getInputStream();
                 Reader reader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8))
            ) {
                BlockModel model = BlockModel.fromStream(reader);
                builder.ao(model.hasAmbientOcclusion)
                        .guiLight(model.getGuiLight())
                        .parent(new UncheckedModelFile(model.getParentLocation()));

                model.textureMap.forEach((s, e) ->
                        e.right().or(() -> e.left().map(m -> m.texture().toString()))
                                .ifPresent(n -> builder.texture(s, n)));
                Arrays.stream(ModelBuilder.Perspective.values()).forEach(p -> {
                    ItemTransform t = model.getTransforms().getTransform(p.vanillaType);
                    builder.transforms().transform(p)
                            .translation(t.translation.x(), t.translation.y(), t.translation.z())
                            .scale(t.scale.x(), t.scale.y(), t.scale.z())
                            .rotation(t.rotation.x(), t.rotation.y(), t.rotation.z());
                });

                model.getElements().forEach(e -> {
                    builder.element()
                            .from(e.from.x(), e.from.y() + 10, e.from.z())
                            .to(e.to.x(), e.to.x() + 10, e.to.z())
                            .allFaces((dir, faceBuilder) -> {
                                BlockElementFace face = e.faces.get(dir);
                                float[] uvs = face.uv.uvs;
                                faceBuilder
                                        .cullface(face.cullForDirection)
                                        .texture(face.texture)
                                        .rotation(ModelBuilder.FaceRotation.values()[face.uv.rotation / 90])
                                        .uvs(uvs[0], uvs[1], uvs[2], uvs[3])
                                        .tintindex(face.tintIndex);
                                    })
                            .shade(e.shade);
//                            no need for rotation
//                            .rotation()
//                            .angle(e.rotation.angle)
//                            .axis(e.rotation.axis)
//                            .origin(e.rotation.origin.x(), e.rotation.origin.y(), e.rotation.origin.z())
//                            .rescale(e.rotation.rescale);
                });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        horizontalBlock(SCULPTOR_WORKSPACE_BLOCK.get(), state -> {
                    boolean lower = state.getValue(SculptorWorkspaceBlock.HALF) == DoubleBlockHalf.LOWER;
                    ModelFile halfModel = models().getExistingFile(
                            MobStatues.getId(
                                    "sculptor_workspace_" + (lower ? "bottom" : "top")
                            )
                    );
                    return halfModel;
        });

        this.itemModels().getBuilder("sculptor_workspace")
                .parent(new UncheckedModelFile("builtin/entity"));
    }
}
