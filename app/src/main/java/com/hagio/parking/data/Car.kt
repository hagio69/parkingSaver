package com.hagio.parking.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 차량 1대와 그 차의 마지막 주차 위치 */
data class Car(
    val id: String,
    val name: String,
    val floor: String = "",
    val zone: String = "",
    /** 주차 위치를 저장한 시각 (epoch ms). 0 이면 아직 저장 안 함 */
    val savedAt: Long = 0L,
) {
    val hasParking: Boolean get() = savedAt > 0L

    /** 예: "B2층" — 사용자가 이미 '층'이나 'F'를 붙였으면 그대로 */
    val floorLabel: String
        get() = when {
            floor.isBlank() -> "-"
            floor.endsWith("층") || floor.endsWith("F", ignoreCase = true) -> floor
            else -> "${floor}층"
        }

    val zoneLabel: String get() = zone.ifBlank { "-" }

    /** 예: "11월 23일 (월) 09:04" */
    fun savedAtLong(): String =
        if (!hasParking) "저장된 위치 없음"
        else SimpleDateFormat("M월 d일 (E) HH:mm", Locale.KOREA).format(Date(savedAt))

    /** 예: "B2층 · A-13" */
    val locationLabel: String get() = "$floorLabel · $zoneLabel"

    /** 예: "11.23(월) 09:04" — 2×1 위젯용 */
    fun savedAtMedium(): String =
        if (!hasParking) "미저장"
        else SimpleDateFormat("M.d(E) HH:mm", Locale.KOREA).format(Date(savedAt))

    /** 예: "11.23 09:04" — 전체 위젯처럼 좁은 곳용 */
    fun savedAtShort(): String =
        if (!hasParking) "-"
        else SimpleDateFormat("M.d HH:mm", Locale.KOREA).format(Date(savedAt))
}
