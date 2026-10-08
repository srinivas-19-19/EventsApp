package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object OperationsRepository {

    // Pre-configured Users for quick role switching
    val adminUser = OpsUser(
        id = "usr_admin",
        name = "Dr. Eleanor Vance",
        email = "eleanor.vance@college.edu",
        phone = "+91 98450-11223",
        department = "Administration",
        role = OpsUserRole.ADMIN
    )

    val eventHeadUser = OpsUser(
        id = "usr_rahul",
        name = "Rahul Sharma",
        email = "rahul.sharma@college.edu",
        phone = "+91 98765-43210",
        department = "Computer Science",
        role = OpsUserRole.EVENT_HEAD
    )

    val teamMemberUser = OpsUser(
        id = "usr_sreenu",
        name = "Sreenu Gorkal",
        email = "gorkalsreenu10@gmail.com",
        phone = "+91 97012-34567",
        department = "Computer Science",
        role = OpsUserRole.TEAM_MEMBER
    )

    val facultyUser = OpsUser(
        id = "usr_marcus",
        name = "Prof. Marcus Sterling",
        email = "marcus.sterling@college.edu",
        phone = "+91 94401-88990",
        department = "Electronics & Communication",
        role = OpsUserRole.FACULTY_COORDINATOR
    )

    // Current Active Logged In User
    private val _currentUser = MutableStateFlow<OpsUser>(eventHeadUser)
    val currentUser: StateFlow<OpsUser> = _currentUser.asStateFlow()

    // Events State
    private val _events = MutableStateFlow<List<CollegeEvent>>(getInitialEvents())
    val events: StateFlow<List<CollegeEvent>> = _events.asStateFlow()

    // Active Selected Event for the Workspace
    private val _activeEventId = MutableStateFlow<String>("evt_techfest_2026")
    val activeEventId: StateFlow<String> = _activeEventId.asStateFlow()

    // Team Members
    private val _teamMembers = MutableStateFlow<List<EventTeamMember>>(getInitialTeamMembers())
    val teamMembers: StateFlow<List<EventTeamMember>> = _teamMembers.asStateFlow()

    // Tasks State
    private val _tasks = MutableStateFlow<List<OpsTask>>(getInitialTasks())
    val tasks: StateFlow<List<OpsTask>> = _tasks.asStateFlow()

    // Schedule Items
    private val _scheduleItems = MutableStateFlow<List<ScheduleItem>>(getInitialSchedule())
    val scheduleItems: StateFlow<List<ScheduleItem>> = _scheduleItems.asStateFlow()

    // Expenses State
    private val _expenses = MutableStateFlow<List<ExpenseItem>>(getInitialExpenses())
    val expenses: StateFlow<List<ExpenseItem>> = _expenses.asStateFlow()

    // Requirements State
    private val _requirements = MutableStateFlow<List<RequirementItem>>(getInitialRequirements())
    val requirements: StateFlow<List<RequirementItem>> = _requirements.asStateFlow()

    // Announcements
    private val _announcements = MutableStateFlow<List<InternalAnnouncement>>(getInitialAnnouncements())
    val announcements: StateFlow<List<InternalAnnouncement>> = _announcements.asStateFlow()

    // Meetings
    private val _meetings = MutableStateFlow<List<EventMeeting>>(getInitialMeetings())
    val meetings: StateFlow<List<EventMeeting>> = _meetings.asStateFlow()

    // Documents
    private val _documents = MutableStateFlow<List<EventDocument>>(getInitialDocuments())
    val documents: StateFlow<List<EventDocument>> = _documents.asStateFlow()

    // Activity Logs
    private val _activities = MutableStateFlow<List<ActivityLog>>(getInitialActivity())
    val activities: StateFlow<List<ActivityLog>> = _activities.asStateFlow()

    // Operational Notifications
    private val _notifications = MutableStateFlow<List<OpsNotification>>(getInitialNotifications())
    val notifications: StateFlow<List<OpsNotification>> = _notifications.asStateFlow()

    fun setCurrentUser(user: OpsUser) {
        _currentUser.value = user
    }

    fun switchRole(role: OpsUserRole) {
        _currentUser.value = when (role) {
            OpsUserRole.ADMIN -> adminUser
            OpsUserRole.EVENT_HEAD -> eventHeadUser
            OpsUserRole.TEAM_MEMBER -> teamMemberUser
            OpsUserRole.FACULTY_COORDINATOR -> facultyUser
        }
    }

    fun setActiveEvent(eventId: String) {
        _activeEventId.value = eventId
    }

    fun getActiveEvent(): CollegeEvent? {
        return _events.value.find { it.id == _activeEventId.value }
    }

    // --- Event Operations ---
    fun addEvent(event: CollegeEvent) {
        _events.value = listOf(event) + _events.value
        addActivityLog(event.id, "Created new event workspace '${event.name}'", _currentUser.value.name)
    }

    fun updateEventStatus(eventId: String, newStatus: EventLifecycleStatus) {
        _events.value = _events.value.map {
            if (it.id == eventId) it.copy(status = newStatus) else it
        }
        addActivityLog(eventId, "Updated event status to ${newStatus.displayName}", _currentUser.value.name)
    }

    // --- Task Operations ---
    fun addTask(task: OpsTask) {
        _tasks.value = listOf(task) + _tasks.value
        // Update member task count
        _teamMembers.value = _teamMembers.value.map {
            if (it.id == task.assignedMemberId) it.copy(assignedTasksCount = it.assignedTasksCount + 1) else it
        }
        addActivityLog(task.eventId, "Created task '${task.name}' assigned to ${task.assignedMemberName}", _currentUser.value.name)
    }

    fun updateTaskStatus(taskId: String, newStatus: TaskStatus) {
        val task = _tasks.value.find { it.id == taskId } ?: return
        val wasCompleted = task.status == TaskStatus.COMPLETED
        val isNowCompleted = newStatus == TaskStatus.COMPLETED

        _tasks.value = _tasks.value.map {
            if (it.id == taskId) it.copy(status = newStatus) else it
        }

        if (!wasCompleted && isNowCompleted) {
            _teamMembers.value = _teamMembers.value.map {
                if (it.id == task.assignedMemberId) it.copy(completedTasksCount = it.completedTasksCount + 1) else it
            }
            addActivityLog(task.eventId, "Completed task '${task.name}'", _currentUser.value.name)
        } else {
            addActivityLog(task.eventId, "Moved task '${task.name}' to ${newStatus.displayName}", _currentUser.value.name)
        }
    }

    fun addTaskComment(taskId: String, text: String) {
        val comment = TaskComment(
            id = "cmt_${UUID.randomUUID()}",
            taskId = taskId,
            authorName = _currentUser.value.name,
            authorRole = _currentUser.value.role.displayName,
            text = text,
            timestamp = "Just now"
        )
        _tasks.value = _tasks.value.map { t ->
            if (t.id == taskId) t.copy(comments = t.comments + comment) else t
        }
    }

    // --- Team Operations ---
    fun addTeamMember(member: EventTeamMember) {
        _teamMembers.value = _teamMembers.value + member
        addActivityLog(member.eventId, "Added ${member.name} as ${member.role} (${member.responsibility})", _currentUser.value.name)
    }

    // --- Budget & Expense Operations ---
    fun addExpense(expense: ExpenseItem) {
        _expenses.value = listOf(expense) + _expenses.value
        // Update spentBudget on the event
        _events.value = _events.value.map { e ->
            if (e.id == expense.eventId) e.copy(spentBudget = e.spentBudget + expense.amount) else e
        }
        addActivityLog(expense.eventId, "Added expense '₹${expense.amount.toInt()} for ${expense.description}'", _currentUser.value.name)
    }

    // --- Requirements Operations ---
    fun addRequirement(req: RequirementItem) {
        _requirements.value = listOf(req) + _requirements.value
        addActivityLog(req.eventId, "Added requirement '${req.item} (${req.quantity})'", _currentUser.value.name)
    }

    fun updateRequirementStatus(reqId: String, newStatus: RequirementStatus) {
        _requirements.value = _requirements.value.map {
            if (it.id == reqId) it.copy(status = newStatus) else it
        }
    }

    // --- Schedule Operations ---
    fun addScheduleItem(item: ScheduleItem) {
        _scheduleItems.value = _scheduleItems.value + item
        addActivityLog(item.eventId, "Added schedule item '${item.time} - ${item.title}'", _currentUser.value.name)
    }

    fun deleteScheduleItem(itemId: String) {
        _scheduleItems.value = _scheduleItems.value.filter { it.id != itemId }
    }

    // --- Announcement Operations ---
    fun addAnnouncement(ann: InternalAnnouncement) {
        _announcements.value = listOf(ann) + _announcements.value
        addActivityLog(ann.eventId, "Posted team announcement '${ann.title}'", _currentUser.value.name)
    }

    // --- Meeting Operations ---
    fun addMeeting(meeting: EventMeeting) {
        _meetings.value = listOf(meeting) + _meetings.value
        addActivityLog(meeting.eventId, "Scheduled ${meeting.meetingType} '${meeting.title}'", _currentUser.value.name)
    }

    fun convertActionItemToTask(meetingId: String, actionItemId: String, taskCategory: TeamCategory = TeamCategory.TECHNICAL) {
        val meeting = _meetings.value.find { it.id == meetingId } ?: return
        val item = meeting.actionItems.find { it.id == actionItemId } ?: return
        if (item.isConvertedToTask) return

        val newTask = OpsTask(
            id = "tsk_${UUID.randomUUID().toString().take(8)}",
            eventId = meeting.eventId,
            eventName = getActiveEvent()?.name ?: "Event",
            name = item.text,
            description = "Converted from action item in meeting '${meeting.title}'",
            assignedMemberId = "mem_sreenu",
            assignedMemberName = item.assignedTo,
            category = taskCategory,
            priority = TaskPriority.HIGH,
            startDate = "Today",
            deadline = "Oct 20, 2026",
            status = TaskStatus.TO_DO
        )
        addTask(newTask)

        // Mark action item as converted
        _meetings.value = _meetings.value.map { m ->
            if (m.id == meetingId) {
                m.copy(actionItems = m.actionItems.map { if (it.id == actionItemId) it.copy(isConvertedToTask = true) else it })
            } else m
        }
    }

    // --- Document Operations ---
    fun addDocument(doc: EventDocument) {
        _documents.value = listOf(doc) + _documents.value
        addActivityLog(doc.eventId, "Uploaded document '${doc.title}'", _currentUser.value.name)
    }

    // --- Notification Operations ---
    fun markNotificationAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun markAllNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun addNotification(notification: OpsNotification) {
        _notifications.value = listOf(notification) + _notifications.value
    }

    private fun addActivityLog(eventId: String, message: String, author: String) {
        val log = ActivityLog(
            id = "act_${UUID.randomUUID().toString().take(8)}",
            eventId = eventId,
            message = message,
            authorName = author,
            timestamp = "Just now"
        )
        _activities.value = listOf(log) + _activities.value
    }

    // ==========================================
    // INITIAL REALISTIC SEED DATA (Demo Event & Team)
    // ==========================================
    private fun getInitialEvents(): List<CollegeEvent> = listOf(
        CollegeEvent(
            id = "evt_techfest_2026",
            name = "Annual Technical Fest 2026",
            eventType = "Technical Fest",
            description = "Flagship annual campus technical festival featuring 24-hour hackathons, robotics combat, code sprints, technical paper symposium, and keynote addresses from industry leaders.",
            department = "Computer Science & Engineering",
            club = "Coding & AI Society",
            eventHeadId = "usr_rahul",
            eventHeadName = "Rahul Sharma",
            facultyCoordinatorId = "usr_marcus",
            facultyCoordinatorName = "Prof. Marcus Sterling",
            startDate = "Oct 24, 2026",
            endDate = "Oct 26, 2026",
            startTime = "09:00 AM",
            endTime = "06:00 PM",
            venue = "Turing Auditorium & Tech Block B",
            expectedParticipants = 1200,
            estimatedBudget = 60000.0,
            spentBudget = 42500.0,
            priority = "Critical",
            objectives = "1. Showcase student engineering innovations\n2. Foster inter-department technical collaboration\n3. Connect students with tech companies and alumni",
            status = EventLifecycleStatus.PLANNING
        ),
        CollegeEvent(
            id = "evt_cultural_2026",
            name = "Tarang 2026: Annual Cultural Fest",
            eventType = "Cultural Fest",
            description = "3-day college celebration of music, dance, theatrical drama, battle of the bands, and art exhibitions.",
            department = "Student Affairs",
            club = "Cultural Arts Society",
            eventHeadId = "usr_rahul",
            eventHeadName = "Priya Varma",
            facultyCoordinatorId = "usr_marcus",
            facultyCoordinatorName = "Dr. Anita Sen",
            startDate = "Nov 05, 2026",
            endDate = "Nov 07, 2026",
            startTime = "04:00 PM",
            endTime = "10:00 PM",
            venue = "Open Air Amphitheatre",
            expectedParticipants = 2500,
            estimatedBudget = 120000.0,
            spentBudget = 35000.0,
            priority = "High",
            objectives = "Celebrate student cultural arts and diversity",
            status = EventLifecycleStatus.UPCOMING
        ),
        CollegeEvent(
            id = "evt_ai_workshop",
            name = "National AI & Machine Learning Workshop",
            eventType = "Workshop",
            description = "Intensive hands-on masterclass on building LLM agents and deploying models.",
            department = "AI & Data Science",
            club = "AI & ML Club",
            eventHeadId = "usr_rahul",
            eventHeadName = "Kiran Rao",
            facultyCoordinatorId = "usr_marcus",
            facultyCoordinatorName = "Prof. Arvind S.",
            startDate = "Oct 18, 2026",
            endDate = "Oct 19, 2026",
            startTime = "10:00 AM",
            endTime = "04:00 PM",
            venue = "Seminar Hall 2",
            expectedParticipants = 180,
            estimatedBudget = 35000.0,
            spentBudget = 22000.0,
            priority = "Medium",
            objectives = "Hands-on generative AI skills for undergrads",
            status = EventLifecycleStatus.APPROVED
        ),
        CollegeEvent(
            id = "evt_sports_meet",
            name = "Inter-Department Cricket & Athletics Championship",
            eventType = "Sports Meet",
            description = "Annual collegiate sports tournament across all 8 engineering departments.",
            department = "Physical Education",
            club = "Sports Council",
            eventHeadId = "usr_rahul",
            eventHeadName = "Arjun Das",
            facultyCoordinatorId = "usr_marcus",
            facultyCoordinatorName = "Coach Vikram Singh",
            startDate = "Nov 12, 2026",
            endDate = "Nov 15, 2026",
            startTime = "08:30 AM",
            endTime = "05:30 PM",
            venue = "Main Sports Complex Oval",
            expectedParticipants = 600,
            estimatedBudget = 45000.0,
            spentBudget = 15000.0,
            priority = "Medium",
            objectives = "Foster physical fitness and departmental sportsmanship",
            status = EventLifecycleStatus.PLANNING
        ),
        CollegeEvent(
            id = "evt_alumni_summit",
            name = "Alumni Entrepreneurship & Venture Summit",
            eventType = "Seminar",
            description = "Founder pitches, angel investor connections, and venture incubation grants.",
            department = "Management & E-Cell",
            club = "E-Cell",
            eventHeadId = "usr_rahul",
            eventHeadName = "Sneha Patel",
            facultyCoordinatorId = "usr_marcus",
            facultyCoordinatorName = "Dr. Rajesh Iyer",
            startDate = "Sep 22, 2026",
            endDate = "Sep 22, 2026",
            startTime = "10:00 AM",
            endTime = "05:00 PM",
            venue = "Auditorium Hall 1",
            expectedParticipants = 350,
            estimatedBudget = 50000.0,
            spentBudget = 48500.0,
            priority = "High",
            objectives = "Student founder incubation",
            status = EventLifecycleStatus.COMPLETED
        )
    )

    private fun getInitialTeamMembers(): List<EventTeamMember> = listOf(
        EventTeamMember("mem_rahul", "evt_techfest_2026", "Rahul Sharma", "rahul@college.edu", "+91 98765-43210", "CSE", "Event Head", "Overall Operations & Management", TeamCategory.OTHER, 6, 5),
        EventTeamMember("mem_sreenu", "evt_techfest_2026", "Sreenu Gorkal", "gorkalsreenu10@gmail.com", "+91 97012-34567", "CSE", "Technical & Web Lead", "Website & Registration System", TeamCategory.TECHNICAL, 5, 4),
        EventTeamMember("mem_kiran", "evt_techfest_2026", "Kiran Reddy", "kiran.reddy@college.edu", "+91 98480-12345", "IT", "Sponsorship Lead", "Corporate Sponsorship & MoUs", TeamCategory.SPONSORSHIP, 4, 3),
        EventTeamMember("mem_priya", "evt_techfest_2026", "Priya Nair", "priya.nair@college.edu", "+91 94401-23456", "ECE", "Decorations Lead", "Stage & Venue Ambience", TeamCategory.DESIGN, 4, 3),
        EventTeamMember("mem_arjun", "evt_techfest_2026", "Arjun Das", "arjun.das@college.edu", "+91 99887-76655", "MECH", "Technical Setup Lead", "AV Systems, Projectors & Power", TeamCategory.LOGISTICS, 5, 4),
        EventTeamMember("mem_manoj", "evt_techfest_2026", "Manoj Kumar", "manoj.k@college.edu", "+91 91234-56789", "CIVIL", "Hospitality Lead", "Food, Catering & Guest Accommodations", TeamCategory.HOSPITALITY, 4, 3),
        EventTeamMember("mem_anjali", "evt_techfest_2026", "Anjali Menon", "anjali.m@college.edu", "+91 95566-77889", "AI_DS", "Photography Lead", "Photography & Video Coverage", TeamCategory.PHOTOGRAPHY, 3, 2),
        EventTeamMember("mem_rohit", "evt_techfest_2026", "Rohit Varma", "rohit.v@college.edu", "+91 96677-88990", "EEE", "Stage Management Lead", "MC Coordination & Timetable", TeamCategory.STAGE_MANAGEMENT, 3, 2),
        EventTeamMember("mem_sneha", "evt_techfest_2026", "Sneha Patel", "sneha.p@college.edu", "+91 97788-99001", "MBA", "Marketing Lead", "Poster Drives & Social Media", TeamCategory.MARKETING, 3, 1),
        EventTeamMember("mem_vikram", "evt_techfest_2026", "Vikram Singh", "vikram.s@college.edu", "+91 98899-00112", "MECH", "Logistics Lead", "Transport & Materials Shifting", TeamCategory.LOGISTICS, 2, 1),
        EventTeamMember("mem_neha", "evt_techfest_2026", "Neha Gupta", "neha.g@college.edu", "+91 99900-11223", "CSE", "Volunteer Coordinator", "Volunteer Roster & Shift Schedules", TeamCategory.OTHER, 2, 1),
        EventTeamMember("mem_karthik", "evt_techfest_2026", "Karthik Pillai", "karthik.p@college.edu", "+91 91122-33445", "ECE", "Security & Facilities", "Crowd Control & Gate Passes", TeamCategory.SECURITY, 2, 1)
    )

    private fun getInitialTasks(): List<OpsTask> {
        val eventId = "evt_techfest_2026"
        val eventName = "Annual Technical Fest 2026"

        val completedList = listOf(
            OpsTask("tsk_01", eventId, eventName, "Design official event poster", "Finalize 4K resolution poster for print and social media", "mem_priya", "Priya Nair", TeamCategory.DESIGN, TaskPriority.HIGH, "Oct 01", "Oct 05", TaskStatus.COMPLETED),
            OpsTask("tsk_02", eventId, eventName, "Book Turing Auditorium & Block B Labs", "Obtain official registrar stamp and keys requisition", "mem_arjun", "Arjun Das", TeamCategory.LOGISTICS, TaskPriority.CRITICAL, "Oct 02", "Oct 06", TaskStatus.COMPLETED),
            OpsTask("tsk_03", eventId, eventName, "Draft sponsorship brochure", "Include tier perks: Title, Gold, Silver sponsorships", "mem_kiran", "Kiran Reddy", TeamCategory.SPONSORSHIP, TaskPriority.HIGH, "Oct 03", "Oct 08", TaskStatus.COMPLETED),
            OpsTask("tsk_04", eventId, eventName, "Launch registration portal backend", "Setup secure student registration and QR generation API", "mem_sreenu", "Sreenu Gorkal", TeamCategory.TECHNICAL, TaskPriority.CRITICAL, "Oct 04", "Oct 10", TaskStatus.COMPLETED),
            OpsTask("tsk_05", eventId, eventName, "Confirm keynote speaker hospitality", "Coordinate pickup from airport and guest house suite", "mem_manoj", "Manoj Kumar", TeamCategory.HOSPITALITY, TaskPriority.HIGH, "Oct 05", "Oct 11", TaskStatus.COMPLETED),
            OpsTask("tsk_06", eventId, eventName, "Assemble camera and gimbal gear", "Book 2 Sony A7IV bodies and wireless lavalier mics", "mem_anjali", "Anjali Menon", TeamCategory.PHOTOGRAPHY, TaskPriority.MEDIUM, "Oct 05", "Oct 11", TaskStatus.COMPLETED),
            OpsTask("tsk_07", eventId, eventName, "Procure main stage floral decor & banners", "Sign agreement with campus florist vendor", "mem_priya", "Priya Nair", TeamCategory.DESIGN, TaskPriority.MEDIUM, "Oct 06", "Oct 12", TaskStatus.COMPLETED),
            OpsTask("tsk_08", eventId, eventName, "Submit budget estimate to Dean office", "Present ₹60,000 itemized budget breakdown", "mem_rahul", "Rahul Sharma", TeamCategory.FINANCE, TaskPriority.CRITICAL, "Oct 01", "Oct 04", TaskStatus.COMPLETED),
            OpsTask("tsk_09", eventId, eventName, "Sign MoU with Google Cloud sponsor", "Deliver $500 cloud voucher codes for hackathon", "mem_kiran", "Kiran Reddy", TeamCategory.SPONSORSHIP, TaskPriority.HIGH, "Oct 06", "Oct 12", TaskStatus.COMPLETED),
            OpsTask("tsk_10", eventId, eventName, "Inspect electrical phase lines in Lab 301", "Ensure stable 3-phase power for combat robots", "mem_arjun", "Arjun Das", TeamCategory.TECHNICAL, TaskPriority.HIGH, "Oct 07", "Oct 12", TaskStatus.COMPLETED),
            OpsTask("tsk_11", eventId, eventName, "Finalize food vendor for 500 meals", "Arrange packaged biryani and vegetarian thali options", "mem_manoj", "Manoj Kumar", TeamCategory.HOSPITALITY, TaskPriority.HIGH, "Oct 08", "Oct 13", TaskStatus.COMPLETED),
            OpsTask("tsk_12", eventId, eventName, "Prepare certificates of participation template", "Incorporate college crest and coordinator sign fields", "mem_sreenu", "Sreenu Gorkal", TeamCategory.DESIGN, TaskPriority.MEDIUM, "Oct 07", "Oct 13", TaskStatus.COMPLETED),
            OpsTask("tsk_13", eventId, eventName, "Brief student volunteer batch 1", "Assign 25 volunteers to registration and ushering", "mem_neha", "Neha Gupta", TeamCategory.OTHER, TaskPriority.MEDIUM, "Oct 08", "Oct 13", TaskStatus.COMPLETED),
            OpsTask("tsk_14", eventId, eventName, "Campus gate security memo", "Issue parking pass instructions to security guards", "mem_karthik", "Karthik Pillai", TeamCategory.SECURITY, TaskPriority.LOW, "Oct 09", "Oct 14", TaskStatus.COMPLETED),
            OpsTask("tsk_15", eventId, eventName, "Deploy SSL on event domain", "Configure Cloudflare CDN and DDoS protection", "mem_sreenu", "Sreenu Gorkal", TeamCategory.TECHNICAL, TaskPriority.HIGH, "Oct 09", "Oct 14", TaskStatus.COMPLETED),
            OpsTask("tsk_16", eventId, eventName, "Print 500 participant badges & lanyards", "Collect badges from press with color coded departments", "mem_priya", "Priya Nair", TeamCategory.LOGISTICS, TaskPriority.HIGH, "Oct 10", "Oct 15", TaskStatus.COMPLETED),
            OpsTask("tsk_17", eventId, eventName, "Test auditorium audio mixing console", "Test 6 cordless mics, surround monitors and amplifiers", "mem_arjun", "Arjun Das", TeamCategory.TECHNICAL, TaskPriority.HIGH, "Oct 10", "Oct 15", TaskStatus.COMPLETED),
            OpsTask("tsk_18", eventId, eventName, "Schedule MCs for Inaugural session", "Draft script for opening remarks and dignitary introductions", "mem_rohit", "Rohit Varma", TeamCategory.STAGE_MANAGEMENT, TaskPriority.MEDIUM, "Oct 11", "Oct 15", TaskStatus.COMPLETED),
            OpsTask("tsk_19", eventId, eventName, "Collect prize trophies from manufacturer", "3 Gold Rolling Cups, 6 Trophies, 12 Medals", "mem_rahul", "Rahul Sharma", TeamCategory.LOGISTICS, TaskPriority.HIGH, "Oct 11", "Oct 15", TaskStatus.COMPLETED),
            OpsTask("tsk_20", eventId, eventName, "Publish social media teaser reels", "Release Instagram promotional video teaser (10K views)", "mem_sneha", "Sneha Patel", TeamCategory.MARKETING, TaskPriority.MEDIUM, "Oct 10", "Oct 15", TaskStatus.COMPLETED),
            OpsTask("tsk_21", eventId, eventName, "Book transportation mini-bus", "College shuttle reserved for external judges from hotel", "mem_vikram", "Vikram Singh", TeamCategory.TRANSPORT, TaskPriority.MEDIUM, "Oct 11", "Oct 15", TaskStatus.COMPLETED),
            OpsTask("tsk_22", eventId, eventName, "Distribute department classroom posters", "Posters pinned on notice boards in CSE, ECE, MECH, IT", "mem_sneha", "Sneha Patel", TeamCategory.MARKETING, TaskPriority.LOW, "Oct 12", "Oct 16", TaskStatus.COMPLETED),
            OpsTask("tsk_23", eventId, eventName, "Setup GitHub classroom for hackathon", "Create team repositories and automated test runners", "mem_sreenu", "Sreenu Gorkal", TeamCategory.TECHNICAL, TaskPriority.HIGH, "Oct 12", "Oct 16", TaskStatus.COMPLETED),
            OpsTask("tsk_24", eventId, eventName, "Install 4 extra Wi-Fi Access Points in Block B", "Network team provisioned 1 Gbps dedicated pipe", "mem_arjun", "Arjun Das", TeamCategory.TECHNICAL, TaskPriority.CRITICAL, "Oct 12", "Oct 16", TaskStatus.COMPLETED),
            OpsTask("tsk_25", eventId, eventName, "Order refreshments for faculty judges", "Tea, cookies, dry fruits and mineral water bottles", "mem_manoj", "Manoj Kumar", TeamCategory.HOSPITALITY, TaskPriority.LOW, "Oct 13", "Oct 16", TaskStatus.COMPLETED),
            OpsTask("tsk_26", eventId, eventName, "Stage backdrop vinyl printing", "30x12 ft banner printed and dry-fitted on truss", "mem_priya", "Priya Nair", TeamCategory.DESIGN, TaskPriority.HIGH, "Oct 13", "Oct 16", TaskStatus.COMPLETED),
            OpsTask("tsk_27", eventId, eventName, "Coordinate medical room first-aid kits", "Campus nurse on standby with basic medical equipment", "mem_karthik", "Karthik Pillai", TeamCategory.OTHER, TaskPriority.MEDIUM, "Oct 13", "Oct 16", TaskStatus.COMPLETED),
            OpsTask("tsk_28", eventId, eventName, "Emergency fire extinguisher inspection", "Campus safety officer passed workshop safety checklist", "mem_arjun", "Arjun Das", TeamCategory.SECURITY, TaskPriority.HIGH, "Oct 13", "Oct 16", TaskStatus.COMPLETED)
        )

        val inProgressList = listOf(
            OpsTask("tsk_29", eventId, eventName, "Test QR ticket check-in scanner speed", "Stress test QR scanner on 100 sample passes in 5 minutes", "mem_sreenu", "Sreenu Gorkal", TeamCategory.TECHNICAL, TaskPriority.HIGH, "Oct 14", "Oct 20", TaskStatus.IN_PROGRESS, comments = listOf(
                TaskComment("c1", "tsk_29", "Rahul Sharma", "Event Head", "Make sure the scanner functions offline if college Wi-Fi drops.", "2 hours ago"),
                TaskComment("c2", "tsk_29", "Sreenu Gorkal", "Tech Lead", "Yes! Offline caching is built in with local barcode verification.", "1 hour ago")
            )),
            OpsTask("tsk_30", eventId, eventName, "Collect remaining 30% sponsor cheques", "Follow up with Infosys and RedBull campus reps", "mem_kiran", "Kiran Reddy", TeamCategory.SPONSORSHIP, TaskPriority.HIGH, "Oct 14", "Oct 21", TaskStatus.IN_PROGRESS),
            OpsTask("tsk_31", eventId, eventName, "LED entrance arch truss assembly", "Assemble metal arch and test programmed DMX lights", "mem_priya", "Priya Nair", TeamCategory.DESIGN, TaskPriority.MEDIUM, "Oct 15", "Oct 22", TaskStatus.IN_PROGRESS)
        )

        val blockedList = listOf(
            OpsTask("tsk_32", eventId, eventName, "Auditorium acoustic velvet drapes", "Fix acoustic resonance in rear seating area", "mem_arjun", "Arjun Das", TeamCategory.LOGISTICS, TaskPriority.MEDIUM, "Oct 14", "Oct 22", TaskStatus.BLOCKED, notes = "Blocked: Estate maintenance team is currently repainting the ceiling.")
        )

        val overdueList = listOf(
            OpsTask("tsk_33", eventId, eventName, "Police station NOC & Sound permission", "Submit sound permission letter for outdoor loudspeaker usage", "mem_rahul", "Rahul Sharma", TeamCategory.OTHER, TaskPriority.CRITICAL, "Oct 05", "Oct 12", TaskStatus.OVERDUE, isOverdue = true),
            OpsTask("tsk_34", eventId, eventName, "Drone aerial photography college permit", "Obtain aviation clearance from Dean of Infrastructure", "mem_anjali", "Anjali Menon", TeamCategory.PHOTOGRAPHY, TaskPriority.HIGH, "Oct 07", "Oct 13", TaskStatus.OVERDUE, isOverdue = true)
        )

        val toDoList = listOf(
            OpsTask("tsk_35", eventId, eventName, "Finalize prize distribution envelope cash", "Withdraw ₹35,000 cash prize money from college accounts", "mem_rahul", "Rahul Sharma", TeamCategory.FINANCE, TaskPriority.HIGH, "Oct 18", "Oct 23", TaskStatus.TO_DO),
            OpsTask("tsk_36", eventId, eventName, "Print 50 schedule booklets for VIP guests", "Include guest profiles and event breakdown", "mem_rohit", "Rohit Varma", TeamCategory.STAGE_MANAGEMENT, TaskPriority.LOW, "Oct 19", "Oct 23", TaskStatus.TO_DO),
            OpsTask("tsk_37", eventId, eventName, "Setup registration helpdesk physical tables", "Set up 4 tables with extension cords and water carboys", "mem_sreenu", "Sreenu Gorkal", TeamCategory.LOGISTICS, TaskPriority.MEDIUM, "Oct 20", "Oct 23", TaskStatus.TO_DO),
            OpsTask("tsk_38", eventId, eventName, "Hackathon midnight energy drinks & snacks", "Stock 300 cans of juice, energy bars and biscuits", "mem_manoj", "Manoj Kumar", TeamCategory.FOOD, TaskPriority.MEDIUM, "Oct 21", "Oct 24", TaskStatus.TO_DO),
            OpsTask("tsk_39", eventId, eventName, "Dry run rehearsal of stage lighting", "Walkthrough with dignitaries and lighting engineer", "mem_rohit", "Rohit Varma", TeamCategory.STAGE_MANAGEMENT, TaskPriority.HIGH, "Oct 22", "Oct 24", TaskStatus.TO_DO),
            OpsTask("tsk_40", eventId, eventName, "Generate post-event comprehensive audit report", "Compile accounts, participant metrics and faculty feedback", "mem_rahul", "Rahul Sharma", TeamCategory.OTHER, TaskPriority.MEDIUM, "Oct 25", "Oct 28", TaskStatus.TO_DO)
        )

        return completedList + inProgressList + blockedList + overdueList + toDoList
    }

    private fun getInitialExpenses(): List<ExpenseItem> = listOf(
        ExpenseItem("exp_01", "evt_techfest_2026", "Stage backdrop truss & vinyl banners", ExpenseCategory.DECORATIONS, 12000.0, "Oct 10, 2026", "Priya Nair", "PAID"),
        ExpenseItem("exp_02", "evt_techfest_2026", "Advance payment for catering (500 meals)", ExpenseCategory.FOOD, 15000.0, "Oct 11, 2026", "Manoj Kumar", "PAID"),
        ExpenseItem("exp_03", "evt_techfest_2026", "Laser Projector & HDMI switcher rental", ExpenseCategory.EQUIPMENT, 8500.0, "Oct 12, 2026", "Arjun Das", "PAID"),
        ExpenseItem("exp_04", "evt_techfest_2026", "Participant lanyards & printed badges (500)", ExpenseCategory.PRINTING, 4500.0, "Oct 13, 2026", "Priya Nair", "PAID"),
        ExpenseItem("exp_05", "evt_techfest_2026", "Audio cables, mic batteries & adapters", ExpenseCategory.EQUIPMENT, 2500.0, "Oct 14, 2026", "Arjun Das", "PAID")
    )

    private fun getInitialRequirements(): List<RequirementItem> = listOf(
        RequirementItem("req_01", "evt_techfest_2026", "4K Laser Projector & Screen", "2 Units", "Arjun Das", "Oct 23, 2026", RequirementStatus.CONFIRMED, "Booked from AV Department"),
        RequirementItem("req_02", "evt_techfest_2026", "Wireless Cordless Microphones", "6 Mics", "Arjun Das", "Oct 23, 2026", RequirementStatus.AVAILABLE, "Tested with new 9V batteries"),
        RequirementItem("req_03", "evt_techfest_2026", "Cushioned VIP Chairs", "40 Chairs", "Vikram Singh", "Oct 22, 2026", RequirementStatus.REQUESTED, "Submitted slip to furniture store"),
        RequirementItem("req_04", "evt_techfest_2026", "Registration Long Tables", "6 Tables", "Sreenu Gorkal", "Oct 23, 2026", RequirementStatus.AVAILABLE, "Allocated in Block B corridor"),
        RequirementItem("req_05", "evt_techfest_2026", "1 Gbps Dedicated Fiber Uplink", "2 Ports", "Sreenu Gorkal", "Oct 21, 2026", RequirementStatus.CONFIRMED, "IP reservations active"),
        RequirementItem("req_06", "evt_techfest_2026", "24-Port Gigabit Switches", "4 Switches", "Sreenu Gorkal", "Oct 22, 2026", RequirementStatus.AVAILABLE, "Fetched from Lab 402"),
        RequirementItem("req_07", "evt_techfest_2026", "Water Dispensers & Paper Cups", "8 Dispensers", "Manoj Kumar", "Oct 23, 2026", RequirementStatus.CONFIRMED, "Delivered by cafeteria"),
        RequirementItem("req_08", "evt_techfest_2026", "Heavy Duty Extension Power Strips", "20 Boards", "Arjun Das", "Oct 22, 2026", RequirementStatus.PENDING, "Requisition pending with storekeeper")
    )

    private fun getInitialSchedule(): List<ScheduleItem> = listOf(
        ScheduleItem("sch_01", "evt_techfest_2026", "08:30 AM", "Volunteer Briefing & Badge Distribution", "Core leads align on registration desks and ushering", "Neha Gupta", 1),
        ScheduleItem("sch_02", "evt_techfest_2026", "09:00 AM", "Registration Desks Open", "QR pass verification, kit distribution and Wi-Fi credentials", "Sreenu Gorkal", 2),
        ScheduleItem("sch_03", "evt_techfest_2026", "10:00 AM", "Grand Inauguration & Lamp Lighting", "Principal, Dean, and Chief Guest addresses in Turing Auditorium", "Rohit Varma", 3),
        ScheduleItem("sch_04", "evt_techfest_2026", "11:15 AM", "Keynote Address: Future of AI Systems", "Dr. Satya Murthy (VP, Research, TechCorp)", "Rahul Sharma", 4),
        ScheduleItem("sch_05", "evt_techfest_2026", "01:00 PM", "Lunch & Networking Break", "Buffet lunch for delegates and students at dining hall", "Manoj Kumar", 5),
        ScheduleItem("sch_06", "evt_techfest_2026", "02:00 PM", "24-Hour CodeSprint Hackathon Commences", "Problem statements unlocked; 40 teams begin building", "Sreenu Gorkal", 6),
        ScheduleItem("sch_07", "evt_techfest_2026", "03:00 PM", "RoboWars Combat Arena Elimination Rounds", "Combat metal cage matches begin in Mech workshop", "Arjun Das", 7),
        ScheduleItem("sch_08", "evt_techfest_2026", "05:00 PM", "Valedictory & Prize Distribution (Day 1)", "Awards ceremony for day-1 workshops and quiz winners", "Rohit Varma", 8)
    )

    private fun getInitialAnnouncements(): List<InternalAnnouncement> = listOf(
        InternalAnnouncement("ann_01", "evt_techfest_2026", "Stage Setup Shifted to 4:00 PM", "All stage and AV leads: Due to morning university lectures in Turing Auditorium, setup commences at 04:00 PM today.", "Urgent", "Entire Team", "Rahul Sharma", "Today at 09:30 AM"),
        InternalAnnouncement("ann_02", "evt_techfest_2026", "All Volunteer Leads Report at 8:00 AM", "Please report to Room 204 for the final radio walkie-talkie check and kit distribution.", "Normal", "Volunteer Leads", "Neha Gupta", "Yesterday at 05:00 PM"),
        InternalAnnouncement("ann_03", "evt_techfest_2026", "Wi-Fi SSID 'TechFest-Gigabit' is Live", "Credentials have been tested with 50 concurrent laptops. Passwords are taped under registration desks.", "Normal", "Technical Team", "Sreenu Gorkal", "2 days ago")
    )

    private fun getInitialMeetings(): List<EventMeeting> = listOf(
        EventMeeting(
            id = "mtg_01",
            eventId = "evt_techfest_2026",
            title = "Final Core Operations & Security Walkthrough",
            meetingType = "Team Meeting",
            date = "Oct 21, 2026",
            time = "04:30 PM",
            location = "Council Conference Room 102",
            participants = "Rahul, Sreenu, Kiran, Priya, Arjun, Manoj, Neha",
            agenda = "1. Walkthrough of day-1 timeline\n2. Power redundancy and generator testing\n3. Food logistics & crowd management\n4. Review pending critical NOCs",
            notes = "Principal has agreed to attend the opening at 10 AM sharp. VIP car parking designated at Gate 2.",
            actionItems = listOf(
                MeetingActionItem("act_1", "Confirm sound system arrival by Thursday", "Arjun Das", isConvertedToTask = false),
                MeetingActionItem("act_2", "Print 20 emergency contact directory sheets", "Neha Gupta", isConvertedToTask = false),
                MeetingActionItem("act_3", "Double check backup diesel generator fuel", "Vikram Singh", isConvertedToTask = false)
            )
        ),
        EventMeeting(
            id = "mtg_02",
            eventId = "evt_techfest_2026",
            title = "Faculty Advisory & Budget Clearance",
            meetingType = "Faculty Meeting",
            date = "Oct 15, 2026",
            time = "03:00 PM",
            location = "Dean Office Boardroom",
            participants = "Prof. Marcus Sterling, Dr. Eleanor Vance, Rahul Sharma",
            agenda = "Budget audit, security sanctions, student attendance leave concessions approval.",
            notes = "Approved ₹60,000 budget with sanction order #AIT-EVT-2026-88.",
            actionItems = listOf(
                MeetingActionItem("act_4", "Submit advance voucher receipts", "Rahul Sharma", isConvertedToTask = true)
            )
        )
    )

    private fun getInitialDocuments(): List<EventDocument> = listOf(
        EventDocument("doc_01", "evt_techfest_2026", "Principal Sanction & Permission Order", "Permission letters", "PDF", "Dr. Eleanor Vance", "Oct 04, 2026", "1.2 MB"),
        EventDocument("doc_02", "evt_techfest_2026", "Corporate Sponsorship Deck & Tiers", "Sponsorship", "PDF", "Kiran Reddy", "Oct 06, 2026", "3.8 MB"),
        EventDocument("doc_03", "evt_techfest_2026", "Official Print Poster (High-Res 300DPI)", "Posters", "PNG", "Priya Nair", "Oct 08, 2026", "8.4 MB"),
        EventDocument("doc_04", "evt_techfest_2026", "Stage & Stall Floor Layout Blueprint", "Schedules", "PDF", "Arjun Das", "Oct 10, 2026", "2.1 MB"),
        EventDocument("doc_05", "evt_techfest_2026", "Catering Advance Invoice & Food Menu", "Invoices", "PDF", "Manoj Kumar", "Oct 12, 2026", "950 KB"),
        EventDocument("doc_06", "evt_techfest_2026", "Laser Projector Vendor Requisition Receipt", "Receipts", "PDF", "Arjun Das", "Oct 13, 2026", "620 KB")
    )

    private fun getInitialActivity(): List<ActivityLog> = listOf(
        ActivityLog("act_01", "evt_techfest_2026", "Sreenu Gorkal completed task 'Test QR ticket check-in scanner speed'", "Sreenu Gorkal", "15 mins ago"),
        ActivityLog("act_02", "evt_techfest_2026", "Rahul Sharma added expense '₹2,500 for Audio cables & adapters'", "Rahul Sharma", "1 hour ago"),
        ActivityLog("act_03", "evt_techfest_2026", "Priya Nair uploaded 'Official Print Poster' (8.4 MB)", "Priya Nair", "3 hours ago"),
        ActivityLog("act_04", "evt_techfest_2026", "Kiran Reddy updated sponsor contract status for Google Cloud", "Kiran Reddy", "Yesterday"),
        ActivityLog("act_05", "evt_techfest_2026", "Arjun Das confirmed requirement '4K Laser Projector & Screen'", "Arjun Das", "Yesterday"),
        ActivityLog("act_06", "evt_techfest_2026", "Rahul Sharma scheduled meeting 'Final Core Operations Walkthrough'", "Rahul Sharma", "2 days ago")
    )

    private fun getInitialNotifications(): List<OpsNotification> = listOf(
        OpsNotification(
            id = "notif_01",
            title = "Task Assignment",
            message = "Rahul Sharma assigned you to 'Set up on-spot ticket registration counter & volunteer laptops'",
            type = NotificationType.TASK,
            timestamp = "20 mins ago",
            eventId = "evt_techfest_2026",
            eventName = "Annual Technical Fest 2026",
            taskId = "tsk_01",
            isRead = false,
            isUrgent = false
        ),
        OpsNotification(
            id = "notif_02",
            title = "Urgent Budget Sanction Cleared",
            message = "Dean Office and Prof. Marcus approved the ₹60,000 budget allocation for Tech Fest 2026.",
            type = NotificationType.BUDGET,
            timestamp = "1 hour ago",
            eventId = "evt_techfest_2026",
            eventName = "Annual Technical Fest 2026",
            isRead = false,
            isUrgent = true
        ),
        OpsNotification(
            id = "notif_03",
            title = "Critical Deadline Approaching",
            message = "Website & Registration launch deadline is due in 3 days. 2 pending subtasks.",
            type = NotificationType.DEADLINE,
            timestamp = "3 hours ago",
            eventId = "evt_techfest_2026",
            eventName = "Annual Technical Fest 2026",
            isRead = false,
            isUrgent = true
        ),
        OpsNotification(
            id = "notif_04",
            title = "Internal Team Broadcast",
            message = "Rahul Sharma: 'Core committee sync meeting scheduled for 5:00 PM at Seminar Hall 2.'",
            type = NotificationType.ANNOUNCEMENT,
            timestamp = "Yesterday",
            eventId = "evt_techfest_2026",
            eventName = "Annual Technical Fest 2026",
            isRead = true,
            isUrgent = false
        ),
        OpsNotification(
            id = "notif_05",
            title = "Logistics Requirement Confirmed",
            message = "Arjun Das marked '4K Laser Projector & Screen' as Available from Central IT Lab.",
            type = NotificationType.GENERAL,
            timestamp = "2 days ago",
            eventId = "evt_techfest_2026",
            eventName = "Annual Technical Fest 2026",
            isRead = true,
            isUrgent = false
        )
    )
}
