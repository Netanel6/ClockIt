package com.netanel.clockit

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Menu

import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.content.FileProvider
import com.netanel.clockit.data.ShiftRepository
import com.netanel.clockit.model.Shift
import com.netanel.clockit.ui.month.AddShiftDialog
import com.netanel.clockit.ui.month.MonthlyScreen
import com.netanel.clockit.ui.month.MonthlyViewModel
import com.netanel.clockit.ui.theme.ClockItTheme
import com.netanel.clockit.utils.PdfExporter
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState

class MainActivity : ComponentActivity() {
    companion object {
        internal const val TAG = "MainActivity"
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDI.provideDb(this)
        val repo: ShiftRepository = AppDI.provideRepo(db)

        setContent {
            ClockItTheme {
                val monthlyVm: MonthlyViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return MonthlyViewModel(repo, initialMonth = YearMonth.now()) as T
                    }
                })

                var showAdd by remember { mutableStateOf(false) }
                var initialDate by remember { mutableStateOf(LocalDate.now()) }
                var editingShift by remember { mutableStateOf<Shift?>(null) }
                var isExporting by remember { mutableStateOf(false) }
                var menuExpanded by remember { mutableStateOf(false) }

                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                val context = LocalContext.current
                val pdfExporter = remember(context) { PdfExporter(context) }

                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = { Text("ClockIt – חישוב חודשי") },
                            actions = {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(
                                        Icons.Filled.MoreVert,
                                        contentDescription = stringResource(R.string.top_app_bar_more_actions)
                                    )
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.export_to_pdf)) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Outlined.Menu,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            scope.launch {
                                                isExporting = true
                                                val message = try {
                                                    val state = monthlyVm.uiState.value
                                                    val file = pdfExporter.exportMonthlyReport(
                                                        month = state.month,
                                                        shifts = state.shifts,
                                                        summary = state.summary
                                                    )
                                                    if (context.sharePdf(file)) {
                                                        context.getString(R.string.export_success, file.name)
                                                    } else {
                                                        context.getString(R.string.export_error)
                                                    }
                                                } catch (e: Exception) {
                                                    Log.e(TAG, "Failed to export PDF", e)
                                                    context.getString(R.string.export_error)
                                                }
                                                isExporting = false
                                                snackbarHostState.showSnackbar(message)
                                            }
                                        }
                                    )
                                }
                            }
                        )
                    }
                ) { padding ->
                    Surface(modifier = Modifier.padding(padding)) {
                        MonthlyScreen(
                            vm = monthlyVm,
                            onAddShift = { date ->
                                editingShift = null
                                initialDate = date
                                showAdd = true
                            },
                            onEditShift = { shift ->
                                editingShift = shift
                                initialDate = shift.date
                                showAdd = true
                            }
                        )
                        if (showAdd) {
                            AddShiftDialog(
                                initialDate = initialDate,
                                defaultHourly =  monthlyVm.profile.collectAsState().value.hourlyRate,
                                existingShift = editingShift,
                                onDismiss = {
                                    showAdd = false
                                    editingShift = null
                                },
                                onSave = { shift ->
                                    val isEditing = editingShift != null
                                    showAdd = false
                                    editingShift = null
                                    if (isEditing) {
                                        monthlyVm.updateShift(shift)
                                    } else {
                                        monthlyVm.addShift(shift)
                                    }
                                }
                            )
                        }
                        if (isExporting) {
                            ExportInProgressDialog()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportInProgressDialog() {
    Dialog(onDismissRequest = {}) {
        Surface(
            tonalElevation = AlertDialogDefaults.TonalElevation,
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(36.dp))
                Text(text = stringResource(R.string.export_progress_message))
            }
        }
    }
}

private fun Context.sharePdf(file: File): Boolean {
    val authority = "$packageName.fileprovider"
    val uri = FileProvider.getUriForFile(this, authority, file)
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        clipData = ClipData.newUri(contentResolver, file.name, uri)
    }

    val resolved = packageManager.queryIntentActivities(sendIntent, PackageManager.MATCH_DEFAULT_ONLY)
    if (resolved.isEmpty()) {
        return false
    }

    val chooserTitle = getString(R.string.export_share_title)
    val chooser = Intent.createChooser(sendIntent, chooserTitle)
    if (this !is Activity) {
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    return try {
        startActivity(chooser)
        true
    } catch (e: ActivityNotFoundException) {
        Log.e(MainActivity.TAG, "No activity found to handle share intent", e)
        false
    }
}
