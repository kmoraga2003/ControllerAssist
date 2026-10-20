// ControllerAssist - Lógica Frontend (Bootstrap 5)
// Sistema de Control de Asistencias - Empresa de Productos Químicos

const API_BASE = '/api';
let currentUser = null;
let adminUsersCache = [];

document.addEventListener('DOMContentLoaded', () => {
    initClock();
    initDateInputs();
    setupEventListeners();
    checkExistingSession();
});

// Reloj digital corporativo en la barra superior
function initClock() {
    const clockEl = document.getElementById('navLiveClock');
    const dateEl = document.getElementById('navLiveDate');

    function tick() {
        const now = new Date();
        clockEl.textContent = now.toLocaleTimeString('es-CL', { hour12: false });
        dateEl.textContent = now.toLocaleDateString('es-CL', {
            weekday: 'short',
            day: 'numeric',
            month: 'short',
            year: 'numeric'
        });
    }
    tick();
    setInterval(tick, 1000);
}

// Configurar fechas por defecto para reportes (hoy)
function initDateInputs() {
    const todayStr = new Date().toISOString().split('T')[0];
    const startDateEl = document.getElementById('reportStartDate');
    const endDateEl = document.getElementById('reportEndDate');
    if (startDateEl) startDateEl.value = todayStr;
    if (endDateEl) endDateEl.value = todayStr;

    const reportTypeSelect = document.getElementById('reportTypeSelect');
    if (reportTypeSelect) {
        reportTypeSelect.addEventListener('change', (e) => {
            const isAbsence = e.target.value === 'RE-03';
            const divEnd = document.getElementById('divEndDate');
            const lblStart = document.querySelector('#divStartDate label');
            if (isAbsence) {
                divEnd.classList.add('d-none');
                lblStart.textContent = 'Fecha de Consulta';
            } else {
                divEnd.classList.remove('d-none');
                lblStart.textContent = 'Fecha Inicial';
            }
        });
    }
}

// Event Listeners principales
function setupEventListeners() {
    // Formulario de Login (CA-01)
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }

    // Botón Cerrar Sesión
    const btnLogout = document.getElementById('btnLogout');
    if (btnLogout) {
        btnLogout.addEventListener('click', logout);
    }

    // Botón Marcación Empleado (CA-01)
    const btnEmpPunch = document.getElementById('btnEmpPunch');
    if (btnEmpPunch) {
        btnEmpPunch.addEventListener('click', handleEmployeePunch);
    }

    // Modal Usuario (GU-01, GU-02)
    const userForm = document.getElementById('userForm');
    if (userForm) {
        userForm.addEventListener('submit', handleSaveUser);
    }
}

// ==============================================================
// GESTIÓN DE SESIÓN Y VISTAS
// ==============================================================

function checkExistingSession() {
    const saved = sessionStorage.getItem('controller_user');
    if (saved) {
        try {
            currentUser = JSON.parse(saved);
            routeSession(currentUser);
            return;
        } catch (e) {
            sessionStorage.removeItem('controller_user');
        }
    }
    showLoginView();
}

function routeSession(user) {
    currentUser = user;
    sessionStorage.setItem('controller_user', JSON.stringify(user));

    // Actualizar navbar
    document.getElementById('navUserSession').classList.remove('d-none');
    document.getElementById('navUserName').textContent = user.fullName;
    document.getElementById('navUserRole').textContent = user.role;

    // Enrutar según rol
    if (user.role === 'ADMIN') {
        showAdminView();
    } else {
        showEmployeeView();
    }
}

function showLoginView() {
    currentUser = null;
    sessionStorage.removeItem('controller_user');

    document.getElementById('navUserSession').classList.add('d-none');
    document.getElementById('viewLogin').classList.remove('d-none');
    document.getElementById('viewEmployee').classList.add('d-none');
    document.getElementById('viewAdmin').classList.add('d-none');

    const alertBox = document.getElementById('loginAlert');
    if (alertBox) alertBox.classList.add('d-none');
}

function showEmployeeView() {
    document.getElementById('viewLogin').classList.add('d-none');
    document.getElementById('viewAdmin').classList.add('d-none');
    document.getElementById('viewEmployee').classList.remove('d-none');

    document.getElementById('empGreetingName').textContent = currentUser.fullName;
    document.getElementById('empRoleInfo').textContent = `${currentUser.position || 'Colaborador'} — ${currentUser.department || 'Planta'}`;
    document.getElementById('empCodeBadge').textContent = currentUser.code;
    document.getElementById('empTodayDate').textContent = new Date().toLocaleDateString('es-CL', {
        weekday: 'long',
        day: 'numeric',
        month: 'long',
        year: 'numeric'
    });

    loadEmployeeTodayStatus();
}

