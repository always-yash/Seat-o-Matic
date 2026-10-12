<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Admin Dashboard - Campus Lost & Found"/>
</jsp:include>
<jsp:include page="/includes/navbar.jsp"/>
<jsp:include page="/includes/alerts.jsp"/>

<main class="container-fluid px-lg-4 py-4">
    <!-- Header -->
    <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
        <div>
            <h4 class="fw-bold mb-1"><i class="bi bi-speedometer2 text-primary me-2"></i>Campus Administrator Portal</h4>
            <p class="text-muted small mb-0">Centralized control panel for users, inventory statistics, moderation, and institutional reports</p>
        </div>
        <div class="d-flex gap-2">
            <a href="${pageContext.request.contextPath}/admin/export-pdf" class="btn btn-danger btn-sm shadow-sm">
                <i class="bi bi-file-earmark-pdf-fill me-1"></i> Export PDF Audit Report
            </a>
            <a href="${pageContext.request.contextPath}/admin/analytics" class="btn btn-primary btn-sm shadow-sm">
                <i class="bi bi-graph-up me-1"></i> View Analytics
            </a>
        </div>
    </div>

    <!-- Stats Cards Row 1: Users & System Overview -->
    <div class="row g-3 mb-4">
        <div class="col-xl-3 col-md-6">
            <div class="stat-card">
                <div>
                    <div class="stat-label">Total Users</div>
                    <div class="stat-number text-dark">${stats.totalUsers}</div>
                    <div class="small text-muted mt-1">
                        <span class="text-success"><i class="bi bi-check-circle"></i> ${stats.activeUsers} Active</span> &bull;
                        <span class="text-danger"><i class="bi bi-slash-circle"></i> ${stats.blockedUsers} Blocked</span>
                    </div>
                </div>
                <div class="stat-icon bg-primary-subtle text-primary">
                    <i class="bi bi-people-fill"></i>
                </div>
            </div>
        </div>

        <div class="col-xl-3 col-md-6">
            <div class="stat-card">
                <div>
                    <div class="stat-label">Lost Items</div>
                    <div class="stat-number text-danger">${stats.lostItems}</div>
                    <div class="small text-muted mt-1">Reported by campus members</div>
                </div>
                <div class="stat-icon bg-danger-subtle text-danger">
                    <i class="bi bi-exclamation-circle-fill"></i>
                </div>
            </div>
        </div>

        <div class="col-xl-3 col-md-6">
            <div class="stat-card">
                <div>
                    <div class="stat-label">Found Items</div>
                    <div class="stat-number text-success">${stats.foundItems}</div>
                    <div class="small text-muted mt-1">Logged into custody</div>
                </div>
                <div class="stat-icon bg-success-subtle text-success">
                    <i class="bi bi-box2-heart-fill"></i>
                </div>
            </div>
        </div>

        <div class="col-xl-3 col-md-6">
            <div class="stat-card">
                <div>
                    <div class="stat-label">Items Returned</div>
                    <div class="stat-number text-teal" style="color: #0f766e;">${stats.returnedItems}</div>
                    <div class="small text-muted mt-1">Verified handovers complete</div>
                </div>
                <div class="stat-icon bg-teal-subtle" style="background-color: #ccfbf1; color: #0f766e;">
                    <i class="bi bi-patch-check-fill"></i>
                </div>
            </div>
        </div>
    </div>

    <!-- Stats Cards Row 2: Workflow & Actionables -->
    <div class="row g-3 mb-4">
        <div class="col-md-3">
            <div class="card p-3 shadow-sm border-0 border-start border-warning border-4">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <div class="text-muted small fw-semibold">PENDING CLAIMS</div>
                        <h4 class="fw-bold mb-0 text-dark">${stats.pendingClaims}</h4>
                    </div>
                    <a href="${pageContext.request.contextPath}/claims?action=manage" class="btn btn-outline-warning btn-sm">Review</a>
                </div>
            </div>
        </div>

        <div class="col-md-3">
            <div class="card p-3 shadow-sm border-0 border-start border-danger border-4">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <div class="text-muted small fw-semibold">OPEN MODERATION FLAGS</div>
                        <h4 class="fw-bold mb-0 text-dark">${stats.openReports}</h4>
                    </div>
                    <a href="${pageContext.request.contextPath}/admin/reports" class="btn btn-outline-danger btn-sm">Inspect</a>
                </div>
            </div>
        </div>

        <div class="col-md-3">
            <div class="card p-3 shadow-sm border-0 border-start border-info border-4">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <div class="text-muted small fw-semibold">ALGORITHMIC MATCHES</div>
                        <h4 class="fw-bold mb-0 text-dark">${stats.matchedItems}</h4>
                    </div>
                    <a href="${pageContext.request.contextPath}/items?action=gallery" class="btn btn-outline-info btn-sm">Explore</a>
                </div>
            </div>
        </div>

        <div class="col-md-3">
            <div class="card p-3 shadow-sm border-0 border-start border-secondary border-4">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <div class="text-muted small fw-semibold">DISPOSAL REVIEW</div>
                        <h4 class="fw-bold mb-0 text-dark">Unclaimed</h4>
                    </div>
                    <a href="${pageContext.request.contextPath}/admin/disposal" class="btn btn-outline-secondary btn-sm">Examine</a>
                </div>
            </div>
        </div>
    </div>

    <!-- Audit Logs Table -->
    <div class="card shadow-sm border-0">
        <div class="card-header bg-white py-3 border-bottom d-flex justify-content-between align-items-center">
            <h6 class="fw-bold mb-0 text-dark"><i class="bi bi-clock-history text-primary me-2"></i>Recent System Audit Trail Logs</h6>
            <span class="badge bg-light text-muted border">Real-time DB Events</span>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-custom table-hover mb-0">
                    <thead>
                        <tr>
                            <th class="ps-3">Log ID</th>
                            <th>Action Performed</th>
                            <th>User</th>
                            <th>IP Address</th>
                            <th>Timestamp</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty auditLogs}">
                                <c:forEach var="log" items="${auditLogs}">
                                    <tr>
                                        <td class="ps-3 fw-bold text-muted">#${log.logId}</td>
                                        <td><code class="text-primary fw-semibold">${log.action}</code></td>
                                        <td>
                                            <span class="fw-semibold text-dark">${log.userName}</span>
                                            <c:if test="${not empty log.userEmail}">
                                                <span class="small text-muted">(${log.userEmail})</span>
                                            </c:if>
                                        </td>
                                        <td><span class="badge bg-light text-secondary border">${log.ipAddress}</span></td>
                                        <td class="small text-muted">${log.createdAt}</td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="5" class="text-center py-4 text-muted">No audit logs recorded yet.</td>
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
