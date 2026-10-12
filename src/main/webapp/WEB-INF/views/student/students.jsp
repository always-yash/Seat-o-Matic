<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="Student Management · Seat-o-Matic" />
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<main class="page-shell">
    <!-- Header Hero Section -->
    <section class="hero" style="max-width: 100%; display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 20px;">
        <div>
            <div class="eyebrow">Module 2 · Student Management</div>
            <h1>Student Roster & Cohort Management</h1>
            <p class="hero-copy">Register and monitor student candidates, manage cohort batch alignments across academic branches, and track seating eligibility.</p>
        </div>
        <div style="display: flex; gap: 12px; align-items: center; flex-wrap: wrap; margin-top: 12px;">
            <a href="${pageContext.request.contextPath}/students?action=new" class="button button-primary">
                + Add Student
            </a>
            <button type="button" class="button button-ghost" onclick="openImportModal()">
                Bulk Import CSV
            </button>
            <a href="${pageContext.request.contextPath}/students?action=export${not empty filter.keyword ? '&q='.concat(filter.keyword) : ''}${not empty filter.branchId ? '&branchId='.concat(filter.branchId) : ''}${not empty filter.semesterId ? '&semesterId='.concat(filter.semesterId) : ''}${not empty filter.status ? '&status='.concat(filter.status) : ''}" class="button button-ghost">
                Export CSV
            </a>
            <a href="${pageContext.request.contextPath}/students?action=template" class="button button-ghost button-small" title="Download sample import CSV template">
                Template
            </a>
        </div>
    </section>

    <!-- Last Import Errors Alert (if any) -->
    <c:if test="${not empty sessionScope.lastImportErrors}">
        <div class="alert alert-warning" style="margin-top: 24px;">
            <div style="display: flex; justify-content: space-between; align-items: center;">
                <strong>Import Validation Warnings (${fn:length(sessionScope.lastImportErrors)} items flagged):</strong>
                <button type="button" onclick="this.parentElement.nextElementSibling.style.display = (this.parentElement.nextElementSibling.style.display === 'none' ? 'block' : 'none')" class="button button-ghost button-small" style="padding: 2px 8px; font-size: 0.75rem;">Toggle Details</button>
            </div>
            <ul style="margin: 10px 0 0 18px; padding: 0; font-size: 0.85rem; max-height: 180px; overflow-y: auto;">
                <c:forEach var="err" items="${sessionScope.lastImportErrors}">
                    <li>Row ${err.rowNumber} (${err.enrollmentNo}): <c:out value="${err.message}" /></li>
                </c:forEach>
            </ul>
        </div>
        <c:remove var="lastImportErrors" scope="session" />
    </c:if>

    <!-- Metric Cards Grid -->
    <section class="stat-grid" style="margin-top: 28px;">
        <div class="card stat-card">
            <div class="stat-label">TOTAL STUDENTS</div>
            <div class="stat-value"><c:out value="${totalStudents}" /></div>
            <div style="margin-top: 8px; font-size: 0.8rem; color: var(--ink-500);">Registered across all cohorts</div>
        </div>
        <div class="card stat-card">
            <div class="stat-label">ACTIVE CANDIDATES</div>
            <div class="stat-value" style="color: var(--mint);"><c:out value="${activeStudents}" /></div>
            <div style="margin-top: 8px; font-size: 0.8rem; color: var(--ink-500);">Eligible for examination seating</div>
        </div>
        <div class="card stat-card">
            <div class="stat-label">INACTIVE / ON LEAVE</div>
            <div class="stat-value" style="color: var(--warning);"><c:out value="${totalStudents - activeStudents}" /></div>
            <div style="margin-top: 8px; font-size: 0.8rem; color: var(--ink-500);">Excluded from active seating plans</div>
        </div>
        <div class="card stat-card">
            <div class="stat-label">ACADEMIC BRANCHES</div>
            <div class="stat-value" style="color: var(--blue-bright);"><c:out value="${fn:length(branches)}" /></div>
            <div style="margin-top: 8px; font-size: 0.8rem; color: var(--ink-500);">Active departments configured</div>
        </div>
    </section>

    <!-- Batch Grouping Pills -->
    <section style="margin-top: 24px; display: flex; gap: 10px; align-items: center; flex-wrap: wrap;">
        <span style="font-size: 0.8rem; font-weight: 600; color: var(--ink-500); text-transform: uppercase; letter-spacing: 0.08em; margin-right: 4px;">Quick Cohort Filter:</span>
        <a href="${pageContext.request.contextPath}/students" class="button button-small ${empty filter.branchId ? 'button-primary' : 'button-ghost'}" style="border-radius: var(--radius-pill);">
            All Cohorts (${totalStudents})
        </a>
        <c:forEach var="bStat" items="${batchStats}">
            <a href="${pageContext.request.contextPath}/students?branchId=${bStat.branchId}"
               class="button button-small ${filter.branchId == bStat.branchId ? 'button-primary' : 'button-ghost'}"
               style="border-radius: var(--radius-pill);"
               title="${bStat.branchName}">
                <c:out value="${bStat.branchCode}" /> (${bStat.totalStudents})
            </a>
        </c:forEach>
    </section>

    <!-- Search & Filter Card -->
    <section class="card" style="padding: 22px 26px; margin-top: 24px;">
        <form id="filterForm" method="get" action="${pageContext.request.contextPath}/students">
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; align-items: flex-end;">
                <!-- Instant Search with Live Autocomplete -->
                <div style="position: relative;">
                    <label class="form-label" for="searchInput">Search Roll No or Name</label>
                    <input type="text" id="searchInput" name="q" value="<c:out value='${filter.keyword}' />" class="form-control" placeholder="Type roll number or name..." autocomplete="off">
                    <div id="instantSearchResults" style="display: none; position: absolute; top: calc(100% + 4px); left: 0; right: 0; background: var(--surface-slate); border: 1px solid var(--line-strong); border-radius: var(--radius-md); box-shadow: var(--shadow-lg); z-index: 50; max-height: 240px; overflow-y: auto;"></div>
                </div>

                <!-- Branch Filter -->
                <div>
                    <label class="form-label" for="filterBranch">Branch</label>
                    <select id="filterBranch" name="branchId" class="form-control" style="background: rgba(20, 31, 52, .95);" onchange="handleBranchFilterChange(this.value)">
                        <option value="">-- All Branches --</option>
                        <c:forEach var="br" items="${branches}">
                            <option value="${br.id}" ${br.id == filter.branchId ? 'selected' : ''}>
                                <c:out value="${br.code}" /> · <c:out value="${br.name}" />
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <!-- Section Filter (Dynamic) -->
                <div>
                    <label class="form-label" for="filterSection">Section</label>
                    <select id="filterSection" name="sectionId" class="form-control" style="background: rgba(20, 31, 52, .95);">
                        <option value="">-- All Sections --</option>
                        <c:forEach var="sec" items="${sections}">
                            <option value="${sec.id}" ${sec.id == filter.sectionId ? 'selected' : ''}>
                                <c:out value="${sec.name}" />
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <!-- Semester Filter -->
                <div>
                    <label class="form-label" for="filterSemester">Semester</label>
                    <select id="filterSemester" name="semesterId" class="form-control" style="background: rgba(20, 31, 52, .95);">
                        <option value="">-- All Semesters --</option>
                        <c:forEach var="sem" items="${semesters}">
                            <option value="${sem.id}" ${sem.id == filter.semesterId ? 'selected' : ''}>
                                Semester <c:out value="${sem.number}" />
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <!-- Status Filter -->
                <div>
                    <label class="form-label" for="filterStatus">Status</label>
                    <select id="filterStatus" name="status" class="form-control" style="background: rgba(20, 31, 52, .95);">
                        <option value="">-- All Statuses --</option>
                        <option value="ACTIVE" ${'ACTIVE' == filter.status ? 'selected' : ''}>Active Only</option>
                        <option value="INACTIVE" ${'INACTIVE' == filter.status ? 'selected' : ''}>Inactive Only</option>
                    </select>
                </div>

                <!-- Filter Actions -->
                <div style="display: flex; gap: 10px;">
                    <button type="submit" class="button button-primary" style="flex: 1;">Filter</button>
                    <a href="${pageContext.request.contextPath}/students" class="button button-ghost" style="text-align: center;">Reset</a>
                </div>
            </div>
        </form>
    </section>

    <!-- Student Table Grid -->
    <section class="card" style="padding: 24px; margin-top: 24px;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 12px;">
            <div>
                <div class="eyebrow">Student Registry</div>
                <h2>Enrolled Student Directory</h2>
            </div>
            <div style="font-size: 0.85rem; color: var(--ink-500);">
                Showing <strong>${studentsPage.startIndex}</strong> to <strong>${studentsPage.endIndex}</strong> of <strong>${studentsPage.totalItems}</strong> students
            </div>
        </div>

        <c:choose>
            <c:when test="${empty studentsPage.items}">
                <div style="padding: 56px 20px; text-align: center; color: var(--ink-500);">
                    <div style="font-size: 2.5rem; margin-bottom: 12px; opacity: 0.6;">📋</div>
                    <p style="font-size: 1.15rem; color: var(--ink-300); margin: 0 0 8px;">No students match the current filter criteria.</p>
                    <p style="margin: 0 0 20px; font-size: 0.9rem;">Try loosening your search query or reset the filters to view all enrolled candidates.</p>
                    <a href="${pageContext.request.contextPath}/students" class="button button-ghost button-small">Clear All Filters</a>
                    <a href="${pageContext.request.contextPath}/students?action=new" class="button button-primary button-small" style="margin-left: 10px;">+ Add New Student</a>
                </div>
            </c:when>
            <c:otherwise>
                <div style="overflow-x: auto;">
                    <table style="width: 100%; border-collapse: collapse; text-align: left; font-size: 0.9rem;">
                        <thead>
                            <tr style="border-bottom: 1px solid var(--line-strong); color: var(--ink-500); font-size: 0.78rem; text-transform: uppercase; letter-spacing: 0.08em;">
                                <th style="padding: 12px 14px;">Roll / Enrollment No</th>
                                <th style="padding: 12px 14px;">Student Name</th>
                                <th style="padding: 12px 14px;">Branch</th>
                                <th style="padding: 12px 14px;">Section</th>
                                <th style="padding: 12px 14px;">Semester</th>
                                <th style="padding: 12px 14px; text-align: center;">Exams</th>
                                <th style="padding: 12px 14px; text-align: center;">Status</th>
                                <th style="padding: 12px 14px; text-align: right; min-width: 180px;">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="s" items="${studentsPage.items}">
                                <tr style="border-bottom: 1px solid var(--line); transition: background .15s ease;" onmouseover="this.style.background='rgba(56, 189, 248, .04)'" onmouseout="this.style.background='transparent'">
                                    <td style="padding: 14px; font-family: monospace; font-size: 0.95rem; font-weight: 700; color: var(--blue-bright);">
                                        <a href="${pageContext.request.contextPath}/students?action=view&id=${s.id}" style="color: inherit; text-decoration: underline; text-decoration-color: rgba(56, 189, 248, .3);">
                                            <c:out value="${s.enrollmentNo}" />
                                        </a>
                                    </td>
                                    <td style="padding: 14px; font-weight: 600; color: var(--white);">
                                        <c:out value="${s.name}" />
                                    </td>
                                    <td style="padding: 14px;">
                                        <span class="badge" style="background: rgba(129, 140, 248, .15); color: var(--violet); border: 1px solid rgba(129, 140, 248, .3); padding: 3px 8px; border-radius: var(--radius-sm); font-size: 0.78rem; font-weight: 600;">
                                            <c:out value="${s.branchCode}" />
                                        </span>
                                    </td>
                                    <td style="padding: 14px; color: var(--ink-300);">
                                        <c:out value="${s.sectionName}" />
                                    </td>
                                    <td style="padding: 14px; color: var(--ink-300);">
                                        Sem <c:out value="${s.semesterNumber}" />
                                    </td>
                                    <td style="padding: 14px; text-align: center;">
                                        <span style="font-weight: 700; color: ${s.examCount > 0 ? 'var(--blue-bright)' : 'var(--ink-500)'};">
                                            <c:out value="${s.examCount}" />
                                        </span>
                                    </td>
                                    <td style="padding: 14px; text-align: center;">
                                        <c:choose>
                                            <c:when test="${s.active}">
                                                <span style="padding: 4px 10px; border-radius: var(--radius-pill); font-size: 0.75rem; font-weight: 700; text-transform: uppercase; border: 1px solid rgba(52, 211, 153, 0.4); color: var(--mint); background: rgba(52, 211, 153, 0.12);">
                                                    ACTIVE
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span style="padding: 4px 10px; border-radius: var(--radius-pill); font-size: 0.75rem; font-weight: 700; text-transform: uppercase; border: 1px solid rgba(251, 191, 36, 0.4); color: var(--warning); background: rgba(251, 191, 36, 0.12);">
                                                    INACTIVE
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="padding: 14px; text-align: right;">
                                        <div style="display: inline-flex; gap: 6px; align-items: center;">
                                            <a href="${pageContext.request.contextPath}/students?action=view&id=${s.id}" class="button button-ghost button-small" title="View Profile">
                                                View
                                            </a>
                                            <a href="${pageContext.request.contextPath}/students?action=edit&id=${s.id}" class="button button-ghost button-small" title="Edit Student">
                                                Edit
                                            </a>
                                            <!-- Status toggle form -->
                                            <form method="post" action="${pageContext.request.contextPath}/students" style="display: inline; margin: 0;">
                                                <input type="hidden" name="_csrf" value="${csrfToken}">
                                                <input type="hidden" name="action" value="toggle-status">
                                                <input type="hidden" name="id" value="${s.id}">
                                                <button type="submit" class="button button-ghost button-small" title="${s.active ? 'Deactivate' : 'Activate'}">
                                                    ${s.active ? 'Disable' : 'Enable'}
                                                </button>
                                            </form>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>

                <!-- Pagination Footer -->
                <c:if test="${studentsPage.totalPages > 1}">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 24px; padding-top: 18px; border-top: 1px solid var(--line); flex-wrap: wrap; gap: 12px;">
                        <div style="font-size: 0.85rem; color: var(--ink-500);">
                            Page <strong>${studentsPage.currentPage}</strong> of <strong>${studentsPage.totalPages}</strong>
                        </div>
                        <div style="display: flex; gap: 6px; align-items: center;">
                            <c:if test="${studentsPage.hasPrevious()}">
                                <a href="${pageContext.request.contextPath}/students?page=${studentsPage.currentPage - 1}${not empty filter.keyword ? '&q='.concat(filter.keyword) : ''}${not empty filter.branchId ? '&branchId='.concat(filter.branchId) : ''}${not empty filter.sectionId ? '&sectionId='.concat(filter.sectionId) : ''}${not empty filter.semesterId ? '&semesterId='.concat(filter.semesterId) : ''}${not empty filter.status ? '&status='.concat(filter.status) : ''}" class="button button-ghost button-small">
                                    ← Previous
                                </a>
                            </c:if>

                            <c:forEach var="p" begin="1" end="${studentsPage.totalPages}">
                                <c:if test="${p == 1 || p == studentsPage.totalPages || (p >= studentsPage.currentPage - 2 && p <= studentsPage.currentPage + 2)}">
                                    <a href="${pageContext.request.contextPath}/students?page=${p}${not empty filter.keyword ? '&q='.concat(filter.keyword) : ''}${not empty filter.branchId ? '&branchId='.concat(filter.branchId) : ''}${not empty filter.sectionId ? '&sectionId='.concat(filter.sectionId) : ''}${not empty filter.semesterId ? '&semesterId='.concat(filter.semesterId) : ''}${not empty filter.status ? '&status='.concat(filter.status) : ''}"
                                       class="button button-small ${p == studentsPage.currentPage ? 'button-primary' : 'button-ghost'}"
                                       style="min-width: 32px; padding: 6px 10px; text-align: center;">
                                        ${p}
                                    </a>
                                </c:if>
                            </c:forEach>

                            <c:if test="${studentsPage.hasNext()}">
                                <a href="${pageContext.request.contextPath}/students?page=${studentsPage.currentPage + 1}${not empty filter.keyword ? '&q='.concat(filter.keyword) : ''}${not empty filter.branchId ? '&branchId='.concat(filter.branchId) : ''}${not empty filter.sectionId ? '&sectionId='.concat(filter.sectionId) : ''}${not empty filter.semesterId ? '&semesterId='.concat(filter.semesterId) : ''}${not empty filter.status ? '&status='.concat(filter.status) : ''}" class="button button-ghost button-small">
                                    Next →
                                </a>
                            </c:if>
                        </div>
                    </div>
                </c:if>
            </c:otherwise>
        </c:choose>
    </section>
