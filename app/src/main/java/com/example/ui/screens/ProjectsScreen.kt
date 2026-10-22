package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.ProjectBudgetItemEntity
import com.example.data.local.entities.ProjectEntity
import com.example.data.local.entities.ProjectMilestoneEntity
import com.example.ui.components.AddBudgetItemDialog
import com.example.ui.components.AddProjectDialog
import com.example.ui.components.EditBudgetItemDialog
import com.example.ui.components.EditProjectDialog
import com.example.ui.components.ProgressBarWithTarget
import com.example.util.CurrencyFormatter

@Composable
fun ProjectsScreen(
    projects: List<ProjectEntity>,
    budgetItems: List<ProjectBudgetItemEntity>,
    milestones: List<ProjectMilestoneEntity>,
    onSaveProject: (ProjectEntity, List<Pair<String, Double>>) -> Unit,
    onUpdateProject: (ProjectEntity) -> Unit,
    onDeleteProject: (Long) -> Unit,
    onUpdateBudgetItem: (ProjectBudgetItemEntity) -> Unit,
    onDeleteBudgetItem: (Long) -> Unit,
    onAddBudgetItem: (ProjectBudgetItemEntity) -> Unit,
    onToggleMilestone: (ProjectMilestoneEntity) -> Unit
) {
    var showAddProjectDialog by remember { mutableStateOf(false) }
    var expandedProjectId by remember { mutableStateOf(projects.firstOrNull()?.id) }

    var editingProject by remember { mutableStateOf<ProjectEntity?>(null) }
    var editingBudgetItem by remember { mutableStateOf<ProjectBudgetItemEntity?>(null) }
    var addingBudgetItemForProjectId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddProjectDialog = true },
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
                modifier = Modifier.testTag("fab_add_project")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Project")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("projects_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Business & Personal Financial Projects", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Track real-world investments such as commercial poultry, solar power backup, retail stores, or property construction with granular budget allocations and milestone tracking. Tap any card or allocation to edit.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            if (projects.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No Active Projects", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Tap + to start tracking poultry, solar installation, agricultural farming, or business ventures.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(projects, key = { it.id }) { project ->
                val itemsForProject = budgetItems.filter { it.projectId == project.id }
                val milestonesForProject = milestones.filter { it.projectId == project.id }
                val totalSpent = itemsForProject.sumOf { it.spentAmount }
                val isExpanded = expandedProjectId == project.id

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { expandedProjectId = if (isExpanded) null else project.id }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Work,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(project.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "${project.category} • ${project.status}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        CurrencyFormatter.formatUsd(project.totalBudget),
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        "Spent: ${CurrencyFormatter.formatUsd(totalSpent)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { editingProject = project },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit Project",
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val progress = if (project.totalBudget > 0) (totalSpent / project.totalBudget).toFloat() else 0f
                        ProgressBarWithTarget(
                            progress = progress,
                            targetLabel = "Budget: ${CurrencyFormatter.formatUsd(project.totalBudget)}",
                            currentLabel = "Spent: ${CurrencyFormatter.formatUsd(totalSpent)} (${String.format("%.1f", progress * 100)}%)",
                            color = MaterialTheme.colorScheme.tertiary
                        )

                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(16.dp))

                            // Budget Item Breakdown Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Budget Category Allocations",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                OutlinedButton(
                                    onClick = { addingBudgetItemForProjectId = project.id },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Item", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            if (itemsForProject.isEmpty()) {
                                Text(
                                    "No budget allocations defined yet.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            itemsForProject.forEach { item ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { editingBudgetItem = item }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(item.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                            val itemPct = if (item.allocatedAmount > 0) (item.spentAmount / item.allocatedAmount) * 100 else 0.0
                                            Text(
                                                "${String.format("%.0f", itemPct)}% utilized",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                "${CurrencyFormatter.formatUsd(item.spentAmount)} / ${CurrencyFormatter.formatUsd(item.allocatedAmount)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit Allocation",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Milestones
                            if (milestonesForProject.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Milestones & Execution Checklist",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                milestonesForProject.forEach { milestone ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onToggleMilestone(milestone) }
                                            .padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = milestone.isCompleted,
                                            onCheckedChange = { onToggleMilestone(milestone) },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = milestone.title,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = if (milestone.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                                                else MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddProjectDialog) {
        AddProjectDialog(
            onDismiss = { showAddProjectDialog = false },
            onSave = { proj, items ->
                onSaveProject(proj, items)
                showAddProjectDialog = false
            }
        )
    }

    editingProject?.let { proj ->
        EditProjectDialog(
            project = proj,
            onDismiss = { editingProject = null },
            onSave = {
                onUpdateProject(it)
                editingProject = null
            },
            onDelete = {
                onDeleteProject(it)
                editingProject = null
            }
        )
    }

    editingBudgetItem?.let { item ->
        EditBudgetItemDialog(
            item = item,
            onDismiss = { editingBudgetItem = null },
            onSave = {
                onUpdateBudgetItem(it)
                editingBudgetItem = null
            },
            onDelete = {
                onDeleteBudgetItem(it)
                editingBudgetItem = null
            }
        )
    }

    addingBudgetItemForProjectId?.let { projId ->
        AddBudgetItemDialog(
            projectId = projId,
            onDismiss = { addingBudgetItemForProjectId = null },
            onSave = {
                onAddBudgetItem(it)
                addingBudgetItemForProjectId = null
            }
        )
    }
}
