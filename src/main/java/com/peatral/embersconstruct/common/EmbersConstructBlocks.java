package com.peatral.embersconstruct.common;

import com.peatral.embersconstruct.common.block.BlockKiln;
import com.peatral.embersconstruct.common.block.BlockStampTable;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.List;

@GameRegistry.ObjectHolder(EmbersConstruct.MODID)
public class EmbersConstructBlocks {
    public static List<Block> blocks = new ArrayList<>();

    public static final Block Kiln = new BlockKiln();
    public static final Block StampTable = new BlockStampTable();

    public static void registerBlocks(IForgeRegistry<Block> registry) {
        registry.register(init(Kiln, "kiln"));
        registry.register(init(StampTable, "stamptable"));
    }

    public static void registerItemBlocks(IForgeRegistry<Item> registry) {
        for (Block block : blocks) {
            registry.register(EmbersConstructItems.init(new ItemBlock(block), block.getRegistryName().getResourcePath()));
        }
    }

    public static Block init(Block block, String name) {
        block = block.setUnlocalizedName(EmbersConstruct.MODID + "." + name).setRegistryName(new ResourceLocation(EmbersConstruct.MODID, name));
        blocks.add(block);
        return block;
    }
}
