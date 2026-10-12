<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<footer>
    <div class="container-fluid px-lg-5">
        <div class="row g-4 mb-4">
            <div class="col-lg-4 col-md-6">
                <div class="d-flex align-items-center gap-2 mb-2">
                    <i class="bi bi-box-seam-fill text-primary fs-4"></i>
                    <h5 class="mb-0 fw-bold text-dark">Campus Lost &amp; Found Portal</h5>
                </div>
                <p class="small text-muted mb-0">
                    A centralized, secure digital lost and property recovery ecosystem designed for college students, faculty, and administrative departments.
                </p>
            </div>
            <div class="col-lg-2 col-md-3 col-6">
                <h6 class="fw-bold text-dark mb-3">Quick Navigation</h6>
                <ul class="list-unstyled small mb-0 d-flex flex-column gap-2">
                    <li><a href="${pageContext.request.contextPath}/items?action=gallery" class="text-decoration-none text-muted">Item Gallery</a></li>
                    <li><a href="${pageContext.request.contextPath}/search" class="text-decoration-none text-muted">Search &amp; Filters</a></li>
                    <li><a href="${pageContext.request.contextPath}/items?action=report-lost" class="text-decoration-none text-muted">Report Lost</a></li>
                    <li><a href="${pageContext.request.contextPath}/items?action=report-found" class="text-decoration-none text-muted">Report Found</a></li>
                </ul>
            </div>
            <div class="col-lg-2 col-md-3 col-6">
                <h6 class="fw-bold text-dark mb-3">Member Services</h6>
                <ul class="list-unstyled small mb-0 d-flex flex-column gap-2">
                    <li><a href="${pageContext.request.contextPath}/claims?action=my-claims" class="text-decoration-none text-muted">Track Claims</a></li>
                    <li><a href="${pageContext.request.contextPath}/profile" class="text-decoration-none text-muted">Account Profile</a></li>
                    <li><a href="${pageContext.request.contextPath}/change-password" class="text-decoration-none text-muted">Security Settings</a></li>
                    <li><a href="${pageContext.request.contextPath}/login.jsp" class="text-decoration-none text-muted">Portal Login</a></li>
                </ul>
            </div>
            <div class="col-lg-4 col-md-6">
                <h6 class="fw-bold text-dark mb-3">Security &amp; Verification</h6>
                <p class="small text-muted mb-2">
                    All handovers are verified with 6-digit Time-based OTPs, Google ZXing QR tokens, and recorded in persistent database audit trails.
                </p>
                <div class="d-flex gap-2">
                    <span class="badge bg-light text-secondary border">BCrypt Secured</span>
                    <span class="badge bg-light text-secondary border">ACID Transactions</span>
                    <span class="badge bg-light text-secondary border">Levenshtein &amp; Haversine Matching</span>
                </div>
            </div>
        </div>
        <hr class="text-muted opacity-25">
        <div class="d-flex flex-column flex-sm-row justify-content-between align-items-center small text-muted">
            <div>&copy; 2026 Campus Lost &amp; Found Management System. All rights reserved.</div>
            <div class="mt-2 mt-sm-0">Running on Apache Tomcat 11 &bull; Jakarta EE &bull; JDK 25</div>
        </div>
    </div>
</footer>

<!-- Bootstrap 5 JS Bundle -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<!-- Application JS -->
<script src="${pageContext.request.contextPath}/js/app.js"></script>
</body>
</html>
