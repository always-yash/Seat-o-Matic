<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="Question Paper Sets · Seat-o-Matic" />
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<main class="page-shell">
    <section class="hero">
        <div class="eyebrow">Module 4 · Paper Management</div>
        <h1>Question Paper Sets</h1>
        <p class="hero-copy">Manage question paper variants (Set A, Set B, Set C), allocate printed stock per examination and subject, and monitor inventory availability.</p>
    </section>

    <c:if test="${not empty sessionScope.flashSuccess}">
        <div class="alert" style="border: 1px solid rgba(121, 225, 192, .3); color: var(--mint); background: rgba(121, 225, 192, .1);">
            <c:out value="${sessionScope.flashSuccess}" />
        </div>
        <c:remove var="flashSuccess" scope="session" />
    </c:if>

    <c:if test="${not empty sessionScope.flashError}">
        <div class="alert alert-danger">
            <c:out value="${sessionScope.flashError}" />
        </div>
        <c:remove var="flashError" scope="session" />
    </c:if>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">
            <c:out value="${errorMessage}" />
        </div>
    </c:if>

    <!-- Filter by Exam -->
    <section class="card" style="padding: 20px 24px; margin-top: 28px;">
        <form method="get" action="${pageContext.request.contextPath}/papers" style="display: flex; gap: 16px; align-items: flex-end; flex-wrap: wrap;">
            <div style="flex: 1; min-width: 260px;">
                <label class="form-label" for="filterExam">Filter by Examination</label>
                <select id="filterExam" name="examId" class="form-control" style="background: rgba(20, 31, 52, .95);">
                    <option value="">-- All Examinations --</option>
                    <c:forEach var="ex" items="${exams}">
                        <option value="${ex.id}" ${ex.id == selectedExamId ? 'selected' : ''}>
                            <c:out value="${ex.title}" /> (<c:out value="${ex.examDate}" />)
                        </option>
                    </c:forEach>
                </select>
            </div>
            <button type="submit" class="button button-ghost">Apply Filter</button>
            <a href="${pageContext.request.contextPath}/papers" class="button button-ghost">Reset</a>
        </form>
    </section>

    <!-- Create New Paper Set Form -->
    <section class="card" style="padding: 26px; margin-top: 28px;">
        <div class="eyebrow" style="margin-bottom: 6px;">New Entry</div>
        <h2>Add Question Paper Set</h2>
        <form method="post" action="${pageContext.request.contextPath}/papers" style="margin-top: 18px; display: grid; grid-template-columns: repeat(auto-fit, minmax(210px, 1fr)); gap: 18px;">
            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
            <input type="hidden" name="action" value="create">

            <div>
                <label class="form-label" for="newExamId">Examination *</label>
                <select id="newExamId" name="examId" class="form-control" required style="background: rgba(20, 31, 52, .95);">
                    <option value="">-- Select Exam --</option>
                    <c:forEach var="ex" items="${exams}">
                        <option value="${ex.id}" ${ex.id == selectedExamId ? 'selected' : ''}>
                            <c:out value="${ex.title}" />
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div>
                <label class="form-label" for="newSubjectId">Subject *</label>
                <select id="newSubjectId" name="subjectId" class="form-control" required style="background: rgba(20, 31, 52, .95);">
                    <option value="">-- Select Subject --</option>
                    <c:forEach var="sub" items="${subjects}">
                        <option value="${sub.id}">
                            <c:out value="${sub.code}" /> - <c:out value="${sub.name}" />
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div>
                <label class="form-label" for="setCode">Set Code *</label>
                <input type="text" id="setCode" name="setCode" class="form-control" placeholder="e.g. Set A, Set B" required maxlength="50">
            </div>

            <div>
                <label class="form-label" for="totalCopies">Printed Stock *</label>
                <input type="number" id="totalCopies" name="totalCopies" class="form-control" placeholder="Total printed copies" min="0" required value="100">
            </div>

            <div style="display: flex; align-items: flex-end;">
                <button type="submit" class="button button-primary" style="width: 100%;">Create Paper Set</button>
            </div>
        </form>
    </section>

    <!-- Paper Sets Table -->
    <section class="card" style="padding: 26px; margin-top: 32px;">
        <div class="section-heading" style="margin-top: 0; margin-bottom: 20px;">
            <div>
                <div class="eyebrow">Inventory Overview</div>
                <h2>Configured Paper Sets (${fn:length(papers)})</h2>
            </div>
            <a href="${pageContext.request.contextPath}/distribution?examId=${selectedExamId != null ? selectedExamId : ''}" class="button button-ghost button-small">View Distribution Board →</a>
        </div>

        <c:choose>
            <c:when test="${empty papers}">
                <div style="padding: 40px; text-align: center; color: var(--ink-500);">
                    <p>No question paper sets found. Use the form above to configure paper variants.</p>
                </div>
            </c:when>
            <c:otherwise>
                <div style="overflow-x: auto;">
                    <table style="width: 100%; border-collapse: collapse; text-align: left; font-size: 0.92rem;">
                        <thead>
                            <tr style="border-bottom: 1px solid var(--line-strong); color: var(--ink-300);">
                                <th style="padding: 12px 14px;">ID</th>
                                <th style="padding: 12px 14px;">Examination</th>
                                <th style="padding: 12px 14px;">Subject</th>
                                <th style="padding: 12px 14px;">Set Variant</th>
                                <th style="padding: 12px 14px;">Printed Stock</th>
                                <th style="padding: 12px 14px;">Adjust Stock</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="p" items="${papers}">
                                <tr style="border-bottom: 1px solid var(--line);">
                                    <td style="padding: 14px; color: var(--ink-500);"><c:out value="${p.id}" /></td>
                                    <td style="padding: 14px; font-weight: 500; color: var(--white);"><c:out value="${p.examTitle}" /></td>
                                    <td style="padding: 14px;"><c:out value="${p.subjectCode}" /> - <c:out value="${p.subjectName}" /></td>
                                    <td style="padding: 14px;">
                                        <span class="status-pill"><c:out value="${p.setCode}" /></span>
                                    </td>
                                    <td style="padding: 14px; font-family: 'Space Grotesk', sans-serif; font-size: 1.15rem; font-weight: 700; color: var(--white);">
                                        <c:out value="${p.totalCopies}" />
                                    </td>
                                    <td style="padding: 14px;">
                                        <form method="post" action="${pageContext.request.contextPath}/papers" style="display: flex; gap: 8px; align-items: center;">
                                            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                                            <input type="hidden" name="action" value="updateStock">
                                            <input type="hidden" name="paperId" value="${p.id}">
                                            <input type="hidden" name="examId" value="${selectedExamId}">
                                            <input type="number" name="totalCopies" value="${p.totalCopies}" min="0" class="form-control" style="width: 85px; padding: 6px 10px; font-size: 0.85rem;" required>
                                            <button type="submit" class="button button-ghost button-small">Update</button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</main>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
