# Academic Overseas (AO) Sign-In Screen

## Overview
New role-based sign-in screen for Academic Overseas employees, matching the provided design mockup with a modern dark theme UI.

## Features

### 1. Tab Navigation
- **Three tabs**: Employee | Admin | AO
- AO tab highlighted in indigo/purple when selected
- Seamless switching between login modes

### 2. Role Selection
Three role options for Academic Overseas staff:
- **Marketing** (with megaphone icon)
- **Counsellor** (with chat icon)
- **Tele Caller** (with phone icon)

Roles are displayed in a 2+1 grid:
```
┌────────────┬────────────┐
│ Marketing  │ Counsellor │
└────────────┴────────────┘
┌────────────────────────┐
│     Tele Caller        │
└────────────────────────┘
```

### 3. Visual Design
**Dark Theme Colors:**
- Background: `#0A0A0A` (near black)
- Cards: `#1A1A1A` (dark gray)
- Primary: `#6366F1` (Indigo-500)
- Text Primary: White
- Text Secondary: `#B0B0B0` (light gray)

**Selected Role State:**
- Background: Soft indigo with 15% opacity
- Border: 2dp indigo border
- Icon & Text: Indigo color
- Font: SemiBold weight

**UI Elements:**
- Building icon in rounded purple badge (80x80dp, 20dp corner radius)
- "WorkCore" title in 32sp bold
- "Academic Overseas sign in" subtitle in 16sp
- Role buttons with icons and labels
- Password field with visibility toggle
- Full-width sign-in button

### 4. Authentication
**Passcode Field:**
- Lock icon on left
- Placeholder: "Enter passcode"
- Eye icon toggle for show/hide password
- Dark themed with subtle borders

**Helper Text:**
- "One login per role · Dashboard opens as roster"
- "Forgot passcode?" link in indigo

