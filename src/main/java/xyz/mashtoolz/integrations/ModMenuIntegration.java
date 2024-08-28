package xyz.mashtoolz.integrations;

import xyz.mashtoolz.config.FaceConfig;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> {
			return FaceConfig.getScreen();
		};
	}
}
