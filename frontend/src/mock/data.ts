import type { EventItem, MetricItem, Participant, ReportItem } from "../types";
import { STATUS_LABELS } from "../constants/status";
import { kstDate, todayISO } from "../utils/date";

const rawEvents = [
  ["EVT-1024", "한강에서 만나는 글로벌 피크닉", "김지우", "연세대학교", "문화 교류", "2026-10-04", "2026-09-27", "여의도 한강공원", "pending", "open", 20],
  ["EVT-1025", "한국어 × 영어, 언어 교환의 밤", "박서준", "고려대학교", "언어 교환", "2026-10-05", "2026-09-28", "안암 캠퍼스 라운지", "pending", "open", 16],
  ["EVT-1026", "처음 만나는 서울, 골목 산책", "Emma Wilson", "서울대학교", "로컬 탐방", "2026-10-08", "2026-09-29", "안국역 3번 출구", "reviewing", "open", 12],
  ["EVT-1027", "세계의 식탁 · 함께 만드는 저녁", "이수민", "성균관대학교", "음식", "2026-10-09", "2026-09-29", "혜화 공유 주방", "pending", "open", 10],
  ["EVT-1019", "주말 배드민턴, 같이 한 게임!", "최유진", "한양대학교", "스포츠", "2026-10-03", "2026-09-23", "왕십리 실내체육관", "approved", "open", 16],
  ["EVT-1018", "북촌 필름 사진 산책", "Alex Chen", "홍익대학교", "로컬 탐방", "2026-10-02", "2026-09-22", "북촌 한옥마을", "approved", "open", 12],
  ["EVT-1015", "외국인 유학생 유료 취업 설명회", "정민호", "서강대학교", "커리어", "2026-10-02", "2026-09-20", "신촌 스터디룸", "rejected", "open", 30],
  ["EVT-1012", "남산 야경과 함께하는 산책", "김하늘", "이화여자대학교", "로컬 탐방", "2026-09-28", "2026-09-18", "남산도서관", "approved", "cancelled", 15],
  ["EVT-1008", "캠퍼스 보드게임 클럽", "박지민", "연세대학교", "문화 교류", "2026-09-25", "2026-09-16", "신촌 학생회관", "approved", "ended", 20],
] as const;

export const initialEvents: EventItem[] = rawEvents.map((item) => {
  const [id, title, host, school, cat, date, submitted, venue, status, rawLife, capacity] = item;
  const life = status === "approved" ? rawLife : "not_open";
  const event: EventItem = {
    id, title, host, school, cat, date, submitted, venue, status, life, capacity,
    version: 1,
    history: [
      { action: "심사신청", at: `${submitted} 10:00`, actor: host },
      ...((status !== "pending" ? [{ action: "심사 시작", at: `${submitted} 14:00`, actor: "김관리" }] : [])),
      ...((["approved", "rejected"].includes(status) ? [{ action: STATUS_LABELS[status], at: `${submitted} 15:00`, actor: "김관리" }] : [])),
    ],
  };
  if (id === "EVT-1015") event.reason = "교류 목적보다 외부 유료 서비스 홍보가 주목적입니다.";
  if (id === "EVT-1012") event.cancelReason = "호스트 개인 사정으로 행사 취소";
  return event;
});

export const initialReports: ReportItem[] = [
  {
    id: "RPT-0031", type: "event", target: "EVT-1019", title: "주말 배드민턴, 같이 한 게임!",
    reason: "모집 내용과 다른 비용 요구", description: "모집 글에 없는 추가 대관료를 개인 메시지로 요청받았습니다.",
    evidence: "예시 증빙: ‘참여 확정 후 별도 대관료를 보내주세요.’", status: "received", date: "2026-09-30", history: [],
  },
  {
    id: "RPT-0030", type: "host", target: "EVT-1018", title: "Alex Chen", reason: "부적절한 메시지",
    description: "참가 신청과 무관한 개인 연락이 반복적으로 왔습니다.", evidence: "예시 증빙: 반복된 개인 연락 내역",
    status: "investigating", date: "2026-09-29", history: [{ action: "검토 시작", at: "2026-09-29 16:00", actor: "김관리" }],
  },
  {
    id: "RPT-0029", type: "event", target: "EVT-1018", title: "북촌 필름 사진 산책", reason: "이벤트 정보 오류",
    description: "상세 장소와 지도에 표시된 위치가 다릅니다.", evidence: "예시 증빙: 장소 안내 화면",
    status: "received", date: "2026-09-28", history: [],
  },
  {
    id: "RPT-0027", type: "host", target: "EVT-1008", title: "박지민", reason: "불친절한 응대",
    description: "신청 결과 안내가 지연되었습니다.", evidence: "예시 증빙: 신청 날짜 기록", status: "resolved",
    date: "2026-09-26", resolution: "조치 불필요", note: "안내 완료 사실을 확인했습니다.", history: [],
  },
];

export const participantsByEvent: Record<string, Participant[]> = {
  "EVT-1019": [
    { name: "김민지", group: "한국학생", status: "confirmed" },
    { name: "Sophie Martin", group: "국제학생", status: "applied" },
  ],
  "EVT-1018": [
    { name: "이준호", group: "한국학생", status: "confirmed" },
    { name: "Daniel Kim", group: "국제학생", status: "declined" },
  ],
  "EVT-1008": [{ name: "박민서", group: "한국학생", status: "confirmed" }],
};

const metricEnd = new Date(`${todayISO}T12:00:00+09:00`);
export const metrics: MetricItem[] = Array.from({ length: 30 }, (_, index) => {
  const date = new Date(metricEnd);
  date.setDate(date.getDate() - (29 - index));
  return {
    date: kstDate.format(date),
    web: 38 + ((index * 17) % 73),
    app: 61 + ((index * 23) % 97),
    joins: 3 + ((index * 7) % 15),
  };
});
