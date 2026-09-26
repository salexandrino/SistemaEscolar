(function () {
    class ApiError extends Error {
        constructor(message, status, payload) {
            super(message);
            this.name = 'ApiError';
            this.status = status;
            this.payload = payload;
        }
    }

    async function parseResponse(response) {
        const contentType = response.headers.get('content-type') || '';
        if (contentType.includes('application/json')) {
            return response.json();
        }
        const text = await response.text();
        return text ? { message: text } : {};
    }

    function messageForStatus(status, payload) {
        if (status === 401) return payload.message || 'Sessão expirada. Faça login novamente.';
        if (status === 403) return payload.message || 'Você não tem permissão para acessar este recurso.';
        if (status >= 500) return 'Não foi possível completar a ação.';
        return payload.message || 'Não foi possível completar a ação.';
    }

    async function apiFetch(url, options) {
        const response = await fetch(url, {
            ...(options || {}),
            credentials: 'include',
            headers: {
                Accept: 'application/json',
                ...(options && options.headers ? options.headers : {})
            }
        });

        const payload = await parseResponse(response);
        if (!response.ok) {
            throw new ApiError(messageForStatus(response.status, payload), response.status, payload);
        }
        return payload;
    }

    window.kutuarApi = {
        ApiError,
        apiFetch
    };
})();
