package com.example.data

object SeedData {
  val TODAY: String
    get() = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
  val DAY_1: String
    get() = getDaysAgoString(1)
  val DAY_2: String
    get() = getDaysAgoString(2)
  val DAY_3: String
    get() = getDaysAgoString(3)
  val DAY_4: String
    get() = getDaysAgoString(4)
  val DAY_5: String
    get() = getDaysAgoString(5)
  val DAY_6: String
    get() = getDaysAgoString(6)
  val DAY_7: String
    get() = getDaysAgoString(7)
  val DAY_8: String
    get() = getDaysAgoString(8)
  val DAY_9: String
    get() = getDaysAgoString(9)

  private fun getDaysAgoString(daysAgo: Int): String {
    val cal = java.util.Calendar.getInstance()
    cal.add(java.util.Calendar.DAY_OF_YEAR, -daysAgo)
    return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(cal.time)
  }

  val companyConfig = CompanyConfigEntity(
    configKey = "default_config",
    eodCutoffTime = "18:30",
    expectedDailyHours = 8.0,
    workDaysPerWeek = 5
  )

  val departments = listOf(
    DepartmentEntity(
      deptId = "DEPT_ML",
      name = "Machine Learning",
      code = "ML",
      prefix = "ML",
      managerIds = "EMP009",
      description = "Predictive modeling, deep learning, and AI algorithms"
    ),
    DepartmentEntity(
      deptId = "DEPT_DB",
      name = "Database",
      code = "DB",
      prefix = "DB",
      managerIds = "EMP009,EMP006",
      description = "Database architecture, query optimization, and warehousing"
    ),
    DepartmentEntity(
      deptId = "DEPT_GT",
      name = "General Tech",
      code = "GT",
      prefix = "GT",
      managerIds = "EMP008",
      description = "Core tech infrastructure and enterprise platforms"
    ),
    DepartmentEntity(
      deptId = "DEPT_WRITING",
      name = "Content & Writing",
      code = "Writing",
      prefix = "WR",
      managerIds = "EMP005",
      description = "Technical documentation, user guides, and research"
    ),
    DepartmentEntity(
      deptId = "DEPT_CYBER",
      name = "Cyber Security",
      code = "Cyber",
      prefix = "CYBER",
      managerIds = "ADMIN",
      description = "Information security, threat detection, and audits"
    )
  )

  val teams = listOf(
    TeamEntity(
      teamId = "TEAM_ML",
      name = "ML Team",
      department = "ML",
      managerId = "EMP009",
      managerName = "Vikram Joshi",
      projects = "Employee Behavior Prediction, NLP Summarizer, Model Evaluation",
      createdDate = "2024-01-10",
      status = "Active",
      description = "Predictive modeling and behavioral analytics team",
      teamPassword = "ml123"  // Unique password for ML Team
    ),
    TeamEntity(
      teamId = "TEAM_WRITE",
      name = "Writing Team",
      department = "Writing",
      managerId = "EMP005",
      managerName = "Anita Desai",
      projects = "Technical Documentation, API Guides, Case Studies, Release Notes",
      createdDate = "2024-01-15",
      status = "Active",
      description = "Developer documentation and product content",
      teamPassword = "write123"  // Unique password for Writing Team
    ),
    TeamEntity(
      teamId = "TEAM_DB",
      name = "DB Team",
      department = "DB",
      managerId = "EMP006",
      managerName = "Ramesh Rao",
      projects = "PostgreSQL Migration, ETL Pipeline, Replica Scaling, Performance Indexing",
      createdDate = "2024-02-01",
      status = "Active",
      description = "Core database infrastructure and data warehousing",
      teamPassword = "db123"  // Unique password for DB Team
    ),
    TeamEntity(
      teamId = "TEAM_GEN",
      name = "GT Team",
      department = "GT",
      managerId = "EMP008",
      managerName = "Sita Verma",
      projects = "Operations & Compliance, Administrative Support, Cross-Team Coordination",
      createdDate = "2023-11-01",
      status = "Active",
      description = "General operations, compliance, and enterprise planning",
      teamPassword = "gt123"  // Unique password for GT Team
    ),
    TeamEntity(
      teamId = "TEAM_CYBER",
      name = "Cyber Security",
      department = "Cyber",
      managerId = "ADMIN",
      managerName = "System Administrator",
      projects = "Security Audits, Penetration Testing, Compliance Monitoring",
      createdDate = "2023-06-01",
      status = "Active",
      description = "Cybersecurity and information security team",
      teamPassword = "cyber123"  // Unique password for Cyber Security
    )
  )

