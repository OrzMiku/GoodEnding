package net.orcinus.goodending;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.reflect.Reflection;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.TagKey;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.VegetationPlacedFeatures;
import net.orcinus.goodending.init.GoodEndingBiomes;
import net.orcinus.goodending.init.GoodEndingBlocks;
import net.orcinus.goodending.init.GoodEndingEntityTypes;
import net.orcinus.goodending.init.GoodEndingFeatures;
import net.orcinus.goodending.init.GoodEndingItems;
import net.orcinus.goodending.init.GoodEndingPotions;
import net.orcinus.goodending.init.GoodEndingSoundEvents;
import net.orcinus.goodending.init.GoodEndingStatusEffects;
import net.orcinus.goodending.init.GoodEndingStructurePieceTypes;
import net.orcinus.goodending.init.GoodEndingStructureProcessors;
import net.orcinus.goodending.init.GoodEndingStructureTypes;
import net.orcinus.goodending.init.GoodEndingTags;
import net.orcinus.goodending.init.GoodEndingTreeDecorators;
import net.orcinus.goodending.init.GoodEndingWorldGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class GoodEnding implements ModInitializer {
	public static final String MODID = "goodending";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
	public static final ItemGroup TAB = FabricItemGroupBuilder.create(new Identifier(MODID, MODID)).icon(() -> new ItemStack(GoodEndingItems.YELLOW_FLOWERING_LILY_PAD)).build();

	@SuppressWarnings("UnstableApiUsage")
	@Override
	public void onInitialize() {

		Reflection.initialize(
			GoodEndingSoundEvents.class,
			GoodEndingItems.class,
			GoodEndingBlocks.class,
			GoodEndingStructureTypes.class,
			GoodEndingStructurePieceTypes.class,
			GoodEndingStructureProcessors.class
		);

		GoodEndingBiomes.init();
		GoodEndingFeatures.init();
		GoodEndingStatusEffects.init();
		GoodEndingTreeDecorators.init();
		GoodEndingPotions.init();

		Util.make(ImmutableMap.<RegistryEntry<PlacedFeature>, RegistryKey<Biome>>builder(), map -> {
			map.put(GoodEndingWorldGen.GRANITE_BOULDER_PLACED, BiomeKeys.SAVANNA);
			map.put(GoodEndingWorldGen.PLAINS_BOULDER_PLACED, BiomeKeys.PLAINS);
			map.put(GoodEndingWorldGen.DESERT_BOULDER_PLACED, BiomeKeys.DESERT);
			map.put(GoodEndingWorldGen.BADLANDS_BOULDER_PLACED, BiomeKeys.BADLANDS);
			
			map.put(GoodEndingWorldGen.BASALT_BOULDER_PLACED, BiomeKeys.WARM_OCEAN);
			map.put(GoodEndingWorldGen.TUFF_BOULDER_PLACED, BiomeKeys.COLD_OCEAN);

		}).build().forEach((featureEntry, biomeKey) -> featureEntry.getKey().ifPresent(featureKey -> BiomeModifications.addFeature(BiomeSelectors.includeByKey(biomeKey), GenerationStep.Feature.LOCAL_MODIFICATIONS, featureKey)));

		Util.make(ImmutableMap.<RegistryEntry<PlacedFeature>, GenerationStep.Feature>builder(), map -> {
			map.put(GoodEndingWorldGen.PATCH_ALGAE_PLACED, GenerationStep.Feature.UNDERGROUND_ORES);
			map.put(GoodEndingWorldGen.BIG_LILY_PADS_PLACED, GenerationStep.Feature.VEGETAL_DECORATION);
			map.put(GoodEndingWorldGen.PATCH_FLOWERING_WATERLILY_PLACED, GenerationStep.Feature.VEGETAL_DECORATION);
			map.put(GoodEndingWorldGen.DISK_PODZOL_PLACED, GenerationStep.Feature.UNDERGROUND_ORES);
			map.put(GoodEndingWorldGen.MARSH_SWAMP_TREE_PLACED, GenerationStep.Feature.VEGETAL_DECORATION);
			map.put(GoodEndingWorldGen.MARSHY_SWAMP_VEGETATION, GenerationStep.Feature.VEGETAL_DECORATION);
		}).build().forEach((featureEntry, feature) -> featureEntry.getKey().ifPresent(featureKey -> BiomeModifications.addFeature(BiomeSelectors.includeByKey(GoodEndingBiomes.MARSHY_SWAMP_KEY), feature, featureKey)));

		Util.make(ImmutableMap.<RegistryEntry<PlacedFeature>, GenerationStep.Feature>builder(), map -> {
			map.put(GoodEndingWorldGen.OAK_HAMMOCK_TREES_PLACED, GenerationStep.Feature.VEGETAL_DECORATION);
			map.put(GoodEndingWorldGen.OAK_HAMMOCK_PATCH_TALL_GRASS_PLACED, GenerationStep.Feature.VEGETAL_DECORATION);
			map.put(GoodEndingWorldGen.PATCH_FERN_PLACED, GenerationStep.Feature.VEGETAL_DECORATION);
			map.put(GoodEndingWorldGen.PATCH_LARGE_FERN_PLACED, GenerationStep.Feature.VEGETAL_DECORATION);
			map.put(GoodEndingWorldGen.MOSSY_BOULDER_PLACED, GenerationStep.Feature.LOCAL_MODIFICATIONS);
		}).build().forEach((featureEntry, feature) -> featureEntry.getKey().ifPresent(featureKey -> BiomeModifications.addFeature(BiomeSelectors.includeByKey(GoodEndingBiomes.OAK_HAMMOCK_FOREST_KEY), feature, featureKey)));

		Util.make(ImmutableMap.<RegistryEntry<PlacedFeature>, GenerationStep.Feature>builder(), map -> {
			map.put(GoodEndingWorldGen.SHALLOW_WATER_MUD_PLACED, GenerationStep.Feature.UNDERGROUND_ORES);
			map.put(GoodEndingWorldGen.CATTAIL_PATCH_PLACED, GenerationStep.Feature.VEGETAL_DECORATION);
			map.put(GoodEndingWorldGen.SWAMP_FALLEN_LOG_PLACED, GenerationStep.Feature.LOCAL_MODIFICATIONS);
			map.put(GoodEndingWorldGen.CYPRESS_TREE_PLACED, GenerationStep.Feature.VEGETAL_DECORATION);
			map.put(GoodEndingWorldGen.SWAMP_VEGETATION_PLACED, GenerationStep.Feature.VEGETAL_DECORATION);
			map.put(GoodEndingWorldGen.DUCKWEED_PATCH_PLACED, GenerationStep.Feature.VEGETAL_DECORATION);
		}).build().forEach((featureEntry, feature) -> featureEntry.getKey().ifPresent(featureKey -> BiomeModifications.addFeature(BiomeSelectors.includeByKey(BiomeKeys.SWAMP), feature, featureKey)));

		Util.make(ImmutableMap.<TagKey<Biome>, RegistryEntry<PlacedFeature>>builder(), map -> {
			map.put(GoodEndingTags.PASTEL_WILDFLOWER_GENERATES, GoodEndingWorldGen.PATCH_PASTEL_WILDFLOWERS_PLACED);
			map.put(GoodEndingTags.TWILIGHT_WILDFLOWER_GENERATES, GoodEndingWorldGen.PATCH_TWILIGHT_WILDFLOWERS_PLACED);
			map.put(GoodEndingTags.SPICY_WILDFLOWER_GENERATES, GoodEndingWorldGen.PATCH_SPICY_WILDFLOWERS_PLACED);
			map.put(GoodEndingTags.BALMY_WILDFLOWER_GENERATES, GoodEndingWorldGen.PATCH_BALMY_WILDFLOWERS_PLACED);
		}).build().forEach((biomeTagKey, featureEntry) -> featureEntry.getKey().ifPresent(featureKey -> BiomeModifications.addFeature(BiomeSelectors.tag(biomeTagKey), GenerationStep.Feature.VEGETAL_DECORATION, featureKey)));

		this.addFeatureToBiome(GoodEndingWorldGen.PATCH_PINK_FLOWERED_LILY_PLACED, BiomeKeys.MANGROVE_SWAMP);

		Util.make(ImmutableMap.<RegistryKey<Biome>, RegistryEntry<PlacedFeature>>builder(), map -> {
			map.put(BiomeKeys.FOREST, GoodEndingWorldGen.OAK_FALLEN_LOG_PLACED);
			map.put(BiomeKeys.WOODED_BADLANDS, GoodEndingWorldGen.OAK_FALLEN_LOG_PLACED);
			map.put(BiomeKeys.TAIGA, GoodEndingWorldGen.SPRUCE_FALLEN_LOG_PLACED);
			map.put(BiomeKeys.SNOWY_TAIGA, GoodEndingWorldGen.SPRUCE_FALLEN_LOG_PLACED);
			map.put(BiomeKeys.WINDSWEPT_FOREST, GoodEndingWorldGen.SPRUCE_FALLEN_LOG_PLACED);
			map.put(BiomeKeys.GROVE, GoodEndingWorldGen.SPRUCE_FALLEN_LOG_PLACED);
			map.put(BiomeKeys.SAVANNA, GoodEndingWorldGen.ACACIA_FALLEN_LOG_PLACED);
			map.put(BiomeKeys.BIRCH_FOREST, GoodEndingWorldGen.BIRCH_FALLEN_LOG_PLACED);
			map.put(BiomeKeys.OLD_GROWTH_BIRCH_FOREST, GoodEndingWorldGen.BIRCH_FALLEN_LOG_PLACED);
		}).build().forEach((biome, worldgen) -> worldgen.getKey().ifPresent(featureKey -> BiomeModifications.addFeature(BiomeSelectors.includeByKey(biome), GenerationStep.Feature.LOCAL_MODIFICATIONS, featureKey)));

		GoodEndingWorldGen.BIRCH_FALLEN_LOG_PLACED.getKey().ifPresent(featureKey -> BiomeModifications.addFeature(BiomeSelectors.includeByKey(BiomeKeys.FOREST), GenerationStep.Feature.LOCAL_MODIFICATIONS, featureKey));

		this.addFeatureToBiome(GoodEndingWorldGen.PATCH_TALL_GRASS_PLACED, BiomeKeys.OLD_GROWTH_BIRCH_FOREST);

		BiomeModifications.create(new Identifier(GoodEnding.MODID, "replace_swamp_trees")).add(ModificationPhase.REPLACEMENTS, BiomeSelectors.includeByKey(BiomeKeys.SWAMP), biomeModificationContext -> {
			Optional<RegistryKey<PlacedFeature>> key = GoodEndingWorldGen.SWAMP_TREE_PLACED.getKey();
			if (key.isPresent() && biomeModificationContext.getGenerationSettings().removeBuiltInFeature(VegetationPlacedFeatures.TREES_SWAMP.value())) {
				biomeModificationContext.getGenerationSettings().addFeature(GenerationStep.Feature.VEGETAL_DECORATION, key.get());
			}
		});

		BiomeModifications.create(new Identifier(GoodEnding.MODID, "remove_small_birch_trees")).add(ModificationPhase.REPLACEMENTS, BiomeSelectors.includeByKey(BiomeKeys.OLD_GROWTH_BIRCH_FOREST), biomeModificationContext -> {
			Optional<RegistryKey<PlacedFeature>> key = GoodEndingWorldGen.TALL_BIRCH_VEGETATION_PLACED.getKey();
			if (key.isPresent() && biomeModificationContext.getGenerationSettings().removeBuiltInFeature(VegetationPlacedFeatures.BIRCH_TALL.value())) {
				biomeModificationContext.getGenerationSettings().addFeature(GenerationStep.Feature.VEGETAL_DECORATION, key.get());
			}
		});

		BiomeModifications.addSpawn(BiomeSelectors.includeByKey(BiomeKeys.SWAMP), SpawnGroup.AMBIENT, GoodEndingEntityTypes.FIREFLY_SWARM, 20, 8, 8);
		BiomeModifications.addSpawn(BiomeSelectors.includeByKey(GoodEndingBiomes.MARSHY_SWAMP_KEY), SpawnGroup.AMBIENT, GoodEndingEntityTypes.FIREFLY_SWARM, 20, 8, 8);
		BiomeModifications.addSpawn(BiomeSelectors.includeByKey(GoodEndingBiomes.MARSHY_SWAMP_KEY), SpawnGroup.CREATURE, EntityType.FROG, 10, 2, 5);

		BiomeModifications.addSpawn(BiomeSelectors.includeByKey(BiomeKeys.OLD_GROWTH_BIRCH_FOREST), SpawnGroup.CREATURE, GoodEndingEntityTypes.WOODPECKER, 40, 1, 4);

		StrippableBlockRegistry.register(GoodEndingBlocks.MUDDY_OAK_LOG, GoodEndingBlocks.STRIPPED_MUDDY_OAK_LOG);
		StrippableBlockRegistry.register(GoodEndingBlocks.MUDDY_OAK_WOOD, GoodEndingBlocks.STRIPPED_MUDDY_OAK_WOOD);
		StrippableBlockRegistry.register(GoodEndingBlocks.CYPRESS_LOG, GoodEndingBlocks.STRIPPED_CYPRESS_LOG);
		StrippableBlockRegistry.register(GoodEndingBlocks.CYPRESS_WOOD, GoodEndingBlocks.STRIPPED_CYPRESS_WOOD);

		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			BlockPos blockPos = hitResult.getBlockPos();
			ItemStack stack = player.getStackInHand(hand);
			if (world.getBlockState(blockPos).isOf(Blocks.LILY_PAD) && stack.isOf(Items.BONE_MEAL) && !world.isClient()) {
				if (!player.getAbilities().creativeMode) {
					stack.decrement(1);
				}
				List<Block> list = Util.make(Lists.newArrayList(), block -> {
					block.add(GoodEndingBlocks.PURPLE_FLOWERING_LILY_PAD);
					block.add(GoodEndingBlocks.PINK_FLOWERING_LILY_PAD);
					block.add(GoodEndingBlocks.YELLOW_FLOWERING_LILY_PAD);
				});
				world.setBlockState(blockPos, list.get(world.getRandom().nextInt(list.size())).getDefaultState(), 2);
				world.playSound(null, blockPos, SoundEvents.ITEM_BONE_MEAL_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
				world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, blockPos, 0);
				return ActionResult.SUCCESS;
			}
			return ActionResult.PASS;
		});



	}

	private void addFeatureToBiome(RegistryEntry<PlacedFeature> featureEntry, RegistryKey<Biome> biomeKey) {
		featureEntry.getKey().ifPresent(featureKey -> BiomeModifications.addFeature(BiomeSelectors.includeByKey(biomeKey), GenerationStep.Feature.VEGETAL_DECORATION, featureKey));
	}
}
