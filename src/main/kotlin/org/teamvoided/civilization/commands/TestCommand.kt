package org.teamvoided.civilization.commands

import arrow.core.raise.ensureNotNull
import com.mojang.authlib.GameProfile
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import eu.pb4.sgui.api.ClickType
import eu.pb4.sgui.api.elements.*
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.enchantment.Enchantments
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Style
import net.minecraft.util.Formatting
import net.minecraft.util.WorldSavePath
import net.minecraft.world.dimension.DimensionType
import org.teamvoided.civilization.Civilization.log
import org.teamvoided.civilization.commands.argument.SettlementArgumentType.settlementArg
import org.teamvoided.civilization.data.DebuggingError
import org.teamvoided.civilization.data.Settlement
import org.teamvoided.civilization.managers.PlayerDataManager
import org.teamvoided.civilization.util.*
import java.util.*

@Suppress("MagicNumber")
object TestCommand {
    @Suppress("UNUSED_VARIABLE")
    fun init(dispatcher: CommandDispatcher<ServerCommandSource>) {
        val civTest = literal("civ_test")
            .build()
            .childOf(dispatcher.root)

        val inbuiltGui = literal("inbuilt_gui")
            .executes(::inbuiltGui)
            .build()
            .childOf(civTest)

        val mapLink = literal("map_link")
            .executes(::mapLink)
            .build()
            .addChild(inbuiltGui)

        val data = literal("data")
            .build()
            .childOf(civTest)

        val dataDump = literal("dump")
            .executes(::dataDump)
            .build()
            .childOf(data)

        val dataClear = literal("clear")
            .executes(::dataClear)
            .build()
            .childOf(data)

        val text = literal("text")
            .build()
            .childOf(civTest)
        val textName = settlementArg()
            .executes(::text)
            .build()
            .childOf(text)


        val t = literal("t")
            .executes(::t)
            .build()
            .childOf(civTest)

        dispatcher.register(literal("ct").redirect(civTest))
    }

    private fun t(c: CommandContext<ServerCommandSource>): Int = c.serverWorldPlayer { src, server, world, player ->
        for (worldI in server.worlds) {
            val path = DimensionType.getSaveDirectory(worldI.registryKey, server.getSavePath(WorldSavePath.ROOT))

            src.tMessage(
                path.toString()
            )
            src.tMessage(path.resolve("data").toString())
            src.tMessage("")
        }

        1
    }