  val employees = listOf(
    EmployeeEntity(
      employeeId = "ADMIN",
      name = "System Administrator",
      email = "admin@workcore.internal",
      phone = "+1 (555) 000-0001",
      department = "Cyber",
      departmentId = "DEPT_CYBER",
      team = "Executive",
      managerId = "BOARD",
      designation = "Managing Director / Admin",
      defaultProject = "Executive Governance",
      joiningDate = "2023-01-01",
      role = Role.ADMIN.name,
      password = "admin123"
    ),
    // Manager: Vikram Joshi (Manages ML and DB)
    EmployeeEntity(
      employeeId = "EMP009",
      name = "Vikram Joshi",
      email = "vikram.joshi@company.com",
      phone = "+1 (555) 012-3459",
      department = "ML",
      departmentId = "DEPT_ML",
      managedDepartments = "ML,DB",
      team = "ML Team",
      managerId = "ADMIN",
      designation = "ML Lead Researcher",
      defaultProject = "Model Evaluation & Infra",
      joiningDate = "2024-02-10",
      role = Role.EMPLOYEE.name
    ),
    // ML Team Member 1: Anil Sharma (Pending today, 73% rate, 3 missed, 2 leaves)
    EmployeeEntity(
      employeeId = "ML-001",
      name = "Anil Sharma",
      email = "anil.sharma@company.com",
      phone = "+1 (555) 234-5671",
      department = "ML",
      departmentId = "DEPT_ML",
      team = "ML Team",
      managerId = "EMP009",
      designation = "ML Researcher",
      defaultProject = "Employee Behavior Prediction",
      joiningDate = "2024-01-15",
      role = Role.EMPLOYEE.name
    ),
    // ML Team Member 2: Priya Nair (Pending today, 91% rate, 1 missed, 0 leaves)
    EmployeeEntity(
      employeeId = "ML-002",
      name = "Priya Nair",
      email = "priya.nair@company.com",
      phone = "+1 (555) 345-6782",
      department = "ML",
      departmentId = "DEPT_ML",
      team = "ML Team",
      managerId = "EMP009",
      designation = "NLP Engineer",
      defaultProject = "NLP Summarizer",
      joiningDate = "2024-02-01",
      role = Role.EMPLOYEE.name
    ),
    // ML Team Member 3: Ravi Kumar (Done today, 100% rate, 0 missed, 1 leave)
    EmployeeEntity(
      employeeId = "EMP001",
      name = "Ravi Kumar",
      email = "ravi.kumar@company.com",
      phone = "+1 (555) 456-7893",
      department = "ML",
      departmentId = "DEPT_ML",
      team = "ML Team",
      managerId = "EMP009",
      designation = "Senior ML Engineer",
      defaultProject = "Churn v2",
      joiningDate = "2024-01-15",
      role = Role.EMPLOYEE.name
    ),
    // ML Team Member 4: Sneha Menon (Leave today, 88% rate, 1 missed, 4 leaves)
    EmployeeEntity(
      employeeId = "ML-004",
      name = "Sneha Menon",
      email = "sneha.menon@company.com",
      phone = "+1 (555) 567-8904",
      department = "ML",
      departmentId = "DEPT_ML",
      team = "ML Team",
      managerId = "EMP009",
      designation = "Data Scientist",
      defaultProject = "Feature Store",
      joiningDate = "2024-03-10",
      role = Role.EMPLOYEE.name
    ),
    // ML Team Member 5: Karthik V (Done today, 95% rate, 1 missed, 0 leaves)
    EmployeeEntity(
      employeeId = "ML-005",
      name = "Karthik V",
      email = "karthik.v@company.com",
      phone = "+1 (555) 678-9015",
      department = "ML",
      departmentId = "DEPT_ML",
      team = "ML Team",
      managerId = "EMP009",
      designation = "MLOps Engineer",
      defaultProject = "Model Deployment Pipeline",
      joiningDate = "2024-03-20",
      role = Role.EMPLOYEE.name
    ),
    // ML Team Member 6: Suresh Yadav (Done today, 100% rate, 0 missed, 0 leaves)
    EmployeeEntity(
      employeeId = "EMP004",
      name = "Suresh Yadav",
      email = "suresh.yadav@company.com",
      phone = "+1 (555) 789-0126",
      department = "ML",
      departmentId = "DEPT_ML",
      team = "ML Team",
      managerId = "EMP009",
      designation = "Data Engineer",
      defaultProject = "Data Cleaning & Preprocessing",
      joiningDate = "2023-11-20",
      role = Role.EMPLOYEE.name
    ),
    // ML Team Member 7: Arjun Mehta (Done today, 100% rate, 0 missed, 0 leaves)
    EmployeeEntity(
      employeeId = "ML-007",
      name = "Arjun Mehta",
      email = "arjun.mehta@company.com",
      phone = "+1 (555) 890-1237",
      department = "ML",
      departmentId = "DEPT_ML",
      team = "ML Team",
      managerId = "EMP009",
      designation = "CV Specialist",
      defaultProject = "Visual Analytics",
      joiningDate = "2024-04-01",
      role = Role.EMPLOYEE.name
    ),
    // ML Team Member 8: Divya Rao (Done today, 100% rate, 0 missed, 0 leaves)
    EmployeeEntity(
      employeeId = "ML-008",
      name = "Divya Rao",
      email = "divya.rao@company.com",
      phone = "+1 (555) 901-2348",
      department = "ML",
      departmentId = "DEPT_ML",
      team = "ML Team",
      managerId = "EMP009",
      designation = "AI Alignment Analyst",
      defaultProject = "Safety & Guardrails",
      joiningDate = "2024-04-15",
      role = Role.EMPLOYEE.name
    ),
    // DB Team Member
    EmployeeEntity(
      employeeId = "DB-001",
      name = "Priya Patel",
      email = "priya.patel@company.com",
      phone = "+1 (555) 012-3450",
      department = "DB",
      departmentId = "DEPT_DB",
      team = "DB Team",
      managerId = "EMP009",
      designation = "Database Architect",
      defaultProject = "PostgreSQL Migration",
      joiningDate = "2023-08-10",
      role = Role.EMPLOYEE.name
    ),
    // ----------------- GT Team (5 Employees, strictly separate personal dashboards) -----------------
    EmployeeEntity(
      employeeId = "GT-001",
      name = "Deepak Kumar",
      email = "deepak.kumar@company.com",
      phone = "+1 (555) 701-0001",
      department = "GT",
      departmentId = "DEPT_GT",
      team = "GT Team",
      managerId = "EMP008",
      designation = "Growth Analyst",
      defaultProject = "Performance Marketing",
      joiningDate = "2024-02-01",
      role = Role.EMPLOYEE.name,
      password = "password123"
    ),
    EmployeeEntity(
      employeeId = "GT-002",
      name = "Nisha Reddy",
      email = "nisha.reddy@company.com",
      phone = "+1 (555) 701-0002",
      department = "GT",
      departmentId = "DEPT_GT",
      team = "GT Team",
      managerId = "EMP008",
      designation = "SEO Specialist",
      defaultProject = "Organic Search Optimization",
      joiningDate = "2024-02-15",
      role = Role.EMPLOYEE.name,
      password = "password123"
    ),
    EmployeeEntity(
      employeeId = "GT-003",
      name = "Arjun Joseph",
      email = "arjun.joseph@company.com",
      phone = "+1 (555) 701-0003",
      department = "GT",
      departmentId = "DEPT_GT",
      team = "GT Team",
      managerId = "EMP008",
      designation = "Growth Marketer",
      defaultProject = "User Acquisition Funnel",
      joiningDate = "2024-03-01",
      role = Role.EMPLOYEE.name,
      password = "password123"
    ),
    EmployeeEntity(
      employeeId = "GT-004",
      name = "Pooja Thomas",
      email = "pooja.thomas@company.com",
      phone = "+1 (555) 701-0004",
      department = "GT",
      departmentId = "DEPT_GT",
      team = "GT Team",
      managerId = "EMP008",
      designation = "Content Strategist",
      defaultProject = "Brand Campaign v2",
      joiningDate = "2024-03-15",
      role = Role.EMPLOYEE.name,
      password = "password123"
    ),
    EmployeeEntity(
      employeeId = "GT-005",
      name = "Vikram Singh",
      email = "vikram.singh@company.com",
      phone = "+1 (555) 701-0005",
      department = "GT",
      departmentId = "DEPT_GT",
      team = "GT Team",
      managerId = "EMP008",
      designation = "Performance Marketer",
      defaultProject = "Paid Ads Scaling",
      joiningDate = "2024-04-01",
      role = Role.EMPLOYEE.name,
      password = "password123"
    )
  )

