# Team Reports Implementation

## Overview
Added a comprehensive Team Reports feature to the Analytics screen that aggregates custom EOD form data for Academic Overseas teams (Counsellor, Telecalling, Marketing, and Other).

## Changes Made

### 1. Data Layer (`WorkCoreRepository.kt`)

#### New Data Classes
```kotlin
data class TeamReportSummary(
  val teamId: String,
  val teamName: String,
  val companyName: String,
  val eodFormType: String,
  val totalMembers: Int,
  val todaySubmissions: Int,
  val customMetrics: CustomTeamMetrics? = null
)

data class CustomTeamMetrics(
  // Telecalling specific
  val totalCalls: Int = 0,
  val totalFollowUps: Int = 0,
  val totalConversions: Int = 0,
  val conversionRate: Double = 0.0,
  
  // Counsellor specific
  val totalStudents: Int = 0,
  val studentsByCountry: Map<String, Int> = emptyMap(),
  val studentsByStatus: Map<String, Int> = emptyMap(),
  
  // Marketing specific
  val totalCollegeVisits: Int = 0,
  val collegesVisited: List<String> = emptyList(),
  
  // General
  val submissionsList: List<CustomFormSubmission> = emptyList()
)

data class CustomFormSubmission(
  val employeeName: String,
  val date: String,
  val formData: String,
  val submissionTime: String
)
```

#### New Repository Methods

**`computeTeamReports()`**
- Filters teams with custom forms (non-STANDARD eodFormType)
- Computes aggregated metrics for each team
- Returns list of TeamReportSummary

**Form-Specific Aggregation Methods:**
- `computeTelecallingMetrics()` - Total calls, follow-ups, conversions, conversion rate
- `computeCounsellorMetrics()` - Total students, breakdown by country & status
- `computeMarketingMetrics()` - College visits, unique colleges visited
- `computeSimpleMetrics()` - Basic submission count

**JSON Deserialization:**
- Uses Gson to parse customFormData JSON from DailyEodEntity
- Graceful error handling for malformed JSON

#### Added Flows
- `allCompanies: Flow<List<CompanyEntity>>` - Exposes company data
- Added `companyDao` to repository

### 2. ViewModel (`WorkCoreViewModel.kt`)

**New State Flow:**
```kotlin
val companies: StateFlow<List<CompanyEntity>>
val teamReports: StateFlow<List<TeamReportSummary>>
```

The `teamReports` flow combines:
- teams
- employees
- dailyEods
- companies

And computes real-time team reports using `repository.computeTeamReports()`.

### 3. UI Layer (`AnalyticsScreen.kt`)

#### Added Third Tab
- Tab 0: Employee Patterns
- Tab 1: Team Analytics
- **Tab 2: Team Reports** (NEW)

#### New Composables

**`TeamReportCard()`**
- Header with team name, company, form type, submission count
- Routes to appropriate metrics display based on form type
- Shows list of today's submissions with employee names and times

**Form-Specific Metric Components:**
1. **`TelecallingMetrics()`**
   - 4 metric cards: Total Calls, Follow-ups, Conversions, Conversion Rate
   - Color-coded cards (green for calls, orange for follow-ups, blue for conversions, purple for rate)

2. **`CounsellorMetrics()`**
   - Total students metric card
   - Country breakdown with counts
   - Shows top 5 countries

3. **`MarketingMetrics()`**
   - College visits metric with unique count
   - List of colleges visited (top 5)

4. **`SimpleMetrics()`**
   - Simple submission count

#### Empty State
Shows message when no custom form teams have submissions:
- "No Team Reports Available"
- "Custom form teams will appear here after submissions"

### 4. MainActivity Integration

**Added State Collection:**
```kotlin
val teamReports by viewModel.teamReports.collectAsState()
```

**Updated AnalyticsScreen Call:**
```kotlin
AnalyticsScreen(
  behaviorMetrics = behaviorMetrics,
  teamSummaries = teamSummaries,
  teamReports = teamReports,  // NEW
  departmentFilter = teamDeptFilter,
  onDepartmentFilterChange = { viewModel.teamDepartmentFilter.value = it },
  onSelectEmployee = { emp -> viewModel.selectedEmployeeForDetail.value = emp }
)
```

## Features

### Real-Time Aggregation
- All metrics compute in real-time from today's EOD submissions
- Conversion rate calculated as: (conversions / calls) × 100
- Country and status breakdowns auto-generated from submission data

### Visual Design
- Material 3 design with rounded cards
- Color-coded metrics based on data type
- Emoji icons for each team type (📞 Telecalling, 🎓 Counsellor, 🏫 Marketing, 📝 Simple)
- Submission rate badge shows X/Y format

### Submission Details
- Each report shows list of individual submissions
- Employee name, summary, and submission time
- Truncated form data for at-a-glance view

## Testing Scenarios

### 1. Telecalling Team
- Submit EOD with: 10 calls, 5 follow-ups, 2 conversions
- Team Report should show:
  - Total Calls: 10
  - Follow-ups: 5
  - Conversions: 2
  - Conversion Rate: 20.0%

### 2. Counsellor Team
- Submit EOD with 3 students (USA, Canada, UK)
- Team Report should show:
  - Total Students: 3
  - By Country: USA (1), Canada (1), UK (1)

### 3. Marketing Team
- Submit EOD with 2 college visits
- Team Report should show:
  - College Visits: 2
  - Unique colleges: 2

### 4. Multiple Submissions
- 3 Telecalling employees submit
- Team totals should aggregate:
  - Employee A: 10 calls, 2 conversions
  - Employee B: 8 calls, 1 conversion
  - Employee C: 5 calls, 1 conversion
  - **Total: 23 calls, 4 conversions, 17.4% rate**

## Error Handling

- Graceful JSON parsing with try-catch
- Returns null metrics if parsing fails
- Empty state shows when no reports available
- Handles missing customFormData gracefully

## Performance

- Efficient Flow-based computation
- Only processes teams with custom forms
- Lazy loading with LazyColumn
- State flows cached with WhileSubscribed(5000)

## Integration Points

✅ **Completed:**
- Form routing in DepartmentRosterScreen
- Custom EOD submission flow
- JSON serialization/deserialization
- Team Reports UI with aggregations

🔄 **Remaining:**
- Company selector dropdown in Admin Dashboard
- Firebase sync for new schema fields
- History screen display of custom forms
- End-to-end testing

## Code Stats

- **New Lines:** ~450 lines
- **Files Modified:** 4 (Repository, ViewModel, AnalyticsScreen, MainActivity)
- **New Composables:** 5 (TeamReportCard + 4 metrics functions)
- **Data Classes:** 3 (TeamReportSummary, CustomTeamMetrics, CustomFormSubmission)

## Next Steps

1. Test form submission → Team Reports flow
2. Add company filter dropdown
3. Verify JSON parsing with real data
4. Add date range selector for historical reports
5. Export team reports to CSV/Excel
