<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Sign in | Seat-o-Matic</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
</head>
<body class="auth-page">
<main class="card auth-card">
    <div class="eyebrow">Examination operations</div>
    <div class="auth-brand">Seat-o-Matic</div>
    <p class="muted">Sign in to coordinate students, rooms, seating, and paper flow.</p>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger" role="alert"><c:out value="${errorMessage}" /></div>
    </c:if>
    <form method="post" action="${pageContext.request.contextPath}/login" novalidate>
        <input type="hidden" name="_csrf" value="${csrfToken}">
        <div class="form-group">
            <label class="form-label" for="username">Username</label>
            <input class="form-control" id="username" name="username" type="text" autocomplete="username" required autofocus>
        </div>
        <div class="form-group">
            <label class="form-label" for="password">Password</label>
            <div class="password-wrap">
                <input class="form-control" id="password" name="password" type="password" autocomplete="current-password" required>
                <button class="toggle-password" type="button" data-toggle-password="password">Show</button>
            </div>
        </div>
        <button class="button button-primary button-block" type="submit">Continue to console</button>
    </form>
</main>
<script src="${pageContext.request.contextPath}/assets/js/main.js"></script>
</body>
</html>
