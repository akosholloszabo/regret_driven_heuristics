package hu.akos.hollo.szabo.utils

fun DoubleArray.asDoubleVector() = DoubleVector(this)
fun DoubleArray.toDoubleVector() = DoubleVector(clone())