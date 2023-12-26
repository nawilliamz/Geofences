/*
 * Copyright (C) 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.treasureHunt

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

/*
 * This class contains the state of the game.  The two important pieces of state are the index
 * of the geofence, which is the geofence that the game thinks is active, and the state of the
 * hint being shown.  If the hint matches the geofence, then the Activity won't update the geofence
 * as it cycles through various activity states.
 *
 * These states are stored in SavedState, which matches the Android lifecycle.  Destroying the
 * associated Activity with the back action will delete all state and reset the game, while
 * the Home action will cause the state to be saved, even if the game is terminated by Android in
 * the background.
 */

//If you need to handle system-initiated process death, you might want to use the SavedStateHandle API as a backup.
//This SavedStateHandle is a key-value map that lets you write and retrieve objects to and from the saved state.
//These values persist after the process is killed by the system and remain available through the SavedStateHandle
//**KeyPoint: Usually, data stored in saved instance state is transient state that depends on user input or
//navigation. Examples of this can be the scroll position of a list, the id of the item the user wants more detail
//about, the in-progress selection of user preferences, or input in text fields.
//Important: the API to use (SavedInstanceState vs. SavedStateHandel) depends on where the state is held and the
//logic it requires. For state that is used in business logic, hold it in a ViewModel and save it using SavedStateHandle.
//For state that is used in UI logic, use the onSaveinstanctState API in the View system or rememberSaveable in Compose.
//Note: State must be simple and lightweight. For complex or large data, you shouls use local persistence (such as
//database or shared preferences.
class GeofenceViewModel(state: SavedStateHandle) : ViewModel() {

    //GeofenceIndex is LiveData that determines which head should be shown oncreen
    private val _geofenceIndex = state.getLiveData(GEOFENCE_INDEX_KEY, -1)
    private val _hintIndex = state.getLiveData(HINT_INDEX_KEY, 0)
    val geofenceIndex: LiveData<Int>
        get() = _geofenceIndex

    //map() is a transformation which is a function that acts on the value of each LiveData within a set of LiveData
    //which yields a new set of LiveData
    val geofenceHintResourceId = geofenceIndex.map {
        val index = geofenceIndex?.value ?: -1
        when {
            index < 0 -> R.string.not_started_hint
            //Returns the hint for the LandmarkData corresponding to the geofenceIndex value as long as game not finished
            index < GeofencingConstants.NUM_LANDMARKS -> GeofencingConstants.LANDMARK_DATA[geofenceIndex.value!!].hint
            else -> R.string.geofence_over
        }
    }

    val geofenceImageResourceId = geofenceIndex.map {
        val index = geofenceIndex.value ?: -1
        when {
            //retreives the same image any time game is still ongoing
            index < GeofencingConstants.NUM_LANDMARKS -> R.drawable.android_map
            else -> R.drawable.android_treasure
        }
    }

    fun updateHint(currentIndex: Int) {
        _hintIndex.value = currentIndex+1
    }

    fun geofenceActivated() {
        _geofenceIndex.value = _hintIndex.value
    }

    //geofence is active as long as the geofenceIndex value is same as hintIndex value. I assume they'd only
    //be different when the game is over
    fun geofenceIsActive() =_geofenceIndex.value == _hintIndex.value
    fun nextGeofenceIndex() = _hintIndex.value ?: 0
}

private const val HINT_INDEX_KEY = "hintIndex"
private const val GEOFENCE_INDEX_KEY = "geofenceIndex"
