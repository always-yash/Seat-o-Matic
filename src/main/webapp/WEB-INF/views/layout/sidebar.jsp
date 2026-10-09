<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="bg-white border-end p-3 shadow-sm" style="min-height: calc(100vh - 72px); width: 260px;">
    <div class="fw-bold text-uppercase text-secondary small mb-3">Navigation</div>
    <nav class="nav flex-column gap-2">
        <a class="nav-link rounded px-3 py-2 text-dark" href="${pageContext.request.contextPath}/dashboard">Overview</a>
        <a class="nav-link rounded px-3 py-2 text-dark" href="${pageContext.request.contextPath}/students">Students</a>
        <a class="nav-link rounded px-3 py-2 text-dark" href="${pageContext.request.contextPath}/exams">Exams</a>
        <a class="nav-link rounded px-3 py-2 text-dark" href="${pageContext.request.contextPath}/rooms">Rooms</a>
        <a class="nav-link rounded px-3 py-2 text-dark" href="${pageContext.request.contextPath}/seating/plan">Seating</a>
        <a class="nav-link rounded px-3 py-2 text-dark" href="${pageContext.request.contextPath}/papers">Paper Flow</a>
        <a class="nav-link rounded px-3 py-2 text-dark" href="${pageContext.request.contextPath}/reports">Reports</a>
    </nav>
</div>
