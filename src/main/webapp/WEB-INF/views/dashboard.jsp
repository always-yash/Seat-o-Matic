<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<main class="page-shell">
    <section class="hero">
        <div class="eyebrow">Operations overview · live workspace</div>
        <h1>Welcome back, <c:out value="${username}" /></h1>
        <p class="hero-copy">Your examination operations at a glance. Keep the next cycle moving with a clear view of people, rooms, schedules, and activity.</p>
    </section>
    <section class="stat-grid" aria-label="System summary">
        <article class="card stat-card">
            <div class="stat-label">Active students</div>
            <div class="stat-value"><c:out value="${dashboardStats.activeStudents}" /></div>
        </article>
        <article class="card stat-card">
            <div class="stat-label">Upcoming exams</div>
            <div class="stat-value"><c:out value="${dashboardStats.upcomingExams}" /></div>
        </article>
        <article class="card stat-card">
            <div class="stat-label">Configured rooms</div>
            <div class="stat-value"><c:out value="${dashboardStats.configuredRooms}" /></div>
        </article>
        <article class="card stat-card">
            <div class="stat-label">Audit events</div>
            <div class="stat-value"><c:out value="${dashboardStats.auditEvents}" /></div>
        </article>
    </section>
    <section class="section-heading">
        <div>
            <div class="eyebrow">Workspace signal</div>
            <h2>Ready for the next move</h2>
            <p>Core systems are connected and the operations console is online.</p>
        </div>
        <span class="status-pill status-dot">All systems ready</span>
    </section>
</main>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
