package com.sajun.sapjil.screen.sensor

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

interface ISensorTestViewModel {

}

@HiltViewModel
class SensorTestViewModel @Inject constructor(

) : ViewModel(), ISensorTestViewModel {

}

object FakeSensorTestViewModel : ISensorTestViewModel {

}