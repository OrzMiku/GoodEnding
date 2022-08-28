package net.orcinus.goodending.entities;

import net.minecraft.block.BlockState;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Flutterer;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FlyGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.orcinus.goodending.entities.ai.AttachToWood;
import net.orcinus.goodending.init.GoodEndingSoundEvents;
import org.jetbrains.annotations.Nullable;

public class WoodpeckerEntity extends PathAwareEntity implements Flutterer {
    public float flapProgress;
    public float maxWingDeviation;
    public float prevMaxWingDeviation;
    public float prevFlapProgress;
    private float flapSpeed = 1.0f;
    private float field_28640 = 1.0f;
    protected static final TrackedData<Direction> ATTACHED_FACE = DataTracker.registerData(WoodpeckerEntity.class, TrackedDataHandlerRegistry.FACING);
    private static final TrackedData<Boolean> ATTACHED_WOOD = DataTracker.registerData(WoodpeckerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private boolean isAttachedWood;
    public final AnimationState woodpecker_peck = new AnimationState();
    public final AnimationState standing = new AnimationState();

    public WoodpeckerEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
        this.moveControl = new FlightMoveControl(this, 10, false);
        this.setPathfindingPenalty(PathNodeType.DANGER_FIRE, -1.0f);
        this.setPathfindingPenalty(PathNodeType.DAMAGE_FIRE, -1.0f);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.world.isClient()) {
            boolean attachedWood = this.isAttachedWood();
             if (attachedWood) {
//                this.standing.startIfNotRunning(this.age);
                this.woodpecker_peck.startIfNotRunning(this.age);
            } else {
//                this.standing.stop();
                this.woodpecker_peck.stop();
            }
        }
    }

    private boolean shouldWalk() {
        return this.onGround && this.getVelocity().horizontalLengthSquared() > 1.0E-6 && !this.isInsideWaterOrBubbleColumn();
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(ATTACHED_FACE, Direction.DOWN);
        this.dataTracker.startTracking(ATTACHED_WOOD, false);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new EscapeDangerGoal(this, 1.25));
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new FlyOntoBranchGoal(this, 1.0f));
        this.goalSelector.add(1, new AttachToWood(this));
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 1.0f));
        this.goalSelector.add(2, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
    }

    public static DefaultAttributeContainer.Builder createWoodPeckerAttributes() {
        return MobEntity.createMobAttributes()
                        .add(EntityAttributes.GENERIC_MAX_HEALTH, 6.0)
                        .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.4f)
                        .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2f);
    }

    @Override
    public void setVelocity(Vec3d velocity) {
        if (this.isAttachedWood()) {
            return;
        }
        super.setVelocity(velocity);
    }

    @Override
    public Vec3d getVelocity() {
        return this.isAttachedWood() ? Vec3d.ZERO : super.getVelocity();
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        BirdNavigation birdNavigation = new BirdNavigation(this, world);
        birdNavigation.setCanPathThroughDoors(false);
        birdNavigation.setCanSwim(true);
        birdNavigation.setCanEnterOpenDoors(true);
        return birdNavigation;
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setAttachedFace(Direction.byId(nbt.getByte("AttachFace")));
        this.setAttachedWood(nbt.getBoolean("AttachedToWood"));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putByte("AttachFace", (byte)this.getAttachedFace().getId());
        nbt.putBoolean("AttachedToWood", this.isAttachedWood());
    }

    public boolean isAttachedWood() {
        return this.dataTracker.get(ATTACHED_WOOD);
    }

    public void setAttachedWood(boolean isAttachedWood) {
        this.dataTracker.set(ATTACHED_WOOD, isAttachedWood);
    }

    public Direction getAttachedFace() {
        Direction direction = this.dataTracker.get(ATTACHED_FACE);
        return direction;
    }

    public void setAttachedFace(Direction face) {
        this.dataTracker.set(ATTACHED_FACE, face);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        this.flapWings();
        if (!this.isNavigating()) this.setVelocity(this.getVelocity().multiply(1.0, 0.6, 1.0));
    }

    @Override
    public boolean isInAir() {
        return false;
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
    }


    private void flapWings() {
        this.prevFlapProgress = this.flapProgress;
        this.prevMaxWingDeviation = this.maxWingDeviation;
        this.maxWingDeviation += (float)(this.onGround || this.hasVehicle() ? -1 : 4) * 0.3f;
        this.maxWingDeviation = MathHelper.clamp(this.maxWingDeviation, 0.0f, 1.0f);
        if (!this.onGround && this.flapSpeed < 1.0f) {
            this.flapSpeed = 1.0f;
        }
        this.flapSpeed *= 0.9f;
        Vec3d vec3d = this.getVelocity();
        if (!this.onGround && vec3d.y < 0.0) {
            this.setVelocity(vec3d.multiply(1.0, 0.6, 1.0));
        }
        this.flapProgress += this.flapSpeed * 2.0f;
    }

    @Override
    protected boolean hasWings() {
        return this.speed > this.field_28640;
    }


    @Override
    protected float getSoundVolume() {
        return 0.5f;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return GoodEndingSoundEvents.ENTITY_WOODPECKER_IDLE;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return GoodEndingSoundEvents.ENTITY_WOODPECKER_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return GoodEndingSoundEvents.ENTITY_WOODPECKER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ENTITY_PARROT_STEP, 0.15f, 1.0f);
    }

    @Override
    protected void addFlapEffects() {
        this.playSound(GoodEndingSoundEvents.ENTITY_WOODPECKER_FLY, 0.15f, 1.0f);
        this.field_28640 = this.speed + this.maxWingDeviation / 2.0f;
    }

    static class FlyOntoBranchGoal extends FlyGoal {
        public FlyOntoBranchGoal(PathAwareEntity pathAwareEntity, double d) {
            super(pathAwareEntity, d);
        }

        @Override
        @Nullable
        protected Vec3d getWanderTarget() {
            Vec3d vec3d = null;
            if (this.mob.isTouchingWater()) {
                vec3d = FuzzyTargeting.find(this.mob, 15, 25);
            }
            if (this.mob.getRandom().nextFloat() >= this.probability) {
                vec3d = this.locateBranch();
            }
            return vec3d == null ? super.getWanderTarget() : vec3d;
        }

        @Nullable
        private Vec3d locateBranch() {
            BlockPos blockPos = this.mob.getBlockPos();
            BlockPos.Mutable mutable = new BlockPos.Mutable();
            BlockPos.Mutable mutable2 = new BlockPos.Mutable();
            Iterable<BlockPos> iterable = BlockPos.iterate(MathHelper.floor(this.mob.getX() - 3.0), MathHelper.floor(this.mob.getY() - 6.0), MathHelper.floor(this.mob.getZ() - 3.0), MathHelper.floor(this.mob.getX() + 3.0), MathHelper.floor(this.mob.getY() + 6.0), MathHelper.floor(this.mob.getZ() + 3.0));
            for (BlockPos blockPos2 : iterable) {
                BlockPos blockPos3 = new BlockPos(blockPos2.getX() + 0.5D, blockPos2.getY() + 1, blockPos2.getZ() + 0.5D) ;
                if (blockPos.equals(blockPos2) || !this.mob.world.getBlockState(mutable2.set(blockPos2, Direction.DOWN)).isIn(BlockTags.LOGS) || !this.mob.world.isAir(blockPos2) || !this.mob.world.isAir(mutable.set(blockPos2, Direction.UP))) continue;
                return Vec3d.of(blockPos3);
            }
            return null;
        }
    }
}
