package org.teamvoided.template.commands

import com.mojang.authlib.GameProfile
import com.mojang.brigadier.context.CommandContext
import eu.pb4.sgui.api.ClickType
import eu.pb4.sgui.api.elements.*
import eu.pb4.sgui.api.gui.SimpleGui
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.boss.BossBar
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.apache.commons.lang3.StringUtils
import xaero.pac.common.server.api.OpenPACServerAPI
import xyz.jpenilla.squaremap.api.*
import xyz.jpenilla.squaremap.api.marker.Marker
import xyz.jpenilla.squaremap.api.marker.MarkerOptions
import java.awt.Color
import java.util.*


object TestCommand {
    fun init() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            val testNode = literal("qtest").build()
            val inbuiltNode = literal("inbuilt").executes(this::test).build()
            val mapTestNode = literal("map_test").executes(this::mapTest).build()

            dispatcher.root.addChild(testNode)
            testNode.addChild(inbuiltNode)
            testNode.addChild(mapTestNode)

            val mapLinkNode = literal("map_link").executes(this::mapLink).build()
            dispatcher.root.addChild(mapLinkNode)
        }

    }

    private fun test(c: CommandContext<ServerCommandSource>): Int {
        try {
            val player = c.source.player
            val gui: SimpleGui = object : SimpleGui(ScreenHandlerType.GENERIC_3X3, player, false) {
                override fun onClick(
                    index: Int,
                    type: ClickType,
                    action: SlotActionType?,
                    element: GuiElementInterface?
                ): Boolean {
                    this.player.sendMessage(Text.literal(type.toString()), false)

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

            gui.setTitle(Text.literal("Nice"))
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
                2, AnimatedGuiElementBuilder()
                    .setItem(Items.NETHERITE_AXE).setDamage(150).saveItemStack()
                    .setItem(Items.DIAMOND_AXE).setDamage(150).unbreakable().saveItemStack()
                    .setItem(Items.GOLDEN_AXE).glow().saveItemStack()
                    .setItem(Items.IRON_AXE).enchant(Enchantments.AQUA_AFFINITY, 1).hideFlags().saveItemStack()
                    .setItem(Items.STONE_AXE).saveItemStack()
                    .setItem(Items.WOODEN_AXE).saveItemStack()
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
                5, GuiElementBuilder(Items.PLAYER_HEAD)
                    .setSkullOwner(
                        "ewogICJ0aW1lc3RhbXAiIDogMTYxOTk3MDIyMjQzOCwKICAicHJvZmlsZUlkIiA6ICI2OTBkMDM2OGM2NTE0OGM5ODZjMzEwN2FjMmRjNjFlYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ5emZyXzciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDI0OGVhYTQxNGNjZjA1NmJhOTY5ZTdkODAxZmI2YTkyNzhkMGZlYWUxOGUyMTczNTZjYzhhOTQ2NTY0MzU1ZiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                        null, null
                    )
                    .setName(Text.literal("Battery"))
                    .glow()
            )

            gui.setSlot(
                6, GuiElementBuilder(Items.PLAYER_HEAD)
                    .setSkullOwner(
                        GameProfile(UUID.fromString("f5a216d9-d660-4996-8d0f-d49053677676"), "patbox"),
                        player!!.server
                    )
                    .setName(Text.literal("Patbox's Head"))
                    .glow()
            )

            gui.setSlot(7, GuiElementBuilder()
                .setItem(Items.BARRIER)
                .glow()
                .setName(
                    Text.literal("Bye")
                        .setStyle(Style.EMPTY.withItalic(false).withBold(true))
                )
                .addLoreLine(Text.literal("Some lore"))
                .addLoreLine(Text.literal("More lore").formatted(Formatting.RED))
                .setCount(3)
                .setCallback { index: Int, clickType: ClickType?, actionType: SlotActionType? -> gui.close() }
            )

            gui.setSlot(8, GuiElementBuilder()
                .setItem(Items.TNT)
                .glow()
                .setName(
                    Text.literal("Test :)")
                        .setStyle(Style.EMPTY.withItalic(false).withBold(true))
                )
                .addLoreLine(Text.literal("Some lore"))
                .addLoreLine(Text.literal("More lore").formatted(Formatting.RED))
                .setCount(1)
                .setCallback { index: Int, clickType: ClickType, _: SlotActionType ->
                    player.sendMessage(Text.literal("derg "), false)
                    val item = gui.getSlot(index).itemStack
                    if (clickType == ClickType.MOUSE_LEFT) {
                        item.count = if (item.count == 1) item.count else item.count - 1
                    } else if (clickType == ClickType.MOUSE_RIGHT) {
                        item.count += 1
                    }
                    (gui.getSlot(index) as GuiElement).itemStack = item
                    if (item.count <= player.enderChestInventory.size()) {
                        gui.setSlotRedirect(4, Slot(player.enderChestInventory, item.count - 1, 0, 0))
                    }
                }
            )
            gui.setSlotRedirect(4, Slot(player.enderChestInventory, 0, 0, 0))

            gui.open()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }


    private fun mapTest(c: CommandContext<ServerCommandSource>): Int {
        try {
            val api: Squaremap = SquaremapProvider.get()
            val world = c.source.world

            val worldKey = world.registryKey
            val layerKey = Key.of("ender_labels")


            val provider = SimpleLayerProvider.builder("Ender Labels")
                .showControls(true)
                .defaultHidden(false)
                .layerPriority(5)
                .zIndex(250)
                .build()


            api.getWorldIfEnabled(WorldIdentifier.parse(worldKey.value.toString())).ifPresent { mapWorld: MapWorld ->
                if(!mapWorld.layerRegistry().hasEntry(layerKey)) mapWorld.layerRegistry().register(layerKey, provider)
            }


            val textXKey = Key.of("test_x");
            provider.removeMarker(textXKey)

            val rect = Marker.rectangle(Point.of(-100.0, -150.0), Point.of(-200.0, -250.0)).markerOptions(
                MarkerOptions.builder().hoverTooltip("Big Wall of text. Lorem Ipsum")
            );
            provider.addMarker(textXKey, rect);

            val p1 = Point.of(100.0, 150.0)
            val p2 = Point.of(90.0, 250.0)
            val p4 = Point.of(200.0, 140.0)
            val p3 = Point.of(200.0, 250.0)
            val polyKey = Key.of("poly");
            provider.removeMarker(polyKey)

            val poly = Marker.polygon(p1, p2, p3, p4).markerOptions(
                MarkerOptions.builder().hoverTooltip("<b>Big Text</b>\nSome more Text")
                    .fillColor(Color.RED)
            );
            provider.addMarker(polyKey, poly);

//            updateClaims(c.source.server, api)



            /* OPACServerAddonRegister.EVENT.register{ server, perms, party, claims ->
                claims.register(object :IClaimsManagerListenerAPI{
                    override fun onWholeRegionChange(identifier: Identifier, i: Int, i1: Int) {}

                    override fun onChunkChange(
                        identifier: Identifier, i: Int, i1: Int, iPlayerChunkClaimAPI: IPlayerChunkClaimAPI?
                    ) {

                    }

                    override fun onDimensionChange(identifier: Identifier) {}
                })

            }
             */
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }


    private fun mapLink(c: CommandContext<ServerCommandSource>): Int {
        c.source.sendSystemMessage(Text.literal("http://localhost:8080/"))
        return 1
    }


    private fun updateClaims(server: MinecraftServer, api: Squaremap) {
        OpenPACServerAPI.get(server).serverClaimsManager.playerInfoStream.forEach { claimInfo ->
            var name = claimInfo.claimsName!!
            val id: Key
            if (StringUtils.isBlank(name)) {
                name = claimInfo.playerUsername.lowercase()
                println(name)

                if (name.length > 2 && name[0] == '"' && name[name.length - 1] == '"') {
                    name = name.substring(1, name.length - 1)
                    id = Key.of(name)
                    name += " claim"
                } else {
                    id = Key.of(name)
                    name += "'s claim"
                }
            } else {
                id = Key.of(name)
            }
            val displayName = name

            for (map in claimInfo.stream) {
                println("Claim:")
                val world =
                    api.getWorldIfEnabled(WorldIdentifier.parse(map.key.toString())).orElse(null) ?: break

                val provider = SimpleLayerProvider.builder("Test Render")
                    .showControls(true).defaultHidden(false).layerPriority(5).zIndex(250).build()

                val layerId = Key.of("test_render")
                if (!world.layerRegistry().hasEntry(layerId)) world.layerRegistry().register(layerId, provider)
val xc = map.value.stream.toList()
                map.value.stream.flatMap { it.stream }.toList().forEach {
                    provider.removeMarker(id)
                    println("render : $id - $it")
                    provider.addMarker(
                        id, Marker.rectangle(
                            Point.of(it.endX.toDouble(), it.endZ.toDouble()),
                            Point.of(it.startX.toDouble(), it.startZ.toDouble())
                        ).markerOptions(MarkerOptions.builder().hoverTooltip(displayName))
                    )
                }


            }
        }
    }
}