package com.peatral.embersconstruct.common;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = EmbersConstruct.MODID)
public class EmbersConstructConfig {

    @Config.LangKey("embersconstruct.config.section.general")
    public static final General general = new General();

    @Config.LangKey("embersconstruct.config.section.embersConstructRecipeSettings")
    @Config.Comment("Changes require a restart to come into effect!")
    public static final ECRecipeSettings embersConstructSettings = new ECRecipeSettings();

    @Config.LangKey("embersconstruct.config.section.tinkersConstructRecipeSettings")
    @Config.Comment("Changes require a restart to come into effect!")
    public static final TCRecipeSettings tinkersConstructSettings = new TCRecipeSettings();

    public static class General {

        @Config.LangKey("embersconstruct.config.showNewVersionMessage")
        @Config.Comment("Show the Update-Checker message if new version is available.")
        public boolean showNewVersionMessage = true;
    }

    public static class ECRecipeSettings {
        @Config.LangKey("embersconstruct.config.stampTableNeedBlank")
        @Config.Comment("Make already made raw stamps non-changeable")
        public boolean stampTableNeedBlank = true;
    }

    public static class TCRecipeSettings {
        @Config.LangKey("embersconstruct.config.removeMelting")
        @Config.Comment("")
        public boolean removeMelting = false;

        @Config.LangKey("embersconstruct.config.removeAlloying")
        @Config.Comment("")
        public boolean removeAlloying = false;

        @Config.LangKey("embersconstruct.config.removeTableCasting")
        @Config.Comment("")
        public boolean removeTableCasting = false;

        @Config.LangKey("embersconstruct.config.removeBasinCasting")
        @Config.Comment("")
        public boolean removeBasinCasting = false;
    }

    @Mod.EventBusSubscriber(modid = EmbersConstruct.MODID)
    private static class EventHandler {
        @SubscribeEvent
        public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(EmbersConstruct.MODID)) {
                ConfigManager.sync(EmbersConstruct.MODID, Config.Type.INSTANCE);
            }
        }
    }
}
