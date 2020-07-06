package arekkuusu.enderskills.common.entity.data;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.common.ES;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public class SkillExtendedData extends ExtendedData<SkillExtendedData> implements INBTSerializable<NBTTagCompound> {

    public static final ExtendedDataSerializer<SkillExtendedData> SERIALIZER = new ExtendedDataSerializer<>();
    public SkillData data;

    static {
        register(SkillExtendedData.class);
    }

    public SkillExtendedData() {
    }

    public SkillExtendedData(@Nullable SkillData data) {
        this.data = data;
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        PacketBuffer beef = new PacketBuffer(buf);
        try {
            if (beef.readBoolean()) {
                data = new SkillData(beef.readCompoundTag());
            } else {
                data = null;
            }
        } catch (Exception e) {
            ES.LOG.error("[Packet] Failed to receive packet");
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        PacketBuffer beef = new PacketBuffer(buf);
        try {
            if (data != null) {
                beef.writeBoolean(true);
                beef.writeCompoundTag(data.serializeNBT());
            } else {
                beef.writeBoolean(false);
            }
        } catch (Exception e) {
            ES.LOG.error("[Packet] Failed to send packet");
            e.printStackTrace();
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        if (data != null) {
            return data.serializeNBT();
        } else {
            return new NBTTagCompound();
        }
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (!nbt.hasNoTags()) {
            data = new SkillData(nbt);
        } else {
            data = null;
        }
    }
}
