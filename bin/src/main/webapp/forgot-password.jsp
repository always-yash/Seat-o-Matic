<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Forgot Password - Campus Lost & Found"/>
</jsp:include>
<jsp:include page="/includes/alerts.jsp"/>

<main class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-8 col-lg-5">

            <!-- Brand Header -->
            <div class="text-center mb-4">
                <a href="${pageContext.request.contextPath}/index.jsp" class="d-inline-flex align-items-center gap-2 text-decoration-none text-primary mb-2">
                    <i class="bi bi-box-seam-fill fs-3"></i>
                    <span class="fs-4 fw-bold">Campus Lost &amp; Found</span>
                </a>
                <p class="text-muted small">Account Recovery &amp; Password Reset Portal</p>
            </div>

            <div class="card shadow border-0 rounded-4 overflow-hidden">
                <div class="card-header bg-primary text-white p-4 text-center">
                    <div class="rounded-circle bg-white text-primary mx-auto d-flex align-items-center justify-content-center mb-2 shadow-sm" style="width: 56px; height: 56px;">
                        <i class="bi bi-key-fill fs-3"></i>
                    </div>
                    <h4 class="fw-bold mb-1">Reset Your Password</h4>
                    <p class="mb-0 text-white-50 small">Verify your identity with a secure 6-digit email OTP</p>
                </div>

                <div class="card-body p-4 p-md-5">

                    <!-- Error Alert -->
                    <c:if test="${not empty error}">
                        <div class="alert alert-danger alert-dismissible fade show d-flex align-items-center mb-4" role="alert">
                            <i class="bi bi-exclamation-triangle-fill fs-5 me-2"></i>
                            <div>${error}</div>
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                        </div>
                    </c:if>

                    <!-- Info Alert -->
                    <c:if test="${not empty info}">
                        <div class="alert alert-info alert-dismissible fade show d-flex align-items-center mb-4" role="alert">
                            <i class="bi bi-info-circle-fill fs-5 me-2"></i>
                            <div>${info}</div>
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                        </div>
                    </c:if>

                    <!-- STEP 1: Request Recovery OTP -->
                    <c:choose>
                        <c:when test="${step eq 'verify'}">
                            <!-- STEP 2: Verify OTP and Enter New Password -->
                            <div class="text-center mb-4">
                                <span class="badge bg-success-subtle text-success px-3 py-2 rounded-pill fw-semibold mb-2">
                                    <i class="bi bi-shield-check me-1"></i> Step 2 of 2: New Password
                                </span>
                                <h5 class="fw-bold text-dark">Enter OTP &amp; New Password</h5>
                                <p class="text-muted small mb-0">We sent a 6-digit recovery code to <strong>${email}</strong></p>
                            </div>

                            <form action="${pageContext.request.contextPath}/forgot-password" method="post" id="resetPasswordForm">
                                <input type="hidden" name="action" value="reset">
                                <input type="hidden" name="email" value="${email}">

                                <!-- 6-Digit OTP -->
                                <div class="mb-3">
                                    <label for="otp" class="form-label fw-semibold">6-Digit Verification Code</label>
                                    <div class="input-group">
                                        <span class="input-group-text bg-light"><i class="bi bi-shield-lock"></i></span>
                                        <input type="text" class="form-control form-control-lg text-center fw-bold letter-spacing-2"
                                               id="otp" name="otp" value="${devOtp}" placeholder="000000" maxlength="6" pattern="[0-9]{6}" required autocomplete="one-time-code">
                                    </div>
                                    <div class="form-text small">Valid for 10 minutes from dispatch.</div>
                                </div>

                                <!-- New Password -->
                                <div class="mb-3">
                                    <label for="newPassword" class="form-label fw-semibold">New Password</label>
                                    <div class="input-group">
                                        <span class="input-group-text bg-light"><i class="bi bi-lock"></i></span>
                                        <input type="password" class="form-control" id="newPassword" name="newPassword"
                                               placeholder="At least 8 characters" required minlength="8" autocomplete="new-password">
                                        <button class="btn btn-outline-secondary" type="button" onclick="togglePasswordVisibility('newPassword', this)">
                                            <i class="bi bi-eye"></i>
                                        </button>
                                    </div>
                                    <div class="form-text small">Must contain letters and numbers/symbols.</div>
                                </div>

                                <!-- Confirm New Password -->
                                <div class="mb-4">
                                    <label for="confirmPassword" class="form-label fw-semibold">Confirm New Password</label>
                                    <div class="input-group">
                                        <span class="input-group-text bg-light"><i class="bi bi-lock-fill"></i></span>
                                        <input type="password" class="form-control" id="confirmPassword" name="confirmPassword"
                                               placeholder="Re-enter new password" required minlength="8" autocomplete="new-password">
                                        <button class="btn btn-outline-secondary" type="button" onclick="togglePasswordVisibility('confirmPassword', this)">
                                            <i class="bi bi-eye"></i>
                                        </button>
                                    </div>
                                </div>

                                <div class="d-grid gap-2 mb-3">
                                    <button type="submit" class="btn btn-primary py-2 fw-semibold">
                                        <i class="bi bi-check-circle-fill me-1"></i> Update Password &amp; Sign In
                                    </button>
                                </div>
                            </form>

                            <!-- Resend code trigger -->
                            <div class="text-center pt-2">
                                <form action="${pageContext.request.contextPath}/forgot-password" method="post" class="d-inline">
                                    <input type="hidden" name="action" value="send-otp">
                                    <input type="hidden" name="email" value="${email}">
                                    <button type="submit" class="btn btn-link text-decoration-none small text-muted p-0">
                                        <i class="bi bi-arrow-repeat me-1"></i> Didn't get code? Resend OTP
                                    </button>
                                </form>
                            </div>
                        </c:when>

                        <c:otherwise>
                            <!-- STEP 1: Enter Email -->
                            <div class="text-center mb-4">
                                <span class="badge bg-primary-subtle text-primary px-3 py-2 rounded-pill fw-semibold mb-2">
                                    <i class="bi bi-envelope me-1"></i> Step 1 of 2: Identity Verification
                                </span>
                                <h5 class="fw-bold text-dark">Find Your Account</h5>
                                <p class="text-muted small mb-0">Enter your college email address to receive a 6-digit recovery code.</p>
                            </div>

                            <form action="${pageContext.request.contextPath}/forgot-password" method="post" id="requestOtpForm">
                                <input type="hidden" name="action" value="send-otp">

                                <div class="mb-4">
                                    <label for="email" class="form-label fw-semibold">Registered Email Address</label>
                                    <div class="input-group">
                                        <span class="input-group-text bg-light"><i class="bi bi-envelope-at"></i></span>
                                        <input type="email" class="form-control" id="email" name="email" value="${email}"
                                               placeholder="e.g. student@college.com" required autocomplete="email" autofocus>
                                    </div>
                                    <div class="form-text small">Enter the email linked to your student or faculty account.</div>
                                </div>

                                <div class="d-grid gap-2 mb-3">
                                    <button type="submit" class="btn btn-primary py-2 fw-semibold">
                                        <i class="bi bi-send-fill me-1"></i> Send Recovery Code
                                    </button>
                                </div>
                            </form>

                            <!-- Quick test helper for development -->
                            <div class="p-3 bg-light rounded-3 border small mb-3">
                                <div class="fw-bold text-dark mb-1"><i class="bi bi-magic text-primary me-1"></i> Quick Test Registered Accounts:</div>
                                <div class="d-flex flex-wrap gap-2">
                                    <button type="button" class="btn btn-sm btn-outline-secondary" onclick="fillForgotEmail('student@college.com')">
                                        student@college.com
                                    </button>
                                    <button type="button" class="btn btn-sm btn-outline-secondary" onclick="fillForgotEmail('admin@college.com')">
                                        admin@college.com
                                    </button>
                                    <button type="button" class="btn btn-sm btn-outline-secondary" onclick="fillForgotEmail('faculty@college.com')">
                                        faculty@college.com
                                    </button>
                                </div>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <hr class="my-4">

                    <div class="d-flex justify-content-between align-items-center small">
                        <a href="${pageContext.request.contextPath}/login.jsp" class="text-decoration-none fw-semibold text-primary">
                            <i class="bi bi-arrow-left me-1"></i> Back to Login
                        </a>
                        <a href="${pageContext.request.contextPath}/register.jsp" class="text-decoration-none text-muted">
                            Need a new account? Register
                        </a>
                    </div>

                </div>
            </div>

            <!-- Dev mode toast for instant OTP inspection -->
            <c:if test="${not empty devOtp}">
                <div class="toast-container position-fixed bottom-0 end-0 p-3">
                    <div class="toast show border-primary shadow" role="alert" aria-live="assertive" aria-atomic="true">
                        <div class="toast-header bg-primary text-white">
                            <i class="bi bi-code-slash me-2"></i>
                            <strong class="me-auto">Dev Environment Notice</strong>
                            <small>Just now</small>
                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="toast" aria-label="Close"></button>
                        </div>
                        <div class="toast-body">
                            Generated 6-Digit Reset OTP: <span class="badge bg-success fs-6">${devOtp}</span>
                            <div class="mt-2 pt-2 border-top">
                                <button type="button" class="btn btn-sm btn-primary" onclick="applyDevOtp('${devOtp}')">
                                    Auto-fill OTP Code
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </c:if>

        </div>
    </div>
</main>

<script>
function fillForgotEmail(email) {
    var emailInput = document.getElementById('email');
    if (emailInput) {
        emailInput.value = email;
    }
}

function applyDevOtp(code) {
    var otpInput = document.getElementById('otp');
    if (otpInput) {
        otpInput.value = code;
    }
}

function togglePasswordVisibility(fieldId, btn) {
    var field = document.getElementById(fieldId);
    if (!field) return;
    var icon = btn.querySelector('i');
    if (field.type === 'password') {
        field.type = 'text';
        icon.classList.remove('bi-eye');
        icon.classList.add('bi-eye-slash');
    } else {
        field.type = 'password';
        icon.classList.remove('bi-eye-slash');
        icon.classList.add('bi-eye');
    }
}

// Client validation for matching password
var form = document.getElementById('resetPasswordForm');
if (form) {
    form.addEventListener('submit', function(e) {
        var p1 = document.getElementById('newPassword').value;
        var p2 = document.getElementById('confirmPassword').value;
        if (p1 !== p2) {
            e.preventDefault();
            alert('Passwords do not match. Please ensure both fields are identical.');
        }
    });
}
</script>

<jsp:include page="/includes/footer.jsp"/>