</main>

<!-- Bulk CSV Import Modal -->
<div id="importModal" style="display: none; position: fixed; inset: 0; background: rgba(7, 11, 22, 0.75); backdrop-filter: blur(8px); z-index: 100; align-items: center; justify-content: center; padding: 20px;">
    <div class="card" style="width: min(520px, 100%); padding: 32px; background: rgba(30, 41, 59, 0.95); border: 1px solid var(--line-strong); box-shadow: var(--shadow-lg);">
        <div style="display: flex; justify-content: space-between; align-items: flex-start;">
            <div>
                <div class="eyebrow">Batch Registration</div>
                <h2 style="margin-top: 4px;">Bulk Student CSV Import</h2>
            </div>
            <button type="button" onclick="closeImportModal()" style="background: none; border: none; color: var(--ink-500); font-size: 1.5rem; cursor: pointer; line-height: 1;">&times;</button>
        </div>
        <p style="margin: 12px 0 20px; font-size: 0.9rem; color: var(--ink-300); line-height: 1.5;">
            Upload a CSV containing student rosters. Duplicate roll numbers are safely recognized and line-by-line validation errors will be reported.
        </p>

        <form method="post" action="${pageContext.request.contextPath}/students/import" enctype="multipart/form-data">
            <input type="hidden" name="_csrf" value="${csrfToken}">

            <div class="form-group" style="margin-top: 0;">
                <label class="form-label" for="csvFileInput">Choose CSV File</label>
                <input type="file" id="csvFileInput" name="file" accept=".csv" class="form-control" required style="padding: 10px;">
            </div>

            <div style="margin: 16px 0; padding: 12px; border-radius: var(--radius-sm); background: rgba(15, 23, 42, 0.6); border: 1px solid var(--line); font-size: 0.8rem; color: var(--ink-500);">
                <strong>Expected columns:</strong> <code>enrollment_no, name, branch_code, section, semester, status</code><br>
                Need a template? <a href="${pageContext.request.contextPath}/students?action=template" style="color: var(--blue-bright); text-decoration: underline;">Download sample CSV</a>
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px;">
                <button type="button" class="button button-ghost" onclick="closeImportModal()">Cancel</button>
                <button type="submit" class="button button-primary">Upload & Process</button>
            </div>
        </form>
    </div>
