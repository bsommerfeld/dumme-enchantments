package gg.norisk.enchantments.impl

import com.jogamp.opengl.math.FloatUtil.pow
import com.mojang.brigadier.context.CommandContext
import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.registeredTypes
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.datatracker.entity.syncedValueChangeEvent
import gg.norisk.enchantments.EnchantmentRegistry.helicopterV2
import gg.norisk.enchantments.EnchantmentUtils.getLevel
import gg.norisk.enchantments.StupidEnchantments.MOD_ID
import gg.norisk.enchantments.StupidEnchantments.toId
import gg.norisk.enchantments.command.EnchantmentsCommand.default
import gg.norisk.enchantments.command.EnchantmentsCommand.getEntry
import gg.norisk.enchantments.sound.FlyingSoundInstance
import gg.norisk.enchantments.sound.HelicopterSoundInstanceV2
import gg.norisk.utils.events.KeyEvents
import gg.norisk.utils.ext.EntityRenderStateExt
import kotlinx.serialization.Serializable
import me.x150.geckoAnimLib.core.ModelPartTransform
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.model.PlayerEntityModel
import net.minecraft.client.render.entity.state.LivingEntityRenderState
import net.minecraft.client.render.entity.state.PlayerEntityRenderState
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.MovementType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d
import net.silkmc.silk.commands.command
import net.silkmc.silk.core.entity.modifyVelocity
import net.silkmc.silk.core.item.itemStack
import net.silkmc.silk.core.text.literal
import net.silkmc.silk.network.packet.c2sPacket
import org.joml.Quaternionf
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.invoke.arg.Args

/**
 * Generische Klasse für Helicopter Movement Daten
 */
