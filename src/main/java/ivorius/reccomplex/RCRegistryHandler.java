/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex;

import com.gibby.dungeon.Dungeons;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.blocks.*;
import ivorius.reccomplex.client.rendering.RCBlockRendering;
import ivorius.reccomplex.dimensions.DimensionDictionary;
import ivorius.reccomplex.items.*;
import ivorius.reccomplex.blocks.materials.MaterialNegativeSpace;
import ivorius.reccomplex.blocks.materials.RCMaterials;
import ivorius.reccomplex.json.SerializableStringTypeRegistry;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.random.PoemLoader;
import ivorius.reccomplex.scripts.world.*;
import ivorius.reccomplex.structures.generic.maze.rules.MazeRuleRegistry;
import ivorius.reccomplex.structures.generic.maze.rules.saved.MazeRuleConnect;
import ivorius.reccomplex.structures.generic.maze.rules.saved.MazeRuleConnectAll;
import ivorius.reccomplex.structures.OperationMoveStructure;
import ivorius.reccomplex.structures.generic.BiomeGenerationInfo;
import ivorius.reccomplex.structures.generic.DimensionGenerationInfo;
import ivorius.reccomplex.structures.generic.StructureSaveHandler;
import ivorius.reccomplex.structures.generic.WeightedBlockState;
import ivorius.reccomplex.structures.generic.transformers.*;
import ivorius.reccomplex.structures.generic.gentypes.*;
import ivorius.reccomplex.structures.generic.matchers.BiomeMatcher;
import ivorius.reccomplex.structures.generic.matchers.DimensionMatcher;
import ivorius.reccomplex.structures.generic.presets.BiomeMatcherPresets;
import ivorius.reccomplex.structures.generic.presets.DimensionMatcherPresets;
import ivorius.reccomplex.structures.generic.presets.WeightedBlockStatePresets;
import ivorius.reccomplex.structures.schematics.OperationGenerateSchematic;
import ivorius.reccomplex.structures.OperationGenerateStructure;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.utils.BlockStates;
import ivorius.reccomplex.utils.FMLUtils;
import ivorius.reccomplex.worldgen.CategoryLoader;
import ivorius.reccomplex.worldgen.inventory.ItemCollectionSaveHandler;
import ivorius.reccomplex.worldgen.inventory.RCInventoryGenerators;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.BiomeDictionary;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ivorius.reccomplex.RecurrentComplex.*;
import static ivorius.reccomplex.blocks.RCBlocks.*;
import static ivorius.reccomplex.gui.RCCreativeTabs.*;
import static ivorius.reccomplex.items.RCItems.*;

/**
 * Created by lukas on 18.01.15.
 */
