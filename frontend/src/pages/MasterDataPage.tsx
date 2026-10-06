import { useCallback, useEffect, useState, type FormEvent } from "react";
import { ApiError } from "../api/client";
import { masterDataApi, type Category, type City, type University, type UniversityDomain } from "../api/masterDataApi";
import { PageHead } from "../components/common/PageHead";
import { Tabs } from "../components/common/Tabs";
import { useAdmin } from "../store/AdminContext";

type Section = "universities" | "cities" | "categories";
const message = (caught: unknown) => caught instanceof ApiError ? caught.message : "요청을 처리하지 못했습니다.";

export function MasterDataPage() {
  const { notify } = useAdmin();
  const [section, setSection] = useState<Section>("universities");
  const [universities, setUniversities] = useState<University[]>([]);
  const [cities, setCities] = useState<City[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [domains, setDomains] = useState<UniversityDomain[]>([]);
  const [selectedUniversity, setSelectedUniversity] = useState<string | null>(null);
  const [editing, setEditing] = useState<string | null>(null);
  const [primary, setPrimary] = useState("");
  const [secondary, setSecondary] = useState("");
  const [tertiary, setTertiary] = useState("");
  const [domainInput, setDomainInput] = useState("");
  const [loading, setLoading] = useState(true);
  const [pending, setPending] = useState(false);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    setLoading(true); setError("");
    try {
      const [universityItems, cityItems, categoryItems] = await Promise.all([
        masterDataApi.universities(), masterDataApi.cities(), masterDataApi.categories(),
      ]);
      setUniversities(universityItems); setCities(cityItems); setCategories(categoryItems);
      if (selectedUniversity && !universityItems.some((item) => item.id === selectedUniversity)) setSelectedUniversity(null);
    } catch (caught) { setError(message(caught)); }
    finally { setLoading(false); }
  }, [selectedUniversity]);
  useEffect(() => { void load(); }, [load]);
  useEffect(() => {
    if (!selectedUniversity) { setDomains([]); return; }
    void masterDataApi.domains(selectedUniversity).then(setDomains).catch((caught) => setError(message(caught)));
  }, [selectedUniversity]);

  const clearForm = () => { setEditing(null); setPrimary(""); setSecondary(""); setTertiary(""); };
  const run = async (action: () => Promise<unknown>, success: string) => {
    setPending(true); setError("");
    try { await action(); notify(success); clearForm(); await load(); }
    catch (caught) { setError(message(caught)); }
    finally { setPending(false); }
  };

  const submit = (event: FormEvent) => {
    event.preventDefault();
    if (!primary.trim() || (section === "cities" && !secondary.trim()) || (!editing && section !== "universities" && !tertiary.trim())) { setError("필수 입력값을 확인해 주세요."); return; }
    if (section === "universities") void run(
      () => editing ? masterDataApi.updateUniversity(editing, primary.trim(), secondary.trim()) : masterDataApi.createUniversity(primary.trim(), secondary.trim()),
      editing ? "대학교 정보를 수정했습니다." : "대학교를 추가했습니다.",
    );
    if (section === "cities") void run(
      () => editing ? masterDataApi.updateCity(editing, { officialName: secondary.trim(), displayNameKo: primary.trim(), displayNameEn: tertiary.trim() || null }) : masterDataApi.createCity({ code: tertiary.trim(), officialName: secondary.trim(), displayNameKo: primary.trim(), displayNameEn: null }),
      editing ? "지역 정보를 수정했습니다." : "지역을 추가했습니다.",
    );
    if (section === "categories") void run(
      () => editing ? masterDataApi.updateCategory(editing, primary.trim(), secondary.trim()) : masterDataApi.createCategory({ code: tertiary.trim(), nameKo: primary.trim(), nameEn: secondary.trim() || null }),
      editing ? "카테고리를 수정했습니다." : "카테고리를 추가했습니다.",
    );
  };

  const startEdit = (item: University | City | Category) => {
    if ("officialName" in item) { setEditing(item.code); setPrimary(item.displayNameKo); setSecondary(item.officialName); setTertiary(item.displayNameEn ?? ""); }
    else if ("code" in item) { setEditing(item.id); setPrimary(item.nameKo); setSecondary(item.nameEn ?? ""); setTertiary(item.code); }
    else { setEditing(item.id); setPrimary(item.nameKo); setSecondary(item.nameEn ?? ""); setTertiary(""); }
  };

  const switchSection = (value: string) => { setSection(value as Section); clearForm(); setError(""); };
  const formLabels = section === "universities" ? ["대학교명(한글)", "대학교명(영문)"] : section === "cities" ? ["표시 이름(한글)", "공식 이름"] : ["카테고리명(한글)", "카테고리명(영문)"];

  return <>
    <PageHead title="기준 정보 관리" description="서비스에서 공통으로 사용하는 대학·학교 도메인·지역·카테고리를 관리하세요." extra={<button className="button" disabled={loading} onClick={() => void load()}>{loading ? "불러오는 중…" : "새로고침"}</button>} />
    <section className="panel master-panel">
      <Tabs items={[{ value: "universities", label: "대학교·도메인" }, { value: "cities", label: "지역" }, { value: "categories", label: "카테고리" }]} value={section} onChange={switchSection} />
      <form className="master-form" onSubmit={submit}>
        <div><label>{formLabels[0]}</label><input value={primary} onChange={(event) => setPrimary(event.target.value)} required /></div>
        <div><label>{formLabels[1]}</label><input value={secondary} onChange={(event) => setSecondary(event.target.value)} required={section === "cities"} /></div>
        {section !== "universities" && <div><label>{editing ? "영문 표시명" : "코드"}</label><input value={tertiary} disabled={Boolean(editing) && section === "categories"} onChange={(event) => setTertiary(event.target.value)} required={!editing} placeholder="SEOUL 또는 CULTURE" /></div>}
        <div className="master-form-actions"><button className="primary" disabled={pending}>{editing ? "수정 저장" : "새 항목 추가"}</button>{editing && <button type="button" className="button" onClick={clearForm}>취소</button>}</div>
      </form>
      {error && <p className="api-error" role="alert">{error}</p>}
      <div className="table-wrap"><table><thead><tr>{section === "universities" ? <><th>대학교</th><th>영문명</th><th>상태</th><th>관리</th></> : section === "cities" ? <><th>코드</th><th>지역명</th><th>공식 이름</th><th>상태</th><th>관리</th></> : <><th>코드</th><th>카테고리</th><th>영문명</th><th>상태</th><th>관리</th></>}</tr></thead><tbody>
        {section === "universities" && (universities.length ? universities.map((item) => <tr key={item.id}><td><button className="event-link" onClick={() => setSelectedUniversity(item.id)}>{item.nameKo}</button><small>도메인 {selectedUniversity === item.id ? "관리 중" : "보기"}</small></td><td>{item.nameEn ?? "-"}</td><td><span className={`badge ${item.active ? "ok" : "no"}`}>{item.active ? "활성" : "비활성"}</span></td><td><div className="row-actions"><button className="button" onClick={() => startEdit(item)}>수정</button><button className="button" disabled={pending} onClick={() => void run(() => masterDataApi.setUniversityActive(item.id, !item.active), `${item.nameKo} 상태를 변경했습니다.`)}>{item.active ? "비활성화" : "활성화"}</button></div></td></tr>) : <tr><td colSpan={4} className="empty">등록된 대학교가 없습니다.</td></tr>)}
        {section === "cities" && (cities.length ? cities.map((item) => <tr key={item.code}><td><code>{item.code}</code></td><td>{item.displayNameKo}</td><td>{item.officialName}</td><td><span className={`badge ${item.active ? "ok" : "no"}`}>{item.active ? "활성" : "비활성"}</span></td><td><div className="row-actions"><button className="button" onClick={() => startEdit(item)}>수정</button><button className="button" disabled={pending} onClick={() => void run(() => masterDataApi.setCityActive(item.code, !item.active), `${item.displayNameKo} 상태를 변경했습니다.`)}>{item.active ? "비활성화" : "활성화"}</button></div></td></tr>) : <tr><td colSpan={5} className="empty">등록된 지역이 없습니다.</td></tr>)}
        {section === "categories" && (categories.length ? categories.map((item) => <tr key={item.id}><td><code>{item.code}</code></td><td>{item.nameKo}</td><td>{item.nameEn ?? "-"}</td><td><span className={`badge ${item.active ? "ok" : "no"}`}>{item.active ? "활성" : "비활성"}</span></td><td><div className="row-actions"><button className="button" onClick={() => startEdit(item)}>수정</button><button className="button" disabled={pending} onClick={() => void run(() => masterDataApi.setCategoryActive(item.id, !item.active), `${item.nameKo} 상태를 변경했습니다.`)}>{item.active ? "비활성화" : "활성화"}</button></div></td></tr>) : <tr><td colSpan={5} className="empty">등록된 카테고리가 없습니다.</td></tr>)}
      </tbody></table></div>
    </section>
    {section === "universities" && selectedUniversity && <section className="panel domain-panel"><div className="panel-head"><div><h2>학교 이메일 도메인</h2><p>{universities.find((item) => item.id === selectedUniversity)?.nameKo}</p></div><button className="quiet" onClick={() => setSelectedUniversity(null)}>닫기</button></div><form className="toolbar" onSubmit={(event) => { event.preventDefault(); if (!domainInput.trim()) return; void run(async () => { await masterDataApi.addDomain(selectedUniversity, domainInput.trim()); setDomainInput(""); setDomains(await masterDataApi.domains(selectedUniversity)); }, "학교 도메인을 추가했습니다."); }}><input placeholder="example.ac.kr" value={domainInput} onChange={(event) => setDomainInput(event.target.value)} /><button className="primary" disabled={pending}>도메인 추가</button></form><div className="domain-list">{domains.length ? domains.map((item) => <div key={item.domain}><div><b>{item.domain}</b><small>{item.active ? "인증에 사용 중" : "비활성"}</small></div><button className="button" disabled={pending} onClick={() => void run(async () => { await masterDataApi.setDomainActive(selectedUniversity, item.domain, !item.active); setDomains(await masterDataApi.domains(selectedUniversity)); }, `도메인을 ${item.active ? "비활성화" : "활성화"}했습니다.`)}>{item.active ? "비활성화" : "활성화"}</button></div>) : <div className="empty">등록된 학교 도메인이 없습니다.</div>}</div></section>}
  </>;
}