@Serializable
data class HelicopterMovement(
    val isFlying: Boolean = false,
    val rotationSpeed: Float = 5.0f, // Schnellere Rotation: 15°/tick statt 5°
    val currentRotation: Float = 0.0f,
    val prevRotation: Float = 0.0f, // Für Lerping
    val rotationPower: Float = 0.0f, // 0-100% Power für Motor-Start
    val powerPresses: Int = 0, // Anzahl Presses für exponentielles Wachstum
    val isBoostPressed: Boolean = false,
    val boostForce: Double = 0.3, // Erhöht von 0.1 auf 0.3 für bessere Sichtbarkeit
    val maxAltitude: Double = 20.0,
    val minAltitude: Double = 0.0
) {
    fun withBoost(pressed: Boolean): HelicopterMovement = copy(isBoostPressed = pressed)
    fun withFlying(flying: Boolean): HelicopterMovement = copy(isFlying = flying)
    fun withRotation(rotation: Float): HelicopterMovement =
        copy(prevRotation = currentRotation, currentRotation = rotation)

    fun withRotationPower(power: Float): HelicopterMovement = copy(rotationPower = power.coerceIn(0f, 100f))
    fun withPowerPresses(presses: Int): HelicopterMovement = copy(powerPresses = presses)

    /**
     * Berechnet Power-Gain für nächsten Space-Press mit konfigurierbarem Easing
     */
    fun getNextPowerGain(): Float {
        return when (PowerConfig.EASING_TYPE) {
            PowerConfig.EasingType.LINEAR -> {
                PowerConfig.BASE_POWER_GAIN + (powerPresses * PowerConfig.LINEAR_DIFFICULTY_SCALING)
            }

            PowerConfig.EasingType.EXPONENTIAL -> {
                PowerConfig.BASE_POWER_GAIN * Math.pow(PowerConfig.EXPONENTIAL_BASE.toDouble(), powerPresses.toDouble())
                    .toFloat()
            }

            PowerConfig.EasingType.EASE_IN_CUBIC -> {
                val t = (powerPresses.toFloat() / PowerConfig.MAX_PRESSES_FOR_EASING).coerceIn(0f, 1f)
                val easedValue = t * t * t // cubic easing
                PowerConfig.BASE_POWER_GAIN + (easedValue * PowerConfig.EASING_MAX_GAIN)
            }

            PowerConfig.EasingType.EASE_OUT_QUAD -> {
                val t = (powerPresses.toFloat() / PowerConfig.MAX_PRESSES_FOR_EASING).coerceIn(0f, 1f)
                val easedValue = 1f - (1f - t) * (1f - t) // quad ease-out
                PowerConfig.BASE_POWER_GAIN + (easedValue * PowerConfig.EASING_MAX_GAIN)
            }

            PowerConfig.EasingType.CUSTOM_SMOOTH -> {
                // Custom smooth curve - langsamer Start, dann beschleunigung, dann flach
                val t = (powerPresses.toFloat() / PowerConfig.MAX_PRESSES_FOR_EASING).coerceIn(0f, 1f)
                val smoothed = t * t * (3f - 2f * t) // smoothstep
                PowerConfig.BASE_POWER_GAIN + (smoothed * PowerConfig.EASING_MAX_GAIN)
            }
        }
    }

    /**
     * Berechnet Schwierigkeits-Multiplikator für nächsten Press
     */
    fun getDifficultyMultiplier(): Float {
        return 1f + (powerPresses * PowerConfig.DIFFICULTY_MULTIPLIER_PER_PRESS)
    }

    /**
     * Berechnet aktuelle Rotationsgeschwindigkeit basierend auf Power
     */
    fun getCurrentRotationSpeed(): Float {
        if (isFlying) return rotationSpeed
        // Startup mit konfigurierbarem Multiplikator
        return (rotationPower / 100f) * rotationSpeed * PowerConfig.STARTUP_ROTATION_MULTIPLIER
    }

    /**
     * Interpoliert Rotation für smooth rendering
     */
    fun getLerpedRotation(tickDelta: Float): Float {
        return prevRotation + (currentRotation - prevRotation) * tickDelta
    }

    /**
     * Prüft ob Motor bereit zum Starten ist
     */
    fun canStartMotor(): Boolean = rotationPower >= PowerConfig.STARTUP_THRESHOLD

    companion object {
        // Power-Konfiguration für einfaches Tuning
        object PowerConfig {
            // === EASING TYPE ===
            enum class EasingType {
                LINEAR,
                EXPONENTIAL,
                EASE_IN_CUBIC,
                EASE_OUT_QUAD,
                CUSTOM_SMOOTH
            }

            var EASING_TYPE = EasingType.CUSTOM_SMOOTH // <- Hier ändern für verschiedene Curves!

            // === BASIC SETTINGS ===
            var BASE_POWER_GAIN = 2f // Basis Power pro Press (war 1f)
            var STARTUP_THRESHOLD = 70f // Ab wieviel % Motor startet
            var POWER_DECAY_PER_TICK = 0.8f // Wie schnell Power sinkt (war 1.2f - jetzt langsamer!)
            var STARTUP_ROTATION_MULTIPLIER = 3f // Rotation-Speed während Startup

            // === LINEAR EASING ===
            var LINEAR_DIFFICULTY_SCALING = 0.8f // +Wieviel pro Press (war 1.25f)

            // === EXPONENTIAL EASING ===
            var EXPONENTIAL_BASE = 1.15f // Exponential-Basis (1.15^n)

            // === SMOOTH EASING ===
            var MAX_PRESSES_FOR_EASING = 15f // Anzahl Presses für volle Curve
            var EASING_MAX_GAIN = 8f // Maximum zusätzlicher Gain bei Easing

            // === DIFFICULTY ===
            var DIFFICULTY_MULTIPLIER_PER_PRESS = 0.1f // Schwierigkeit pro Press (war 0.2f)
        }

        const val STARTUP_THRESHOLD = 70f // Legacy - nutze PowerConfig.STARTUP_THRESHOLD
        const val POWER_DECAY_PER_TICK = 0.7f // Legacy - nutze PowerConfig.POWER_DECAY_PER_TICK
    }
}

/**
 * Input Packet - nur Space Input
 */
@Serializable
data class HelicopterInputPacket(
    val spacePressed: Boolean
)