public class RCRegistryHandler
{
    public static void preInit(FMLPreInitializationEvent event, RecurrentComplex mod)
    {
        if (!RecurrentComplex.isLite())
        {
            tabStructureTools = new CreativeTabs("structureTools")
            {
                @Override
                public Item getTabIconItem()
                {
                    return RCItems.blockSelector;
                }
            };
            tabInventoryGenerators = new CreativeTabs("inventoryGenerators")
            {
                @Override
                public Item getTabIconItem()
                {
                    return RCItems.inventoryGenerationTag;
                }
            };
        }

        RCMaterials.materialNegativeSpace = new MaterialNegativeSpace();
        RCMaterials.materialGenericSolid = (new Material(MapColor.stoneColor));

        blockSelector = new ItemBlockSelectorBlock().setUnlocalizedName("blockSelector").setTextureName(textureBase + "blockSelector");
        blockSelector.setCreativeTab(tabStructureTools);
        register(blockSelector, "block_selector");
        RecurrentComplex.cremapper.registerLegacyIDs(blockSelector, "blockSelector");

        blockSelectorFloating = new ItemBlockSelectorFloating().setUnlocalizedName("blockSelectorFloating").setTextureName(textureBase + "blockSelectorFloating");
        blockSelectorFloating.setCreativeTab(tabStructureTools);
        register(blockSelectorFloating, "block_selector_floating");
        RecurrentComplex.cremapper.registerLegacyIDs(blockSelectorFloating, "blockSelectorFloating");

        inventoryGenerationTag = (ItemInventoryGenMultiTag) new ItemInventoryGenMultiTag().setUnlocalizedName("inventoryGenerationTag").setTextureName(textureBase + "inventoryGenerationTag");
        inventoryGenerationTag.setCreativeTab(tabInventoryGenerators);
        register(inventoryGenerationTag, "inventory_generation_tag");
        RecurrentComplex.cremapper.registerLegacyIDs(inventoryGenerationTag, "inventoryGenerationTag");

        inventoryGenerationSingleTag = (ItemInventoryGenSingleTag) new ItemInventoryGenSingleTag().setUnlocalizedName("inventoryGenerationSingleTag").setTextureName(textureBase + "inventoryGenerationSingleTag");
        inventoryGenerationSingleTag.setCreativeTab(tabInventoryGenerators);
        register(inventoryGenerationSingleTag, "inventory_generation_single_tag");
        RecurrentComplex.cremapper.registerLegacyIDs(inventoryGenerationSingleTag, "inventoryGenerationSingleTag");

        inventoryGenerationComponentTag = (ItemInventoryGenComponentTag) new ItemInventoryGenComponentTag().setUnlocalizedName("inventoryGenerationComponentTag").setTextureName(textureBase + "inventoryGenerationComponentTag");
        inventoryGenerationComponentTag.setCreativeTab(tabInventoryGenerators);
        register(inventoryGenerationComponentTag, "inventory_generation_component_tag");

        artifactGenerationTag = new ItemArtifactGenerator().setUnlocalizedName("artifactGenerationTag").setTextureName(textureBase + "artifactGenerationTag");
        artifactGenerationTag.setCreativeTab(tabInventoryGenerators);
        register(artifactGenerationTag, "artifact_generation_tag");
        RecurrentComplex.cremapper.registerLegacyIDs(artifactGenerationTag, "artifactGenerationTag");

        bookGenerationTag = new ItemBookGenerator().setUnlocalizedName("bookGenerationTag").setTextureName(textureBase + "bookGenerationTag");
        bookGenerationTag.setCreativeTab(tabInventoryGenerators);
        register(bookGenerationTag, "book_generation_tag");
        RecurrentComplex.cremapper.registerLegacyIDs(bookGenerationTag, "bookGenerationTag");

        genericSpace = new BlockGenericSpace().setBlockName("negativeSpace").setBlockTextureName(textureBase + "negativeSpace");
        genericSpace.setCreativeTab(tabStructureTools);
        register(genericSpace, ItemBlockNegativeSpace.class, "generic_space");
        RecurrentComplex.cremapper.registerLegacyIDs(genericSpace, true, "negativeSpace");

        genericSolid = new BlockGenericSolid().setBlockName("naturalFloor").setBlockTextureName(textureBase + "naturalFloor");
        genericSolid.setCreativeTab(tabStructureTools);
        register(genericSolid, ItemBlockGenericSolid.class, "generic_solid");
        RecurrentComplex.cremapper.registerLegacyIDs(genericSolid, true, "naturalFloor");

        structureGenerator = new BlockStructureGenerator().setBlockName("structureGenerator").setBlockTextureName(textureBase + "structureGenerator");
        register(structureGenerator, "structure_generator");
        register(TileEntityStructureGenerator.class, "RCStructureGenerator", "SGStructureGenerator");
        RecurrentComplex.cremapper.registerLegacyIDs(structureGenerator, true, "structureGenerator");

        mazeGenerator = new BlockMazeGenerator().setBlockName("mazeGenerator").setBlockTextureName(textureBase + "mazeGenerator");
        register(mazeGenerator, "maze_generator");
        register(TileEntityMazeGenerator.class, "RCMazeGenerator", "SGMazeGenerator");
        RecurrentComplex.cremapper.registerLegacyIDs(mazeGenerator, true, "mazeGenerator");

        spawnCommands = new BlockSpawnCommand().setBlockName("spawn_command").setBlockTextureName(textureBase + "spawnCommand");
        register(spawnCommands, "weighted_command_block");
        register(TileEntitySpawnCommand.class, "RCSpawnCommand");
        RecurrentComplex.cremapper.registerLegacyIDs(spawnCommands, true, "spawnCommand");

        spawnScript = new BlockSpawnScript().setBlockName("spawn_script").setBlockTextureName(textureBase + "spawnScript");
        spawnScript.setCreativeTab(tabStructureTools);
        register(spawnScript, "spawn_script");
        register(TileEntitySpawnScript.class, "RCSpawnScript");

        // Register early to allow proper loading
        registerDimensionPresets();
        registerBiomePresets();
        registerBlockStatePresets();
    }

    public static void register(Item item, String id)
    {
        if (!RecurrentComplex.isLite())
            GameRegistry.registerItem(item, id);
        else
            specialRegistry.register(FMLUtils.addPrefix(id), item);
    }

    public static void register(Block block, String id)
    {
        if (!RecurrentComplex.isLite())
            GameRegistry.registerBlock(block, id);
        else
        {
            specialRegistry.register(FMLUtils.addPrefix(id), block);
            specialRegistry.register(FMLUtils.addPrefix(id), new ItemBlock(block));
        }
    }

