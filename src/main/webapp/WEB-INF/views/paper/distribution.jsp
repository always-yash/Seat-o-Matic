<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="Paper Distribution & Workflow · Seat-o-Matic" />
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<main class="page-shell">
    <section class="hero">
        <div class="eyebrow">Module 4 · Distribution Workflow</div>
        <h1>Paper Distribution Board</h1>
        <p class="hero-copy">Room-wise and subject-wise paper requirement tracking, configurable buffers, and guarded state transitions (PENDING → PREPARED → DISTRIBUTED → VERIFIED).</p>
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

    <!-- Controls: Exam Selector, Buffer, Actions -->
    <section class="card" style="padding: 24px; margin-top: 28px;">
        <div style="display: flex; gap: 20px; align-items: flex-end; flex-wrap: wrap; justify-content: space-between;">
            <form method="get" action="${pageContext.request.contextPath}/distribution" style="display: flex; gap: 14px; align-items: flex-end; flex-wrap: wrap;">
                <div style="min-width: 260px;">
                    <label class="form-label" for="selectExam">Examination Session</label>
                    <select id="selectExam" name="examId" class="form-control" style="background: rgba(20, 31, 52, .95);">
                        <c:forEach var="ex" items="${exams}">
                            <option value="${ex.id}" ${ex.id == selectedExamId ? 'selected' : ''}>
                                <c:out value="${ex.title}" /> (<c:out value="${ex.examDate}" />)
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div>
                    <label class="form-label" for="bufferInput">Buffer Copies</label>
                    <input type="number" id="bufferInput" name="buffer" value="${buffer != null ? buffer : 2}" min="0" class="form-control" style="width: 110px;">
                </div>
                <button type="submit" class="button button-ghost">View Board</button>
            </form>

            <div style="display: flex; gap: 12px; align-items: flex-end;">
                <!-- Recalculate & Persist Button -->
                <form method="post" action="${pageContext.request.contextPath}/distribution">
                    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                    <input type="hidden" name="action" value="calculate">
                    <input type="hidden" name="examId" value="${selectedExamId}">
                    <input type="hidden" name="buffer" value="${buffer != null ? buffer : 2}">
                    <button type="submit" class="button button-primary">Recalculate Requirements</button>
                </form>

                <!-- Batch Prepare Packets Button -->
                <form method="post" action="${pageContext.request.contextPath}/distribution">
                    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                    <input type="hidden" name="action" value="prepareAll">
                    <input type="hidden" name="examId" value="${selectedExamId}">
                    <button type="submit" class="button button-ghost">Batch Prepare Pending</button>
                </form>
            </div>
        </div>
    </section>

    <!-- Formula Explanation Card -->
    <div style="margin-top: 16px; padding: 14px 20px; border-radius: 12px; border: 1px solid var(--line); background: rgba(11, 17, 32, .6); font-size: 0.85rem; color: var(--ink-500);">
        <strong style="color: var(--blue-bright);">Requirement Formula:</strong> Required Papers = Assigned Students (for Room & Subject) + Configured Buffer (${buffer != null ? buffer : 2}). Mixed-subject rooms preserve separate subject allotments.
    </div>

    <!-- Requirements Breakdown Table -->
    <section class="card" style="padding: 26px; margin-top: 24px;">
        <div class="section-heading" style="margin-top: 0; margin-bottom: 20px;">
            <div>
                <div class="eyebrow">Room-Wise Allotments</div>
                <h2>Distribution Lifecycle Grid</h2>
            </div>
            <a href="${pageContext.request.contextPath}/papers?examId=${selectedExamId}" class="button button-ghost button-small">Manage Paper Stock →</a>
        </div>

        <c:choose>
            <c:when test="${empty requirements}">
                <div style="padding: 48px; text-align: center; color: var(--ink-500);">
                    <p style="font-size: 1.1rem; color: var(--ink-300);">No room paper requirements calculated yet.</p>
                    <p>Ensure that an approved seating plan exists for this examination, then click <strong>Recalculate Requirements</strong> above.</p>
                </div>
            </c:when>
            <c:otherwise>
                <div style="overflow-x: auto;">
                    <table style="width: 100%; border-collapse: collapse; text-align: left; font-size: 0.9rem;">
                        <thead>
                            <tr style="border-bottom: 1px solid var(--line-strong); color: var(--ink-300);">
                                <th style="padding: 12px 14px;">Room</th>
                                <th style="padding: 12px 14px;">Subject</th>
                                <th style="padding: 12px 14px; text-align: right;">Assigned</th>
                                <th style="padding: 12px 14px; text-align: right;">Buffer</th>
                                <th style="padding: 12px 14px; text-align: right;">Required</th>
                                <th style="padding: 12px 14px;">Paper Variant & Stock</th>
                                <th style="padding: 12px 14px; text-align: center;">Status</th>
                                <th style="padding: 12px 14px; text-align: right;">Dispatched / Collected</th>
                                <th style="padding: 12px 14px; min-width: 230px;">Workflow Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="r" items="${requirements}">
                                <tr style="border-bottom: 1px solid var(--line);">
                                    <td style="padding: 14px; font-weight: 700; color: var(--white); font-family: 'Space Grotesk', sans-serif;">
                                        <c:out value="${r.roomCode}" />
                                    </td>
                                    <td style="padding: 14px;">
                                        <div style="font-weight: 600; color: var(--ink-300);"><c:out value="${r.subjectCode}" /></div>
                                        <div style="font-size: 0.78rem; color: var(--ink-500);"><c:out value="${r.subjectName}" /></div>
                                    </td>
                                    <td style="padding: 14px; text-align: right; color: var(--white); font-weight: 600;">
                                        <c:out value="${r.studentCount}" />
                                    </td>
                                    <td style="padding: 14px; text-align: right; color: var(--ink-500);">
                                        +<c:out value="${r.bufferCount}" />
                                    </td>
                                    <td style="padding: 14px; text-align: right; font-family: 'Space Grotesk', sans-serif; font-size: 1.1rem; font-weight: 700; color: var(--blue-bright);">
                                        <c:out value="${r.totalRequired}" />
                                    </td>
                                    <td style="padding: 14px;">
                                        <c:choose>
                                            <c:when test="${not empty r.paperSetCode}">
                                                <span class="status-pill"><c:out value="${r.paperSetCode}" /></span>
                                                <c:choose>
                                                    <c:when test="${r.stockSufficient}">
                                                        <span style="font-size: 0.78rem; color: var(--mint); margin-left: 6px;">(<c:out value="${r.availableStock}" /> avail)</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span style="font-size: 0.78rem; color: var(--danger); margin-left: 6px; font-weight: 700;">⚠ Shortfall: <c:out value="${r.availableStock}" /></span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:when>
                                            <c:otherwise>
                                                <span style="color: var(--danger); font-size: 0.8rem;">No Paper Set</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="padding: 14px; text-align: center;">
                                        <c:choose>
                                            <c:when test="${r.distributionStatus == 'PENDING'}">
                                                <span class="status-pill" style="border-color: rgba(255, 193, 7, .3); color: #ffc107; background: rgba(255, 193, 7, .1);">PENDING</span>
                                            </c:when>
                                            <c:when test="${r.distributionStatus == 'PREPARED'}">
                                                <span class="status-pill" style="border-color: rgba(112, 167, 255, .3); color: var(--blue-bright); background: rgba(112, 167, 255, .1);">PREPARED</span>
                                            </c:when>
                                            <c:when test="${r.distributionStatus == 'DISTRIBUTED'}">
                                                <span class="status-pill" style="border-color: rgba(151, 133, 255, .3); color: var(--violet); background: rgba(151, 133, 255, .1);">DISTRIBUTED</span>
                                            </c:when>
                                            <c:when test="${r.distributionStatus == 'VERIFIED'}">
                                                <span class="status-pill" style="border-color: rgba(121, 225, 192, .3); color: var(--mint); background: rgba(121, 225, 192, .1);">VERIFIED</span>
                                            </c:when>
                                        </c:choose>
                                    </td>
                                    <td style="padding: 14px; text-align: right;">
                                        <span style="color: var(--white); font-weight: 600;"><c:out value="${r.distributedCount}" /></span>
                                        <span style="color: var(--ink-500);"> / </span>
                                        <span style="color: var(--white); font-weight: 600;"><c:out value="${r.collectedCount}" /></span>
                                    </td>
                                    <td style="padding: 14px;">
                                        <c:choose>
                                            <c:when test="${r.distributionStatus == 'PENDING'}">
                                                <!-- PENDING -> PREPARED -->
                                                <form method="post" action="${pageContext.request.contextPath}/distribution" style="display: inline-block;">
                                                    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                                                    <input type="hidden" name="action" value="prepare">
                                                    <input type="hidden" name="distributionId" value="${r.paperId != null ? r.paperId : ''}">
                                                    <input type="hidden" name="examId" value="${selectedExamId}">
                                                    <button type="submit" class="button button-ghost button-small" style="border-color: var(--blue);">Mark Prepared</button>
                                                </form>
                                            </c:when>
                                            <c:when test="${r.distributionStatus == 'PREPARED'}">
                                                <!-- PREPARED -> DISTRIBUTED -->
                                                <form method="post" action="${pageContext.request.contextPath}/distribution" style="display: flex; gap: 6px; align-items: center;">
                                                    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                                                    <input type="hidden" name="action" value="dispatch">
                                                    <input type="hidden" name="distributionId" value="${r.paperId}">
                                                    <input type="hidden" name="examId" value="${selectedExamId}">
                                                    <input type="number" name="distributedCount" value="${r.totalRequired}" min="1" class="form-control" style="width: 75px; padding: 5px 8px; font-size: 0.8rem;" required>
                                                    <button type="submit" class="button button-primary button-small">Dispatch</button>
                                                </form>
                                            </c:when>
                                            <c:when test="${r.distributionStatus == 'DISTRIBUTED'}">
                                                <!-- DISTRIBUTED -> VERIFIED -->
                                                <form method="post" action="${pageContext.request.contextPath}/distribution" style="display: flex; gap: 6px; align-items: center;">
                                                    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                                                    <input type="hidden" name="action" value="reconcile">
                                                    <input type="hidden" name="distributionId" value="${r.paperId}">
                                                    <input type="hidden" name="examId" value="${selectedExamId}">
                                                    <input type="number" name="collectedCount" value="${r.distributedCount}" min="0" class="form-control" style="width: 75px; padding: 5px 8px; font-size: 0.8rem;" required>
                                                    <button type="submit" class="button button-ghost button-small" style="border-color: var(--mint); color: var(--mint);">Verify</button>
                                                </form>
                                            </c:when>
                                            <c:when test="${r.distributionStatus == 'VERIFIED'}">
                                                <span style="color: var(--mint); font-size: 0.82rem; font-weight: 600;">✓ Reconciled (Terminal)</span>
                                            </c:when>
                                        </c:choose>
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
