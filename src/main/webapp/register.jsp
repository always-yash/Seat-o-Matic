<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Register - Campus Lost & Found Portal"/>
</jsp:include>
<!-- <jsp:include page="/includes/navbar.jsp"/> -->
<jsp:include page="/includes/alerts.jsp"/>

<main class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-9 col-lg-7">

            <!-- Portal Header -->
            <div class="text-center mb-4">
                <a href="${pageContext.request.contextPath}/index.jsp" class="d-inline-flex align-items-center gap-2 text-decoration-none text-primary mb-2">
                    <i class="bi bi-box-seam-fill fs-2"></i>
                    <span class="fs-4 fw-bold">Campus Lost &amp; Found</span>
                </a>
                <p class="text-muted small">Centralized Campus Property &amp; Claim Recovery Network</p>
            </div>

            <div class="card shadow border-0 rounded-4 overflow-hidden">
                <div class="card-body p-4 p-md-5">
                    <div class="text-center mb-4">
                        <div class="rounded-circle bg-success-subtle text-success mx-auto d-flex align-items-center justify-content-center mb-2" style="width: 52px; height: 52px;">
                            <i class="bi bi-person-plus-fill fs-4"></i>
                        </div>
                        <h4 class="fw-bold">Create Campus Account</h4>
                        <p class="text-muted small">Register to report items, search campus property, and verify claims</p>
                    </div>

                    <!-- Instant Sign Up with Google / Gmail -->
                    <div class="d-grid gap-2 mb-3">
                        <button type="button" class="btn btn-outline-dark py-2 d-flex align-items-center justify-content-center gap-2 shadow-sm" onclick="openGoogleAuthModal()">
                            <svg width="20" height="20" viewBox="0 0 24 24">
                                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"/>
                                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"/>
                            </svg>
                            <span class="fw-semibold">Sign up with Gmail / Google</span>
                        </button>
                    </div>

                    <!-- Google Identity Services (GIS) Container -->
                    <c:set var="gClientId" value="<%= com.lostfound.config.AppConfig.getGoogleClientId() %>"/>
                    <c:if test="${not empty gClientId}">
                        <div id="g_id_onload"
                             data-client_id="${gClientId}"
                             data-context="signup"
                             data-ux_mode="popup"
                             data-callback="handleGoogleCredentialResponse"
                             data-auto_prompt="false">
                        </div>
                        <div class="g_id_signin d-flex justify-content-center mb-3"
                             data-type="standard"
                             data-shape="rectangular"
                             data-theme="outline"
                             data-text="signup_with"
                             data-size="large"
                             data-logo_alignment="left">
                        </div>
                    </c:if>

                    <div class="position-relative text-center my-4">
                        <hr class="text-muted">
                        <span class="position-absolute top-50 start-50 translate-middle bg-white px-3 text-muted small fw-medium">
                            or register with email
                        </span>
                    </div>

                    <form id="registerForm" action="${pageContext.request.contextPath}/register" method="post">
                        <div class="row g-3">
                            <div class="col-md-12">
                                <label for="name" class="form-label">Full Name <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="name" name="name" value="${user.name}" placeholder="e.g. Aarav Sharma" required minlength="2">
                            </div>

                            <div class="col-md-6">
                                <label for="email" class="form-label">Email Address <span class="text-danger">*</span></label>
                                <input type="email" class="form-control" id="email" name="email" value="${user.email}" placeholder="username@college.com" required>
                            </div>

                            <div class="col-md-6">
                                <label for="phone" class="form-label">Phone Number</label>
                                <input type="tel" class="form-control" id="phone" name="phone" value="${user.phone}" placeholder="+91 9876543210">
                            </div>

                            <div class="col-md-6">
                                <label for="department" class="form-label">Department / Branch</label>
                                <input type="text" class="form-control" id="department" name="department" value="${user.department}" placeholder="e.g. Computer Science">
                            </div>

                            <div class="col-md-6">
                                <label for="role" class="form-label">Role <span class="text-danger">*</span></label>
                                <select class="form-select" id="role" name="role" required>
                                    <option value="STUDENT" ${user.role eq 'STUDENT' ? 'selected' : ''}>Student</option>
                                    <option value="FACULTY" ${user.role eq 'FACULTY' ? 'selected' : ''}>Faculty / Staff</option>
                                </select>
                            </div>

                            <div class="col-md-6">
                                <label for="regPassword" class="form-label">Password <span class="text-danger">*</span></label>
                                <input type="password" class="form-control" id="regPassword" name="password" placeholder="At least 8 characters" required minlength="8" autocomplete="new-password">
                                <div class="form-text small">Must contain letters &amp; numbers/symbols.</div>
                            </div>

                            <div class="col-md-6">
                                <label for="regConfirmPassword" class="form-label">Confirm Password <span class="text-danger">*</span></label>
                                <input type="password" class="form-control" id="regConfirmPassword" name="confirmPassword" placeholder="Re-enter password" required minlength="8" autocomplete="new-password">
                            </div>
                        </div>

                        <div class="d-grid gap-2 mt-4">
                            <button type="submit" class="btn btn-primary py-2 fw-semibold">
                                <i class="bi bi-person-check-fill me-1"></i> Create Account
                            </button>
                        </div>
                    </form>

                    <div class="text-center mt-3 small text-muted">
                        Already have an account?
                        <a href="${pageContext.request.contextPath}/login.jsp" class="text-primary fw-semibold text-decoration-none">Sign in here</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</main>

