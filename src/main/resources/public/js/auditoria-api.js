(function () {
    const AUDITORIA_ENDPOINT = '/api/admin/auditoria';

    function listarEventos(filtros) {
        const params = new URLSearchParams();
        const valores = filtros || {};
        params.set('page', String(valores.page || 1));
        params.set('size', String(valores.size || 20));
        ['acao', 'entidade', 'dataInicio', 'dataFim'].forEach((campo) => {
            const valor = valores[campo];
            if (valor !== undefined && valor !== null && String(valor).trim()) {
                params.set(campo, String(valor).trim());
            }
        });
        return window.kutuarApi.apiFetch(`${AUDITORIA_ENDPOINT}?${params.toString()}`);
    }

    function buscarDashboard() {
        return window.kutuarApi.apiFetch('/api/admin/dashboard');
    }

    window.auditoriaApi = {
        listarEventos,
        buscarDashboard
    };
})();
