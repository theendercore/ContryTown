package org.teamvoided.civilization.util

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos

@Suppress("MagicNumber")
fun PlayerEntity.teleport(pos: BlockPos) = this.teleport(pos.x + 0.5, pos.y + 0.2, pos.z + 0.5)
