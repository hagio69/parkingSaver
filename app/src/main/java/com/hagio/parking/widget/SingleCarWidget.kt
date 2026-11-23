package com.hagio.parking.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import com.hagio.parking.data.ParkingRepository

/** 차량 1대를 크게 보여주는 위젯 (놓을 때 차량 선택) */
class SingleCarWidget : AppWidgetProvider() {
    override fun onUpdate(ctx: Context, mgr: AppWidgetManager, ids: IntArray) {
        ids.forEach { WidgetUpdater.updateSingle(ctx, mgr, it) }
    }

    override fun onDeleted(ctx: Context, ids: IntArray) {
        ids.forEach { ParkingRepository.removeWidget(ctx, it) }
    }
}
