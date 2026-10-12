<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="404 - Page Not Found"/>
</jsp:include>
<jsp:include page="/includes/navbar.jsp"/>

<main class="container py-5 text-center">
    <div class="row justify-content-center">
        <div class="col-md-7 col-lg-5">
            <div class="card shadow-sm border-0 p-4">
                <div class="display-1 fw-bold text-secondary mb-2">404</div>
                <h4 class="fw-bold text-dark">Page or Item Not Found</h4>
                <p class="text-muted">The resource or item listing you are looking for may have been removed, closed, or the link is incorrect.</p>
                <div class="mt-3">
                    <a href="${pageContext.request.contextPath}/items?action=gallery" class="btn btn-primary px-4">
                        <i class="bi bi-grid-fill me-1"></i> Browse Gallery
                    </a>
                </div>
            </div>
        </div>
    </div>
</main>

<jsp:include page="/includes/footer.jsp"/>
