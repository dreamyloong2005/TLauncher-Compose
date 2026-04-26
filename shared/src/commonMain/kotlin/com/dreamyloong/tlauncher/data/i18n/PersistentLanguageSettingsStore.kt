package com.dreamyloong.tlauncher.data.i18n

import com.dreamyloong.tlauncher.core.i18n.LanguagePreference
import com.dreamyloong.tlauncher.core.i18n.LanguageSettingsStore
import com.dreamyloong.tlauncher.data.persistence.LauncherStateRepository
import com.dreamyloong.tlauncher.data.persistence.PersistedLanguagePreference

class PersistentLanguageSettingsStore(
    private val stateRepository: LauncherStateRepository,
) : LanguageSettingsStore {
    override fun currentPreference(): LanguagePreference {
        return stateRepository.read().language.toDomain()
    }

    override fun setCurrentPreference(preference: LanguagePreference) {
        stateRepository.update { state ->
            state.copy(language = PersistedLanguagePreference.fromDomain(preference))
        }
    }
}
