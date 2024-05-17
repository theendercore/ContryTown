package org.teamvoided.civilization.commands

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
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.util.Formatting
import org.teamvoided.civilization.commands.argument.SettlementArgumentType
import org.teamvoided.civilization.commands.argument.SettlementArgumentType.settlementArg
import org.teamvoided.civilization.data.ResultType
import org.teamvoided.civilization.managers.PlayerDataManager
import org.teamvoided.civilization.data.Settlement
import org.teamvoided.civilization.data.wrappedText
import org.teamvoided.civilization.init.CivCommands.DEBUG_MODE
import org.teamvoided.civilization.util.Util.lTxt
import java.util.*


object TestCommand {
    fun init(dispatcher: CommandDispatcher<ServerCommandSource>) {
        val testNode = literal("qtest").build()
        dispatcher.root.addChild(testNode)
        val inbuiltGuiNode = literal("inbuilt_gui").executes(this::test).build()
        testNode.addChild(inbuiltGuiNode)

        val mapLinkNode = literal("map_link").executes(this::mapLink).build()
        dispatcher.root.addChild(mapLinkNode)

        val clearDataNode = literal("clear_data").executes(this::clearData).build()
        testNode.addChild(clearDataNode)


        val debugNode = literal("text").build()
        testNode.addChild(debugNode)
        debugNode.addChild(
            settlementArg()
                .executes { text(it, SettlementArgumentType.getSettlement(it)) }
                .build()
        )

        val debugMode = literal("debug_mode").executes {
            DEBUG_MODE = !DEBUG_MODE
            it.source.sendSystemMessage(lTxt("Debug mode : $DEBUG_MODE"))
            1
        }.build()
        dispatcher.root.addChild(debugMode)

        dispatcher.register(literal("tq").redirect(testNode))
    }

    private fun test(c: CommandContext<ServerCommandSource>): Int {
        try {
            val player = c.source.player
            val gui: SimpleGui = object : SimpleGui(ScreenHandlerType.GENERIC_3X3, player, false) {
                override fun onClick(
                    index: Int, type: ClickType, action: SlotActionType?, element: GuiElementInterface?
                ): Boolean {
                    this.player.sendMessage(lTxt(type.toString()), false)

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

            gui.setTitle(lTxt("Nice"))
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
            ) { x: Int, y: ClickType, z: SlotActionType -> })

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
                ) { index: Int, clickType: ClickType?, actionType: SlotActionType? -> })
            }

            gui.setSlot(
                5, GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(
                    "ewogICJ0aW1lc3RhbXAiIDogMTYxOTk3MDIyMjQzOCwKICAicHJvZmlsZUlkIiA6ICI2OTBkMDM2OGM2NTE0OGM5ODZjMzEwN2FjMmRjNjFlYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ5emZyXzciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDI0OGVhYTQxNGNjZjA1NmJhOTY5ZTdkODAxZmI2YTkyNzhkMGZlYWUxOGUyMTczNTZjYzhhOTQ2NTY0MzU1ZiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                    null,
                    null
                ).setName(lTxt("Battery")).glow()
            )

            gui.setSlot(
                6, GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(
                    GameProfile(UUID.fromString("f5a216d9-d660-4996-8d0f-d49053677676"), "patbox"), player!!.server
                ).setName(lTxt("Patbox's Head")).glow()
            )

            gui.setSlot(7, GuiElementBuilder().setItem(Items.BARRIER).glow().setName(
                lTxt("Bye").setStyle(Style.EMPTY.withItalic(false).withBold(true))
            ).addLoreLine(lTxt("Some lore")).addLoreLine(lTxt("More lore").formatted(Formatting.RED))
                .setCount(3)
                .setCallback { index: Int, clickType: ClickType?, actionType: SlotActionType? -> gui.close() })

            gui.setSlot(8, GuiElementBuilder().setItem(Items.TNT).glow().setName(
                lTxt("Test :)").setStyle(Style.EMPTY.withItalic(false).withBold(true))
            ).addLoreLine(lTxt("Some lore")).addLoreLine(lTxt("More lore").formatted(Formatting.RED))
                .setCount(1).setCallback { index: Int, clickType: ClickType, _: SlotActionType ->
                    player.sendMessage(lTxt("derg "), false)
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
            e.printStackTrace()
        }
        return 0
    }


    private fun mapLink(c: CommandContext<ServerCommandSource>): Int {
        c.source.sendSystemMessage(lTxt("http://localhost:8080/"))
        return 1
    }

    private fun clearData(c: CommandContext<ServerCommandSource>): Int {
        val src = c.source
        val player = src.player ?: return 0
        PlayerDataManager.clearD(player)
        src.sendSystemMessage(lTxt("data reset"))
        return 1
    }


    private fun text(c: CommandContext<ServerCommandSource>, settlement: Settlement): Int {
        val src = c.source
        val player = src.player ?: return 0
        val slotRemover = GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).hideTooltip()


        val gui = SimpleGui(ScreenHandlerType.GENERIC_3X3, player, false)
        for (x in 0..8) gui.setSlot(x, slotRemover)

        gui.setSlot(
            1,
            GuiElementBuilder(Items.STICK)
                .setName(toText())
        )
        gui.open()
        return 1
    }

}

fun toText(): MutableText {
    val bl = false
    val num = 1.0
    val str = "hello"
    val nil = null
    val obj = ResultType.SUCCESS
    val lst = listOf("1", "t")

    // bool, number, text, null, object, list
    return wrappedText {
        addBool(bl)
        addNumber(num)
        addString(str)
        addObject("result") {
            addString(obj.toString())
        }
        addList {
            lst.forEach { addString(it) }
        }
    }
}
