<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Claim Item - Campus Lost & Found"/>
</jsp:include>
<jsp:include page="/includes/navbar.jsp"/>
<jsp:include page="/includes/alerts.jsp"/>

<main class="container py-4">
    <div class="row justify-content-center">
        <div class="col-lg-8">
            <div class="card shadow-sm border-0">
                <div class="card-header bg-success-subtle text-success py-3 border-bottom d-flex align-items-center justify-content-between">
                    <h5 class="mb-0 fw-bold"><i class="bi bi-hand-index-thumb-fill me-2"></i>File Ownership Claim</h5>
                    <span class="badge bg-success text-white">Item #${item.itemId}</span>
                </div>
                <div class="card-body p-4">
                    <!-- Item Summary Card -->
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
                                <h6 class="fw-bold mb-1">${item.title}</h6>
                                <div class="small text-muted">
                                    <i class="bi bi-geo-alt text-danger me-1"></i>Found at: ${item.location} &bull;
                                    <i class="bi bi-calendar3 me-1"></i>${item.itemDate}
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Claim Submission Form -->
                    <form action="${pageContext.request.contextPath}/claims" method="post">
                        <input type="hidden" name="action" value="submit">
                        <input type="hidden" name="itemId" value="${item.itemId}">

                        <div class="mb-3">
                            <label for="reason" class="form-label">Why is this your item? <span class="text-danger">*</span></label>
                            <textarea class="form-control" id="reason" name="reason" rows="3" placeholder="Explain how and when you lost this item on campus..." required minlength="5"></textarea>
                            <div class="form-text small">Explain your relationship to the item (e.g. bought it last semester, has my name on it).</div>
                        </div>

                        <div class="mb-4">
                            <label for="proofDescription" class="form-label">Detailed Proof of Ownership <span class="text-danger">*</span></label>
                            <textarea class="form-control" id="proofDescription" name="proofDescription" rows="4" placeholder="Provide confidential identifying details not visible in photos (e.g. lock screen PIN, specific contents, scratch location, receipts, roll number on card)..." required minlength="5"></textarea>
                            <div class="form-text small">This confidential proof will only be visible to the finder and campus administrators for verification.</div>
                        </div>

                        <div class="alert alert-info small d-flex align-items-center" role="alert">
                            <i class="bi bi-shield-check fs-5 me-2"></i>
                            <div>
                                False claims violate university honor code. If approved, a secure OTP and QR code will be generated for in-person verification.
                            </div>
                        </div>

                        <div class="d-flex justify-content-between align-items-center pt-3 border-top">
                            <a href="${pageContext.request.contextPath}/items?action=view&id=${item.itemId}" class="btn btn-outline-secondary">
                                Cancel
                            </a>
                            <button type="submit" class="btn btn-success px-4 fw-semibold">
                                <i class="bi bi-send-check me-1"></i> Submit Claim
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</main>

<jsp:include page="/includes/footer.jsp"/>
