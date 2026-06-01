package ec.cityalerta.app.model.data.map

import com.google.android.gms.maps.model.LatLng

data class BarrioRiskState(
    val barrioId: String,
    val nombre: String,
    val center: LatLng,
    val radius: Double,
    val reportCount: Int,
    val fillColor: Int,
    val strokeColor: Int
)
