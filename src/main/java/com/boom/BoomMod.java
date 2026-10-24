package com.boom;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BoomMod implements ModInitializer {
    public static final String MOD_ID = "boom";

    public static Block DINAMITA;
    public static Block TNT_5X;
    public static Item DINAMITA_ITEM;
    public static Item TNT_5X_ITEM;

    private static Block registerBlock(String name, BoomBlock.Kind kind, float strength) {
        Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, name);
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);
        BlockBehaviour.Properties props = BlockBehaviour.Properties.of()
                .setId(key).strength(strength).sound(SoundType.GRASS);
        return Registry.register(BuiltInRegistries.BLOCK, key, new BoomBlock(kind, props));
    }

    private static Item registerBlockItem(String name, Block block) {
        Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, name);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        Item.Properties props = new Item.Properties().setId(key).useBlockDescriptionPrefix();
        return Registry.register(BuiltInRegistries.ITEM, key, new BlockItem(block, props));
    }

    @Override
    public void onInitialize() {
        DINAMITA = registerBlock("dinamita", BoomBlock.Kind.DINAMITA, 0.2f);
        TNT_5X = registerBlock("tnt_5x", BoomBlock.Kind.TNT_5X, 0.2f);
        DINAMITA_ITEM = registerBlockItem("dinamita", DINAMITA);
        TNT_5X_ITEM = registerBlockItem("tnt_5x", TNT_5X);

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.REDSTONE_BLOCKS).register(entries -> {
            entries.accept(DINAMITA_ITEM);
            entries.accept(TNT_5X_ITEM);
        });
    }
}
