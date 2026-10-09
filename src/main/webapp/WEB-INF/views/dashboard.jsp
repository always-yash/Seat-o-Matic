<%@ include file="/WEB-INF/views/layout/header.jsp" %>
<div class="d-flex">
    <%@ include file="/WEB-INF/views/layout/sidebar.jsp" %>
    <main class="flex-grow-1 p-4">
        <div class="container-fluid">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 class="fw-bold mb-1">Welcome back, <c:out value="${username}" /></h2>
                    <p class="text-muted mb-0">Role-aware operations dashboard</p>
                </div>
                <span class="badge rounded-pill bg-primary-subtle text-primary px-3 py-2"><c:out value="${userRole}" /></span>
            </div>

            <div class="row g-4">
                <div class="col-md-6 col-xl-3">
                    <div class="card border-0 shadow-sm rounded-4 h-100">
                        <div class="card-body">
                            <div class="text-muted small">Students</div>
                            <div class="display-6 fw-bold mt-2">1,248</div>
                        </div>
                    </div>
                </div>
                <div class="col-md-6 col-xl-3">
                    <div class="card border-0 shadow-sm rounded-4 h-100">
                        <div class="card-body">
                            <div class="text-muted small">Exams</div>
                            <div class="display-6 fw-bold mt-2">42</div>
                        </div>
                    </div>
                </div>
                <div class="col-md-6 col-xl-3">
                    <div class="card border-0 shadow-sm rounded-4 h-100">
                        <div class="card-body">
                            <div class="text-muted small">Rooms</div>
                            <div class="display-6 fw-bold mt-2">18</div>
                        </div>
                    </div>
                </div>
                <div class="col-md-6 col-xl-3">
                    <div class="card border-0 shadow-sm rounded-4 h-100">
                        <div class="card-body">
                            <div class="text-muted small">Audit Events</div>
                            <div class="display-6 fw-bold mt-2">96</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </main>
</div>
<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
