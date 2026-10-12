<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="container-fluid px-lg-4 mt-3">
    <%-- Success message --%>
    <c:if test="${not empty sessionScope.flashSuccess}">
        <div class="alert alert-success alert-dismissible fade show d-flex align-items-center shadow-sm" role="alert">
            <i class="bi bi-check-circle-fill fs-5 me-2"></i>
            <div>${sessionScope.flashSuccess}</div>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
        <c:remove var="flashSuccess" scope="session"/>
    </c:if>
    <c:if test="${not empty requestScope.success and empty sessionScope.flashSuccess}">
        <div class="alert alert-success alert-dismissible fade show d-flex align-items-center shadow-sm" role="alert">
            <i class="bi bi-check-circle-fill fs-5 me-2"></i>
            <div>${requestScope.success}</div>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>
    <c:if test="${not empty param.success and empty requestScope.success and empty sessionScope.flashSuccess}">
        <div class="alert alert-success alert-dismissible fade show d-flex align-items-center shadow-sm" role="alert">
            <i class="bi bi-check-circle-fill fs-5 me-2"></i>
            <div><c:out value="${param.success}"/></div>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>

    <%-- Error message --%>
    <c:if test="${not empty sessionScope.flashError}">
        <div class="alert alert-danger alert-dismissible fade show d-flex align-items-center shadow-sm" role="alert">
            <i class="bi bi-exclamation-triangle-fill fs-5 me-2"></i>
            <div>${sessionScope.flashError}</div>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
        <c:remove var="flashError" scope="session"/>
    </c:if>
    <c:if test="${not empty requestScope.error and empty sessionScope.flashError}">
        <div class="alert alert-danger alert-dismissible fade show d-flex align-items-center shadow-sm" role="alert">
            <i class="bi bi-exclamation-triangle-fill fs-5 me-2"></i>
            <div>${requestScope.error}</div>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>
    <c:if test="${not empty param.error and empty requestScope.error and empty sessionScope.flashError}">
        <div class="alert alert-danger alert-dismissible fade show d-flex align-items-center shadow-sm" role="alert">
            <i class="bi bi-exclamation-triangle-fill fs-5 me-2"></i>
            <div><c:out value="${param.error}"/></div>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>

    <%-- Info message --%>
    <c:if test="${not empty sessionScope.flashInfo}">
        <div class="alert alert-info alert-dismissible fade show d-flex align-items-center shadow-sm" role="alert">
            <i class="bi bi-info-circle-fill fs-5 me-2"></i>
            <div>${sessionScope.flashInfo}</div>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
        <c:remove var="flashInfo" scope="session"/>
    </c:if>
    <c:if test="${not empty requestScope.info and empty sessionScope.flashInfo}">
        <div class="alert alert-info alert-dismissible fade show d-flex align-items-center shadow-sm" role="alert">
            <i class="bi bi-info-circle-fill fs-5 me-2"></i>
            <div>${requestScope.info}</div>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>
    <c:if test="${not empty param.info and empty requestScope.info and empty sessionScope.flashInfo}">
        <div class="alert alert-info alert-dismissible fade show d-flex align-items-center shadow-sm" role="alert">
            <i class="bi bi-info-circle-fill fs-5 me-2"></i>
            <div><c:out value="${param.info}"/></div>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>
</div>
