<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Edit Item - Campus Lost & Found"/>
</jsp:include>
<jsp:include page="/includes/navbar.jsp"/>
<jsp:include page="/includes/alerts.jsp"/>

<main class="container py-4">
    <div class="row justify-content-center">
        <div class="col-lg-8">
            <div class="card shadow-sm border-0">
                <div class="card-header bg-white py-3 border-bottom d-flex align-items-center justify-content-between">
                    <h5 class="mb-0 fw-bold"><i class="bi bi-pencil-square text-primary me-2"></i>Edit Item Details</h5>
                    <span class="badge ${item.itemType eq 'LOST' ? 'badge-lost' : 'badge-found'}">
                        ${item.itemType}
                    </span>
                </div>
                <div class="card-body p-4">
                    <form action="${pageContext.request.contextPath}/items" method="post" enctype="multipart/form-data">
                        <input type="hidden" name="action" value="update">
                        <input type="hidden" name="itemId" value="${item.itemId}">
                        <input type="hidden" name="existingImage" value="${item.image}">

                        <div class="row g-3">
                            <div class="col-md-12">
                                <label for="title" class="form-label">Item Title <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="title" name="title" value="${item.title}" required minlength="3">
                            </div>

                            <div class="col-md-6">
                                <label for="categoryId" class="form-label">Category <span class="text-danger">*</span></label>
                                <select class="form-select" id="categoryId" name="categoryId" required>
                                    <c:forEach var="cat" items="${categories}">
                                        <option value="${cat.categoryId}" ${item.categoryId eq cat.categoryId ? 'selected' : ''}>${cat.name}</option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="col-md-6">
                                <label for="itemDate" class="form-label">Date <span class="text-danger">*</span></label>
                                <input type="date" class="form-control" id="itemDate" name="itemDate" value="${item.itemDate}" required>
                            </div>

                            <div class="col-md-6">
                                <label for="status" class="form-label">Status <span class="text-danger">*</span></label>
                                <select class="form-select" id="status" name="status" required>
                                    <option value="ACTIVE" ${item.status eq 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                                    <option value="MATCHED" ${item.status eq 'MATCHED' ? 'selected' : ''}>MATCHED</option>
                                    <option value="CLAIMED" ${item.status eq 'CLAIMED' ? 'selected' : ''}>CLAIMED</option>
                                    <option value="RETURNED" ${item.status eq 'RETURNED' ? 'selected' : ''}>RETURNED</option>
                                    <option value="CLOSED" ${item.status eq 'CLOSED' ? 'selected' : ''}>CLOSED</option>
                                </select>
                            </div>

                            <div class="col-md-6">
                                <label for="location" class="form-label">Campus Location <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="location" name="location" value="${item.location}" required>
                            </div>

                            <div class="col-md-5">
                                <label for="latitudeInput" class="form-label">Latitude</label>
                                <input type="number" step="any" class="form-control" id="latitudeInput" name="latitude" value="${item.latitude}">
                            </div>

                            <div class="col-md-5">
                                <label for="longitudeInput" class="form-label">Longitude</label>
                                <input type="number" step="any" class="form-control" id="longitudeInput" name="longitude" value="${item.longitude}">
                            </div>

                            <div class="col-md-2 d-flex align-items-end">
                                <button type="button" id="btnGetCoordinates" class="btn btn-outline-secondary w-100" title="Auto-detect GPS via Browser">
                                    <i class="bi bi-geo-alt"></i> GPS
                                </button>
                            </div>

                            <div class="col-md-12">
                                <label for="description" class="form-label">Description <span class="text-danger">*</span></label>
                                <textarea class="form-control" id="description" name="description" rows="4" required minlength="5">${item.description}</textarea>
                            </div>

                            <div class="col-md-12">
                                <label for="imageFileInput" class="form-label">Replace Photo (Optional, Max 5MB)</label>
                                <input type="file" class="form-control" id="imageFileInput" name="imageFile" accept="image/jpeg,image/png,image/webp">
                                <c:if test="${not empty item.image}">
                                    <div class="mt-2 small text-muted">
                                        Current photo: <a href="${pageContext.request.contextPath}/${item.image}" target="_blank">${item.image}</a>
                                    </div>
                                </c:if>
                                <div id="imagePreviewContainer" class="mt-2 d-none">
                                    <img id="imagePreview" src="#" alt="Preview" class="img-thumbnail" style="max-height: 180px;">
                                </div>
                            </div>
                        </div>

                        <div class="d-flex justify-content-between align-items-center mt-4 pt-3 border-top">
                            <a href="${pageContext.request.contextPath}/items?action=view&id=${item.itemId}" class="btn btn-outline-secondary">
                                Cancel
                            </a>
                            <button type="submit" class="btn btn-primary px-4 fw-semibold">
                                <i class="bi bi-save me-1"></i> Update Item
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</main>

<jsp:include page="/includes/footer.jsp"/>