**Sign-in Button:**
- Dynamic text: "Sign in to [Selected Role]"
- Full width, 56dp height
- Indigo background (#6366F1)
- White text, semi-bold
- 12dp corner radius

**Footer:**
- "You'll see all [Role] members"
- Shows what data will be visible after login

## Implementation Details

### File Structure
```
app/src/main/java/com/example/ui/screens/
├── AcademicOverseasSignInScreen.kt (NEW)
└── LoginScreen.kt (MODIFIED)
```

### Components Created

#### 1. AcademicOverseasSignInScreen (@Composable)
Main screen component with role selection and authentication.

**Parameters:**
- `onNavigateToRoleDashboard: (role: String) -> Unit` - Callback when user signs in
- `onNavigateBack: () -> Unit` - Callback to return to main login

#### 2. TabButton (@Composable - Private)
Reusable tab button for Employee/Admin/AO selector.

**Parameters:**
- `text: String` - Tab label
- `isSelected: Boolean` - Selection state
- `onClick: () -> Unit` - Click handler
- `backgroundColor, selectedColor, textColor: Color` - Theme colors

#### 3. RoleButton (@Composable - Private)
Role selection button with icon and label.

**Parameters:**
- `role: String` - Role name (Marketing, Counsellor, Tele Caller)
- `icon: ImageVector` - Material icon
- `isSelected: Boolean` - Selection state
- `onClick: () -> Unit` - Click handler
- `modifier: Modifier` - Layout modifier
- `backgroundColor, selectedColor, textColor: Color` - Theme colors

### Integration with LoginScreen

**Modified LoginScreen.kt:**
1. Added `showAOScreen` state variable
2. Updated tab switcher to include AO option
3. Conditional rendering:
   - When `showAOScreen = true`: Show AcademicOverseasSignInScreen
   - When `showAOScreen = false`: Show normal Employee/Admin login
4. Navigation callback to return from AO screen

**State Management:**
```kotlin
var showAOScreen by remember { mutableStateOf(false) }
```

**Tab Logic:**
```kotlin
Triple(Role.EMPLOYEE, "AO", true) // isAO flag
if (isAO) {
  showAOScreen = true
} else {
  showAOScreen = false
  selectedRole = role
}
```

## User Flow

### Sign-In Process
1. App starts → Shows LoginScreen with Employee/Admin/AO tabs
2. User taps **AO** tab → `showAOScreen = true`
3. AcademicOverseasSignInScreen appears
4. User selects role (Marketing/Counsellor/Tele Caller)
5. User enters passcode
6. User taps "Sign in to [Role]"
7. System validates passcode
8. On success: Navigate to role-specific dashboard
9. On error: Show error message

### Navigation Back
- User taps Employee or Admin tab
- `showAOScreen = false`
- Original login screen appears

## Future Enhancements

### To Be Implemented
1. **Backend Integration:**
   - Validate role passcodes against database
   - Create AO-specific AuthSession
   - Store role selection in session

2. **Role-Specific Dashboards:**
   - Marketing Dashboard: View all marketing team members and their EODs
   - Counsellor Dashboard: Student counselling data and reports
   - Tele Caller Dashboard: Call logs and follow-ups

3. **Passcode Management:**
   - Forgot passcode flow
   - Reset passcode functionality
   - Admin-managed role passcodes

4. **Data Filtering:**
   - Show only relevant team members per role
   - Role-based permissions for data access
   - Custom EOD forms per role (already implemented)

5. **Analytics:**
   - Track role-based login events
   - Monitor role-specific activity
   - Generate role performance reports

## Icons Used

| Role | Material Icon | Code |
|------|---------------|------|
| Marketing | Campaign | `Icons.Default.Campaign` |
| Counsellor | Chat | `Icons.Default.Chat` |
| Tele Caller | Phone | `Icons.Default.Phone` |
| Building (Logo) | Business | `Icons.Default.Business` |
| Lock (Passcode) | Lock | `Icons.Default.Lock` |
| Eye (Show) | Visibility | `Icons.Default.Visibility` |
| Eye Off (Hide) | VisibilityOff | `Icons.Default.VisibilityOff` |

## Design Fidelity

### Matches Provided Mockup:
✅ Dark theme with near-black background  
✅ Three-tab navigation (Employee/Admin/AO)  
✅ Purple/indigo branding color  
✅ Building icon in rounded badge  
✅ "WorkCore" title  
✅ "Academic Overseas sign in" subtitle  
✅ Role selection with icons  
✅ Selected role highlighting  
✅ Passcode field with lock and eye icons  
✅ Helper text and forgot password link  
✅ Full-width sign-in button  
✅ Footer caption  

## Testing Checklist

### Visual Testing
- [ ] AO tab appears in login screen
- [ ] Clicking AO shows Academic Overseas screen
- [ ] Clicking Employee/Admin returns to original login
- [ ] Building icon displays correctly
- [ ] All three roles display with correct icons
- [ ] Role selection highlights properly
- [ ] Passcode field shows/hides password
- [ ] Button text updates with selected role

### Interaction Testing
- [ ] Tab switching works smoothly
- [ ] Role buttons are tappable
- [ ] Passcode input accepts text
- [ ] Visibility toggle works
- [ ] Forgot passcode link tappable
- [ ] Sign-in button tappable
- [ ] Error messages display correctly

### Integration Testing
- [ ] Navigation callbacks work
- [ ] State persists during interaction
- [ ] Error states handled gracefully
- [ ] Toast messages appear correctly

## Files Modified

1. **NEW:** `app/src/main/java/com/example/ui/screens/AcademicOverseasSignInScreen.kt` (231 lines)
2. **MODIFIED:** `app/src/main/java/com/example/ui/screens/LoginScreen.kt`
   - Added `showAOScreen` state
   - Updated tab switcher (3 tabs instead of 2)
   - Added conditional rendering for AO screen
3. **MODIFIED:** `app/src/main/java/com/example/MainActivity.kt`
   - Added import for AcademicOverseasSignInScreen

## Screenshots Reference
Original mockup shows the exact design that was implemented with:
- Dark theme
- Role-based selection
- Modern Material Design 3 components
- Intuitive navigation
- Clear visual hierarchy

---
*Created: October 9, 2026*  
*Status: Ready for testing*  
*Next: Backend integration for role authentication*
