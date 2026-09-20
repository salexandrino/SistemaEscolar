(() => {
    const sidebar = document.querySelector('[data-k-sidebar]');
    const toggle = document.querySelector('[data-k-sidebar-toggle]');
    const backdrop = document.querySelector('[data-k-sidebar-backdrop]');
    if (!sidebar || !toggle) return;
    const setOpen = (open) => {
        sidebar.classList.toggle('is-open', open);
        backdrop?.classList.toggle('is-visible', open);
        toggle.setAttribute('aria-expanded', String(open));
    };
    toggle.addEventListener('click', () => setOpen(!sidebar.classList.contains('is-open')));
    backdrop?.addEventListener('click', () => setOpen(false));
    document.addEventListener('keydown', (event) => { if (event.key === 'Escape') setOpen(false); });
})();
