package com.netanel.clockit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.netanel.clockit.data.ShiftRepository
import com.netanel.clockit.ui.month.AddShiftDialog
import com.netanel.clockit.ui.month.MonthlyScreen
import com.netanel.clockit.ui.month.MonthlyViewModel
import com.netanel.clockit.ui.theme.ClockItTheme

import java.time.LocalDate
import java.time.YearMonth

class MainActivity : ComponentActivity() {
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

                Scaffold(
                    topBar = { TopAppBar(title = { Text("ClockIt – חישוב חודשי") }) }
                ) { padding ->
                    Surface(modifier = Modifier.padding(padding)) {
                        MonthlyScreen(
                            vm = monthlyVm,
                            onAddShift = { date ->
                                initialDate = date
                                showAdd = true
                            }
                        )
                        if (showAdd) {
                            AddShiftDialog(
                                initialDate = initialDate,
                                defaultHourly =  monthlyVm.profile.value.hourlyRate,
                                onDismiss = { showAdd = false },
                                onSave = { shift ->
                                    showAdd = false
                                    monthlyVm.addShift(shift)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
