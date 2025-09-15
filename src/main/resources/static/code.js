
document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('getForm').addEventListener('submit', (e) => {
    e.preventDefault();
    const id = document.getElementById('userId').value.trim();
    loadGetMsg(id);
  });
});

function loadGetMsg(id) {
  id = String(id ?? '').trim();
  if (!id) { alert('Ingrese un ID'); return; }

  const url = `/api/users?id=${encodeURIComponent(id)}`;
  fetch(url, { method: 'GET', headers: { 'Accept': 'application/json' }})
    .then(r => { if (!r.ok) throw new Error(`Request failed (${r.status})`); return r.json(); })
    .then(user => renderUserCard(user))
    .catch(err => alert("Error: " + err.message));
}

function renderUserCard(user) {
  const el = document.getElementById("getrespmsg");
  el.innerHTML = `
    <div class="card shadow-sm">
      <div class="card-body">
        <h5 class="card-title mb-3">User Id: ${user.id}</h5>
        <ul class="list-group list-group-flush">
          <li class="list-group-item"><strong>Name:</strong> <span id="u-name"></span></li>
          <li class="list-group-item"><strong>Age:</strong> <span id="u-age"></span></li>
        </ul>
      </div>
    </div>`;
  document.getElementById("u-name").textContent = user.name ?? '';
  document.getElementById("u-age").textContent  = user.age ?? '';
}
