package arekkuusu.enderskills.common.network;

import arekkuusu.enderskills.common.EnderSkills;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class ClientToServerPacket implements IMessage {

    private NBTTagCompound data;
    private IPacketHandler handler;

    public ClientToServerPacket() {

    }

    public ClientToServerPacket(IPacketHandler handler, NBTTagCompound data) {
        this.handler = handler;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer beef = new PacketBuffer(buf);
        try {
            handler = PacketHandler.HANDLERS.get(buf.readInt());
            data = beef.readCompoundTag();
        } catch (Exception e) {
            EnderSkills.LOG.error("[Packet] Failed to receive packet");
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer beef = new PacketBuffer(buf);
        try {
            buf.writeInt(PacketHandler.HANDLERS.indexOf(handler));
            beef.writeCompoundTag(data);
        } catch (Exception e) {
            EnderSkills.LOG.error("[Packet] Failed to send packet");
            e.printStackTrace();
        }
    }

    public static class Handler implements IMessageHandler<ClientToServerPacket, IMessage> {

        @Override
        @Nullable
        @SuppressWarnings("ConstantConditions")
        public IMessage onMessage(ClientToServerPacket message, MessageContext ctx) {
            ctx.getServerHandler().player.getServer().addScheduledTask(() -> {
                if (message.data != null && message.handler != null) {
                    message.handler.handleData(message.data, ctx);
                }
            });
            return null;
        }
    }
}
