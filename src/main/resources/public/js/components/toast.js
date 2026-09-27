(function () {
    const icons = {
        success: 'bi-check-circle',
        error: 'bi-exclamation-circle',
        warning: 'bi-exclamation-triangle'
    };

    function container() {
        let element = document.querySelector('[data-toast-container]');
        if (!element) {
            element = document.createElement('div');
            element.className = 'k-toast-container';
            element.setAttribute('data-toast-container', '');
            element.setAttribute('aria-live', 'polite');
            document.body.appendChild(element);
        }
        return element;
    }

    function showToast(mensagem, tipo) {
        const toastType = ['success', 'error', 'warning'].includes(tipo) ? tipo : 'success';
        const toast = document.createElement('div');
        toast.className = `k-toast k-toast--${toastType}`;
        toast.setAttribute('role', 'status');

        const icon = document.createElement('i');
        icon.className = `bi ${icons[toastType]} k-toast__icon`;
        icon.setAttribute('aria-hidden', 'true');

        const text = document.createElement('p');
        text.className = 'k-toast__message';
        text.textContent = mensagem || 'Ação concluída.';

        const close = document.createElement('button');
        close.className = 'k-toast__close';
        close.type = 'button';
        close.setAttribute('aria-label', 'Fechar notificação');
        close.innerHTML = '<i class="bi bi-x-lg" aria-hidden="true"></i>';
        close.addEventListener('click', () => toast.remove());

        toast.append(icon, text, close);
        container().appendChild(toast);
        window.setTimeout(() => toast.remove(), 5200);
    }

    window.showToast = showToast;
})();

