<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="400 - Bad Request"/>
</jsp:include>
<jsp:include page="/includes/navbar.jsp"/>

<main class="container py-5 text-center">
    <div class="row justify-content-center">
        <div class="col-md-7 col-lg-5">
            <div class="card shadow-sm border-0 p-4">
                <div class="display-1 fw-bold text-warning mb-2">400</div>
                <h4 class="fw-bold text-dark">Bad Request</h4>
                <p class="text-muted">The system received invalid or malformed data in your request parameters.</p>
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
