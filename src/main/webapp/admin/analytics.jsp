<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Analytics & Trends - Admin Portal"/>
</jsp:include>
<jsp:include page="/includes/navbar.jsp"/>
<jsp:include page="/includes/alerts.jsp"/>

<main class="container-fluid px-lg-4 py-4">
    <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
        <div>
            <h4 class="fw-bold mb-1"><i class="bi bi-graph-up text-primary me-2"></i>Campus Recovery Analytics</h4>
            <p class="text-muted small mb-0">Visual data representations dynamically rendered from current database records</p>
        </div>
        <a href="${pageContext.request.contextPath}/admin/export-pdf" class="btn btn-outline-danger btn-sm">
            <i class="bi bi-file-earmark-pdf-fill me-1"></i> Export PDF Summary
        </a>
    </div>

    <!-- Quick Metrics Row -->
    <div class="row g-3 mb-4">
        <div class="col-md-3">
            <div class="card p-3 shadow-sm border-0 text-center">
                <div class="text-muted small">LOST ITEMS RATIO</div>
                <h3 class="fw-bold text-danger mb-0">${stats.lostItems}</h3>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card p-3 shadow-sm border-0 text-center">
                <div class="text-muted small">FOUND ITEMS LOGGED</div>
                <h3 class="fw-bold text-success mb-0">${stats.foundItems}</h3>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card p-3 shadow-sm border-0 text-center">
                <div class="text-muted small">REUNITED HANDOVERS</div>
                <h3 class="fw-bold text-primary mb-0">${stats.returnedItems}</h3>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card p-3 shadow-sm border-0 text-center">
                <div class="text-muted small">TOTAL RECOVERY RATE</div>
                <h3 class="fw-bold text-dark mb-0">
                    <c:choose>
                        <c:when test="${stats.totalItems gt 0}">
                            <fmt:formatNumber type="number" maxFractionDigits="0" value="${(stats.returnedItems * 100.0) / stats.totalItems}"/>%
                        </c:when>
                        <c:otherwise>0%</c:otherwise>
                    </c:choose>
                </h3>
            </div>
        </div>
    </div>

    <!-- Visual Charts Row -->
    <div class="row g-4">
        <!-- Category Distribution Chart -->
        <div class="col-lg-6">
            <div class="card shadow-sm border-0 h-100">
                <div class="card-header bg-white py-3 border-bottom">
                    <h6 class="fw-bold mb-0 text-dark"><i class="bi bi-pie-chart-fill text-primary me-2"></i>Items by Category</h6>
                </div>
                <div class="card-body p-4 d-flex align-items-center justify-content-center">
                    <div style="width: 100%; max-height: 350px;">
                        <canvas id="categoryChart"></canvas>
                    </div>
                </div>
            </div>
        </div>

        <!-- Status Breakdown Chart -->
        <div class="col-lg-6">
            <div class="card shadow-sm border-0 h-100">
                <div class="card-header bg-white py-3 border-bottom">
                    <h6 class="fw-bold mb-0 text-dark"><i class="bi bi-bar-chart-fill text-success me-2"></i>Item Status Breakdown</h6>
                </div>
                <div class="card-body p-4 d-flex align-items-center justify-content-center">
                    <div style="width: 100%; max-height: 350px;">
                        <canvas id="statusChart"></canvas>
                    </div>
                </div>
            </div>
        </div>
    </div>
</main>

<script>
document.addEventListener("DOMContentLoaded", function () {
    // 1. Category Chart
    const catLabels = [];
    const catData = [];
    <c:forEach var="entry" items="${analytics.categoryCounts}">
        catLabels.push("${entry.key}");
        catData.push(${entry.value});
    </c:forEach>

    const ctxCat = document.getElementById("categoryChart");
    if (ctxCat && catLabels.length > 0) {
        new Chart(ctxCat, {
            type: "doughnut",
            data: {
                labels: catLabels,
                datasets: [{
                    data: catData,
                    backgroundColor: [
                        "#2563eb", "#3b82f6", "#60a5fa", "#10b981", "#34d399",
                        "#f59e0b", "#fbbf24", "#ef4444", "#8b5cf6", "#ec4899",
                        "#64748b", "#94a3b8"
                    ]
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: "right" }
                }
            }
        });
    }

    // 2. Status Chart
    const statusLabels = [];
    const statusData = [];
    <c:forEach var="entry" items="${analytics.statusCounts}">
        statusLabels.push("${entry.key}");
        statusData.push(${entry.value});
    </c:forEach>

    const ctxStat = document.getElementById("statusChart");
    if (ctxStat && statusLabels.length > 0) {
        new Chart(ctxStat, {
            type: "bar",
            data: {
                labels: statusLabels,
                datasets: [{
                    label: "Count",
                    data: statusData,
                    backgroundColor: "#2563eb",
                    borderRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: { beginAtZero: true, ticks: { precision: 0 } }
                },
                plugins: {
                    legend: { display: false }
                }
            }
        });
    }
});
</script>

<jsp:include page="/includes/footer.jsp"/>
