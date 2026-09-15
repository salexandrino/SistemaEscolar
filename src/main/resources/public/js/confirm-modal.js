(function () {
    function confirmarAcao(mensagem, titulo, rotulo) {
        const modalElement = document.getElementById('deleteConfirmationModal');
        if (!modalElement || !window.bootstrap) {
            return Promise.resolve(window.confirm(mensagem));
        }

        const modal = bootstrap.Modal.getOrCreateInstance(modalElement);
        const body = modalElement.querySelector('.modal-body');
        const title = modalElement.querySelector('.modal-title');
        const confirmButton = document.getElementById('confirmDeleteButton');

        body.textContent = mensagem || 'Tem certeza de que deseja continuar?';
        title.textContent = titulo || 'Confirmar ação';
        confirmButton.textContent = rotulo || 'Confirmar';

        return new Promise(function (resolve) {
            let finalizado = false;
            function concluir(resultado) {
                if (finalizado) return;
                finalizado = true;
                confirmButton.removeEventListener('click', confirmar);
                modalElement.removeEventListener('hidden.bs.modal', cancelar);
                modal.hide();
                resolve(resultado);
            }
            function confirmar() { concluir(true); }
            function cancelar() { concluir(false); }

            confirmButton.addEventListener('click', confirmar);
            modalElement.addEventListener('hidden.bs.modal', cancelar);
            modal.show();
        });
    }

    window.confirmarAcao = confirmarAcao;
})();
