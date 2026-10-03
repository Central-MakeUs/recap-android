# Recap production-readiness audit — 2026-09-20

Audit baseline: `3657ed7e` (v1.1.4). Scope: the production Remote backend and the Mock-only paths that share runtime code. This is a correctness audit, not an architecture migration.

API reference: [live Swagger](https://re-cap.duckdns.org/swagger-ui/index.html) and its [OpenAPI document](https://re-cap.duckdns.org/v3/api-docs), read on 2026-09-20. No authenticated backend mutations were performed. The specification confirms the upload limit (1–20), `ORGANIZE_IN_PROGRESS` conflict, pending-result, cancel, and ack endpoints; it does not specify idempotency or a processing SLA.

## 1. Executive summary

Recap has useful production safeguards: bounded image preparation concurrency, explicit image ownership/recycling, cancellation-aware Retrofit error mapping, session-tagged shared Home/Storage data, account-owner isolation, backup exclusions, and a substantial regression suite. Debug and minified release builds passed before changes.

The main risks occur at ownership boundaries: token refresh versus sign-out, client cancellation versus server analysis, restored navigation versus in-memory work, and activity-lived ViewModels versus changing capture IDs. Full-resolution upload preparation also has a material OOM risk for long screenshots. The fixes in this audit reduce these risks without changing image quality or the server contract. Durable analysis recovery, destructive-account UX, and lifecycle scoping still need follow-up before low-maintenance operation.

### Runtime model established before edits

| Flow | Actual execution and ownership |
| --- | --- |
| Modules / DI | `:app` owns Application/activities, root navigation, notifications and Firebase adapters. Hilt `SingletonComponent` selects Mock/Remote repositories; `:core:data` owns APIs/storage, `:core:model` contracts, `:core:design` shared UI; feature modules own ViewModels/UI. |
| Startup | `RecapApplication` initializes Kakao, Crashlytics/Performance and notification collection. `RecapStartupViewModel` performs corruption recovery, combines onboarding and refresh-token presence, then prefetches Home/Storage. Offline alone does not erase a session. |
| Authentication | Kakao login → profile ID → salted owner hash → wipe/rebind if account differs → OAuth server login/device ID → DataStore tokens. Auth interceptor refreshes before expiry; authenticator handles 401. |
| Onboarding | Landing/login → guide steps → first analysis overlay; terminal dismissal completes onboarding. Skip completes immediately. Reauth reuses landing and returns to Main without replaying tutorial. |
| Gallery intake | MediaStore metadata query on IO → picker (max 20) → confirmation → consent check and speculative JPEG preparation. Selection and preparation generations reject superseded callbacks. |
| External share | Exported ShareReceiver validates action/type/URI and filters/limits images → confirmation → process-only one-shot request registry → MainActivity. JPEG arrays are not put in Intent/SavedState bundles. URI-only restoration cannot guarantee access after process loss. |
| Preprocessing | Singleton mutex → source bytes → bitmap decode (HEIC/HEIF fallback) → white alpha flatten → EXIF orientation → quality-75 JPEG. No upload pixel/byte cap; confirmation can retain 20 JPEG arrays. |
| Analysis | MainActivity ViewModel → issue presigned URLs → serial PUTs → organize POST → 1-second status polling → ack → capture invalidation/result UI. Partial preparation failures are counted. No persistent work queue; starting another run cancels the previous local job. |
| Background | Foreground service observes an in-memory progress tracker; it does not own/resume the analysis. `START_NOT_STICKY`; notification preference controls service use. Configuration changes retain the Activity ViewModel; process death loses the batch ID and work. |
| Persistence | Production capture truth is the server, with private thumbnail files and DataStore session/preferences/search/owner hash. Room is the Mock capture store and is also touched by account cleanup. Thumbnail downloads are deduplicated and limited to four, with generation invalidation on wipe. |
| Home / collection / search | Shared, session-tagged Remote Home/Storage summary streams; invalidation on mutations; error-only foreground/connectivity retry for main surfaces. Search/recent paging deduplicates capture IDs. Collection detail currently converts API failures into empty lists. |
| Detail / edit | Capture ID → detail API → cache/original URL → shared Detail/Edit/Fullscreen navigation and capture mutations. Display decoding has its own bounded image request policy. The navigation wrapper currently lacks a per-entry ViewModelStore decorator. |
| Settings / sign-out | Account/data/consent repositories → server mutation → local token/cache/owner cleanup. Root entry resets to Reauth. Withdrawal currently clears the local session even when the server DELETE fails. |
| Observability | Firebase Crashlytics keys/nonfatals and Performance traces; debug collection gate; Timber debug tree. No application product-analytics event implementation was found. Trace ownership already rejects stopping another run's trace. |

## 2. Critical findings

Critical here describes impact, not an assertion that every issue is a release-blocking P0.

### C1 — Refresh can exhaust the same dispatcher it needs to finish — fixed

- **Components:** `NetworkModule`, `AuthInterceptor`, `TokenAuthenticator`, `TokenRefreshCoordinator`, `AuthRepository`.
- **Scenario:** enough protected calls reach an expired token (or 401) concurrently. OkHttp workers block in `runBlocking` awaiting a refresh which Retrofit enqueues on the same client's per-host dispatcher. All slots can be occupied by waiters; the refresh cannot run. Connect/read timeouts do not resolve queue/interceptor starvation.
- **Root cause:** refresh and protected requests shared one async dispatcher.
- **Change:** dedicated, qualified refresh API/client with independent dispatcher and no auth interceptor/authenticator. Ordinary login/logout API behavior is retained.
- **Confidence:** high; real dispatcher saturation regression, not just a mocked call-order assertion.

### C2 — A late refresh can restore a signed-out session or clear its replacement — fixed for refresh

- **Components:** `SessionTokenStore`, `AuthRepository.refresh`, `TokenRefreshCoordinator`.
- **Scenario:** refresh starts, user signs out or signs into another account, then the old refresh returns. Previously its unconditional save could revive the old session; an invalid-refresh response could unconditionally clear a new one. Hydration and writes also published their memory cache without one shared critical section.
- **Change:** serialize hydration/save/clear; compare the current refresh token before committing refresh success or invalidation.
- **Confidence:** high; stale-response rejection and deferred store-ordering regressions. This is not a general generation fence for every account mutation; late logout/withdraw and account-wide cleanup remain follow-up work.

### C3 — Long screenshots require several full-size pixel allocations — reduced, not eliminated

- **Component:** `ScreenshotUploadPreparer`.
- **Scenario:** a 1440 × 30,000 scrolling screenshot contains 43.2 million pixels. One ARGB bitmap is about **165 MiB**. Before the fix, flattening could require three full-size allocations (~495 MiB), and EXIF conversion four (~659 MiB), excluding source bytes, JPEG output, previous selections and Coil. Per-pixel `Pair` allocations further pressure GC.
- **Change:** Canvas white compositing and native Matrix EXIF conversion remove the full-frame Kotlin arrays and per-pixel objects; an opaque bitmap can be reused. Two simultaneous bitmaps can still require ~330 MiB. These are allocation estimates, not measured device peaks.
- **Recommendation:** agree an OCR-preserving pixel/byte limit and stream/spool encoded inputs. Up to 20 prepared JPEG arrays remain retained. A mutex prevents parallel decodes, but cannot cap one pathological input.
- **Confidence:** high; dimensions/quality retained, all EXIF orientations and alpha blending protected by image tests.

### C4 — Process recreation restores a destination whose analysis no longer exists — navigation fixed; work recovery unresolved

- **Components:** `RecapNavHost`, `OnboardingFirstOrganizeHost`, first-organize reducer.
- **Scenario:** kill/recreate during analysis. Saved navigation/phase survives but the Activity analysis ViewModel and retained result do not. Main could show a blank status entry; onboarding could remain in AnalysisSession with no visible result, ineffective Back and a rejected OpenPicker action.
- **Change:** remove an orphaned status entry and reset an orphaned onboarding phase to Idle. Do not erase an active retained run or complete onboarding automatically.
- **Confidence:** high; restored-state regressions. This recovers navigation only; it does not recover a server batch.

### C5 — Server analysis can outlive local cancel/failure with no reconciliation — unresolved

- **Components:** `RemoteScreenshotAnalysisRepository`, `ScreenshotAnalysisProgressViewModel`, `MainActivity`, `CaptureRepository`, `OrganizeForegroundService`.
- **Scenario:** user cancels/back-navigates, shares a second batch, loses connectivity while polling, finishes the Activity, or loses the process after organize succeeded. Local state is cancelled/failed/lost, while the server may continue. A new run can receive documented 409 `ORGANIZE_IN_PROGRESS`, or a retry after an ambiguous response can redo server work.
- **Root cause:** batch ID only exists on the coroutine stack; pending-result/cancel methods have no production callers; ack occurs before the user consumes a durable result. Polling has no total deadline. Synchronous upload `execute()` is not linked to coroutine cancellation, so a cancelled call may still consume network resources until it returns.
- **Recommendation:** account-bound persisted batch identity, pending-result reconciliation on entry/foreground, explicit cancel-vs-leave semantics, and backend idempotency/SLA agreement. Do not add blind retries to organize POST.
- **Confidence:** high; execution trace and Swagger agree. Completed server captures are not claimed to be deleted; the lost state is the client operation/result and reliable retry knowledge.

### C6 — Failed account deletion is treated as sign-out — unresolved policy

- **Components:** `RemoteUserRepository.withdraw`, `AccountManagementViewModel`.
- **Scenario:** account DELETE fails offline or with an HTTP error. Tokens are cleared anyway, local account data is reset, and the user is routed out. The server account/data can remain; no failure/retry result is presented.
- **Root cause:** explicit unconditional local sign-out after a withdrawal attempt and ignored withdrawal Result in the ViewModel, reinforced by existing tests.
- **Recommendation:** separate successful deletion from failed/ambiguous deletion, retain a retry path, and agree privacy/support behavior. Do not silently redefine this intentional policy during an audit.
- **Confidence:** high about client behavior; backend deletion/retention after ambiguous transport failure is unknown.

### C7 — Authentication/privacy and storage failure boundary — fixed in the audited paths

- **Components:** `RecapAuthPaths`, `NetworkModule`, `AuthRepository.loginWithServer`.
- **Scenarios:** auth classification used only a URL path, so a foreign thumbnail/upload URL with an `/api/v1/` path could receive the Recap bearer token; HTTP BODY logging emitted provider/access/refresh tokens and screenshot content in debug; device ID DataStore failure happened before the login try/catch and escaped the Result contract into a ViewModel coroutine.
- **Changes:** require the configured API origin; remove sensitive HTTP wire logging; include device-ID read/create inside the login failure boundary.
- **Confidence:** high for path classification/logging/exception handling. There is no evidence the current server actually returned a malicious foreign URL. Production HTTP BODY logging was already disabled.

### C8 — Old requests can be retried as a newly signed-in account — unresolved

- **Components:** `TokenAuthenticator`, Activity-owned feature ViewModels, logout/withdraw cleanup.
- **Scenario:** account A starts an authenticated request, signs out and account B signs in while the old request is awaiting a response. A late 401 sees a changed access token; the authenticator treats it as normal rotation and retries with B's token. A user-scoped endpoint such as `/users/me/data` has no capture ID to separate the intended account. A late sign-out/withdraw completion can also clear B's session.
- **Root cause:** credentials rotate without a stable account/session generation attached to in-flight requests and terminal mutations; feature jobs survive root navigation.
- **Recommendation:** attach and validate a stable session generation for request/retry/publication and serialize sign-out cleanup with sign-in. Refresh-specific compare-and-set does not cover this larger boundary.
- **Confidence:** high in reachable client control flow; no cross-account destructive request was executed against the production server.

## 3. Important findings

| ID | Evidence / reachable behavior | Disposition / confidence |
| --- | --- | --- |
| I1 | `RecapNavDisplay` installs only saveable-state decoration. Feature `hiltViewModel()` instances can remain Activity-owned after pop/root change, retaining collectors and state. Capture A's delayed mutation can affect a newly bound capture B. | Targeted screenshot mutation guard applied; broader per-entry ownership remains P1. High. [Navigation3 ownership guidance](https://developer.android.com/guide/navigation/navigation-3/save-state). |
| I2 | Direct swipe-to-edit starts the inner Screenshot stack at Edit. Without an inner previous entry, system Back bypasses edit-discard handling. | Explicit root-edit BackHandler routes through existing discard/save guard. High; runtime gesture coverage still needed. |
| I3 | `RemoteStorageRepository.observeCapturesByType/observeFavoriteCaptures` uses `getOrElse { emptyList }`; collection-detail network failures look like legitimate empty collections and do not reach main overview retry logic. | P1: propagate loading/error/result and offer detail retry; contract change deferred. High. |
| I4 | `safeData` retries IO then rethrows. App-level observability/notification collectors and repository `shareIn` owners lack terminal error handling. Persistent DataStore IO failure can cause uncaught coroutine exceptions despite the startup ReadError UI. | P1: centralized failure/retry policy with fault-injection tests. High static confidence; no disk-fault runtime test. |
| I5 | `RemoteUserRepository.deleteAccountData` ignores all local cleanup exceptions, including cancellation; if Room deletion fails, thumbnail cleanup and invalidation are skipped. File cache has no size/age eviction; failed thumbnail downloads can leave null list thumbnails until another refresh. | P1: independently recover cleanup/invalidation and add bounded cache policy. High for control flow; storage growth depends on use. |
| I6 | `SearchRoute` disposes an active search session on configuration change; account/consent/data screen failures also have incomplete retry feedback. Rapid favorite toggles can overlap PATCH requests and rollback newer optimistic state. | P1/P2 targeted state-transition tests and per-item mutation ordering, no broad state rewrite. High for code paths. |
| I7 | Global font scale is capped at 1.5. Several screens contain fixed-height controls; Search uses fixed bottom padding and needs navigation/IME device QA. | Design/accessibility decision for scale cap; run TalkBack/small-screen/IME matrix. Do not label every decorative null description a defect. Static confidence; no complete accessibility certification. |
| I8 | Infinite PROCESSING polling plus a dataSync foreground service with no `onTimeout` handling can eventually meet Android 15+ time limits. Disabling the completion-notification preference also disables the foreground-service keepalive. | P1 long-work policy and platform timeout handling. High structural confidence; six-hour stall not reproduced. [Platform limit](https://developer.android.com/develop/background-work/services/fgs/timeout). |
| I9 | Room is version 1 with `exportSchema=false`; history includes same-version entity changes and there are no installed-release migration fixtures. A previously shipped incompatible DB would fail open/cleanup. | P2: establish which schemas shipped, export fixtures, test upgrades. Conditional deployment risk, not a proven current-user failure. |
| I10 | `androidTestImplementation` declared a versionless Compose test artifact without its BOM; lint/test APK dependency resolution failed. | Fixed by adding the existing BOM to that configuration; no library upgrade. [Official BOM setup](https://developer.android.com/develop/ui/compose/bom). High. |

### Additional account/settings risks

- **High confidence, P1:** Swagger explicitly documents refresh `404 USER_NOT_FOUND`, but `TokenRefreshCoordinator` only ends sessions for invalid/expired refresh tokens. Removed server users can remain in Main with repeated failures. Add contract-backed terminal-session handling.
- **High confidence, P1:** `TokenAuthenticator` assumes a changed access token is a rotation of the same account. It can replay an old request with a new account token; late logout/withdraw also clear tokens without checking account generation. The applied refresh guard does not claim to solve these transitions.
- **High confidence, P1:** an initial summary/consent failure in `DataManagementViewModel` disables its actions. There is no retry/reconnect/re-entry refresh; Activity-owned collectors keep this state alive. Reopening the screen can stay disabled until session rotation/another refresh. Mutation failures also return without actionable feedback.
- **High confidence, P1:** sign-out cleanup has no catch/recovery marker. Persistent storage failure can escape the ViewModel coroutine or leave a partial wipe. Current root navigation does not cancel these Activity-owned VMs; cancellation due to a future entry-scope migration is a design constraint, not a claimed current bug.
- **High confidence, P2:** Mock thumbnail EXIF 5/7 transforms in `ScreenshotImageStorage` are reversed; production Remote cache bypasses this local thumbnail generation. Defer with its own pixel regressions.

### Other audited areas

- Permissions correctly distinguish API 30–32 storage, API 33 media, and API 34+ selected images; MediaStore queries use IO and close cursors. Unsupported/unavailable sources are caught; inaccessible sources can still require reselection after process loss.
- MainActivity explicitly configures edge-to-edge and light system bars. Display-image requests are bounded and list UIs use lazy containers. API 30 blur/gesture/3-button and large-font behavior still need a device matrix; no blanket layout redesign is justified.
- Retrofit serialization uses generated Kotlin serializers, ignores unknown fields, and maps malformed/missing response data and 4xx/5xx/IO to error Results. Cancellation is rethrown in the main remote wrapper. Required enums are strict; new server enum values become recoverable request failures, not silently guessed categories.
- Hilt-injected `lateinit` fields are framework-managed; suspicious index/first calls reviewed in context had guards or were Preview-only. No GlobalScope was found. Intent registry prevents forged external intents from supplying an internal analysis request; external share URIs are still untrusted input.
- Private image streams and OkHttp response bodies generally use `use`; thumbnail invalidation includes generation checks. Long-lived application-context singletons are not themselves Activity leaks.
- Backup rules exclude Recap image files, Room DB, tokens/preferences and owner markers for cloud/device transfer. No claim is made about every third-party SDK's private persistence without a device extraction audit.
- Release/qa select Remote regardless of debug override. SDK range is min 30 / target 37 / compile 37.2; Java 17. Release optimization/R8 is enabled, qa inherits release with debug signing. Existing Kakao/Retrofit keep rules plus serializer consumer rules compile under minification; compilation alone does not prove live OAuth correctness.
- No analytics taxonomy redesign was made. Performance traces and safe booleans/counts were examined; debug URI/file-name logs and exception payloads still merit a documented privacy policy. Release Timber has no planted tree.

## 4. Changes made and verification

| Modification | Before → after / reason | Verification |
| --- | --- | --- |
| `NetworkModule` refresh DI | Shared dispatcher could starve → dedicated refresh client/API without recursive auth. | Five protected async slots saturated while refresh completes; core-data tests and debug compilation. |
| `SessionTokenStore`, `AuthRepository`, refresh coordinator | Unconditional late refresh writes/clear and competing cache publication → serialized store plus token-conditional commit/invalidation. Device-ID IO exceptions now become login failure Results. | Stale-session replacement/clear, deferred persistence ordering and device-ID-failure regressions. |
| `RecapAuthPaths`, HTTP logging | Path-only credential classification and BODY logs → configured-origin requirement and no HTTP wire logging. | Foreign origin/protocol/port and auth-endpoint classification tests; static privacy review. |
| `ScreenshotUploadPreparer` | Full-frame arrays and per-pixel objects → Canvas/Matrix, opaque reuse, alias-safe recycle. Same resolution and JPEG quality 75. | 14 image tests: all EXIF orientations, pixel positions, alpha, PNG/JPEG, cancellation cleanup, serialized preparation; existing HEIC/HEIF shadow paths retained. |
| Main/onboarding analysis restoration | Saved destination with missing in-memory operation → navigable Main/Idle state. | Four navigation/reducer regression cases; no premature onboarding completion or active-run reset. |
| `ScreenshotViewModel` | Capture A's late results/events could affect B → generation-checked mutation publication and buffered events. | Four delayed/queued completion cases, including A→B→A. |
| `ScreenshotRoute` | Direct root Edit delegated system Back outside discard logic → explicit BackHandler; no exit during save. | Static Navigation3 execution trace and compilation; actual predictive gesture/unsaved-edit UI journey remains manual QA. |
| Build/test declarations | Missing Android-test Compose BOM and screenshot JUnit5 setup → existing BOM/Jupiter/Vintage wired correctly. `core:data` now declares the network-state permission already present in the app. | Full tests/build/lint; merged app Manifest contains one ACCESS_NETWORK_STATE declaration. No production dependency/version added. |
| Notification/toast boundary | Terminal notification had no SecurityException boundary → permission changes cannot escape that call. Toast flags use the same platform text/icon bits directly, resolving cross-module IntDef lint. | App tests, lint and compilation; existing timeout/policy tests retained. No warning suppression. |
| Audit documentation | Previously scattered backlog → runtime map, evidence, limits and prioritized follow-ups in this report and `BACKLOG.md`. | Source/OpenAPI cross-check and independent auth review. |

**Final verification:**

- `testDebugUnitTest assembleDebug`: **PASS**, 684 tests in current included modules, zero failures/errors/skips. Obsolete build outputs from the removed `feature:mypage` module are excluded from the count.
- After the final notification/Manifest changes, `:app:testDebugUnitTest :core:data:testDebugUnitTest assembleDebug`: **PASS**.
- `lintDebug`: **PASS**, zero errors; 70 warnings retained. They include dependency-version notices, asset/text warnings, onboarding pager recomposition warnings and notification constants whose API gate is in a helper. No blanket suppressions or dependency upgrades were applied.
- `assembleRelease assembleQa :app:assembleDebugAndroidTest`: **PASS**, including R8 optimization. Release APK is unsigned; QA is minified and debug-signed.
- API 37 emulator: minified QA update-install and ordinary cold start **PASS** (`MainActivity` resumed and process remained alive, no crash in its PID buffer). This is a startup smoke check, not a full UI journey.
- Matching debug app/test APKs: Android instrumentation **PASS**, 1 application-context test. APKs were installed with `adb install -r`; no app-data clear or uninstall was performed. The emulator was returned to the final debug APK.
- A debug test APK initially run against minified QA failed before tests with missing `kotlin.jvm.internal.Intrinsics` in the AndroidX runner. The mismatched-variant attempt is not counted as passing or as an ordinary QA app crash; use a QA-targeted test build for future minified instrumentation.

The initial clean baseline also passed debug tests/debug/minified release. An all-variants + all-module instrumentation + lint invocation exceeded the existing 2 GiB Gradle daemon heap. Final verification is split and uses an invocation-only `'-Dorg.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8' --max-workers=2`; `gradle.properties` is unchanged. The broad instrumentation attempt also reached an empty core-data test APK whose configured AndroidJUnitRunner was absent; no all-module connected-test pass is claimed. Initial test-integration issues (missing Jupiter in screenshot and native host HEIC decoder support) were repaired and the regressions rerun; no failing or skipped test was relabelled as passing.

### Meaningful test coverage and remaining gaps

Existing tests already protect consent/preparation ordering and cancellation, URI filtering/share restoration and one-shot consumption, startup/session routing, token refresh errors, capture response mapping, local transactional persistence, account-owner wipe policy, thumbnail invalidation and terminal progress mapping. This audit adds focused tests for the bugs above rather than a coverage target.

Still missing are end-to-end process death with a real accepted server batch, cancel/re-share reconciliation, accepted POST with a lost response, cancellable HTTP PUT, reversed favorite responses, sustained disk faults, and measured 20-image memory peaks. The existing Android instrumentation test is only an application-context smoke test, so passing it is not a substitute for these journeys. HEIC/HEIF host adapter tests do not prove a real device codec; transform pixel tests use Robolectric native graphics.

## 5. Unresolved risks

- Pending-result semantics, idempotency, cancellation outcome, maximum processing duration and retryable status codes need a documented backend/client contract. Swagger describes endpoints but does not answer every ambiguous-failure question.
- Full-resolution decode and the retained batch of JPEG arrays can still exhaust low-memory devices; the native conversion fix is a reduction, not a hard memory bound.
- Account deletion failure policy, image quality limits, notification/keepalive coupling and the font-scale cap need product decisions.
- Activity-wide feature ViewModels need an ownership migration that also makes account cleanup safe when its originating screen is removed. Adding a decorator everywhere without that analysis could introduce cancellation regressions.
- Minified APK compilation is not live OAuth/upload validation. Real accounts, server errors/rate limits, API 30/34 devices, TalkBack and destructive backend flows were not exercised against production.

## 6. Technical debt backlog

The shared actionable backlog is also maintained in `docs/BACKLOG.md`.

- **P0 — production correctness:** fence old authenticated requests from a newly signed-in account; define reliable account-deletion outcome/retry; reconcile accepted analysis batches after process/network loss and prevent ambiguous duplicate work. These priorities reflect privacy and incorrect-operation outcomes, not code style.
- **P1 — reliability/performance:** hard upload memory budget/spooling; feature ViewModel ownership; collection detail error/retry; DataStore failure containment; ordered favorite mutations; cancellable uploads, foreground timeout policy; cache eviction and cleanup recovery.
- **P2 — maintainability:** installed-schema migration fixtures; deterministic API fault tests (429/5xx/invalid JSON/missing fields/late results); Navigation3 process/back tests on devices; documented logging/privacy and accessibility policies. Keep existing dispatcher DI work incremental and tied to needed tests.
- **P3 — optional cleanup:** no new cosmetic work proposed. Existing test-framework unification and module dependency tidy-up should not displace the correctness items above.

## 7. Recommended next actions

1. Fence request retry and account cleanup by a stable account/session generation; handle terminal refresh USER_NOT_FOUND.
2. Design and implement account-bound analysis recovery using pending-result, persisted batch identity and explicit ack timing.
3. Separate account deletion failure/ambiguity from successful deletion; expose an actionable retry path.
4. Establish a measured upload pixel/byte budget with OCR-quality acceptance tests on long screenshots and 20-image batches.
5. Scope feature ViewModels to navigation entries while preserving intentional Activity analysis ownership and durable account cleanup.
6. Propagate collection-detail failures and serialize rapid favorite updates with regression tests.
7. Fault-inject DataStore/cache failures and prevent uncaught application-scope collectors from crashing startup.
8. Make upload cancellation and foreground service timeout behavior explicit; test back/background/network-loss combinations.
9. Run signed minified QA on API 30, 34 and 37: login/refresh/share, process kill/reentry, gesture/3-button Back, large font/IME, and TalkBack.
10. Export shipped Room schemas and maintain upgrade fixtures; bound thumbnail cache size and recover interrupted deletions.
