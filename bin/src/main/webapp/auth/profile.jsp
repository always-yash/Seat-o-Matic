<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="My Profile - Campus Lost & Found Portal"/>
</jsp:include>
<jsp:include page="/includes/navbar.jsp"/>
<jsp:include page="/includes/alerts.jsp"/>

<main class="container py-4">
    <div class="row justify-content-center">
        <div class="col-lg-8">
            <div class="card shadow-sm border-0">
                <div class="card-header bg-white py-3 border-bottom d-flex align-items-center justify-content-between">
                    <h5 class="mb-0 fw-bold"><i class="bi bi-person-circle text-primary me-2"></i>User Profile</h5>
                    <span class="badge ${profileUser.role eq 'ADMIN' ? 'badge-role-admin' : (profileUser.role eq 'FACULTY' ? 'badge-role-faculty' : 'badge-role-student')}">
                        ${profileUser.role}
                    </span>
                </div>
                <div class="card-body p-4">
                    <form action="${pageContext.request.contextPath}/profile" method="post">
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label text-muted">User ID</label>
                                <input type="text" class="form-control bg-light" value="#${profileUser.userId}" readonly>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-muted">Account Status</label>
                                <div>
                                    <span class="badge ${profileUser.active ? 'bg-success' : 'bg-danger'} px-3 py-2">
                                        ${profileUser.status}
                                    </span>
                                </div>
                            </div>
                            <div class="col-md-12">
                                <label class="form-label text-muted">College Email (Permanent)</label>
                                <input type="email" class="form-control bg-light" value="${profileUser.email}" readonly>
                                <div class="form-text small">Your registered college email cannot be modified.</div>
                            </div>
                            <div class="col-md-12">
                                <label for="name" class="form-label">Full Name <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="name" name="name" value="${profileUser.name}" required minlength="2">
                            </div>
                            <div class="col-md-6">
                                <label for="phone" class="form-label">Phone Number</label>
                                <input type="tel" class="form-control" id="phone" name="phone" value="${profileUser.phone}">
                            </div>
                            <div class="col-md-6">
                                <label for="department" class="form-label">Department / Branch</label>
                                <input type="text" class="form-control" id="department" name="department" value="${profileUser.department}">
                            </div>
                            <div class="col-md-12">
                                <label class="form-label text-muted">Registered On</label>
                                <div class="text-muted small">${profileUser.createdAt}</div>
                            </div>
                        </div>

                        <div class="d-flex justify-content-between align-items-center mt-4 pt-3 border-top">
                            <a href="${pageContext.request.contextPath}/change-password" class="btn btn-outline-secondary btn-sm">
                                <i class="bi bi-key me-1"></i> Change Password
                            </a>
                            <button type="submit" class="btn btn-primary px-4">
                                <i class="bi bi-save me-1"></i> Save Changes
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</main>

<jsp:include page="/includes/footer.jsp"/>
