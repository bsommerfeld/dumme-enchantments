package gg.norisk.enchantments.command

import com.mojang.brigadier.context.CommandContext
import gg.norisk.enchantments.EnchantmentRegistry.animation
import gg.norisk.enchantments.EnchantmentRegistry.arrowTrail
import gg.norisk.enchantments.EnchantmentRegistry.bouncy
import gg.norisk.enchantments.EnchantmentRegistry.chainReaction
import gg.norisk.enchantments.EnchantmentRegistry.circleShoot
import gg.norisk.enchantments.EnchantmentRegistry.crush
import gg.norisk.enchantments.EnchantmentRegistry.experience
import gg.norisk.enchantments.EnchantmentRegistry.instantFishing
import gg.norisk.enchantments.EnchantmentRegistry.multiFishing
//import gg.norisk.satisfying.SatisfyingSuperStar
//import gg.norisk.satisfying.SatisfyingTrail.AFTER_IMAGE_EFFECT_REGISTRY
//import gg.norisk.satisfying.SatisfyingTrail.AFTER_IMAGE_POTION
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.PotionContentsComponent
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.potion.Potion
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.world.GameMode
import net.minecraft.world.GameRules
import net.minecraft.world.World
import net.silkmc.silk.commands.PermissionLevel
import net.silkmc.silk.commands.command
import net.silkmc.silk.core.item.itemStack
import net.silkmc.silk.core.item.setPotion
import net.silkmc.silk.core.text.literal
import net.silkmc.silk.core.text.literalText
import java.awt.Color
import java.util.*
import kotlin.time.Duration.Companion.seconds


object SatisfyingCommand {
    fun initServer() {
        command("satisfyingnichtausführen") {
            requires { it.hasPermissionLevel(PermissionLevel.COMMAND_RIGHTS.level) }
            literal("1") {
                runs {
                    this.default()
                    this.fishing()
                }
            }
            literal("2") {
                runs {
                    this.default()
                    this.hydraulic()
                }
            }
            literal("3") {
                runs {
                    this.default()
                    this.superstar()
                }
            }
            literal("4") {
                runs {
                    this.default()
                    this.xp()
                }
            }
            literal("5") {
                runs {
                    this.default()
                    this.blockAnimation()
                }
            }
            literal("6") {
                runs {
                    this.default()
                    this.chainreaction()
                }
            }
            literal("7") {
                runs {
                    this.default()
                    this.afterimage()
                }
            }
            literal("8") {
                runs {
                    this.default()
                    this.bowforms()
                }
            }
        }
    }

    /*private fun createPotion(
        registryEntry: RegistryEntry<Potion>, statusEffectInstance: StatusEffectInstance, item: Item = Items.POTION
    ): ItemStack {
        val itemStack = ItemStack(item)
        itemStack[DataComponentTypes.POTION_CONTENTS] = PotionContentsComponent(
            Optional.of(registryEntry), Optional.empty(), listOf(
                statusEffectInstance
            )
        )
        return itemStack
    }*/

    private fun <S : ServerCommandSource> CommandContext<S>.afterimage() {
        val player = this.source.playerOrThrow

        /*player.inventory.setStack(
            0, createPotion(
                AFTER_IMAGE_POTION, StatusEffectInstance(
                    AFTER_IMAGE_EFFECT_REGISTRY, (10.seconds.inWholeMilliseconds / 50).toInt(), 0, false, false, false
                )
            )
        )
        player.inventory.setStack(
            4, createPotion(
                AFTER_IMAGE_POTION, StatusEffectInstance(
                    AFTER_IMAGE_EFFECT_REGISTRY, (10.seconds.inWholeMilliseconds / 50).toInt(), 1, false, false, false
                )
            )
        )
        player.inventory.setStack(
            8, createPotion(
                AFTER_IMAGE_POTION, StatusEffectInstance(
                    AFTER_IMAGE_EFFECT_REGISTRY, (20.seconds.inWholeMilliseconds / 50).toInt(), 2, false, false, false
                )
            )
        )

        player.sendMessage(literalText {
            text("hover über mich bevor du die letzte potion trinks") {}
            italic = true
            color = Color.LIGHT_GRAY.rgb
            hoverEvent = HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                "lauf so als würdest du curve fever spielen, geh dann wieder an den anfang und schlag den 1. spieler".literal
            )
        })*/
    }

    private fun <S : ServerCommandSource> CommandContext<S>.bowforms() {
        val player = this.source.playerOrThrow
        player.changeGameMode(GameMode.CREATIVE)

        player.inventory.setStack(0, itemStack(Items.BOW, 1) {
            addEnchantment(circleShoot.getEntry(player.world), 1)
            addEnchantment(bouncy.getEntry(player.world), 1)
            addEnchantment(arrowTrail.getEntry(player.world), 1)
        })
        player.inventory.setStack(1, itemStack(Items.BOW, 1) {
            addEnchantment(circleShoot.getEntry(player.world), 2)
            addEnchantment(bouncy.getEntry(player.world), 1)
            addEnchantment(arrowTrail.getEntry(player.world), 1)
        })
        player.inventory.setStack(3, itemStack(Items.BOW, 1) {
            addEnchantment(circleShoot.getEntry(player.world), 3)
            addEnchantment(bouncy.getEntry(player.world), 1)
            addEnchantment(arrowTrail.getEntry(player.world), 1)
        })

        player.inventory.setStack(8, itemStack(Items.ARROW, 1) {})

        player.sendMessage(literalText {
            text("das hier musst du nicht testen kannst auch gehen (danke nochmal!!!)") {}
            italic = true
            color = Color.LIGHT_GRAY.rgb
        })
    }

