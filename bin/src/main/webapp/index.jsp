<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.lostfound.member2_items.dao.ItemDAO,com.lostfound.member2_items.dao.ItemDAOImpl,com.lostfound.model.Item,java.util.List" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
%>

<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Home - Campus Lost & Found Portal"/>
</jsp:include>
<jsp:include page="/includes/navbar.jsp"/>
<jsp:include page="/includes/alerts.jsp"/>

<%
    ItemDAO itemDAO = new ItemDAOImpl();
    int lostCount = 0;
    int foundCount = 0;
    int returnedCount = 0;
    List<Item> recentItems = null;
    try {
        lostCount = itemDAO.countItemsByType("LOST");
        foundCount = itemDAO.countItemsByType("FOUND");
        returnedCount = itemDAO.countItemsByStatus("RETURNED");
        recentItems = itemDAO.findAll();
        if (recentItems != null && recentItems.size() > 8) {
            recentItems = recentItems.subList(0, 8);
        }
    } catch (Exception ignored) {
    }
    request.setAttribute("lostCount", lostCount);
    request.setAttribute("foundCount", foundCount);
    request.setAttribute("returnedCount", returnedCount);
    request.setAttribute("recentItems", recentItems);
%>

<main class="container-fluid px-lg-4 py-4">
    <!-- Hero Banner -->
    <div class="hero-banner">
        <div class="row align-items-center">
            <div class="col-lg-8">
                <span class="badge bg-white text-primary px-3 py-2 fw-semibold rounded-pill mb-3">
                    <i class="bi bi-shield-check me-1"></i> Official Campus Property Recovery Network
                </span>
                <h1 class="display-5 fw-bold mb-3">Lost Something on Campus? Found an Item?</h1>
                <p class="lead mb-4 text-white-50">
                    Connect directly with fellow students, faculty, and administrators. Powered by intelligent Levenshtein text matching, Haversine geospatial proximity, and secure OTP/QR verification.
                </p>
                <div class="d-flex flex-wrap gap-3">
                    <a href="${pageContext.request.contextPath}/items?action=report-lost" class="btn btn-danger btn-lg px-4 shadow">
                        <i class="bi bi-exclamation-circle me-1"></i> Report Lost Item
                    </a>
                    <a href="${pageContext.request.contextPath}/items?action=report-found" class="btn btn-success btn-lg px-4 shadow">
                        <i class="bi bi-plus-circle me-1"></i> Report Found Item
                    </a>
                    <a href="${pageContext.request.contextPath}/items?action=gallery" class="btn btn-light btn-lg px-4 shadow-sm text-primary">
                        <i class="bi bi-grid-fill me-1"></i> Browse Gallery
                    </a>
                </div>
            </div>
            <div class="col-lg-4 text-center d-none d-lg-block">
                <i class="bi bi-compass text-white-50" style="font-size: 11rem;"></i>
            </div>
        </div>
    </div>

    <!-- Quick Stats Section -->
    <div class="row g-3 mb-5">
        <div class="col-md-4 col-sm-6">
            <div class="stat-card">
                <div>
                    <div class="stat-label">Lost Items Reported</div>
                    <div class="stat-number text-danger">${lostCount}</div>
                    <div class="small text-muted mt-1">Pending recovery</div>
                </div>
                <div class="stat-icon bg-danger-subtle text-danger">
                    <i class="bi bi-search"></i>
                </div>
            </div>
        </div>
        <div class="col-md-4 col-sm-6">
            <div class="stat-card">
                <div>
                    <div class="stat-label">Found Items Logged</div>
                    <div class="stat-number text-success">${foundCount}</div>
                    <div class="small text-muted mt-1">Safely reported</div>
                </div>
                <div class="stat-icon bg-success-subtle text-success">
                    <i class="bi bi-box2-heart"></i>
                </div>
            </div>
        </div>
        <div class="col-md-4 col-sm-12">
            <div class="stat-card">
                <div>
                    <div class="stat-label">Successfully Returned</div>
                    <div class="stat-number text-primary">${returnedCount}</div>
                    <div class="small text-muted mt-1">Reunited via OTP/QR</div>
                </div>
                <div class="stat-icon bg-primary-subtle text-primary">
                    <i class="bi bi-check-all"></i>
                </div>
            </div>
        </div>
    </div>

    <!-- How It Works Section -->
    <div class="mb-5">
        <div class="text-center mb-4">
            <h3 class="fw-bold">How the Recovery System Works</h3>
            <p class="text-muted">A streamlined, 4-step verified workflow ensuring property reaches its rightful owner.</p>
        </div>
        <div class="row g-4">
            <div class="col-md-3">
                <div class="card h-100 p-3 text-center border-0 shadow-sm">
                    <div class="rounded-circle bg-primary-subtle text-primary mx-auto d-flex align-items-center justify-content-center mb-3" style="width: 56px; height: 56px;">
                        <i class="bi bi-pencil-square fs-4"></i>
                    </div>
                    <h6 class="fw-bold">1. Report Item</h6>
                    <p class="small text-muted mb-0">Post details with title, description, campus location, date, and uploaded photo.</p>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card h-100 p-3 text-center border-0 shadow-sm">
                    <div class="rounded-circle bg-info-subtle text-info mx-auto d-flex align-items-center justify-content-center mb-3" style="width: 56px; height: 56px;">
                        <i class="bi bi-cpu fs-4"></i>
                    </div>
                    <h6 class="fw-bold">2. Smart Matching</h6>
                    <p class="small text-muted mb-0">Our engine compares text similarity and GPS coordinates to highlight potential matches.</p>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card h-100 p-3 text-center border-0 shadow-sm">
                    <div class="rounded-circle bg-warning-subtle text-warning mx-auto d-flex align-items-center justify-content-center mb-3" style="width: 56px; height: 56px;">
                        <i class="bi bi-shield-lock fs-4"></i>
                    </div>
                    <h6 class="fw-bold">3. File Claim &amp; Proof</h6>
                    <p class="small text-muted mb-0">The owner claims the item with proof description. Once approved, secure OTP &amp; QR tokens are generated.</p>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card h-100 p-3 text-center border-0 shadow-sm">
                    <div class="rounded-circle bg-success-subtle text-success mx-auto d-flex align-items-center justify-content-center mb-3" style="width: 56px; height: 56px;">
                        <i class="bi bi-person-check fs-4"></i>
                    </div>
                    <h6 class="fw-bold">4. Verified Handover</h6>
                    <p class="small text-muted mb-0">Handover is finalized with OTP/QR scan, atomic database update, and instant audit logging.</p>
                </div>
            </div>
        </div>
    </div>

    <!-- Recent Items Section -->
    <div>
        <div class="d-flex justify-content-between align-items-center mb-3">
            <div>
                <h4 class="fw-bold mb-0">Recently Logged Campus Items</h4>
                <p class="text-muted small mb-0">Live feed from database</p>
            </div>
            <a href="${pageContext.request.contextPath}/items?action=gallery" class="btn btn-outline-primary btn-sm">
                View All Items <i class="bi bi-arrow-right ms-1"></i>
            </a>
        </div>

        <div class="row g-4">
            <c:choose>
                <c:when test="${not empty recentItems}">
                    <c:forEach var="item" items="${recentItems}">
                        <div class="col-xl-3 col-lg-4 col-md-6">
                            <div class="card h-100 card-hover">
                                <c:choose>
                                    <c:when test="${not empty item.image}">
                                        <img src="${pageContext.request.contextPath}/${item.image}" class="item-img-card" alt="${item.title}" onerror="this.src='https://placehold.co/400x250/e2e8f0/475569?text=Campus+Item'">
                                    </c:when>
                                    <c:otherwise>
                                        <div class="item-img-placeholder">
                                            <i class="bi bi-image fs-1 mb-1"></i>
                                            <span class="small">No Photo Provided</span>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                                <div class="card-body d-flex flex-column">
                                    <div class="d-flex justify-content-between align-items-start mb-2">
                                        <span class="badge ${item.itemType eq 'LOST' ? 'badge-lost' : 'badge-found'}">
                                            ${item.itemType}
                                        </span>
                                        <span class="badge bg-secondary-subtle text-secondary small">
                                            ${item.categoryName}
                                        </span>
                                    </div>
                                    <h6 class="card-title fw-bold text-dark mb-1 text-truncate" title="${item.title}">
                                        ${item.title}
                                    </h6>
                                    <p class="card-text text-muted small text-truncate-2 mb-3" style="display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; height: 38px;">
                                        ${item.description}
                                    </p>
                                    <div class="mt-auto pt-2 border-top">
                                        <div class="d-flex align-items-center justify-content-between small text-muted mb-2">
                                            <span class="text-truncate" style="max-width: 140px;">
                                                <i class="bi bi-geo-alt me-1 text-danger"></i>${item.location}
                                            </span>
                                            <span>
                                                <i class="bi bi-calendar3 me-1"></i>${item.itemDate}
                                            </span>
                                        </div>
                                        <div class="d-flex gap-2">
                                            <a href="${pageContext.request.contextPath}/items?action=view&id=${item.itemId}" class="btn btn-outline-primary btn-sm w-100">
                                                View Details
                                            </a>
                                            <a href="${pageContext.request.contextPath}/matches?itemId=${item.itemId}" class="btn btn-outline-info btn-sm" title="Compute Matches">
                                                <i class="bi bi-cpu"></i>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <div class="col-12 text-center py-5">
                        <i class="bi bi-inbox text-muted" style="font-size: 3rem;"></i>
                        <p class="text-muted mt-2">No items logged yet in database. Be the first to report!</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</main>

<jsp:include page="/includes/footer.jsp"/>
