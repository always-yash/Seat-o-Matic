<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Sign In - Campus Lost & Found Portal"/>
</jsp:include>
<jsp:include page="/includes/alerts.jsp"/>

<main class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-8 col-lg-5">

            <!-- Portal Header -->
            <div class="text-center mb-4">
                <a href="${pageContext.request.contextPath}/index.jsp" class="d-inline-flex align-items-center gap-2 text-decoration-none text-primary mb-2">
                    <i class="bi bi-box-seam-fill fs-2"></i>
                    <span class="fs-4 fw-bold">Campus Lost &amp; Found</span>
                </a>
                <p class="text-muted small">Centralized Campus Property &amp; Claim Recovery Network</p>
            </div>

            <!-- Login Card -->
            <div class="card shadow border-0 rounded-4 overflow-hidden">
                <!-- Top Brand Header Banner -->
                <div class="card-header bg-white border-bottom p-4 text-center">
                    <div class="rounded-circle bg-primary-subtle text-primary mx-auto d-flex align-items-center justify-content-center mb-2" style="width: 54px; height: 54px;">
                        <i class="bi bi-shield-lock-fill fs-4"></i>
                    </div>
                    <h4 class="fw-bold text-dark mb-1">Welcome to Campus Portal</h4>
                    <p class="text-muted small mb-0">Sign in to access lost &amp; found listings, claims, and verified handovers</p>
                </div>

                <div class="card-body p-4 p-md-5">

                    <!-- Google & Gmail Fast Sign-In -->
                    <div class="d-grid gap-2 mb-3">
                        <button type="button" class="btn btn-outline-dark py-2 d-flex align-items-center justify-content-center gap-2 shadow-sm rounded-3" onclick="openGoogleAuthModal()">
                            <svg width="20" height="20" viewBox="0 0 24 24">
                                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"/>
                                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"/>
                            </svg>
                            <span class="fw-semibold">Sign in with Gmail / Google</span>
                        </button>
                    </div>

                    <!-- Google Identity Services (GIS) One-Tap / Button Container -->
                    <c:set var="gClientId" value="<%= com.lostfound.config.AppConfig.getGoogleClientId() %>"/>
                    <c:if test="${not empty gClientId}">
                        <div id="g_id_onload"
                             data-client_id="${gClientId}"
                             data-context="signin"
                             data-ux_mode="popup"
                             data-callback="handleGoogleCredentialResponse"
                             data-auto_prompt="false">
                        </div>
                        <div class="g_id_signin d-flex justify-content-center mb-3"
                             data-type="standard"
                             data-shape="rectangular"
                             data-theme="outline"
                             data-text="signin_with"
                             data-size="large"
                             data-logo_alignment="left">
                        </div>
                    </c:if>

                    <div class="position-relative text-center my-4">
                        <hr class="text-muted">
                        <span class="position-absolute top-50 start-50 translate-middle bg-white px-3 text-muted small fw-medium">
                            or choose sign-in method
                        </span>
                    </div>

                    <!-- Navigation Tabs: Password Login vs OTP Login -->
                    <ul class="nav nav-pills nav-fill mb-4 p-1 bg-light rounded-3" id="authTabs" role="tablist">
                        <li class="nav-item" role="presentation">
                            <button class="nav-link ${empty otpSent ? 'active' : ''} fw-semibold" id="password-tab"
                                    data-bs-toggle="pill" data-bs-target="#password-pane" type="button" role="tab">
                                <i class="bi bi-key me-1"></i> Password Login
                            </button>
                        </li>
                        <li class="nav-item" role="presentation">
                            <button class="nav-link ${not empty otpSent ? 'active' : ''} fw-semibold" id="otp-tab"
                                    data-bs-toggle="pill" data-bs-target="#otp-pane" type="button" role="tab">
                                <i class="bi bi-envelope-check me-1"></i> Login with OTP
                            </button>
                        </li>
                    </ul>

                    <div class="tab-content" id="authTabsContent">

                        <!-- TAB 1: PASSWORD LOGIN -->
                        <div class="tab-pane fade ${empty otpSent ? 'show active' : ''}" id="password-pane" role="tabpanel">
                            <form action="${pageContext.request.contextPath}/login" method="post" id="passwordLoginForm">
                                <div class="mb-3">
                                    <label for="email" class="form-label fw-semibold">Email Address</label>
                                    <div class="input-group">
                                        <span class="input-group-text bg-light"><i class="bi bi-envelope"></i></span>
                                        <input type="email" class="form-control" id="email" name="email" value="${email}"
                                               placeholder="student@college.com" required autocomplete="username">
                                    </div>
                                </div>

                                <div class="mb-3">
                                    <div class="d-flex justify-content-between align-items-center mb-1">
                                        <label for="password" class="form-label fw-semibold mb-0">Password</label>
                                        <a href="${pageContext.request.contextPath}/forgot-password" class="small text-primary text-decoration-none">
                                            Forgot password?
                                        </a>
                                    </div>
                                    <div class="input-group">
                                        <span class="input-group-text bg-light"><i class="bi bi-lock"></i></span>
                                        <input type="password" class="form-control" id="password" name="password"
                                               placeholder="Enter password" required autocomplete="current-password">
                                        <button class="btn btn-outline-secondary" type="button" onclick="togglePasswordVisibility('password', this)">
                                            <i class="bi bi-eye"></i>
                                        </button>
                                    </div>
                                </div>

                                <div class="d-grid gap-2 mb-3">
                                    <button type="submit" class="btn btn-primary py-2 fw-semibold">
                                        <i class="bi bi-box-arrow-in-right me-1"></i> Sign In to Portal
                                    </button>
                                </div>
                            </form>

                            <!-- Development Quick Fill Credentials -->
                            <div class="p-3 bg-light rounded-3 small border mt-4">
                                <div class="fw-bold text-dark mb-2"><i class="bi bi-code-slash text-primary me-1"></i> Quick Test Credentials:</div>
                                <div class="d-flex flex-wrap gap-2">
                                    <button type="button" class="btn btn-sm btn-outline-secondary" onclick="fillCreds('admin@college.com', 'Admin@123')">
                                        Admin (Admin@123)
                                    </button>
                                    <button type="button" class="btn btn-sm btn-outline-secondary" onclick="fillCreds('student@college.com', 'Student@123')">
                                        Student (Student@123)
                                    </button>
                                    <button type="button" class="btn btn-sm btn-outline-secondary" onclick="fillCreds('faculty@college.com', 'Faculty@123')">
                                        Faculty (Faculty@123)
                                    </button>
                                </div>
                            </div>
                        </div>

                        <!-- TAB 2: EMAIL & OTP LOGIN -->
                        <div class="tab-pane fade ${not empty otpSent ? 'show active' : ''}" id="otp-pane" role="tabpanel">
                            <div class="small text-muted mb-3">
                                <i class="bi bi-shield-check text-success me-1"></i>
                                Passwordless Sign-In: Enter your email to receive a secure 6-digit login OTP code.
                            </div>

                            <!-- OTP Form -->
                            <form action="${pageContext.request.contextPath}/auth/otp" method="post" id="otpLoginForm">
                                <input type="hidden" name="action" id="otpAction" value="${not empty otpSent ? 'verify' : 'send'}">

                                <!-- Email Input -->
                                <div class="mb-3">
                                    <label for="otpEmail" class="form-label fw-semibold">Email Address</label>
                                    <div class="input-group">
                                        <span class="input-group-text bg-light"><i class="bi bi-envelope-at"></i></span>
                                        <input type="email" class="form-control" id="otpEmail" name="email" value="${email}"
                                               placeholder="student@college.com" required autocomplete="email">
                                        <button class="btn btn-outline-primary" type="button" id="btnSendOtp" onclick="sendOtpAjax()">
                                            <span id="sendOtpSpinner" class="spinner-border spinner-border-sm d-none" role="status"></span>
                                            <span id="sendOtpText">${not empty otpSent ? 'Resend OTP' : 'Send OTP'}</span>
                                        </button>
                                    </div>
                                    <div id="otpStatusMsg" class="form-text small ${not empty info ? 'text-success' : ''}">
                                        ${not empty info ? info : 'We will send a 6-digit verification code to this address.'}
                                    </div>
                                </div>

                                <!-- OTP Code Input -->
                                <div class="mb-3" id="otpInputSection" style="${not empty otpSent ? '' : 'display:none;'}">
                                    <label for="otpCode" class="form-label fw-semibold">6-Digit Verification Code</label>
                                    <div class="input-group">
                                        <span class="input-group-text bg-light"><i class="bi bi-shield-lock"></i></span>
                                        <input type="text" class="form-control form-control-lg text-center fw-bold letter-spacing-2"
                                               id="otpCode" name="otp" value="${devOtp}" placeholder="000000" maxlength="6" pattern="[0-9]{6}">
                                    </div>
                                    <div class="d-flex justify-content-between align-items-center mt-1">
                                        <span class="form-text small text-muted">Valid for 5 minutes.</span>
                                        <span id="otpCountdown" class="small text-muted fw-semibold"></span>
                                    </div>
                                </div>

                                <div class="d-grid gap-2 mb-3">
                                    <button type="button" class="btn btn-success py-2 fw-semibold" id="btnVerifyOtp"
                                            style="${not empty otpSent ? '' : 'display:none;'}" onclick="verifyOtpAjax()">
                                        <i class="bi bi-check2-circle me-1"></i> Verify OTP &amp; Access Portal
                                    </button>
                                </div>
                            </form>

                            <!-- Quick test fill for OTP -->
                            <div class="p-3 bg-light rounded-3 small border mt-3">
                                <div class="fw-bold text-dark mb-2"><i class="bi bi-lightning-charge text-primary me-1"></i> Quick Test OTP Accounts:</div>
                                <div class="d-flex flex-wrap gap-2">
                                    <button type="button" class="btn btn-sm btn-outline-secondary" onclick="fillOtpEmail('student@college.com')">
                                        student@college.com
                                    </button>
                                    <button type="button" class="btn btn-sm btn-outline-secondary" onclick="fillOtpEmail('admin@college.com')">
                                        admin@college.com
                                    </button>
                                    <button type="button" class="btn btn-sm btn-outline-secondary" onclick="fillOtpEmail('aarav.sharma@college.com')">
                                        aarav.sharma@college.com
                                    </button>
                                </div>
                            </div>
                        </div>

                    </div>

                    <hr class="my-4">

                    <!-- Registration link -->
                    <div class="text-center small text-muted">
                        Don't have a campus account yet?
                        <a href="${pageContext.request.contextPath}/register.jsp" class="text-primary fw-semibold text-decoration-none">
                            Register here <i class="bi bi-arrow-right"></i>
                        </a>
                    </div>

                </div>
            </div>

            <!-- Dev mode toast for instant OTP inspection -->
            <c:if test="${not empty devOtp}">
                <div class="toast-container position-fixed bottom-0 end-0 p-3">
                    <div class="toast show border-success shadow" role="alert" aria-live="assertive" aria-atomic="true">
                        <div class="toast-header bg-success text-white">
                            <i class="bi bi-key-fill me-2"></i>
                            <strong class="me-auto">Login OTP Dispatched</strong>
                            <small>Dev Mode</small>
                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="toast" aria-label="Close"></button>
                        </div>
                        <div class="toast-body">
                            6-Digit Login Code: <span class="badge bg-primary fs-6">${devOtp}</span>
                            <div class="mt-2 pt-2 border-top">
                                <button type="button" class="btn btn-sm btn-success" onclick="applyDevOtp('${devOtp}')">
                                    Auto-fill OTP
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </c:if>

        </div>
    </div>
