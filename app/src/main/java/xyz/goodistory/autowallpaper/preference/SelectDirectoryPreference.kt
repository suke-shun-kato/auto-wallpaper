package xyz.goodistory.autowallpaper.preference

import android.content.Context
import android.util.AttributeSet
import android.widget.Toast
import androidx.preference.DialogPreference

class SelectDirectoryPreference : DialogPreference {

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context): super(context)

    override fun onClick() {
        super.onClick()

        Toast.makeText(context, "sssss", Toast.LENGTH_LONG).show()
    }
}