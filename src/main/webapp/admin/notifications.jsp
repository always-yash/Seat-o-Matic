<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="My Notifications - Campus Lost & Found"/>
</jsp:include>
<jsp:include page="/includes/navbar.jsp"/>
<jsp:include page="/includes/alerts.jsp"/>

<main class="container py-4">
    <div class="row justify-content-center">
        <div class="col-lg-9">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h4 class="fw-bold mb-1"><i class="bi bi-bell-fill text-primary me-2"></i>System Notifications</h4>
                    <p class="text-muted small mb-0">Alerts regarding claim approvals, potential matches, and handover confirmations</p>
                </div>
                <c:if test="${not empty notifications}">
                    <form action="${pageContext.request.contextPath}/notifications" method="post">
                        <input type="hidden" name="action" value="mark-all-read">
                        <button type="submit" class="btn btn-outline-secondary btn-sm">
                            <i class="bi bi-check2-all me-1"></i> Mark All as Read
                        </button>
                    </form>
                </c:if>
            </div>

            <div class="card shadow-sm border-0">
                <div class="card-body p-0">
                    <c:choose>
                        <c:when test="${not empty notifications}">
                            <div class="list-group list-group-flush">
                                <c:forEach var="n" items="${notifications}">
                                    <div class="list-group-item p-3 ${n.read ? 'bg-white' : 'bg-primary-subtle bg-opacity-25'}">
                                        <div class="d-flex w-100 justify-content-between align-items-start">
                                            <div class="d-flex align-items-start gap-3">
                                                <div class="rounded-circle ${n.read ? 'bg-light text-muted' : 'bg-primary text-white'} d-flex align-items-center justify-content-center mt-1" style="width: 36px; height: 36px;">
                                                    <i class="bi bi-bell"></i>
                                                </div>
                                                <div>
                                                    <h6 class="mb-1 fw-bold text-dark">${n.title}</h6>
                                                    <p class="mb-1 text-secondary small">${n.message}</p>
                                                    <small class="text-muted"><i class="bi bi-clock me-1"></i>${n.createdAt}</small>
                                                </div>
                                            </div>
                                            <c:if test="${not n.read}">
                                                <form action="${pageContext.request.contextPath}/notifications" method="post" class="ms-2">
                                                    <input type="hidden" name="action" value="mark-read">
                                                    <input type="hidden" name="id" value="${n.notificationId}">
                                                    <button type="submit" class="btn btn-sm btn-outline-primary py-0" title="Mark as Read">
                                                        <i class="bi bi-check"></i>
                                                    </button>
                                                </form>
                                            </c:if>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="text-center py-5 text-muted">
                                <i class="bi bi-bell-slash fs-1 text-muted d-block mb-2"></i>
                                You have no notifications at this time.
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </div>
</main>

<jsp:include page="/includes/footer.jsp"/>
