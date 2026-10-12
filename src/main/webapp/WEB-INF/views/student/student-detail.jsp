<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="${student.name} (${student.enrollmentNo}) · Student Profile · Seat-o-Matic" />
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<main class="page-shell">
    <!-- Breadcrumb -->
    <div style="margin-bottom: 24px;">
        <a href="${pageContext.request.contextPath}/students" class="button button-ghost button-small">
            ← Back to Student Directory
        </a>
    </div>

    <!-- Student Profile Header Banner -->
    <section class="card" style="padding: 32px 36px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 24px;">
        <div style="display: flex; gap: 20px; align-items: center;">
            <div style="width: 64px; height: 64px; border-radius: 50%; background: linear-gradient(135deg, var(--blue), var(--violet)); display: grid; place-items: center; color: var(--ink-950); font-family: 'Space Grotesk', sans-serif; font-size: 1.6rem; font-weight: 700; box-shadow: var(--glow-brand);">
                ${fn:substring(student.name, 0, 1)}
            </div>
            <div>
                <div style="display: flex; align-items: center; gap: 12px; flex-wrap: wrap;">
                    <h1 style="margin: 0; font-size: 1.85rem;"><c:out value="${student.name}" /></h1>
                    <c:choose>
                        <c:when test="${student.active}">
                            <span style="padding: 4px 12px; border-radius: var(--radius-pill); font-size: 0.75rem; font-weight: 700; text-transform: uppercase; border: 1px solid rgba(52, 211, 153, 0.4); color: var(--mint); background: rgba(52, 211, 153, 0.12);">
                                ACTIVE
                            </span>
                        </c:when>
                        <c:otherwise>
                            <span style="padding: 4px 12px; border-radius: var(--radius-pill); font-size: 0.75rem; font-weight: 700; text-transform: uppercase; border: 1px solid rgba(251, 191, 36, 0.4); color: var(--warning); background: rgba(251, 191, 36, 0.12);">
                                INACTIVE
                            </span>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div style="margin-top: 6px; font-family: monospace; font-size: 1.05rem; color: var(--blue-bright);">
                    <c:out value="${student.enrollmentNo}" />
                </div>
            </div>
        </div>

        <div style="display: flex; gap: 12px; align-items: center; flex-wrap: wrap;">
            <a href="${pageContext.request.contextPath}/students?action=edit&id=${student.id}" class="button button-primary">
                Edit Profile
            </a>
            <form method="post" action="${pageContext.request.contextPath}/students" style="display: inline; margin: 0;">
                <input type="hidden" name="_csrf" value="${csrfToken}">
                <input type="hidden" name="action" value="toggle-status">
                <input type="hidden" name="id" value="${student.id}">
                <input type="hidden" name="redirect" value="/students?action=view&id=${student.id}">
                <button type="submit" class="button button-ghost">
                    ${student.active ? 'Disable Candidate' : 'Activate Candidate'}
                </button>
            </form>
            <form method="post" action="${pageContext.request.contextPath}/students" style="display: inline; margin: 0;" onsubmit="return confirm('Are you sure you want to permanently delete this student record?');">
                <input type="hidden" name="_csrf" value="${csrfToken}">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="id" value="${student.id}">
                <button type="submit" class="button button-ghost" style="color: var(--danger); border-color: rgba(248, 113, 113, .3);">
                    Delete
                </button>
            </form>
        </div>
    </section>

    <!-- Two-Column Profile & Eligibility Grid -->
    <div style="display: grid; grid-template-columns: 360px 1fr; gap: 24px; margin-top: 24px;">
        <!-- Left: Academic Cohort Identity -->
        <section class="card" style="padding: 28px;">
            <div class="eyebrow">Academic Registry</div>
            <h2 style="margin-top: 4px; margin-bottom: 20px;">Cohort Placement</h2>

            <div style="display: flex; flex-direction: column; gap: 16px;">
                <div>
                    <div style="font-size: 0.78rem; text-transform: uppercase; color: var(--ink-500); font-weight: 600; letter-spacing: 0.08em;">Academic Department</div>
                    <div style="margin-top: 4px; font-weight: 600; color: var(--white); font-size: 1rem;">
                        <c:out value="${student.branchCode}" /> · <c:out value="${student.branchName}" />
                    </div>
                </div>

                <div>
                    <div style="font-size: 0.78rem; text-transform: uppercase; color: var(--ink-500); font-weight: 600; letter-spacing: 0.08em;">Class Section</div>
                    <div style="margin-top: 4px; font-weight: 600; color: var(--white); font-size: 1rem;">
                        <c:out value="${student.sectionName}" />
                    </div>
                </div>

                <div>
                    <div style="font-size: 0.78rem; text-transform: uppercase; color: var(--ink-500); font-weight: 600; letter-spacing: 0.08em;">Current Semester</div>
                    <div style="margin-top: 4px; font-weight: 600; color: var(--white); font-size: 1rem;">
                        Semester <c:out value="${student.semesterNumber}" /> (<c:out value="${student.semesterName}" />)
                    </div>
                </div>

                <div>
                    <div style="font-size: 0.78rem; text-transform: uppercase; color: var(--ink-500); font-weight: 600; letter-spacing: 0.08em;">Enrolled Exams</div>
                    <div style="margin-top: 4px; font-weight: 700; color: var(--blue-bright); font-size: 1.15rem;">
                        <c:out value="${student.examCount}" /> exams
                    </div>
                </div>

                <div>
                    <div style="font-size: 0.78rem; text-transform: uppercase; color: var(--ink-500); font-weight: 600; letter-spacing: 0.08em;">Seating Allocations</div>
                    <div style="margin-top: 4px; font-weight: 700; color: var(--mint); font-size: 1.15rem;">
                        <c:out value="${student.seatAssignmentCount}" /> desks assigned
                    </div>
                </div>

                <div>
                    <div style="font-size: 0.78rem; text-transform: uppercase; color: var(--ink-500); font-weight: 600; letter-spacing: 0.08em;">Registration Date</div>
                    <div style="margin-top: 4px; color: var(--ink-300); font-size: 0.9rem;">
                        <c:out value="${student.createdAt != null ? student.createdAt : 'Historical Record'}" />
                    </div>
                </div>
            </div>
        </section>

        <!-- Right: Examination Eligibility and Seating Allocation -->
        <section class="card" style="padding: 28px;">
            <div class="eyebrow">Exam Cockpit Integration</div>
            <h2 style="margin-top: 4px; margin-bottom: 20px;">Upcoming Examination Eligibility</h2>

            <c:choose>
                <c:when test="${empty eligibleExams}">
                    <div style="padding: 48px 20px; text-align: center; color: var(--ink-500);">
                        <div style="font-size: 2.2rem; margin-bottom: 10px; opacity: 0.6;">📅</div>
                        <p style="font-size: 1.05rem; color: var(--ink-300); margin: 0 0 6px;">No examination syllabi assigned yet.</p>
                        <p style="font-size: 0.85rem; margin: 0;">This candidate is not currently linked to scheduled examination papers in <code>exam_students</code>.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div style="display: flex; flex-direction: column; gap: 16px;">
                        <c:forEach var="ex" items="${eligibleExams}">
                            <div style="padding: 18px 20px; border-radius: var(--radius-md); border: 1px solid var(--line); background: rgba(15, 23, 42, 0.6); display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 14px;">
                                <div>
                                    <div style="display: flex; align-items: center; gap: 10px;">
                                        <h3 style="font-size: 1.05rem; margin: 0;"><c:out value="${ex.examTitle}" /></h3>
                                        <span class="badge" style="background: rgba(56, 189, 248, .12); color: var(--blue-bright); border: 1px solid rgba(56, 189, 248, .3); padding: 2px 8px; font-size: 0.75rem; border-radius: var(--radius-sm);">
                                            <c:out value="${ex.subjectCode}" />
                                        </span>
                                    </div>
                                    <div style="margin-top: 6px; font-size: 0.85rem; color: var(--ink-300);">
                                        <c:out value="${ex.subjectName}" />
                                    </div>
                                    <div style="margin-top: 4px; font-size: 0.8rem; color: var(--ink-500);">
                                        Date: <strong><c:out value="${ex.examDate}" /></strong> · Time: <c:out value="${ex.startTime}" /> - <c:out value="${ex.endTime}" />
                                    </div>
                                </div>

                                <div style="text-align: right;">
                                    <div style="font-size: 0.78rem; text-transform: uppercase; color: var(--ink-500); font-weight: 600; letter-spacing: 0.08em; margin-bottom: 4px;">Desk Allocation</div>
                                    <c:choose>
                                        <c:when test="${ex.seated}">
                                            <span style="display: inline-block; padding: 6px 12px; border-radius: var(--radius-sm); font-size: 0.85rem; font-weight: 700; color: var(--white); background: rgba(56, 189, 248, .18); border: 1px solid rgba(56, 189, 248, .4);">
                                                Room <c:out value="${ex.roomCode}" /> · Row ${ex.seatRow}, Col ${ex.seatCol}
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="display: inline-block; padding: 6px 12px; border-radius: var(--radius-sm); font-size: 0.82rem; font-weight: 600; color: var(--warning); background: rgba(251, 191, 36, .12); border: 1px solid rgba(251, 191, 36, .3);">
                                                Pending Seating Plan
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </div>
</main>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
