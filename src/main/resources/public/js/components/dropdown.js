// Action dropdowns use Bootstrap's accessible dropdown behavior when available.
document.querySelectorAll('[data-k-dropdown]').forEach((dropdown) => {
    dropdown.addEventListener('keydown', (event) => {
        if (event.key === 'Escape') dropdown.querySelector('.k-dropdown__trigger')?.focus();
    });
});
