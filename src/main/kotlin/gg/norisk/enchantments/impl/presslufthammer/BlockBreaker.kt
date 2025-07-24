package gg.norisk.enchantments.impl.presslufthammer

import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.Entity
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.util.math.BlockPos
import net.silkmc.silk.commands.command
import net.silkmc.silk.core.packet.sendPacket
import kotlin.random.Random

object BlockBreaker {
    val blockCache = mutableMapOf<BlockPos, Int>()

    fun initServer() {
        if (FabricLoader.getInstance().isDevelopmentEnvironment) {
            command("blockbreaker") {
                literal("under") {
                    runs {
                        val player = this.source.playerOrThrow
                        incrementBlockBreak(player.blockPos.down(), player.serverWorld, player)
                    }
                }
            }
        }
    }

    fun incrementBlockBreak(blockPos: BlockPos, world: ServerWorld, breakingEntity: Entity?) {
        val block = world.getBlockState(blockPos)
        val progress = blockCache.computeIfAbsent(blockPos) { 0 }
        world.players.sendPacket(BlockBreakingProgressS2CPacket(Random.nextInt(), blockPos, progress))
        if (!block.isAir && Random.nextBoolean()) {
            world.playSound(null, blockPos, block.soundGroup.hitSound, SoundCategory.BLOCKS, 1.0f, 1.0f)
        }
        if (progress >= 10) {
            blockCache.remove(blockPos)
            world.breakBlock(blockPos, true, breakingEntity)
        } else {
            blockCache.put(blockPos, progress + 1)
        }
        //world.breakBlock(blockPos, true)
    }
}