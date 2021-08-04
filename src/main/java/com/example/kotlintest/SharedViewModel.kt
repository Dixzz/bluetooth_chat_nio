package com.example.kotlintest

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {
    private val mutableLiveData: MutableLiveData<ArrayList<Pair<String, String>>> = MutableLiveData()
    private val list = ArrayList<Pair<String, String>>()
    fun getMutable() = mutableLiveData as LiveData<ArrayList<Pair<String, String>>>
    fun postInData(int: Double, string: String) {
        list.add(int.toString() to string)
        mutableLiveData.postValue(list)
    }

    override fun onCleared() {
        super.onCleared()
        list.clear()
        mutableLiveData.value = null
    }
}