function showAdminView() {
    document.getElementById('viewLogin').classList.add('d-none');
    document.getElementById('viewEmployee').classList.add('d-none');
    document.getElementById('viewAdmin').classList.remove('d-none');

    loadAdminDashboard();
    loadAdminEmployees();
}

window.logout = function() {
    showLoginView();
};

window.fillDemoLogin = function(email, password) {
    document.getElementById('loginEmail').value = email;
    document.getElementById('loginPassword').value = password;
};

// ==============================================================
// AUTENTICACIÓN (CA-01)
// ==============================================================

async function handleLogin(e) {
    e.preventDefault();
    const email = document.getElementById('loginEmail').value.trim();
    const password = document.getElementById('loginPassword').value;
    const alertBox = document.getElementById('loginAlert');
    const submitBtn = document.getElementById('btnLoginSubmit');

    alertBox.classList.add('d-none');
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Validando...';

    try {
        const res = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        const data = await res.json();

        if (res.ok) {
            routeSession(data);
        } else {
            alertBox.textContent = data.message || 'Credenciales inválidas. Revisa correo y contraseña.';
            alertBox.classList.remove('d-none');
        }
    } catch (err) {
        console.error('Error de login:', err);
        alertBox.textContent = 'No se pudo conectar con el servidor. Revisa si está iniciado.';
        alertBox.classList.remove('d-none');
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="bi bi-box-arrow-in-right me-1"></i> Iniciar Sesión';
    }
}

// ==============================================================
// FLUJO DE EMPLEADO (CA-01: Marcación Entrada/Salida)
// ==============================================================

async function loadEmployeeTodayStatus() {
    if (!currentUser) return;

    const inEl = document.getElementById('empCheckInDisplay');
    const outEl = document.getElementById('empCheckOutDisplay');
    const hoursEl = document.getElementById('empWorkedHoursDisplay');
    const btn = document.getElementById('btnEmpPunch');
    const btnText = document.getElementById('btnEmpPunchText');

    try {
        const res = await fetch(`${API_BASE}/attendances/my-today/${currentUser.id}`);

        if (res.status === 204 || !res.ok) {
            // Sin registro aún hoy -> Corresponde ENTRADA
            inEl.textContent = '--:--:--';
            outEl.textContent = '--:--:--';
            hoursEl.textContent = 'Sin iniciar';
            btn.className = 'btn btn-success btn-lg fw-bold py-3 shadow-sm';
            btnText.textContent = 'Marcar Entrada';
            btn.disabled = false;
            return;
        }

        const data = await res.json();
        inEl.textContent = formatIsoTime(data.checkInTime);
        outEl.textContent = formatIsoTime(data.checkOutTime);
        hoursEl.textContent = data.workedHoursFormatted || '-';

        if (data.checkInTime && !data.checkOutTime) {
            // Ya marcó entrada, falta salida -> Corresponde SALIDA
            btn.className = 'btn btn-primary btn-lg fw-bold py-3 shadow-sm';
            btnText.textContent = 'Marcar Salida';
            btn.disabled = false;
        } else if (data.checkInTime && data.checkOutTime) {
            // Ambos completados -> Jornada terminada
            btn.className = 'btn btn-secondary btn-lg fw-bold py-3 shadow-sm';
            btnText.textContent = 'Jornada Finalizada Hoy';
            btn.disabled = true;
        }
    } catch (err) {
        console.error('Error cargando estado de hoy:', err);
    }
}

async function handleEmployeePunch() {
    if (!currentUser) return;

    const notes = document.getElementById('empPunchNotes').value.trim();
    const alertBox = document.getElementById('empPunchAlert');
    const btn = document.getElementById('btnEmpPunch');

    btn.disabled = true;

    try {
        const res = await fetch(`${API_BASE}/attendances/punch`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                employeeCode: currentUser.code,
                notes: notes || null
            })
        });

        const data = await res.json();

        if (res.ok) {
            alertBox.className = 'alert alert-success text-start small mb-0';
            alertBox.innerHTML = `<strong>¡Marcación registrada!</strong><br>${data.message || 'Operación completada con éxito.'}`;
            alertBox.classList.remove('d-none');
            document.getElementById('empPunchNotes').value = '';
            loadEmployeeTodayStatus();
        } else {
            alertBox.className = 'alert alert-danger text-start small mb-0';
            alertBox.innerHTML = `<strong>No fue posible marcar:</strong> ${data.message || 'Error en la solicitud.'}`;
            alertBox.classList.remove('d-none');
            btn.disabled = false;
        }
    } catch (err) {
        console.error('Error al marcar asistencia:', err);
        alertBox.className = 'alert alert-danger text-start small mb-0';
        alertBox.textContent = 'Error de conexión con el servidor.';
        alertBox.classList.remove('d-none');
        btn.disabled = false;
    }
}