</main>

<!-- Hidden form for Google Auth submission -->
<form id="googleAuthForm" action="${pageContext.request.contextPath}/auth/google" method="post" style="display:none;">
    <input type="hidden" name="credential" id="googleCredential">
    <input type="hidden" name="email" id="googleEmail">
    <input type="hidden" name="name" id="googleName">
</form>

<!-- Gmail Fast Sign-In Modal -->
<div class="modal fade" id="googleModal" tabindex="-1" aria-labelledby="googleModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow rounded-4">
            <div class="modal-header border-0 pb-0">
                <h5 class="modal-title fw-bold" id="googleModalLabel">
                    <svg width="24" height="24" viewBox="0 0 24 24" class="me-2">
                        <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                        <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                        <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"/>
                        <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"/>
                    </svg>
                    Sign In with Google / Gmail
                </h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body p-4">
                <p class="text-muted small mb-3">
                    Authenticate instantly with your Google or Gmail account. If you do not have an existing campus profile, one will automatically be created for you with zero configuration!
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
                        <i class="bi bi-box-arrow-in-right me-1"></i> Continue to Portal
                    </button>
                </div>
                <hr class="my-3">
                <div class="small text-muted mb-2 fw-semibold">Or select a quick 1-click Google account:</div>
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
var countdownInterval = null;

