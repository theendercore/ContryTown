package org.teamvoided.civilization.compat

import org.teamvoided.civilization.data.Nation
import org.teamvoided.civilization.data.Settlement
import org.teamvoided.civilization.managers.SettlementManager

object WebMaps {

    var squaremapEnabled = false


    fun addSettlement(settlement: Settlement) {
        if (squaremapEnabled) SquaremapIntegrations.addSettlementMarker(settlement)
    }

    fun addNation(nation: Nation) {
        if (squaremapEnabled) {
            nation.settlements.forEach { SquaremapIntegrations.modifySettlement(SettlementManager.getById(it)!!) }
        }
    }

    fun modifySettlement(settlement: Settlement) {
        if (squaremapEnabled) SquaremapIntegrations.modifySettlement(settlement)


    }

    fun removeSettlement(settlement: Settlement) {
        if (squaremapEnabled) SquaremapIntegrations.removeSettlementMarker(settlement)

    }
}
