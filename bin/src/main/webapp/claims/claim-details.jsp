<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Claim #${claim.claimId} Details - Campus Lost & Found"/>
</jsp:include>
<jsp:include page="/includes/navbar.jsp"/>
<jsp:include page="/includes/alerts.jsp"/>

<main class="container py-4">
    <div class="row g-4">
        <!-- Main Details Column -->
        <div class="col-lg-8">
            <div class="card shadow-sm border-0 mb-4">
                <div class="card-header bg-white py-3 border-bottom d-flex align-items-center justify-content-between">
                    <div>
                        <span class="text-muted small">Claim ID</span>
                        <h5 class="fw-bold mb-0 text-dark">#${claim.claimId}</h5>
                    </div>
                    <span class="badge ${claim.status eq 'APPROVED' ? 'badge-approved' : (claim.status eq 'COMPLETED' ? 'badge-returned' : (claim.status eq 'REJECTED' ? 'badge-rejected' : (claim.status eq 'VERIFIED' ? 'badge-matched' : 'badge-pending')))} fs-6 px-3 py-2">
                        STATUS: ${claim.status}
                    </span>
                </div>
                <div class="card-body p-4">
                    <!-- Item Overview -->
                    <h6 class="fw-bold text-dark mb-2">Claimed Item</h6>
                    <div class="card bg-light border-0 p-3 mb-4">
                        <div class="d-flex align-items-center gap-3">
                            <c:choose>
                                <c:when test="${not empty item.image}">
                                    <img src="${pageContext.request.contextPath}/${item.image}" class="rounded" style="width: 64px; height: 64px; object-fit: cover;" alt="" onerror="this.src='https://placehold.co/64x64/e2e8f0/475569?text=Item'">
                                </c:when>
                                <c:otherwise>
                                    <div class="rounded bg-white d-flex align-items-center justify-content-center text-muted border" style="width: 64px; height: 64px;">
                                        <i class="bi bi-image fs-3"></i>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                            <div>
                                <h6 class="fw-bold mb-1">
                                    <a href="${pageContext.request.contextPath}/items?action=view&id=${item.itemId}" class="text-dark text-decoration-none">
                                        ${item.title}
                                    </a>
                                </h6>
                                <div class="small text-muted">
                                    <span class="badge ${item.itemType eq 'LOST' ? 'badge-lost' : 'badge-found'} me-1">${item.itemType}</span>
                                    <i class="bi bi-geo-alt text-danger me-1"></i>${item.location} &bull;
                                    <i class="bi bi-calendar3 me-1"></i>${item.itemDate}
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Claimant Info -->
                    <h6 class="fw-bold text-dark mb-2">Claimant Profile</h6>
                    <div class="row g-2 p-3 bg-light rounded mb-4 small">
                        <div class="col-sm-6">
                            <span class="text-muted">Name:</span> <strong class="text-dark">${claim.claimantName}</strong>
                        </div>
                        <div class="col-sm-6">
                            <span class="text-muted">Email:</span> <strong class="text-dark">${claim.claimantEmail}</strong>
                        </div>
                        <div class="col-sm-6">
                            <span class="text-muted">Phone:</span> <strong class="text-dark">${not empty claim.claimantPhone ? claim.claimantPhone : 'N/A'}</strong>
                        </div>
                        <div class="col-sm-6">
                            <span class="text-muted">Date Filed:</span> <strong class="text-dark">${claim.createdAt}</strong>
                        </div>
                    </div>

                    <!-- Reason -->
                    <h6 class="fw-bold text-dark mb-2">Claim Reason</h6>
                    <p class="text-secondary p-3 bg-light rounded mb-4">${claim.reason}</p>

                    <!-- Proof Description -->
                    <h6 class="fw-bold text-dark mb-2">Proof of Ownership Description</h6>
                    <div class="p-3 bg-light rounded border border-warning-subtle mb-4">
                        <div class="small text-warning-emphasis fw-bold mb-1"><i class="bi bi-shield-lock-fill me-1"></i> Confidential Verification Proof:</div>
                        <p class="mb-0 text-dark">${claim.proofDescription}</p>
                    </div>

                    <!-- Approval & Review Buttons (if PENDING and user is owner or ADMIN) -->
                    <c:if test="${claim.status eq 'PENDING' and (isOwner or sessionScope.userRole eq 'ADMIN')}">
                        <div class="p-3 bg-light rounded border d-flex flex-wrap gap-2 align-items-center justify-content-between">
                            <div>
                                <span class="fw-bold text-dark">Action Required:</span>
                                <div class="small text-muted">Review claimant proof above before making a decision.</div>
                            </div>
                            <div class="d-flex gap-2">
                                <form action="${pageContext.request.contextPath}/claims" method="post" onsubmit="return confirm('Approve this claim? This will generate a verification OTP and QR code.');">
                                    <input type="hidden" name="action" value="approve">
                                    <input type="hidden" name="claimId" value="${claim.claimId}">
                                    <button type="submit" class="btn btn-success">
                                        <i class="bi bi-check-lg me-1"></i> Approve Claim
                                    </button>
                                </form>
                                <form action="${pageContext.request.contextPath}/claims" method="post" onsubmit="return confirm('Reject this claim?');">
                                    <input type="hidden" name="action" value="reject">
                                    <input type="hidden" name="claimId" value="${claim.claimId}">
                                    <button type="submit" class="btn btn-outline-danger">
                                        <i class="bi bi-x-lg me-1"></i> Reject Claim
                                    </button>
                                </form>
                            </div>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>

        <!-- Right: Verification & QR Column -->
        <div class="col-lg-4">
            <div class="card shadow-sm border-0 mb-4">
                <div class="card-header bg-white py-3 border-bottom">
                    <h6 class="fw-bold mb-0"><i class="bi bi-qr-code text-primary me-2"></i>Verification Tokens</h6>
                </div>
                <div class="card-body p-4 text-center">
                    <c:choose>
                        <c:when test="${claim.status eq 'APPROVED' or claim.status eq 'VERIFIED' or claim.status eq 'COMPLETED'}">
                            <!-- QR Code Preview -->
                            <c:if test="${not empty qrBase64}">
                                <div class="p-3 bg-light rounded d-inline-block border mb-3">
                                    <img src="${qrBase64}" alt="Claim QR Code" class="img-fluid" style="width: 200px; height: 200px;">
                                </div>
                                <div class="small text-muted mb-3 d-flex align-items-center justify-content-center gap-2">
                                    Token: <code class="text-dark">${claim.qrToken}</code>
                                    <button class="btn btn-sm btn-outline-secondary py-0 px-2" type="button" title="Copy QR Token"
                                            onclick="navigator.clipboard.writeText('${claim.qrToken}'); alert('QR Token copied to clipboard!');">
                                        <i class="bi bi-copy"></i>
                                    </button>
                                </div>
                            </c:if>

                            <!-- OTP Details (Development display) -->
                            <c:if test="${not empty claim.otp}">
                                <div class="p-3 bg-success-subtle rounded border border-success-subtle mb-3">
                                    <div class="small text-success fw-bold">6-Digit Verification OTP</div>
                                    <div class="display-6 fw-bold text-success my-1 tracking-wider">${claim.otp}</div>
                                    <div class="small text-muted mb-2">Expires: ${claim.otpExpiry}</div>
                                    <button class="btn btn-sm btn-outline-success" type="button"
                                            onclick="navigator.clipboard.writeText('${claim.otp}'); alert('OTP copied to clipboard!');">
                                        <i class="bi bi-copy me-1"></i> Copy OTP
                                    </button>
                                </div>
                            </c:if>

                            <!-- Verification / Handover CTA -->
                            <c:if test="${claim.status eq 'APPROVED'}">
                                <a href="${pageContext.request.contextPath}/claims/verify?claimId=${claim.claimId}" class="btn btn-primary w-100 mb-2">
                                    <i class="bi bi-check-circle me-1"></i> Verify OTP / QR Token
                                </a>
                            </c:if>
                            <c:if test="${claim.status eq 'VERIFIED'}">
                                <div class="alert alert-success small mb-3">
                                    <i class="bi bi-check-circle-fill me-1"></i> Token Verified! Proceed with item transfer.
                                </div>
                                <a href="${pageContext.request.contextPath}/claims/handover?claimId=${claim.claimId}" class="btn btn-success w-100">
                                    <i class="bi bi-hand-thumbs-up me-1"></i> Complete Handover
                                </a>
                            </c:if>
                            <c:if test="${claim.status eq 'COMPLETED'}">
                                <div class="alert alert-info small mb-0">
                                    <i class="bi bi-check-all me-1"></i> Handover finalized. Item returned to owner.
                                </div>
                            </c:if>
                        </c:when>
                        <c:when test="${claim.status eq 'REJECTED'}">
                            <div class="text-danger py-4">
                                <i class="bi bi-x-circle fs-1"></i>
                                <div class="fw-bold mt-2">Claim Rejected</div>
                                <div class="small text-muted">Ownership proof did not match or satisfy verification requirements.</div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="text-muted py-4">
                                <i class="bi bi-hourglass-split fs-1 text-warning"></i>
                                <div class="fw-bold mt-2">Pending Owner / Admin Review</div>
                                <div class="small">Verification OTP and QR code will be generated once approved.</div>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </div>
</main>

<jsp:include page="/includes/footer.jsp"/>
