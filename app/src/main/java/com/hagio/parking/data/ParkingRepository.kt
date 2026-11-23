package com.hagio.parking.data

import android.content.Context
import com.hagio.parking.widget.WidgetUpdater
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * 차량 목록을 SharedPreferences 에 JSON 으로 저장한다.
 * 데이터가 바뀔 때마다 모든 홈 화면 위젯을 갱신한다.
 */
object ParkingRepository {
    private const val PREFS = "parking_data"
    private const val KEY_CARS = "cars"

    private fun prefs(ctx: Context) =
        ctx.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getCars(ctx: Context): List<Car> {
        val raw = prefs(ctx).getString(KEY_CARS, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Car(
                    id = o.getString("id"),
                    name = o.optString("name"),
                    floor = o.optString("floor"),
                    zone = o.optString("zone"),
                    savedAt = o.optLong("savedAt"),
                )
            }
        }.getOrDefault(emptyList())
    }

    fun getCar(ctx: Context, id: String): Car? = getCars(ctx).firstOrNull { it.id == id }

    private fun save(ctx: Context, cars: List<Car>) {
        val arr = JSONArray()
        cars.forEach { c ->
            arr.put(
                JSONObject()
                    .put("id", c.id)
                    .put("name", c.name)
                    .put("floor", c.floor)
                    .put("zone", c.zone)
                    .put("savedAt", c.savedAt)
            )
        }
        prefs(ctx).edit().putString(KEY_CARS, arr.toString()).apply()
        WidgetUpdater.updateAll(ctx)
    }

    fun addCar(ctx: Context, name: String): Car {
        val car = Car(id = UUID.randomUUID().toString(), name = name.trim())
        save(ctx, getCars(ctx) + car)
        return car
    }

    fun renameCar(ctx: Context, id: String, name: String) =
        save(ctx, getCars(ctx).map { if (it.id == id) it.copy(name = name.trim()) else it })

    fun saveParking(ctx: Context, id: String, floor: String, zone: String) =
        save(ctx, getCars(ctx).map {
            if (it.id == id) it.copy(
                floor = floor.trim(),
                zone = zone.trim(),
                savedAt = System.currentTimeMillis(),
            ) else it
        })

    fun clearParking(ctx: Context, id: String) =
        save(ctx, getCars(ctx).map {
            if (it.id == id) it.copy(floor = "", zone = "", savedAt = 0L) else it
        })

    fun deleteCar(ctx: Context, id: String) =
        save(ctx, getCars(ctx).filterNot { it.id == id })

    // ---- 위젯 ID → 차량 ID 연결 (1대용 위젯) ----

    private fun widgetKey(widgetId: Int) = "widget_$widgetId"

    fun setWidgetCar(ctx: Context, widgetId: Int, carId: String) =
        prefs(ctx).edit().putString(widgetKey(widgetId), carId).apply()

    fun getWidgetCarId(ctx: Context, widgetId: Int): String? =
        prefs(ctx).getString(widgetKey(widgetId), null)

    fun removeWidget(ctx: Context, widgetId: Int) =
        prefs(ctx).edit().remove(widgetKey(widgetId)).apply()
}
