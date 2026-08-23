# CLAUDE.md — KakaoTalk Notification Filter

Read this file before any other. It prevents redundant exploration and tells you exactly what to look at for each task type.

---

## Architecture

```
Clean Architecture + MVVM + Hilt + Coroutines/Flow

domain/          ← 순수 Kotlin. Android 의존 없음.
  model/         ← FilterRule, NotificationLog (data class)
  repository/    ← 인터페이스만. data/ 에서 구현.
  usecase/       ← operator fun invoke() 하나씩. VM은 repo 직접 호출 금지.

data/
  local/db/      ← Room: AppDatabase (v1), FilterRuleDao, NotificationLogDao
  local/prefs/   ← DataStore: listenerEnabled
  remote/auth/   ← FirebaseAuthProvider (Google Sign-In → Firebase Auth ID 토큰)
  repository/    ← *Impl. Hilt @Binds 연결은 core/di/RepositoryModule.kt

service/
  KakaoNotificationListenerService  ← onNotificationPosted() 진입점
  FirestoreSyncWorker                ← WorkManager CoroutineWorker (재시도)

ui/              ← Compose MVVM
  theme/         ← Material3 Color/Type/Theme
  screen/        ← dashboard, rules, log, settings
  component/     ← 공유 Composable (stateless)

core/
  di/            ← AppModule, DatabaseModule, FirebaseModule, RepositoryModule
  util/          ← Constants, Extensions, NotificationPermissionHelper
  navigation/    ← AppNavHost, Screen
```

> ⚠️ **2026-08-22 — 구글시트 릴레이를 Firestore로 교체(banana-portfolio-v2 ADR 0014
> 참고)**. 원래(`remote/sheets/` + `GoogleOAuthProvider` + `SheetsSyncWorker`)는
> 구글시트 "알람" 탭에 원문을 append하는 구조였다. Mac 쪽(banana-portfolio-v2)이 이제
> Firestore `kakaoInbox` 컬렉션을 직접 읽도록 바뀌면서, 이 앱도 Firestore에 직접
> 쓰도록 재작성됐다 — Retrofit/OkHttp/Moshi(Sheets REST 호출 전용이었음)와
> `data/remote/sheets/*` 전체가 제거됐다. 아래 표·패턴 설명은 새 구조 기준.

---

## Key Entry Points

| 작업 | 먼저 읽을 파일 |
|------|---------------|
| 알림 감지/필터링 | `service/KakaoNotificationListenerService.kt` |
| 매칭 로직 | `domain/usecase/MatchNotificationUseCase.kt` |
| Firestore 동기화 | `service/FirestoreSyncWorker.kt` → `domain/usecase/SyncToFirestoreUseCase.kt` |
| 필터 규칙 UI | `ui/screen/rules/FilterRulesViewModel.kt` |
| DI 연결 | `core/di/RepositoryModule.kt` → `FirebaseModule.kt` |
| DB 스키마 | `data/local/db/AppDatabase.kt` |
| Firebase 인증 | `data/remote/auth/FirebaseAuthProvider.kt` |
| 네비게이션 | `core/navigation/AppNavHost.kt` |
| 사용자 설정 | `data/local/preferences/AppPreferences.kt` |

---

## Patterns

### UseCase
```kotlin
class SomethingUseCase @Inject constructor(private val repo: SomeRepository) {
    suspend operator fun invoke(param: Foo): Result<Bar> { ... }
}
```
ViewModel → UseCase → Repository. ViewModel이 Repository 직접 호출 금지.

### Repository
- 인터페이스: `domain/repository/` (순수 Kotlin)
- 구현체: `data/repository/`에 `Impl` 접미사
- Hilt 바인딩: `core/di/RepositoryModule.kt` `@Binds`

### Flow vs suspend
- Room 목록 조회 → `Flow<List<T>>` (ViewModel에서 `collectAsStateWithLifecycle`)
- 네트워크/WorkManager 단발성 → `suspend fun`

### Result<T>
모든 data layer 경계에서 `runCatching { }` 사용. ViewModel은 `Result`를 `UiState` sealed class로 변환.

### Hilt EntryPoint (NLS)
`NotificationListenerService`는 `@AndroidEntryPoint` 불가 → `@EntryPoint` 인터페이스 + `EntryPointAccessors.fromApplication()` lazy 초기화.

### KakaoTalk 알림 추출
```kotlin
sbn.packageName == "com.kakao.talk"          // 패키지 필터
EXTRA_TITLE                                   // 발신자
EXTRA_BIG_TEXT ?: EXTRA_TEXT                  // 메시지 본문
```

### WorkManager 트리거
알림 매칭 → Room 저장 → `SyncToFirestoreUseCase` 즉시 호출.
네트워크 실패 시 `FirestoreSyncWorker` 예약 (네트워크 제약 + 지수 백오프).

---

## 읽지 않아도 되는 파일 (시간 절약)

- `ui/theme/` — Material3 보일러플레이트. 비즈니스 로직 없음.
- `res/values/` — 문자열/스타일만. 로직 작업 시 무시.
- `gradle/wrapper/` — 의존성 추가/업그레이드 시에만 확인.
- `build.gradle.kts` (root) — 의존성 작업 시에만 확인.
- `ui/component/` — 상태 없는 Composable. UI 렌더링 버그 시에만 확인.
- `domain/model/` — 안정적인 data class. 새 필드 추가 시에만 확인.

---

## Quick Reference

| 항목 | 값 |
|------|-----|
| 패키지 루트 | `com.minepapa.kakaonotification` |
| 카카오톡 패키지 | `com.kakao.talk` |
| Room DB 이름 | `kakao_notification_db` |
| Room 버전 | 1 (마이그레이션은 AppDatabase.kt) |
| WorkManager 태그 | `firestore_sync_worker` |
| Firestore 컬렉션 | `kakaoInbox` (banana-portfolio-v2의 `scripts/lib/kakao-inbox.mjs`가 읽음) |
| DataStore 키 | `LISTENER_ENABLED` |
| 알림 extras 키 | `EXTRA_TITLE` (발신자), `EXTRA_BIG_TEXT`, `EXTRA_TEXT` |

### 매칭 알고리즘 (MatchNotificationUseCase)
- `senderMatch`: `rule.senderName.isBlank()` OR 제목에 발신자 포함 (대소문자 무시)
- `keywordMatch`: `rule.keywords.isEmpty()` OR 본문에 키워드 하나라도 포함 (대소문자 무시)
- 두 조건 AND

### Firestore 문서 스키마 (kakaoInbox)
```
{ ts: "YYYY-MM-DD HH:mm:ss"(KST), sender, body, createdAt: serverTimestamp() }
```
쓰기 권한은 Firestore 보안규칙에서 본인 Google 계정(이메일)으로만 제한(create 전용,
read/update/delete는 항상 거부 — Mac 쪽 Admin SDK만 전체 읽기/삭제 가능).

---

## 세션 시작 체크리스트

1. 이 파일 읽기 — 완료
2. `core/di/RepositoryModule.kt`, `FirebaseModule.kt` 읽기 (의존성 파악)
3. 작업 유형에 맞는 진입점 파일 읽기 (위 표 참고)
4. `git log --oneline -10` 확인
5. `ui/theme/`, `res/`, `gradle/wrapper/` 읽지 않기 (작업과 무관한 경우)
