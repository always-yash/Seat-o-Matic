<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Unclaimed Disposal Review - Admin Portal"/>
</jsp:include>
<jsp:include page="/includes/navbar.jsp"/>
<jsp:include page="/includes/alerts.jsp"/>

<main class="container-fluid px-lg-4 py-4">
    <div class="mb-4">
        <h4 class="fw-bold mb-1"><i class="bi bi-archive-fill text-secondary me-2"></i>Unclaimed Property &amp; Disposal Review</h4>
        <p class="text-muted small mb-0">Items remaining active for over ${threshold} days eligible for college lost-property auction, donation, or recycling</p>
    </div>

    <div class="card shadow-sm border-0">
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-custom table-hover mb-0">
                    <thead>
                        <tr>
                            <th class="ps-3">Item ID</th>
                            <th>Title</th>
                            <th>Type</th>
                            <th>Category</th>
                            <th>Campus Location</th>
                            <th>Date Reported</th>
                            <th>Reporter</th>
                            <th class="text-end pe-3">Disposal Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty candidates}">
                                <c:forEach var="it" items="${candidates}">
                                    <tr>
                                        <td class="ps-3 fw-bold text-muted">#${it.itemId}</td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/items?action=view&id=${it.itemId}" class="fw-bold text-dark text-decoration-none">
                                                ${it.title}
                                            </a>
                                        </td>
                                        <td>
                                            <span class="badge ${it.itemType eq 'LOST' ? 'badge-lost' : 'badge-found'}">
                                                ${it.itemType}
                                            </span>
                                        </td>
                                        <td>${it.categoryName}</td>
                                        <td>${it.location}</td>
                                        <td>${it.itemDate}</td>
                                        <td>
                                            <span class="fw-semibold text-dark">${it.userName}</span>
                                            <div class="small text-muted">${it.userEmail}</div>
                                        </td>
                                        <td class="text-end pe-3">
                                            <form action="${pageContext.request.contextPath}/items" method="post" class="d-inline" onsubmit="return confirm('Archive/Close item #${it.itemId} for institutional disposal?');">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="itemId" value="${it.itemId}">
                                                <button type="submit" class="btn btn-outline-danger btn-sm">
                                                    <i class="bi bi-box-arrow-down me-1"></i> Archive / Dispose
                                                </button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="8" class="text-center py-5 text-muted">
                                        <i class="bi bi-check2-circle fs-2 text-success d-block mb-2"></i>
                                        No unclaimed items currently exceeding the ${threshold}-day aging threshold.
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
