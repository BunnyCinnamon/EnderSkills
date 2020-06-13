package arekkuusu.enderskills.common.entity.data;

import arekkuusu.enderskills.common.lib.LibMod;
import net.minecraftforge.registries.DataSerializerEntry;
import net.minecraftforge.registries.IForgeRegistry;

public final class ModSerializer {

    public static void init(IForgeRegistry<DataSerializerEntry> registry) {
        registry.register(new DataSerializerEntry(WallSegmentBehaviorExtendedData.SERIALIZER).setRegistryName(LibMod.MOD_ID, "wall_segment_behavior"));
        registry.register(new DataSerializerEntry(ListBlockStateExtendedData.SERIALIZER).setRegistryName(LibMod.MOD_ID, "list_block_state"));
        registry.register(new DataSerializerEntry(ListBlockPosExtendedData.SERIALIZER).setRegistryName(LibMod.MOD_ID, "list_block_pos"));
        registry.register(new DataSerializerEntry(ListUUIDExtendedData.SERIALIZER).setRegistryName(LibMod.MOD_ID, "list_uuid"));
        registry.register(new DataSerializerEntry(SkillExtendedData.SERIALIZER).setRegistryName(LibMod.MOD_ID, "skill_data"));
    }
}
