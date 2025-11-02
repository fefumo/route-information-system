async function readErrorBody(res) {
  const ct = res.headers.get("content-type") || "";
  if (ct.includes("application/json")) {
    try {
      return await res.json();
    } catch {}
  }
  try {
    return await res.text();
  } catch {
    return "";
  }
}

(function () {
  const btn = document.getElementById("btn-import-json");
  const input = document.getElementById("file-import-json");
  const flashBox = document.querySelector(".flash");

  function showFlash(msg, isErr) {
    if (!flashBox) {
      alert(msg);
      return;
    }
    const ok = document.createElement("div");
    ok.className = "msg " + (isErr ? "err" : "ok");
    ok.textContent = msg;
    flashBox.innerHTML = "";
    flashBox.appendChild(ok);
    setTimeout(() => {
      ok.remove();
    }, 7000);
  }

  function getCsrfHeader() {
    const token = document
      .querySelector('meta[name="_csrf"]')
      ?.getAttribute("content");
    const header = document
      .querySelector('meta[name="_csrf_header"]')
      ?.getAttribute("content");
    return { header, token };
  }

  // guard input existence inside the handler
  btn?.addEventListener("click", () => {
    if (input) input.click();
  });

  input?.addEventListener("change", async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    let payload;
    try {
      const text = await file.text();
      payload = JSON.parse(text);
      if (!Array.isArray(payload))
        throw new Error("JSON must be an array of RouteImportDto items.");
    } catch (err) {
      showFlash("Invalid JSON: " + err.message, true);
      input.value = "";
      return;
    }

    const { header, token } = getCsrfHeader();
    const headers = { "Content-Type": "application/json" };
    if (header && token) headers[header] = token;

    try {
      const res = await fetch("/routes/import", {
        method: "POST",
        headers,
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        const body = await readErrorBody(res);
        let msg = `Import failed: ${res.status} ${res.statusText}`;
        if (body && typeof body === "object") {
          const parts = [];
          if (body.message) parts.push(body.message);
          if (Array.isArray(body.errors) && body.errors.length) {
            const first = body.errors
              .slice(0, 3)
              .map((e) =>
                e.field
                  ? `${e.field}: ${e.message}`
                  : `${e.path || "item"}: ${e.message}`,
              )
              .join(" | ");
            parts.push(first);
          }
          if (parts.length) msg = parts.join(" — ");
        } else if (typeof body === "string" && body.trim()) {
          msg = body.trim().slice(0, 500);
        }
        showFlash(msg, true);
        input.value = "";
        return; // do NOT reload on failure
      }

      const report = await res.json();
      const okCount = (report.imported || []).length;
      const errCount = (report.errors || []).length;
      const msg = `Import done: ${okCount}/${report.received} imported${errCount ? `, ${errCount} errors.` : "."}`;
      const firstErrors = (report.errors || []).slice(0, 3).join(" | ");
      showFlash(errCount ? `${msg} ${firstErrors}` : msg, errCount > 0);

      window.location.reload(); // success only
    } catch (err) {
      showFlash("Import error: " + err.message, true);
    } finally {
      input.value = "";
    }
  });
})();
