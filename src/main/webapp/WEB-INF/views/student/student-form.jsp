<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="${isEdit ? 'Edit Student · Seat-o-Matic' : 'Register Student · Seat-o-Matic'}" />
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<main class="page-shell">
    <div style="max-width: 680px; margin: 0 auto;">
        <!-- Header breadcrumb navigation -->
        <div style="margin-bottom: 20px;">
            <a href="${pageContext.request.contextPath}/students" class="button button-ghost button-small">
                ← Back to Student Directory
            </a>
        </div>

        <section class="hero" style="max-width: 100%; margin-bottom: 28px;">
            <div class="eyebrow">${isEdit ? 'Record Maintenance' : 'Candidate Intake'}</div>
            <h1>${isEdit ? 'Update Student Profile' : 'Register New Student'}</h1>
            <p class="hero-copy">
                ${isEdit ? 'Modify student registration details, update cohort alignments, or adjust eligibility status.' : 'Enlist a new examination candidate with cohort alignment for automated seating arrangement.'}
            </p>
        </section>

        <!-- Form Card -->
        <section class="card" style="padding: 36px 32px;">
            <form id="studentForm" method="post" action="${pageContext.request.contextPath}/students" onsubmit="return validateForm();">
                <input type="hidden" name="_csrf" value="${csrfToken}">
                <input type="hidden" name="action" value="${isEdit ? 'update' : 'create'}">
                <c:if test="${isEdit}">
                    <input type="hidden" name="id" value="${student.id}">
                </c:if>

                <!-- Roll Number / Enrollment Number -->
                <div class="form-group" style="margin-top: 0;">
                    <label class="form-label" for="enrollmentNo">
                        Roll / Enrollment Number <span style="color: var(--danger);">*</span>
                    </label>
                    <input type="text"
                           id="enrollmentNo"
                           name="enrollmentNo"
                           value="<c:out value='${student.enrollmentNo}' />"
                           class="form-control"
                           placeholder="e.g. 2026CS101 or BTECH-ECE-042"
                           maxlength="50"
                           required
                           style="font-family: monospace; font-size: 1rem; font-weight: 600;">
                    <div id="enrollmentHint" style="margin-top: 6px; font-size: 0.78rem; color: var(--ink-500);">
                        3 to 50 alphanumeric characters or hyphens/underscores/slashes. Must be unique across institution.
                    </div>
                </div>

                <!-- Full Name -->
                <div class="form-group">
                    <label class="form-label" for="studentName">
                        Full Name <span style="color: var(--danger);">*</span>
                    </label>
                    <input type="text"
                           id="studentName"
                           name="name"
                           value="<c:out value='${student.name}' />"
                           class="form-control"
                           placeholder="e.g. Aarav Sharma"
                           maxlength="120"
                           required>
                </div>

                <!-- Academic Branch Selection (Cascades to Sections) -->
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 18px;">
                    <div class="form-group" style="margin: 0;">
                        <label class="form-label" for="branchSelect">
                            Academic Branch <span style="color: var(--danger);">*</span>
                        </label>
                        <select id="branchSelect" class="form-control" style="background: rgba(20, 31, 52, .95);" onchange="onBranchChanged(this.value)" required>
                            <option value="">-- Select Branch --</option>
                            <c:forEach var="br" items="${branches}">
                                <option value="${br.id}" ${br.id == student.branchId ? 'selected' : ''}>
                                    <c:out value="${br.code}" /> · <c:out value="${br.name}" />
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <!-- Academic Section Selection -->
                    <div class="form-group" style="margin: 0;">
                        <label class="form-label" for="sectionSelect">
                            Section <span style="color: var(--danger);">*</span>
                        </label>
                        <select id="sectionSelect" name="sectionId" class="form-control" style="background: rgba(20, 31, 52, .95);" required>
                            <option value="">-- Select Section --</option>
                            <c:forEach var="sec" items="${sections}">
                                <option value="${sec.id}" ${sec.id == student.sectionId ? 'selected' : ''}>
                                    <c:out value="${sec.name}" />
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <!-- Semester & Status Grid -->
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 18px; margin-top: 20px;">
                    <!-- Academic Semester -->
                    <div class="form-group" style="margin: 0;">
                        <label class="form-label" for="semesterSelect">
                            Semester <span style="color: var(--danger);">*</span>
                        </label>
                        <select id="semesterSelect" name="semesterId" class="form-control" style="background: rgba(20, 31, 52, .95);" required>
                            <option value="">-- Select Semester --</option>
                            <c:forEach var="sem" items="${semesters}">
                                <option value="${sem.id}" ${sem.id == student.semesterId ? 'selected' : ''}>
                                    Semester <c:out value="${sem.number}" /> (<c:out value="${sem.name}" />)
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <!-- Status Selection -->
                    <div class="form-group" style="margin: 0;">
                        <label class="form-label" for="statusSelect">
                            Candidate Status <span style="color: var(--danger);">*</span>
                        </label>
                        <select id="statusSelect" name="status" class="form-control" style="background: rgba(20, 31, 52, .95);" required>
                            <option value="ACTIVE" ${'INACTIVE' != student.status ? 'selected' : ''}>ACTIVE (Eligible for Exams)</option>
                            <option value="INACTIVE" ${'INACTIVE' == student.status ? 'selected' : ''}>INACTIVE (Excluded from Seating)</option>
                        </select>
                    </div>
                </div>

                <!-- Form Action Buttons -->
                <div style="display: flex; justify-content: flex-end; gap: 14px; margin-top: 36px; padding-top: 24px; border-top: 1px solid var(--line);">
                    <a href="${pageContext.request.contextPath}/students${isEdit ? '?action=view&id='.concat(student.id) : ''}" class="button button-ghost">
                        Cancel
                    </a>
                    <button type="submit" class="button button-primary">
                        ${isEdit ? 'Save Changes' : 'Register Candidate'}
                    </button>
                </div>
            </form>
        </section>
    </div>
