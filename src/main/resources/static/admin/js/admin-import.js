'use strict';
(function () {
    const form = document.getElementById('importForm');
    const fileInput = document.getElementById('osmFile');
    const label = document.getElementById('chosenFile');
    const button = document.getElementById('importButton');
    const progress = document.getElementById('importProgress');
    if (!form || !fileInput || !label || !button || !progress) return;

    fileInput.addEventListener('change', function () {
        const file = fileInput.files && fileInput.files[0];
        label.textContent = file
            ? file.name + ' (' + (file.size / 1024 / 1024).toFixed(2) + ' МБ)'
            : 'Файл не выбран';
    });

    form.addEventListener('submit', function () {
        if (!form.checkValidity()) return;
        button.disabled = true;
        button.textContent = 'Импорт выполняется…';
        progress.hidden = false;
    });
})();
