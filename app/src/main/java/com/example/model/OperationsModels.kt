package com.example.model

enum class OpsUserRole(val displayName: String, val badgeColor: Long) {
    ADMIN("College Admin", 0xFF4338CA),
    EVENT_HEAD("Event Head", 0xFF0D9488),
    TEAM_MEMBER("Team Member", 0xFF6366F1),
    FACULTY_COORDINATOR("Faculty Coordinator", 0xFFD97706)
}

enum class EventLifecycleStatus(val displayName: String, val colorHex: Long) {
    DRAFT("Draft", 0xFF64748B),
    PLANNING("Planning", 0xFF0284C7),
    APPROVED("Approved", 0xFF0D9488),
    UPCOMING("Upcoming", 0xFF6366F1),
    ONGOING("Ongoing", 0xFF10B981),
    COMPLETED("Completed", 0xFF3B82F6),
    CANCELLED("Cancelled", 0xFFEF4444)
}

enum class TaskStatus(val displayName: String, val colorHex: Long) {
    TO_DO("To Do", 0xFF64748B),
    IN_PROGRESS("In Progress", 0xFF0284C7),
    BLOCKED("Blocked", 0xFFEF4444),
    COMPLETED("Completed", 0xFF10B981),
    OVERDUE("Overdue", 0xFFDC2626)
}

enum class TaskPriority(val displayName: String, val colorHex: Long) {
    LOW("Low", 0xFF64748B),
    MEDIUM("Medium", 0xFF0284C7),
    HIGH("High", 0xFFF59E0B),
    CRITICAL("Critical", 0xFFEF4444)
}

enum class TeamCategory(val displayName: String) {
    ALL("All Categories"),
    TECHNICAL("Technical"),
    DESIGN("Design"),
    MARKETING("Marketing"),
    SPONSORSHIP("Sponsorship"),
    FINANCE("Finance"),
    HOSPITALITY("Hospitality"),
    LOGISTICS("Logistics"),
    PHOTOGRAPHY("Photography"),
    REGISTRATION("Registration"),
    STAGE_MANAGEMENT("Stage Management"),
    SECURITY("Security"),
    FOOD("Food"),
    TRANSPORT("Transport"),
    OTHER("Other")
}

enum class RequirementStatus(val displayName: String, val colorHex: Long) {
    PENDING("Pending", 0xFFF59E0B),
    REQUESTED("Requested", 0xFF0284C7),
    CONFIRMED("Confirmed", 0xFF6366F1),
    AVAILABLE("Available", 0xFF10B981),
    NOT_AVAILABLE("Not Available", 0xFFEF4444)
}

enum class ExpenseCategory(val displayName: String) {
    VENUE("Venue"),
    FOOD("Food"),
    DECORATIONS("Decorations"),
    EQUIPMENT("Equipment"),
    MARKETING("Marketing"),
    TRANSPORTATION("Transportation"),
    PRIZES("Prizes"),
    PRINTING("Printing"),
    PHOTOGRAPHY("Photography"),
    OTHER("Other")
}

data class OpsUser(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val department: String,
    val role: OpsUserRole,
    val avatarInitials: String = name.split(" ").map { it.take(1) }.joinToString("")
)

data class EventTeamMember(
    val id: String,
    val eventId: String,
    val name: String,
    val email: String,
    val phone: String,
    val department: String,
    val role: String, // e.g. "Event Head", "Technical Lead", custom
    val responsibility: String, // e.g. "Website & Registration"
    val category: TeamCategory = TeamCategory.TECHNICAL,
    val assignedTasksCount: Int = 0,
    val completedTasksCount: Int = 0
)

data class TaskComment(
    val id: String,
    val taskId: String,
    val authorName: String,
    val authorRole: String,
    val text: String,
    val timestamp: String
)

data class OpsTask(
    val id: String,
    val eventId: String,
    val eventName: String,
    val name: String,
    val description: String,
    val assignedMemberId: String,
    val assignedMemberName: String,
    val category: TeamCategory,
    val priority: TaskPriority,
    val startDate: String,
    val deadline: String,
    val status: TaskStatus,
    val notes: String = "",
    val comments: List<TaskComment> = emptyList(),
    val isOverdue: Boolean = false
)

data class ScheduleItem(
    val id: String,
    val eventId: String,
    val time: String, // e.g. "09:00 AM"
    val title: String,
    val description: String = "",
    val responsiblePerson: String = "",
    val orderIndex: Int = 0
)

data class ExpenseItem(
    val id: String,
    val eventId: String,
    val description: String,
    val category: ExpenseCategory,
    val amount: Double,
    val date: String,
    val paidBy: String,
    val paymentStatus: String = "PAID", // PAID, PENDING
    val notes: String = ""
)

data class RequirementItem(
    val id: String,
    val eventId: String,
    val item: String,
    val quantity: String,
    val responsiblePerson: String,
    val requiredBy: String,
    val status: RequirementStatus,
    val notes: String = ""
)

data class InternalAnnouncement(
    val id: String,
    val eventId: String,
    val title: String,
    val message: String,
    val priority: String = "Normal", // Normal, Urgent
    val targetAudience: String = "Entire Team",
    val postedBy: String,
    val postedAt: String
)

data class MeetingActionItem(
    val id: String,
    val text: String,
    val assignedTo: String,
    val isConvertedToTask: Boolean = false
)

data class EventMeeting(
    val id: String,
    val eventId: String,
    val title: String,
    val meetingType: String = "Team Meeting", // Team Meeting, Faculty Meeting, Sponsor Meeting, Venue Meeting
    val date: String,
    val time: String,
    val location: String,
    val participants: String,
    val agenda: String,
    val notes: String = "",
    val actionItems: List<MeetingActionItem> = emptyList()
)

data class EventDocument(
    val id: String,
    val eventId: String,
    val title: String,
    val category: String, // Permission letters, Posters, Schedules, Invoices, Receipts, Reports
    val fileType: String = "PDF",
    val uploadedBy: String,
    val uploadedAt: String,
    val fileSize: String = "1.4 MB"
)

data class ActivityLog(
    val id: String,
    val eventId: String,
    val message: String,
    val authorName: String,
    val timestamp: String
)

data class CollegeEvent(
    val id: String,
    val name: String,
    val eventType: String, // Technical Fest, Cultural Fest, Hackathon, Workshop, Sports Meet
    val description: String,
    val department: String,
    val club: String,
    val eventHeadId: String,
    val eventHeadName: String,
    val facultyCoordinatorId: String,
    val facultyCoordinatorName: String,
    val startDate: String,
    val endDate: String,
    val startTime: String,
    val endTime: String,
    val venue: String,
    val expectedParticipants: Int,
    val estimatedBudget: Double,
    val spentBudget: Double,
    val priority: String = "High",
    val objectives: String,
    val notes: String = "",
    val status: EventLifecycleStatus = EventLifecycleStatus.PLANNING
)

enum class NotificationType(val displayName: String, val colorHex: Long) {
    TASK("Task Assigned", 0xFF6366F1),
    BUDGET("Budget Approval", 0xFF0D9488),
    DEADLINE("Deadline Alert", 0xFFDC2626),
    ANNOUNCEMENT("Broadcast", 0xFF0284C7),
    MEETING("Meeting Notice", 0xFFD97706),
    GENERAL("Update", 0xFF64748B)
}

data class OpsNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: String,
    val eventId: String? = null,
    val eventName: String? = null,
    val taskId: String? = null,
    val isRead: Boolean = false,
    val isUrgent: Boolean = false
)
