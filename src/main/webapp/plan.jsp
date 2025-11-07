<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <title>나만의 여행 계획</title>
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <style>
    :root {
      color-scheme: light;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Arial, "Noto Sans KR", sans-serif;
    }
    * { box-sizing: border-box; }
    body { margin: 0; background: #f3f4f6; color: #111827; }
    header, .plan-shell { background: #fff; }
    .plan-shell { padding: 24px 28px; border-bottom: 1px solid #e5e7eb; }
    .plan-shell form { display: grid; grid-template-columns: repeat(4, minmax(160px, 1fr)); gap: 16px; align-items: end; }
    .field { display: flex; flex-direction: column; gap: 6px; font-size: 12px; color: #4b5563; text-transform: uppercase; letter-spacing: 0.04em; }
    .field input, .field textarea { font-size: 14px; padding: 10px 12px; border: 1px solid #d1d5db; border-radius: 8px; font-family: inherit; }
    .field textarea { min-height: 72px; resize: vertical; }
    .actions { display: flex; justify-content: flex-end; gap: 10px; }
    button.primary { background: #2563eb; color: #fff; border: none; border-radius: 8px; padding: 10px 16px; font-size: 14px; cursor: pointer; }
    button.ghost { border: 1px solid #d1d5db; background: #fff; color: #111827; border-radius: 8px; padding: 10px 16px; cursor: pointer; }
    button.small { padding: 6px 10px; font-size: 12px; border-radius: 6px; }
    button:disabled { opacity: 0.6; cursor: not-allowed; }
    .layout { display: grid; grid-template-columns: 320px 360px 1fr; height: calc(100vh - 200px); }
    .itinerary { border-right: 1px solid #e5e7eb; background: #fff; display: flex; flex-direction: column; }
    .itinerary-header { display: flex; justify-content: space-between; align-items: center; padding: 16px; border-bottom: 1px solid #e5e7eb; }
    .plan-items { flex: 1; overflow: auto; padding: 16px; display: flex; flex-direction: column; gap: 12px; }
    .plan-empty { padding: 24px; text-align: center; color: #9ca3af; }
    .plan-item { border: 1px solid #d1d5db; border-radius: 10px; padding: 14px; background: #fff; cursor: grab; }
    .plan-item.dragging { opacity: 0.5; border-style: dashed; }
    .plan-item-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; gap: 12px; }
    .plan-item-title { font-weight: 600; flex: 1; }
    .plan-item-address { font-size: 12px; color: #6b7280; margin-bottom: 10px; }
    .plan-item-body { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; }
    .plan-item-body .field { font-size: 11px; text-transform: none; color: #6b7280; }
    .plan-item-body textarea { grid-column: 1 / -1; min-height: 70px; font-size: 13px; }
    .plan-order { width: 26px; height: 26px; border-radius: 50%; border: 1px solid #2563eb; display: inline-flex; align-items: center; justify-content: center; font-size: 12px; color: #2563eb; margin-right: 8px; }
    .search-panel { border-right: 1px solid #e5e7eb; background: #fff; padding: 20px; display: flex; flex-direction: column; gap: 16px; overflow: auto; }
    .search-controls { display: flex; flex-direction: column; gap: 12px; }
    .row { display: flex; gap: 10px; }
    .row > * { flex: 1; }
    select { padding: 10px 12px; border: 1px solid #d1d5db; border-radius: 8px; font-size: 14px; }
    .chips { display: flex; flex-wrap: wrap; gap: 8px; }
    .chip { border: 1px solid #d1d5db; border-radius: 999px; padding: 6px 12px; font-size: 13px; cursor: pointer; background: #fff; }
    .chip.active { background: #111827; color: #fff; border-color: #111827; }
    .spot-list { display: flex; flex-direction: column; gap: 10px; overflow: auto; }
    .spot-card { border: 1px solid #e5e7eb; border-radius: 8px; padding: 12px; cursor: pointer; display: grid; gap: 6px; }
    .spot-card:hover { background: #f9fafb; }
    .spot-actions { display: flex; justify-content: flex-end; }
    .map-panel { position: relative; }
    #plan-map { width: 100%; height: 100%; }
    .muted { color: #6b7280; font-size: 12px; }
    .infowindow { padding: 8px 10px; max-width: 220px; font-size: 13px; }
    .infowindow strong { display: block; margin-bottom: 4px; }
    .modal-backdrop { position: fixed; inset: 0; background: rgba(17, 24, 39, 0.45); display: none; align-items: center; justify-content: center; z-index: 1200; padding: 16px; }
    .modal-backdrop.show { display: flex; }
    .modal-dialog { background: #fff; border-radius: 12px; box-shadow: 0 20px 45px rgba(15, 23, 42, 0.18); width: min(360px, 100%); max-width: 420px; padding: 24px; display: flex; flex-direction: column; gap: 16px; }
    .modal-dialog strong { font-size: 18px; color: #111827; }
    .modal-dialog p { margin: 0; font-size: 14px; color: #4b5563; }
    .modal-actions { display: flex; justify-content: flex-end; gap: 10px; }
    @media (max-width: 1200px) {
      .layout { grid-template-columns: 320px 1fr; grid-template-rows: 420px 1fr; height: auto; }
      .map-panel { grid-column: 1 / -1; height: 480px; }
    }
    @media (max-width: 768px) {
      .plan-shell form { grid-template-columns: repeat(2, minmax(0, 1fr)); }
      .layout { grid-template-columns: 1fr; grid-template-rows: auto auto 480px; }
      .itinerary, .search-panel { border-right: none; border-bottom: 1px solid #e5e7eb; }
    }
  </style>
</head>
<body>
<%@include file="/header.jsp"%>

<section class="plan-shell">
  <form id="plan-form" autocomplete="off">
    <div class="field">
      <span>여행 제목</span>
      <input type="text" name="title" placeholder="예: 광주 2박3일 가족여행" />
    </div>
    <div class="field">
      <span>여행 시작일</span>
      <input type="date" name="startDate" />
    </div>
    <div class="field">
      <span>여행 종료일</span>
      <input type="date" name="endDate" />
    </div>
    <div class="field">
      <span>여행 경비</span>
      <input type="text" name="budget" placeholder="예: 500,000원" />
    </div>
    <div class="field" style="grid-column: 1 / -1;">
      <span>상세 계획 메모</span>
      <textarea name="memo" placeholder="전체 일정에 대한 메모를 정리하세요."></textarea>
    </div>
    <div class="actions" style="grid-column: 1 / -1;">
      <button type="button" class="ghost" id="btn-clear-plan">동선 초기화</button>
      <button type="button" class="primary" id="btn-save-plan">여행 계획 저장</button>
    </div>
  </form>
</section>

<div class="layout">
  <aside class="itinerary">
    <div class="itinerary-header">
      <div>
        <strong>여행 동선</strong>
        <div class="muted" id="plan-count">0개의 여행지가 추가되었습니다.</div>
      </div>
      <button type="button" class="ghost small" id="btn-fetch-plan">임시 저장본 조회</button>
    </div>
    <div class="plan-empty" id="plan-empty">검색한 관광지를 추가하면 이곳에서 순서를 바꿔 여행 동선을 짤 수 있습니다.</div>
    <div class="plan-items" id="plan-items" aria-live="polite"></div>
  </aside>

  <section class="search-panel">
    <div class="search-controls">
      <div class="row">
        <select id="search-area">
          <option value="">지역 선택</option>
        </select>
        <select id="search-sigungu">
          <option value="">시군구 선택</option>
        </select>
      </div>
      <div class="chips" id="content-chips">
        <span class="chip active" data-type="">전체</span>
        <span class="chip" data-type="12">관광지</span>
        <span class="chip" data-type="32">숙박</span>
        <span class="chip" data-type="39">음식</span>
        <span class="chip" data-type="14">문화시설</span>
        <span class="chip" data-type="15">공연/축제</span>
        <span class="chip" data-type="25">코스</span>
        <span class="chip" data-type="38">쇼핑</span>
      </div>
      <div class="row">
        <button type="button" class="primary" id="btn-search-spot">관광지 검색</button>
        <button type="button" class="ghost" id="btn-reset-search">검색 초기화</button>
      </div>
    </div>

    <div>
      <strong>검색 결과</strong>
      <div class="muted" id="search-count">검색 대기 중</div>
    </div>
    <div class="spot-list" id="spot-list"></div>
  </section>

  <section class="map-panel">
    <div id="plan-map"></div>
  </section>
</div>

<div id="save-modal" class="modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="save-modal-title" aria-hidden="true">
  <div class="modal-dialog">
    <strong id="save-modal-title">저장 완료</strong>
    <p id="save-modal-message" class="muted"></p>
    <div class="modal-actions">
      <button type="button" class="primary" id="save-modal-close">확인</button>
    </div>
  </div>
</div>

<script src="//dapi.kakao.com/v2/maps/sdk.js?appkey=9d4d9f67c91e42b726115bd0e1bbcec8"></script>
<script>
(() => {
  const CTX = '<%=request.getContextPath()%>';
  const form = document.getElementById('plan-form');
  const planItemsEl = document.getElementById('plan-items');
  const planEmptyEl = document.getElementById('plan-empty');
  const planCountEl = document.getElementById('plan-count');
  const btnSave = document.getElementById('btn-save-plan');
  const btnClearPlan = document.getElementById('btn-clear-plan');
  const btnFetchPlan = document.getElementById('btn-fetch-plan');
  const modal = document.getElementById('save-modal');
  const modalMessage = document.getElementById('save-modal-message');
  const modalClose = document.getElementById('save-modal-close');

  const searchArea = document.getElementById('search-area');
  const searchSigungu = document.getElementById('search-sigungu');
  const chips = document.getElementById('content-chips');
  const btnSearchSpot = document.getElementById('btn-search-spot');
  const btnResetSearch = document.getElementById('btn-reset-search');
  const spotListEl = document.getElementById('spot-list');
  const searchCountEl = document.getElementById('search-count');

  const inputs = {
    title: form.querySelector('input[name="title"]'),
    startDate: form.querySelector('input[name="startDate"]'),
    endDate: form.querySelector('input[name="endDate"]'),
    budget: form.querySelector('input[name="budget"]'),
    memo: form.querySelector('textarea[name="memo"]')
  };

  const planState = {
    planId: 0,
    title: '',
    startDate: '',
    endDate: '',
    budget: '',
    memo: '',
    items: []
  };

  let map;
  let itineraryMarkers = [];
  let itineraryPolyline = null;
  const infoWindow = new kakao.maps.InfoWindow({ zIndex: 10 });
  let draggingId = null;

  let latestSpots = [];

  function openModal(message) {
    if (!modal) return;
    if (modalMessage) {
      modalMessage.textContent = message ?? '';
    }
    modal.classList.add('show');
    modal.setAttribute('aria-hidden', 'false');
  }

  function closeModal() {
    if (!modal) return;
    modal.classList.remove('show');
    modal.setAttribute('aria-hidden', 'true');
    if (modalMessage) {
      modalMessage.textContent = '';
    }
  }

  if (modal && modalClose) {
    modalClose.addEventListener('click', closeModal);
    modal.addEventListener('click', (event) => {
      if (event.target === modal) closeModal();
    });
  }

  document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape' && modal && modal.classList.contains('show')) {
      closeModal();
    }
  });

  document.addEventListener('DOMContentLoaded', init);

  function init() {
    attachMetaListeners();
    wireButtons();
    initMap();
    loadAreas().catch(console.error);
    updatePlanEmptyState();
  }

  function attachMetaListeners() {
    Object.values(inputs).forEach(input => {
      input.addEventListener('input', updatePlanMeta);
    });
  }

  function updatePlanMeta() {
    planState.title = inputs.title.value.trim();
    planState.startDate = inputs.startDate.value;
    planState.endDate = inputs.endDate.value;
    planState.budget = inputs.budget.value.trim();
    planState.memo = inputs.memo.value.trim();
  }

  function wireButtons() {
    btnSave.addEventListener('click', handleSavePlan);
    btnClearPlan.addEventListener('click', () => {
      planState.items = [];
      renderPlanItems();
    });
    btnFetchPlan.addEventListener('click', fetchTempPlans);

    chips.addEventListener('click', handleChipClick);
    btnSearchSpot.addEventListener('click', () => searchSpots().catch(console.error));
    btnResetSearch.addEventListener('click', resetSearch);

    searchArea.addEventListener('change', handleAreaChange);
  }

  function initMap() {
    map = new kakao.maps.Map(document.getElementById('plan-map'), {
      center: new kakao.maps.LatLng(37.5665, 126.9780),
      level: 7
    });
  }

  async function loadAreas(areaCode) {
    const qs = areaCode ? `?action=areas&areaCode=${encodeURIComponent(areaCode)}` : '?action=areas';
    const json = await fetchJSON(`${CTX}/api${qs}`);
    const items = toArray(json?.response?.body?.items?.item);
    if (areaCode) {
      fillSelect(searchSigungu, '시군구 선택', items);
    } else {
      fillSelect(searchArea, '지역 선택', items);
      fillSelect(searchSigungu, '시군구 선택', []);
    }
  }

  function handleAreaChange() {
    const value = searchArea.value;
    if (!value) {
      fillSelect(searchSigungu, '시군구 선택', []);
      return;
    }
    loadAreas(value).catch(console.error);
  }

  function fillSelect(select, placeholder, items) {
    select.innerHTML = '';
    const first = document.createElement('option');
    first.value = '';
    first.textContent = placeholder;
    select.appendChild(first);
    items.forEach((item) => {
      const opt = document.createElement('option');
      opt.value = item.code;
      opt.textContent = item.name;
      select.appendChild(opt);
    });
  }

  function handleChipClick(event) {
    const target = event.target.closest('.chip');
    if (!target) return;
    chips.querySelectorAll('.chip').forEach(chip => chip.classList.remove('active'));
    target.classList.add('active');
  }

  async function searchSpots() {
    searchCountEl.textContent = '검색 중입니다...';
    const params = new URLSearchParams({ action: 'places' });
    if (searchArea.value) params.set('areaCode', searchArea.value);
    if (searchSigungu.value) params.set('sigunguCode', searchSigungu.value);
    const activeChip = chips.querySelector('.chip.active');
    if (activeChip && activeChip.dataset.type) {
      params.set('contentTypeId', activeChip.dataset.type);
    }
    const json = await fetchJSON(`${CTX}/api?${params.toString()}`);
    const items = toArray(json?.response?.body?.items?.item);
    latestSpots = items;
    renderSpots(items);
  }

  function resetSearch() {
    searchArea.value = '';
    searchSigungu.value = '';
    chips.querySelectorAll('.chip').forEach(chip => chip.classList.remove('active'));
    chips.querySelector('.chip').classList.add('active');
    spotListEl.innerHTML = '';
    searchCountEl.textContent = '검색 대기 중';
    latestSpots = [];
    fillSelect(searchSigungu, '시군구 선택', []);
  }

  function renderSpots(spots) {
    spotListEl.innerHTML = '';
    if (!spots.length) {
      searchCountEl.textContent = '검색 결과가 없습니다.';
      return;
    }
    searchCountEl.textContent = `${spots.length}건의 관광지를 찾았습니다.`;
    spots.forEach((spot) => {
      const card = document.createElement('div');
      card.className = 'spot-card';
      card.dataset.id = spot.contentid || '';
      const title = document.createElement('strong');
      title.textContent = spot.title ?? '이름 없는 관광지';
      const address = document.createElement('span');
      address.className = 'muted';
      address.textContent = spot.addr1 ?? '';
      const actions = document.createElement('div');
      actions.className = 'spot-actions';
      const btn = document.createElement('button');
      btn.type = 'button';
      btn.className = 'primary small';
      btn.textContent = '동선에 추가';
      btn.addEventListener('click', (event) => {
        event.stopPropagation();
        addSpotToPlan(spot);
      });
      card.addEventListener('click', () => focusSpotOnMap(spot));
      actions.appendChild(btn);
      card.append(title, address, actions);
      spotListEl.appendChild(card);
    });
  }

  function addSpotToPlan(spot) {
    const localId = crypto.randomUUID ? crypto.randomUUID() : `plan-${Date.now()}-${Math.random().toString(16).slice(2)}`;
    planState.items.push({
      localId,
      placeId: spot.contentid || '',
      title: spot.title || '이름 없는 장소',
      address: spot.addr1 || '',
      day: planState.startDate || '',
      startTime: '',
      endTime: '',
      memo: '',
      mapx: Number(spot.mapx || 0),
      mapy: Number(spot.mapy || 0)
    });
    renderPlanItems();
    focusSpotOnMap(spot);
  }

  function renderPlanItems() {
    planItemsEl.innerHTML = '';
    planState.items.forEach((item, index) => {
      planItemsEl.appendChild(createPlanItemCard(item, index));
    });
    updatePlanEmptyState();
    refreshItineraryMap();
  }

  function createPlanItemCard(item, index) {
    const card = document.createElement('div');
    card.className = 'plan-item';
    card.draggable = true;
    card.dataset.id = item.localId;

    const header = document.createElement('div');
    header.className = 'plan-item-header';

    const order = document.createElement('span');
    order.className = 'plan-order';
    order.textContent = index + 1;

    const title = document.createElement('strong');
    title.className = 'plan-item-title';
    title.textContent = item.title || '이름 없는 장소';

    const remove = document.createElement('button');
    remove.type = 'button';
    remove.className = 'ghost small';
    remove.dataset.id = item.localId;
    remove.textContent = '삭제';
    remove.addEventListener('click', (event) => {
      event.stopPropagation();
      removePlanItem(item.localId);
    });

    header.append(order, title, remove);

    const address = document.createElement('div');
    address.className = 'plan-item-address';
    address.textContent = item.address || '';

    const body = document.createElement('div');
    body.className = 'plan-item-body';

    const dayField = buildField('일자', 'date', item.day ?? '', 'day', item.localId);
    const startField = buildField('시작 시간', 'time', item.startTime ?? '', 'startTime', item.localId);
    const endField = buildField('종료 시간', 'time', item.endTime ?? '', 'endTime', item.localId);
    const memoField = buildTextarea('메모', item.memo ?? '', 'memo', item.localId);

    body.append(dayField.container, startField.container, endField.container, memoField.container);

    card.append(header, address, body);

    card.addEventListener('dragstart', handleDragStart);
    card.addEventListener('dragend', handleDragEnd);
    card.addEventListener('dragover', (event) => event.preventDefault());
    card.addEventListener('drop', (event) => handleDrop(event, item.localId));
    card.addEventListener('click', () => focusPlanItem(item.localId));

    return card;
  }

  function buildField(labelText, type, value, fieldName, localId) {
    const container = document.createElement('label');
    container.className = 'field';
    const span = document.createElement('span');
    span.textContent = labelText;
    const input = document.createElement('input');
    input.type = type;
    input.value = value || '';
    input.dataset.field = fieldName;
    input.dataset.id = localId;
    input.addEventListener('input', handleItemFieldChange);
    container.append(span, input);
    return { container, input };
  }

  function buildTextarea(labelText, value, fieldName, localId) {
    const container = document.createElement('label');
    container.className = 'field';
    const span = document.createElement('span');
    span.textContent = labelText;
    const textarea = document.createElement('textarea');
    textarea.value = value || '';
    textarea.dataset.field = fieldName;
    textarea.dataset.id = localId;
    textarea.addEventListener('input', handleItemFieldChange);
    container.append(span, textarea);
    return { container, textarea };
  }

  function handleItemFieldChange(event) {
    const field = event.target.dataset.field;
    const id = event.target.dataset.id;
    const value = event.target.value;
    const target = planState.items.find(it => it.localId === id);
    if (!target) return;
    target[field] = value;
  }

  function removePlanItem(id) {
    planState.items = planState.items.filter(item => item.localId !== id);
    renderPlanItems();
  }

  function handleDragStart(event) {
    draggingId = event.currentTarget.dataset.id;
    event.dataTransfer.effectAllowed = 'move';
    event.currentTarget.classList.add('dragging');
  }

  function handleDragEnd(event) {
    event.currentTarget.classList.remove('dragging');
    draggingId = null;
  }

  function handleDrop(event, targetId) {
    event.preventDefault();
    if (!draggingId || draggingId === targetId) return;
    const sourceIndex = planState.items.findIndex(it => it.localId === draggingId);
    const targetIndex = planState.items.findIndex(it => it.localId === targetId);
    if (sourceIndex === -1 || targetIndex === -1) return;
    const [moved] = planState.items.splice(sourceIndex, 1);
    planState.items.splice(targetIndex, 0, moved);
    renderPlanItems();
  }

  function updatePlanEmptyState() {
    const count = planState.items.length;
    planCountEl.textContent = `${count}개의 여행지가 추가되었습니다.`;
    planEmptyEl.style.display = count === 0 ? 'block' : 'none';
  }

  function refreshItineraryMap() {
    itineraryMarkers.forEach(marker => marker.setMap(null));
    itineraryMarkers = [];
    if (itineraryPolyline) {
      itineraryPolyline.setMap(null);
      itineraryPolyline = null;
    }
    if (!planState.items.length) {
      return;
    }
    const bounds = new kakao.maps.LatLngBounds();
    const path = [];

    planState.items.forEach((item, index) => {
      if (!item.mapx || !item.mapy) return;
      const position = new kakao.maps.LatLng(item.mapy, item.mapx);
      const marker = new kakao.maps.Marker({
        map,
        position,
        title: item.title || '',
        label: { text: String(index + 1) }
      });
      kakao.maps.event.addListener(marker, 'click', () => focusPlanItem(item.localId));
      itineraryMarkers.push(marker);
      bounds.extend(position);
      path.push(position);
    });

    if (path.length >= 2) {
      itineraryPolyline = new kakao.maps.Polyline({
        path,
        strokeWeight: 4,
        strokeColor: '#2563eb',
        strokeOpacity: 0.8,
        strokeStyle: 'solid'
      });
      itineraryPolyline.setMap(map);
    }

    if (!bounds.isEmpty()) {
      map.setBounds(bounds);
    }
  }

  function focusSpotOnMap(spot) {
    const x = Number(spot.mapx || 0);
    const y = Number(spot.mapy || 0);
    if (!x || !y) return;
    const position = new kakao.maps.LatLng(y, x);
    map.panTo(position);
    infoWindow.setContent(buildInfoHtml(spot.title || '', spot.addr1 || ''));
    infoWindow.setPosition(position);
    infoWindow.open(map);
  }

  function focusPlanItem(localId) {
    const item = planState.items.find(it => it.localId === localId);
    if (!item || !item.mapx || !item.mapy) return;
    const position = new kakao.maps.LatLng(item.mapy, item.mapx);
    map.panTo(position);
    infoWindow.setContent(buildInfoHtml(item.title || '', item.address || ''));
    infoWindow.setPosition(position);
    infoWindow.open(map);
  }

  function buildInfoHtml(title, address) {
    return `
      <div class="infowindow">
        <strong>${escapeHtml(title)}</strong>
        <span class="muted">${escapeHtml(address)}</span>
      </div>
    `;
  }

  function escapeHtml(value) {
    return (value || '').replace(/[&<>"']/g, (ch) => ({
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      '"': '&quot;',
      "'": '&#39;'
    })[ch]);
  }

  async function handleSavePlan() {
    updatePlanMeta();
    if (!planState.title) {
      alert('여행 제목을 입력해 주세요.');
      inputs.title.focus();
      return;
    }
    if (!planState.items.length) {
      alert('최소 1개 이상의 여행지를 동선에 추가해야 합니다.');
      return;
    }
    try {
      const response = await fetch(`${CTX}/plan?action=save-plan`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(buildPayload())
      });
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }
      const data = await response.json();
      planState.planId = data.planId;
      openModal('여행 계획이 저장되었습니다.');
    } catch (error) {
      console.error(error);
      alert('저장 중 문제가 발생했습니다. 콘솔 로그를 확인하세요.');
    }
  }

  function buildPayload() {
    return {
      planId: planState.planId,
      title: planState.title,
      startDate: planState.startDate,
      endDate: planState.endDate,
      budget: planState.budget,
      memo: planState.memo,
      items: planState.items.map((item, index) => ({
        localId: item.localId,
        placeId: item.placeId,
        title: item.title,
        address: item.address,
        day: item.day,
        startTime: item.startTime,
        endTime: item.endTime,
        memo: item.memo,
        mapx: item.mapx,
        mapy: item.mapy,
        order: index + 1
      }))
    };
  }

  async function fetchTempPlans() {
    try {
      const data = await fetchJSON(`${CTX}/plan?action=list-json`);
      alert(`임시 저장된 계획 수: ${data.length}\nDB 연동이 완료되면 목록 페이지로 대체하세요.`);
    } catch (error) {
      console.error(error);
      alert('임시 저장본을 불러오지 못했습니다.');
    }
  }

  function fetchJSON(url) {
    return fetch(url).then((res) => {
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      return res.json();
    });
  }

  function toArray(items) {
    if (!items) return [];
    return Array.isArray(items) ? items : [items];
  }
})();
</script>
</body>
</html>
