<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<main class="page-shell">
    <div class="eyebrow">Operations overview</div>
    <h1>Welcome back, <c:out value="${username}" /></h1>
    <p class="muted">A calm command centre for the next examination cycle.</p>
    <section class="stat-grid" aria-label="System summary">
        <article class="card stat-card"><div class="stat-label">Active students</div><div class="stat-value">1,248</div></article>
        <article class="card stat-card"><div class="stat-label">Upcoming exams</div><div class="stat-value">42</div></article>
        <article class="card stat-card"><div class="stat-label">Configured rooms</div><div class="stat-value">18</div></article>
        <article class="card stat-card"><div class="stat-label">Audit events</div><div class="stat-value">96</div></article>
    </section>
</main>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
