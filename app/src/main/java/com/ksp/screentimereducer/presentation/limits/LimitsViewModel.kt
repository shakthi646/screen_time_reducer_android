package com.ksp.screentimereducer.presentation.limits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ksp.screentimereducer.domain.model.AppRule
import com.ksp.screentimereducer.domain.model.InstalledApp
import com.ksp.screentimereducer.domain.usecase.GetInstalledAppsUseCase
import com.ksp.screentimereducer.domain.usecase.ObserveAppRulesUseCase
import com.ksp.screentimereducer.domain.usecase.UpsertAppRuleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LimitsUiState(
    val rules: List<AppRule> = emptyList(),
    val pickerOpen: Boolean = false,
    val installed: List<InstalledApp> = emptyList(),
)

@HiltViewModel
class LimitsViewModel @Inject constructor(
    observeRules: ObserveAppRulesUseCase,
    private val getInstalled: GetInstalledAppsUseCase,
    private val upsert: UpsertAppRuleUseCase,
) : ViewModel() {

    private val ui = MutableStateFlow(LimitsUiState())

    val state: StateFlow<LimitsUiState> = run {
        viewModelScope.launch {
            observeRules().collect { rules ->
                ui.update { it.copy(rules = rules) }
            }
        }
        ui.asStateFlow()
    }

    fun openPicker() {
        viewModelScope.launch {
            val apps = getInstalled()
            ui.update { it.copy(pickerOpen = true, installed = apps) }
        }
    }

    fun closePicker() = ui.update { it.copy(pickerOpen = false) }

    fun addApp(pkg: String) {
        viewModelScope.launch {
            upsert(
                AppRule(
                    packageName = pkg,
                    delayEnabled = true,
                    delaySeconds = 5,
                    dailyLimitMs = 0L,
                )
            )
            closePicker()
        }
    }

    fun setLimitMinutes(pkg: String, minutes: Int) {
        viewModelScope.launch {
            val existing = ui.value.rules.find { it.packageName == pkg } ?: return@launch
            upsert(existing.copy(dailyLimitMs = minutes * 60_000L))
        }
    }

    fun toggleDelay(pkg: String, enabled: Boolean) {
        viewModelScope.launch {
            val existing = ui.value.rules.find { it.packageName == pkg } ?: return@launch
            upsert(existing.copy(delayEnabled = enabled))
        }
    }

    fun setDelaySeconds(pkg: String, seconds: Int) {
        viewModelScope.launch {
            val existing = ui.value.rules.find { it.packageName == pkg } ?: return@launch
            upsert(existing.copy(delaySeconds = seconds.coerceIn(3, 30)))
        }
    }

    /**
     * Save delay seconds and daily limit in a single upsert. The edit dialog
     * uses this instead of calling [setDelaySeconds] + [setLimitMinutes]
     * back-to-back, which would race: each call would read the still-stale
     * `ui.value.rules` and overwrite the other's field.
     */
    fun setDelayAndLimit(pkg: String, seconds: Int, minutes: Int) {
        viewModelScope.launch {
            val existing = ui.value.rules.find { it.packageName == pkg } ?: return@launch
            upsert(
                existing.copy(
                    delaySeconds = seconds.coerceIn(3, 30),
                    dailyLimitMs = minutes * 60_000L,
                )
            )
        }
    }
}

