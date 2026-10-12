<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="User Management - Admin Portal"/>
</jsp:include>
<jsp:include page="/includes/navbar.jsp"/>
<jsp:include page="/includes/alerts.jsp"/>

<main class="container-fluid px-lg-4 py-4">
    <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
        <div>
            <h4 class="fw-bold mb-1"><i class="bi bi-people-fill text-primary me-2"></i>Campus User Management</h4>
            <p class="text-muted small mb-0">Oversee active students, faculty, security personnel, and system administrators</p>
        </div>

        <form action="${pageContext.request.contextPath}/admin/users" method="get" class="d-flex gap-2">
            <input type="text" name="keyword" class="form-control form-control-sm" placeholder="Search name, email..." value="${keyword}">
            <button type="submit" class="btn btn-primary btn-sm px-3">
                <i class="bi bi-search"></i>
            </button>
            <c:if test="${not empty keyword}">
                <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-outline-secondary btn-sm">Clear</a>
            </c:if>
        </form>
    </div>

    <div class="card shadow-sm border-0">
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-custom table-hover mb-0">
                    <thead>
                        <tr>
                            <th class="ps-3">User ID</th>
                            <th>Name</th>
                            <th>Email</th>
                            <th>Department</th>
                            <th>Phone</th>
                            <th>Role</th>
                            <th>Status</th>
                            <th>Registered</th>
                            <th class="text-end pe-3">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty users}">
                                <c:forEach var="u" items="${users}">
                                    <tr>
                                        <td class="ps-3 fw-bold text-muted">#${u.userId}</td>
                                        <td class="fw-bold text-dark">${u.name}</td>
                                        <td>${u.email}</td>
                                        <td>${not empty u.department ? u.department : '-'}</td>
                                        <td>${not empty u.phone ? u.phone : '-'}</td>
                                        <td>
                                            <!-- Change Role Form -->
                                            <form action="${pageContext.request.contextPath}/admin/users" method="post" class="d-inline">
                                                <input type="hidden" name="action" value="updateRole">
                                                <input type="hidden" name="userId" value="${u.userId}">
                                                <select name="newRole" class="form-select form-select-sm d-inline-block w-auto" onchange="this.form.submit()">
                                                    <option value="STUDENT" ${u.role eq 'STUDENT' ? 'selected' : ''}>STUDENT</option>
                                                    <option value="FACULTY" ${u.role eq 'FACULTY' ? 'selected' : ''}>FACULTY</option>
                                                    <option value="ADMIN" ${u.role eq 'ADMIN' ? 'selected' : ''}>ADMIN</option>
                                                </select>
                                            </form>
                                        </td>
                                        <td>
                                            <span class="badge ${u.active ? 'bg-success' : 'bg-danger'}">
                                                ${u.status}
                                            </span>
                                        </td>
                                        <td class="small text-muted">${u.createdAt}</td>
                                        <td class="text-end pe-3">
                                            <c:choose>
                                                <c:when test="${u.active}">
                                                    <form action="${pageContext.request.contextPath}/admin/users" method="post" class="d-inline" onsubmit="return confirm('Block user #${u.userId} (${u.name})?');">
                                                        <input type="hidden" name="action" value="block">
                                                        <input type="hidden" name="userId" value="${u.userId}">
                                                        <button type="submit" class="btn btn-outline-danger btn-sm" ${u.userId eq sessionScope.userId ? 'disabled' : ''}>
                                                            <i class="bi bi-slash-circle me-1"></i> Block
                                                        </button>
                                                    </form>
                                                </c:when>
                                                <c:otherwise>
                                                    <form action="${pageContext.request.contextPath}/admin/users" method="post" class="d-inline" onsubmit="return confirm('Unblock user #${u.userId} (${u.name})?');">
                                                        <input type="hidden" name="action" value="unblock">
                                                        <input type="hidden" name="userId" value="${u.userId}">
                                                        <button type="submit" class="btn btn-outline-success btn-sm">
                                                            <i class="bi bi-check-circle me-1"></i> Unblock
                                                        </button>
                                                    </form>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="9" class="text-center py-4 text-muted">No users found.</td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</main>

<jsp:include page="/includes/footer.jsp"/>
