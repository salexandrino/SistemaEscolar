(function () {
    const statusLabelMap = {
        Todos: '',
        Ativo: 'ATIVO',
        Pendente: 'PENDENTE',
        Bloqueado: 'BLOQUEADO'
    };

    const statusTextMap = {
        ATIVO: 'Ativo',
        PENDENTE: 'Pendente',
        BLOQUEADO: 'Bloqueado'
    };

    const state = {
        search: '',
        perfil: '',
        status: '',
        escolaId: '',
        page: 1,
        size: 20,
        usuarios: [],
        filtered: [],
        escolas: new Map()
    };

    let root;
    let debounceTimer;

    function qs(selector) {
        return root.querySelector(selector);
    }

    function qsa(selector) {
        return Array.from(root.querySelectorAll(selector));
    }

    function getStatus(usuario) {
        if (usuario.bloqueado) return 'BLOQUEADO';
        if (!usuario.ativo) return 'PENDENTE';
        return 'ATIVO';
    }

    function escolaNome(escolaId) {
        if (!escolaId) return '—';
        return state.escolas.get(String(escolaId)) || 'Escola não encontrada';
    }

    function initFromUrl() {
        const params = new URLSearchParams(window.location.search);
        state.search = params.get('search') || '';
        state.perfil = params.get('perfil') || '';
        state.status = (params.get('status') || '').toUpperCase();
        if (!['ATIVO', 'PENDENTE', 'BLOQUEADO'].includes(state.status)) state.status = '';
        state.escolaId = params.get('escolaId') || '';
        state.page = Math.max(parseInt(params.get('page') || '1', 10), 1);
        state.size = Math.min(Math.max(parseInt(params.get('size') || '20', 10), 1), 100);
    }

    function syncControls() {
        qs('#userSearch').value = state.search;
        qs('#userPerfil').value = state.perfil || 'Todos';
        qs('#userStatus').value = Object.keys(statusLabelMap).find((label) => statusLabelMap[label] === state.status) || 'Todos';
        qs('#userEscola').value = state.escolaId || 'Todas';
    }

    function syncUrl() {
        const params = new URLSearchParams();
        if (state.search) params.set('search', state.search);
        if (state.perfil) params.set('perfil', state.perfil);
        if (state.status) params.set('status', state.status);
        if (state.escolaId) params.set('escolaId', state.escolaId);
        if (state.page > 1) params.set('page', state.page);
        if (state.size !== 20) params.set('size', state.size);
        const nextUrl = `${window.location.pathname}${params.toString() ? `?${params}` : ''}`;
        window.history.replaceState({}, '', nextUrl);
    }

    function showRedirectToast() {
        const params = new URLSearchParams(window.location.search);
        const criado = params.get('criado') === '1';
        const atualizado = params.get('atualizado') === '1';
        if (!criado && !atualizado) return;

        window.showToast(criado ? 'Usuário cadastrado com sucesso.' : 'Usuário atualizado com sucesso.', 'success');
        params.delete('criado');
        params.delete('atualizado');
        const nextUrl = `${window.location.pathname}${params.toString() ? `?${params}` : ''}`;
        window.history.replaceState({}, '', nextUrl);
    }

    function showState(name) {
        qsa('[data-state]').forEach((element) => {
            element.hidden = element.dataset.state !== name;
        });
        qs('[data-pagination]').hidden = name !== 'table';
    }

    function setKpis() {
        const total = state.usuarios.length;
        const pendentes = state.usuarios.filter((usuario) => getStatus(usuario) === 'PENDENTE').length;
        const bloqueados = state.usuarios.filter((usuario) => getStatus(usuario) === 'BLOQUEADO').length;
        qs('[data-kpi="totalUsuarios"]').textContent = total;
        qs('[data-kpi="usuariosPendentes"]').textContent = pendentes;
        qs('[data-kpi="usuariosBloqueados"]').textContent = bloqueados;
    }

    function hasActiveFilters() {
        return Boolean(state.search || state.perfil || state.status || state.escolaId);
    }

    function applyFilters() {
        const search = state.search.toLowerCase();
        state.filtered = state.usuarios.filter((usuario) => {
            const status = getStatus(usuario);
            const nome = (usuario.nomeCompleto || '').toLowerCase();
            const email = (usuario.email || '').toLowerCase();
            const matchesSearch = !search || nome.includes(search) || email.includes(search);
            const matchesPerfil = !state.perfil || usuario.perfil === state.perfil;
            const matchesStatus = !state.status || status === state.status;
            const matchesEscola = !state.escolaId || String(usuario.escolaId || '') === state.escolaId;
            return matchesSearch && matchesPerfil && matchesStatus && matchesEscola;
        });
    }

    function renderSummary(totalPages) {
        const currentPage = totalPages === 0 ? 1 : state.page;
        qs('[data-results-summary]').textContent = `${state.filtered.length} resultado(s). Página ${currentPage} de ${totalPages || 1}.`;
        qs('[data-pagination-summary]').textContent = `${state.filtered.length} resultado(s)`;
    }

    function pageList(totalPages, currentPage) {
        if (totalPages <= 7) return Array.from({ length: totalPages }, (_, index) => index + 1);
        const pages = new Set([1, totalPages, currentPage]);
        if (currentPage > 2) pages.add(currentPage - 1);
        if (currentPage < totalPages - 1) pages.add(currentPage + 1);
        return Array.from(pages).sort((a, b) => a - b);
    }

    function renderPagination(totalPages) {
        const pagesContainer = qs('[data-page-numbers]');
        const previous = qs('[data-page-action="previous"]');
        const next = qs('[data-page-action="next"]');
        const hasPrevious = state.page > 1;
        const hasNext = state.page < totalPages;

        previous.disabled = !hasPrevious;
        next.disabled = !hasNext;
        previous.dataset.targetPage = hasPrevious ? state.page - 1 : state.page;
        next.dataset.targetPage = hasNext ? state.page + 1 : state.page;
        pagesContainer.replaceChildren();

        pageList(totalPages, state.page).forEach((page) => {
            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'k-button k-button--outline k-button--sm gestao-usuarios-page-button';
            button.textContent = page;
            button.dataset.targetPage = page;
            button.classList.toggle('is-current', page === state.page);
            button.disabled = page === state.page;
            pagesContainer.appendChild(button);
        });
    }

    function renderRows(items) {
        const tbody = qs('[data-users-table-body]');
        const template = document.getElementById('user-row-template');
        tbody.replaceChildren();

        items.forEach((usuario) => {
            const row = template.content.firstElementChild.cloneNode(true);
            const status = getStatus(usuario);
            const badge = row.querySelector('[data-user-field="status"]');
            const approve = row.querySelector('[data-user-action="approve"]');
            const block = row.querySelector('[data-user-action="block"]');
            const unlock = row.querySelector('[data-user-action="unlock"]');
            const reactivate = row.querySelector('[data-user-action="reactivate"]');

            row.querySelector('[data-user-field="nome"]').textContent = usuario.nomeCompleto || 'Não informado';
            row.querySelector('[data-user-field="email"]').textContent = usuario.email || 'Não informado';
            row.querySelector('[data-user-field="perfil"]').textContent = usuario.perfil || 'Não informado';
            row.querySelector('[data-user-field="escola"]').textContent = escolaNome(usuario.escolaId);
            badge.textContent = statusTextMap[status] || status;
            badge.classList.toggle('is-inactive', status === 'PENDENTE');
            badge.classList.toggle('is-danger', status === 'BLOQUEADO');

            row.querySelector('[data-user-action="details"]').href = `/dashboard/usuarios/visualizar/${usuario.id}`;
            row.querySelector('[data-user-action="edit"]').href = `/dashboard/usuarios/editar/${usuario.id}`;

            [approve, block, unlock, reactivate].forEach((button) => {
                button.dataset.userId = usuario.id;
                button.dataset.userName = usuario.nomeCompleto || 'este usuário';
            });
            block.innerHTML = '<i class="bi bi-person-dash me-2" aria-hidden="true"></i>Inativar';

            approve.hidden = status !== 'PENDENTE';
            block.hidden = status !== 'ATIVO';
            unlock.hidden = status !== 'BLOQUEADO';
            reactivate.hidden = true;

            tbody.appendChild(row);
        });
    }

    function render() {
        syncUrl();
        applyFilters();
        setKpis();

        const totalPages = Math.ceil(state.filtered.length / state.size);
        if (state.page > totalPages) state.page = totalPages || 1;
        renderSummary(totalPages);

        if (state.filtered.length === 0) {
            showState(hasActiveFilters() ? 'no-results' : 'empty');
            return;
        }

        const start = (state.page - 1) * state.size;
        renderRows(state.filtered.slice(start, start + state.size));
        renderPagination(totalPages);
        showState('table');
    }

    function populateEscolasSelect() {
        const select = qs('#userEscola');
        select.replaceChildren(new Option('Todas', 'Todas'));
        Array.from(state.escolas.entries())
            .sort((a, b) => a[1].localeCompare(b[1], 'pt-BR'))
            .forEach(([id, nome]) => select.appendChild(new Option(nome, id)));
    }

    async function loadData() {
        showState('loading');
        try {
            const [usuariosResponse, escolasResponse] = await Promise.all([
                window.usuariosApi.listarUsuarios(),
                window.usuariosApi.listarEscolas()
            ]);

            state.usuarios = usuariosResponse.usuarios || [];
            state.escolas = new Map((escolasResponse.items || []).map((escola) => [String(escola.id), escola.nome]));
            populateEscolasSelect();
            syncControls();
            render();
        } catch (error) {
            if (error.status === 401) {
                window.showToast('Sessão expirada. Faça login novamente.', 'warning');
                window.setTimeout(() => { window.location.href = '/super-admin/login'; }, 1200);
            } else {
                window.showToast(error.message || 'Não foi possível carregar os usuários.', 'error');
            }
            showState('error');
        }
    }

    async function runAction(button, action) {
        const id = button.dataset.userId;
        const name = button.dataset.userName || 'este usuário';
        const config = {
            approve: {
                title: 'Aprovar usuário?',
                message: `O usuário ${name} passará a acessar o sistema.`,
                label: 'Aprovar usuário',
                request: () => window.usuariosApi.aprovarUsuario(id)
            },
            block: {
                title: 'Inativar usuário?',
                message: `O usuário ${name} perderá acesso ao sistema até ser aprovado novamente.`,
                label: 'Inativar usuário',
                request: () => window.usuariosApi.inativarUsuario(id)
            },
            unlock: {
                title: 'Desbloquear usuário?',
                message: `O usuário ${name} poderá tentar acessar o sistema novamente.`,
                label: 'Desbloquear usuário',
                request: () => window.usuariosApi.desbloquearUsuario(id)
            },
            reactivate: {
                title: 'Reativar usuário?',
                message: `O usuário ${name} voltará a acessar o sistema.`,
                label: 'Reativar usuário',
                request: () => window.usuariosApi.reativarUsuario(id)
            }
        }[action];

        if (!config) return;
        const confirmed = await window.confirmarAcao(config.message, config.title, config.label);
        if (!confirmed) return;

        try {
            const response = await config.request();
            window.showToast(response.message || 'Ação concluída com sucesso.', 'success');
            await loadData();
        } catch (error) {
            window.showToast(error.message || 'Não foi possível completar a ação.', 'error');
        }
    }

    function bindEvents() {
        qs('#userSearch').addEventListener('input', (event) => {
            window.clearTimeout(debounceTimer);
            debounceTimer = window.setTimeout(() => {
                state.search = event.target.value.trim();
                state.page = 1;
                render();
            }, 400);
        });

        qs('#userPerfil').addEventListener('change', (event) => {
            state.perfil = event.target.value === 'Todos' ? '' : event.target.value;
            state.page = 1;
            render();
        });

        qs('#userStatus').addEventListener('change', (event) => {
            state.status = statusLabelMap[event.target.value] || '';
            state.page = 1;
            render();
        });

        qs('#userEscola').addEventListener('change', (event) => {
            state.escolaId = event.target.value === 'Todas' ? '' : event.target.value;
            state.page = 1;
            render();
        });

        qs('[data-action="retry"]').addEventListener('click', loadData);

        qs('[data-pagination]').addEventListener('click', (event) => {
            const button = event.target.closest('[data-target-page]');
            if (!button || button.disabled) return;
            state.page = Number(button.dataset.targetPage);
            render();
        });

        qs('[data-users-table-body]').addEventListener('click', (event) => {
            const button = event.target.closest('[data-user-action]');
            if (!button || button.tagName === 'A') return;
            runAction(button, button.dataset.userAction);
        });
    }

    document.addEventListener('DOMContentLoaded', () => {
        root = document.querySelector('[data-page="gestao-usuarios"]');
        if (!root || !window.usuariosApi) return;
        showRedirectToast();
        initFromUrl();
        syncControls();
        bindEvents();
        loadData();
    });
})();
