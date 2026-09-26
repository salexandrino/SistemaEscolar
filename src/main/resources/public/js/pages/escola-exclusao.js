(function () {
    const root = document.querySelector('[data-page="escola-exclusao"]');
    if (!root) return;

    const analysisButton = root.querySelector('[data-action="analisar-exclusao"]');
    const feedback = root.querySelector('[data-exclusao-feedback]');
    const details = root.querySelector('[data-exclusao-impacto]');
    const reasons = root.querySelector('[data-exclusao-motivos]');
    const dependencies = root.querySelector('[data-exclusao-dependencias]');
    const schoolId = root.dataset.schoolId;

    function show(message, type) {
        feedback.hidden = false;
        feedback.className = `alert alert-${type}`;
        feedback.textContent = message;
    }

    function setLoading(loading) {
        analysisButton.disabled = loading;
        analysisButton.innerHTML = loading
            ? '<span class="k-button__spinner" aria-hidden="true"></span>Analisando dados da escola...'
            : '<i class="bi bi-search" aria-hidden="true"></i>Analisar exclusão';
    }

    function labelFor(key) {
        return key.replaceAll('_', ' ');
    }

    analysisButton.addEventListener('click', async () => {
        setLoading(true);
        feedback.hidden = true;
        details.hidden = true;
        try {
            const impacto = await window.escolasApi.analisarImpactoExclusao(schoolId);
            reasons.replaceChildren(...impacto.motivosBloqueio.map(motivo => {
                const item = document.createElement('li');
                item.textContent = motivo;
                return item;
            }));
            dependencies.replaceChildren(...Object.entries(impacto.dependencias).map(([nome, total]) => {
                const item = document.createElement('li');
                item.textContent = `${labelFor(nome)}: ${total}`;
                return item;
            }));
            details.hidden = false;
            show('A exclusão definitiva permanece bloqueada até a definição da política de retenção.', 'warning');
        } catch (error) {
            const message = error.status === 404 ? 'Escola não encontrada.' : error.message || 'Não foi possível analisar a exclusão.';
            show(message, error.status === 409 ? 'warning' : 'danger');
            window.showToast(message, 'error');
        } finally {
            setLoading(false);
        }
    });
})();
