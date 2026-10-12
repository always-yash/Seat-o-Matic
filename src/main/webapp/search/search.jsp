<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Search & Filters - Campus Lost & Found"/>
</jsp:include>
<jsp:include page="/includes/navbar.jsp"/>
<jsp:include page="/includes/alerts.jsp"/>

<main class="container-fluid px-lg-4 py-4">
    <div class="mb-4">
        <h4 class="fw-bold mb-1"><i class="bi bi-search text-primary me-2"></i>Multi-Criteria Item Search</h4>
        <p class="text-muted small mb-0">Filter by keywords, categories, campus zones, item type, or reported date</p>
    </div>

    <!-- Filters Form -->
    <jsp:include page="/search/filters.jsp"/>

    <!-- Results Grid -->
    <jsp:include page="/search/results.jsp"/>
</main>

<jsp:include page="/includes/footer.jsp"/>
