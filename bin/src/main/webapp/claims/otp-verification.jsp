<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="OTP & QR Verification - Campus Lost & Found"/>
</jsp:include>
<jsp:include page="/includes/navbar.jsp"/>
<jsp:include page="/includes/alerts.jsp"/>

<main class="container py-4">
    <div class="row justify-content-center">
        <div class="col-md-8 col-lg-6">
            <div class="card shadow-sm border-0">
                <div class="card-header bg-white py-3 border-bottom text-center">
                    <h5 class="fw-bold mb-0 text-dark"><i class="bi bi-shield-lock-fill text-primary me-2"></i>Handover Security Verification</h5>
                    <p class="text-muted small mb-0 mt-1">Claim #${claim.claimId} &bull; Item: ${item.title}</p>
                </div>
                <div class="card-body p-4">
                    <c:if test="${not empty qrBase64}">
                        <div class="text-center mb-4">
                            <div class="p-2 bg-light d-inline-block border rounded">
                                <img src="${qrBase64}" alt="Verification QR Code" style="width: 150px; height: 150px;">
                            </div>
                            <div class="small text-muted mt-1">Scan or verify token directly</div>
                        </div>
                    </c:if>

                    <!-- Nav tabs for verification methods -->
                    <ul class="nav nav-pills nav-fill mb-3" id="verifyTab" role="tablist">
                        <li class="nav-item" role="presentation">
                            <button class="nav-link active fw-semibold" id="otp-tab" data-bs-toggle="pill" data-bs-target="#otp-pane" type="button" role="tab">
                                <i class="bi bi-123 me-1"></i> Enter OTP
                            </button>
                        </li>
                        <li class="nav-item" role="presentation">
                            <button class="nav-link fw-semibold" id="qr-tab" data-bs-toggle="pill" data-bs-target="#qr-pane" type="button" role="tab">
                                <i class="bi bi-qr-code me-1"></i> Enter QR Token
                            </button>
                        </li>
                    </ul>

                    <div class="tab-content" id="verifyTabContent">
                        <!-- OTP Pane -->
                        <div class="tab-pane fade show active" id="otp-pane" role="tabpanel">
                            <form action="${pageContext.request.contextPath}/claims/verify" method="post">
                                <input type="hidden" name="claimId" value="${claim.claimId}">
                                <input type="hidden" name="verificationType" value="OTP">

                                <div class="mb-3 text-center">
                                    <label for="otpCode" class="form-label text-muted">Enter 6-Digit Verification Code</label>
                                    <input type="text" class="form-control form-control-lg text-center fw-bold letter-spacing-2" id="otpCode" name="code" placeholder="000000" maxlength="6" pattern="[0-9]{6}" required autocomplete="one-time-code">
                                    <div class="form-text small">The OTP generated upon claim approval (valid for 5 minutes).</div>
                                </div>

                                <div class="d-grid mt-4">
                                    <button type="submit" class="btn btn-primary py-2 fw-semibold">
                                        <i class="bi bi-shield-check me-1"></i> Verify OTP Code
                                    </button>
                                </div>
                                <c:if test="${not empty claim.otp}">
                                    <div class="mt-3 text-center">
                                        <button type="button" class="btn btn-sm btn-outline-primary" onclick="document.getElementById('otpCode').value = '${claim.otp}'">
                                            <i class="bi bi-magic me-1"></i> Quick Test OTP: <strong>${claim.otp}</strong>
                                        </button>
                                    </div>
                                </c:if>
                            </form>
                        </div>

                        <!-- QR Token Pane -->
                        <div class="tab-pane fade" id="qr-pane" role="tabpanel">
                            <form action="${pageContext.request.contextPath}/claims/verify" method="post">
                                <input type="hidden" name="claimId" value="${claim.claimId}">
                                <input type="hidden" name="verificationType" value="QR">

                                <div class="mb-3">
                                    <label for="qrCodeInput" class="form-label text-muted">Scan or Paste QR Token</label>
                                    <input type="text" class="form-control" id="qrCodeInput" name="code" placeholder="QR-CLAIM-..." required>
                                    <div class="form-text small">Enter the alphanumeric token encoded in the recipient's claim QR code.</div>
                                </div>

                                <div class="d-grid mt-4">
                                    <button type="submit" class="btn btn-success py-2 fw-semibold">
                                        <i class="bi bi-qr-code-scan me-1"></i> Verify QR Token
                                    </button>
                                </div>
                                <c:if test="${not empty claim.qrToken}">
                                    <div class="mt-3 text-center">
                                        <button type="button" class="btn btn-sm btn-outline-success" onclick="document.getElementById('qrCodeInput').value = '${claim.qrToken}'">
                                            <i class="bi bi-magic me-1"></i> Quick Test QR Token: <strong>${claim.qrToken}</strong>
                                        </button>
                                    </div>
                                </c:if>
                            </form>
                        </div>
                    </div>

                    <div class="text-center mt-4 pt-3 border-top">
                        <a href="${pageContext.request.contextPath}/claims?action=details&id=${claim.claimId}" class="text-muted small text-decoration-none">
                            <i class="bi bi-arrow-left me-1"></i> Back to Claim Details
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</main>

<jsp:include page="/includes/footer.jsp"/>
