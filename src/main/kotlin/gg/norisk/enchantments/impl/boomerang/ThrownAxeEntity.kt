package gg.norisk.enchantments.impl.boomerang

import gg.norisk.enchantments.sound.BoomerangSoundInstance
import gg.norisk.enchantments.utils.Animation
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.TridentEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import java.util.function.Supplier
import kotlin.time.Duration.Companion.seconds

//used https://github.com/ekulxam/axe_throw/blob/main/LICENSE
class ThrownAxeEntity : TridentEntity {
    private var slot = 0
    private var firstClientTicket = false

    constructor(entityType: EntityType<out TridentEntity?>, world: World?) : super(entityType, world)

    private constructor(world: World?, owner: LivingEntity, stack: ItemStack, slot: Int) : super(world, owner, stack) {
        this.setAttached<Long?>(AxeThrowAttachments.THROWN_AXE_TICKS_ACTIVE, 0L)
        this.setAttached<ItemStack?>(AxeThrowAttachments.THROWN_AXE_ITEM_STACK, stack)
        this.slot = slot
    }

    override fun tick() {
        if (this.getWorld().isClient && !firstClientTicket) {
            firstClientTicket = true
            MinecraftClient.getInstance().getSoundManager().play(BoomerangSoundInstance(this))
        }

        if (!this.isOnGround() && this.inGroundTime <= 0 && !this.horizontalCollision && !this.verticalCollision) {
            this.setAttached<Long?>(AxeThrowAttachments.THROWN_AXE_TICKS_ACTIVE, this.ticksActive + 1)
        }

        val owner = this.getOwner()
        // Boomerang logic: return after 15 ticks
        if (owner != null && owner.isAlive() && this.ticksActive > 15) {
            this.setNoClip(true)
            val vec3d = owner.getEyePos().subtract(this.getPos())
            // This logic is inspired by the returning of a trident with loyalty
            this.setVelocity(this.getVelocity().multiply(0.85).add(vec3d.normalize().multiply(0.4)))
        }

        super.tick()
    }

    val animation = Animation(0f, 360f, 0.4.seconds)

    fun getTicksActiveLerped(tickDelta: Float): Float {
        if (velocity.horizontalLengthSquared() <= 0) {
            return ticksActive * 15f
        }
        return animation.get().apply {
            if (animation.isDone) {
                animation.reset()
            }
        }
    }

    val ticksActive: Long
        get() = this.getAttachedOrCreate(AxeThrowAttachments.THROWN_AXE_TICKS_ACTIVE)

    override fun getDefaultItemStack(): ItemStack? {
        return DEFAULT_ITEM_STACK_SUPPLIER.get()
    }

    override fun tryPickup(player: PlayerEntity): Boolean {
        if (PickupPermission.CREATIVE_ONLY == this.pickupType) {
            return true
        }
        if (isOwner(player)) {
            val inventory = player.getInventory()
            try {
                val stack = inventory.getStack(this.slot)
                if (stack == null || stack.isEmpty()) {
                    inventory.setStack(this.slot, this.asItemStack())
                    return true
                }
                return inventory.insertStack(this.asItemStack())
            } catch (e: ArrayIndexOutOfBoundsException) {
                AxeThrow.LOGGER.error("An error occurred when picking up a thrown axe!", e)
                return inventory.insertStack(this.asItemStack())
            }
        }
        return super.tryPickup(player)
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        super.writeCustomDataToNbt(nbt)
        nbt.putInt(SLOT_KEY, this.slot)
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        super.readCustomDataFromNbt(nbt)
        if (nbt.contains(SLOT_KEY)) {
            this.slot = nbt.getInt(SLOT_KEY).orElse(0)
        }
    } /*@Override
    protected float getDragInWater() {
        return (float) ((ServerWorld) this.getWorld()).getGameRules().get(AxeThrowGameRules.PROJECTILE_DRAG_IN_WATER).get();
    }

    @Override
    protected SoundEvent getHitSound() {
        return AxeThrowSoundEvents.ITEM_THROWN_AXE_HIT_GROUND;
    }*/

    companion object {
        @JvmField
        val DEFAULT_ITEM_STACK_SUPPLIER: Supplier<ItemStack?> = Supplier { ItemStack(Items.DIAMOND_AXE) }
        const val SLOT_KEY: String = "shotFromInventorySlot"

        @JvmStatic
        fun fromOwnerAndItemStack(world: World?, owner: LivingEntity, stack: ItemStack, slot: Int): ThrownAxeEntity {
            AxeThrow.throwingAxeAndNotTrident = true
            val thrownAxe = ThrownAxeEntity(world, owner, stack, slot)
            AxeThrow.throwingAxeAndNotTrident = false
            return thrownAxe
        }
    }
}