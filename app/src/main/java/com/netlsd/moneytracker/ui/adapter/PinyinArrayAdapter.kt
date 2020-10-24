package com.netlsd.moneytracker.ui.adapter

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import com.netlsd.moneytracker.model.PeopleName
import com.pinyinsearch.util.QwertyUtil

// todo trigger first
class PinyinArrayAdapter(context: Context, textViewResourceId: Int, objects: List<String>) :
    ArrayAdapter<String>(context, textViewResourceId, objects) {

    private val nameList = objects
    val filterList = ArrayList<String>()

    override fun getFilter(): Filter {
        return PinyinFilter()
    }

    override fun getCount(): Int {
        return filterList.size
    }

    override fun getItem(position: Int): String? {
        return filterList[position]
    }

    inner class PinyinFilter : Filter() {
        private val peopleNameList = convertToPeopleNameList(nameList)

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            if (constraint != null) {
                val keyWord: String = constraint.toString().trim()
                filterList.clear()

                for (peopleName in peopleNameList) {
                    if (QwertyUtil.match(peopleName.searchUnit, keyWord)) {
                        filterList.add(peopleName.name)
                    }
                }
            }

            val result = FilterResults()
            result.values = filterList
            result.count = filterList.size
            return result
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results != null && results.count > 0) {
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }
        }
    }

    fun convertToPeopleNameList(nameList: List<String>): List<PeopleName> {
        val peopleNameList: MutableList<PeopleName> = ArrayList()
        for (name in nameList) {
            peopleNameList.add(PeopleName(name))
        }
        return peopleNameList
    }
}