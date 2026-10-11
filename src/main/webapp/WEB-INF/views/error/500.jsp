<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Server Error | Seat-o-Matic</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
</head>
<body class="error-page">
<div class="card error-card">
    <div class="eyebrow">System interruption</div>
    <div class="error-code">500</div>
    <h1>Something went wrong.</h1>
    <p>The workspace hit an unexpected issue. Return to sign in and try again.</p>
    <a class="button button-primary" href="${pageContext.request.contextPath}/login">Back to sign in</a>
</div>
</body>
</html>
