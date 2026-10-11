<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Access denied | Seat-o-Matic</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
</head>
<body class="error-page">
<div class="card error-card">
    <div class="eyebrow">Workspace permissions</div>
    <div class="error-code">403</div>
    <h1>Access denied.</h1>
    <p>This workspace is not available for the current request.</p>
    <a class="button button-primary" href="${pageContext.request.contextPath}/login">Back to sign in</a>
</div>
</body>
</html>