// ==============================================================
// PANEL ADMINISTRADOR: CONTROL DIARIO
// ==============================================================

async function loadAdminDashboard() {
    // KPIs del día
    try {
        const resSummary = await fetch(`${API_BASE}/attendances/summary/today`);
        if (resSummary.ok) {
            const s = await resSummary.json();
            document.getElementById('statTotalEmployees').textContent = s.totalEmployees;
            document.getElementById('statPresentToday').textContent = s.presentToday;
            document.getElementById('statLateToday').textContent = s.lateToday;
            document.getElementById('statAbsentToday').textContent = s.absentToday;
        }
    } catch (err) {
        console.error('Error cargando KPIs:', err);
    }

    // Tabla de marcaciones de hoy
    const tbody = document.getElementById('todayAttendanceTableBody');
    try {
        const res = await fetch(`${API_BASE}/attendances/today`);
        if (!res.ok) return;
        const records = await res.json();

        if (records.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center py-4 text-muted">Aún no hay marcaciones registradas en el día de hoy.</td></tr>';
            return;
        }

        tbody.innerHTML = records.map(r => `
            <tr>
                <td><span class="badge bg-secondary font-monospace">${r.employeeCode}</span></td>
                <td class="fw-semibold">${r.employeeName}</td>
                <td>${r.department || '-'}</td>
                <td><span class="text-success fw-bold">${formatIsoTime(r.checkInTime)}</span></td>
                <td>${r.checkOutTime ? `<span class="text-danger fw-bold">${formatIsoTime(r.checkOutTime)}</span>` : '<span class="badge bg-warning text-dark">Pendiente</span>'}</td>
                <td><span class="badge bg-light text-dark border font-monospace">${r.workedHoursFormatted || '-'}</span></td>
                <td>${getStatusBadge(r.status, r.statusDescription)}</td>
                <td class="small text-muted">${r.notes || '-'}</td>
            </tr>
        `).join('');
    } catch (err) {
        console.error('Error cargando asistencias de hoy:', err);
    }
}

// ==============================================================
// PANEL ADMINISTRADOR: REPORTES (RE-01, RE-02, RE-03)
// ==============================================================

async function generateSelectedReport() {
    const reportType = document.getElementById('reportTypeSelect').value;
    const startDate = document.getElementById('reportStartDate').value;
    const endDate = document.getElementById('reportEndDate').value;
    const tbody = document.getElementById('reportTableBody');
    const titleEl = document.getElementById('reportResultTitle');
    const countEl = document.getElementById('reportResultCount');
    const timeHeader = document.getElementById('reportTimeColHeader');

    tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4 text-muted">Consultando datos del reporte...</td></tr>';

    let url = '';
    if (reportType === 'RE-01') {
        titleEl.textContent = 'RE-01: Reporte de Atrasos (Ingresos post 09:30 AM)';
        timeHeader.textContent = 'Hora Entrada';
        const params = new URLSearchParams();
        if (startDate) params.append('startDate', startDate);
        if (endDate) params.append('endDate', endDate);
        url = `${API_BASE}/reports/late?${params.toString()}`;
    } else if (reportType === 'RE-02') {
        titleEl.textContent = 'RE-02: Reporte de Salidas Anticipadas (Antes de 17:30 PM)';
        timeHeader.textContent = 'Hora Salida';
        const params = new URLSearchParams();
        if (startDate) params.append('startDate', startDate);
        if (endDate) params.append('endDate', endDate);
        url = `${API_BASE}/reports/early-exit?${params.toString()}`;
    } else if (reportType === 'RE-03') {
        titleEl.textContent = 'RE-03: Reporte de Inasistencias (Sin marcación en la fecha)';
        timeHeader.textContent = 'Hora';
        const params = new URLSearchParams();
        if (startDate) params.append('date', startDate);
        url = `${API_BASE}/reports/absences?${params.toString()}`;
    }

    try {
        const res = await fetch(url);
        if (!res.ok) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4 text-danger">Error al generar el reporte.</td></tr>';
            return;
        }

        const items = await res.json();
        countEl.textContent = `(${items.length} registro${items.length === 1 ? '' : 's'})`;

        if (items.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4 text-muted">No se encontraron registros para los criterios indicados.</td></tr>';
            return;
        }

        tbody.innerHTML = items.map(item => `
            <tr>
                <td class="fw-semibold">${item.date}</td>
                <td><span class="badge bg-secondary font-monospace">${item.employeeCode}</span></td>
                <td class="fw-semibold">${item.employeeName}</td>
                <td>${item.department || '-'}</td>
                <td class="font-monospace">${item.eventTime || '-'}</td>
                <td><span class="badge bg-warning-subtle text-dark border border-warning-subtle">${item.detail}</span></td>
            </tr>
        `).join('');

    } catch (err) {
        console.error('Error en reporte:', err);
        tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4 text-danger">Error de red al consultar el reporte.</td></tr>';
    }
}

