<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="401 - Unauthorized Access"/>
</jsp:include>
<jsp:include page="/includes/navbar.jsp"/>

<main class="container py-5 text-center">
    <div class="row justify-content-center">
        <div class="col-md-7 col-lg-5">
            <div class="card shadow-sm border-0 p-4">
                <div class="display-1 fw-bold text-primary mb-2">401</div>
                <h4 class="fw-bold text-dark">Authentication Required</h4>
                <p class="text-muted">You must be logged in with a valid college account to view or perform this action.</p>
                <div class="mt-3 d-flex gap-2 justify-content-center">
                    <a href="${pageContext.request.contextPath}/login.jsp" class="btn btn-primary px-4">
                        <i class="bi bi-box-arrow-in-right me-1"></i> Sign In
                    </a>
                    <a href="${pageContext.request.contextPath}/index.jsp" class="btn btn-outline-secondary px-3">
                        Homepage
                    </a>
                </div>
            </div>
        </div>
    </div>
</main>

<jsp:include page="/includes/footer.jsp"/>