    private fun <S : ServerCommandSource> CommandContext<S>.chainreaction() {
        val player = this.source.playerOrThrow
        player.changeGameMode(GameMode.CREATIVE)

        player.inventory.setStack(0, itemStack(Items.BOW, 1) {
            addEnchantment(chainReaction.getEntry(player.world), 1)
            addEnchantment(Enchantments.INFINITY.getEntry(player.world), 1)
        })

        /*player.inventory.setStack(4, itemStack(Items.BOW, 1) {
            addEnchantment(chainReaction.getEntry(player.world), 1)
            addEnchantment(arrowTrail.getEntry(player.world), 1)
            addEnchantment(experience.getEntry(player.world), 1)
            addEnchantment(Enchantments.POWER.getEntry(player.world), 5)
            addEnchantment(Enchantments.INFINITY.getEntry(player.world), 1)
        })*/

        player.inventory.setStack(1, itemStack(Items.ARROW, 1) {})
        player.inventory.setStack(8, itemStack(Items.IRON_GOLEM_SPAWN_EGG, 64) {})

        player.sendMessage(literalText {
            text("platziere in ~5 blöcken abstand ein paar iron golems") {}
            italic = true
            color = Color.LIGHT_GRAY.rgb
        })
    }


    private fun <S : ServerCommandSource> CommandContext<S>.xp() {
        val player = this.source.playerOrThrow

        player.inventory.setStack(0, itemStack(Items.BOW, 1) {
            addEnchantment(experience.getEntry(player.world), 1)
            addEnchantment(Enchantments.POWER.getEntry(player.world), 5)
            addEnchantment(Enchantments.INFINITY.getEntry(player.world), 1)
        })
        player.inventory.setStack(1, itemStack(Items.ARROW, 1) {})
        player.giveItemStack(itemStack(Items.PIG_SPAWN_EGG, 64) {})
        player.giveItemStack(itemStack(Items.HUSK_SPAWN_EGG, 64) {})
        player.giveItemStack(itemStack(Items.IRON_GOLEM_SPAWN_EGG, 64) {})
    }

    private fun <S : ServerCommandSource> CommandContext<S>.hydraulic() {
        val player = this.source.playerOrThrow
        player.changeGameMode(GameMode.CREATIVE)

        player.inventory.setStack(0, itemStack(Items.WOODEN_SWORD, 1) {
            addEnchantment(crush.getEntry(player.world), 1)
        })

        player.giveItemStack(itemStack(Items.IRON_GOLEM_SPAWN_EGG, 64) {})
    }

    private fun <S : ServerCommandSource> CommandContext<S>.fishing() {
        val player = this.source.playerOrThrow

        player.inventory.setStack(0, itemStack(Items.FISHING_ROD, 1) {
            addEnchantment(instantFishing.getEntry(player.world), 1)
        })
        player.inventory.setStack(2, itemStack(Items.FISHING_ROD, 1) {
            addEnchantment(multiFishing.getEntry(player.world), 1)
        })
        player.inventory.setStack(3, itemStack(Items.FISHING_ROD, 1) {
            addEnchantment(multiFishing.getEntry(player.world), 4)
        })
        player.inventory.setStack(4, itemStack(Items.FISHING_ROD, 1) {
            addEnchantment(multiFishing.getEntry(player.world), 10)
        })
    }

    private fun <S : ServerCommandSource> CommandContext<S>.blockAnimation() {
        val player = this.source.playerOrThrow

        player.giveItemStack(itemStack(Items.DIAMOND_PICKAXE, 1) {
            addEnchantment(animation.getEntry(player.world), 1)
        })
        player.giveItemStack(itemStack(Items.DIAMOND_AXE, 1) {
            addEnchantment(animation.getEntry(player.world), 1)
        })

        /*player.sendMessage(literalText {
            text("bau irgendwas ab und danach schau richtung osten und") {}
            italic = true
            color = Color.LIGHT_GRAY.rgb
            text(" [klick mich]") {
                clickEvent = ClickEvent(
                    ClickEvent.Action.SUGGEST_COMMAND,
                    "/satisfying place minecraft:village/plains/houses/plains_small_house_1 BOUNCE_OUT 0.3"
                )
            }
        })*/
    }

    private fun <S : ServerCommandSource> CommandContext<S>.superstar() {
        val player = this.source.playerOrThrow

        player.giveItemStack(itemStack(Items.IRON_GOLEM_SPAWN_EGG, 64) {})
        /*player.giveItemStack(itemStack(Items.POTION) {
            setPotion(SatisfyingSuperStar.SUPER_STAR_POTION)
        })*/

        player.sendMessage(literalText {
            text("Platziere mobs, geh F5 und trink die Potion") {
                italic = true
                color = Color.LIGHT_GRAY.rgb
            }
        })
    }

    private fun RegistryKey<Enchantment>.getEntry(world: World): RegistryEntry<Enchantment> {
        TODO("1.21.5 Port")
    }

    private fun <S : ServerCommandSource> CommandContext<S>.default() {
        val world = this.source.world
        val server = this.source.server
        world.gameRules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server)
        world.gameRules.get(GameRules.DO_WEATHER_CYCLE).set(false, server)
        world.gameRules.get(GameRules.ANNOUNCE_ADVANCEMENTS).set(false, server)
        world.timeOfDay = 6000
        world.resetWeather()
        val player = this.source.playerOrThrow
        player.heal(player.maxHealth)
        player.hungerManager.saturationLevel = 2000f
        player.hungerManager.foodLevel = 2000
        player.inventory.clear()
        player.clearStatusEffects()
        player.changeGameMode(GameMode.SURVIVAL)
    }
}
