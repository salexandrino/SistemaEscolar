(function () {
    function paramsToQuery(params) {
        const query = new URLSearchParams();
        if (params.search) query.set('search', params.search);
        if (params.status) query.set('status', params.status);
        if (params.page) query.set('page', params.page);
        if (params.size) query.set('size', params.size);
        const queryString = query.toString();
        return queryString ? `?${queryString}` : '';
    }

    function listarEscolas(params) {
        return window.kutuarApi.apiFetch(`/api/admin/escolas${paramsToQuery(params || {})}`);
    }

    function buscarDashboard() {
        return window.kutuarApi.apiFetch('/api/admin/dashboard');
    }

    function ativarEscola(id) {
        return window.kutuarApi.apiFetch(`/escolas/${id}/ativar`, {
            method: 'PATCH',
            referrerPolicy: 'no-referrer'
        });
    }

    function inativarEscola(id) {
        return window.kutuarApi.apiFetch(`/escolas/${id}/inativar`, {
            method: 'PATCH',
            referrerPolicy: 'no-referrer'
        });
    }

    window.escolasApi = {
        listarEscolas,
        buscarDashboard,
        ativarEscola,
        inativarEscola
    };
})();