  val notifications = listOf(
    NotificationEntity(
      title = "🔴 EOD Pending",
      message = "Suresh Yadav (ML Team) has not submitted today's EOD.",
      type = "PENDING",
      targetRole = "ALL",
      targetTeam = "ML Team",
      targetEmployeeId = "EMP004",
      timeAgo = "10 mins ago"
    ),
    NotificationEntity(
      title = "🔴 Multiple Pending",
      message = "2 employees in ML Team have pending EODs.",
      type = "MULTIPLE_PENDING",
      targetRole = "ADMIN",
      targetTeam = "ML Team",
      timeAgo = "30 mins ago"
    ),
    NotificationEntity(
      title = "🟠 Late EOD",
      message = "Anita Desai submitted today's EOD after the 18:30 cutoff.",
      type = "LATE",
      targetRole = "ADMIN",
      targetEmployeeId = "EMP005",
      timeAgo = "1 hr ago"
    ),
    NotificationEntity(
      title = "🟢 EOD Complete",
      message = "All active members in Writing Team have submitted today's EOD.",
      type = "COMPLETE",
      targetRole = "ALL",
      targetTeam = "Writing Team",
      timeAgo = "2 hrs ago"
    ),
    NotificationEntity(
      title = "🔴 Attention Alert",
      message = "Arun Singh has missed 3 consecutive EOD submissions.",
      type = "PENDING",
      targetRole = "ADMIN",
      targetEmployeeId = "EMP003",
      timeAgo = "Today"
    )
  )

