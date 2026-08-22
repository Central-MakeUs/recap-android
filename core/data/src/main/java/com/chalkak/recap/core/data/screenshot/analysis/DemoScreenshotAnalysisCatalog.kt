package com.chalkak.recap.core.data.screenshot.analysis

import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import java.time.Instant

/**
 * 데모 기기 스크린샷 폴더의 고정 20장에 대한 분석 결과.
 * 조회 키는 원본 파일명(`LocalImage.displayName`과 동일한 `demo_N.ext`)이다.
 */
object DemoScreenshotAnalysisCatalog {
    val fileNames: Set<String>
        get() = byFileName.keys

    val results: List<ScreenshotAnalysisResult>
        get() = entries.map { it.second }

    fun resultForFileName(fileName: String): ScreenshotAnalysisResult? {
        val key = fileName.substringAfterLast('/').substringAfterLast('\\')
        return byFileName[key]
    }

    private val entries: List<Pair<String, ScreenshotAnalysisResult>> = listOf(
        capture(
            fileName = "demo_1.jpeg",
            captureId = 2_026_000_001L,
            typeCode = ScreenshotContentType.SCHEDULE,
            title = "사카리 오라모 & BBC 심포니 예매 완료",
            summary = "2026년 3월 25일 예술의전당 콘서트홀 사카리 오라모 & BBC 심포니 오케스트라 예매 내역입니다.",
            organizedAt = "2026-03-12T12:00:00+09:00",
            body = "일시: 2026-03-25 (수) 19:30\n" +
                    "장소: 예술의전당 콘서트홀\n" +
                    "내용: 사카리 오라모 & BBC 심포니 오케스트라 with 손열음 (3.25)\n" +
                    "상태: 예매 완료\n" +
                    "예약번호: T2931098300\n" +
                    "\n" +
                    "상세 정보\n" +
                    "- 좌석: R석 2층 C블록2열 1\n" +
                    "- 예매매수: 1매\n" +
                    "- 출처: 인터파크 티켓 (mticket.interpark.com)\n" +
                    "- 고객센터: 1544-1555\n" +
                    "\n"
        ),
        capture(
            fileName = "demo_2.png",
            captureId = 2_026_000_002L,
            typeCode = ScreenshotContentType.KNOWLEDGE,
            title = "주니어 DA를 위한 전략적 사고 가이드",
            summary = "주니어 데이터 분석가가 목표 지표와 핵심 레버를 정의하고 설득하는 전략적 사고 방법입니다.",
            organizedAt = "2026-07-15T12:00:00+09:00",
            body = "주제: 주니어 DA의 전략적 사고\n" +
                    "결론: 목표 Y를 위해 행동 X를 N기간 내에 M번 시킨다는 문장으로 방향성을 제시해야 한다\n" +
                    "\n" +
                    "핵심 내용\n" +
                    "- 수동적인 데이터 추출기에서 벗어나 전략적 사고로 방향성 제시\n" +
                    "- 전략적 사고란 답이 없는 문제에서 맞을 가능성이 높은 시나리오를 그리고 주장·설득하는 것\n" +
                    "- 수많은 변수(찜하기, 장바구니, 구매버튼 클릭 등) 중 진짜 핵심 레버 X 좁히기\n" +
                    "- 지표를 팀 모두가 같은 기준으로 볼 수 있도록 명확히 정의\n" +
                    "- 얼마나 올려야 의미 있는지 목표치 설정\n" +
                    "- 그 판단이 왜 맞는지 도메인 지식으로 뒷받침해서 설득\n" +
                    "\n"
        ),
        capture(
            fileName = "demo_3.png",
            captureId = 2_026_000_003L,
            typeCode = ScreenshotContentType.KNOWLEDGE,
            title = "AI 활용 회사 분석 및 지원동기 작성 팁",
            summary = "회사 소개와 채용공고를 AI에 입력해 맞춤형 지원동기를 도출하는 5단계 가이드입니다.",
            organizedAt = "2026-06-24T09:34:00+09:00",
            body = "주제: AI를 활용한 회사 분석 및 지원동기 작성\n" +
                    "결론: AI는 회사 정보를 내 맞춤형 지원 이유로 바꿔주는 도구이다\n" +
                    "\n" +
                    "방법\n" +
                    "1. 회사 소개를 넣음\n" +
                    "2. 채용공고를 넣음\n" +
                    "3. 최근 사업 방향을 정리함\n" +
                    "4. 내 경험과 연결함\n" +
                    "5. 지원동기로 바꿈\n" +
                    "\n" +
                    "핵심 내용\n" +
                    "- 회사 분석은 검색보다 AI 정리가 빠름\n" +
                    "- 아는 척보다 맞춤형이 중요함\n" +
                    "- AI는 회사 정보를 내 지원 이유로 바꿔주는 도구임\n" +
                    "- 보조자로 활용할 때 좋은 결과 도출 가능\n" +
                    "\n"
        ),
        capture(
            fileName = "demo_4.png",
            captureId = 2_026_000_004L,
            typeCode = ScreenshotContentType.SCHEDULE,
            title = "TOEIC Speaking Test 접수 완료",
            summary = "2026년 4월 18일 광주 YBM CBT센터에서 진행되는 토익스피킹 시험 접수 내역입니다.",
            organizedAt = "2026-04-02T12:00:00+09:00",
            body = "일시: 2026-04-18 (토) 11:30\n" +
                    "장소: 광주 - YBM 광주CBT센터/5층-상무지구 BYC빌딩\n" +
                    "내용: TOEIC Speaking Test\n" +
                    "상태: 접수 완료\n" +
                    "참고: 11:40부터 입실불가, 규정신분증 지참\n" +
                    "\n" +
                    "상세 정보\n" +
                    "- 시험명: TOEIC Speaking Test\n" +
                    "- 입실 마감: 11:40 (11:40부터 입실불가)\n" +
                    "- 준비물: 규정신분증\n" +
                    "- 변경/취소: 시험일 변경 시 취소 후 재접수 필요 (동일인만 응시 가능)\n" +
                    "- 출처: YBM TOEIC Speaking and Writing tests\n" +
                    "\n"
        ),
        capture(
            fileName = "demo_5.png",
            captureId = 2_026_000_005L,
            typeCode = ScreenshotContentType.PLACE,
            title = "코지로네일",
            summary = "서울대입구역 인근 네일아트 및 네일샵 전문점 코지로네일 매장 정보입니다.",
            organizedAt = "2026-06-10T12:00:00+09:00",
            body = "장소명: 코지로네일\n" +
                    "위치: 서울 관악구 남부순환로231길 12 1층\n" +
                    "영업시간: 10:00 영업 시작\n" +
                    "전화: 0507-1482-0838\n" +
                    "예약: 예약 가능\n" +
                    "\n" +
                    "방문 팁\n" +
                    "- 서울대입구역 8번 출구에서 675m\n" +
                    "- 리뷰 184개 등록된 네일아트·네일샵\n" +
                    "- 카카오톡 채널: http://pf.kakao.com/_BNwxoG\n" +
                    "- 네이버 지도에서 예약, 전화, 길찾기, 공유 지원\n" +
                    "\n"
        ),
        capture(
            fileName = "demo_6.png",
            captureId = 2_026_000_006L,
            typeCode = ScreenshotContentType.KNOWLEDGE,
            title = "입영 준비물 체크리스트",
            summary = "군 입대 전 챙겨야 할 세면도구, 의약품, 필기구, 신분증 등 지퍼백 포장 준비물 목록입니다.",
            organizedAt = "2026-07-08T12:00:00+09:00",
            body = "주제: 군 입영 준비물 점검 목록\n" +
                    "출처: 군인아들 부모님카페\n" +
                    "결론: 세면용품, 의약품, 필기구, 필수 서류 등을 지퍼백 단위로 분류하여 준비한다\n" +
                    "\n" +
                    "핵심 내용\n" +
                    "- 세면: 텀블러, 발포세정제, 올인원워시, 폼클렌징, 올인원로션, 선크림, 선스틱, 물티슈, 휴지, 치약, 칫솔, 빨래망, 섬유탈취제\n" +
                    "- 의약품: 모기기피제, 버물리, 모기패치(물리기전,후), 메디폼습윤밴드, 듀오덤, 밴드, 방수밴드, 물집방지패드, 파스, 맨소래담쿨롤, 뿌리는소독약, 열패치, 면봉, 처방약(감기약, 코스프레이, 알러지비염약, 해열진통제), 타이레놀, 종합감기약, 스트렙실, 탁센, 마데카솔\n" +
                    "- 필기·잡화: 여분안경, 우산, 네임펜, 삼색볼펜, 라이트펜, 사격용귀마개, 안대, 지퍼백, 작은수첩, 편지지\n" +
                    "- 필수 서류: 입영통지서, 나사카(나라사랑카드), 신분증\n" +
                    "\n"
        ),
        capture(
            fileName = "demo_7.png",
            captureId = 2_026_000_007L,
            typeCode = ScreenshotContentType.PLACE,
            title = "호숫가 가족 호텔 위생·방음 리뷰",
            summary = "호숫가 위치는 좋으나 침구 위생 불량과 방음 문제로 3.0점을 기록한 호텔 투숙 후기입니다.",
            organizedAt = "2026-01-05T12:00:00+09:00",
            body = "장소명: 호숫가 가족 호텔\n" +
                    "평점: 3.0 / 10\n" +
                    "작성일: 2026-01-04\n" +
                    "투숙일: 2025년 12월\n" +
                    "객실: 디럭스 룸\n" +
                    "여행 유형: 1인 여행자\n" +
                    "\n" +
                    "리뷰 요약\n" +
                    "- 침대 시트 및 이불 위생 불량(오염 및 벌레 물림 발생)\n" +
                    "- 방음 불량으로 옆방 소음(코골이) 피해\n" +
                    "- 호숫가 바로 옆 위치만 장점으로 평가\n" +
                    "\n"
        ),
        capture(
            fileName = "demo_8.jpg",
            captureId = 2_026_000_008L,
            typeCode = ScreenshotContentType.JOB,
            title = "B2B 외국계 기업 찾는 법",
            summary = "산업군 선정부터 KOTRA 외투기업 채용관 활용까지 외국계 B2B 기업 탐색 순서를 다룬 가이드입니다.",
            organizedAt = "2026-07-22T12:00:00+09:00",
            body = "주제: B2B 외국계 기업 취업 탐색 순서\n" +
                    "출처: Threads (@career_coach_joanne)\n" +
                    "결론: 관심 산업군을 고르고 KOTRA 외투기업 채용관을 통해 외국인투자기업 공고를 확인한 후 채용 홈페이지 인재풀에 등록한다\n" +
                    "\n" +
                    "방법\n" +
                    "1. 관심 산업군 선정: 헬스케어, 제조, IT, 물류 등 분야 선택\n" +
                    "2. KOTRA 외투기업 채용관 탐색: 잡코리아 내 전용관 또는 구글 검색을 통해 외국인투자기업 채용공고 일괄 확인\n" +
                    "3. 전략적 지원: 산업군 → 외국계 기업 리스트업 → 채용 홈페이지 확인 → 인재풀 등록 순으로 접근\n" +
                    "\n"
        ),
        capture(
            fileName = "demo_9.jpg",
            captureId = 2_026_000_009L,
            typeCode = ScreenshotContentType.KNOWLEDGE,
            title = "프로다운 업무 보고 작성법 3가지",
            summary = "동사형 수치 활용, 마감 기한 시각화, 대안 제시를 통해 효과적으로 업무를 보고하는 3가지 방법입니다.",
            organizedAt = "2026-07-23T12:00:00+09:00",
            body = "주제: 프로페셔널한 업무 보고 작성법\n" +
                    "출처: Threads (@brandynkk)\n" +
                    "결론: 명사 나열 대신 결과 중심의 동사형 수치, 마감 기한 시각화, 대안을 포함한 의견을 제시하여 임팩트를 전달한다\n" +
                    "\n" +
                    "방법\n" +
                    "1. 명사형 나열 대신 '동사형 수치'로 쓰기: 완료한 결과와 임팩트 중심 작성 (예: 마케팅 시안 3종 검토 완료 및 차주 실행 예산 15% 절감안 확정)\n" +
                    "2. '진행 중'인 일은 마감 기한 시각화하기: 진척률과 데드라인 명시 (예: A 프로젝트 기획안 작성 (현재 진척률 70%, 00일 최종 보고 예정))\n" +
                    "3. 문제점(Issue) 적을 땐 내 의견(Opinion) 같이 박기: 대안을 함께 제시하여 리더십 표현 (예: 00 부서 리소스 부족으로 지연 중, 금주 중 기획 범위 조정 후 재협의 예정)\n" +
                    "\n"
        ),
        capture(
            fileName = "demo_10.png",
            captureId = 2_026_000_010L,
            typeCode = ScreenshotContentType.KNOWLEDGE,
            title = "금융공기업 준비, 선배의 조언",
            summary = "경영 직렬 선배가 성향별 A매치 금융공기업 선택과 스펙 호환 B매치 플랜 B 설정법을 조언한 글입니다.",
            organizedAt = "2025-09-04T12:00:00+09:00",
            body = "주제: 금융공기업(금공) 경영직렬 준비 전략\n" +
                    "출처: 에브리타임 전남대 광주캠 자유게시판\n" +
                    "작성일: 09/03 16:01\n" +
                    "결론: 성향에 맞는 A매치를 선정하고 시험 과목 및 스펙이 호환되는 B매치를 플랜 B로 설정하여 2학년 전 인강으로 선행 학습한다\n" +
                    "\n" +
                    "핵심 내용\n" +
                    "- 무보·수은: 어학능력과 다양한 대외활동형 스펙이 맞는 경우 유리\n" +
                    "- 산은·거래소·금감원: 회계 적성이 맞고 CPA 1차 합격 수준의 학습량을 소화할 수 있는 경우 유리\n" +
                    "- 한은: 타 기관과 합격생 특징이 상이함\n" +
                    "- B매치 연계: 산은 목표 기준 미시·거시·국제 경제학 보강 시 주금공 제외 B매치 공기업 지원 가능\n" +
                    "- 학습 팁: 2학년 진학 전 인강 1회 수강 후 학부 수업을 복습으로 활용\n" +
                    "\n"
        ),
        capture(
            fileName = "demo_11.jpg",
            captureId = 2_026_000_011L,
            typeCode = ScreenshotContentType.SCHEDULE,
            title = "극장판 귀멸의 칼날: 무한성편 예매 완료",
            summary = "2025년 8월 22일 CGV 광주금남로 2관 영화 예매 완료 내역입니다.",
            organizedAt = "2025-08-09T12:00:00+09:00",
            body = "일시: 2025-08-22 (금) 21:30 ~ 24:15\n" +
                    "장소: CGV-광주금남로 2관 (리클라이너,Laser)\n" +
                    "내용: 극장판 귀멸의 칼날: 무한성편\n" +
                    "상태: 예매 완료\n" +
                    "예약번호: 2025-0809-7805-2387\n" +
                    "\n" +
                    "상세 정보\n" +
                    "- 관람인원: 성인 1\n" +
                    "- 관람좌석: E13\n" +
                    "- 결제금액: 0원\n" +
                    "- 취소기한: 2025-08-22 (금) 21:00까지\n" +
                    "\n"
        ),
        capture(
            fileName = "demo_12.jpg",
            captureId = 2_026_000_012L,
            typeCode = ScreenshotContentType.PLACE,
            title = "섬진강 벚꽃길 방문 팁",
            summary = "한국에서 가장 아름다운 길 100선 섬진강 벚꽃길 주차 및 코스 정보입니다.",
            organizedAt = "2026-04-06T12:00:00+09:00",
            body = "장소명: 섬진강 벚꽃길\n" +
                    "구간: 섬진강 ~ 하동 (약 100리)\n" +
                    "특징: 한국에서 가장 아름다운 길 100선 선정\n" +
                    "\n" +
                    "방문 팁\n" +
                    "- 네이버 내비에 '섬진강 변 벚꽃축제'로 검색해야 주차장이 나옴\n" +
                    "- 산책과 드라이브 코스 모두 추천\n" +
                    "\n"
        ),
        capture(
            fileName = "demo_13.jpg",
            captureId = 2_026_000_013L,
            typeCode = ScreenshotContentType.CONTENT,
            title = "poemmag 최근 구매한 시집과 소설",
            summary = "인스타그램 poemmag 계정에서 소개한 최근 구매 도서 6권 목록입니다.",
            organizedAt = "2026-08-05T12:00:00+09:00",
            body = "제목: 최근 구매한 책\n" +
                    "유형: 도서 (시집·소설·산문)\n" +
                    "제작자: 고명재, 헤르만 헤세, 권승재, 이면우, 최승자 외\n" +
                    "플랫폼: Instagram (poemmag)\n" +
                    "\n" +
                    "도서 목록\n" +
                    "- 어깨에 머리를 기대던 시절 (고명재, 난다)\n" +
                    "- 데미안 (헤르만 헤세)\n" +
                    "- 다들 시를 뭐라고 생각하는 걸까 (권승재, 타이피스트)\n" +
                    "- 우울과 경청 (이면우, 창비)\n" +
                    "- 한 게으른 시인의 이야기 (최승자, 난다)\n" +
                    "- 인간을 생각하면 잠이 와 (아침달)\n" +
                    "\n" +
                    "인용\n" +
                    "\"이런 거 샀어요\"\n" +
                    "\n"
        ),
        capture(
            fileName = "demo_14.png",
            captureId = 2_026_000_014L,
            typeCode = ScreenshotContentType.PLACE,
            title = "성수 무신사 투어 코스",
            summary = "성수역 인근 무신사 오프라인 매장 5곳을 하루에 둘러보는 쇼핑 코스입니다.",
            organizedAt = "2026-08-01T12:00:00+09:00",
            body = "장소명: 성수 무신사 투어\n" +
                    "위치: 성수역 4번 출구 (도보 2분)\n" +
                    "특징: 성수동 무신사 오프라인 매장 하루 투어 코스\n" +
                    "\n" +
                    "코스 안내\n" +
                    "- 무신사 with 소담상회: 유니크한 소상공인 브랜드 아이템\n" +
                    "- 무신사 엠프티 성수: 해외 및 국내 라이징 브랜드\n" +
                    "- 무신사 스토어 성수 대림창고: 샵인샵 브랜드존 및 팝업존\n" +
                    "- 무신사 뷰티 스페이스 1: 주기적으로 핫한 뷰티 팝업 성지\n" +
                    "- 무신사 스탠다드 성수점: 언제 입어도 좋은 베이직템\n" +
                    "\n" +
                    "방문 팁\n" +
                    "- 성수역 4번 출구에서 시작해 순서대로 이동\n" +
                    "\n"
        ),
        capture(
            fileName = "demo_15.png",
            captureId = 2_026_000_015L,
            typeCode = ScreenshotContentType.JOB,
            title = "대한항공 승무원 자소서 1번 가이드",
            summary = "대한항공 객실승무원 지원동기 및 직무 적합성 문항 작성 요령입니다.",
            organizedAt = "2026-07-28T12:00:00+09:00",
            body = "주제: 대한항공 객실승무원 자기소개서 1번 문항 작성 가이드\n" +
                    "결론: 단순한 꿈 나열보다 현장에서 기준을 지키며 일할 준비도를 증명해야 한다\n" +
                    "\n" +
                    "핵심 내용\n" +
                    "- 문항: 대한항공 객실승무원 지원 이유 및 직무 적합성 서술 (최대 600자)\n" +
                    "- 왜 대한항공인가: 통합 대한항공 방향성, 글로벌 메가 캐리어 도약, 브랜드 가치(안전·서비스·신뢰)\n" +
                    "- 왜 객실승무원인가: 안전 업무 최우선 인식, 팀워크·커뮤니케이션, 제한된 공간 및 돌발 상황 대응\n" +
                    "- 말 잘하는 사람보다 상황 속에서 기준을 지킬 수 있는 승무원 선호\n" +
                    "\n"
        ),
        capture(
            fileName = "demo_16.png",
            captureId = 2_026_000_016L,
            typeCode = ScreenshotContentType.BENEFIT,
            title = "2026 서울시 희망두배 청년통장 신규 모집",
            summary = "서울시 거주 근로 청년 대상 매월 15만 원 저축 시 시 매칭으로 최대 1,080만 원을 지원합니다.",
            organizedAt = "2026-05-23T12:00:00+09:00",
            body = "혜택: 매월 15만 원 저축 시 서울시 매칭 15만 원 지원 (2년 만기 720만 원 / 3년 만기 1,080만 원)\n" +
                    "마감일: 2026-06-19 (금)\n" +
                    "대상: 서울시 거주 만 18세~34세 근로 청년 (월평균 근로소득 255만 원 이하 등)\n" +
                    "선발인원: 10,000명\n" +
                    "신청방법: 서울시자산형성지원사업 홈페이지 온라인 신청 (https://account.welfare.seoul.kr)\n" +
                    "문의: 1688-1453\n" +
                    "\n" +
                    "조건\n" +
                    "- 서울시 거주 만 18세~34세 근로 중인 청년\n" +
                    "- 공고일 기준 최근 1년간 3개월 이상 근로하였거나 현재 3개월 이상 근로 중인 자\n" +
                    "- 월 평균 근로소득 255만 원 이하 (자세한 신청자격은 공고문 확인)\n" +
                    "\n" +
                    "지원 내용\n" +
                    "- 본인 저축액 월 15만 원 + 서울시 매칭 지원금 월 15만 원\n" +
                    "- 2년 만기: 720만 원 + 이자\n" +
                    "- 3년 만기: 1,080만 원 + 이자\n" +
                    "\n" +
                    "참여 방법\n" +
                    "- 신청 기간: 2026.06.08 (월) ~ 2026.06.19 (금)\n" +
                    "- 서울시자산형성지원사업 홈페이지(https://account.welfare.seoul.kr) 온라인 신청\n" +
                    "\n"
        ),
        capture(
            fileName = "demo_17.jpg",
            captureId = 2_026_000_017L,
            typeCode = ScreenshotContentType.RECORD,
            title = "5/11~5/15 주간 식단표",
            summary = "5월 11일부터 15일까지 요일별 7,000원 점심 식단 메뉴 안내입니다.",
            organizedAt = "2026-05-11T12:00:00+09:00",
            body = "출처: 구내식당 주간 식단표\n" +
                    "날짜: 2026-05-11 (월) ~ 2026-05-15 (금)\n" +
                    "가격: 7,000원 (한식류, 분식류, 사이드 메뉴 포함)\n" +
                    "\n" +
                    "식단 내용\n" +
                    "- 월요일: 백미밥, 잡곡밥 / 두부된장국 / 돈육두루치기 / 생선까스 / 단호박샐러드 / 메밀막국수 / 쌈채소&쌈장 / 파절이무침 / 배추김치\n" +
                    "- 화요일: 백미밥, 잡곡밥 / 어묵무채국 / 안동찜닭 / 양념순두부 / 맛살튀김 / 꽈리고추멸치볶음 / 미나리오이초무침 / 샐러드 / 쪽파김치\n" +
                    "- 수요일: 백미밥, 잡곡밥 / 돈육김치찌개 / 순살치킨 / 감자스팸짜글이 / 물만두 / 오징어젓 / 무말랭이무침 / 홀그레인오이무침 / 블루베리샐러드 / 백김치\n" +
                    "- 목요일: 백미밥, 잡곡밥 / 메밀국수 / 오징어불고기 / 고기산적조림 / 씨리얼 연근튀김 / 메밀면 / 콩나물김가루무침 / 방울토마토샐러드 / 깍두기\n" +
                    "- 금요일: 백미밥, 잡곡밥 / 황태콩나물국 / 목살찹스테이크 / 돈까스 / 라볶이 / 어묵채볶음 / 청경채찜 / 샐러드 / 배추김치\n" +
                    "\n" +
                    "참고 사항\n" +
                    "- 식자재 수급에 따라 메뉴가 임의 변경될 수 있음\n" +
                    "\n"
        ),
        capture(
            fileName = "demo_18.jpg",
            captureId = 2_026_000_018L,
            typeCode = ScreenshotContentType.CONTENT,
            title = "알폰스 무하: 빛과 꿈 전시 패키지",
            summary = "더현대 서울 ALT1에서 진행되는 알폰스 무하 전시 도슨트 및 티켓 10% 할인 안내입니다.",
            organizedAt = "2026-02-10T11:31:00+09:00",
            body = "제목: 알폰스 무하: 빛과 꿈 (도슨트+티켓 패키지)\n" +
                    "유형: 전시\n" +
                    "제작자: 알폰스 무하\n" +
                    "플랫폼: YouTube\n" +
                    "장소: 더현대 서울 ALT1\n" +
                    "관람기간: ~2026-03-04\n" +
                    "혜택: 10% 할인 (한정 수량, 댓글 링크 확인)\n" +
                    "문의: business@artculture4u.kr\n" +
                    "\n" +
                    "핵심 내용\n" +
                    "- 아르누보 거장 알폰스 무하 전시 도슨트 및 티켓 패키지 안내\n" +
                    "- 더현대 서울 ALT1에서 2026년 3월 4일까지 관람 가능\n" +
                    "- 유튜브 커뮤니티 댓글 링크를 통한 10% 할인 티켓 한정 수량 제공\n" +
                    "\n" +
                    "인용\n" +
                    "\"《알폰스 무하: 빛과 꿈 (도슨트+티켓 패키지)》\"\n" +
                    "\n"
        ),
        capture(
            fileName = "demo_19.jpeg",
            captureId = 2_026_000_019L,
            typeCode = ScreenshotContentType.SCHEDULE,
            title = "파라타항공 WE503 인천-나리타 탑승 안내",
            summary = "2026년 6월 25일 11시 20분 인천발 나리타행 WE503편 탑승수속 및 카운터 안내입니다.",
            organizedAt = "2026-06-24T15:39:00+09:00",
            body = "일시: 2026-06-25 (목) 11:20\n" +
                    "장소: 인천공항 제1여객터미널 3층 B25-B32\n" +
                    "내용: 파라타항공 WE503 인천(ICN) - 나리타(NRT)\n" +
                    "상태: 탑승수속 안내\n" +
                    "참고: 카운터 운영시간 항공기 출발 3시간 전 ~ 1시간 전까지\n" +
                    "문의: 1800-8877\n" +
                    "\n" +
                    "상세 정보\n" +
                    "- 편명: WE503\n" +
                    "- 여정: 인천(ICN) - 나리타(NRT)\n" +
                    "- 출발 일시: 2026년 6월 25일 11:20\n" +
                    "- 체크인 카운터: 인천공항 제1여객터미널 3층 B25-B32\n" +
                    "- 카운터 운영: 항공기 출발 3시간 전 ~ 1시간 전까지\n" +
                    "- 안내: 공항 혼잡 및 보안검색 지연에 대비해 체크인 카운터 오픈 시간에 맞춰 도착 권장\n" +
                    "\n"
        ),
        capture(
            fileName = "demo_20.jpeg",
            captureId = 2_026_000_020L,
            typeCode = ScreenshotContentType.JOB,
            title = "HD현대중공업 Ocean Transformation 과정 모집",
            summary = "K-조선업계 취업 희망 청년 대상 3개월 실무 교육 및 교육비·기숙사 전액 지원 과정입니다.",
            organizedAt = "2026-06-11T12:00:00+09:00",
            body = "회사: HD현대중공업\n" +
                    "직무: 조선 설계·생산관리 (Ocean Transformation 과정 수강생)\n" +
                    "마감일: 2026-06-18 (목)\n" +
                    "근무지: HD현대중공업 인재교육원(울산)\n" +
                    "모집인원: 60명 (30명 × 2차수)\n" +
                    "교육시간: 총 400시간 (약 3개월)\n" +
                    "혜택: 교육비 전액 지원, 정부 훈련수당 월 50만 원 지급, 기숙사 제공(울산 외 거주자), 수료증 발급\n" +
                    "문의: 052-203-0953 (조윤호 선임) / 052-203-0903 (곽미주 사원)\n" +
                    "\n" +
                    "주요 업무\n" +
                    "- 이론교육: 조선공학, 전기·전자공학, AI·코딩·데이터분석 등\n" +
                    "- 실무교육: 조선 설계·생산관리 실무, 야드 투어, 승선 체험 등\n" +
                    "- 취업 스킬: 비즈니스 기초역량, 커리어 코칭, 모의 면접 등\n" +
                    "\n" +
                    "자격·우대\n" +
                    "- K-조선업계 취업을 희망하는 2030 미취업 청년\n" +
                    "- 국내외 4년제 대학 졸업생 및 졸업예정자\n" +
                    "\n" +
                    "선발 절차\n" +
                    "- 서류 전형 > 면접 전형 > 최종 합격\n" +
                    "\n" +
                    "참여 방법\n" +
                    "- 모집 기간: 2026.06.11(목) ~ 2026.06.18(목)\n" +
                    "- 교육 시작일: 1차수 2026.07.06(월) / 2차수 2026.07.20(월)\n" +
                    "- 접수: HD현대 채용 홈페이지 접수 (QR 스캔)\n" +
                    "\n"
        ),
    )

    private val byFileName: Map<String, ScreenshotAnalysisResult> = entries.toMap()

    private fun capture(
        fileName: String,
        captureId: Long,
        typeCode: ScreenshotContentType,
        title: String,
        summary: String,
        body: String,
        organizedAt: String,
    ): Pair<String, ScreenshotAnalysisResult> {
        return fileName to ScreenshotAnalysisResult(
            captureId = captureId,
            typeCode = typeCode,
            title = title,
            summary = summary,
            body = body,
            originalImageUrl = "mock://captures/$captureId",
            isFavorite = false,
            organizedAt = Instant.parse(organizedAt),
        )
    }
}
