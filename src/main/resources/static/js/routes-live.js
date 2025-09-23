(function () {
  const tbody = document.getElementById("routes-tbody");

  function trSelector(id) {
    return `tr[data-id="${id}"]`;
  }

  function fmtDate(iso) {
    if (!iso) return "";
    // yyyy-MM-dd HH:mm
    return iso.replace("T", " ").substring(0, 16);
  }

  function rowHtml(r) {
    return `
      <tr data-id="${r.id}">
        <td>${r.id ?? ""}</td>
        <td>${r.name ?? ""}</td>
        <td>${r.coordX ?? ""}</td>
        <td>${r.coordY ?? ""}</td>
        <td>${r.fromName ?? "-"}</td>
        <td>${r.toName ?? ""}</td>
        <td>${r.distance ?? ""}</td>
        <td>${r.rating ?? ""}</td>
        <td>${fmtDate(r.creationDate)}</td>
        <td class="actions">
          <a class="button btn-secondary" href="/routes/${r.id}">View</a>
          <a class="button" href="/routes/${r.id}/edit">Edit</a>
          <form action="/routes/${r.id}/delete" method="post">
            <button type="submit" class="btn-danger"
              onclick="return confirm('Delete route #${r.id}?')">Delete</button>
          </form>
        </td>
      </tr>`;
  }

  function htmlToEl(html) {
    const t = document.createElement("template");
    t.innerHTML = html.trim();
    return t.content.firstElementChild;
  }

  function upsertRow(r) {
    const existing = tbody.querySelector(trSelector(r.id));
    const el = htmlToEl(rowHtml(r));
    if (existing) existing.replaceWith(el);
    else tbody.insertBefore(el, tbody.firstElementChild);
    const empty = document.getElementById("empty-row");
    if (empty) empty.remove();
  }

  function removeRow(id) {
    const existing = tbody.querySelector(trSelector(id));
    if (existing) existing.remove();
    if (
      !tbody.querySelector("tr[data-id]") &&
      !document.getElementById("empty-row")
    ) {
      const tr = document.createElement("tr");
      tr.id = "empty-row";
      tr.innerHTML = `<td colspan="10">No routes found</td>`;
      tbody.appendChild(tr);
    }
  }

  const es = new EventSource("/sse/routes");
  es.onmessage = (e) => {
    const msg = JSON.parse(e.data); // { action, route }
    if (!msg || !msg.action) return;
    if (msg.action === "CREATED" || msg.action === "UPDATED")
      upsertRow(msg.route);
    else if (msg.action === "DELETED") removeRow(msg.route && msg.route.id);
  };
  es.onerror = () => {
    /* browser will attempt to reconnect */
  };
})();
