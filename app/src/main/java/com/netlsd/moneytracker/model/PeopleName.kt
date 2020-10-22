package com.netlsd.moneytracker.model

import com.pinyinsearch.model.PinyinSearchUnit
import com.pinyinsearch.util.PinyinUtil

data class PeopleName(var name: String, val searchUnit: PinyinSearchUnit = PinyinSearchUnit()) {
    init {
        searchUnit.baseData = name
        PinyinUtil.parse(searchUnit)
    }
}