
'use strict';
(() => {
    const CSRF_URL = '/api/v1/auth/csrf';
    const REFRESH_URL = '/api/v1/auth/refresh';
    const REFRESH_INTERVAL_MS = 7 * 60 * 1000;
    const SHARED_TIMESTAMP_KEY = 'putevodika-admin-last-refresh';
    const LOCK_NAME = 'putevodika-admin-refresh';

    let lastLocalSuccess = 0;
    let pending = null;
    let warningElement = null;

    function lastSuccessfulRefresh() {
        let shared = 0;
        try {
            shared = Number(localStorage.getItem(SHARED_TIMESTAMP_KEY) || '0');
        } catch (_) {
            // Например, браузер заблокировал localStorage.
        }
        if (!Number.isFinite(shared) || shared < 0 || shared > Date.now()) {
            shared = 0;
        }
        return Math.max(shared, lastLocalSuccess);
    }

    function recordSuccess() {
        lastLocalSuccess = Date.now();
        try {
            localStorage.setItem(SHARED_TIMESTAMP_KEY, String(lastLocalSuccess));
        } catch (_) {
            // Сессия не зависит от localStorage.
        }
        if (warningElement) {
            warningElement.remove();
            warningElement = null;
        }
    }

    function showWarning(status) {
        if (warningElement) return;
        const notice = document.createElement('div');
        notice.className = 'admin-session-warning';
        notice.setAttribute('role', 'status');
        notice.textContent = 'Не удалось автоматически продлить сессию' +
            (status ? ' (HTTP ' + status + ')' : '') +
            '. Просмотр и отправка форм не блокируются. Если возникнет ошибка доступа, сохраните данные и войдите снова.';
        const loginLink = document.createElement('a');
        loginLink.href = '/admin/login';
        loginLink.textContent = 'Страница входа';
        notice.append(' ', loginLink);
        document.body.prepend(notice);
        warningElement = notice;
    }

    async function readCsrfToken() {
        // Принудительно обращаемся к серверу: токен может ещё не быть
        // создан при GET-запросе HTML-страницы админки.
        const response = await fetch(CSRF_URL, {
            method: 'GET',
            credentials: 'same-origin',
            cache: 'no-store',
            headers: { 'Accept': 'application/json' }
        });
        if (!response.ok) {
            return { token: null, status: response.status };
        }
        const data = await response.json();
        const token = typeof data.token === 'string' ? data.token : null;
        return { token, status: response.status };
    }

    async function attemptRefresh() {
        try {
            const csrf = await readCsrfToken();
            if (!csrf.token) {
                return { ok: false, status: csrf.status };
            }
            const response = await fetch(REFRESH_URL, {
                method: 'POST',
                credentials: 'same-origin',
                cache: 'no-store',
                headers: { 'X-XSRF-TOKEN': csrf.token }
            });
            if (!response.ok) {
                return { ok: false, status: response.status };
            }
            recordSuccess();
            return { ok: true, status: response.status };
        } catch (_) {
            // Ошибка сети или недоступный backend; формы не блокируем.
            return { ok: false, status: null };
        }
    }

    function refreshIfDue() {
        if (pending) return pending;

        const run = async () => {
            if (Date.now() - lastSuccessfulRefresh() < REFRESH_INTERVAL_MS) {
                return { ok: true, skipped: true };
            }
            // Разные вкладки не должны одновременно ротировать refresh-токен.
            const work = async () => {
                // После получения межвкладочной блокировки проверим время ещё раз.
                if (Date.now() - lastSuccessfulRefresh() < REFRESH_INTERVAL_MS) {
                    return { ok: true, skipped: true };
                }
                return attemptRefresh();
            };
            if (navigator.locks && typeof navigator.locks.request === 'function') {
                return navigator.locks.request(LOCK_NAME, work);
            }
            return work();
        };

        pending = run()
            .then(result => {
                if (!result.ok) {
                    console.warn('Putevodika Admin: автоматическое продление не удалось',
                        result.status === null ? '(сеть)' : '(HTTP ' + result.status + ')');
                    showWarning(result.status);
                }
                return result;
            })
            .catch(() => {
                showWarning(null);
                return { ok: false, status: null };
            })
            .finally(() => {
                pending = null;
            });
        return pending;
    }

    // Первая проверка при входе на страницу; далее — раз в семь минут.
    // При переходах между страницами сработает общий timestamp и новый
    // POST /refresh не будет выполняться без необходимости.
    void refreshIfDue();
    setInterval(() => {
        if (!document.hidden) void refreshIfDue();
    }, REFRESH_INTERVAL_MS);

    document.addEventListener('visibilitychange', () => {
        if (!document.hidden) void refreshIfDue();
    });
    window.addEventListener('focus', () => {
        if (!document.hidden) void refreshIfDue();
    });

    // Здесь намеренно нет обработчиков submit: они должны работать штатно.
})();
