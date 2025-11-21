(() => {
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
    const url = new URL(`${CTX}/api/areas`);
    if (areaCode) {
        url.searchParams.set('areaCode', areaCode);
    }
    const json = await fetchJSON(url);
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
    const params = new URLSearchParams();
    if (searchArea.value) params.set('areaCode', searchArea.value);
    if (searchSigungu.value) params.set('sigunguCode', searchSigungu.value);
    const activeChip = chips.querySelector('.chip.active');
    if (activeChip && activeChip.dataset.type) {
      params.set('contentTypeId', activeChip.dataset.type);
    }
    const json = await fetchJSON(`${CTX}/api/places?${params.toString()}`);
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
    return (
      `<div class="infowindow">
        <strong>${escapeHtml(title)}</strong>
        <span class="muted">${escapeHtml(address)}</span>
      </div>`
    );
  }

  function escapeHtml(value) {
    return (value || '').replace(/[&<>'"\]/g, (ch) => ({
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      '"': '&quot;',
      "'": '&#39;',
      '`': '&#96;'
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
      const response = await fetch(`${CTX}/plan/save-plan`, {
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
      const data = await fetchJSON(`${CTX}/plan/list-json`);
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