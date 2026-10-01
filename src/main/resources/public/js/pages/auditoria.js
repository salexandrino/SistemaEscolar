(function () {
    let root;
    let eventos = [];

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
    }

    function filteredEvents() {
        const action = qs('#auditAction').value.toLowerCase();
        const entity = qs('#auditEntity').value.toLowerCase();
        const start = qs('#auditStartDate').value;
        const end = qs('#auditEndDate').value;
        return eventos.filter((event) => {
            const created = (event.criadoEm || event.criado_em || '').slice(0, 10);
            const matchesAction = !action || String(event.acao || '').toLowerCase().includes(action);
            const matchesEntity = !entity || String(event.entidade || '').toLowerCase().includes(entity);
            const matchesStart = !start || created >= start;
            const matchesEnd = !end || created <= end;
            return matchesAction && matchesEntity && matchesStart && matchesEnd;
        });
    }

    function renderEvents() {
        const items = filteredEvents();
        const tbody = qs('[data-audit-events]');
        const template = document.getElementById('audit-event-template');
        tbody.replaceChildren();

        if (eventos.length === 0) {
            showEventState('empty');
            return;
        }
        if (items.length === 0) {
            showEventState('no-results');
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
            meta.textContent = data.meta;
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
                meta: alerta.link || ''
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
            const response = await window.auditoriaApi.listarEventos();
            eventos = response.items || [];
            qs('[data-audit-summary]').textContent = response.pendingBackend
                ? 'TODO backend: criar endpoint de listagem de auditoria.'
                : `${eventos.length} evento(s) encontrado(s).`;
            renderEvents();
        } catch (error) {
            window.showToast(error.message || 'Não foi possível carregar a auditoria.', 'error');
            showEventState('error');
        }
    }

    function bindEvents() {
        qsa('[data-audit-tab]').forEach((button) => {
            button.addEventListener('click', () => showPanel(button.dataset.auditTab));
        });
        ['#auditAction', '#auditEntity', '#auditStartDate', '#auditEndDate'].forEach((selector) => {
            qs(selector).addEventListener('input', renderEvents);
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