<!-- Hidden form for Google Auth submission -->
<form id="googleAuthForm" action="${pageContext.request.contextPath}/auth/google" method="post" style="display:none;">
    <input type="hidden" name="credential" id="googleCredential">
    <input type="hidden" name="email" id="googleEmail">
    <input type="hidden" name="name" id="googleName">
</form>

<!-- Gmail Fast Sign-Up Modal -->
<div class="modal fade" id="googleModal" tabindex="-1" aria-labelledby="googleModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
            <div class="modal-header border-0 pb-0">
                <h5 class="modal-title fw-bold" id="googleModalLabel">
                    <i class="bi bi-google text-danger me-2"></i>Sign Up Instantly with Gmail
                </h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body p-4">
                <p class="text-muted small mb-3">
                    Skip passwords! Sign up with your Gmail address to instantly create your campus profile and start reporting or claiming items.
                </p>
                <div class="mb-3">
                    <label for="gmailInput" class="form-label fw-semibold">Enter your Gmail Address</label>
                    <div class="input-group">
                        <span class="input-group-text bg-light"><i class="bi bi-envelope-at"></i></span>
                        <input type="email" class="form-control" id="gmailInput" placeholder="username@gmail.com" required>
                    </div>
                </div>
                <div class="mb-3">
                    <label for="gmailNameInput" class="form-label fw-semibold">Your Full Name (Optional)</label>
                    <input type="text" class="form-control" id="gmailNameInput" placeholder="e.g. Aarav Sharma">
                </div>
                <div class="d-grid gap-2 mt-4">
                    <button type="button" class="btn btn-primary py-2 fw-semibold" onclick="submitDirectGmail()">
                        <i class="bi bi-check-circle me-1"></i> Register with this Gmail
                    </button>
                </div>
                <hr class="my-3">
                <div class="small text-muted mb-2 fw-semibold">Or select a quick demo Gmail account:</div>
                <div class="d-flex flex-wrap gap-2">
                    <button type="button" class="btn btn-sm btn-outline-secondary" onclick="quickGmail('aarav.sharma@gmail.com', 'Aarav Sharma')">
                        aarav.sharma@gmail.com
                    </button>
                    <button type="button" class="btn btn-sm btn-outline-secondary" onclick="quickGmail('priya.patel@gmail.com', 'Priya Patel')">
                        priya.patel@gmail.com
                    </button>
                    <button type="button" class="btn btn-sm btn-outline-secondary" onclick="quickGmail('campus.admin@gmail.com', 'Campus Admin')">
                        campus.admin@gmail.com
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://accounts.google.com/gsi/client" async defer></script>
<script>
function handleGoogleCredentialResponse(response) {
    if (response && response.credential) {
        document.getElementById('googleCredential').value = response.credential;
        document.getElementById('googleAuthForm').submit();
    }
}

function openGoogleAuthModal() {
    var modal = new bootstrap.Modal(document.getElementById('googleModal'));
    modal.show();
}

function submitDirectGmail() {
    var email = document.getElementById('gmailInput').value;
    var name = document.getElementById('gmailNameInput').value;
    if (!email || !email.includes('@')) {
        alert('Please enter a valid Gmail / Google email address.');
        return;
    }
    document.getElementById('googleEmail').value = email;
    document.getElementById('googleName').value = name;
    document.getElementById('googleAuthForm').submit();
}

function quickGmail(email, name) {
    document.getElementById('googleEmail').value = email;
    document.getElementById('googleName').value = name;
    document.getElementById('googleAuthForm').submit();
}
</script>

<jsp:include page="/includes/footer.jsp"/>
