package arekkuusu.enderskills.common.entity.data;

import arekkuusu.enderskills.common.entity.EntityWallSegment;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

public abstract class WallSegmentBehaviorExtendedData extends ExtendedData<WallSegmentBehaviorExtendedData> {

    public static final ExtendedDataSerializer<WallSegmentBehaviorExtendedData> SERIALIZER = new ExtendedDataSerializer<>();

    static {
        register(Drop.class);
        register(Rising.class);
        register(Waiting.class);
    }

    public abstract WallSegmentBehaviorExtendedData update(EntityWallSegment entity);

    @Override
    public void fromBytes(PacketBuffer buf) {
        //For Rent
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        //I'm Rent
    }

    public static class Drop extends WallSegmentBehaviorExtendedData {

        private int ticks = 0;

        @Override
        public WallSegmentBehaviorExtendedData update(EntityWallSegment entity) {
            if (ticks == 1) { //Add initial motion to move exactly x blocks
                entity.motionY = -((double) entity.getSize() / (5D));
            }
            ticks++;
            return this;
        }
    }

    public static class Rising extends WallSegmentBehaviorExtendedData {

        private int ticks = 0;

        @Override
        public WallSegmentBehaviorExtendedData update(EntityWallSegment entity) {
            if (ticks == 1) { //Add initial motion to move exactly x blocks
                entity.motionY = entity.getSize() / 5D;
            }
            entity.motionY = getCollided(entity, entity.motionY);
            ticks++;
            return ticks > 5 ? new Waiting() : this;
        }
    }

    public double getCollided(EntityWallSegment segment, double moveUp) {
        AxisAlignedBB ebb = segment.getEntityBoundingBox();
        AxisAlignedBB bb = new AxisAlignedBB(ebb.minX, ebb.minY + (double) segment.getSize() - 0.9D, ebb.minZ, ebb.maxX, ebb.maxY, ebb.maxZ);
        List<AxisAlignedBB> list = segment.world.getCollisionBoxes(segment, bb.expand(0, moveUp, 0));
        int k = 0;
        for (int l = list.size(); k < l; ++k) {
            moveUp = list.get(k).calculateYOffset(bb, moveUp);
        }
        return Math.max(moveUp, 0);
    }

    public static class Waiting extends WallSegmentBehaviorExtendedData {

        private int ticks = 0;

        @Override
        public WallSegmentBehaviorExtendedData update(EntityWallSegment entity) {
            ticks++;
            entity.motionY = 0;
            boolean drop = ticks >= entity.getDuration();
            return drop ? new Drop() : this;
        }
    }
}
