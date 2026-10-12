<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="Invigilator Room Workspace · Seat-o-Matic" />
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<main class="page-shell">
    <section class="hero">
        <div class="eyebrow">Module 4 · Invigilation Desk</div>
        <h1>Room Operations Workspace</h1>
        <p class="hero-copy">Room-scoped invigilator console for monitoring paper distribution, tracking candidate presence, and submitting answer script collection reconciliation.</p>
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

    <c:choose>
        <c:when test="${noAssignments}">
            <!-- Empty state: No room assigned -->
            <section class="card" style="padding: 48px; text-align: center; margin-top: 32px;">
                <div style="font-size: 3rem; margin-bottom: 12px;">📋</div>
                <h2>No Active Room Assignment</h2>
                <p style="color: var(--ink-500); max-width: 480px; margin: 12px auto 24px;">
                    You are not currently assigned to any examination rooms for the active session. If you believe this is an error, please contact the examination control cell.
                </p>
                <a href="${pageContext.request.contextPath}/dashboard" class="button button-ghost">Return to Dashboard</a>
            </section>
        </c:when>

        <c:otherwise>
            <!-- Exam & Room Session Summary Card -->
            <section class="card" style="padding: 28px; margin-top: 28px;">
                <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 20px;">
                    <div>
                        <div class="eyebrow">Assigned Room</div>
                        <h2 style="font-size: 2rem; color: var(--blue-bright); margin-top: 4px;">
                            Room <c:out value="${roomView.roomCode}" />
                        </h2>
                        <div style="margin-top: 8px; font-size: 1.05rem; color: var(--white); font-weight: 500;">
                            <c:out value="${roomView.examTitle}" />
                        </div>
                        <div style="margin-top: 4px; font-size: 0.88rem; color: var(--ink-500);">
                            Date: <span style="color: var(--ink-300);"><c:out value="${roomView.examDate}" /></span> ·
                            Session: <span style="color: var(--ink-300);"><c:out value="${roomView.startTime}" /> – <c:out value="${roomView.endTime}" /></span>
                        </div>
                    </div>

                    <div style="text-align: right;">
                        <div style="margin-bottom: 8px;">
                            <span class="role-badge"><c:out value="${roomView.assignmentStatus != null ? roomView.assignmentStatus : 'ACTIVE'}" /></span>
                        </div>
                        <c:choose>
                            <c:when test="${roomView.reconciled}">
                                <span class="status-pill" style="border-color: rgba(121, 225, 192, .4); color: var(--mint); background: rgba(121, 225, 192, .15);">
                                    ✓ All Scripts Reconciled
                                </span>
                            </c:when>
                            <c:otherwise>
                                <span class="status-pill status-dot">Session in Progress</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </section>

            <!-- Room Stat Grid -->
            <section class="stat-grid" style="margin-top: 24px;">
                <article class="card stat-card">
                    <div class="stat-label">Assigned Candidates</div>
                    <div class="stat-value"><c:out value="${roomView.totalAssignedStudents}" /></div>
                </article>
                <article class="card stat-card">
                    <div class="stat-label">Total Required Papers</div>
                    <div class="stat-value"><c:out value="${roomView.totalRequiredPapers}" /></div>
                </article>
                <article class="card stat-card">
                    <div class="stat-label">Dispatched Papers</div>
                    <div class="stat-value" style="color: var(--violet);"><c:out value="${roomView.totalDistributedPapers}" /></div>
                </article>
                <article class="card stat-card">
                    <div class="stat-label">Collected Answer Scripts</div>
                    <div class="stat-value" style="color: var(--mint);"><c:out value="${roomView.totalCollectedScripts}" /></div>
                </article>
            </section>

            <!-- Question Paper Requirements & Collection Table -->
            <section class="card" style="padding: 26px; margin-top: 28px;">
                <div class="section-heading" style="margin-top: 0; margin-bottom: 20px;">
                    <div>
                        <div class="eyebrow">Inventory Checklist</div>
                        <h2>Question Papers & Collection Submission</h2>
                    </div>
                </div>

                <div style="overflow-x: auto;">
                    <table style="width: 100%; border-collapse: collapse; text-align: left; font-size: 0.92rem;">
                        <thead>
                            <tr style="border-bottom: 1px solid var(--line-strong); color: var(--ink-300);">
                                <th style="padding: 12px 14px;">Subject</th>
                                <th style="padding: 12px 14px;">Paper Set</th>
                                <th style="padding: 12px 14px; text-align: right;">Allotted (incl. Buffer)</th>
                                <th style="padding: 12px 14px; text-align: right;">Distributed</th>
                                <th style="padding: 12px 14px; text-align: right;">Collected</th>
                                <th style="padding: 12px 14px; text-align: center;">Status</th>
                                <th style="padding: 12px 14px; min-width: 220px;">Reconciliation Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="req" items="${roomView.requirements}">
                                <tr style="border-bottom: 1px solid var(--line);">
                                    <td style="padding: 14px;">
                                        <div style="font-weight: 600; color: var(--white);"><c:out value="${req.subjectCode}" /></div>
                                        <div style="font-size: 0.8rem; color: var(--ink-500);"><c:out value="${req.subjectName}" /></div>
                                    </td>
                                    <td style="padding: 14px;">
                                        <span class="status-pill"><c:out value="${req.paperSetCode != null ? req.paperSetCode : 'Standard'}" /></span>
                                    </td>
                                    <td style="padding: 14px; text-align: right; font-weight: 600; color: var(--white);">
                                        <c:out value="${req.totalRequired}" />
                                    </td>
                                    <td style="padding: 14px; text-align: right; font-weight: 600; color: var(--violet);">
                                        <c:out value="${req.distributedCount}" />
                                    </td>
                                    <td style="padding: 14px; text-align: right; font-weight: 600; color: var(--mint);">
                                        <c:out value="${req.collectedCount}" />
                                    </td>
                                    <td style="padding: 14px; text-align: center;">
                                        <c:choose>
                                            <c:when test="${req.distributionStatus == 'VERIFIED'}">
                                                <span class="status-pill" style="border-color: rgba(121, 225, 192, .3); color: var(--mint); background: rgba(121, 225, 192, .1);">VERIFIED</span>
                                            </c:when>
                                            <c:when test="${req.distributionStatus == 'DISTRIBUTED'}">
                                                <span class="status-pill" style="border-color: rgba(151, 133, 255, .3); color: var(--violet); background: rgba(151, 133, 255, .1);">DISTRIBUTED</span>
                                            </c:when>
                                            <c:when test="${req.distributionStatus == 'PREPARED'}">
                                                <span class="status-pill" style="border-color: rgba(112, 167, 255, .3); color: var(--blue-bright); background: rgba(112, 167, 255, .1);">PREPARED</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="status-pill" style="border-color: rgba(255, 193, 7, .3); color: #ffc107; background: rgba(255, 193, 7, .1);">PENDING</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="padding: 14px;">
                                        <c:choose>
                                            <c:when test="${req.distributionStatus == 'DISTRIBUTED'}">
                                                <!-- Action: Submit collected scripts -->
                                                <form method="post" action="${pageContext.request.contextPath}/invigilator/room" style="display: flex; gap: 8px; align-items: center;">
                                                    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                                                    <input type="hidden" name="action" value="submitCollection">
                                                    <input type="hidden" name="examId" value="${roomView.examId}">
                                                    <input type="hidden" name="roomId" value="${roomView.roomId}">
                                                    <input type="hidden" name="distributionId" value="${req.paperId}">
                                                    <input type="number" name="collectedCount" value="${req.distributedCount}" min="0" class="form-control" style="width: 80px; padding: 6px 10px; font-size: 0.85rem;" required>
                                                    <button type="submit" class="button button-primary button-small">Submit & Verify</button>
                                                </form>
                                            </c:when>
                                            <c:when test="${req.distributionStatus == 'VERIFIED'}">
                                                <span style="color: var(--mint); font-weight: 600; font-size: 0.85rem;">✓ Reconciled & Closed</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span style="color: var(--ink-500); font-size: 0.82rem;">Awaiting dispatch</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </section>

            <!-- Assigned Students Roll Numbers List -->
            <section class="card" style="padding: 26px; margin-top: 28px;">
                <div class="eyebrow" style="margin-bottom: 6px;">Authorized Seating</div>
                <h2>Assigned Candidates (<c:out value="${fn:length(roomView.studentRollNumbers)}" />)</h2>
                <p style="color: var(--ink-500); font-size: 0.88rem; margin: 4px 0 18px;">
                    Candidates officially assigned to seats in Room <c:out value="${roomView.roomCode}" /> under the active plan.
                </p>

                <c:choose>
                    <c:when test="${empty roomView.studentRollNumbers}">
                        <p style="color: var(--ink-500); font-style: italic;">No student assignments recorded for this room.</p>
                    </c:when>
                    <c:otherwise>
                        <div style="display: flex; flex-wrap: wrap; gap: 8px;">
                            <c:forEach var="roll" items="${roomView.studentRollNumbers}">
                                <span class="status-pill" style="font-family: monospace; font-size: 0.82rem; background: rgba(255, 255, 255, .04); border-color: var(--line);">
                                    <c:out value="${roll}" />
                                </span>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>
        </c:otherwise>
    </c:choose>
</main>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
