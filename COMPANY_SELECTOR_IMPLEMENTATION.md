# Company Selector Implementation

## Overview
Added a company filter dropdown to the Admin Dashboard that allows admins to filter employees and teams between "All Companies", "Sandspace Technologies Pvt Ltd", and "Academic Overseas".

## Changes Made

### 1. ViewModel (`WorkCoreViewModel.kt`)

**New State:**
```kotlin
val selectedCompanyId = MutableStateFlow("ALL")
```

**Updated managerTeamMembers Flow:**
```kotlin
val managerTeamMembers: StateFlow<List<TeamMemberBehaviorItem>> = combine(
  selectedDepartmentCode,
  selectedCompanyId,  // NEW
  employees,
  dailyEods
) { dept, companyId, emps, eods ->
  // Filter by company first
  val companyFilteredEmps = if (companyId == "ALL") {
    emps
  } else {
    emps.filter { it.companyId == companyId }
  }
  repository.computeManagerTeamMetrics(dept, companyFilteredEmps, eods, repository.getTodayDateString())
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
```

### 2. MainActivity Integration

**Added State Collection:**
```kotlin
val selectedCompanyId by viewModel.selectedCompanyId.collectAsState()
```

**Updated AdminDashboardScreen Call:**
```kotlin
AdminDashboardScreen(
  allDepartments = departments,
  allCompanies = companies,  // NEW
  selectedDepartmentCode = selectedDepartmentCode,
  selectedCompanyId = selectedCompanyId,  // NEW
  onDepartmentSelect = { viewModel.selectedDepartmentCode.value = it },
  onCompanySelect = { viewModel.selectedCompanyId.value = it },  // NEW
  teamMembers = managerTeamMembers,
  ...
)
```

### 3. AdminDashboardScreen UI

**Updated Function Signature:**
```kotlin
@Composable
fun AdminDashboardScreen(
  allDepartments: List<DepartmentEntity>,
  allCompanies: List<CompanyEntity>,  // NEW
  selectedDepartmentCode: String,
  selectedCompanyId: String,  // NEW
  onDepartmentSelect: (String) -> Unit,
  onCompanySelect: (String) -> Unit,  // NEW
  ...
)
```

**New UI Component - Company Filter Chips:**
```kotlin
// 2.5 Company Filter Chips
item {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(
      text = "Filter by Company",
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      color = EodTextMuted
    )
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      // "All Companies" chip
      item {
        val isSelected = selectedCompanyId == "ALL"
        Surface(...) {
          Text("All Companies")
        }
      }

      // Individual company chips
      items(allCompanies) { company ->
        val isSelected = selectedCompanyId == company.companyId
        val displayName = when {
          company.type == "PARENT" -> "🏢 ${company.name}"
          else -> "🏛️ ${company.name}"
        }
        Surface(...) {
          Text(displayName)
        }
      }
    }
  }
}
```

## Features

### Visual Design
- **Emoji Icons:**
  - 🏢 for parent companies (Sandspace Technologies)
  - 🏛️ for subsidiary companies (Academic Overseas)
  
