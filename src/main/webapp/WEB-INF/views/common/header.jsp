<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${empty pageTitle ? 'Seat-o-Matic' : pageTitle}" /></title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
</head>
<body>
<header class="topbar">
    <a class="brand" href="${pageContext.request.contextPath}/dashboard">
        <span class="brand-mark">S</span><span>Seat-o-Matic</span>
    </a>
    <nav class="topnav" aria-label="Primary navigation">
        <a href="${pageContext.request.contextPath}/dashboard">Overview</a>
        <a href="${pageContext.request.contextPath}/students">Students</a>
        <a href="${pageContext.request.contextPath}/exams">Exams</a>
        <a href="${pageContext.request.contextPath}/rooms">Rooms</a>
        <a href="${pageContext.request.contextPath}/reports">Reports</a>
    </nav>
    <div class="topbar-actions">
        <span class="role-badge"><c:out value="${sessionScope.userRole}" /></span>
        <span class="user-name"><c:out value="${sessionScope.username}" /></span>
        <form method="post" action="${pageContext.request.contextPath}/logout">
            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
            <button class="button button-ghost button-small" type="submit">Sign out</button>
        </form>
    </div>
</header>