function fillCreds(email, pass) {
    document.getElementById('email').value = email;
    document.getElementById('password').value = pass;
}

function fillOtpEmail(email) {
    document.getElementById('otpEmail').value = email;
}

function applyDevOtp(code) {
    var otpInput = document.getElementById('otpCode');
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

// Google Identity Services (GIS) token handler
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

// AJAX Send OTP handler
function sendOtpAjax() {
    var email = document.getElementById('otpEmail').value;
    if (!email || !email.includes('@')) {
        alert('Please enter a valid email address.');
        return;
    }

    var btn = document.getElementById('btnSendOtp');
    var spinner = document.getElementById('sendOtpSpinner');
    var text = document.getElementById('sendOtpText');
    var statusMsg = document.getElementById('otpStatusMsg');

    btn.disabled = true;
    spinner.classList.remove('d-none');
    text.textContent = 'Sending...';

    var params = new URLSearchParams();
    params.append('action', 'send');
    params.append('email', email);
    params.append('ajax', 'true');

    fetch('${pageContext.request.contextPath}/auth/otp', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-Requested-With': 'true'
        },
        body: params.toString()
    })
    .then(function(res) { return res.json(); })
    .then(function(data) {
        btn.disabled = false;
        spinner.classList.add('d-none');
        text.textContent = 'Resend OTP';

        if (data.success) {
            statusMsg.className = 'form-text small text-success';
            statusMsg.innerHTML = '<i class="bi bi-check-circle me-1"></i> ' + data.message;

            document.getElementById('otpInputSection').style.display = 'block';
            document.getElementById('btnVerifyOtp').style.display = 'block';
            document.getElementById('otpAction').value = 'verify';

            // Auto-fill dev code if available
            if (data.data && data.data.devOtp) {
                document.getElementById('otpCode').value = data.data.devOtp;
                showDevToast(data.data.devOtp);
            }

            startOtpCountdown(60);
        } else {
            statusMsg.className = 'form-text small text-danger';
            statusMsg.innerHTML = '<i class="bi bi-exclamation-circle me-1"></i> ' + data.message;
        }
    })
    .catch(function(err) {
        btn.disabled = false;
        spinner.classList.add('d-none');
        text.textContent = 'Resend OTP';
        statusMsg.className = 'form-text small text-danger';
        statusMsg.textContent = 'Network error while sending OTP. Please try again.';
    });
}

