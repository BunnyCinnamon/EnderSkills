package arekkuusu.enderskills.common.block.tile;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class TileBase extends TileEntity {

    public boolean isClientWorld() {
        return world.isRemote;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    <T extends Comparable<T>> Optional<T> getStateValue(IProperty<T> property, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getPropertyKeys().contains(property) ? Optional.of(state.getValue(property)) : Optional.empty();
    }

    <T> Optional<T> getCapability(IBlockAccess world, BlockPos pos, @Nullable EnumFacing facing, Capability<T> capability) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile != null && tile.hasCapability(capability, facing)) {
            return Optional.ofNullable(tile.getCapability(capability, facing));
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    <T extends TileEntity> Optional<T> getTile(Class<T> clazz, IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        return clazz.isInstance(tile) ? Optional.of((T) tile) : Optional.empty();
    }

    @Override
    public double getDistanceSq(double x, double y, double z) {
        return Math.sqrt(super.getDistanceSq(x, y, z));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        NBTTagCompound tag = pkt.getNbtCompound();
        super.readFromNBT(tag);
        this.readSync(tag);
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = new NBTTagCompound();
        super.writeToNBT(tag);
        this.writeSync(tag);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        readNBT(tag);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);
        writeNBT(tag);
        return tag;
    }

    void readNBT(NBTTagCompound compound) {
        //ON RENT
    }

    void writeNBT(NBTTagCompound compound) {
        //ON RENT
    }

    public final void sync() {
        if (!world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 2);
        }
    }

    void writeSync(NBTTagCompound compound) {
        writeNBT(compound);
    }

    @SideOnly(Side.CLIENT)
    void readSync(NBTTagCompound compound) {
        readNBT(compound);
    }
}
