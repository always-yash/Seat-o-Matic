<%@ include file="/WEB-INF/views/layout/header.jsp" %>
<div class="container py-5">
    <div class="row justify-content-center align-items-center min-vh-75">
        <div class="col-lg-5 col-md-7">
            <div class="card border-0 shadow-lg rounded-4 overflow-hidden">
                <div class="card-body p-4 p-lg-5">
                    <div class="text-center mb-4">
                        <div class="display-6 fw-bold text-primary">Seat-o-Matic</div>
                        <p class="text-muted mb-0">Secure exam orchestration cockpit</p>
                    </div>
                    <c:if test="${not empty errorMessage}">
                        <div class="alert alert-danger rounded-3">${errorMessage}</div>
                    </c:if>
                    <form method="post" action="${pageContext.request.contextPath}/login">
                        <input type="hidden" name="_csrf" value="${csrfToken}">
                        <div class="mb-3">
                            <label class="form-label">Username</label>
                            <input type="text" class="form-control form-control-lg rounded-3" name="username" required>
                        </div>
                        <div class="mb-4">
                            <label class="form-label">Password</label>
                            <input type="password" class="form-control form-control-lg rounded-3" name="password" required>
                        </div>
                        <button type="submit" class="btn btn-primary btn-lg w-100 rounded-3">Login</button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>
<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
