package com.udacity.downloadLibsApp.ui.customViews

import com.udacity.downloadLibsApp.R


sealed class ButtonState(var labelResource: Int) {
    object Clicked : ButtonState(R.string.button_loading)
    object Loading : ButtonState(R.string.button_loading)
    object Completed : ButtonState(R.string.button_name)
}