// ==============================================================
// PANEL ADMINISTRADOR: GESTIÓN DE USUARIOS (GU-01, GU-02, GU-03, AP-01)
// ==============================================================

async function loadAdminEmployees() {
    const status = document.getElementById('userStatusFilter').value;
    const tbody = document.getElementById('usersTableBody');

    try {
        const res = await fetch(`${API_BASE}/employees?status=${encodeURIComponent(status)}`);
        if (!res.ok) return;
        adminUsersCache = await res.json();
        renderAdminUsersTable(adminUsersCache);
    } catch (err) {
        console.error('Error cargando empleados:', err);
        tbody.innerHTML = '<tr><td colspan="8" class="text-center py-4 text-danger">Error cargando usuarios.</td></tr>';
    }
}

function renderAdminUsersTable(users) {
    const tbody = document.getElementById('usersTableBody');

    if (users.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center py-4 text-muted">No se encontraron usuarios.</td></tr>';
        return;
    }

    tbody.innerHTML = users.map(u => `
        <tr>
            <td><span class="badge bg-secondary font-monospace">${u.code}</span></td>
            <td class="fw-semibold">${u.fullName}</td>
            <td>${u.email}</td>
            <td><span class="badge ${u.role === 'ADMIN' ? 'bg-dark' : 'bg-info text-dark'}">${u.role}</span></td>
            <td>${u.department || '-'}</td>
            <td>${u.position || '-'}</td>
            <td>
                <span class="badge ${u.active ? 'bg-success' : 'bg-danger'}">
                    ${u.active ? 'Activo' : 'Inactivo'}
                </span>
            </td>
            <td class="text-end">
                <button type="button" class="btn btn-outline-primary btn-sm me-1" onclick="openEditUserModal(${u.id})" title="Modificar Usuario (GU-02)">
                    <i class="bi bi-pencil"></i>
                </button>
                ${u.active ? `
                <button type="button" class="btn btn-outline-danger btn-sm" onclick="deleteUser(${u.id})" title="Desactivar / Eliminar Usuario (GU-03)">
                    <i class="bi bi-trash"></i>
                </button>` : ''}
            </td>
        </tr>
    `).join('');
}

function filterUsersTableLocal() {
    const q = document.getElementById('userSearchInput').value.toLowerCase().trim();
    if (!q) {
        renderAdminUsersTable(adminUsersCache);
        return;
    }

    const filtered = adminUsersCache.filter(u =>
        (u.fullName && u.fullName.toLowerCase().includes(q)) ||
        (u.code && u.code.toLowerCase().includes(q)) ||
        (u.email && u.email.toLowerCase().includes(q)) ||
        (u.department && u.department.toLowerCase().includes(q))
    );
    renderAdminUsersTable(filtered);
}

window.openNewUserModal = function() {
    document.getElementById('userModalLabel').textContent = 'Crear Nuevo Usuario (GU-01)';
    document.getElementById('userForm').reset();
    document.getElementById('modalUserId').value = '';
    document.getElementById('modalUserActive').checked = true;
    document.getElementById('modalUserPassword').required = true;
    new bootstrap.Modal(document.getElementById('userModal')).show();
};

