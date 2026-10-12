<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Finalize Handover - Campus Lost & Found"/>
</jsp:include>
<jsp:include page="/includes/navbar.jsp"/>
<jsp:include page="/includes/alerts.jsp"/>

<main class="container py-4">
    <div class="row justify-content-center">
        <div class="col-lg-7">
            <div class="card shadow-sm border-0">
                <div class="card-header bg-success text-white py-3">
                    <h5 class="mb-0 fw-bold"><i class="bi bi-box2-check-fill me-2"></i>Finalize Item Handover</h5>
                </div>
                <div class="card-body p-4">
                    <div class="alert alert-success d-flex align-items-center mb-4" role="alert">
                        <i class="bi bi-check-circle-fill fs-4 me-3"></i>
                        <div>
                            <strong>Ownership Verification Confirmed!</strong><br>
                            <span class="small">Complete this handover transaction to officially mark the item as RETURNED in the campus registry.</span>
                        </div>
                    </div>

                    <!-- Summary details -->
                    <div class="bg-light p-3 rounded mb-4">
                        <div class="row g-2 small">
                            <div class="col-sm-6">
                                <span class="text-muted">Item Title:</span>
                                <strong class="d-block text-dark">${item.title}</strong>
                            </div>
                            <div class="col-sm-6">
                                <span class="text-muted">Item ID:</span>
                                <strong class="d-block text-dark">#${item.itemId}</strong>
                            </div>
                            <div class="col-sm-6">
                                <span class="text-muted">Receiver (Claimant):</span>
                                <strong class="d-block text-dark">${claim.claimantName} (${claim.claimantEmail})</strong>
                            </div>
                            <div class="col-sm-6">
                                <span class="text-muted">Finder / Presenter:</span>
                                <strong class="d-block text-dark">${sessionScope.userName}</strong>
                            </div>
                        </div>
                    </div>

                    <!-- Handover form -->
                    <form action="${pageContext.request.contextPath}/claims/handover" method="post">
                        <input type="hidden" name="claimId" value="${claim.claimId}">

                        <div class="mb-3">
                            <label for="verificationMethod" class="form-label">Verification Method Applied <span class="text-danger">*</span></label>
                            <select class="form-select" id="verificationMethod" name="verificationMethod" required>
                                <option value="OTP_VERIFIED">6-Digit One-Time Password (OTP)</option>
                                <option value="QR_TOKEN_SCANNED">Secure QR Token Scanned</option>
                                <option value="COLLEGE_ID_IN_PERSON">Physical College Identity Card Verified</option>
                                <option value="ADMIN_DIRECT_DISPATCH">Administrative Supervised Handover</option>
                            </select>
                        </div>

                        <div class="mb-4">
                            <label for="remarks" class="form-label">Handover Remarks / Notes</label>
                            <textarea class="form-control" id="remarks" name="remarks" rows="3" placeholder="e.g. Handed over at CS Department Office. Item condition was in good order and student ID verified."></textarea>
                            <div class="form-text small">Permanent transaction remarks logged into institutional audit records.</div>
                        </div>

                        <div class="d-flex justify-content-between align-items-center pt-3 border-top">
                            <a href="${pageContext.request.contextPath}/claims?action=details&id=${claim.claimId}" class="btn btn-outline-secondary">
                                Cancel
                            </a>
                            <button type="submit" class="btn btn-success px-4 fw-semibold" onclick="return confirm('Confirm item handover? This will update item status to RETURNED and cannot be undone.');">
                                <i class="bi bi-check-all me-1"></i> Confirm &amp; Finalize Handover
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</main>

<jsp:include page="/includes/footer.jsp"/>
