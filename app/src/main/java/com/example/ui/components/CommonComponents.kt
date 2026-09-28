package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StatusErrorBg
import com.example.ui.theme.StatusErrorText
import com.example.ui.theme.StatusInfoBg
import com.example.ui.theme.StatusInfoText
import com.example.ui.theme.StatusPendingBg
import com.example.ui.theme.StatusPendingText
import com.example.ui.theme.StatusSuccessBg
import com.example.ui.theme.StatusSuccessText
import com.example.ui.theme.StatusWarningBg
import com.example.ui.theme.StatusWarningText
import com.example.ui.theme.WorkOutline
import com.example.ui.theme.WorkOutlineVariant
import com.example.ui.theme.WorkPrimary
import com.example.ui.theme.WorkPrimaryContainer
import com.example.ui.theme.WorkSurface

@Composable
fun MetricCard(
  title: String,
  count: String,
  subtitle: String,
  backgroundColor: Color,
  textColor: Color,
  borderColor: Color,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null
) {
  Box(
    modifier = modifier
      .height(112.dp)
      .clip(RoundedCornerShape(24.dp))
      .background(backgroundColor)
      .border(1.dp, borderColor, RoundedCornerShape(24.dp))
      .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
      .padding(14.dp)
  ) {
    Column(
      modifier = Modifier.matchParentSize(),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = title.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.8.sp,
        color = textColor.copy(alpha = 0.85f),
        maxLines = 1
      )
      Text(
        text = count,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = textColor
      )
      Text(
        text = subtitle,
        fontSize = 10.sp,
        color = Color(0xFF49454F),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
fun StatusBadge(
  status: String,
  modifier: Modifier = Modifier
) {
  val (bg, fg) = when (status.lowercase()) {
    "completed" -> StatusSuccessBg to StatusSuccessText
    "in progress" -> StatusInfoBg to StatusInfoText
    "blocked" -> StatusWarningBg to StatusWarningText
    "no work" -> Color(0xFFE7E0EC) to Color(0xFF49454F)
    "on leave" -> Color(0xFFFFD8E4) to Color(0xFF633B48)
    "pending" -> StatusErrorBg to StatusErrorText
    else -> Color(0xFFE7E0EC) to Color(0xFF49454F)
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(6.dp))
      .background(bg)
      .padding(horizontal = 8.dp, vertical = 3.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = status,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = fg
    )
  }
}

@Composable
fun IndicatorBadge(
  indicator: String,
  modifier: Modifier = Modifier
) {
  val (bg, fg) = when (indicator.lowercase()) {
    "strong" -> Color(0xFFC8E6C9) to Color(0xFF1B5E20)
    "good" -> Color(0xFFD1E1FF) to Color(0xFF0D47A1)
    "needs attention" -> Color(0xFFFFE0B2) to Color(0xFFE65100)
    "inactive" -> Color(0xFFFFCDD2) to Color(0xFFB71C1C)
    else -> Color(0xFFE7E0EC) to Color(0xFF49454F)
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(bg)
      .padding(horizontal = 8.dp, vertical = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = indicator,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = fg
    )
  }
}

@Composable
fun UserAvatar(
  name: String,
  modifier: Modifier = Modifier,
  size: Int = 36,
  backgroundColor: Color = WorkPrimary
) {
  val initials = name.split(" ")
    .mapNotNull { it.firstOrNull()?.toString() }
    .take(2)
    .joinToString("")
    .uppercase()

  Box(
    modifier = modifier
      .size(size.dp)
      .clip(CircleShape)
      .background(backgroundColor),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = if (initials.isNotEmpty()) initials else "U",
      color = Color.White,
      fontSize = (size * 0.38).sp,
      fontWeight = FontWeight.Bold
    )
  }
}

@Composable
fun UserAvatar(
  name: String,
  size: androidx.compose.ui.unit.Dp,
  modifier: Modifier = Modifier,
  backgroundColor: Color = WorkPrimary
) {
  UserAvatar(
    name = name,
    modifier = modifier,
    size = size.value.toInt().coerceAtLeast(16),
    backgroundColor = backgroundColor
  )
}

/**
 * Section 4 Table Card Component:
 * | Employee | Today's Work | Hours | Progress | Status | EOD (Submitted vs Pending) |
 */
@Composable
fun EmployeeWorkMonitorCard(
  employeeName: String,
  department: String,
  todayWork: String,
  hours: Double,
  progress: Int,
  status: String,
  isEodSubmitted: Boolean,
  isOnTime: Boolean,
  attentionReason: String?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .border(1.dp, if (attentionReason != null) Color(0xFFF2B8B5) else WorkOutlineVariant, RoundedCornerShape(16.dp))
      .clickable { onClick() },
    color = if (attentionReason != null) Color(0xFFFFF8F8) else Color.White
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      // Row 1: Employee Header & EOD Submission Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          UserAvatar(
            name = employeeName,
            size = 36,
            backgroundColor = if (isEodSubmitted) WorkPrimary else Color(0xFFB3261E)
          )
          Column {
            Text(
              text = employeeName,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1D1B20)
            )
            Text(
              text = department,
              fontSize = 11.sp,
              color = Color(0xFF49454F)
            )
          }
        }

        // EOD Status Badge
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isEodSubmitted) StatusSuccessBg else StatusErrorBg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = if (isEodSubmitted) {
              if (isOnTime) "Submitted" else "Late Submitted"
            } else "Pending",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isEodSubmitted) StatusSuccessText else StatusErrorText
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Row 2: Today's Work
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "TODAY'S WORK",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF79747E),
            letterSpacing = 0.5.sp
          )
          Text(
            text = todayWork,
            fontSize = 12.sp,
            color = Color(0xFF1D1B20),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Row 3: Hours | Progress | Work Status
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Hours
          Column {
            Text(
              text = "HOURS",
              fontSize = 9.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF79747E)
            )
            Text(
              text = if (hours > 0) "${hours}h" else "0h",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF21005D)
            )
          }

          // Progress
          Column {
            Text(
              text = "PROGRESS",
              fontSize = 9.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF79747E)
            )
            Text(
              text = "$progress%",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = if (progress >= 80) Color(0xFF1D6C2F) else Color(0xFF21005D)
            )
          }
        }

        // Status Badge
        StatusBadge(status = status)
      }

      // Attention Alert (if present)
      if (attentionReason != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFFFEBEE))
            .padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = Color(0xFFB3261E),
            modifier = Modifier.size(13.dp)
          )
          Text(
            text = attentionReason,
            fontSize = 11.sp,
            color = Color(0xFFB3261E),
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    }
  }
}

