(() => {
  const date = document.querySelector('#today-label');
  if (date) date.textContent = new Intl.DateTimeFormat(undefined, { weekday: 'short', month: 'short', day: 'numeric' }).format(new Date());

  const search = document.querySelector('#table-search');
  if (search) search.addEventListener('input', () => {
    const query = search.value.trim().toLowerCase();
    document.querySelectorAll('tbody tr').forEach((row) => {
      row.hidden = !row.querySelector('.empty-state') && !row.textContent.toLowerCase().includes(query);
    });
  });

  const csrf = document.querySelector('meta[name="_csrf"]')?.content;
  const sendJson = async (url, payload = {}) => {
    const response = await fetch(url, { method: 'POST', headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': csrf }, body: JSON.stringify(payload) });
    if (!response.ok) {
      const body = await response.json().catch(() => ({}));
      throw new Error(body.detail || body.message || 'The action could not be completed.');
    }
    return response.json();
  };

  document.querySelectorAll('.generate-plan').forEach((button) => button.addEventListener('click', async () => {
    button.disabled = true;
    try {
      const plan = await sendJson(`/api/v1/exams/${button.dataset.examId}/seating-plan/generate`);
      window.location.assign(`/workspace/plans/${plan.id}`);
    } catch (error) {
      button.disabled = false;
      window.alert(error.message);
    }
  }));

  const regeneratePlan = document.querySelector('.regenerate-plan');
  if (regeneratePlan) regeneratePlan.addEventListener('click', async () => {
    regeneratePlan.disabled = true;
    try {
      await sendJson(`/api/v1/exams/${document.body.dataset.planExamId}/seating-plan/generate`);
      window.location.reload();
    } catch (error) {
      document.querySelector('#plan-alert').textContent = error.message;
      regeneratePlan.disabled = false;
    }
  });

  document.querySelectorAll('.paper-transition').forEach((button) => button.addEventListener('click', async () => {
    button.disabled = true;
    try {
      await sendJson(`/api/v1/papers/${button.dataset.runId}/transition`, { status: button.dataset.next });
      window.location.reload();
    } catch (error) {
      window.alert(error.message);
      button.disabled = false;
    }
  }));

  const addCandidate = document.querySelector('#candidate-add');
  if (addCandidate) addCandidate.addEventListener('click', async () => {
    const select = document.querySelector('#candidate-select');
    const message = document.querySelector('#candidate-error');
    if (!select.value) { message.textContent = 'Choose a student first.'; return; }
    addCandidate.disabled = true;
    try {
      await sendJson(`/api/v1/exams/${window.location.pathname.split('/')[3]}/candidates`, { studentId: select.value });
      window.location.reload();
    } catch (error) { message.textContent = error.message; addCandidate.disabled = false; }
  });

  const generateRoster = document.querySelector('#roster-generate');
  if (generateRoster) generateRoster.addEventListener('click', async () => {
    generateRoster.disabled = true;
    try {
      const plan = await sendJson(`/api/v1/exams/${generateRoster.dataset.examId}/seating-plan/generate`);
      window.location.assign(`/workspace/plans/${plan.id}`);
    } catch (error) {
      document.querySelector('#candidate-error').textContent = error.message;
      generateRoster.disabled = false;
    }
  });

  const transition = document.querySelector('.plan-transition');
  if (transition) transition.addEventListener('click', async () => {
    transition.disabled = true;
    try {
      await sendJson(`/api/v1/seating-plans/${document.body.dataset.planId}/transition`, { status: transition.dataset.next });
      window.location.reload();
    } catch (error) { document.querySelector('#plan-alert').textContent = error.message; transition.disabled = false; }
  });

  const seatMap = document.querySelector('.seat-map');
  if (seatMap && document.body.dataset.planStatus !== 'LOCKED') {
    let selected = null;
    seatMap.addEventListener('click', async (event) => {
      const seat = event.target.closest('.seat-cell');
      if (!seat || seat.disabled) return;
      const alert = document.querySelector('#plan-alert');
      alert.textContent = '';
      if (seat.dataset.assignmentId) {
        seatMap.querySelectorAll('.seat-selected').forEach((item) => item.classList.remove('seat-selected'));
        selected = seat;
        seat.classList.add('seat-selected');
        return;
      }
      if (!selected) { alert.textContent = 'Select a student seat before choosing an empty seat.'; return; }
      try {
        await sendJson(`/api/v1/seating-plans/${document.body.dataset.planId}/move`, {
          assignmentId: Number(selected.dataset.assignmentId), row: Number(seat.dataset.row), column: Number(seat.dataset.column)
        });
        window.location.reload();
      } catch (error) { alert.textContent = error.message; }
    });
  }

  const importButton = document.querySelector('#import-students');
  const csvInput = document.querySelector('#student-csv');
  const importReport = document.querySelector('#import-report');
  const showImportReport = (title, errors = []) => {
    if (!importReport) { window.alert(title); return; }
    importReport.replaceChildren();
    const summary = document.createElement('p');
    summary.textContent = title;
    importReport.append(summary);
    if (errors.length) {
      const list = document.createElement('ul');
      errors.forEach((error) => {
        const item = document.createElement('li');
        item.textContent = `Row ${error.row}: ${error.message}`;
        list.append(item);
      });
      importReport.append(list);
    }
    if (title.startsWith('Imported ')) {
      const refresh = document.createElement('button');
      refresh.className = 'button button-quiet';
      refresh.textContent = 'Refresh student list';
      refresh.addEventListener('click', () => window.location.reload());
      importReport.append(refresh);
    }
    importReport.hidden = false;
    importReport.scrollIntoView({ behavior: 'smooth', block: 'center' });
  };
  if (importButton && csvInput) {
    importButton.addEventListener('click', () => csvInput.click());
    csvInput.addEventListener('change', async () => {
      const file = csvInput.files?.[0];
      if (!file) return;
      importButton.disabled = true;
      try {
        if (file.size > 2 * 1024 * 1024) throw new Error('CSV files must be 2 MB or smaller.');
        const response = await fetch('/api/v1/students/import', {
          method: 'POST',
          headers: { 'Content-Type': 'text/plain;charset=UTF-8', 'X-CSRF-TOKEN': csrf },
          body: await file.text()
        });
        const body = await response.json().catch(() => ({}));
        if (!response.ok) throw new Error(body.detail || body.message || 'The CSV could not be imported.');
        showImportReport(`Imported ${body.imported} student${body.imported === 1 ? '' : 's'}; ${body.rejected} row${body.rejected === 1 ? '' : 's'} rejected.`, body.errors || []);
      } catch (error) { showImportReport(error.message || 'A network error interrupted the import.'); }
      finally { importButton.disabled = false; csvInput.value = ''; }
    });
  }

  const dialog = document.querySelector('#create-dialog');
  const opener = document.querySelector('#open-create');
  const form = document.querySelector('#create-form');
  if (!dialog || !opener || !form) return;

  const page = document.body.dataset.page;
  const endpoint = page === 'team' ? '/api/v1/users' : `/api/v1/${page}`;
  const fields = { students: 'student-fields', rooms: 'room-fields', exams: 'exam-fields', team: 'user-fields' };
  const titles = { students: 'Add a student', rooms: 'Add an examination room', exams: 'Plan an examination', team: 'Invite a teammate' };
  opener.addEventListener('click', () => {
    Object.entries(fields).forEach(([key, id]) => {
      const container = document.getElementById(id);
      if (!container) return;
      container.hidden = key !== page;
      container.querySelectorAll('input,select').forEach((input) => { input.disabled = key !== page; });
    });
    document.getElementById('dialog-title').textContent = titles[page];
    document.getElementById('form-error').textContent = '';
    dialog.showModal();
  });
  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const payload = Object.fromEntries(new FormData(form).entries());
    ['semester', 'rows', 'columns', 'capacity', 'expectedStudents', 'roomId'].forEach((key) => {
      if (key in payload && payload[key] !== '') payload[key] = Number(payload[key]);
    });
    try {
      const response = await fetch(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content },
        body: JSON.stringify(payload)
      });
      if (!response.ok) {
        const body = await response.json().catch(() => ({}));
        const detail = body.detail || body.message || Object.values(body.errors || {}).map((error) => error.defaultMessage).join(' ');
        throw new Error(detail || 'Could not save this record. Check the fields and try again.');
      }
      dialog.close();
      window.location.reload();
    } catch (error) {
      document.getElementById('form-error').textContent = error.message || 'A network error interrupted the save.';
    }
  });

  document.querySelectorAll('.user-toggle').forEach((button) => button.addEventListener('click', async () => {
    button.disabled = true;
    try {
      const response = await fetch(`/api/v1/users/${button.dataset.userId}/active`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': csrf },
        body: JSON.stringify({ active: button.dataset.active === 'true' })
      });
      if (!response.ok) {
        const body = await response.json().catch(() => ({}));
        throw new Error(body.detail || body.message || 'Could not update this account.');
      }
      window.location.reload();
    } catch (error) {
      document.querySelector('#team-message').textContent = error.message;
      button.disabled = false;
    }
  }));
})();
