package org.teamvoided.template.compat

import org.teamvoided.template.data.Settlement

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