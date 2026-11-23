# 주차 위치 (ParkingSaver)

Android용 주차 위치 저장 앱 — Kotlin + Jetpack Compose, 홈 화면 위젯 2종.

## 실행
1. Android Studio(Ladybug 이상)에서 `File > Open` → 이 폴더 선택
2. Gradle Sync가 끝나면 기기 연결 후 ▶ Run

## 기능
- 차량 여러 대 등록 (이름 변경·삭제)
- 차량별 주차 위치 저장: 층(빠른 선택 칩 B6~5층 + 직접 입력), 구역, 저장 시각 자동 기록
- 위젯
  - **주차 위치 (1대)** 2×2 — 놓을 때 차량 선택, 길게 눌러 다른 차량으로 재설정
  - **주차 위치 (전체)** 4×2 — 등록 차량 최대 4대를 한 줄씩 (5대 이상이면 "외 N대")
  - 앱에서 저장하면 즉시 위젯 갱신, 위젯을 누르면 앱이 열림

## 구조
```
app/src/main/java/com/hagio/parking/
├─ MainActivity.kt              앱 화면 (차량 카드 목록)
├─ data/Car.kt                  차량 모델, 날짜·층 표시 형식
├─ data/ParkingRepository.kt    SharedPreferences(JSON) 저장 + 위젯 연결
├─ ui/Dialogs.kt                위치 저장 / 이름 / 확인 대화상자
├─ ui/Theme.kt                  Material 3 (Android 12+ 다이내믹 컬러)
└─ widget/                      위젯 Provider, 갱신, 차량 선택 화면
app/src/main/res/layout/widget_*.xml   위젯 레이아웃
```
