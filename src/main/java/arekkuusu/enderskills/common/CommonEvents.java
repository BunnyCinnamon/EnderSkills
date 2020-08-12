package arekkuusu.enderskills.common;

import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.common.block.ModBlocks;
import arekkuusu.enderskills.common.entity.ModEntities;
import arekkuusu.enderskills.common.entity.data.ModSerializer;
import arekkuusu.enderskills.common.item.ModItems;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.potion.ModPotionTypes;
import arekkuusu.enderskills.common.potion.ModPotions;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.registries.DataSerializerEntry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nonnull;

@EventBusSubscriber(modid = LibMod.MOD_ID)
public final class CommonEvents {

    @SubscribeEvent
    public static void registerSkills(RegistryEvent.Register<Skill> event) {
        ModAttributes.register(event.getRegistry());
        ModAbilities.register(event.getRegistry());
        ModEffects.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        ModItems.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        ModBlocks.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        ModEntities.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerPotions(RegistryEvent.Register<Potion> event) {
        ModPotions.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerPotionTypes(RegistryEvent.Register<PotionType> event) {
        ModPotionTypes.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        ModSounds.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerRegistry(RegistryEvent.NewRegistry event) {
        RegistryBuilder<Skill> builder = new RegistryBuilder<>();
        builder.setType(Skill.class).set(new IForgeRegistry.DummyFactory<Skill>() {
            @Override
            public Skill createDummy(ResourceLocation resourceLocation) {
                return new Skill(new Skill.Properties()) {
                    @Nonnull
                    @Override
                    public SkillInfo createInfo(NBTTagCompound compound) {
                        return new SkillInfo(compound) {
                            @Override
                            public void writeNBT(NBTTagCompound compound) {

                            }

                            @Override
                            public void readNBT(NBTTagCompound compound) {

                            }
                        };
                    }
                };
            }
        }).set(new IForgeRegistry.MissingFactory<Skill>() {
            @Override
            public Skill createMissing(ResourceLocation resourceLocation, boolean b) {
                return new Skill(new Skill.Properties()) {
                    @Nonnull
                    @Override
                    public SkillInfo createInfo(NBTTagCompound compound) {
                        return new SkillInfo(compound) {
                            @Override
                            public void writeNBT(NBTTagCompound compound) {

                            }

                            @Override
                            public void readNBT(NBTTagCompound compound) {

                            }
                        };
                    }
                };
            }
        }).allowModification().setName(new ResourceLocation(LibMod.MOD_ID, "skills")).create();
    }

    @SubscribeEvent
    public static void registerSerializer(RegistryEvent.Register<DataSerializerEntry> event) {
        ModSerializer.init(event.getRegistry());
    }

    @SubscribeEvent
    public static void configChange(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(LibMod.MOD_ID)) {
            ConfigManager.sync(LibMod.MOD_ID, Config.Type.INSTANCE);
        }
    }
}