// AJAX Verify OTP handler
function verifyOtpAjax() {
    var email = document.getElementById('otpEmail').value;
    var otp = document.getElementById('otpCode').value;

    if (!otp || otp.trim().length !== 6) {
        alert('Please enter the 6-digit OTP verification code.');
        return;
    }

    var btn = document.getElementById('btnVerifyOtp');
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Verifying...';

    var params = new URLSearchParams();
    params.append('action', 'verify');
    params.append('email', email);
    params.append('otp', otp);
    params.append('ajax', 'true');

    fetch('${pageContext.request.contextPath}/auth/otp', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-Requested-With': 'true'
        },
        body: params.toString()
    })
    .then(function(res) { return res.json(); })
    .then(function(data) {
        if (data.success) {
            btn.className = 'btn btn-success py-2 fw-semibold';
            btn.innerHTML = '<i class="bi bi-check-circle-fill me-1"></i> Success! Redirecting...';
            window.location.href = data.data.redirectUrl;
        } else {
            btn.disabled = false;
            btn.className = 'btn btn-success py-2 fw-semibold';
            btn.innerHTML = '<i class="bi bi-check2-circle me-1"></i> Verify OTP & Access Portal';
            alert('Verification Error: ' + data.message);
        }
    })
    .catch(function(err) {
        btn.disabled = false;
        btn.className = 'btn btn-success py-2 fw-semibold';
        btn.innerHTML = '<i class="bi bi-check2-circle me-1"></i> Verify OTP & Access Portal';
        alert('Network error while verifying OTP.');
    });
}

function startOtpCountdown(seconds) {
    if (countdownInterval) clearInterval(countdownInterval);
    var countdownEl = document.getElementById('otpCountdown');
    var btn = document.getElementById('btnSendOtp');
    var text = document.getElementById('sendOtpText');

    btn.disabled = true;
    var remaining = seconds;
    countdownEl.textContent = 'Resend in ' + remaining + 's';

    countdownInterval = setInterval(function() {
        remaining--;
        if (remaining <= 0) {
            clearInterval(countdownInterval);
            countdownEl.textContent = '';
            btn.disabled = false;
            text.textContent = 'Resend OTP';
        } else {
            countdownEl.textContent = 'Resend in ' + remaining + 's';
        }
    }, 1000);
}

function showDevToast(code) {
    var existingToast = document.querySelector('.toast-container');
    if (!existingToast) {
        var container = document.createElement('div');
        container.className = 'toast-container position-fixed bottom-0 end-0 p-3';
        container.innerHTML = 
            '<div class="toast show border-success shadow" role="alert">' +
                '<div class="toast-header bg-success text-white">' +
                    '<i class="bi bi-key-fill me-2"></i><strong class="me-auto">Dev Environment Notice</strong><button type="button" class="btn-close btn-close-white" data-bs-dismiss="toast"></button>' +
                '</div>' +
                '<div class="toast-body">' +
                    'Generated 6-Digit Login Code: <span class="badge bg-primary fs-6">' + code + '</span>' +
                '</div>' +
            '</div>';
        document.body.appendChild(container);
    }
}
</script>

<jsp:include page="/includes/footer.jsp"/>