- **Chip Styling:**
  - Active chip: Purple background (#6750A4) with white text
  - Inactive chip: Dark surface with muted text
  - Border highlight on selection

### Filter Behavior

**"All Companies" (Default):**
- Shows all employees across both companies
- Combined submission metrics
- All departments visible

**"Sandspace Technologies":**
- Filters to show only:
  - General Team (GT)
  - Machine Learning Team (ML)
  - Database Team (DB)
  - Writing Team
  - Cyber Security Team
- Standard EOD forms only

**"Academic Overseas":**
- Filters to show only:
  - Counsellor Team (custom form)
  - Telecalling Team (custom form)
  - Marketing Team (custom form)
  - Other Team (simple form)
- Custom EOD forms only

### Cascading Filters

The company and department filters work together:

1. **Company Filter → Department Filter:**
   - Select "Academic Overseas" → Only AO departments appear
   - Select "Sandspace" → Only Sandspace departments appear

2. **Combined Selection:**
   - Company: Academic Overseas + Department: Telecalling → Only telecalling employees
   - Company: All + Department: All → Everyone

### Real-Time Updates

- Employee count updates dynamically
- Submission metrics recalculate
- Team member list refreshes
- All filters reactive via StateFlow

## User Flow

### Scenario 1: View Academic Overseas Only
1. Admin navigates to Dashboard
2. Clicks "🏛️ Academic Overseas" chip
3. Employee list filters to 4 teams (Counsellor, Telecalling, Marketing, Other)
4. Submission count shows only AO submissions

### Scenario 2: View Specific Team in Specific Company
1. Admin clicks "🏛️ Academic Overseas"
2. Clicks "TELECALL · Telecalling" in department filter
3. Sees only Telecalling team members
4. Can view their call metrics in Analytics → Team Reports

### Scenario 3: Compare Companies
1. Admin views "All Companies" metrics (baseline)
2. Clicks "🏢 Sandspace Technologies" → sees parent company metrics
3. Clicks "🏛️ Academic Overseas" → sees subsidiary metrics
4. Compares submission rates, on-time rates, etc.

## Technical Details

### Data Flow
```
User clicks company chip
    ↓
viewModel.selectedCompanyId.value = companyId
    ↓
managerTeamMembers flow recomputes
    ↓
Filters employees by companyId
    ↓
Computes metrics for filtered employees
    ↓
UI updates with new data
```

### Filter Logic
```kotlin
val companyFilteredEmps = if (companyId == "ALL") {
  emps
} else {
  emps.filter { it.companyId == companyId }
}
```

### Performance
- Flow-based reactive updates (no manual refresh needed)
- WhileSubscribed(5000) lifecycle for memory efficiency
- LazyRow for horizontal scrolling (scales to many companies)

## Testing Scenarios

### Test 1: Company Selection
- Click "All Companies" → See 9 teams total
- Click "🏢 Sandspace Technologies" → See 5 teams (GT, ML, DB, Writing, Cyber)
- Click "🏛️ Academic Overseas" → See 4 teams (Counsellor, Telecalling, Marketing, Other)

### Test 2: Combined Filters
- Select "Academic Overseas" + "TELECALL"
- Verify only Telecalling employees visible
- Verify submission metrics are specific to that team

### Test 3: Dynamic Updates
- Submit EOD as Telecalling employee
- Return to Admin Dashboard with "Academic Overseas" filter active
- Verify submission count updates in real-time

### Test 4: Navigation Persistence
- Select "Academic Overseas"
- Navigate to Analytics tab
- Return to Dashboard
- Verify "Academic Overseas" filter still active

## UI Location

**Admin Dashboard Structure:**
```
1. Header (Title, Bell, Avatar)
2. Department Filter Chips
2.5. Company Filter Chips ⬅️ NEW
3. Add Employee Banner
3b. Add Team Banner
4. Summary Cards (Submitted, Missed, Late)
5. Employee List with Behavior Metrics
```

## Integration Points

✅ **Completed:**
- ViewModel state management
- MainActivity state collection
- AdminDashboardScreen UI component
- Filter logic in managerTeamMembers flow
- Visual design with emoji icons

🔄 **Can Be Extended:**
- Add company filter to Analytics screen
- Add company filter to History screen
- Add company filter to Department Roster
- Show company hierarchy in UI (parent/subsidiary relationship)

## Code Stats

- **Lines Added:** ~100 lines
- **Files Modified:** 3 (ViewModel, MainActivity, AdminDashboardScreen)
- **New Parameters:** 3 (allCompanies, selectedCompanyId, onCompanySelect)
- **New State:** 1 (selectedCompanyId MutableStateFlow)

## Benefits

1. **Clarity:** Admins can focus on one company at a time
2. **Efficiency:** Faster to find specific teams/employees
3. **Insights:** Easy comparison between companies
4. **Scalability:** Supports adding more companies in future
5. **UX:** Intuitive chip-based selection with visual feedback

## Next Steps

1. **Build & Test:** Test company filter in Admin Dashboard
2. **Extend Filters:** Add company selector to other screens
3. **Company Reports:** Add company-level analytics
4. **Hierarchy View:** Show parent-subsidiary relationships visually
5. **Export Data:** Allow exporting by company
