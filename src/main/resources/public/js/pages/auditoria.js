(function () {
    let root;
    let page = 1;
    let totalPages = 0;
    let totalItems = 0;
    let filterTimer;
    const pageSize = 20;

    function qs(selector) {
        return root.querySelector(selector);
    }

    function qsa(selector) {
        return Array.from(root.querySelectorAll(selector));
    }

    function formatDate(value) {
        if (!value) return '—';
        const date = new Date(value);
        return Number.isNaN(date.getTime()) ? value : date.toLocaleString('pt-BR');
    }

    function showPanel(name) {
        qsa('[data-audit-panel]').forEach((panel) => {
            panel.hidden = panel.dataset.auditPanel !== name;
        });
        qsa('[data-audit-tab]').forEach((button) => {
            const active = button.dataset.auditTab === name;
            button.classList.toggle('k-button--primary', active);
            button.classList.toggle('k-button--outline', !active);
        });
    }

    function showEventState(name) {
        qsa('[data-audit-state]').forEach((element) => {
            element.hidden = element.dataset.auditState !== name;
        });
        qs('[data-audit-pagination]').hidden = name !== 'table';
    }

    function readFilters() {
        return {
            acao: qs('#auditAction').value.trim(),
            entidade: qs('#auditEntity').value.trim(),
            dataInicio: qs('#auditStartDate').value,
            dataFim: qs('#auditEndDate').value
        };
    }

    function hasFilters(filters) {
        return Object.values(filters).some((value) => Boolean(value));
    }

    function renderEvents(response) {
        const items = response.items || [];
        const tbody = qs('[data-audit-events]');
        const template = document.getElementById('audit-event-template');
        tbody.replaceChildren();
        totalItems = response.totalItems || 0;
        totalPages = response.totalPages || 0;
        page = response.page || page;
        qs('[data-audit-summary]').textContent = `${totalItems} evento(s) encontrado(s).`;

        if (items.length === 0) {
            if (hasFilters(readFilters())) {
                qs('[data-audit-no-results-title]').textContent = 'Nenhum evento encontrado para os filtros informados.';
                showEventState('no-results');
            } else {
                showEventState('empty');
            }
            return;
        }

        items.forEach((event) => {
            const row = template.content.firstElementChild.cloneNode(true);
            row.querySelector('[data-audit-field="criadoEm"]').textContent = formatDate(event.criadoEm || event.criado_em);
            row.querySelector('[data-audit-field="executor"]').textContent = event.executor || event.executorId || '—';
            row.querySelector('[data-audit-field="acao"]').textContent = event.acao || '—';
            row.querySelector('[data-audit-field="entidade"]').textContent = event.entidade || '—';
            row.querySelector('[data-audit-field="detalhes"]').textContent = event.detalhes || '—';
            tbody.appendChild(row);
        });
        showEventState('table');
        qs('[data-audit-page-info]').textContent = `Página ${page} de ${totalPages} · ${totalItems} eventos`;
        qs('[data-audit-previous]').disabled = page <= 1;
        qs('[data-audit-next]').disabled = page >= totalPages;
    }

    function internalHref(value) {
        if (typeof value !== 'string' || !value.startsWith('/') || value.startsWith('//')) return null;
        try {
            const url = new URL(value, window.location.origin);
            return url.origin === window.location.origin ? `${url.pathname}${url.search}${url.hash}` : null;
        } catch (error) {
            return null;
        }
    }

    function renderCards(selector, emptySelector, items, formatter) {
        const container = qs(selector);
        const empty = qs(emptySelector);
        container.replaceChildren();
        empty.hidden = items.length > 0;
        items.forEach((item) => {
            const card = document.createElement('article');
            card.className = 'auditoria-card';
            const title = document.createElement('strong');
            const subtitle = document.createElement('span');
            const meta = document.createElement('small');
            const data = formatter(item);
            title.textContent = data.title;
            subtitle.textContent = data.subtitle;
            const href = internalHref(data.link);
            if (href) {
                const link = document.createElement('a');
                link.href = href;
                link.textContent = 'Ver detalhes';
                meta.appendChild(link);
            } else {
                meta.textContent = data.meta || '';
            }
            card.append(title, subtitle, meta);
            container.appendChild(card);
        });
    }

    async function loadDashboardData() {
        try {
            const dashboard = await window.auditoriaApi.buscarDashboard();
            renderCards('[data-alerts-list]', '[data-alerts-empty]', dashboard.alertas || [], (alerta) => ({
                title: alerta.mensagem || alerta.tipo || 'Alerta',
                subtitle: `${alerta.severidade || 'INFO'} · ${alerta.quantidade ?? 0} ocorrência(s)`,
                link: alerta.link
            }));
            renderCards('[data-activities-list]', '[data-activities-empty]', dashboard.atividades || [], (atividade) => ({
                title: atividade.descricao || atividade.tipo || 'Atividade',
                subtitle: atividade.tipo || '',
                meta: formatDate(atividade.ocorridoEm)
            }));
        } catch (error) {
            window.showToast(error.message || 'Não foi possível carregar alertas e atividades.', 'error');
        }
    }

    async function loadEvents() {
        showEventState('loading');
        try {
            const response = await window.auditoriaApi.listarEventos({
                ...readFilters(),
                page,
                size: pageSize
            });
            renderEvents(response);
        } catch (error) {
            qs('[data-audit-error-message]').textContent = error.message || 'Confira sua conexão e tente novamente.';
            showEventState('error');
        }
    }

    function scheduleFilteredSearch() {
        window.clearTimeout(filterTimer);
        filterTimer = window.setTimeout(() => {
            page = 1;
            loadEvents();
        }, 250);
    }

    function bindEvents() {
        qsa('[data-audit-tab]').forEach((button) => {
            button.addEventListener('click', () => showPanel(button.dataset.auditTab));
        });
        ['#auditAction', '#auditEntity', '#auditStartDate', '#auditEndDate'].forEach((selector) => {
            qs(selector).addEventListener('input', scheduleFilteredSearch);
            qs(selector).addEventListener('change', scheduleFilteredSearch);
        });
        qs('[data-audit-clear]').addEventListener('click', () => {
            ['#auditAction', '#auditEntity', '#auditStartDate', '#auditEndDate'].forEach((selector) => {
                qs(selector).value = '';
            });
            page = 1;
            loadEvents();
        });
        qs('[data-audit-retry]').addEventListener('click', loadEvents);
        qs('[data-audit-previous]').addEventListener('click', () => {
            if (page > 1) {
                page--;
                loadEvents();
            }
        });
        qs('[data-audit-next]').addEventListener('click', () => {
            if (page < totalPages) {
                page++;
                loadEvents();
            }
        });
    }

    document.addEventListener('DOMContentLoaded', () => {
        root = document.querySelector('[data-page="auditoria"]');
        if (!root || !window.auditoriaApi) return;
        bindEvents();
        loadEvents();
        loadDashboardData();
    });
})();
