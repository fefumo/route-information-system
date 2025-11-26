(function () {
  const btn = document.getElementById("btn-import-json");
  const input = document.getElementById("file-import-json");
  const form = document.getElementById("import-json-form");

  if (!btn || !input || !form) {
    return;
  }

  // По клику на кнопку открываем диалог выбора файла
  btn.addEventListener("click", () => {
    input.click();
  });

  // После выбора файла просто отправляем форму на /imports/upload
  input.addEventListener("change", () => {
    if (input.files && input.files.length > 0) {
      form.submit();
    }
  });
})();
