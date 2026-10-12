<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="card shadow-sm border-0 mb-4">
    <div class="card-body p-4">
        <form action="${pageContext.request.contextPath}/search" method="get">
            <div class="row g-3">
                <div class="col-md-4">
                    <label for="keyword" class="form-label">Keyword</label>
                    <div class="input-group">
                        <span class="input-group-text bg-light"><i class="bi bi-search"></i></span>
                        <input type="text" class="form-control" id="keyword" name="keyword" value="${keyword}" placeholder="Title, brand, or content...">
                    </div>
                </div>

                <div class="col-md-3">
                    <label for="categoryId" class="form-label">Category</label>
                    <select class="form-select" id="categoryId" name="categoryId">
                        <option value="">All Categories</option>
                        <c:forEach var="cat" items="${categories}">
                            <option value="${cat.categoryId}" ${selectedCategory eq cat.categoryId ? 'selected' : ''}>${cat.name}</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="col-md-3">
                    <label for="location" class="form-label">Campus Location</label>
                    <div class="input-group">
                        <span class="input-group-text bg-light"><i class="bi bi-geo-alt"></i></span>
                        <input type="text" class="form-control" id="location" name="location" value="${selectedLocation}" placeholder="Library, Lab, Canteen...">
                    </div>
                </div>

                <div class="col-md-2">
                    <label for="type" class="form-label">Item Type</label>
                    <select class="form-select" id="type" name="type">
                        <option value="ALL" ${selectedType eq 'ALL' ? 'selected' : ''}>All Types</option>
                        <option value="LOST" ${selectedType eq 'LOST' ? 'selected' : ''}>Lost Only</option>
                        <option value="FOUND" ${selectedType eq 'FOUND' ? 'selected' : ''}>Found Only</option>
                    </select>
                </div>

                <div class="col-md-3">
                    <label for="date" class="form-label">Date</label>
                    <input type="date" class="form-control" id="date" name="date" value="${selectedDate}">
                </div>

                <div class="col-md-3">
                    <label for="status" class="form-label">Status</label>
                    <select class="form-select" id="status" name="status">
                        <option value="ALL" ${selectedStatus eq 'ALL' ? 'selected' : ''}>All Statuses</option>
                        <option value="ACTIVE" ${selectedStatus eq 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                        <option value="MATCHED" ${selectedStatus eq 'MATCHED' ? 'selected' : ''}>MATCHED</option>
                        <option value="CLAIMED" ${selectedStatus eq 'CLAIMED' ? 'selected' : ''}>CLAIMED</option>
                        <option value="RETURNED" ${selectedStatus eq 'RETURNED' ? 'selected' : ''}>RETURNED</option>
                    </select>
                </div>

                <div class="col-md-6 d-flex align-items-end gap-2">
                    <button type="submit" class="btn btn-primary px-4">
                        <i class="bi bi-funnel-fill me-1"></i> Apply Filters
                    </button>
                    <a href="${pageContext.request.contextPath}/search" class="btn btn-outline-secondary">
                        Reset
                    </a>
                </div>
            </div>
        </form>
    </div>
</div>