    public static void register(Block block, Class<? extends ItemBlock> itemClass, String id, Object... itemArgs)
    {
        if (!RecurrentComplex.isLite())
            GameRegistry.registerBlock(block, itemClass, id, itemArgs);
        else
        {
            specialRegistry.register(FMLUtils.addPrefix(id), block);
            Item item = FMLUtils.constructItem(block, itemClass, itemArgs);
            if (item != null) specialRegistry.register(FMLUtils.addPrefix(id), item);
        }
    }

    public static void register(Class<? extends TileEntity> tileEntity, String id, String...alternatives)
    {
        if (!RecurrentComplex.isLite())
            GameRegistry.registerTileEntityWithAlternatives(tileEntity, id, alternatives);
        else
        {
            specialRegistry.register(id, tileEntity);
            for (String aid : alternatives) specialRegistry.register(aid, tileEntity);
        }
    }

    public static void load(FMLInitializationEvent event, RecurrentComplex mod)
    {
        MCRegistry mcRegistry = RecurrentComplex.specialRegistry;

        fileTypeRegistry.put(StructureSaveHandler.FILE_SUFFIX, StructureSaveHandler.INSTANCE);
        fileTypeRegistry.put(ItemCollectionSaveHandler.FILE_SUFFIX, ItemCollectionSaveHandler.INSTANCE);
        fileTypeRegistry.put(PoemLoader.FILE_SUFFIX, new PoemLoader());
        fileTypeRegistry.put(CategoryLoader.FILE_SUFFIX, new CategoryLoader());

        WorldScriptRegistry.INSTANCE.register("multi", WorldScriptMulti.class);
        WorldScriptRegistry.INSTANCE.register("strucGen", WorldScriptStructureGenerator.class);
        WorldScriptRegistry.INSTANCE.register("mazeGen", WorldScriptMazeGenerator.class);
        WorldScriptRegistry.INSTANCE.register("command", WorldScriptCommand.class);

        SerializableStringTypeRegistry<Transformer> transformerRegistry = StructureRegistry.INSTANCE.getTransformerRegistry();
        transformerRegistry.registerType("natural", TransformerNatural.class, new TransformerNatural.Serializer(mcRegistry));
        transformerRegistry.registerType("naturalAir", TransformerNaturalAir.class, new TransformerNaturalAir.Serializer(mcRegistry));
        transformerRegistry.registerType("pillar", TransformerPillar.class, new TransformerPillar.Serializer(mcRegistry));
        transformerRegistry.registerType("replaceAll", TransformerReplaceAll.class, new TransformerReplaceAll.Serializer(mcRegistry));
        transformerRegistry.registerType("replace", TransformerReplace.class, new TransformerReplace.Serializer(mcRegistry));
        transformerRegistry.registerType("ruins", TransformerRuins.class, new TransformerRuins.Serializer(mcRegistry));
        transformerRegistry.registerType("negativeSpace", TransformerNegativeSpace.class, new TransformerNegativeSpace.Serializer(mcRegistry));

        SerializableStringTypeRegistry<StructureGenerationInfo> genInfoRegistry = StructureRegistry.INSTANCE.getStructureGenerationInfoRegistry();
        genInfoRegistry.registerType("natural", NaturalGenerationInfo.class, new NaturalGenerationInfo.Serializer());
        genInfoRegistry.registerType("structureList", StructureListGenerationInfo.class, new StructureListGenerationInfo.Serializer());
        genInfoRegistry.registerType("mazeComponent", MazeGenerationInfo.class, new MazeGenerationInfo.Serializer());
        genInfoRegistry.registerType("static", StaticGenerationInfo.class, new StaticGenerationInfo.Serializer());
        genInfoRegistry.registerType("vanilla", VanillaStructureGenerationInfo.class, new VanillaStructureGenerationInfo.Serializer());

        MazeRuleRegistry.INSTANCE.register("connect", MazeRuleConnect.class);
        MazeRuleRegistry.INSTANCE.register("connectall", MazeRuleConnectAll.class);

        OperationRegistry.register("strucGen", OperationGenerateStructure.class);
        OperationRegistry.register("schemGen", OperationGenerateSchematic.class);
        OperationRegistry.register("strucMove", OperationMoveStructure.class);

//        GameRegistry.registerWorldGenerator(new WorldGenStructures(), 50);
        RCInventoryGenerators.registerVanillaInventoryGenerators();
//        MapGenStructureIO.func_143031_a(GenericVillagePiece.class, "RcGSP");
//        VillagerRegistry.instance().registerVillageCreationHandler(new GenericVillageCreationHandler("DesertHut"));

        RCBlockRendering.negativeSpaceRenderID = RenderingRegistry.getNextAvailableRenderId();
    }

