package com.hagio.parking.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hagio.parking.data.Car
import com.hagio.parking.data.ParkingRepository
import com.hagio.parking.ui.NameDialog
import com.hagio.parking.ui.ParkingTheme

/** 1대용 위젯을 홈 화면에 놓을 때(또는 길게 눌러 재설정할 때) 차량을 고르는 화면 */
class WidgetConfigActivity : ComponentActivity() {

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // 사용자가 뒤로 가면 위젯 추가 취소
        setResult(Activity.RESULT_CANCELED, resultIntent())
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) { finish(); return }

        setContent {
            ParkingTheme {
                var cars by remember { mutableStateOf(ParkingRepository.getCars(this)) }
                var adding by remember { mutableStateOf(false) }

                Scaffold(topBar = { TopAppBar(title = { Text("위젯에 표시할 차량") }) }) { padding ->
                    Column(
                        Modifier.fillMaxSize().padding(padding).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (cars.isEmpty()) {
                            Text("등록된 차량이 없습니다. 먼저 추가하세요.")
                        }
                        LazyColumn(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(cars, key = { it.id }) { car -> CarRow(car) { choose(car) } }
                        }
                        OutlinedButton(onClick = { adding = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("새 차량 추가")
                        }
                    }
                }

                if (adding) {
                    NameDialog(
                        title = "차량 추가",
                        onDismiss = { adding = false },
                        onConfirm = {
                            ParkingRepository.addCar(this, it)
                            cars = ParkingRepository.getCars(this)
                            adding = false
                        },
                    )
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun CarRow(car: Car, onClick: () -> Unit) {
        Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(car.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(
                    if (car.hasParking) "${car.floorLabel} · ${car.zoneLabel}" else "미저장",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    private fun choose(car: Car) {
        ParkingRepository.setWidgetCar(this, widgetId, car.id)
        WidgetUpdater.updateSingle(this, AppWidgetManager.getInstance(this), widgetId)
        setResult(Activity.RESULT_OK, resultIntent())
        finish()
    }

    private fun resultIntent() = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
}
