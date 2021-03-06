/*
 * Copyright (C) 2020  AniTrend
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.wax911.trakt.core.koin.helper

import co.anitrend.arch.extension.lifecycle.SupportLifecycle
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import timber.log.Timber

/**
 * Module loader helper for dynamic features
 */
class DynamicFeatureModuleHelper(
    private val modules: List<Module>,
    private val unloadOnDestroy: Boolean = false
) : SupportLifecycle {

    override val moduleTag = DynamicFeatureModuleHelper::class.java.simpleName

    /**
     * Triggered when the lifecycleOwner reaches it's onCreate state
     *
     * @see [androidx.lifecycle.LifecycleOwner]
     */
    override fun onCreate() {
        super.onCreate()
        Timber.tag(moduleTag).v(
            "Loading ${modules.size} feature modules"
        )
        loadModules()
    }

    /**
     * Triggered when the lifecycleOwner reaches it's onDestroy state
     *
     * @see [androidx.lifecycle.LifecycleOwner]
     */
    override fun onDestroy() {
        super.onDestroy()
        if (unloadOnDestroy) {
            Timber.tag(moduleTag).v(
                "Unloading ${modules.size} feature modules"
            )
            unloadModules()
        }
    }

    companion object {
        private fun DynamicFeatureModuleHelper.loadModules() = loadKoinModules(modules)
        private fun DynamicFeatureModuleHelper.unloadModules() = unloadKoinModules(modules)
    }
}