/**
 * Bar Chart Component for Hours and Progress Trends
 */
@Composable
fun SimpleBarChart(
  dataPoints: List<Pair<String, Double>>, // Label (e.g. "09-01") to Value (e.g. 7.5 or 80)
  maxValue: Double,
  barColor: Color = WorkPrimary,
  unit: String = "",
  modifier: Modifier = Modifier
) {
  if (dataPoints.isEmpty()) {
    Box(
      modifier = modifier
        .fillMaxWidth()
        .height(140.dp),
      contentAlignment = Alignment.Center
    ) {
      Text("No data available for chart", fontSize = 12.sp, color = Color(0xFF79747E))
    }
    return
  }

  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(130.dp)
        .padding(horizontal = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom
    ) {
      val safeMax = if (maxValue > 0) maxValue else 10.0
      dataPoints.takeLast(7).forEach { (label, value) ->
        val barFraction = (value / safeMax).toFloat().coerceIn(0.05f, 1f)

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.weight(1f)
        ) {
          Text(
            text = if (value % 1.0 == 0.0) "${value.toInt()}$unit" else "${String.format("%.1f", value)}$unit",
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF49454F)
          )
          Spacer(modifier = Modifier.height(3.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth(0.55f)
              .height((barFraction * 80).dp)
              .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
              .background(barColor)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = label.takeLast(5),
            fontSize = 9.sp,
            color = Color(0xFF79747E),
            maxLines = 1
          )
        }
      }
    }
  }
}

/**
 * Horizontal Status Distribution Segmented Bar
 */
@Composable
fun SegmentedStatusBar(
  completed: Int,
  inProgress: Int,
  blocked: Int,
  noWorkOrLeave: Int,
  modifier: Modifier = Modifier
) {
  val total = (completed + inProgress + blocked + noWorkOrLeave).coerceAtLeast(1)

  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(12.dp)
        .clip(RoundedCornerShape(6.dp))
        .background(Color(0xFFE7E0EC))
    ) {
      if (completed > 0) {
        Box(
          modifier = Modifier
            .weight(completed.toFloat() / total)
            .fillMaxHeight()
            .background(Color(0xFF1D6C2F))
        )
      }
      if (inProgress > 0) {
        Box(
          modifier = Modifier
            .weight(inProgress.toFloat() / total)
            .fillMaxHeight()
            .background(Color(0xFF0D47A1))
        )
      }
      if (blocked > 0) {
        Box(
          modifier = Modifier
            .weight(blocked.toFloat() / total)
            .fillMaxHeight()
            .background(Color(0xFFE65100))
        )
      }
      if (noWorkOrLeave > 0) {
        Box(
          modifier = Modifier
            .weight(noWorkOrLeave.toFloat() / total)
            .fillMaxHeight()
            .background(Color(0xFF79747E))
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text("Completed: $completed", fontSize = 10.sp, color = Color(0xFF1D6C2F), fontWeight = FontWeight.SemiBold)
      Text("In Progress: $inProgress", fontSize = 10.sp, color = Color(0xFF0D47A1), fontWeight = FontWeight.SemiBold)
      Text("Blocked: $blocked", fontSize = 10.sp, color = Color(0xFFE65100), fontWeight = FontWeight.SemiBold)
      Text("Other: $noWorkOrLeave", fontSize = 10.sp, color = Color(0xFF79747E), fontWeight = FontWeight.SemiBold)
    }
  }
}
