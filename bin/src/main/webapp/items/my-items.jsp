<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="My Reported Items - Campus Lost & Found"/>
</jsp:include>
<jsp:include page="/includes/navbar.jsp"/>
<jsp:include page="/includes/alerts.jsp"/>

<main class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h4 class="fw-bold mb-1"><i class="bi bi-collection-fill text-primary me-2"></i>My Reported Items</h4>
            <p class="text-muted small mb-0">Track and manage all items you have logged into the campus system</p>
        </div>
        <div class="d-flex gap-2">
            <a href="${pageContext.request.contextPath}/items?action=report-lost" class="btn btn-outline-danger btn-sm">
                <i class="bi bi-plus-lg me-1"></i> Report Lost
            </a>
            <a href="${pageContext.request.contextPath}/items?action=report-found" class="btn btn-outline-success btn-sm">
                <i class="bi bi-plus-lg me-1"></i> Report Found
            </a>
        </div>
    </div>

    <div class="card shadow-sm border-0">
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-custom table-hover mb-0">
                    <thead>
                        <tr>
                            <th class="ps-3">Item Details</th>
                            <th>Type</th>
                            <th>Category</th>
                            <th>Date</th>
                            <th>Location</th>
                            <th>Status</th>
                            <th class="text-end pe-3">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty items}">
                                <c:forEach var="it" items="${items}">
                                    <tr>
                                        <td class="ps-3">
                                            <div class="d-flex align-items-center gap-3">
                                                <c:choose>
                                                    <c:when test="${not empty it.image}">
                                                        <img src="${pageContext.request.contextPath}/${it.image}" class="rounded" style="width: 48px; height: 48px; object-fit: cover;" alt="" onerror="this.src='https://placehold.co/48x48/e2e8f0/475569?text=Item'">
                                                    </c:when>
                                                    <c:otherwise>
                                                        <div class="rounded bg-light d-flex align-items-center justify-content-center text-muted" style="width: 48px; height: 48px;">
                                                            <i class="bi bi-image"></i>
                                                        </div>
                                                    </c:otherwise>
                                                </c:choose>
                                                <div>
                                                    <a href="${pageContext.request.contextPath}/items?action=view&id=${it.itemId}" class="fw-bold text-dark text-decoration-none">
                                                        ${it.title}
                                                    </a>
                                                    <div class="small text-muted">ID: #${it.itemId}</div>
                                                </div>
                                            </div>
                                        </td>
                                        <td>
                                            <span class="badge ${it.itemType eq 'LOST' ? 'badge-lost' : 'badge-found'}">
                                                ${it.itemType}
                                            </span>
                                        </td>
                                        <td>${it.categoryName}</td>
                                        <td>${it.itemDate}</td>
                                        <td class="text-truncate" style="max-width: 160px;" title="${it.location}">${it.location}</td>
                                        <td>
                                            <span class="badge ${it.status eq 'ACTIVE' ? 'badge-active' : (it.status eq 'RETURNED' ? 'badge-returned' : (it.status eq 'CLAIMED' ? 'badge-claimed' : (it.status eq 'CLOSED' ? 'bg-secondary' : 'badge-matched')))}">
                                                ${it.status}
                                            </span>
                                        </td>
                                        <td class="text-end pe-3">
                                            <div class="btn-group btn-group-sm">
                                                <a href="${pageContext.request.contextPath}/items?action=view&id=${it.itemId}" class="btn btn-outline-secondary" title="View Details">
                                                    <i class="bi bi-eye"></i>
                                                </a>
                                                <a href="${pageContext.request.contextPath}/matches?itemId=${it.itemId}" class="btn btn-outline-info" title="Compute Matches">
                                                    <i class="bi bi-cpu"></i>
                                                </a>
                                                <a href="${pageContext.request.contextPath}/items?action=edit&id=${it.itemId}" class="btn btn-outline-primary" title="Edit Item">
                                                    <i class="bi bi-pencil"></i>
                                                </a>
                                                <form action="${pageContext.request.contextPath}/items" method="post" class="d-inline" onsubmit="return confirm('Delete or close this item?');">
                                                    <input type="hidden" name="action" value="delete">
                                                    <input type="hidden" name="itemId" value="${it.itemId}">
                                                    <button type="submit" class="btn btn-outline-danger" title="Remove">
                                                        <i class="bi bi-trash"></i>
                                                    </button>
                                                </form>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="7" class="text-center py-5 text-muted">
                                        <i class="bi bi-inbox fs-2 d-block mb-2"></i>
                                        You have not reported any items yet.
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</main>

<jsp:include page="/includes/footer.jsp"/>
