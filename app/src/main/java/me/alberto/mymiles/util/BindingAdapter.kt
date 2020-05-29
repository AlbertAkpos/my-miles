package me.alberto.mymiles.util

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import me.alberto.mymiles.R
import me.alberto.mymiles.database.Miles
import me.alberto.mymiles.screens.home.RecentDetailsAdapter

@BindingAdapter("app:displayText")
fun setButtonDisplayText(button: MaterialButton, start: Boolean?) {

    if (start == null || start == false) {
        button.text = button.context.getString(R.string.start)
        return
    }

    button.text = button.context.getString(R.string.stop)
}

@BindingAdapter("app:recentData")
fun setRecyclerView(recyclerView: RecyclerView, data: List<Miles>?) {
    val adapter = recyclerView.adapter as RecentDetailsAdapter
    adapter.submitList(data?.reversed())
}