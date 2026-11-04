package com.ritesh;

import com.ritesh.config.ModConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> me.shedaniel.autoconfig.AutoConfigClient.getConfigScreen(ModConfig.class, parent).get();
    }
}
