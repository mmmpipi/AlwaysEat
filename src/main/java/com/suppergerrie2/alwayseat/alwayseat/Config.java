package com.suppergerrie2.alwayseat.alwayseat;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class Config {

    public static final String CATEGORY_EATABLE = "eatable";

    public static final Config CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;


    public static ModConfigSpec.ConfigValue<List<?>> ITEM_LIST;
    public static ModConfigSpec.ConfigValue<List<?>> UNEATABLE_ITEMS;
    public static ModConfigSpec.EnumValue<Mode> MODE;

    public enum Mode{
        BLACKLIST,
        WHITELIST
    }

    public Config(ModConfigSpec.Builder builder) {
        builder.comment("Eatable settings").push(CATEGORY_EATABLE);

        ITEM_LIST = builder
                .comment(
                        "List of items",
                        "Depending on the mode only these items will be made eatable (WHITELIST) or these items will keep their vanilla behaviour (BLACKLIST)",
                        "If an item is not affected according to the rules above they will keep their vanilla behaviour"
                )
                .defineList("item_list", new ArrayList<>(),()->"minecraft:bread", Config::isValidResourceLocation);

        UNEATABLE_ITEMS = builder
                .comment(
                        "List of items",
                        "These items will be made uneatable while full (Overrides vanilla behaviour)"
                )
                .defineList("uneatable_list", new ArrayList<>(),()->"minecraft:bread", Config::isValidResourceLocation);
        MODE = builder
                .comment("Mode as explained in other settings")
                .defineEnum("mode", Mode.BLACKLIST);


        //builder.pop();

    }

    //CONFIG and CONFIG_SPEC are both built from the same builder, so we use a static block to seperate the properties
    static {
        Pair<Config, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(Config::new);

        //Store the resulting values
        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    static boolean isValidResourceLocation(Object o) {
        if(o instanceof ResourceLocation) return true;

        if(o instanceof String resourceName) {
            return ResourceLocation.tryParse(resourceName) != null;
        }

        return false;
    }
}
