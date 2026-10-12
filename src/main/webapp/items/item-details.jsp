<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="${item.title} - Campus Lost & Found"/>
</jsp:include>
<jsp:include page="/includes/navbar.jsp"/>
<jsp:include page="/includes/alerts.jsp"/>

<main class="container py-4">
    <div class="row g-4">
        <!-- Left: Image Preview -->
        <div class="col-lg-5">
            <div class="card shadow-sm border-0 overflow-hidden">
                <c:choose>
                    <c:when test="${not empty item.image}">
                        <img src="${pageContext.request.contextPath}/${item.image}" class="img-fluid w-100" style="max-height: 400px; object-fit: cover;" alt="${item.title}" onerror="this.src='https://placehold.co/600x400/e2e8f0/475569?text=Item+Photo'">
                    </c:when>
                    <c:otherwise>
                        <div class="p-5 text-center bg-light text-muted">
                            <i class="bi bi-camera fs-1"></i>
                            <div class="mt-2">No photo uploaded for this item</div>
                        </div>
                    </c:otherwise>
                </c:choose>
                <div class="card-body bg-light border-top">
                    <div class="d-flex justify-content-between align-items-center small text-muted">
                        <span><i class="bi bi-clock me-1"></i> Logged: ${item.createdAt}</span>
                        <span>Item ID: #${item.itemId}</span>
                    </div>
                </div>
            </div>
        </div>

        <!-- Right: Details -->
        <div class="col-lg-7">
            <div class="card shadow-sm border-0">
                <div class="card-body p-4">
                    <div class="d-flex flex-wrap gap-2 align-items-center mb-3">
                        <span class="badge ${item.itemType eq 'LOST' ? 'badge-lost' : 'badge-found'} fs-6">
                            ${item.itemType}
                        </span>
                        <span class="badge ${item.status eq 'ACTIVE' ? 'badge-active' : (item.status eq 'RETURNED' ? 'badge-returned' : (item.status eq 'CLAIMED' ? 'badge-claimed' : 'badge-matched'))} fs-6">
                            ${item.status}
                        </span>
                        <span class="badge bg-secondary-subtle text-secondary fs-6">
                            ${item.categoryName}
                        </span>
                    </div>

                    <h3 class="fw-bold text-dark mb-3">${item.title}</h3>

                    <div class="bg-light p-3 rounded mb-3">
                        <div class="row g-2 small">
                            <div class="col-sm-6">
                                <span class="text-muted"><i class="bi bi-calendar3 me-1"></i> Date of Event:</span>
                                <strong class="ms-1 text-dark">${item.itemDate}</strong>
                            </div>
                            <div class="col-sm-6">
                                <span class="text-muted"><i class="bi bi-geo-alt me-1 text-danger"></i> Campus Location:</span>
                                <strong class="ms-1 text-dark">${item.location}</strong>
                            </div>
                            <c:if test="${item.latitude ne 0.0 or item.longitude ne 0.0}">
                                <div class="col-12">
                                    <span class="text-muted"><i class="bi bi-pin-map me-1 text-primary"></i> GPS Coordinates:</span>
                                    <span class="badge bg-white border text-dark ms-1">${item.latitude}, ${item.longitude}</span>
                                </div>
                            </c:if>
                        </div>
                    </div>

                    <h6 class="fw-bold text-dark mb-2">Description</h6>
                    <p class="text-secondary mb-4" style="white-space: pre-line;">${item.description}</p>

                    <!-- Reporter info card -->
                    <div class="card bg-light border-0 p-3 mb-4">
                        <div class="d-flex align-items-center gap-3">
                            <div class="rounded-circle bg-primary text-white d-flex align-items-center justify-content-center fw-bold" style="width: 44px; height: 44px;">
                                <i class="bi bi-person"></i>
                            </div>
                            <div>
                                <div class="fw-bold text-dark">Reported by ${item.userName}</div>
                                <div class="small text-muted">
                                    <c:choose>
                                        <c:when test="${not empty sessionScope.userId}">
                                            <i class="bi bi-envelope me-1"></i> ${item.userEmail}
                                            <c:if test="${not empty item.userPhone}">
                                                &bull; <i class="bi bi-telephone me-1"></i> ${item.userPhone}
                                            </c:if>
                                        </c:when>
                                        <c:otherwise>
                                            <a href="${pageContext.request.contextPath}/login.jsp" class="text-primary text-decoration-none">
                                                Login to view reporter contact details
                                            </a>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Action buttons -->
                    <div class="d-flex flex-wrap gap-2 pt-2 border-top">
                        <!-- Find Matches button -->
                        <a href="${pageContext.request.contextPath}/matches?itemId=${item.itemId}" class="btn btn-outline-info">
                            <i class="bi bi-cpu me-1"></i> Run Algorithm Match
                        </a>

                        <!-- Claim button (If FOUND and user is not reporter and status is ACTIVE) -->
                        <c:if test="${item.itemType eq 'FOUND' and item.status eq 'ACTIVE'}">
                            <c:choose>
                                <c:when test="${not empty sessionScope.userId and sessionScope.userId ne item.userId}">
                                    <a href="${pageContext.request.contextPath}/claims?action=create&itemId=${item.itemId}" class="btn btn-success">
                                        <i class="bi bi-hand-index-thumb me-1"></i> Claim This Item
                                    </a>
                                </c:when>
                                <c:when test="${empty sessionScope.userId}">
                                    <a href="${pageContext.request.contextPath}/login.jsp" class="btn btn-success">
                                        <i class="bi bi-box-arrow-in-right me-1"></i> Login to Claim
                                    </a>
                                </c:when>
                            </c:choose>
                        </c:if>

                        <!-- Edit / Delete (If owner or ADMIN) -->
                        <c:if test="${(sessionScope.userRole eq 'ADMIN') or (not empty sessionScope.userId and sessionScope.userId eq item.userId)}">
                            <a href="${pageContext.request.contextPath}/items?action=edit&id=${item.itemId}" class="btn btn-outline-primary">
                                <i class="bi bi-pencil me-1"></i> Edit
                            </a>
                            <form action="${pageContext.request.contextPath}/items" method="post" class="d-inline" onsubmit="return confirm('Are you sure you want to delete/close this item listing?');">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="itemId" value="${item.itemId}">
                                <button type="submit" class="btn btn-outline-danger">
                                    <i class="bi bi-trash me-1"></i> Remove
                                </button>
                            </form>
                        </c:if>

                        <!-- Notice QR Code button -->
                        <c:if test="${not empty qrBase64}">
                            <button type="button" class="btn btn-outline-dark" data-bs-toggle="modal" data-bs-target="#qrShareModal">
                                <i class="bi bi-qr-code me-1"></i> Notice QR Code
                            </button>
                        </c:if>

                        <!-- Flag / Report button -->
                        <c:if test="${not empty sessionScope.userId and sessionScope.userId ne item.userId}">
                            <button type="button" class="btn btn-outline-secondary ms-auto" data-bs-toggle="modal" data-bs-target="#reportModal">
                                <i class="bi bi-flag me-1"></i> Flag
                            </button>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Flag Item Modal -->
    <div class="modal fade" id="reportModal" tabindex="-1" aria-labelledby="reportModalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <form action="${pageContext.request.contextPath}/reports" method="post">
                    <input type="hidden" name="action" value="file">
                    <input type="hidden" name="itemId" value="${item.itemId}">

                    <div class="modal-header">
                        <h5 class="modal-title fw-bold" id="reportModalLabel"><i class="bi bi-flag-fill text-danger me-2"></i>Flag Item to Admin</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <div class="mb-3">
                            <label for="flagReason" class="form-label">Reason for Flagging <span class="text-danger">*</span></label>
                            <select class="form-select" id="flagReason" name="reason" required>
                                <option value="Inappropriate Content">Inappropriate Content</option>
                                <option value="Duplicate Listing">Duplicate Listing</option>
                                <option value="False Information">False Information</option>
                                <option value="Already Claimed / Returned">Already Claimed / Returned</option>
                                <option value="Spam / Scams">Spam / Scam</option>
                                <option value="Other Policy Violation">Other Policy Violation</option>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label for="flagDescription" class="form-label">Additional Context</label>
                            <textarea class="form-control" id="flagDescription" name="description" rows="3" placeholder="Provide additional details for college moderation team..."></textarea>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-danger">Submit Moderation Report</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- QR Share Modal -->
    <c:if test="${not empty qrBase64}">
        <div class="modal fade" id="qrShareModal" tabindex="-1" aria-labelledby="qrShareModalLabel" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content border-0 shadow rounded-4 text-center">
                    <div class="modal-header border-0 pb-0">
                        <h5 class="modal-title fw-bold" id="qrShareModalLabel">
                            <i class="bi bi-qr-code text-primary me-2"></i>Campus Notice QR Code
                        </h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body p-4">
                        <p class="text-muted small mb-3">Scan with a smartphone camera or tablet to open this lost &amp; found notice instantly</p>
                        <div class="p-3 bg-light rounded-3 d-inline-block border shadow-sm mb-3">
                            <img src="${qrBase64}" alt="Item Notice QR Code" class="img-fluid" style="width: 220px; height: 220px;">
                        </div>
                        <div class="fw-bold text-dark">${item.title}</div>
                        <div class="small text-muted mb-3"><i class="bi bi-geo-alt text-danger me-1"></i>${item.location} &bull; Item #${item.itemId}</div>
                        <div class="alert alert-light border small text-muted mb-0">
                            <i class="bi bi-info-circle text-primary me-1"></i> Students and staff can scan this token anywhere on campus to verify or claim ownership.
                        </div>
                    </div>
                    <div class="modal-footer border-0 pt-0 justify-content-center">
                        <button type="button" class="btn btn-primary px-4 fw-semibold" data-bs-dismiss="modal">Done</button>
                    </div>
                </div>
            </div>
        </div>
    </c:if>
</main>

<jsp:include page="/includes/footer.jsp"/>
