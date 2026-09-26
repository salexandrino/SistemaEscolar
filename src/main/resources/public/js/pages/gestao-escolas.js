(function () {
    const statusMap = {
        Todos: '',
        Ativas: 'ATIVA',
        Inativas: 'INATIVA'
    };

    const statusLabelMap = {
        '': 'Todos',
        ATIVA: 'Ativas',
        INATIVA: 'Inativas'
    };

    const state = {
        search: '',
        status: '',
        page: 1,
        size: 20,
        totalItems: 0,
        totalPages: 0
    };

    let root;
    let debounceTimer;

    function qs(selector) {
        return root.querySelector(selector);
    }

    function qsa(selector) {
        return Array.from(root.querySelectorAll(selector));
    }

    function initFromUrl() {
        const params = new URLSearchParams(window.location.search);
        state.search = params.get('search') || '';
        state.status = ['ATIVA', 'INATIVA'].includes(params.get('status')) ? params.get('status') : '';
        state.page = Math.max(parseInt(params.get('page') || '1', 10), 1);
        state.size = Math.min(Math.max(parseInt(params.get('size') || '20', 10), 1), 100);
    }

    function syncControls() {
        qs('#schoolSearch').value = state.search;
        qs('#schoolStatus').value = statusLabelMap[state.status] || 'Todos';
    }

    function syncUrl() {
        const params = new URLSearchParams();
        if (state.search) params.set('search', state.search);
        if (state.status) params.set('status', state.status);
        if (state.page > 1) params.set('page', state.page);
        if (state.size !== 20) params.set('size', state.size);
        const nextUrl = `${window.location.pathname}${params.toString() ? `?${params}` : ''}`;
        window.history.replaceState({}, '', nextUrl);
    }

    function showState(name) {
        qsa('[data-state]').forEach((element) => {
            element.hidden = element.dataset.state !== name;
        });
        qs('[data-pagination]').hidden = name !== 'table';
    }

    function setKpiLoading(isLoading) {
        qsa('[data-kpi-card]').forEach((card) => card.classList.toggle('is-loading', isLoading));
    }

    async function loadKpis() {
        setKpiLoading(true);
        try {
            const dashboard = await window.escolasApi.buscarDashboard();
            qs('[data-kpi="totalEscolas"]').textContent = dashboard.totalEscolas ?? 0;
            qs('[data-kpi="escolasAtivas"]').textContent = dashboard.escolasAtivas ?? 0;
            qs('[data-kpi="escolasInativas"]').textContent = dashboard.escolasInativas ?? 0;
        } catch (error) {
            window.showToast(error.message || 'Não foi possível carregar os indicadores.', 'error');
        } finally {
            setKpiLoading(false);
        }
    }

    function hasActiveFilters() {
        return Boolean(state.search || state.status);
    }

    function renderSummary(data) {
        state.totalItems = data.totalItems || 0;
        state.totalPages = data.totalPages || 0;
        const currentPage = state.totalPages === 0 ? 1 : data.page;
        qs('[data-results-summary]').textContent = `${state.totalItems} resultado(s). Página ${currentPage} de ${state.totalPages || 1}.`;
        qs('[data-pagination-summary]').textContent = `${state.totalItems} resultado(s)`;
    }

    function renderRows(items) {
        const tbody = qs('[data-schools-table-body]');
        const template = document.getElementById('school-row-template');
        tbody.replaceChildren();

        items.forEach((school) => {
            const row = template.content.firstElementChild.cloneNode(true);
            const status = school.status || '';
            const isInactive = status === 'INATIVA';
            const statusBadge = row.querySelector('[data-school-field="status"]');
            const toggle = row.querySelector('[data-school-action="toggle"]');

            row.querySelector('[data-school-field="nome"]').textContent = school.nome || 'Não informado';
            row.querySelector('[data-school-field="cnpj"]').textContent = school.cnpj || 'Não informado';
            row.querySelector('[data-school-field="cidade"]').textContent = school.cidade || 'Não informado';
            statusBadge.textContent = status || 'Não informado';
            statusBadge.classList.toggle('is-inactive', isInactive);

            row.querySelector('[data-school-action="details"]').href = `/dashboard/escolas/visualizar/${school.id}`;
            row.querySelector('[data-school-action="edit"]').href = `/dashboard/escolas/editar/${school.id}`;
            toggle.dataset.schoolId = school.id;
            toggle.dataset.schoolName = school.nome || 'esta escola';
            toggle.dataset.schoolStatus = status;
            toggle.innerHTML = isInactive
                ? '<i class="bi bi-check-circle me-2" aria-hidden="true"></i>Ativar escola'
                : '<i class="bi bi-slash-circle me-2" aria-hidden="true"></i>Inativar escola';
            toggle.classList.toggle('text-danger', !isInactive);
            toggle.classList.toggle('text-success', isInactive);

            tbody.appendChild(row);
        });
    }

    function pageList(totalPages, currentPage) {
        if (totalPages <= 7) return Array.from({ length: totalPages }, (_, index) => index + 1);
        const pages = new Set([1, totalPages, currentPage]);
        if (currentPage > 2) pages.add(currentPage - 1);
        if (currentPage < totalPages - 1) pages.add(currentPage + 1);
        return Array.from(pages).sort((a, b) => a - b);
    }

    function renderPagination(data) {
        const pagesContainer = qs('[data-page-numbers]');
        const previous = qs('[data-page-action="previous"]');
        const next = qs('[data-page-action="next"]');
        const totalPages = data.totalPages || 0;
        const currentPage = data.page || 1;
        const hasPrevious = currentPage > 1;
        const hasNext = currentPage < totalPages;

        previous.disabled = !hasPrevious;
        next.disabled = !hasNext;
        previous.dataset.targetPage = hasPrevious ? currentPage - 1 : currentPage;
        next.dataset.targetPage = hasNext ? currentPage + 1 : currentPage;
        pagesContainer.replaceChildren();

        pageList(totalPages, currentPage).forEach((page) => {
            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'k-button k-button--outline k-button--sm gestao-escolas-page-button';
            button.textContent = page;
            button.dataset.targetPage = page;
            button.classList.toggle('is-current', page === currentPage);
            button.disabled = page === currentPage;
            pagesContainer.appendChild(button);
        });
    }

    async function loadSchools() {
        syncUrl();
        showState('loading');
        try {
            const data = await window.escolasApi.listarEscolas({
                search: state.search,
                status: state.status,
                page: state.page,
                size: state.size
            });

            state.page = data.page || 1;
            renderSummary(data);

            if (!data.items || data.items.length === 0) {
                showState(hasActiveFilters() ? 'no-results' : 'empty');
                return;
            }

            renderRows(data.items);
            renderPagination(data);
            showState('table');
        } catch (error) {
            if (error.status === 401) {
                window.showToast('Sessão expirada. Faça login novamente.', 'warning');
                window.setTimeout(() => { window.location.href = '/super-admin/login'; }, 1200);
            } else {
                window.showToast(error.message || 'Não foi possível carregar as escolas.', 'error');
            }
            showState('error');
        }
    }

    async function toggleSchool(button) {
        const id = button.dataset.schoolId;
        const name = button.dataset.schoolName || 'esta escola';
        const active = button.dataset.schoolStatus === 'ATIVA';
        const confirmed = await window.confirmarAcao(
            active ? `A escola ${name} perderá acesso ao sistema.` : `A escola ${name} voltará a ter acesso ao sistema.`,
            active ? 'Inativar escola?' : 'Ativar escola?',
            active ? 'Inativar escola' : 'Ativar escola'
        );

        if (!confirmed) return;

        try {
            const response = active
                ? await window.escolasApi.inativarEscola(id)
                : await window.escolasApi.ativarEscola(id);
            window.showToast(response.message || 'Ação concluída com sucesso.', 'success');
            await Promise.all([loadSchools(), loadKpis()]);
        } catch (error) {
            window.showToast(error.message || 'Não foi possível completar a ação.', 'error');
        }
    }

    function bindEvents() {
        qs('#schoolSearch').addEventListener('input', (event) => {
            window.clearTimeout(debounceTimer);
            debounceTimer = window.setTimeout(() => {
                state.search = event.target.value.trim();
                state.page = 1;
                loadSchools();
            }, 400);
        });

        qs('#schoolStatus').addEventListener('change', (event) => {
            state.status = statusMap[event.target.value] || '';
            state.page = 1;
            loadSchools();
        });

        qs('[data-action="retry"]').addEventListener('click', loadSchools);

        qs('[data-pagination]').addEventListener('click', (event) => {
            const button = event.target.closest('[data-target-page]');
            if (!button || button.disabled) return;
            state.page = Number(button.dataset.targetPage);
            loadSchools();
        });

        qs('[data-schools-table-body]').addEventListener('click', (event) => {
            const button = event.target.closest('[data-school-action="toggle"]');
            if (!button) return;
            toggleSchool(button);
        });
    }

    document.addEventListener('DOMContentLoaded', () => {
        root = document.querySelector('[data-page="gestao-escolas"]');
        if (!root || !window.escolasApi) return;
        initFromUrl();
        syncControls();
        bindEvents();
        loadKpis();
        loadSchools();
    });
})();