  val dailyEods = listOf(
    // ----------------- EMP001: Ravi Kumar (ML Team - Done today) -----------------
    DailyEodEntity(
      employeeId = "EMP001",
      employeeName = "Ravi Kumar",
      departmentId = "DEPT_ML",
      departmentCode = "ML",
      attendanceStatus = "Present",
      date = TODAY,
      project = "Churn v2",
      todayWork = "Completed login API integration and OAuth token refresh testing",
      workCompleted = "Added session expiration handler and integrated with auth interceptor",
      hoursWorked = 7.5,
      progressPercentage = 80,
      workStatus = "Completed",
      blockers = "Waiting on staging DB access",
      remarks = "All tests green",
      tomorrowPlan = "Feature store migration",
      submissionTime = "17:45",
      isOnTime = true
    ),
    DailyEodEntity(
      employeeId = "EMP001",
      employeeName = "Ravi Kumar",
      departmentId = "DEPT_ML",
      departmentCode = "ML",
      attendanceStatus = "Present",
      date = DAY_1,
      project = "Churn v2",
      todayWork = "Churn v2 evaluation, 3 tasks",
      workCompleted = "Model trained on Q3 data",
      hoursWorked = 8.5,
      progressPercentage = 75,
      workStatus = "Completed",
      submissionTime = "18:10",
      isOnTime = true
    ),

    // ----------------- ML-004: Sneha Menon (ML Team - Leave today) -----------------
    DailyEodEntity(
      employeeId = "ML-004",
      employeeName = "Sneha Menon",
      departmentId = "DEPT_ML",
      departmentCode = "ML",
      attendanceStatus = "Leave",
      date = TODAY,
      project = "Feature Store",
      todayWork = "On planned medical leave",
      workCompleted = "Handed off tickets to Karthik",
      hoursWorked = 0.0,
      progressPercentage = 0,
      workStatus = "On Leave",
      blockers = "",
      remarks = "Returning tomorrow",
      tomorrowPlan = "Resume feature store ingestion",
      submissionTime = "09:30",
      isOnTime = true
    ),

    // ----------------- ML-005: Karthik V (ML Team - Done today) -----------------
    DailyEodEntity(
      employeeId = "ML-005",
      employeeName = "Karthik V",
      departmentId = "DEPT_ML",
      departmentCode = "ML",
      attendanceStatus = "Present",
      date = TODAY,
      project = "Model Deployment Pipeline",
      todayWork = "Configured Triton inference server and k8s auto-scaling",
      workCompleted = "Deployed v2 replica to staging cluster",
      hoursWorked = 8.0,
      progressPercentage = 85,
      workStatus = "Completed",
      submissionTime = "18:00",
      isOnTime = true
    ),

    // ----------------- EMP004: Suresh Yadav (ML Team - Done today) -----------------
    DailyEodEntity(
      employeeId = "EMP004",
      employeeName = "Suresh Yadav",
      departmentId = "DEPT_ML",
      departmentCode = "ML",
      attendanceStatus = "Present",
      date = TODAY,
      project = "Data Cleaning & Preprocessing",
      todayWork = "ETL pipeline validation and schema checks",
      workCompleted = "Cleaned 40M clickstream events",
      hoursWorked = 8.0,
      progressPercentage = 90,
      workStatus = "Completed",
      submissionTime = "17:30",
      isOnTime = true
    ),

    // ----------------- ML-007: Arjun Mehta (ML Team - Done today) -----------------
    DailyEodEntity(
      employeeId = "ML-007",
      employeeName = "Arjun Mehta",
      departmentId = "DEPT_ML",
      departmentCode = "ML",
      attendanceStatus = "Present",
      date = TODAY,
      project = "Visual Analytics",
      todayWork = "YOLOv8 benchmark latency profiling on TensorRT",
      workCompleted = "Achieved 12ms inference per frame",
      hoursWorked = 8.0,
      progressPercentage = 80,
      workStatus = "Completed",
      submissionTime = "17:50",
      isOnTime = true
    ),

    // ----------------- ML-008: Divya Rao (ML Team - Done today) -----------------
    DailyEodEntity(
      employeeId = "ML-008",
      employeeName = "Divya Rao",
      departmentId = "DEPT_ML",
      departmentCode = "ML",
      attendanceStatus = "Present",
      date = TODAY,
      project = "Safety & Guardrails",
      todayWork = "Red-teaming evaluation on prompt injections",
      workCompleted = "Tested 250 boundary safety test cases",
      hoursWorked = 8.0,
      progressPercentage = 95,
      workStatus = "Completed",
      submissionTime = "18:05",
      isOnTime = true
    ),

    // ----------------- ML-001: Anil Sharma (ML Team - Pending today, past EODs) -----------------
    DailyEodEntity(
      employeeId = "ML-001",
      employeeName = "Anil Sharma",
      departmentId = "DEPT_ML",
      departmentCode = "ML",
      attendanceStatus = "Present",
      date = DAY_1,
      project = "Employee Behavior Prediction",
      todayWork = "Hyperparameter tuning for gradient boost",
      workCompleted = "Logged experiment runs to MLflow",
      hoursWorked = 8.0,
      progressPercentage = 70,
      workStatus = "In Progress",
      submissionTime = "18:20",
      isOnTime = true
    ),

    // ----------------- ML-002: Priya Nair (ML Team - Pending today, past EODs) -----------------
    DailyEodEntity(
      employeeId = "ML-002",
      employeeName = "Priya Nair",
      departmentId = "DEPT_ML",
      departmentCode = "ML",
      attendanceStatus = "Present",
      date = DAY_1,
      project = "NLP Summarizer",
      todayWork = "Token trimming and context window caching",
      workCompleted = "Evaluated ROUGE scores",
      hoursWorked = 8.0,
      progressPercentage = 75,
      workStatus = "In Progress",
      submissionTime = "18:15",
      isOnTime = true
    ),
    DailyEodEntity(
      employeeId = "EMP001",
      employeeName = "Ravi Kumar",
      date = DAY_1,
      project = "E-Commerce Website",
      todayWork = "Built user profile edit endpoints and avatar file upload",
      workCompleted = "Implemented S3 signed url generation and client preview",
      hoursWorked = 8.0,
      progressPercentage = 70,
      workStatus = "In Progress",
      blockers = "",
      remarks = "All unit tests green",
      tomorrowPlan = "Work on login security tests",
      submissionTime = "18:10",
      isOnTime = true
    ),
    DailyEodEntity(
      employeeId = "EMP001",
      employeeName = "Ravi Kumar",
      date = DAY_2,
      project = "E-Commerce Website",
      todayWork = "Shopping cart checkout validation and tax calculator service",
      workCompleted = "Tax service calculates region-based vat and handles coupon codes",
      hoursWorked = 8.0,
      progressPercentage = 60,
      workStatus = "In Progress",
      blockers = "",
      remarks = "Tested edge cases with zero-item and max quantity carts",
      tomorrowPlan = "User profile and avatar upload",
      submissionTime = "17:50",
      isOnTime = true
    ),
    DailyEodEntity(
      employeeId = "EMP001",
      employeeName = "Ravi Kumar",
      date = DAY_3,
      project = "E-Commerce Website",
      todayWork = "Cart state management and local storage cache persistence",
      workCompleted = "Finished reactive cart store with synchronization",
      hoursWorked = 7.5,
      progressPercentage = 50,
      workStatus = "In Progress",
      blockers = "",
      remarks = "Smooth integration with frontend hooks",
      tomorrowPlan = "Checkout validation",
      submissionTime = "18:15",
      isOnTime = true
    ),
    DailyEodEntity(
      employeeId = "EMP001",
      employeeName = "Ravi Kumar",
      date = DAY_4,
      project = "E-Commerce Website",
      todayWork = "Catalog search and Elasticsearch query optimization",
      workCompleted = "Added filter facets for price, category, and availability",
      hoursWorked = 8.5,
      progressPercentage = 40,
      workStatus = "In Progress",
      blockers = "",
      remarks = "Search latency dropped from 220ms to 45ms",
      tomorrowPlan = "Cart state management",
      submissionTime = "17:35",
      isOnTime = true
    ),
    DailyEodEntity(
      employeeId = "EMP001",
      employeeName = "Ravi Kumar",
      date = DAY_5,
      project = "E-Commerce Website",
      todayWork = "Product detail view API and inventory check endpoint",
      workCompleted = "Created endpoint with Redis cached product details",
      hoursWorked = 8.0,
      progressPercentage = 30,
      workStatus = "In Progress",
      blockers = "",
      remarks = "Cache invalidation logic tested",
      tomorrowPlan = "Elasticsearch query optimization",
      submissionTime = "18:00",
      isOnTime = true
    ),
    DailyEodEntity(
      employeeId = "EMP001",
      employeeName = "Ravi Kumar",
      date = DAY_6,
      project = "E-Commerce Website",
      todayWork = "Category navigation hierarchy and product listing query",
      workCompleted = "Finished database migrations and seeded test catalog",
      hoursWorked = 7.5,
      progressPercentage = 20,
      workStatus = "In Progress",
      blockers = "",
      remarks = "Ready for frontend integration",
      tomorrowPlan = "Product details view API",
      submissionTime = "18:05",
      isOnTime = true
    ),

    // ----------------- EMP002: Priya Patel (Database Architect, Strong) -----------------
    DailyEodEntity(
      employeeId = "EMP002",
      employeeName = "Priya Patel",
      date = TODAY,
      project = "PostgreSQL Migration",
      todayWork = "Optimized transaction log partitioning and query planner statistics",
      workCompleted = "Created 12 monthly partitions and verified sequential scan elimination",
      hoursWorked = 8.0,
      progressPercentage = 90,
      workStatus = "In Progress",
      blockers = "",
      remarks = "Benchmarked at 15,000 writes/sec without lock contention",
      tomorrowPlan = "Perform failover simulation in staging replica",
      submissionTime = "18:05",
      isOnTime = true
    ),
    DailyEodEntity(
      employeeId = "EMP002",
      employeeName = "Priya Patel",
      date = DAY_1,
      project = "PostgreSQL Migration",
      todayWork = "Configured logical replication stream between primary and staging",
      workCompleted = "Initial snapshot synchronized 2.4 TB of customer records",
      hoursWorked = 7.5,
      progressPercentage = 80,
      workStatus = "In Progress",
      blockers = "",
      remarks = "Replication lag currently under 200 milliseconds",
      tomorrowPlan = "Partitioning setup",
      submissionTime = "17:40",
      isOnTime = true
    ),
    DailyEodEntity(
      employeeId = "EMP002",
      employeeName = "Priya Patel",
      date = DAY_2,
      project = "PostgreSQL Migration",
      todayWork = "Audited foreign key indexes to eliminate unindexed table joins",
      workCompleted = "Added 14 missing composite indexes on order_items table",
      hoursWorked = 8.0,
      progressPercentage = 70,
      workStatus = "In Progress",
      blockers = "",
      remarks = "Query CPU utilization reduced by 35%",
      tomorrowPlan = "Configure replication stream",
      submissionTime = "18:25",
      isOnTime = true
    ),
    DailyEodEntity(
      employeeId = "EMP002",
      employeeName = "Priya Patel",
      date = DAY_3,
      project = "PostgreSQL Migration",
      todayWork = "Connection pool tuning with PgBouncer under simulated load",
      workCompleted = "Determined optimal pool size of 80 connections per replica node",
      hoursWorked = 7.0,
      progressPercentage = 60,
      workStatus = "In Progress",
      blockers = "",
      remarks = "Connection churn eliminated",
      tomorrowPlan = "Foreign key audit",
      submissionTime = "17:55",
      isOnTime = true
    ),

    // ----------------- EMP003: Arun Singh (Needs Attention - Missed EODs, Missed Today & Day 1) -----------------
    // NO submission for TODAY
    // NO submission for DAY_1
    DailyEodEntity(
      employeeId = "EMP003",
      employeeName = "Arun Singh",
      date = DAY_2,
      project = "Automated Test Suite",
      todayWork = "Automated smoke test pipeline execution for release 2.4",
      workCompleted = "Configured GitHub Actions runner for parallel regression suite",
      hoursWorked = 6.0,
      progressPercentage = 45,
      workStatus = "In Progress",
      blockers = "Flaky tests in checkout module due to network timeouts",
      remarks = "Investigating timeout triggers",
      tomorrowPlan = "Fix retry policies and test isolation",
      submissionTime = "19:15",
      isOnTime = false // Late submission
    ),
    DailyEodEntity(
      employeeId = "EMP003",
      employeeName = "Arun Singh",
      date = DAY_3,
      project = "Automated Test Suite",
      todayWork = "Wrote Cypress end-to-end tests for customer registration flow",
      workCompleted = "18 new test scenarios covering password requirements and verification",
      hoursWorked = 5.5,
      progressPercentage = 40,
      workStatus = "In Progress",
      blockers = "Test environment server reboot delayed testing by 3 hours",
      remarks = "Submitting late due to environment recovery",
      tomorrowPlan = "Automated smoke tests",
      submissionTime = "19:50",
      isOnTime = false // Late submission
    ),
    DailyEodEntity(
      employeeId = "EMP003",
      employeeName = "Arun Singh",
      date = DAY_5,
      project = "Automated Test Suite",
      todayWork = "Performance load testing on product search endpoint",
      workCompleted = "Simulated 5,000 concurrent users using k6 framework",
      hoursWorked = 6.5,
      progressPercentage = 35,
      workStatus = "In Progress",
      blockers = "",
      remarks = "Report shared with backend team",
      tomorrowPlan = "Cypress registration tests",
      submissionTime = "18:10",
      isOnTime = true
    ),

    // ----------------- EMP004: Suresh Yadav (API Architect, Strong) -----------------
    DailyEodEntity(
      employeeId = "EMP004",
      employeeName = "Suresh Yadav",
      date = TODAY,
      project = "Payment Gateway API",
      todayWork = "Completed Stripe & PayPal unified webhook reconciliation engine",
      workCompleted = "Implemented idempotent charge verification and ledger balance updates",
      hoursWorked = 8.5,
      progressPercentage = 100,
      workStatus = "Completed",
      blockers = "",
      remarks = "Passed full penetration review with 0 critical security issues",
      tomorrowPlan = "Begin architecture specification for multi-currency wallet",
      submissionTime = "17:30",
      isOnTime = true
    ),
    DailyEodEntity(
      employeeId = "EMP004",
      employeeName = "Suresh Yadav",
      date = DAY_1,
      project = "Payment Gateway API",
      todayWork = "Refund processing engine and dispute event listener",
      workCompleted = "Added automated credit issuance logic and audit journal entries",
      hoursWorked = 8.0,
      progressPercentage = 85,
      workStatus = "In Progress",
      blockers = "",
      remarks = "Reconciliation edge cases covered",
      tomorrowPlan = "Unified webhook reconciliation",
      submissionTime = "17:40",
      isOnTime = true
    ),
    DailyEodEntity(
      employeeId = "EMP004",
      employeeName = "Suresh Yadav",
      date = DAY_2,
      project = "Payment Gateway API",
      todayWork = "Card tokenization service with PCI-DSS vault encryption",
      workCompleted = "Encrypted card hashes with AES-256-GCM and key rotation mechanism",
      hoursWorked = 8.0,
      progressPercentage = 75,
      workStatus = "In Progress",
      blockers = "",
      remarks = "Key rotation test passed",
      tomorrowPlan = "Refund processing engine",
      submissionTime = "18:10",
      isOnTime = true
    ),

    // ----------------- EMP005: Anita Desai (UI/UX Engineer, Good) -----------------
    DailyEodEntity(
      employeeId = "EMP005",
      employeeName = "Anita Desai",
      date = TODAY,
      project = "Design System & UI",
      todayWork = "Designed and coded responsive checkout drawer and payment cards",
      workCompleted = "Created composables for M3 token system and adaptive layout breakpoints",
      hoursWorked = 7.0,
      progressPercentage = 75,
      workStatus = "In Progress",
      blockers = "",
      remarks = "Design reviewed and approved by product manager",
      tomorrowPlan = "Order confirmation animated success screen",
      submissionTime = "18:15",
      isOnTime = true
    ),
    DailyEodEntity(
      employeeId = "EMP005",
      employeeName = "Anita Desai",
      date = DAY_1,
      project = "Design System & UI",
      todayWork = "Color palette refactoring to support dynamic contrast and dark theme",
      workCompleted = "Audited all 45 typography and surface tokens for WCAG AAA compliance",
      hoursWorked = 7.5,
      progressPercentage = 65,
      workStatus = "In Progress",
      blockers = "",
      remarks = "Accessibility check score 100%",
      tomorrowPlan = "Checkout drawer UI",
      submissionTime = "18:00",
      isOnTime = true
    ),
    DailyEodEntity(
      employeeId = "EMP005",
      employeeName = "Anita Desai",
      date = DAY_2,
      project = "Design System & UI",
      todayWork = "Component library documentation and Figma variable sync script",
      workCompleted = "Automated token generation from Figma REST API into Android resources",
      hoursWorked = 8.0,
      progressPercentage = 50,
      workStatus = "In Progress",
      blockers = "",
      remarks = "Engineers can now pull latest tokens via Gradle task",
      tomorrowPlan = "Color palette refactoring",
      submissionTime = "17:55",
      isOnTime = true
    ),

    // ----------------- EMP006: Ramesh Rao (Frequent Blockers - Needs Attention) -----------------
    DailyEodEntity(
      employeeId = "EMP006",
      employeeName = "Ramesh Rao",
      date = TODAY,
      project = "Kubernetes Cluster v2",
      todayWork = "Attempted deployment of ingress controller on AWS EKS cluster",
      workCompleted = "Wrote Helm chart overrides and cert-manager configuration",
      hoursWorked = 5.5,
      progressPercentage = 40,
      workStatus = "Blocked",
      blockers = "AWS IAM cross-account production role pending enterprise infosec approval",
      remarks = "Escalated ticket #SEC-8921 to security team lead twice today",
      tomorrowPlan = "Follow up with Infosec director for credential provisioning",
      submissionTime = "16:45",
      isOnTime = true
    ),
    DailyEodEntity(
      employeeId = "EMP006",
      employeeName = "Ramesh Rao",
      date = DAY_1,
      project = "Kubernetes Cluster v2",
      todayWork = "Configured Prometheus and Grafana monitoring daemonsets",
      workCompleted = "Dashboard imported for node memory pressure and pod restart tracking",
      hoursWorked = 6.0,
      progressPercentage = 40,
      workStatus = "Blocked",
      blockers = "Infosec approval for VPC peering gateway blocked by compliance review",
      remarks = "Standing by for compliance sign-off",
      tomorrowPlan = "Ingress controller setup",
      submissionTime = "17:15",
      isOnTime = true
    ),
    DailyEodEntity(
      employeeId = "EMP006",
      employeeName = "Ramesh Rao",
      date = DAY_2,
      project = "Kubernetes Cluster v2",
      todayWork = "Set up cluster autoscaler and spot instance pricing rules",
      workCompleted = "Cluster scales from 3 to 18 worker nodes based on CPU threshold",
      hoursWorked = 7.5,
      progressPercentage = 35,
      workStatus = "In Progress",
      blockers = "",
      remarks = "Spot instance termination handler successfully tested",
      tomorrowPlan = "Prometheus daemonset setup",
      submissionTime = "18:10",
      isOnTime = true
    ),

    // ----------------- EMP007: Kiran Sharma (Frequent Late Submissions) -----------------
    // NO submission for TODAY (Pending)
    DailyEodEntity(
      employeeId = "EMP007",
      employeeName = "Kiran Sharma",
      date = DAY_1,
      project = "Android Client v3",
      todayWork = "Implemented offline Room database sync and conflict resolution worker",
      workCompleted = "Added background WorkManager periodic synchronization",
      hoursWorked = 8.0,
      progressPercentage = 70,
      workStatus = "In Progress",
      blockers = "",
      remarks = "Finished late after long debugging session on SQLite threading",
      tomorrowPlan = "Push notification integration",
      submissionTime = "20:45",
      isOnTime = false // Late submission
    ),
    DailyEodEntity(
      employeeId = "EMP007",
      employeeName = "Kiran Sharma",
      date = DAY_2,
      project = "Android Client v3",
      todayWork = "Jetpack Compose navigation routing and deep link handler",
      workCompleted = "Configured type-safe arguments with Kotlin serialization",
      hoursWorked = 7.5,
      progressPercentage = 60,
      workStatus = "In Progress",
      blockers = "",
      remarks = "Submitted from mobile on commute home",
      tomorrowPlan = "Room database sync worker",
      submissionTime = "19:30",
      isOnTime = false // Late submission
    ),
    DailyEodEntity(
      employeeId = "EMP007",
      employeeName = "Kiran Sharma",
      date = DAY_3,
      project = "Android Client v3",
      todayWork = "Biometric authentication prompt and keystore secure storage",
      workCompleted = "Fingerprint and Face Unlock support added for app resume",
      hoursWorked = 8.0,
      progressPercentage = 50,
      workStatus = "In Progress",
      blockers = "",
      remarks = "Submitting after testing on physical devices",
      tomorrowPlan = "Navigation deep links",
      submissionTime = "19:15",
      isOnTime = false // Late submission
    ),

    // ----------------- EMP008: Sita Verma (Manager, Strong) -----------------
    DailyEodEntity(
      employeeId = "EMP008",
      employeeName = "Sita Verma",
      date = TODAY,
      project = "Release Management",
      todayWork = "Sprint 24 backlog grooming and quarterly OKR milestone tracking",
      workCompleted = "Reviewed daily EOD submissions across Platform Core and Experience teams",
      hoursWorked = 8.0,
      progressPercentage = 95,
      workStatus = "Completed",
      blockers = "",
      remarks = "Coordinated unblocking of Ramesh's IAM ticket with Infosec leadership",
      tomorrowPlan = "Final sprint demo and stakeholder alignment",
      submissionTime = "18:15",
      isOnTime = true
    ),
    DailyEodEntity(
      employeeId = "EMP008",
      employeeName = "Sita Verma",
      date = DAY_1,
      project = "Release Management",
      todayWork = "Cross-team dependency mapping and architectural review board session",
      workCompleted = "Validated API contracts between backend payments and mobile client",
      hoursWorked = 8.5,
      progressPercentage = 85,
      workStatus = "In Progress",
      blockers = "",
      remarks = "All engineering leads aligned on release schedule",
      tomorrowPlan = "Sprint backlog grooming",
      submissionTime = "18:00",
      isOnTime = true
    ),

    // ----------------- EMP009: Vikram Joshi (On Leave Today) -----------------
    DailyEodEntity(
      employeeId = "EMP009",
      employeeName = "Vikram Joshi",
      date = TODAY,
      project = "Cache & Queue Layer",
      todayWork = "Approved medical leave",
      workCompleted = "Handed off urgent standby coverage to Ravi Kumar",
      hoursWorked = 0.0,
      progressPercentage = 0,
      workStatus = "On Leave",
      blockers = "",
      remarks = "Scheduled medical appointment and recovery day",
      tomorrowPlan = "Resume Redis cluster migration",
      submissionTime = "09:15",
      isOnTime = true
    ),
    DailyEodEntity(
      employeeId = "EMP009",
      employeeName = "Vikram Joshi",
      date = DAY_1,
      project = "Cache & Queue Layer",
      todayWork = "Redis cluster sharding configuration and failover sentinel setup",
      workCompleted = "Configured 3 master nodes and 3 slave nodes with automated election",
      hoursWorked = 8.0,
      progressPercentage = 75,
      workStatus = "In Progress",
      blockers = "",
      remarks = "Simulated master node outage with 4-second recovery time",
      tomorrowPlan = "Medical leave on Monday",
      submissionTime = "17:50",
      isOnTime = true
    ),

    // ----------------- EMP010: Meera Nair (No Work Today - Workshop/Training) -----------------
    DailyEodEntity(
      employeeId = "EMP010",
      employeeName = "Meera Nair",
      date = TODAY,
      project = "Penetration Testing",
      todayWork = "Full-day internal cybersecurity seminar and OWASP compliance training",
      workCompleted = "Completed 8-hour certified module on API security and GraphQL attacks",
      hoursWorked = 0.0, // Operational work 0 hours, counted as No Work day per system rules
      progressPercentage = 0,
      workStatus = "No Work",
      blockers = "",
      remarks = "Training certification attained",
      tomorrowPlan = "Apply automated API scanning against staging environment",
      submissionTime = "17:20",
      isOnTime = true
    ),
    DailyEodEntity(
      employeeId = "EMP010",
      employeeName = "Meera Nair",
      date = DAY_1,
      project = "Penetration Testing",
      todayWork = "Security vulnerability assessment on public REST endpoints",
      workCompleted = "Identified and patched 2 rate-limiting gaps on registration endpoint",
      hoursWorked = 7.5,
      progressPercentage = 60,
      workStatus = "In Progress",
      blockers = "",
      remarks = "Report shared with backend engineering team",
      tomorrowPlan = "Attend cybersecurity training workshop",
      submissionTime = "18:05",
      isOnTime = true
    ),

    // ----------------- GT Team Separate Employee Records -----------------
    // GT-001: Deepak Kumar (Submitted for TODAY)
    DailyEodEntity(
      employeeId = "GT-001",
      employeeName = "Deepak Kumar",
      departmentId = "DEPT_GT",
      departmentCode = "GT",
      attendanceStatus = "Present",
      date = TODAY,
      project = "Performance Marketing",
      todayWork = "Audited Google Ads search queries and adjusted bid multipliers for Tier 1 geo",
      workCompleted = "CPA reduced by 14% on core brand campaign",
      hoursWorked = 8.0,
      progressPercentage = 100,
      workStatus = "Completed",
      submissionTime = "18:10",
      isOnTime = true
    ),
    DailyEodEntity(
      employeeId = "GT-001",
      employeeName = "Deepak Kumar",
      departmentId = "DEPT_GT",
      departmentCode = "GT",
      attendanceStatus = "Present",
      date = DAY_1,
      project = "Performance Marketing",
      todayWork = "Created lookalike audience seeds for retargeting campaign",
      workCompleted = "Campaign live in staging",
      hoursWorked = 8.0,
      progressPercentage = 80,
      workStatus = "Completed",
      submissionTime = "18:00",
      isOnTime = true
    ),
    // GT-002: Nisha Reddy (Pending for TODAY, submitted yesterday)
    DailyEodEntity(
      employeeId = "GT-002",
      employeeName = "Nisha Reddy",
      departmentId = "DEPT_GT",
      departmentCode = "GT",
      attendanceStatus = "Present",
      date = DAY_1,
      project = "Organic Search Optimization",
      todayWork = "Technical SEO audit: fixed canonical URL loops and 301 redirect chains",
      workCompleted = "Sitemap regenerated and submitted to Search Console",
      hoursWorked = 8.0,
      progressPercentage = 75,
      workStatus = "Completed",
      submissionTime = "17:45",
      isOnTime = true
    ),
    // GT-003: Arjun Joseph (Pending for TODAY, submitted yesterday)
    DailyEodEntity(
      employeeId = "GT-003",
      employeeName = "Arjun Joseph",
      departmentId = "DEPT_GT",
      departmentCode = "GT",
      attendanceStatus = "Present",
      date = DAY_1,
      project = "User Acquisition Funnel",
      todayWork = "A/B test setup for signup checkout flow variant B",
      workCompleted = "Tracking events verified in Mixpanel",
      hoursWorked = 8.0,
      progressPercentage = 60,
      workStatus = "In Progress",
      submissionTime = "18:25",
      isOnTime = true
    ),
    // GT-004: Pooja Thomas (Submitted for TODAY)
    DailyEodEntity(
      employeeId = "GT-004",
      employeeName = "Pooja Thomas",
      departmentId = "DEPT_GT",
      departmentCode = "GT",
      attendanceStatus = "Present",
      date = TODAY,
      project = "Brand Campaign v2",
      todayWork = "Finalized Q4 thought leadership editorial calendar and designer briefs",
      workCompleted = "5 blog articles and 2 case studies approved by VP",
      hoursWorked = 8.0,
      progressPercentage = 100,
      workStatus = "Completed",
      submissionTime = "17:55",
      isOnTime = true
    ),
    DailyEodEntity(
      employeeId = "GT-004",
      employeeName = "Pooja Thomas",
      departmentId = "DEPT_GT",
      departmentCode = "GT",
      attendanceStatus = "Present",
      date = DAY_1,
      project = "Brand Campaign v2",
      todayWork = "Drafted executive interview highlights and newsletter copy",
      workCompleted = "Reviewed with PR lead",
      hoursWorked = 8.0,
      progressPercentage = 90,
      workStatus = "Completed",
      submissionTime = "17:30",
      isOnTime = true
    ),
    // GT-005: Vikram Singh (Pending for TODAY, submitted yesterday)
    DailyEodEntity(
      employeeId = "GT-005",
      employeeName = "Vikram Singh",
      departmentId = "DEPT_GT",
      departmentCode = "GT",
      attendanceStatus = "Present",
      date = DAY_1,
      project = "Paid Ads Scaling",
      todayWork = "Testing TikTok ad creative rotations and high-intent keyword match types",
      workCompleted = "Initial test batch published",
      hoursWorked = 8.0,
      progressPercentage = 50,
      workStatus = "In Progress",
      submissionTime = "18:20",
      isOnTime = true
    )
  )

  val auditLogs = listOf(
    AuditLogEntity(
      userId = "SYSTEM",
      action = "INIT",
      entityType = "DATABASE",
      entityId = "WORKCORE",
      description = "WorkCore EOD & Work Behavior Analytics database initialized"
    ),
    AuditLogEntity(
      userId = "EMP001",
      action = "SUBMIT_EOD",
      entityType = "DAILY_EOD",
      entityId = "EMP001-2026-09-07",
      description = "Ravi Kumar submitted EOD for 2026-09-07 (7.5 hrs, 80% progress)"
    ),
    AuditLogEntity(
      userId = "EMP004",
      action = "SUBMIT_EOD",
      entityType = "DAILY_EOD",
      entityId = "EMP004-2026-09-07",
      description = "Suresh Yadav submitted EOD for 2026-09-07 (8.5 hrs, 100% Completed)"
    )
  )
}
