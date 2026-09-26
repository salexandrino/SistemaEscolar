document.addEventListener('keydown', (event) => {
    if (event.key !== 'Escape') return;
    const dropdown = event.target.closest('[data-k-dropdown]');
    if (dropdown) dropdown.querySelector('.k-dropdown__trigger')?.focus();
});

document.addEventListener('show.bs.dropdown', (event) => {
    const wrapper = event.target.closest('.table-responsive');
    if (!wrapper) return;
    wrapper.dataset.prevOverflow = wrapper.style.overflow || '';
    wrapper.style.overflow = 'visible';
});

document.addEventListener('hide.bs.dropdown', (event) => {
    const wrapper = event.target.closest('.table-responsive');
    if (!wrapper) return;
    wrapper.style.overflow = wrapper.dataset.prevOverflow || '';
    delete wrapper.dataset.prevOverflow;
});