</main>

<script>
function onBranchChanged(branchId) {
    const sectionSelect = document.getElementById('sectionSelect');
    if (!branchId) {
        sectionSelect.innerHTML = '<option value="">-- Select Section --</option>';
        return;
    }
    fetch('${pageContext.request.contextPath}/students?action=sections&branchId=' + encodeURIComponent(branchId), {
        headers: { 'Accept': 'application/json' }
    })
    .then(res => res.json())
    .then(response => {
        let opts = '<option value="">-- Select Section --</option>';
        if (response && response.data && response.data.length > 0) {
            response.data.forEach(sec => {
                opts += '<option value="' + sec.id + '">' + sec.name + '</option>';
            });
        } else {
            opts += '<option value="">No sections found for branch</option>';
        }
        sectionSelect.innerHTML = opts;
    })
    .catch(err => {
        console.error('Failed to cascade sections:', err);
    });
}

function validateForm() {
    const roll = document.getElementById('enrollmentNo').value.trim();
    const name = document.getElementById('studentName').value.trim();
    const sec = document.getElementById('sectionSelect').value;
    const sem = document.getElementById('semesterSelect').value;

    const rollRegex = /^[A-Za-z0-9/_-]{3,50}$/;
    if (!rollRegex.test(roll)) {
        alert('Please enter a valid Roll / Enrollment number (3 to 50 alphanumeric characters or hyphens).');
        document.getElementById('enrollmentNo').focus();
        return false;
    }

    if (name.length < 2) {
        alert('Please enter a valid Student Full Name (at least 2 characters).');
        document.getElementById('studentName').focus();
        return false;
    }

    if (!sec) {
        alert('Please select an Academic Section.');
        document.getElementById('sectionSelect').focus();
        return false;
    }

    if (!sem) {
        alert('Please select an Academic Semester.');
        document.getElementById('semesterSelect').focus();
        return false;
    }

    return true;
}
</script>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>

