package com.abulubad.sojourn.core.designsystem.components.map

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.tokens.darkSojournColors
import com.abulubad.sojourn.core.designsystem.tokens.lightSojournColors
import com.abulubad.sojourn.core.model.GeoBounds
import com.abulubad.sojourn.core.model.GeoPoint
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SojournMapTest {

    private val paddington = GeoPoint(51.5180, -0.1800)
    private val nextDoor = GeoPoint(51.5182, -0.1798)
    private val shoreditch = GeoPoint(51.5262, -0.0790)

    @Test
    fun `the projection goes there and back`() {
        val world = SojournMapProjection.world(paddington, 12.0)
        val back = SojournMapProjection.point(world, 12.0)
        // Through DpOffset (floats): within a metre at city zoom.
        assertTrue(abs(back.latitude - paddington.latitude) < 1e-5)
        assertTrue(abs(back.longitude - paddington.longitude) < 1e-5)
    }

    @Test
    fun `the centre of the map is the centre of the screen`() {
        val size = DpSize(400.dp, 800.dp)
        val at = SojournMapProjection.screen(paddington, paddington, 13.0, size)
        assertEquals(200.0, at.x.value.toDouble(), 1e-3)
        assertEquals(400.0, at.y.value.toDouble(), 1e-3)
    }

    @Test
    fun `a fit keeps every point on screen`() {
        val size = DpSize(400.dp, 800.dp)
        val bounds = GeoBounds.around(listOf(paddington, shoreditch))!!
        val camera = SojournMapProjection.fit(bounds, size, 48.dp, maxZoom = 18.0)
        listOf(paddington, shoreditch).forEach { point ->
            val at = SojournMapProjection.screen(point, camera.center, camera.zoom, size)
            assertTrue(at.x.value in 47f..353f, "$point at $at")
            assertTrue(at.y.value in 0f..800f, "$point at $at")
        }
    }

    @Test
    fun `pins that would overlap become one cluster and the kept pin stays its own`() {
        val points = listOf("a" to paddington, "b" to nextDoor, "c" to shoreditch)
        val city = sojournPinGroups(points, zoom = 12.0, position = { it.second })
        assertEquals(2, city.size)
        val cluster = assertIs<SojournPinGroup.Cluster<Pair<String, GeoPoint>>>(city.first())
        assertEquals(listOf("a", "b"), cluster.items.map { it.first })

        val street = sojournPinGroups(points, zoom = 19.0, position = { it.second })
        assertEquals(3, street.size)

        val kept = sojournPinGroups(points, zoom = 12.0, position = { it.second }, keep = { it.first == "b" })
        assertEquals(3, kept.size)
    }

    @Test
    fun `the map style is the theme's map tokens and the app's language`() {
        listOf(lightSojournColors(), darkSojournColors()).forEach { colors ->
            val style = sojournMapStyle(colors, "ar")
            listOf(colors.mapLand, colors.mapWater, colors.mapPark, colors.mapRoad).forEach { color ->
                assertTrue(color.hex in style, "${color.hex} missing")
            }
            assertTrue("name:ar" in style)
            assertTrue(SojournMapDefaults.TILES_URL in style)
        }
    }
}
