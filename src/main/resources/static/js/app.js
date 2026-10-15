// ControllerAssist - Frontend Application Logic

const API_BASE = '/api';

// Elementos DOM principales
let employeesCache = [];

document.addEventListener('DOMContentLoaded', () => {
    initClock();
    loadDashboardSummary();
    loadEmployees();
    loadTodayAttendances();
    setupEventListeners();
});

// Reloj digital en vivo
function initClock() {
    const clockEl = document.getElementById('liveClock');
    const dateEl = document.getElementById('liveDate');

    function update() {
        const now = new Date();
        clockEl.textContent = now.toLocaleTimeString('es-CL', { hour12: false });
        dateEl.textContent = now.toLocaleDateString('es-CL', {
            weekday: 'long',
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    }
    update();
    setInterval(update, 1000);
}

// Configuración de listeners
function setupEventListeners() {
    // Punch Form (Automático)
    document.getElementById('punchForm').addEventListener('submit', (e) => {
        e.preventDefault();
        const code = document.getElementById('punchCodeInput').value.trim();
        const notes = document.getElementById('punchNotesInput').value.trim();
        submitPunch(code, null, notes);
    });

    // Botones explícitos de Entrada y Salida
    document.getElementById('btnExplicitCheckIn').addEventListener('click', () => {
        const code = document.getElementById('punchCodeInput').value.trim();
        const notes = document.getElementById('punchNotesInput').value.trim();
        if (!code) {
            showAlert('warning', 'Atención', 'Por favor ingresa o selecciona un código de empleado');
            return;
        }
        submitPunch(code, 'CHECK_IN', notes);
    });

    document.getElementById('btnExplicitCheckOut').addEventListener('click', () => {
        const code = document.getElementById('punchCodeInput').value.trim();
        const notes = document.getElementById('punchNotesInput').value.trim();
        if (!code) {
            showAlert('warning', 'Atención', 'Por favor ingresa o selecciona un código de empleado');
            return;
        }
        submitPunch(code, 'CHECK_OUT', notes);
    });

    // Selector rápido de empleados en el kiosco
    document.getElementById('punchEmployeeSelect').addEventListener('change', (e) => {
        if (e.target.value) {
            document.getElementById('punchCodeInput').value = e.target.value;
        }
    });

    // Botón refrescar hoy
    document.getElementById('btnRefreshToday').addEventListener('click', () => {
        loadTodayAttendances();
        loadDashboardSummary();
    });

    // Formulario de filtros de historial
    document.getElementById('filterForm').addEventListener('submit', (e) => {
        e.preventDefault();
        loadHistoryAttendances();
    });

    document.getElementById('btnResetFilters').addEventListener('click', () => {
        document.getElementById('filterStartDate').value = '';
        document.getElementById('filterEndDate').value = '';
        document.getElementById('filterEmployeeSelect').value = '';
        document.getElementById('filterStatusSelect').value = '';
        loadHistoryAttendances();
    });

    // Buscador en vivo de empleados
    document.getElementById('employeeSearchInput').addEventListener('input', (e) => {
        renderEmployeesTable(e.target.value);
    });

    // Botón Nuevo Empleado (limpiar modal)
    document.getElementById('btnNewEmployee').addEventListener('click', () => {
        document.getElementById('employeeModalLabel').textContent = 'Nuevo Empleado';
        document.getElementById('employeeForm').reset();
        document.getElementById('empIdInput').value = '';
        document.getElementById('empActiveInput').checked = true;
    });

    // Guardar Empleado (Crear o Editar)
    document.getElementById('employeeForm').addEventListener('submit', handleSaveEmployee);

    // Actualizar datos al cambiar de pestaña
    document.querySelectorAll('button[data-bs-toggle="pill"]').forEach(tab => {
        tab.addEventListener('shown.bs.tab', (event) => {
            if (event.target.id === 'today-tab') loadTodayAttendances();
            if (event.target.id === 'history-tab') loadHistoryAttendances();
            if (event.target.id === 'employees-tab') loadEmployees();
            loadDashboardSummary();
        });
    });
}

// Cargar métricas del Dashboard
async function loadDashboardSummary() {
    try {
        const res = await fetch(`${API_BASE}/attendances/summary/today`);
        if (!res.ok) return;
        const data = await res.json();
        document.getElementById('statTotalEmployees').textContent = data.totalEmployees;
        document.getElementById('statPresentToday').textContent = data.presentToday;
        document.getElementById('statLateToday').textContent = data.lateToday;
        document.getElementById('statAbsentToday').textContent = data.absentToday;
    } catch (err) {
        console.error('Error cargando métricas:', err);
    }
}

// Cargar lista de empleados
async function loadEmployees() {
    try {
        const res = await fetch(`${API_BASE}/employees`);
        if (!res.ok) return;
        employeesCache = await res.json();

        // Actualizar selects
        populateEmployeeSelects();
        renderEmployeesTable();
    } catch (err) {
        console.error('Error cargando empleados:', err);
    }
}

function populateEmployeeSelects() {
    const kioskSelect = document.getElementById('punchEmployeeSelect');
    const filterSelect = document.getElementById('filterEmployeeSelect');

    const currentKioskVal = kioskSelect.value;
    const currentFilterVal = filterSelect.value;

    kioskSelect.innerHTML = '<option value="">-- Seleccionar empleado --</option>';
    filterSelect.innerHTML = '<option value="">-- Todos los empleados --</option>';

    employeesCache.forEach(emp => {
        if (emp.active) {
            const optKiosk = document.createElement('option');
            optKiosk.value = emp.code;
            optKiosk.textContent = `${emp.code} - ${emp.fullName} (${emp.department || 'General'})`;
            kioskSelect.appendChild(optKiosk);
        }

        const optFilter = document.createElement('option');
        optFilter.value = emp.id;
        optFilter.textContent = `${emp.fullName} (${emp.code})`;
        filterSelect.appendChild(optFilter);
    });

    kioskSelect.value = currentKioskVal;
    filterSelect.value = currentFilterVal;
}

// Renderizar tabla de empleados
function renderEmployeesTable(search = '') {
    const tbody = document.getElementById('employeesTableBody');
    const filtered = employeesCache.filter(emp => {
        if (!search) return true;
        const q = search.toLowerCase();
        return (emp.fullName && emp.fullName.toLowerCase().includes(q)) ||
               (emp.code && emp.code.toLowerCase().includes(q)) ||
               (emp.department && emp.department.toLowerCase().includes(q)) ||
               (emp.email && emp.email.toLowerCase().includes(q));
    });

    if (filtered.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-muted">No se encontraron empleados.</td></tr>';
        return;
    }

    tbody.innerHTML = filtered.map(emp => `
        <tr>
            <td><span class="badge bg-secondary font-monospace">${emp.code}</span></td>
            <td class="fw-semibold">${emp.fullName}</td>
            <td>${emp.email}</td>
            <td>${emp.department || '-'}</td>
            <td>${emp.position || '-'}</td>
            <td>
                <span class="badge ${emp.active ? 'bg-success' : 'bg-danger'}">
                    ${emp.active ? 'Activo' : 'Inactivo'}
                </span>
            </td>
            <td class="text-end">
                <button class="btn btn-outline-primary btn-sm me-1" onclick="openEditEmployee(${emp.id})">
                    <i class="bi bi-pencil"></i>
                </button>
                ${emp.active ? `
                <button class="btn btn-outline-danger btn-sm" onclick="deactivateEmployee(${emp.id})">
                    <i class="bi bi-person-x"></i>
                </button>` : ''}
            </td>
        </tr>
    `).join('');
}

// Registrar Marcación (Punch)
async function submitPunch(employeeCode, type = null, notes = '') {
    const payload = {
        employeeCode: employeeCode.trim().toUpperCase(),
        notes: notes || null
    };

    let url = `${API_BASE}/attendances/punch`;
    if (type === 'CHECK_IN') {
        url = `${API_BASE}/attendances/check-in`;
        payload.type = 'CHECK_IN';
    } else if (type === 'CHECK_OUT') {
        url = `${API_BASE}/attendances/check-out`;
        payload.type = 'CHECK_OUT';
    }

    try {
        const res = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await res.json();

        if (res.ok) {
            showAlert('success', '¡Marcación Exitosa!', data.message || `Registro completado para ${data.employeeName}`);
            document.getElementById('punchCodeInput').value = '';
            document.getElementById('punchNotesInput').value = '';
            document.getElementById('punchEmployeeSelect').value = '';
            loadDashboardSummary();
            loadTodayAttendances();
        } else {
            showAlert('danger', 'Error al Marcar', data.message || 'No fue posible registrar la asistencia.');
        }
    } catch (err) {
        console.error('Error enviando marcación:', err);
        showAlert('danger', 'Error de Conexión', 'No se pudo conectar con el servidor. Revisa si está activo.');
    }
}

// Cargar asistencias del día
async function loadTodayAttendances() {
    const tbody = document.getElementById('todayAttendanceTableBody');
    try {
        const res = await fetch(`${API_BASE}/attendances/today`);
        if (!res.ok) return;
        const records = await res.json();

        if (records.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-muted">Aún no hay asistencias registradas hoy.</td></tr>';
            return;
        }

        tbody.innerHTML = records.map(r => `
            <tr>
                <td><span class="badge bg-secondary font-monospace">${r.employeeCode}</span></td>
                <td class="fw-semibold">${r.employeeName}</td>
                <td>${r.department || '-'}</td>
                <td><span class="text-success fw-bold">${formatTime(r.checkInTime)}</span></td>
                <td>${r.checkOutTime ? `<span class="text-danger fw-bold">${formatTime(r.checkOutTime)}</span>` : '<span class="badge bg-warning text-dark">Pendiente</span>'}</td>
                <td>${getStatusBadge(r.status, r.statusDescription)}</td>
                <td class="small text-muted">${r.notes || '-'}</td>
            </tr>
        `).join('');
    } catch (err) {
        console.error('Error cargando asistencias de hoy:', err);
    }
}

// Cargar historial de asistencias con filtros
async function loadHistoryAttendances() {
    const tbody = document.getElementById('historyTableBody');
    tbody.innerHTML = '<tr><td colspan="8" class="text-center py-4 text-muted">Buscando...</td></tr>';

    const startDate = document.getElementById('filterStartDate').value;
    const endDate = document.getElementById('filterEndDate').value;
    const employeeId = document.getElementById('filterEmployeeSelect').value;
    const status = document.getElementById('filterStatusSelect').value;

    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    if (employeeId) params.append('employeeId', employeeId);
    if (status) params.append('status', status);

    try {
        const res = await fetch(`${API_BASE}/attendances?${params.toString()}`);
        if (!res.ok) return;
        const records = await res.json();

        if (records.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center py-4 text-muted">No se encontraron registros con los filtros seleccionados.</td></tr>';
            return;
        }

        tbody.innerHTML = records.map(r => `
            <tr>
                <td><span class="fw-semibold">${r.attendanceDate}</span></td>
                <td><span class="badge bg-secondary font-monospace">${r.employeeCode}</span></td>
                <td class="fw-semibold">${r.employeeName}</td>
                <td>${r.department || '-'}</td>
                <td><span class="text-success fw-bold">${formatTime(r.checkInTime)}</span></td>
                <td>${r.checkOutTime ? `<span class="text-danger fw-bold">${formatTime(r.checkOutTime)}</span>` : '<span class="badge bg-warning text-dark">Pendiente</span>'}</td>
                <td>${getStatusBadge(r.status, r.statusDescription)}</td>
                <td class="small text-muted">${r.notes || '-'}</td>
            </tr>
        `).join('');
    } catch (err) {
        console.error('Error cargando historial:', err);
    }
}

// Guardar o Actualizar Empleado
async function handleSaveEmployee(e) {
    e.preventDefault();
    const id = document.getElementById('empIdInput').value;
    const payload = {
        code: document.getElementById('empCodeInput').value.trim(),
        firstName: document.getElementById('empFirstNameInput').value.trim(),
        lastName: document.getElementById('empLastNameInput').value.trim(),
        email: document.getElementById('empEmailInput').value.trim(),
        department: document.getElementById('empDepartmentInput').value.trim(),
        position: document.getElementById('empPositionInput').value.trim(),
        active: document.getElementById('empActiveInput').checked
    };

    const isEdit = !!id;
    const url = isEdit ? `${API_BASE}/employees/${id}` : `${API_BASE}/employees`;
    const method = isEdit ? 'PUT' : 'POST';

    try {
        const res = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await res.json();

        if (res.ok) {
            bootstrap.Modal.getInstance(document.getElementById('employeeModal')).hide();
            loadEmployees();
            loadDashboardSummary();
            alert(`Empleado ${isEdit ? 'actualizado' : 'creado'} con éxito.`);
        } else {
            alert(`Error: ${data.message || 'No fue posible guardar el empleado.'}`);
        }
    } catch (err) {
        console.error('Error guardando empleado:', err);
        alert('Error de red al guardar el empleado.');
    }
}

// Abrir modal en modo edición
window.openEditEmployee = function(id) {
    const emp = employeesCache.find(e => e.id === id);
    if (!emp) return;

    document.getElementById('employeeModalLabel').textContent = 'Editar Empleado';
    document.getElementById('empIdInput').value = emp.id;
    document.getElementById('empCodeInput').value = emp.code;
    document.getElementById('empFirstNameInput').value = emp.firstName;
    document.getElementById('empLastNameInput').value = emp.lastName;
    document.getElementById('empEmailInput').value = emp.email;
    document.getElementById('empDepartmentInput').value = emp.department || '';
    document.getElementById('empPositionInput').value = emp.position || '';
    document.getElementById('empActiveInput').checked = emp.active;

    new bootstrap.Modal(document.getElementById('employeeModal')).show();
};

// Desactivar empleado
window.deactivateEmployee = async function(id) {
    if (!confirm('¿Estás seguro de desactivar este empleado?')) return;
    try {
        const res = await fetch(`${API_BASE}/employees/${id}`, { method: 'DELETE' });
        if (res.ok) {
            loadEmployees();
            loadDashboardSummary();
        } else {
            alert('No se pudo desactivar el empleado.');
        }
    } catch (err) {
        console.error('Error desactivando empleado:', err);
    }
};

// Utilidades UI
function showAlert(type, title, message) {
    const alertBox = document.getElementById('punchAlert');
    const alertTitle = document.getElementById('punchAlertTitle');
    const alertText = document.getElementById('punchAlertText');
    const alertIcon = document.getElementById('punchAlertIcon');

    alertBox.className = `alert alert-${type} mt-4 text-start shadow-sm`;
    alertTitle.textContent = title;
    alertText.textContent = message;

    if (type === 'success') {
        alertIcon.className = 'bi bi-check-circle-fill text-success fs-3 me-3';
    } else if (type === 'warning') {
        alertIcon.className = 'bi bi-exclamation-triangle-fill text-warning fs-3 me-3';
    } else {
        alertIcon.className = 'bi bi-x-circle-fill text-danger fs-3 me-3';
    }

    alertBox.classList.remove('d-none');
}

function formatTime(isoDateTime) {
    if (!isoDateTime) return '-';
    const parts = isoDateTime.split('T');
    if (parts.length > 1) {
        return parts[1].substring(0, 8);
    }
    return isoDateTime;
}

function getStatusBadge(status, description) {
    switch (status) {
        case 'ON_TIME':
            return `<span class="badge bg-success">${description || 'A tiempo'}</span>`;
        case 'LATE':
            return `<span class="badge bg-warning text-dark">${description || 'Tardanza'}</span>`;
        case 'EARLY_EXIT':
            return `<span class="badge bg-info text-dark">${description || 'Salida anticipada'}</span>`;
        default:
            return `<span class="badge bg-secondary">${description || 'Normal'}</span>`;
    }
}
