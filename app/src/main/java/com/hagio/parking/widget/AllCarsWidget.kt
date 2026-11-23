package com.hagio.parking.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

/** 저장된 모든 차량을 한 줄씩 보여주는 위젯 (최대 4대) */
class AllCarsWidget : AppWidgetProvider() {
    override fun onUpdate(ctx: Context, mgr: AppWidgetManager, ids: IntArray) {
        ids.forEach { WidgetUpdater.updateAllCars(ctx, mgr, it) }
    }
}
