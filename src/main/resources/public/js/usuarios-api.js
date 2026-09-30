(function () {
    function listarUsuarios() {
        return window.kutuarApi.apiFetch('/users');
    }

    async function listarEscolas() {
        const primeira = await window.kutuarApi.apiFetch('/api/admin/escolas?size=100&page=1');
        let items = primeira.items || [];
        const totalPages = primeira.totalPages || 1;

        for (let page = 2; page <= totalPages; page++) {
            const proxima = await window.kutuarApi.apiFetch(`/api/admin/escolas?size=100&page=${page}`);
            items = items.concat(proxima.items || []);
        }

        return { items };
    }

    function aprovarUsuario(id) {
        return window.kutuarApi.apiFetch(`/users/${id}/approve`, { method: 'PATCH' });
    }

    function inativarUsuario(id) {
        return window.kutuarApi.apiFetch(`/users/${id}`, { method: 'DELETE' });
    }

    function desbloquearUsuario(id) {
        return window.kutuarApi.apiFetch(`/users/${id}/unlock`, { method: 'PATCH' });
    }

    function reativarUsuario(id) {
        return window.kutuarApi.apiFetch(`/users/${id}/reativar`, { method: 'PATCH' });
    }

    window.usuariosApi = {
        listarUsuarios,
        listarEscolas,
        aprovarUsuario,
        inativarUsuario,
        desbloquearUsuario,
        reativarUsuario
    };
})();