    protected static void registerDimensionPresets()
    {
        DimensionMatcherPresets.instance().register("clear");

        DimensionMatcherPresets.instance().register("overworld",
                new DimensionGenerationInfo(DimensionMatcher.ofTypes(DimensionDictionary.UNCATEGORIZED), null),
                new DimensionGenerationInfo(DimensionMatcher.ofTypes(DimensionDictionary.NO_TOP_LIMIT, DimensionDictionary.BOTTOM_LIMIT, DimensionDictionary.INFINITE, DimensionDictionary.EARTH), null)
        );
        DimensionMatcherPresets.instance().setDefault("overworld");

        DimensionMatcherPresets.instance().register("anyplanet",
                new DimensionGenerationInfo(DimensionMatcher.ofTypes(DimensionDictionary.UNCATEGORIZED), null),
                new DimensionGenerationInfo(DimensionMatcher.ofTypes(DimensionDictionary.NO_TOP_LIMIT, DimensionDictionary.BOTTOM_LIMIT, DimensionDictionary.INFINITE), null)
        );

        DimensionMatcherPresets.instance().register("nether",
                new DimensionGenerationInfo(DimensionMatcher.ofTypes(DimensionDictionary.HELL, DimensionDictionary.TOP_LIMIT, DimensionDictionary.BOTTOM_LIMIT), null)
        );

        DimensionMatcherPresets.instance().register("end",
                new DimensionGenerationInfo(DimensionMatcher.ofTypes(DimensionDictionary.ENDER, DimensionDictionary.NO_TOP_LIMIT, DimensionDictionary.NO_BOTTOM_LIMIT), null)
        );
        DimensionMatcherPresets.instance().register("Crystal Dimension(Arcana RPG)",
                new DimensionGenerationInfo(DimensionMatcher.ofTypes(String.valueOf(Dungeons.crystalDimensionId), DimensionDictionary.NO_TOP_LIMIT, DimensionDictionary.NO_BOTTOM_LIMIT), null)
        );
        DimensionMatcherPresets.instance().register("Crystallium Dimension(Arcana RPG)",
                new DimensionGenerationInfo(DimensionMatcher.ofTypes(String.valueOf(Dungeons.crystalliumPlainsDimensionId), DimensionDictionary.NO_TOP_LIMIT, DimensionDictionary.NO_BOTTOM_LIMIT), null)
        );
        DimensionMatcherPresets.instance().register("Sunset Dimension(Arcana RPG)",
                new DimensionGenerationInfo(DimensionMatcher.ofTypes(String.valueOf(Dungeons.sunsetDimensionId), DimensionDictionary.NO_TOP_LIMIT, DimensionDictionary.NO_BOTTOM_LIMIT), null)
        );
        DimensionMatcherPresets.instance().register("Montane Dimension(Arcana RPG)",
                new DimensionGenerationInfo(DimensionMatcher.ofTypes(String.valueOf(Dungeons.montaneDungeonDimensionId), DimensionDictionary.NO_TOP_LIMIT, DimensionDictionary.NO_BOTTOM_LIMIT), null)
        );
    }

    protected static void registerBiomePresets()
    {
        BiomeMatcherPresets.instance().register("clear");

        BiomeMatcherPresets.instance().register("overworld",
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.WATER), 0.0),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.PLAINS), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.FOREST), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.MOUNTAIN), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.HILLS), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.SWAMP), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.SANDY), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.MESA), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.SAVANNA), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.WASTELAND), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.MUSHROOM), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.JUNGLE), null));
        BiomeMatcherPresets.instance().setDefault("overworld");

        BiomeMatcherPresets.instance().register("underground",
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.PLAINS), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.FOREST), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.MOUNTAIN), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.HILLS), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.SWAMP), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.SANDY), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.MESA), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.SAVANNA), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.RIVER), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.OCEAN), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.WASTELAND), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.MUSHROOM), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.JUNGLE), null));

        BiomeMatcherPresets.instance().register("ocean",
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.OCEAN, BiomeDictionary.Type.SNOWY), 0.0),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.OCEAN), null));
    }

    protected static void registerBlockStatePresets()
    {
        WeightedBlockStatePresets.instance().register("clear");

        WeightedBlockStatePresets.instance().register("allWool",  IntStream.range(0, 16).mapToObj(i -> new WeightedBlockState(null, BlockStates.fromMetadata(Blocks.wool, i), "")).collect(Collectors.toList()));
        WeightedBlockStatePresets.instance().setDefault("allWool");
    }
}