</div>

<script>
function openImportModal() {
    const modal = document.getElementById('importModal');
    modal.style.display = 'flex';
}

function closeImportModal() {
    const modal = document.getElementById('importModal');
    modal.style.display = 'none';
}

// Cascading section loading on branch filter change
function handleBranchFilterChange(branchId) {
    const sectionSelect = document.getElementById('filterSection');
    if (!branchId) {
        sectionSelect.innerHTML = '<option value="">-- All Sections --</option>';
        return;
    }
    fetch('${pageContext.request.contextPath}/students?action=sections&branchId=' + encodeURIComponent(branchId), {
        headers: { 'Accept': 'application/json' }
    })
    .then(res => res.json())
    .then(response => {
        let opts = '<option value="">-- All Sections --</option>';
        if (response && response.data) {
            response.data.forEach(sec => {
                opts += '<option value="' + sec.id + '">' + sec.name + '</option>';
            });
        }
        sectionSelect.innerHTML = opts;
    })
    .catch(err => console.error('Failed to load sections:', err));
}

// Instant Live Search Autocomplete
(function() {
    const searchInput = document.getElementById('searchInput');
    const resultsContainer = document.getElementById('instantSearchResults');
    let debounceTimer;

    if (!searchInput || !resultsContainer) return;

    searchInput.addEventListener('input', function() {
        clearTimeout(debounceTimer);
        const query = this.value.trim();
        if (query.length < 2) {
            resultsContainer.style.display = 'none';
            resultsContainer.innerHTML = '';
            return;
        }

        debounceTimer = setTimeout(() => {
            fetch('${pageContext.request.contextPath}/students?action=search&q=' + encodeURIComponent(query), {
                headers: { 'Accept': 'application/json' }
            })
            .then(res => res.json())
            .then(res => {
                if (!res.data || res.data.length === 0) {
                    resultsContainer.innerHTML = '<div style="padding: 12px 14px; font-size: 0.85rem; color: var(--ink-500);">No matching students found</div>';
                    resultsContainer.style.display = 'block';
                    return;
                }
                let html = '';
                res.data.forEach(s => {
                    html += '<a href="${pageContext.request.contextPath}/students?action=view&id=' + s.id + '" style="display: flex; justify-content: space-between; align-items: center; padding: 10px 14px; border-bottom: 1px solid var(--line); color: var(--white); font-size: 0.85rem; text-decoration: none;" onmouseover="this.style.background=\'rgba(56, 189, 248, .1)\'" onmouseout="this.style.background=\'transparent\'">' +
                        '<div><strong>' + s.name + '</strong> <span style="color: var(--blue-bright); font-family: monospace; margin-left: 6px;">' + s.enrollmentNo + '</span></div>' +
                        '<div style="font-size: 0.78rem; color: var(--ink-500);">' + (s.branchCode || '') + ' · Sem ' + (s.semesterNumber || '') + '</div>' +
                        '</a>';
                });
                resultsContainer.innerHTML = html;
                resultsContainer.style.display = 'block';
            })
            .catch(() => {
                resultsContainer.style.display = 'none';
            });
        }, 250);
    });

    document.addEventListener('click', function(e) {
        if (!searchInput.contains(e.target) && !resultsContainer.contains(e.target)) {
            resultsContainer.style.display = 'none';
        }
    });
})();
</script>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
