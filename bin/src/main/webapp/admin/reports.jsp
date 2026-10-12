<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Item Moderation Reports - Admin Portal"/>
</jsp:include>
<jsp:include page="/includes/navbar.jsp"/>
<jsp:include page="/includes/alerts.jsp"/>

<main class="container-fluid px-lg-4 py-4">
    <div class="mb-4">
        <h4 class="fw-bold mb-1"><i class="bi bi-flag-fill text-danger me-2"></i>Content Moderation &amp; Flags</h4>
        <p class="text-muted small mb-0">Review reports filed by users against duplicate, inaccurate, or inappropriate listings</p>
    </div>

    <div class="card shadow-sm border-0">
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-custom table-hover mb-0">
                    <thead>
                        <tr>
                            <th class="ps-3">Report ID</th>
                            <th>Flagged Item</th>
                            <th>Reported By</th>
                            <th>Reason</th>
                            <th>Additional Description</th>
                            <th>Date Filed</th>
                            <th>Status</th>
                            <th class="text-end pe-3">Moderation Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty reports}">
                                <c:forEach var="rep" items="${reports}">
                                    <tr>
                                        <td class="ps-3 fw-bold text-muted">#${rep.reportId}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty rep.itemId}">
                                                    <a href="${pageContext.request.contextPath}/items?action=view&id=${rep.itemId}" class="fw-semibold text-dark text-decoration-none">
                                                        <c:choose>
                                                            <c:when test="${not empty rep.itemTitle}">
                                                                ${rep.itemTitle}
                                                            </c:when>
                                                            <c:otherwise>
                                                                Item #${rep.itemId}
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </a>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="text-muted">Item deleted</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <span class="fw-semibold text-dark">${rep.reporterName}</span>
                                            <div class="small text-muted">${rep.reporterEmail}</div>
                                        </td>
                                        <td>
                                            <span class="badge bg-danger-subtle text-danger border border-danger-subtle">${rep.reason}</span>
                                        </td>
                                        <td class="text-truncate" style="max-width: 200px;" title="${rep.description}">
                                            ${not empty rep.description ? rep.description : '-'}
                                        </td>
                                        <td class="small text-muted">${rep.createdAt}</td>
                                        <td>
                                            <span class="badge ${rep.status eq 'RESOLVED' ? 'bg-success' : (rep.status eq 'REVIEWED' ? 'bg-info' : 'bg-warning text-dark')}">
                                                ${rep.status}
                                            </span>
                                        </td>
                                        <td class="text-end pe-3">
                                            <form action="${pageContext.request.contextPath}/reports" method="post" class="d-inline">
                                                <input type="hidden" name="action" value="updateStatus">
                                                <input type="hidden" name="reportId" value="${rep.reportId}">
                                                <div class="btn-group btn-group-sm">
                                                    <button type="submit" name="status" value="REVIEWED" class="btn btn-outline-info" title="Mark as Reviewed">
                                                        Review
                                                    </button>
                                                    <button type="submit" name="status" value="RESOLVED" class="btn btn-outline-success" title="Mark as Resolved">
                                                        Resolve
                                                    </button>
                                                </div>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="8" class="text-center py-5 text-muted">
                                        <i class="bi bi-shield-check fs-2 text-success d-block mb-2"></i>
                                        No open moderation reports! The community feed is in good standing.
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