    @Suppress("LongMethod", "MaxLineLength", "TooGenericExceptionCaught")
    private fun inbuiltGui(c: CommandContext<ServerCommandSource>): Int {
        try {
            val player = c.source.player
            val gui: SimpleGui = object : SimpleGui(ScreenHandlerType.GENERIC_3X3, player, false) {
                override fun onClick(
                    index: Int, type: ClickType, action: SlotActionType?, element: GuiElementInterface?
                ): Boolean {
                    this.player.sendMessage(lText(type.toString()), false)

                    return super.onClick(index, type, action, element)
                }

                override fun onTick() {
                    this.setSlot(0, GuiElementBuilder(Items.ARROW).setCount((player!!.serverWorld.time % 127).toInt()))
                    super.onTick()
                }

                override fun canPlayerClose(): Boolean {
                    return false
                }
            }

            gui.setTitle(lText("Nice"))
            gui.setSlot(0, GuiElementBuilder(Items.ARROW).setCount(100))
            gui.setSlot(1, AnimatedGuiElement(
                arrayOf<ItemStack>(
                    Items.NETHERITE_PICKAXE.defaultStack,
                    Items.DIAMOND_PICKAXE.defaultStack,
                    Items.GOLDEN_PICKAXE.defaultStack,
                    Items.IRON_PICKAXE.defaultStack,
                    Items.STONE_PICKAXE.defaultStack,
                    Items.WOODEN_PICKAXE.defaultStack
                ), 10, false
            ) { _: Int, _: ClickType, _: SlotActionType -> })

            gui.setSlot(
                2,
                AnimatedGuiElementBuilder().setItem(Items.NETHERITE_AXE).setDamage(150).saveItemStack()
                    .setItem(Items.DIAMOND_AXE).setDamage(150).unbreakable().saveItemStack().setItem(Items.GOLDEN_AXE)
                    .glow().saveItemStack().setItem(Items.IRON_AXE).enchant(Enchantments.AQUA_AFFINITY, 1)
                    .saveItemStack().setItem(Items.STONE_AXE).saveItemStack().setItem(Items.WOODEN_AXE).saveItemStack()
                    .setInterval(10).setRandom(true)
            )

            for (x in 3 until gui.size) {
                val itemStack: ItemStack = Items.STONE.defaultStack
                itemStack.count = x
                gui.setSlot(x, GuiElement(
                    itemStack
                ) { _: Int, _: ClickType?, _: SlotActionType? -> })
            }

            gui.setSlot(
                5, GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(
                    "ewogICJ0aW1lc3RhbXAiIDogMTYxOTk3MDIyMjQzOCwKICAicHJvZmlsZUlkIiA6ICI2OTBkMDM2OGM2NTE0OGM5ODZjMzEwN2FjMmRjNjFlYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ5emZyXzciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDI0OGVhYTQxNGNjZjA1NmJhOTY5ZTdkODAxZmI2YTkyNzhkMGZlYWUxOGUyMTczNTZjYzhhOTQ2NTY0MzU1ZiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                    null,
                    null
                ).setName(lText("Battery")).glow()
            )

            gui.setSlot(
                6, GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(
                    GameProfile(UUID.fromString("f5a216d9-d660-4996-8d0f-d49053677676"), "patbox"), player!!.server
                ).setName(lText("Patbox's Head")).glow()
            )

            gui.setSlot(7, GuiElementBuilder().setItem(Items.BARRIER).glow().setName(
                lText("Bye").setStyle(Style.EMPTY.withItalic(false).withBold(true))
            ).addLoreLine(lText("Some lore")).addLoreLine(lText("More lore").formatted(Formatting.RED))
                .setCount(3)
                .setCallback { _: Int, _: ClickType?, _: SlotActionType? -> gui.close() })

            gui.setSlot(8, GuiElementBuilder().setItem(Items.TNT).glow().setName(
                lText("Test :)").setStyle(Style.EMPTY.withItalic(false).withBold(true))
            ).addLoreLine(lText("Some lore")).addLoreLine(lText("More lore").formatted(Formatting.RED))
                .setCount(1).setCallback { index: Int, clickType: ClickType, _: SlotActionType ->
                    player.sendMessage(lText("derg "), false)
                    val item = gui.getSlot(index)!!.itemStack
                    if (clickType == ClickType.MOUSE_LEFT) {
                        item.count = if (item.count == 1) item.count else item.count - 1
                    } else if (clickType == ClickType.MOUSE_RIGHT) {
                        item.count += 1
                    }
                    (gui.getSlot(index) as GuiElement).itemStack = item
                    if (item.count <= player.enderChestInventory.size()) {
                        gui.setSlotRedirect(4, Slot(player.enderChestInventory, item.count - 1, 0, 0))
                    }
                })
            gui.setSlotRedirect(4, Slot(player.enderChestInventory, 0, 0, 0))

            gui.open()
        } catch (e: Exception) {
            log.error("Error in test", e)
        }
        return 0
    }

    private fun mapLink(c: CommandContext<ServerCommandSource>): Int {
        c.source.litMessage("http://localhost:8080/")
        return 1
    }

    private fun dataClear(c: CommandContext<ServerCommandSource>): Int = c.serverWorldPlayer { src, _, _, player ->
        PlayerDataManager.clearD(player)
        src.tMessage("Data cleared!")

        1
    }

    private fun dataDump(c: CommandContext<ServerCommandSource>): Int = c.serverWorldPlayer { src, _, _, player ->
        val data = PlayerDataManager.getDataD(player)
        ensureNotNull(data) { DebuggingError("No data found!") }
        data.toText().siblings.forEach { src.message(it) }

        1
    }

    private fun text(c: CommandContext<ServerCommandSource>, settlement: Settlement): Int {
        val src = c.source
        val player = src.player ?: return 0
        val slotRemover = GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setCustomModelData(1).hideTooltip()

        val gui = SimpleGui(ScreenHandlerType.GENERIC_3X3, player, false)
        for (x in 0..8) gui.setSlot(x, slotRemover)

        gui.setSlot(
            4,
            GuiElementBuilder(Items.STICK)
                .setLore(settlement.toText().siblings)
        )
        gui.open()
        return 1
    }

}
