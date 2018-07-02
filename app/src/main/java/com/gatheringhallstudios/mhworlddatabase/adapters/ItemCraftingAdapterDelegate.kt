package com.gatheringhallstudios.mhworlddatabase.adapters

import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gatheringhallstudios.mhworlddatabase.R
import com.gatheringhallstudios.mhworlddatabase.adapters.common.SimpleListDelegate
import com.gatheringhallstudios.mhworlddatabase.data.views.ItemCombinationView
import com.gatheringhallstudios.mhworlddatabase.getRouter
import kotlinx.android.synthetic.main.listitem_item_crafting.view.*

/**
 * Defines an adapter delegate for a list of item combinations
 */
class ItemCraftingAdapterDelegate : SimpleListDelegate<ItemCombinationView, View>() {
    override fun getDataClass() = ItemCombinationView::class

    override fun onCreateView(parent: ViewGroup): View {
        return LayoutInflater.from(parent.context).inflate(R.layout.listitem_item_crafting, parent, false)
    }

    override fun bindView(view: View, data: ItemCombinationView) {
        view.result_icon.setImageDrawable(ContextCompat.getDrawable(view.context, R.drawable.ic_question_mark))
        view.result_name.text = data.result.name

        view.item1_icon.setImageDrawable(ContextCompat.getDrawable(view.context, R.drawable.ic_question_mark))
        view.item1_name.text = data.first.name

        view.item2_view.visibility = View.GONE
        if (data.second != null) {
            view.item2_view.visibility = View.VISIBLE
            view.item2_icon.setImageDrawable(ContextCompat.getDrawable(view.context, R.drawable.ic_question_mark))
            view.item2_name.text = data.second.name
        }

        view.yield_label.text = view.resources.getString(R.string.item_crafting_yield, data.quantity)

        view.setOnClickListener {
            view.getRouter().navigateItemDetail(data.result.id)
        }

        view.item1_view.setOnClickListener {
            view.getRouter().navigateItemDetail(data.first.id)
        }

        if (data.second != null) {
            view.item2_view.setOnClickListener {
                view.getRouter().navigateItemDetail(data.second.id)
            }
        } else {
            view.item2_view.setOnClickListener(null)
        }
    }
}