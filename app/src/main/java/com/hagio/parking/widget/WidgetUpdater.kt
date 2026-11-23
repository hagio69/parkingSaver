package com.hagio.parking.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.hagio.parking.MainActivity
import com.hagio.parking.R
import com.hagio.parking.data.ParkingRepository

/** 위젯 화면(RemoteViews)을 그리는 곳 */
object WidgetUpdater {

    private val rowIds = intArrayOf(R.id.row1, R.id.row2, R.id.row3, R.id.row4)
    private val nameIds = intArrayOf(R.id.name1, R.id.name2, R.id.name3, R.id.name4)
    private val locIds = intArrayOf(R.id.loc1, R.id.loc2, R.id.loc3, R.id.loc4)
    private val dateIds = intArrayOf(R.id.date1, R.id.date2, R.id.date3, R.id.date4)

    /** 앱 데이터가 바뀌면 호출 — 홈 화면의 모든 위젯 갱신 */
    fun updateAll(ctx: Context) {
        val mgr = AppWidgetManager.getInstance(ctx)
        mgr.getAppWidgetIds(ComponentName(ctx, SingleCarWidget::class.java))
            .forEach { updateSingle(ctx, mgr, it) }
        mgr.getAppWidgetIds(ComponentName(ctx, AllCarsWidget::class.java))
            .forEach { updateAllCars(ctx, mgr, it) }
    }

    private fun openAppIntent(ctx: Context, requestCode: Int, carId: String?): PendingIntent {
        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (carId != null) putExtra(MainActivity.EXTRA_CAR_ID, carId)
        }
        return PendingIntent.getActivity(
            ctx, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun updateSingle(ctx: Context, mgr: AppWidgetManager, widgetId: Int) {
        val views = RemoteViews(ctx.packageName, R.layout.widget_single)
        val car = ParkingRepository.getWidgetCarId(ctx, widgetId)
            ?.let { ParkingRepository.getCar(ctx, it) }

        if (car == null) {
            views.setTextViewText(R.id.tv_name, "차량 없음")
            views.setTextViewText(R.id.tv_floor, "-")
            views.setTextViewText(R.id.tv_zone, "-")
            views.setTextViewText(R.id.tv_date, "위젯을 길게 눌러 차량 선택")
        } else {
            views.setTextViewText(R.id.tv_name, car.name)
            views.setTextViewText(R.id.tv_floor, if (car.hasParking) car.floorLabel else "-")
            views.setTextViewText(R.id.tv_zone, if (car.hasParking) car.zoneLabel else "-")
            views.setTextViewText(R.id.tv_date, car.savedAtLong())
        }
        views.setOnClickPendingIntent(R.id.widget_root, openAppIntent(ctx, widgetId, car?.id))
        mgr.updateAppWidget(widgetId, views)
    }

    fun updateAllCars(ctx: Context, mgr: AppWidgetManager, widgetId: Int) {
        val views = RemoteViews(ctx.packageName, R.layout.widget_all)
        val cars = ParkingRepository.getCars(ctx)

        views.setViewVisibility(R.id.tv_empty, if (cars.isEmpty()) View.VISIBLE else View.GONE)

        for (i in rowIds.indices) {
            val car = cars.getOrNull(i)
            if (car == null) {
                views.setViewVisibility(rowIds[i], View.GONE)
                continue
            }
            views.setViewVisibility(rowIds[i], View.VISIBLE)
            views.setTextViewText(nameIds[i], car.name)
            views.setTextViewText(
                locIds[i],
                if (car.hasParking) "${car.floorLabel} · ${car.zoneLabel}" else "미저장"
            )
            views.setTextViewText(dateIds[i], car.savedAtShort())
        }

        val extra = cars.size - rowIds.size
        if (extra > 0) {
            views.setViewVisibility(R.id.tv_more, View.VISIBLE)
            views.setTextViewText(R.id.tv_more, "외 ${extra}대")
        } else {
            views.setViewVisibility(R.id.tv_more, View.GONE)
        }

        views.setOnClickPendingIntent(R.id.widget_root, openAppIntent(ctx, 100_000 + widgetId, null))
        mgr.updateAppWidget(widgetId, views)
    }
}
