(function () {
    // TODO backend: registrar endpoint real para listagem de eventos de auditoria.
    const AUDITORIA_ENDPOINT = null;

    async function listarEventos() {
        if (!AUDITORIA_ENDPOINT) {
            return { items: [], pendingBackend: true };
        }
        return window.kutuarApi.apiFetch(AUDITORIA_ENDPOINT);
    }

    function buscarDashboard() {
        return window.kutuarApi.apiFetch('/api/admin/dashboard');
    }

    window.auditoriaApi = {
        listarEventos,
        buscarDashboard
    };
})();