window.openEditUserModal = function(id) {
    const user = adminUsersCache.find(u => u.id === id);
    if (!user) return;

    document.getElementById('userModalLabel').textContent = 'Modificar Usuario (GU-02)';
    document.getElementById('modalUserId').value = user.id;
    document.getElementById('modalUserCode').value = user.code;
    document.getElementById('modalUserRole').value = user.role || 'EMPLEADO';
    document.getElementById('modalUserFirstName').value = user.firstName;
    document.getElementById('modalUserLastName').value = user.lastName;
    document.getElementById('modalUserEmail').value = user.email;
    document.getElementById('modalUserPassword').value = '';
    document.getElementById('modalUserPassword').required = false;
    document.getElementById('modalUserDepartment').value = user.department || '';
    document.getElementById('modalUserPosition').value = user.position || '';
    document.getElementById('modalUserActive').checked = user.active;

    new bootstrap.Modal(document.getElementById('userModal')).show();
};

async function handleSaveUser(e) {
    e.preventDefault();
    const id = document.getElementById('modalUserId').value;
    const isEdit = !!id;

    const payload = {
        code: document.getElementById('modalUserCode').value.trim(),
        role: document.getElementById('modalUserRole').value,
        firstName: document.getElementById('modalUserFirstName').value.trim(),
        lastName: document.getElementById('modalUserLastName').value.trim(),
        email: document.getElementById('modalUserEmail').value.trim(),
        department: document.getElementById('modalUserDepartment').value.trim(),
        position: document.getElementById('modalUserPosition').value.trim(),
        active: document.getElementById('modalUserActive').checked
    };

    const pwd = document.getElementById('modalUserPassword').value;
    if (pwd) {
        payload.password = pwd;
    }

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
            bootstrap.Modal.getInstance(document.getElementById('userModal')).hide();
            loadAdminEmployees();
            loadAdminDashboard();
            alert(`Usuario ${isEdit ? 'modificado' : 'creado'} exitosamente.`);
        } else {
            alert(`Error: ${data.message || 'No fue posible guardar el usuario.'}`);
        }
    } catch (err) {
        console.error('Error guardando usuario:', err);
        alert('Error de red al guardar usuario.');
    }
}

window.deleteUser = async function(id) {
    if (!confirm('¿Confirmas eliminar/desactivar este usuario (GU-03)? Sus datos y asistencias históricas se mantendrán.')) {
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/employees/${id}`, { method: 'DELETE' });
        if (res.ok) {
            loadAdminEmployees();
            loadAdminDashboard();
        } else {
            alert('No se pudo eliminar el usuario.');
        }
    } catch (err) {
        console.error('Error eliminando usuario:', err);
    }
};

// ==============================================================
// UTILIDADES: EXPORTAR A CSV
// ==============================================================

window.exportTableToCSV = function(tableId, filename) {
    const table = document.getElementById(tableId);
    if (!table) return;

    let csv = [];
    const rows = table.querySelectorAll('tr');

    for (let i = 0; i < rows.length; i++) {
        const row = [];
        const cols = rows[i].querySelectorAll('td, th');
        for (let j = 0; j < cols.length; j++) {
            let data = cols[j].innerText.replace(/(\r\n|\n|\r)/gm, '').replace(/(\s\s+)/gm, ' ');
            data = data.replace(/"/g, '""');
            row.push('"' + data + '"');
        }
        csv.push(row.join(';')); // Separador punto y coma para Excel latino
    }

    const csvFile = new Blob(['\ufeff' + csv.join('\n')], { type: 'text/csv;charset=utf-8;' });
    const downloadLink = document.createElement('a');
    downloadLink.download = filename;
    downloadLink.href = window.URL.createObjectURL(csvFile);
    downloadLink.style.display = 'none';
    document.body.appendChild(downloadLink);
    downloadLink.click();
    document.body.removeChild(downloadLink);
};

// ==============================================================
// HELPERS
// ==============================================================

function formatIsoTime(isoStr) {
    if (!isoStr) return '--:--:--';
    const parts = isoStr.split('T');
    if (parts.length > 1) {
        return parts[1].substring(0, 8);
    }
    return isoStr;
}

function getStatusBadge(status, description) {
    switch (status) {
        case 'ON_TIME':
            return `<span class="badge bg-success">${description || 'A tiempo'}</span>`;
        case 'LATE':
            return `<span class="badge bg-warning text-dark">${description || 'Atraso'}</span>`;
        case 'EARLY_EXIT':
            return `<span class="badge bg-danger">${description || 'Salida anticipada'}</span>`;
        default:
            return `<span class="badge bg-secondary">${description || 'Normal'}</span>`;
    }
}
