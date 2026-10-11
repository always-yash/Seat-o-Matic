<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Not found | Seat-o-Matic</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
</head>
<body class="error-page">
<div class="card error-card">
    <div class="eyebrow">Navigation signal</div>
    <div class="error-code">404</div>
    <h1>That page drifted away.</h1>
    <p>The requested workspace route does not exist.</p>
    <a class="button button-primary" href="${pageContext.request.contextPath}/login">Back to sign in</a>
</div>
</body>
</html>
