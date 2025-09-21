(function () {
  if (!window.EventSource) return;
  try {
    const es = new EventSource('/api/stream/routes');
    es.addEventListener('route', () => {
      window.location.reload();
    });
  } catch (_) {}
})();
