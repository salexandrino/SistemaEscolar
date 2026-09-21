(() => {
    const sidebar = document.querySelector('[data-k-sidebar]');
    const toggle = document.querySelector('[data-k-sidebar-toggle]');
    const backdrop = document.querySelector('[data-k-sidebar-backdrop]');
    if (!sidebar || !toggle) return;
    const dashboard = document.querySelector('.admin-dashboard');
    const setOpen = (open) => {
        sidebar.classList.toggle('is-open', open);
        backdrop?.classList.toggle('is-visible', open);
        toggle.setAttribute('aria-expanded', String(open));
        if (dashboard) {
            toggle.setAttribute('aria-label', open ? 'Fechar menu' : 'Abrir menu');
            if (open) sidebar.querySelector('a')?.focus();
            else if (sidebar.contains(document.activeElement)) toggle.focus();
        }
    };
    toggle.addEventListener('click', () => setOpen(!sidebar.classList.contains('is-open')));
    backdrop?.addEventListener('click', () => setOpen(false));
    document.addEventListener('keydown', (event) => { if (event.key === 'Escape') setOpen(false); });
    if (dashboard) {
        window.matchMedia('(max-width: 991.98px)').addEventListener('change', () => setOpen(false));
    }
})();
