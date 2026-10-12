<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<nav class="navbar navbar-expand-lg navbar-custom sticky-top">
    <div class="container-fluid px-lg-4">
        <a class="navbar-brand" href="${pageContext.request.contextPath}/index.jsp">
            <i class="bi bi-box-seam-fill text-primary"></i>
            <span>Campus Lost &amp; Found</span>
        </a>
        <button class="navbar-toggler border-0" type="button" data-bs-toggle="collapse" data-bs-target="#mainNavbar" aria-controls="mainNavbar" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="mainNavbar">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/items?action=gallery">
                        <i class="bi bi-grid-fill me-1"></i> Browse Gallery
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/search">
                        <i class="bi bi-search me-1"></i> Search &amp; Filter
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link text-danger fw-semibold" href="${pageContext.request.contextPath}/items?action=report-lost">
                        <i class="bi bi-exclamation-circle-fill text-danger me-1"></i> Report Lost
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link text-success fw-semibold" href="${pageContext.request.contextPath}/items?action=report-found">
                        <i class="bi bi-check-circle-fill text-success me-1"></i> Report Found
                    </a>
                </li>

                <c:if test="${not empty sessionScope.userId}">
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/items?action=my-items">
                            <i class="bi bi-collection me-1"></i> My Items
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/claims?action=my-claims">
                            <i class="bi bi-hand-index-thumb me-1"></i> Claims
                        </a>
                    </li>

                    <c:if test="${sessionScope.userRole eq 'ADMIN'}">
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle text-primary fw-bold" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                <i class="bi bi-shield-lock-fill me-1"></i> Admin Portal
                            </a>
                            <ul class="dropdown-menu shadow-sm">
                                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/admin/dashboard"><i class="bi bi-speedometer2 me-2"></i> Dashboard</a></li>
                                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/admin/users"><i class="bi bi-people me-2"></i> Manage Users</a></li>
                                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/admin/reports"><i class="bi bi-flag me-2"></i> Moderation Reports</a></li>
                                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/admin/analytics"><i class="bi bi-graph-up me-2"></i> Analytics &amp; Trends</a></li>
                                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/admin/disposal"><i class="bi bi-archive me-2"></i> Unclaimed Disposal</a></li>
                                <li><hr class="dropdown-divider"></li>
                                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/admin/export-pdf"><i class="bi bi-file-earmark-pdf-fill text-danger me-2"></i> Download PDF Audit</a></li>
                            </ul>
                        </li>
                    </c:if>
                </c:if>
            </ul>

            <ul class="navbar-nav ms-auto align-items-center">
                <c:choose>
                    <c:when test="${not empty sessionScope.userId}">
                        <!-- Notifications -->
                        <li class="nav-item me-2">
                            <a class="nav-link position-relative" href="${pageContext.request.contextPath}/notifications" title="Notifications">
                                <i class="bi bi-bell-fill fs-5"></i>
                            </a>
                        </li>

                        <!-- User Profile Dropdown -->
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle d-flex align-items-center gap-2" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                <div class="rounded-circle bg-primary text-white d-flex align-items-center justify-content-center fw-bold" style="width: 32px; height: 32px; font-size: 0.85rem;">
                                    ${fn:substring(sessionScope.userName, 0, 1)}
                                </div>
                                <div class="d-none d-md-block text-start lh-1">
                                    <div class="fw-semibold small">${sessionScope.userName}</div>
                                    <span class="badge ${sessionScope.userRole eq 'ADMIN' ? 'badge-role-admin' : (sessionScope.userRole eq 'FACULTY' ? 'badge-role-faculty' : 'badge-role-student')}" style="font-size: 0.65rem;">
                                        ${sessionScope.userRole}
                                    </span>
                                </div>
                            </a>
                            <ul class="dropdown-menu dropdown-menu-end shadow-sm">
                                <li>
                                    <div class="dropdown-header text-muted small">
                                        Signed in as<br>
                                        <strong class="text-dark">${sessionScope.userEmail}</strong>
                                    </div>
                                </li>
                                <li><hr class="dropdown-divider"></li>
                                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/profile"><i class="bi bi-person me-2"></i> My Profile</a></li>
                                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/change-password"><i class="bi bi-key me-2"></i> Change Password</a></li>
                                <li><hr class="dropdown-divider"></li>
                                <li>
                                    <a class="dropdown-item text-danger" href="${pageContext.request.contextPath}/logout">
                                        <i class="bi bi-box-arrow-right me-2"></i> Logout
                                    </a>
                                </li>
                            </ul>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <li class="nav-item me-2">
                            <a class="btn btn-outline-primary btn-sm px-3" href="${pageContext.request.contextPath}/login.jsp">
                                <i class="bi bi-box-arrow-in-right me-1"></i> Login
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="btn btn-primary btn-sm px-3" href="${pageContext.request.contextPath}/register.jsp">
                                <i class="bi bi-person-plus me-1"></i> Register
                            </a>
                        </li>
                    </c:otherwise>
                </c:choose>
            </ul>
        </div>
    </div>
</nav>
