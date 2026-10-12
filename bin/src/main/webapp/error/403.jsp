<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="403 - Forbidden Access"/>
</jsp:include>
<jsp:include page="/includes/navbar.jsp"/>

<main class="container py-5 text-center">
    <div class="row justify-content-center">
        <div class="col-md-7 col-lg-5">
            <div class="card shadow-sm border-0 p-4">
                <div class="display-1 fw-bold text-danger mb-2">403</div>
                <h4 class="fw-bold text-dark">Access Forbidden</h4>
                <p class="text-muted">You do not have the required administrative or ownership permissions to access this campus resource.</p>
                <div class="mt-3">
                    <a href="${pageContext.request.contextPath}/index.jsp" class="btn btn-primary px-4">
                        <i class="bi bi-house me-1"></i> Return to Homepage
                    </a>
                </div>
            </div>
        </div>
    </div>
</main>

<jsp:include page="/includes/footer.jsp"/>
