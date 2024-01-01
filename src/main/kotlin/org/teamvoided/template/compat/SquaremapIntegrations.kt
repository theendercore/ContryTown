package org.teamvoided.template.compat

import org.teamvoided.template.data.Settlement
import org.teamvoided.template.data.SettlementsManager
import xyz.jpenilla.squaremap.api.Key
import xyz.jpenilla.squaremap.api.Point
import xyz.jpenilla.squaremap.api.SimpleLayerProvider
import xyz.jpenilla.squaremap.api.SquaremapProvider
import xyz.jpenilla.squaremap.api.marker.Marker
import xyz.jpenilla.squaremap.api.marker.MarkerOptions
import xyz.jpenilla.squaremap.api.marker.Rectangle
import java.awt.Color


object SquaremapIntegrations {

    private val markerLayers = HashMap<String, SimpleLayerProvider>()
    private const val MARKER_ID = "civilization"
    private const val LABEL = "Civilization"
    fun reg() {
        val api = SquaremapProvider.get()

        for (level in api.mapWorlds()) {
            val tempId: Key =
                Key.of(MARKER_ID + "-" + level.identifier().namespace() + "-" + level.identifier().value())

            if (!level.layerRegistry().hasEntry(tempId)) {
                val layer: SimpleLayerProvider =
                    SimpleLayerProvider.builder(LABEL).defaultHidden(true).zIndex(23).layerPriority(23)
                        .showControls(true).build()
                level.layerRegistry().register(tempId, layer)
                markerLayers[level.identifier().asString()] = layer
            }
        }
        Thread {
            for (settlement in SettlementsManager.getAllSettlement()) {
                addSettlementMarker(settlement)
            }
        }.start()
        WebMaps.squaremapEnabled = true
    }

    fun addSettlementMarker(settlement: Settlement) {
        if (markerLayers.isEmpty()) return
//        Use polygons instead
//        val points = mutableListOf<Point>()
        settlement.chunks.forEach { pos ->
            val marker: Rectangle = Marker.rectangle(
                Point.of(pos.startX.toDouble(), pos.startZ.toDouble()),
                Point.of(pos.endX.toDouble(), pos.endZ.toDouble())
            )
            marker.markerOptions(
                MarkerOptions.builder().hoverTooltip(settlement.name).strokeColor(Color.YELLOW).strokeOpacity(0.8)
                    .strokeWeight(3).fillColor(Color.YELLOW).fillOpacity(0.2).build()
            )
            markerLayers[settlement.dimension.toString()]!!.addMarker(
                Key.of(settlement.formatId()), marker
            )
        }
    }

    fun removeSettlementMarker(settlement: Settlement){
        if (markerLayers.isEmpty()) return
        markerLayers[settlement.dimension.toString()]!!.removeMarker(Key.of(settlement.formatId()))
    }
}