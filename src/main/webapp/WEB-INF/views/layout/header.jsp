<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${pageTitle != null ? pageTitle : 'Seat-o-Matic'}" /></title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
</head>
<body class="bg-body-tertiary text-dark">
<nav class="navbar navbar-expand-lg navbar-dark bg-dark shadow-sm sticky-top">
    <div class="container-fluid px-4">
        <a class="navbar-brand fw-bold" href="${pageContext.request.contextPath}/dashboard">Seat-o-Matic</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNav">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="mainNav">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/dashboard">Dashboard</a></li>
                <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/students">Students</a></li>
                <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/exams">Exams</a></li>
                <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/rooms">Rooms</a></li>
                <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/reports">Reports</a></li>
            </ul>
            <div class="d-flex align-items-center gap-3 text-white-50">
                <span><c:out value="${sessionScope.fullName != null ? sessionScope.fullName : 'Guest'}" /></span>
                <span class="badge rounded-pill bg-light text-dark"><c:out value="${sessionScope.role != null ? sessionScope.role : 'VISITOR'}" /></span>
                <form method="post" action="${pageContext.request.contextPath}/logout">
                    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                    <button class="btn btn-outline-light btn-sm" type="submit">Logout</button>
                </form>
            </div>
        </div>
    </div>
</nav>
