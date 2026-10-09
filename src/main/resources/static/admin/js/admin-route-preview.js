'use strict';
(function () {
    const parent = document.getElementById('routePreview');
    const svg = document.getElementById('routePreviewSvg');
    if (!parent || !svg) return;

    const parsePoint = (latitude, longitude, label, edge) => {
        const lat = Number(latitude);
        const lon = Number(longitude);
        if (!Number.isFinite(lat) || !Number.isFinite(lon)) return null;
        return { lat, lon, label, edge };
    };
    const pts = [parsePoint(parent.dataset.startLat, parent.dataset.startLon, 'Старт', true)];
    for (const row of document.querySelectorAll('[data-route-stop]')) {
        pts.push(parsePoint(row.dataset.lat, row.dataset.lon,
            String(pts.length), false));
    }
    pts.push(parsePoint(parent.dataset.finishLat, parent.dataset.finishLon, 'Финиш', true));
    if (pts.some(p => p === null)) return;

    const minLat = Math.min(...pts.map(p => p.lat));
    const maxLat = Math.max(...pts.map(p => p.lat));
    const minLon = Math.min(...pts.map(p => p.lon));
    const maxLon = Math.max(...pts.map(p => p.lon));
    const latRange = maxLat - minLat;
    const lonRange = maxLon - minLon;
    const scale = Math.min(660 / Math.max(lonRange, 0.00001), 235 / Math.max(latRange, 0.00001));
    const left = (780 - lonRange * scale) / 2;
    const top = (360 - latRange * scale) / 2;
    const coords = pts.map(p => ({
        ...p,
        x: left + (p.lon - minLon) * scale,
        y: top + (maxLat - p.lat) * scale
    }));

    const NS = 'http://www.w3.org/2000/svg';
    const node = (tag, props, content) => {
        const item = document.createElementNS(NS, tag);
        for (const [key, value] of Object.entries(props || {})) {
            item.setAttribute(key, String(value));
        }
        if (content != null) item.textContent = content;
        return item;
    };
    svg.appendChild(node('polyline', {
        class: 'route-preview-line',
        points: coords.map(p => `${p.x},${p.y}`).join(' ')
    }));
    for (let i = 0; i < coords.length; i++) {
        const p = coords[i];
        const marker = node('circle', {
            cx: p.x, cy: p.y, r: p.edge ? 10 : 8,
            class: p.edge ? 'route-preview-point route-preview-point--edge' : 'route-preview-point'
        });
        marker.appendChild(node('title', {}, `${p.label}: ${p.lat}, ${p.lon}`));
        svg.appendChild(marker);
        if (p.edge || coords.length <= 17) {
            svg.appendChild(node('text', {
                x: p.x, y: p.y - 16,
                class: 'route-preview-label'
            }, p.label));
        }
    }
})();
