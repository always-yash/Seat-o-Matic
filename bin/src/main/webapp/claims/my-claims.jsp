<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Claims Management - Campus Lost & Found"/>
</jsp:include>
<jsp:include page="/includes/navbar.jsp"/>
<jsp:include page="/includes/alerts.jsp"/>

<main class="container py-4">
    <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
        <div>
            <h4 class="fw-bold mb-1"><i class="bi bi-hand-index-thumb-fill text-primary me-2"></i>Claims Portal</h4>
            <p class="text-muted small mb-0">Track verification status, review ownership proofs, and coordinate handovers</p>
        </div>

        <div class="btn-group" role="group">
            <a href="${pageContext.request.contextPath}/claims?action=my-claims" class="btn btn-sm ${viewType ne 'RECEIVED_CLAIMS' ? 'btn-primary' : 'btn-outline-secondary'}">
                <i class="bi bi-person me-1"></i> Claims I Filed
            </a>
            <a href="${pageContext.request.contextPath}/claims?action=manage" class="btn btn-sm ${viewType eq 'RECEIVED_CLAIMS' ? 'btn-primary' : 'btn-outline-secondary'}">
                <i class="bi bi-inbox me-1"></i> Claims On My Items
            </a>
        </div>
    </div>

    <div class="card shadow-sm border-0">
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-custom table-hover mb-0">
                    <thead>
                        <tr>
                            <th class="ps-3">Claim ID</th>
                            <th>Item Details</th>
                            <th>${viewType eq 'RECEIVED_CLAIMS' ? 'Claimant' : 'Item Reporter'}</th>
                            <th>Reason Summary</th>
                            <th>Filed Date</th>
                            <th>Status</th>
                            <th class="text-end pe-3">Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty claims}">
                                <c:forEach var="cl" items="${claims}">
                                    <tr>
                                        <td class="ps-3 fw-bold">#${cl.claimId}</td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/items?action=view&id=${cl.itemId}" class="fw-semibold text-dark text-decoration-none">
                                                ${cl.itemTitle}
                                            </a>
                                            <div class="small text-muted">${cl.itemLocation}</div>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${viewType eq 'RECEIVED_CLAIMS'}">
                                                    <span class="fw-semibold text-dark">${cl.claimantName}</span>
                                                    <div class="small text-muted">${cl.claimantEmail}</div>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="fw-semibold text-dark">${cl.ownerName}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-truncate" style="max-width: 180px;" title="${cl.reason}">
                                            ${cl.reason}
                                        </td>
                                        <td>${cl.createdAt}</td>
                                        <td>
                                            <span class="badge ${cl.status eq 'APPROVED' ? 'badge-approved' : (cl.status eq 'COMPLETED' ? 'badge-returned' : (cl.status eq 'REJECTED' ? 'badge-rejected' : (cl.status eq 'VERIFIED' ? 'badge-matched' : 'badge-pending')))}">
                                                ${cl.status}
                                            </span>
                                        </td>
                                        <td class="text-end pe-3">
                                            <a href="${pageContext.request.contextPath}/claims?action=details&id=${cl.claimId}" class="btn btn-outline-primary btn-sm">
                                                <i class="bi bi-eye me-1"></i> Details
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="7" class="text-center py-5 text-muted">
                                        <i class="bi bi-inbox fs-2 d-block mb-2"></i>
                                        No claims found in this section.
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
