<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>

        <jsp:include page="/includes/header.jsp">
            <jsp:param name="title" value="Report Found Item - Campus Lost & Found Portal" />
        </jsp:include>
        <jsp:include page="/includes/navbar.jsp" />
        <jsp:include page="/includes/alerts.jsp" />

        <main class="container py-4">
            <div class="row justify-content-center">
                <div class="col-lg-8">
                    <div class="card shadow-sm border-0">
                        <div
                            class="card-header bg-success-subtle text-success py-3 border-bottom d-flex align-items-center justify-content-between">
                            <h5 class="mb-0 fw-bold"><i class="bi bi-box2-heart-fill me-2"></i>Report a Found Item</h5>
                            <span class="badge bg-success text-white">FOUND ITEM</span>
                        </div>
                        <div class="card-body p-4">
                            <form action="${pageContext.request.contextPath}/items" method="post"
                                enctype="multipart/form-data">
                                <input type="hidden" name="action" value="create">
                                <input type="hidden" name="itemType" value="FOUND">

                                <div class="row g-3">
                                    <div class="col-md-12">
                                        <label for="title" class="form-label">Item Title <span
                                                class="text-danger">*</span></label>
                                        <input type="text" class="form-control" id="title" name="title"
                                            placeholder="e.g. Black Leather Men Wallet found on bench" required
                                            minlength="3">
                                        <div class="form-text small">Provide a clear description of the found object.
                                        </div>
                                    </div>

                                    <div class="col-md-6">
                                        <label for="categoryId" class="form-label">Category <span
                                                class="text-danger">*</span></label>
                                        <select class="form-select" id="categoryId" name="categoryId" required>
                                            <option value="">-- Choose Category --</option>
                                            <c:forEach var="cat" items="${categories}">
                                                <option value="${cat.categoryId}">${cat.name}</option>
                                            </c:forEach>
                                        </select>
                                    </div>

                                    <div class="col-md-6">
                                        <label for="itemDate" class="form-label">Date Found <span
                                                class="text-danger">*</span></label>
                                        <input type="date" class="form-control" id="itemDate" name="itemDate" required>
                                    </div>

                                    <div class="col-md-12">
                                        <label for="location" class="form-label">Where Was It Found? <span
                                                class="text-danger">*</span></label>
                                        <input type="text" class="form-control" id="location" name="location"
                                            placeholder="e.g. Sports Complex, near basketball pavilion seating"
                                            required>
                                    </div>

                                    <div class="col-md-5">
                                        <label for="latitudeInput" class="form-label">Latitude (Optional)</label>
                                        <input type="number" step="any" class="form-control" id="latitudeInput"
                                            name="latitude" placeholder="28.613920">
                                    </div>

                                    <div class="col-md-5">
                                        <label for="longitudeInput" class="form-label">Longitude (Optional)</label>
                                        <input type="number" step="any" class="form-control" id="longitudeInput"
                                            name="longitude" placeholder="77.209020">
                                    </div>

                                    <div class="col-md-2 d-flex align-items-end">
                                        <button type="button" id="btnGetCoordinates"
                                            class="btn btn-outline-secondary w-100" title="Auto-detect GPS via Browser">
                                            <i class="bi bi-geo-alt"></i> GPS
                                        </button>
                                    </div>

                                    <div class="col-md-12">
                                        <label for="description" class="form-label">Found Item Details &amp; Custody
                                            <span class="text-danger">*</span></label>
                                        <textarea class="form-control" id="description" name="description" rows="4"
                                            placeholder="Mention where the item is currently kept (e.g. handed to Security desk, Dean office, or kept with finder)."
                                            required minlength="5"></textarea>
                                    </div>

                                    <div class="col-md-12">
                                        <label for="imageFileInput" class="form-label">Upload Photo of Found Item (Max
                                            5MB)</label>
                                        <input type="file" class="form-control" id="imageFileInput" name="imageFile"
                                            accept="image/jpeg,image/png,image/webp">
                                        <div class="form-text small">Accepted formats: JPG, JPEG, PNG, WEBP.</div>

                                        <div id="imagePreviewContainer" class="mt-2 d-none">
                                            <img id="imagePreview" src="#" alt="Preview" class="img-thumbnail"
                                                style="max-height: 180px;">
                                        </div>
                                    </div>
                                </div>
                                <div class="mt-4 p-3 border rounded bg-light">
                                    <div class="d-flex justify-content-between align-items-center mb-2">
                                        <div>
                                            <h6 class="fw-bold mb-1">
                                                <i class="bi bi-stars text-primary me-1"></i>
                                                AI Item Analysis
                                            </h6>
                                            <small class="text-muted">
                                                Analyze the found item details and get smart suggestions.
                                            </small>
                                        </div>

                                        <button type="button" id="btnAIAnalyze" class="btn btn-primary btn-sm">
                                            <i class="bi bi-stars me-1"></i> Analyze with AI
                                        </button>
                                    </div>

                                    <div id="aiAnalysisResult" class="d-none mt-3">
                                        <div class="alert alert-primary mb-0">
                                            <strong>AI Analysis Result</strong>
                                            <hr>

                                            <p class="mb-1">
                                                <strong>Suggested Keywords:</strong>
                                                <span id="aiKeywords"></span>
                                            </p>

                                            <p class="mb-1">
                                                <strong>Item Details:</strong>
                                                <span id="aiDetails"></span>
                                            </p>

                                            <p class="mb-0">
                                                <strong>Match Readiness:</strong>
                                                <span id="aiReadiness" class="badge bg-success"></span>
                                            </p>
                                        </div>
                                    </div>
                                </div>

                                <div class="d-flex justify-content-between align-items-center mt-4 pt-3 border-top">
                                    <a href="${pageContext.request.contextPath}/items?action=gallery"
                                        class="btn btn-outline-secondary">
                                        Cancel
                                    </a>
                                    <button type="submit" class="btn btn-success px-4 fw-semibold">
                                        <i class="bi bi-send me-1"></i> Submit Found Report
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </main>
        <script>
            document.getElementById("btnAIAnalyze").addEventListener("click", async function () {

                const title = document.getElementById("title").value.trim();
                const description = document.getElementById("description").value.trim();
                const location = document.getElementById("location").value.trim();

                if (!title || !description || !location) {
                    alert("Please enter title, description and location first.");
                    return;
                }

                const button = this;
                button.disabled = true;
                button.innerHTML = "Analyzing...";

                try {
                    const formData = new URLSearchParams();

                    formData.append("title", title);
                    formData.append("description", description);
                    formData.append("location", location);

                    const response = await fetch(
                        "${pageContext.request.contextPath}/api/ai-analysis",
                        {
                            method: "POST",
                            headers: {
                                "Content-Type": "application/x-www-form-urlencoded"
                            },
                            body: formData
                        }
                    );

                    const data = await response.json();

                    if (!response.ok) {
                        throw new Error(data.error || "Analysis failed");
                    }

                    document.getElementById("aiDetails").textContent =
                        data.analysis;

                    document.getElementById("aiKeywords").textContent =
                        "AI analyzed successfully";

                    document.getElementById("aiReadiness").textContent =
                        "AI READY";

                    document.getElementById("aiAnalysisResult")
                        .classList.remove("d-none");

                } catch (error) {

                    alert("AI Analysis failed: " + error.message);

                } finally {

                    button.disabled = false;

                    button.innerHTML =
                        '<i class="bi bi-stars me-1"></i> Analyze with AI';
                }
            });
        </script>
        <jsp:include page="/includes/footer.jsp" />