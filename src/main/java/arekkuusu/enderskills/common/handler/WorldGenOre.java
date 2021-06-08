package arekkuusu.enderskills.common.handler;

import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.block.ModBlocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Arrays;
import java.util.Random;

public final class WorldGenOre implements IWorldGenerator {

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (Arrays.stream(CommonConfig.getSyncValues().worldGen.enderOreSpawnDimensions).anyMatch(i -> i == world.provider.getDimension())) {
            int veinSize = CommonConfig.getSyncValues().worldGen.enderOreQuantity;
            int maxHeight = CommonConfig.getSyncValues().worldGen.enderOreSpawnHeightMax;
            int minHeight = CommonConfig.getSyncValues().worldGen.enderOreSpawnHeightMin;
            for (int i = 0; i < CommonConfig.getSyncValues().worldGen.enderOreSpawnRate; i++) {
                if (random.nextGaussian() > CommonConfig.getSyncValues().worldGen.enderOreSpawnChance) return;
                WorldGenMinable gen = new WorldGenMinable(ModBlocks.ENDER_ORE.getDefaultState(), veinSize);
                int xRand = chunkX * 16 + random.nextInt(16);
                int yRand = random.nextInt(maxHeight - minHeight) + minHeight;
                int zRand = chunkZ * 16 + random.nextInt(16);
                gen.generate(world, random, new BlockPos(xRand, yRand, zRand));
            }
        }
    }
}
