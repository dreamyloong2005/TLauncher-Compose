package com.dreamyloong.tlauncher.data.theme

import com.dreamyloong.tlauncher.core.theme.ThemePreference
import com.dreamyloong.tlauncher.core.theme.ThemeSettingsStore
import com.dreamyloong.tlauncher.data.persistence.LauncherStateRepository
import com.dreamyloong.tlauncher.data.persistence.PersistedThemePreference

class PersistentThemeSettingsStore(
    private val stateRepository: LauncherStateRepository,
) : ThemeSettingsStore {
    override fun currentPreference(): ThemePreference {
        return stateRepository.read().theme.toDomain()
    }

    override fun setCurrentPreference(preference: ThemePreference) {
        stateRepository.update { state ->
            state.copy(theme = PersistedThemePreference.fromDomain(preference))
        }
    }
}
