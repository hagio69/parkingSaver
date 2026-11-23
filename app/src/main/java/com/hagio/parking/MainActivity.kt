package com.hagio.parking

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hagio.parking.data.Car
import com.hagio.parking.data.ParkingRepository
import com.hagio.parking.ui.ConfirmDialog
import com.hagio.parking.ui.NameDialog
import com.hagio.parking.ui.ParkDialog
import com.hagio.parking.ui.ParkingTheme

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_CAR_ID = "car_id"
    }

    /** 위젯을 눌러 들어왔을 때 강조할 차량 */
    private var highlightId by mutableStateOf<String?>(null)

    /** 화면에 돌아올 때마다 증가 → 위젯에서 바꾼 내용도 목록에 반영 */
    private var resumeCount by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        highlightId = intent.getStringExtra(EXTRA_CAR_ID)
        setContent { ParkingTheme { ParkingScreen(highlightId, resumeCount) } }
    }

    override fun onResume() {
        super.onResume()
        resumeCount++
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        highlightId = intent.getStringExtra(EXTRA_CAR_ID)
    }
}

/** 지금 어떤 대화상자가 열려 있는지 */
private sealed interface DialogState {
    data object AddCar : DialogState
    data class Park(val car: Car) : DialogState
    data class Rename(val car: Car) : DialogState
    data class Delete(val car: Car) : DialogState
    data class Clear(val car: Car) : DialogState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParkingScreen(highlightId: String?, resumeCount: Int) {
    val ctx = LocalContext.current
    var cars by remember { mutableStateOf(ParkingRepository.getCars(ctx)) }
    var dialog by remember { mutableStateOf<DialogState?>(null) }
    fun reload() { cars = ParkingRepository.getCars(ctx); dialog = null }
    LaunchedEffect(resumeCount) { cars = ParkingRepository.getCars(ctx) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("주차 위치") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { dialog = DialogState.AddCar },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("차량 추가") },
            )
        },
    ) { padding ->
        if (cars.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    "오른쪽 아래 '차량 추가'로\n차를 먼저 등록하세요",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(cars, key = { it.id }) { car ->
                    CarCard(
                        car = car,
                        highlighted = car.id == highlightId,
                        onPark = { dialog = DialogState.Park(car) },
                        onRename = { dialog = DialogState.Rename(car) },
                        onClear = { dialog = DialogState.Clear(car) },
                        onDelete = { dialog = DialogState.Delete(car) },
                    )
                }
            }
        }
    }

    when (val d = dialog) {
        null -> Unit
        DialogState.AddCar -> NameDialog(
            title = "차량 추가",
            onDismiss = { dialog = null },
            onConfirm = { ParkingRepository.addCar(ctx, it); reload() },
        )
        is DialogState.Park -> ParkDialog(
            car = d.car,
            onDismiss = { dialog = null },
            onSave = { f, z -> ParkingRepository.saveParking(ctx, d.car.id, f, z); reload() },
        )
        is DialogState.Rename -> NameDialog(
            title = "이름 바꾸기",
            initial = d.car.name,
            onDismiss = { dialog = null },
            onConfirm = { ParkingRepository.renameCar(ctx, d.car.id, it); reload() },
        )
        is DialogState.Clear -> ConfirmDialog(
            title = "위치 지우기",
            message = "${d.car.name}의 주차 위치를 지울까요?",
            confirmText = "지우기",
            onDismiss = { dialog = null },
            onConfirm = { ParkingRepository.clearParking(ctx, d.car.id); reload() },
        )
        is DialogState.Delete -> ConfirmDialog(
            title = "차량 삭제",
            message = "${d.car.name}을(를) 목록에서 삭제할까요? 이 차를 보여주던 위젯은 '차량 없음'으로 바뀝니다.",
            confirmText = "삭제",
            onDismiss = { dialog = null },
            onConfirm = { ParkingRepository.deleteCar(ctx, d.car.id); reload() },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CarCard(
    car: Car,
    highlighted: Boolean,
    onPark: () -> Unit,
    onRename: () -> Unit,
    onClear: () -> Unit,
    onDelete: () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    // 카드 아무 곳이나 누르면 위치 저장 창
    Card(
        onClick = onPark,
        modifier = Modifier.fillMaxWidth(),
        border = if (highlighted) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Column(Modifier.padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    car.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Box {
                    IconButton(onClick = { menu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "메뉴")
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text("이름 바꾸기") }, onClick = { menu = false; onRename() })
                        if (car.hasParking) {
                            DropdownMenuItem(text = { Text("위치 지우기") }, onClick = { menu = false; onClear() })
                        }
                        DropdownMenuItem(text = { Text("차량 삭제") }, onClick = { menu = false; onDelete() })
                    }
                }
            }

            Row(Modifier.padding(end = 12.dp)) {
                ValueBlock("층", if (car.hasParking) car.floorLabel else "-", Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                ValueBlock("구역", if (car.hasParking) car.zoneLabel else "-", Modifier.weight(1f))
            }
            Spacer(Modifier.height(4.dp))
            Text(
                car.savedAtLong(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "눌러서 위치 저장",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun ValueBlock(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 30.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}
