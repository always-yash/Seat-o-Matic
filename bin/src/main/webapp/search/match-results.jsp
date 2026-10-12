<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Match Results - Campus Lost & Found"/>
</jsp:include>
<jsp:include page="/includes/navbar.jsp"/>
<jsp:include page="/includes/alerts.jsp"/>

<main class="container py-4">
    <!-- Target Item Header Banner -->
    <div class="card shadow-sm border-0 mb-4">
        <div class="card-body p-4 bg-light rounded">
            <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3">
                <div>
                    <span class="badge ${targetItem.itemType eq 'LOST' ? 'badge-lost' : 'badge-found'} mb-2">
                        TARGET: ${targetItem.itemType}
                    </span>
                    <h4 class="fw-bold mb-1">${targetItem.title}</h4>
                    <div class="small text-muted">
                        <i class="bi bi-geo-alt text-danger me-1"></i>${targetItem.location} &bull;
                        <i class="bi bi-calendar3 me-1"></i>${targetItem.itemDate} &bull;
                        <span class="badge bg-secondary-subtle text-secondary">${targetItem.categoryName}</span>
                    </div>
                </div>
                <div class="text-md-end">
                    <span class="badge bg-primary px-3 py-2 fs-6">
                        <i class="bi bi-cpu-fill me-1"></i> ${matchCount != null ? matchCount : 0} Potential Matches Found
                    </span>
                    <div class="mt-2">
                        <a href="${pageContext.request.contextPath}/items?action=view&id=${targetItem.itemId}" class="btn btn-outline-secondary btn-sm">
                            <i class="bi bi-arrow-left me-1"></i> Back to Item
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Match Results Listing -->
    <div class="mb-4">
        <h5 class="fw-bold mb-3"><i class="bi bi-diagram-3-fill text-primary me-2"></i>Algorithmic Matching Analysis</h5>
        <p class="text-muted small">
            Scored via <strong>Levenshtein Text Distance (40%)</strong>, <strong>Haversine GPS Formula (30%)</strong>, <strong>Temporal Proximity (20%)</strong>, and <strong>Category Alignment (10%)</strong>.
        </p>

        <c:choose>
            <c:when test="${not empty matches}">
                <div class="row g-4">
                    <c:forEach var="m" items="${matches}">
                        <c:set var="matchedItem" value="${m.lostItemId eq targetItem.itemId ? m.foundItem : m.lostItem}"/>
                        <div class="col-12">
                            <div class="card shadow-sm border-0">
                                <div class="card-body p-4">
                                    <div class="row align-items-center">
                                        <!-- Matched Item Overview -->
                                        <div class="col-lg-5 col-md-6 mb-3 mb-md-0">
                                            <div class="d-flex align-items-center gap-3">
                                                <c:choose>
                                                    <c:when test="${not empty matchedItem.image}">
                                                        <img src="${pageContext.request.contextPath}/${matchedItem.image}" class="rounded" style="width: 70px; height: 70px; object-fit: cover;" alt="" onerror="this.src='https://placehold.co/70x70/e2e8f0/475569?text=Item'">
                                                    </c:when>
                                                    <c:otherwise>
                                                        <div class="rounded bg-light d-flex align-items-center justify-content-center text-muted" style="width: 70px; height: 70px;">
                                                            <i class="bi bi-image fs-3"></i>
                                                        </div>
                                                    </c:otherwise>
                                                </c:choose>
                                                <div>
                                                    <span class="badge ${matchedItem.itemType eq 'LOST' ? 'badge-lost' : 'badge-found'} mb-1">
                                                        ${matchedItem.itemType}
                                                    </span>
                                                    <h6 class="fw-bold mb-1">
                                                        <a href="${pageContext.request.contextPath}/items?action=view&id=${matchedItem.itemId}" class="text-dark text-decoration-none">
                                                            ${matchedItem.title}
                                                        </a>
                                                    </h6>
                                                    <div class="small text-muted">
                                                        <i class="bi bi-geo-alt text-danger me-1"></i>${matchedItem.location}<br>
                                                        <i class="bi bi-calendar3 me-1"></i>${matchedItem.itemDate}
                                                    </div>
                                                </div>
                                            </div>
                                        </div>

                                        <!-- Score Breakdown -->
                                        <div class="col-lg-5 col-md-6 mb-3 mb-md-0">
                                            <div class="d-flex align-items-center justify-content-between mb-1">
                                                <span class="badge ${m.ratingBadgeClass} fs-6 px-3 py-1">
                                                    ${m.rating}
                                                </span>
                                                <span class="fw-bold fs-5 text-dark">
                                                    ${m.totalScore}%
                                                </span>
                                            </div>
                                            <div class="progress score-progress-bar mb-2">
                                                <div class="progress-bar ${m.totalScore >= 70 ? 'bg-success' : (m.totalScore >= 50 ? 'bg-primary' : 'bg-warning')}" role="progressbar" style="width: ${m.totalScore}%" aria-valuenow="${m.totalScore}" aria-valuemin="0" aria-valuemax="100"></div>
                                            </div>
                                            <div class="row g-1 small text-muted text-center" style="font-size: 0.75rem;">
                                                <div class="col-3">Text: <strong class="text-dark">${m.textScore}%</strong></div>
                                                <div class="col-3">Location: <strong class="text-dark">${m.locationScore}%</strong></div>
                                                <div class="col-3">Date: <strong class="text-dark">${m.dateScore}%</strong></div>
                                                <div class="col-3">Category: <strong class="text-dark">${m.categoryScore}%</strong></div>
                                            </div>
                                        </div>

                                        <!-- Action Buttons -->
                                        <div class="col-lg-2 col-md-12 text-lg-end mt-2 mt-lg-0">
                                            <div class="d-flex flex-lg-column gap-2 justify-content-end">
                                                <a href="${pageContext.request.contextPath}/items?action=view&id=${matchedItem.itemId}" class="btn btn-outline-primary btn-sm w-100">
                                                    View Item
                                                </a>
                                                <c:if test="${matchedItem.itemType eq 'FOUND' and (not empty sessionScope.userId and sessionScope.userId ne matchedItem.userId)}">
                                                    <a href="${pageContext.request.contextPath}/claims?action=create&itemId=${matchedItem.itemId}" class="btn btn-success btn-sm w-100">
                                                        Claim Item
                                                    </a>
                                                </c:if>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:when test="${empty matches}">
                <div class="card shadow-sm border-0 p-5 text-center">
                    <i class="bi bi-cpu text-muted" style="font-size: 3.5rem;"></i>
                    <h5 class="fw-bold mt-3">No Algorithmic Matches Found Yet</h5>
                    <p class="text-muted small">
                        Our matching engine searches for opposite items (LOST vs FOUND) within campus bounds. As new items are logged, correlation scores will update automatically.
                    </p>
                </div>
            </c:when>
        </c:choose>
    </div>
</main>

<jsp:include page="/includes/footer.jsp"/>