object HelicopterEnchantmentV2 {

    private val helicopterInputPacket = c2sPacket<HelicopterInputPacket>("helicopter-input".toId())

    fun initServer() {
        // Registriere den HelicopterMovement Serializer
        (registeredTypes as MutableMap<Any, Any>).put(
            HelicopterMovement::class,
            HelicopterMovement.serializer(),
        )

        command("enchantments") {
            literal("helicopterv2") {
                runs {
                    this.default()
                    this.helicopter()
                }
            }
        }

        // Packet Handler - Space Input für Power-System
        helicopterInputPacket.receiveOnServer { packet, context ->
            val player = context.player
            val helmetStack = player.getEquippedStack(EquipmentSlot.HEAD)
            if (helicopterV2.getLevel(helmetStack) != null) {
                val movement = player.helicopterMovement

                if (packet.spacePressed) {
                    player.helicopterMovement = movement.withBoost(true)

                    // Power hinzufügen nur wenn nicht fliegend
                    if (!movement.isFlying) {
                        val powerGain = movement.getNextPowerGain()
                        val newPower = movement.rotationPower + powerGain
                        val newPresses = movement.powerPresses + 1

                        player.helicopterMovement = movement
                            .withBoost(true)
                            .withRotationPower(newPower)
                            .withPowerPresses(newPresses)

                        // Debug-Nachricht mit dynamischen Werten
                        val nextGain = movement.getNextPowerGain()
                        val difficulty = movement.getDifficultyMultiplier()
                        player.sendMessage(
                            "§7Power: §a${newPower.toInt()}% §8(+${powerGain.toInt()}%) §7Next: §c${nextGain.toInt()}% §8(${
                                String.format(
                                    "%.1f",
                                    difficulty
                                )
                            }x)".literal, true
                        )
                    }
                } else {
                    player.helicopterMovement = movement.withBoost(false)
                }
            }
        }

        ServerTickEvents.END_WORLD_TICK.register { world ->
            for (player in world.players) {
                if (player is ServerPlayerEntity) {
                    player.handleHelicopterMovement()
                }
            }
        }
    }

    fun initClient() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            val player = client.player ?: return@register
            player.handleHelicopterInput()
        }

        // Sound Event Listener für HelicopterV2
        syncedValueChangeEvent.listen { event ->
            if (event.key == "$MOD_ID:IsHelicopterMotorRunning") {
                if (!event.entity.world.isClient) return@listen
                if (event.entity.isHelicopterMotorRunning) {
                    // Sound starten bei 5%+ Power
                    MinecraftClient.getInstance().soundManager.play(HelicopterSoundInstanceV2(event.entity))
                }
            }

            if (event.key == "$MOD_ID:IsHelicopterFlying") {
                if (!event.entity.world.isClient) return@listen
                if (event.entity.isHelicopterFlying) {
                    MinecraftClient.getInstance().soundManager.play(
                        FlyingSoundInstance(
                            event.entity as ClientPlayerEntity,
                            {
                                event.entity.isHelicopterFlying
                            })
                    )
                }
            }
        }

        // Reaktive Key Events - nur Input senden
        KeyEvents.keyEvent.listen { keyEvent ->
            val player = MinecraftClient.getInstance().player ?: return@listen
            val helmetStack = player.getEquippedStack(EquipmentSlot.HEAD)
            if (helicopterV2.getLevel(helmetStack) == null) return@listen

            // Nur Space Input an Server senden
            if (keyEvent.matchesKeyBinding(MinecraftClient.getInstance().options.jumpKey)) {
                when (keyEvent.action) {
                    1 -> {
                        helicopterInputPacket.send(HelicopterInputPacket(true))  // Press
                    }

                    0 -> {
                        helicopterInputPacket.send(HelicopterInputPacket(false)) // Release
                    }
                }
            }
        }
    }

    private fun ServerPlayerEntity.handleHelicopterMovement() {
        val helmetStack = getEquippedStack(EquipmentSlot.HEAD)
        val helicopterLevel = helicopterV2.getLevel(helmetStack) ?: return

        val movement = helicopterMovement

        // Check Motor-Start bei 70%+ Power - NUR EINMAL!
        if (!movement.isFlying && movement.canStartMotor()) {
            helicopterMovement = movement.withFlying(true).withRotationPower(0f).withPowerPresses(0)
            isHelicopterFlying = true // Sound-Event triggern
            modifyVelocity(Vec3d(0.0, 1.0, 0.0))
            sendMessage(
                "§a§l[HELICOPTER] §7Motor gestartet! Halte Space zum Fliegen.".literal,
                false
            )
            return // Wichtig: Return nach Motor-Start!
        }

        // Startup-Phase: Power-System mit Decay
        if (!movement.isFlying) {
            val currentPower = movement.rotationPower

            if (currentPower > 0) {
                // Motor Sound ab 5% Power!
                val motorShouldRun = currentPower >= 5f
                if (isHelicopterMotorRunning != motorShouldRun) {
                    isHelicopterMotorRunning = motorShouldRun
                }

                // Power Decay pro Tick - jetzt konfigurierbar!
                val newPower = (currentPower - HelicopterMovement.POWER_DECAY_PER_TICK).coerceAtLeast(0f)

                // Rotation basierend auf aktueller Power
                val startupRotationSpeed = movement.getCurrentRotationSpeed()
                val newRotation = (movement.currentRotation + startupRotationSpeed) % 360f

                helicopterMovement = movement
                    .withRotationPower(newPower)
                    .withRotation(newRotation)

                // Reset Presses wenn Power auf 0
                if (newPower <= 0f) {
                    helicopterMovement = movement.withPowerPresses(0).withRotation(0f)
                    isHelicopterMotorRunning = false // Motor Sound stoppen
                }
            } else {
                // Kein Power = Motor Sound aus
                if (isHelicopterMotorRunning) {
                    isHelicopterMotorRunning = false
                }
            }
        }

        // Flug-Phase: Normale Helicopter-Bewegung
        if (movement.isFlying) {
            // Kontinuierliche Rotation
            val newRotation = (movement.currentRotation + movement.rotationSpeed) % 360f
            helicopterMovement = movement.withRotation(newRotation)

            // Wind-Effekt: Entities und Blöcke wegschleudern!
            this.helicopterWindEffect()

            // Kontinuierliche Bewegung pro Tick
            if (movement.isBoostPressed) {
                //println("###SERVER: BOOST ACTIVE - Force: ${movement.boostForce}")
                //modifyVelocity(Vec3d(0.0, movement.boostForce, 0.0))
            } else {
                //println("###SERVER: SINKING - No boost")
                //modifyVelocity(Vec3d(0.0, -0.05, 0.0))
            }

            // Auto-Stop bei Boden
            if (isOnGround) {
                helicopterMovement = movement.withFlying(false).withRotationPower(0f).withPowerPresses(0)
                isHelicopterFlying = false // Sound-Event triggern
                sendMessage("§c§l[HELICOPTER] §7Motor gestoppt.".literal, false)
            }
        }
    }

    /**
     * Helicopter Wind-Effekt: Schleudert Entities weg und zerstört leichte Blöcke
     */
    private fun ServerPlayerEntity.helicopterWindEffect() {
        // === LOKALE VARIABLEN FÜR HOT-SWAPPING ===
        val WIND_RADIUS = 8.0 // Radius um Spieler
        val ENTITY_PUSH_FORCE = 3.5 // Kraft zum Wegschleudern von Entities
        val VERTICAL_PUSH_FORCE = 1.5 // Vertikale Kraft
        val BLOCK_BREAK_RADIUS = 5.0 // Radius für Block-Zerstörung
        val BLOCK_BREAK_CHANCE = 0.3f // 30% Chance pro Tick
        val AFFECT_PLAYERS = false // Andere Spieler auch wegschleudern?
        val AFFECT_PASSIVE_MOBS = true // Passive Mobs (Kühe, Schafe, etc.)
        val AFFECT_HOSTILE_MOBS = true // Hostile Mobs

        val playerPos = this.pos
        val world = this.world

        // === ENTITIES WEGSCHLEUDERN ===
        val nearbyEntities = world.getOtherEntities(this, this.boundingBox.expand(WIND_RADIUS))

        for (entity in nearbyEntities) {
            // Skip basierend auf Entity-Type
            when {
                entity is PlayerEntity && !AFFECT_PLAYERS -> continue
                entity is net.minecraft.entity.passive.PassiveEntity && !AFFECT_PASSIVE_MOBS -> continue
                entity is net.minecraft.entity.mob.HostileEntity && !AFFECT_HOSTILE_MOBS -> continue
            }

            val distance = entity.pos.distanceTo(playerPos)
            if (distance <= WIND_RADIUS) {
                // Berechne Push-Richtung (weg vom Spieler)
                val direction = entity.pos.subtract(playerPos).normalize()

                // Kraft basierend auf Distanz (näher = stärker)
                val forceFactor = (1.0 - (distance / WIND_RADIUS)).coerceAtLeast(0.0)
                val pushForce = ENTITY_PUSH_FORCE * forceFactor

                // Wind-Velocity anwenden
                val windVelocity = Vec3d(
                    direction.x * pushForce,
                    VERTICAL_PUSH_FORCE * forceFactor, // Leicht nach oben
                    direction.z * pushForce
                )

                entity.addVelocity(windVelocity.x, windVelocity.y, windVelocity.z)
                entity.velocityModified = true
            }
        }

        // === LEICHTE BLÖCKE ZERSTÖREN ===
        val centerPos = this.blockPos
        val blockRadius = BLOCK_BREAK_RADIUS.toInt()

        for (x in -blockRadius..blockRadius) {
            for (y in -blockRadius..blockRadius) {
                for (z in -blockRadius..blockRadius) {
                    val blockPos = centerPos.add(x, y, z)
                    val distance = blockPos.getSquaredDistance(centerPos)

                    if (distance <= BLOCK_BREAK_RADIUS * BLOCK_BREAK_RADIUS) {
                        val blockState = world.getBlockState(blockPos)
                        val block = blockState.block

                        // Prüfe Block-Tags für leichte Blöcke
                        val isLightBlock = blockState.isIn(net.minecraft.registry.tag.BlockTags.LEAVES) ||
                                blockState.isIn(net.minecraft.registry.tag.BlockTags.FLOWERS) ||
                                blockState.isIn(net.minecraft.registry.tag.BlockTags.REPLACEABLE) ||
                                blockState.isIn(net.minecraft.registry.tag.BlockTags.SAPLINGS) ||
                                block.toString().contains("vine") ||  // Ranken
                                block.toString().contains("fern")     // Farne

                        if (isLightBlock && Math.random() < BLOCK_BREAK_CHANCE) {
                            // Block zerstören mit Partikel-Effekt
                            world.breakBlock(blockPos, true, this)

                            // Optional: Spawn falling block entity für dramatischen Effekt
                            if (Math.random() < 0.1) { // 10% Chance für falling block
                                val fallingBlock = net.minecraft.entity.FallingBlockEntity.spawnFromBlock(
                                    world, blockPos, blockState
                                )
                                // Gib dem falling block etwas velocity weg vom Spieler
                                val direction = blockPos.toCenterPos().subtract(playerPos).normalize()
                                fallingBlock?.addVelocity(
                                    direction.x * 0.5,
                                    0.3,
                                    direction.z * 0.5
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun PlayerEntity.handleHelicopterInput() {
        val helmetStack = getEquippedStack(EquipmentSlot.HEAD)
        val helicopterLevel = helicopterV2.getLevel(helmetStack) ?: return

        // Input handling is now done via KeyEvents only
    }

    fun handleRotationRendering(
        playerEntityRenderState: PlayerEntityRenderState,
        matrices: MatrixStack,
        tickDelta: Float,
        g: Float
    ) {
        val entity = (playerEntityRenderState as? EntityRenderStateExt?)?.nrc_entity ?: return
        val movement = entity.helicopterMovement
        val actualTickDelta = MinecraftClient.getInstance().renderTickCounter.getTickProgress(false)

        // Wie bei Elytra: setupTransforms logic
        val h = (entity as? PlayerEntity?)?.getLeaningPitch(actualTickDelta)!!
        val i = playerEntityRenderState.pitch

        // Einfach: Yaw (Blickrichtung) + Forward Lean
        //matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(playerEntityRenderState.relativeHeadYaw * (Math.PI / 180.0).toFloat())) // In Blickrichtung drehen
        //matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(20f)) // Nach vorne lehnen (20°)

        if (movement.isFlying || movement.rotationPower > 2) {
            // Helicopter Y-Rotation (Spinning)
            // Verwende unsere eigene Rotation statt flyingRotation
            val rotationSpeed = movement.getCurrentRotationSpeed()
            entity.helicopterCurrentClientSidedRotation += rotationSpeed * actualTickDelta * 10f

            //matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.helicopterCurrentClientSidedRotation))
        } else {
            // Reset Client-Rotation wenn Motor aus
            entity.helicopterCurrentClientSidedRotation = 0f
        }
    }

    fun PlayerEntity.handleHelicopterTravel(
        movementInput: Vec3d,
        ci: CallbackInfo
    ) {
        // Elytra-basierte Gliding Logic als Basis
        val vec3d = this.getVelocity()
        val d = vec3d.horizontalLength()

        // Helicopter Y-Movement Logic hinzufügen
        val movement = this.helicopterMovement
        val currentVelocity = this.calcGlidingVelocity(vec3d)

        // Y-Velocity basierend auf Boost-Input anpassen
        val newYVelocity = when {
            movement.isBoostPressed -> {
                // Space gedrückt = Hoch fliegen
                currentVelocity.y + movement.boostForce * 0.2
            }

            else -> {
                // Kein Space = Langsam sinken
                currentVelocity.y - 0.05
            }
        }

        // Neue Velocity mit angepasster Y-Komponente setzen
        val helicopterVelocity = Vec3d(currentVelocity.x, newYVelocity, currentVelocity.z)
        this.setVelocity(helicopterVelocity)

        // Bewegung anwenden (wie bei Elytra)
        this.move(MovementType.SELF, this.getVelocity())

        // Kollisionsprüfung (nur server-side)
        if (!this.getWorld().isClient) {
            val e = this.getVelocity().horizontalLength()
            this.checkGlidingCollision(d, e)
        }

        ci.cancel()
    }

    // Extension property für HelicopterMovement
    var Entity.helicopterMovement: HelicopterMovement
        get() = this.getSyncedData<HelicopterMovement>("$MOD_ID:HelicopterMovement") ?: HelicopterMovement()
        set(value) {
            this.setSyncedData("$MOD_ID:HelicopterMovement", value)
        }
    private var Entity.helicopterCurrentClientSidedRotation: Float
        get() = this.getSyncedData<Float>("$MOD_ID:helicopterCurrentClientSidedRotation") ?: 0f
        set(value) {
            this.setSyncedData("$MOD_ID:helicopterCurrentClientSidedRotation", value)
        }

    private fun <S : ServerCommandSource> CommandContext<S>.helicopter() {
        val player = this.source.playerOrThrow

        player.giveItemStack(itemStack(Items.DIAMOND_HELMET) {
            addEnchantment(helicopterV2.getEntry(player.world), 1)
        })

        // Anleitung für Freunde
        player.sendMessage("".literal, false)
        player.sendMessage("§a§l=== HELICOPTER ENCHANTMENT V2 ===".literal, false)
        player.sendMessage("§7Ziehe den §bDiamond Helmet §7an:".literal, false)
        player.sendMessage("".literal, false)
        player.sendMessage("§e§l1. Motor starten:".literal, false)
        player.sendMessage("§7   • §fLeertaste mehrmals drücken §7bis Motor startet (Am Besten mit F5)".literal, false)
        player.sendMessage("".literal, false)
        player.sendMessage("§e§l2. Fliegen:".literal, false)
        player.sendMessage("§7   • §fLeertaste halten §7= Hoch fliegen".literal, false)
        player.sendMessage("§7   • §fLeertaste loslassen §7= Sinken".literal, false)
        player.sendMessage("§7   • §fMaus bewegen §7= Richtung ändern".literal, false)
        player.sendMessage("".literal, false)
    }

    fun PlayerEntityModel.handleApplyPlayerModelRotations(playerEntityRenderState: PlayerEntityRenderState) {
        val entity = (playerEntityRenderState as? EntityRenderStateExt?)?.nrc_entity ?: return
        if (entity.isHelicopterFlying) {
            this.head.pitch = 0f
            this.body.yaw = 0f
            this.leftLeg.pitch = 0f
            this.rightLeg.pitch = 0f
            this.leftLeg.yaw = 0f
            this.rightLeg.yaw = 0f

            this.rightArm.yaw = 0f
            this.leftArm.yaw = 0f
            this.leftArm.pitch = 0f
            this.rightArm.pitch = 0f

            // T-Pose: Arme horizontal ausgestreckt
            // π/2 ≈ 1.5708f Radians = 90°
            this.rightArm.roll = 1.5708f  // Rechter Arm nach rechts (negative Z-Rotation)
            this.leftArm.roll = -this.rightArm.roll    // Linker Arm nach links (positive Z-Rotation)
        }
    }

    fun handleForwardRotation(
        args: Args,
        livingEntityRenderState: LivingEntityRenderState,
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        i: Int
    ) {
        val entity = (livingEntityRenderState as? EntityRenderStateExt?)?.nrc_entity as? PlayerEntity? ?: return

        val matrices = args.get<MatrixStack>(0)
        val fullBodyTransform = ModelPartTransform.identity()

        // Pivot-Fix: Erst zum Player-Zentrum, dann rotieren, dann zurück
        val playerHeight = 1.8f // Standard Player-Höhe

        // 1. Zum Player-Zentrum bewegen
        matrices.translate(0f, playerHeight / 2f, 0f)

        // 2. Pitch von Degrees zu Radians konvertieren
        val pitchRadians = livingEntityRenderState.pitch * (Math.PI / 180.0).toFloat()

        // 3. Rotation um Player-Zentrum
        matrices.multiply(
            Quaternionf().rotationZYX(
                0f, // rotZ
                0f, // rotY  
                if (entity.isHelicopterFlying) pitchRadians else 0f
            ).rotateY(entity.helicopterCurrentClientSidedRotation * (Math.PI / 180.0).toFloat(), Quaternionf())
        )

        // 4. Zurück zur ursprünglichen Position
        matrices.translate(0f, -playerHeight / 2f, 0f)

        args.set(0, matrices)
    }

    fun handleFixBody(player: AbstractClientPlayerEntity) {
        if (player.isHelicopterFlying) {
            player.bodyYaw = player.headYaw
            player.lastBodyYaw = player.lastHeadYaw
        }
    }

    // Flag für Sound-Event System
    var Entity.isHelicopterFlying: Boolean
        get() = this.getSyncedData<Boolean>("$MOD_ID:IsHelicopterFlying") ?: false
        set(value) {
            this.setSyncedData("$MOD_ID:IsHelicopterFlying", value)
            if (!world.isClient) {
                val player = this as? PlayerEntity? ?: return
                //abilities.flying = value
                //abilities.allowFlying = value
                //+
                // sendAbilitiesUpdate()
            }
        }

    // Motor Sound Flag - startet schon bei 5% Power
    var Entity.isHelicopterMotorRunning: Boolean
        get() = this.getSyncedData<Boolean>("$MOD_ID:IsHelicopterMotorRunning") ?: false
        set(value) {
            this.setSyncedData("$MOD_ID:IsHelicopterMotorRunning", value)
        }
}