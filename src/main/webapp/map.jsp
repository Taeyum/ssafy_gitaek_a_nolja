<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>

<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <title>EnjoyTrip - 지도</title>
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <style>
    body { font-family: system-ui, -apple-system, "Segoe UI", Roboto, Arial, "Noto Sans KR", sans-serif; margin: 0; }
    header { padding: 12px 16px; border-bottom: 1px solid #e5e7eb; display:flex; align-items:center; gap:12px; }
    .container { display:grid; grid-template-columns: 360px 1fr; height: calc(100vh - 57px); }
    .side { border-right: 1px solid #e5e7eb; padding: 12px; overflow:auto; }
    .controls { display:grid; gap:10px; }
    .row { display:flex; gap:8px; }
    select, button { padding:8px; font-size:14px; }
    #map { width: 100%; height: 100%; }
    .result-header { display:flex; justify-content:space-between; align-items:center; margin:10px 0; }
    .list { display:flex; flex-direction:column; gap:8px; }
    .item { padding:8px; border:1px solid #e5e7eb; border-radius:8px; cursor:pointer; }
    .item:hover { background:#fafafa; }
    .muted { color:#6b7280; font-size:12px; }
    .chips { display:flex; flex-wrap:wrap; gap:6px; margin-top:6px; }
    .chip { border:1px solid #e5e7eb; border-radius:999px; padding:4px 8px; font-size:12px; cursor:pointer; }
    .chip.active { background:#111827; color:#fff; border-color:#111827; }
  </style>
</head>
<body>
<%@include file="/header.jsp"%>
<header>
  <strong>EnjoyTrip</strong>
  <span class="muted">관광지 · 숙박 · 음식점 · 문화시설 · 공연/축제 · 코스 · 쇼핑</span>
</header>

<div class="container">
  <aside class="side">
    <div class="controls">
      <div class="row">
        <select id="areaCode" style="flex:1"></select>
        <select id="sigunguCode" style="flex:1"></select>
      </div>

      <div class="chips" id="chips">
        <!-- contentTypeId chips -->
        <span class="chip" data-type="">전체</span>
        <span class="chip" data-type="12">관광지</span>
        <span class="chip" data-type="32">숙박</span>
        <span class="chip" data-type="39">음식점</span>
        <span class="chip" data-type="14">문화시설</span>
        <span class="chip" data-type="15">공연/축제</span>
        <span class="chip" data-type="25">코스</span>
        <span class="chip" data-type="38">쇼핑</span>
      </div>

      <div class="row">
        <button id="btn_trip_search" style="flex:1">검색</button>
        <button id="btn_clear" style="flex:1">초기화</button>
      </div>
    </div>

    <div class="result-header">
      <strong>검색 결과</strong>
      <span class="muted" id="count">0건</span>
    </div>
    <div class="list" id="list"></div>
  </aside>

  <main>
    <div id="map"></div>
  </main>
</div>

<!-- ✅ 카카오 지도 SDK: 여기 "JavaScript 키"를 직접 넣으세요 -->
<script src="//dapi.kakao.com/v2/maps/sdk.js?appkey=9d4d9f67c91e42b726115bd0e1bbcec8"></script>
<script>
  // ------ 공통 ------
  const CTX = '<%=request.getContextPath()%>';
  const area = document.getElementById('areaCode');
  const sigungu = document.getElementById('sigunguCode');
  const listEl = document.getElementById('list');
  const countEl = document.getElementById('count');
  const chipsEl = document.getElementById('chips');
  const btnSearch = document.getElementById('btn_trip_search');
  const btnClear = document.getElementById('btn_clear');

  // ------ 지도 ------
  const map = new kakao.maps.Map(document.getElementById('map'), {
    center: new kakao.maps.LatLng(37.5665, 126.9780), // 서울
    level: 7
  });
  let markers = [];
  function clearMarkers() {
    markers.forEach(m => m.setMap(null));
    markers = [];
  }
  function drawMarkers(spots) {
    clearMarkers();
    const bounds = new kakao.maps.LatLngBounds();
    const iw = new kakao.maps.InfoWindow({removable: true});
    spots.forEach(s => {
      const x = Number(s.mapx); // 경도
      const y = Number(s.mapy); // 위도
      if (!x || !y) return;
      const pos = new kakao.maps.LatLng(y, x);
      const marker = new kakao.maps.Marker({ map, position: pos, title: s.title || "" });
      markers.push(marker);
      bounds.extend(pos);

      const html = `
        <div style="padding:8px;max-width:240px">
          <div style="font-weight:600;margin-bottom:4px">${s.title ?? ""}</div>
          <div class="muted">${s.addr1 ?? ""}</div>
        </div>`;
      kakao.maps.event.addListener(marker, 'click', () => {
        iw.setContent(html);
        iw.open(map, marker);
      });
    });
    if (!bounds.isEmpty()) map.setBounds(bounds);
  }

  // ------ 유틸 ------
  async function fetchJSON(url) {
    const res = await fetch(url);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return res.json();
  }
  function toArray(items) {
    if (!items) return [];
    return Array.isArray(items) ? items : [items];
  }
  function updateSelect(selectEl, placeholder, items, valueKey='code', labelKey='name') {
    selectEl.innerHTML = '';
    const first = document.createElement('option');
    first.value = '';
    first.textContent = `${placeholder} 선택`;
    selectEl.appendChild(first);

    items.forEach(it => {
      const opt = document.createElement('option');
      opt.value = it[valueKey];
      opt.textContent = it[labelKey];
      selectEl.appendChild(opt);
    });
  }
  function renderList(spots) {
    listEl.innerHTML = '';
    countEl.textContent = `${spots.length}건`;
    spots.forEach((s, idx) => {
      const item = document.createElement('div');
      item.className = 'item';
      item.innerHTML = `
        <div style="font-weight:600">${s.title ?? ''}</div>
        <div class="muted">${s.addr1 ?? ''}</div>
      `;
      item.addEventListener('click', () => {
        const x = Number(s.mapx), y = Number(s.mapy);
        if (!x || !y) return;
        const pos = new kakao.maps.LatLng(y, x);
        map.setLevel(4);
        map.panTo(pos);
      });
      listEl.appendChild(item);
    });
  }

  // ------ API 연동 ------
  async function loadAreas(areaCode) {
    const qs = areaCode ? `?action=areas&areaCode=${encodeURIComponent(areaCode)}` : `?action=areas`;
    const json = await fetchJSON(`${CTX}/api${qs}`);
    let items = toArray(json?.response?.body?.items?.item);
    if (areaCode) {
      // 시군구
      updateSelect(sigungu, '시군구', items);
    } else {
      // 시도
      updateSelect(area, '지역', items);
      updateSelect(sigungu, '시군구', []);
    }
  }

  async function searchPlaces(params) {
    const search = new URLSearchParams({ action: 'places' });
    if (params.areaCode)    search.set('areaCode', params.areaCode);
    if (params.sigunguCode) search.set('sigunguCode', params.sigunguCode);
    if (params.contentTypeId) search.set('contentTypeId', params.contentTypeId);

    const json = await fetchJSON(`${CTX}/api?${search.toString()}`);
    const items = toArray(json?.response?.body?.items?.item);
    drawMarkers(items);
    renderList(items);
  }

  // ------ 이벤트 ------
  document.addEventListener('DOMContentLoaded', () => {
    // 초기 시도 목록
    loadAreas().catch(console.error);

    // chip 선택
    chipsEl.addEventListener('click', (e) => {
      const chip = e.target.closest('.chip');
      if (!chip) return;
      [...chipsEl.querySelectorAll('.chip')].forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
    });

    // 지역 변경 → 시군구 로드
    area.addEventListener('change', () => {
      if (!area.value) {
        updateSelect(sigungu, '시군구', []);
        return;
      }
      loadAreas(area.value).catch(console.error);
    });

    // 검색
    btnSearch.addEventListener('click', () => {
      const activeChip = chipsEl.querySelector('.chip.active');
      const contentTypeId = activeChip ? activeChip.dataset.type : '';
      searchPlaces({
        areaCode: area.value || '',
        sigunguCode: sigungu.value || '',
        contentTypeId: contentTypeId || ''
      }).catch(console.error);
    });

    // 초기화
    btnClear.addEventListener('click', () => {
      area.value = '';
      updateSelect(sigungu, '시군구', []);
      [...chipsEl.querySelectorAll('.chip')].forEach(c => c.classList.remove('active'));
      countEl.textContent = '0건';
      listEl.innerHTML = '';
      clearMarkers();
      map.setLevel(7);
      map.setCenter(new kakao.maps.LatLng(37.5665, 126.9780));
    });
  });
</script>
</body>
</html>
