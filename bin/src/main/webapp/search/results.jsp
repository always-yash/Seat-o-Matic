<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div>
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h5 class="fw-bold mb-0">Search Results (${totalFound != null ? totalFound : (items != null ? items.size() : 0)})</h5>
    </div>

    <div class="row g-4">
        <c:choose>
            <c:when test="${not empty items}">
                <c:forEach var="item" items="${items}">
                    <div class="col-xl-3 col-lg-4 col-md-6">
                        <div class="card h-100 card-hover">
                            <c:choose>
                                <c:when test="${not empty item.image}">
                                    <img src="${pageContext.request.contextPath}/${item.image}" class="item-img-card" alt="${item.title}" onerror="this.src='https://placehold.co/400x250/e2e8f0/475569?text=Item'">
                                </c:when>
                                <c:otherwise>
                                    <div class="item-img-placeholder">
                                        <i class="bi bi-image fs-1 mb-1"></i>
                                        <span class="small">No Photo</span>
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
                                        <span class="text-truncate" style="max-width: 140px;" title="${item.location}">
                                            <i class="bi bi-geo-alt me-1 text-danger"></i>${item.location}
                                        </span>
                                        <span>
                                            <i class="bi bi-calendar3 me-1"></i>${item.itemDate}
                                        </span>
                                    </div>
                                    <div class="d-flex gap-2">
                                        <a href="${pageContext.request.contextPath}/items?action=view&id=${item.itemId}" class="btn btn-outline-primary btn-sm w-100">
                                            View
                                        </a>
                                        <a href="${pageContext.request.contextPath}/matches?itemId=${item.itemId}" class="btn btn-outline-info btn-sm" title="Matches">
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
                    <i class="bi bi-search text-muted" style="font-size: 3rem;"></i>
                    <h5 class="text-muted mt-2">No matching items found</h5>
                    <p class="text-muted small">Try broadening your search keywords or clearing location filters.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>
