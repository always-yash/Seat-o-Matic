<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Change Password - Campus Lost & Found Portal"/>
</jsp:include>
<jsp:include page="/includes/navbar.jsp"/>
<jsp:include page="/includes/alerts.jsp"/>

<main class="container py-4">
    <div class="row justify-content-center">
        <div class="col-md-7 col-lg-5">
            <div class="card shadow-sm border-0">
                <div class="card-header bg-white py-3 border-bottom">
                    <h5 class="mb-0 fw-bold"><i class="bi bi-shield-lock-fill text-primary me-2"></i>Update Password</h5>
                </div>
                <div class="card-body p-4">
                    <form action="${pageContext.request.contextPath}/change-password" method="post">
                        <div class="mb-3">
                            <label for="currentPassword" class="form-label">Current Password <span class="text-danger">*</span></label>
                            <input type="password" class="form-control" id="currentPassword" name="currentPassword" required autocomplete="current-password">
                        </div>

                        <div class="mb-3">
                            <label for="newPassword" class="form-label">New Password <span class="text-danger">*</span></label>
                            <input type="password" class="form-control" id="newPassword" name="newPassword" required minlength="8" autocomplete="new-password">
                            <div class="form-text small">At least 8 characters, letters &amp; numbers/symbols.</div>
                        </div>

                        <div class="mb-3">
                            <label for="confirmPassword" class="form-label">Confirm New Password <span class="text-danger">*</span></label>
                            <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" required minlength="8" autocomplete="new-password">
                        </div>

                        <div class="d-flex justify-content-between align-items-center mt-4 pt-3 border-top">
                            <a href="${pageContext.request.contextPath}/profile" class="btn btn-outline-secondary btn-sm">
                                Cancel
                            </a>
                            <button type="submit" class="btn btn-primary px-4">
                                Update Password
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</main>

<jsp:include page="/includes/footer.jsp"/>
