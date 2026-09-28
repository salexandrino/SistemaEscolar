// Menus em tabelas precisam escapar dos wrappers responsivos (overflow-x: auto).
// O Bootstrap segue responsável por foco, teclado, fechamento e Popper; enquanto
// aberto, o menu é renderizado no body como um portal.
(() => {
    const portalSelector = '[data-k-dropdown-portal]';
    const portalAnchors = new WeakMap();
    const portalMenus = new WeakMap();

    document.addEventListener('show.bs.dropdown', (event) => {
        const trigger = event.target;
        const dropdown = trigger.closest(portalSelector);
        if (!dropdown) return;

        const menu = dropdown.querySelector('.dropdown-menu');
        if (!menu || portalAnchors.has(menu)) return;

        const anchor = document.createComment('k-dropdown-portal');
        menu.parentNode.insertBefore(anchor, menu);
        document.body.appendChild(menu);
        menu.classList.add('k-dropdown__menu--portal');
        portalAnchors.set(menu, anchor);
        portalMenus.set(dropdown, menu);
    });

    document.addEventListener('hidden.bs.dropdown', (event) => {
        const trigger = event.target;
        const dropdown = trigger.closest(portalSelector);
        const menu = dropdown && portalMenus.get(dropdown);
        const anchor = menu && portalAnchors.get(menu);
        if (!anchor?.parentNode) return;

        anchor.parentNode.insertBefore(menu, anchor);
        anchor.remove();
        menu.classList.remove('k-dropdown__menu--portal');
        portalAnchors.delete(menu);
        portalMenus.delete(dropdown);
    });

    document.querySelectorAll('[data-k-dropdown]').forEach((dropdown) => {
        dropdown.addEventListener('keydown', (event) => {
            if (event.key === 'Escape') dropdown.querySelector('.k-dropdown__trigger')?.focus();
        });
    });
})();
