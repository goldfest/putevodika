/* Продление административной JWT-сессии через существующий /api/v1/auth/refresh.
 * Никаких токенов в localStorage: лишь время последнего успешного обновления. */
'use strict';
(() => {
    const ENDPOINT = '/api/v1/auth/refresh';
    const REFRESH_INTERVAL_MS = 7 * 60 * 1000;
    const SHARED_TIMESTAMP_KEY = 'putevodika-admin-last-refresh';
    let lastCheck = Date.now();
    let inflight = null;
    let warningShown = false;

    const getCookie = name => {
        const prefix = name + '=';
        const part = document.cookie.split('; ').find(part => part.startsWith(prefix));
        return part ? decodeURIComponent(part.slice(prefix.length)) : '';
    };
    const lastSharedRefresh = () => {
        try { return Number(localStorage.getItem(SHARED_TIMESTAMP_KEY) || '0'); }
        catch (_) { return 0; }
    };
    const updateSharedRefresh = () => {
        try { localStorage.setItem(SHARED_TIMESTAMP_KEY, String(Date.now())); }
        catch (_) { /* Хранилище может быть недоступно. */ }
    };

    function showWarning() {
        if (warningShown) return;
        warningShown = true;
        const notice = document.createElement('div');
        notice.setAttribute('role', 'alert');
        notice.className = 'admin-session-warning';
        notice.textContent = 'Не удалось продлить сессию. Не закрывайте страницу: скопируйте несохранённые данные, затем войдите снова.';
        const link = document.createElement('a');
        link.href = '/admin/login';
        link.textContent = 'Перейти ко входу';
        notice.append(' ', link);
        document.body.prepend(notice);
    }

    async function refreshImpl(force = false) {
        const doRefresh = async () => {
            // Если другая вкладка только что обновила общие cookie, не ротируем refresh-токен повторно.
            const graceMs = force ? 10 * 1000 : REFRESH_INTERVAL_MS;
            if (Date.now() - lastSharedRefresh() < graceMs) return true;
            const csrf = getCookie('XSRF-TOKEN') ||
                document.querySelector('input[name="_csrf"]')?.value;
            if (!csrf) return false;
            try {
                const response = await fetch(ENDPOINT, {
                    method: 'POST',
                    credentials: 'same-origin',
                    cache: 'no-store',
                    headers: { 'X-XSRF-TOKEN': csrf }
                });
                if (!response.ok) return false;
                updateSharedRefresh();
                lastCheck = Date.now();
                return true;
            } catch (_) {
                return false;
            }
        };
        if (navigator.locks?.request) {
            return navigator.locks.request('putevodika-admin-refresh', doRefresh);
        }
        return doRefresh();
    }

    function refresh(force = false) {
        if (inflight) return inflight;
        inflight = refreshImpl(force).finally(() => { inflight = null; });
        return inflight;
    }

    // Автообновление, пока админка открыта.
    setInterval(async () => {
        if (document.hidden) return;
        if (!await refresh()) showWarning();
    }, REFRESH_INTERVAL_MS);

    // Фоновая вкладка может не получать таймеры: обновим при возврате.
    document.addEventListener('visibilitychange', async () => {
        if (!document.hidden && Date.now() - lastCheck >= REFRESH_INTERVAL_MS) {
            if (!await refresh()) showWarning();
        }
    });

    // Перед сохранением формы обеспечим действительный access-токен.
    document.querySelectorAll('form[method="post"], form[method="POST"]').forEach(form => {
        if (form.action.includes('/api/v1/auth/admin-logout')) return;
        form.addEventListener('submit', async event => {
            event.preventDefault();
            const submitButton = event.submitter;
            if (submitButton) submitButton.disabled = true;
            const ok = await refresh(true);
            if (ok) {
                HTMLFormElement.prototype.submit.call(form);
            } else {
                if (submitButton) submitButton.disabled = false;
                showWarning();
            }
        });
    });
})();
