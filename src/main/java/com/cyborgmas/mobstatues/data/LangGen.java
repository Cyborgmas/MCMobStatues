package com.cyborgmas.mobstatues.data;

import com.cyborgmas.mobstatues.MobStatues;
import com.cyborgmas.mobstatues.objects.sculptor.SculptorWorkspaceBlock;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

import static com.cyborgmas.mobstatues.registration.Registration.SCULPTOR_WORKSPACE_BLOCK;
import static com.cyborgmas.mobstatues.registration.Registration.STATUE_BLOCK;

public class LangGen extends LanguageProvider {
    public LangGen(DataGenerator gen) {
        super(gen, MobStatues.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        this.add(STATUE_BLOCK.get(), "Statue");
        this.add(SCULPTOR_WORKSPACE_BLOCK.get(), "Sculptor Workspace");
        this.add(MobStatues.translateRaw("entity", "unknown"), "Unknown");
        this.add(MobStatues.translateRaw("tooltip", "statue"), "%s statue");
        this.add(SculptorWorkspaceBlock.CONTAINER_TITLE.getString(), "Sculptor Workspace");
    }
}
