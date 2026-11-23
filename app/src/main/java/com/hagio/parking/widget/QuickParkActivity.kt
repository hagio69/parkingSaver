package com.hagio.parking.widget

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hagio.parking.data.ParkingRepository
import com.hagio.parking.ui.ParkDialog
import com.hagio.parking.ui.ParkingTheme

/**
 * 위젯을 눌렀을 때 홈 화면 위에 '위치 저장' 대화상자만 띄우는 화면.
 * 저장하거나 취소하면 바로 닫혀서 홈 화면으로 돌아간다.
 */
class QuickParkActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_CAR_ID = "car_id"

        fun intent(ctx: Context, carId: String): Intent =
            Intent(ctx, QuickParkActivity::class.java)
                .putExtra(EXTRA_CAR_ID, carId)
                // 위젯마다·차량마다 다른 PendingIntent 가 되도록 data 를 구분
                .setData(android.net.Uri.parse("parking://car/$carId"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val car = intent.getStringExtra(EXTRA_CAR_ID)?.let { ParkingRepository.getCar(this, it) }
        if (car == null) {
            Toast.makeText(this, "차량을 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        setContent {
            ParkingTheme {
                ParkDialog(
                    car = car,
                    onDismiss = { finish() },
                    onSave = { floor, zone ->
                        ParkingRepository.saveParking(this, car.id, floor, zone)
                        Toast.makeText(this, "${car.name} 위치를 저장했습니다", Toast.LENGTH_SHORT).show()
                        finish()
                    },
                )
            }
        }
    }
}
