package org.teamvoided.civilization.compat

import org.teamvoided.civilization.data.Settlement

object WebMaps {

    var squaremapEnabled = false


    fun addSettlement(settlement: Settlement) {
        if (squaremapEnabled) SquaremapIntegrations.addSettlementMarker(settlement)
    }

    fun modifySettlement(settlement: Settlement) {
        if (squaremapEnabled) {
            SquaremapIntegrations.removeSettlementMarker(settlement)
            SquaremapIntegrations.addSettlementMarker(settlement)
        }

    }

    fun removeSettlement(settlement: Settlement) {
        if (squaremapEnabled) SquaremapIntegrations.removeSettlementMarker(settlement)

    }
}