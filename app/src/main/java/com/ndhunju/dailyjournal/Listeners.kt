package com.ndhunju.dailyjournal

import android.content.Intent
import androidx.annotation.IntDef
import com.ndhunju.dailyjournal.controller.fragment.DatePickerFragment


/**
 * To be used to receive result from [DatePickerFragment]
 */
interface OnDatePickerDialogBtnClickedListener {

    companion object {
        const val BUTTON_POSITIVE = 1
        const val BUTTON_NEUTRAL = 0
        const val BUTTON_NEGATIVE = -1
    }

    //Define the list of accepted constants
    @IntDef(*[BUTTON_POSITIVE, BUTTON_NEUTRAL, BUTTON_NEGATIVE]) //Tell the compiler not to store annotation data in the .class file
    //TODO This way, as opposed to using enum, is it more efficient in terms of memory??
    @Retention(AnnotationRetention.SOURCE) //Declare the ButtonType annotation

    annotation class ButtonType


    /**
     * This a callback which can be invoked in a DialogFragment to pass data back to
     * the calling class mainly Activity and Fragment
     * @param data : data to pass
     * @param whichBtn : button that was clicked
     * @param result : was it a success
     * @param requestCode : request code
     */
    fun onDialogBtnClicked(data: Intent, @ButtonType whichBtn: Int, result: Int, requestCode: Int)
}
