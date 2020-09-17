package com.fahru.hrdreminder.submenu

import android.app.AlertDialog
import android.os.Bundle
import com.fahru.hrdreminder.R
import com.fahru.hrdreminder.handler.*
import com.fahru.hrdreminder.handler.Unit
import com.fahru.hrdreminder.model.BaseModel
import com.fahru.hrdreminder.model.SettingModel
import kotlinx.android.synthetic.main.activity_setting.*

class Setting : BaseModel() {
    private val st = SettingHandler()
    private var multiplier: Long = 0
    private var timeSt: Long = 0
    private var unitSt: String = ""
    private var unitNow : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        loadSetting()
        firstPrepare()
    }

    private fun firstPrepare() {
        setting_title.typeface = faceHymen(this)
        setting_title.setOnClickListener {
            toastNormal(this, "change: $unitSt == $unitNow then: ${setChangeCheck()}")
        }
        setting_rg.setOnCheckedChangeListener {rg, i ->

            when(i){
                R.id.setting_rb_day -> {
                    Unit.D.unit.let {
                        setForSave(it)
                        unitNow = it
                    }
                }
                R.id.setting_rb_week -> {
                    Unit.W.unit.let {
                        setForSave(it)
                        unitNow = it
                    }
                }
                R.id.setting_rb_month -> {
                    Unit.M.unit.let {
                        setForSave(it)
                        unitNow = it
                    }
                }
            }
        }
    }

    private fun setForSave(unit: String) {
        val unitBhs = unit.let {
            when(it){
                Unit.D.unit -> UnitBhs.D.unit
                Unit.W.unit -> UnitBhs.W.unit
                Unit.M.unit -> UnitBhs.M.unit
                else -> UnitBhs.D.unit
            }
        }

        setting_urgent_unit.text = unitBhs
        setting_warning_unit.text = unitBhs
        multiplier = st.getMultiplierWhenUnit(unit)
    }

    private fun loadSetting() {
        st.loadSetting(object : SettingCallback{
            override fun onSetting(code: Int, setting: SettingModel) {
                setting.also {
                    setSetting(it)
                }
            }
        })
    }

    fun setSetting(set: SettingModel){
        when(set.unit){
            Unit.D.unit -> setting_rb_day.isChecked = true
            Unit.W.unit -> setting_rb_week.isChecked = true
            Unit.M.unit -> setting_rb_month.isChecked = true
        }
        setting_val_urgent.setText(set.urgent.toString())
        setting_val_warning.setText(set.warning.toString())
        multiplier = st.getMultiplierWhenUnit(set.unit)

        unitSt = set.unit
        timeSt = set.warning+set.urgent
    }

    private fun setChangeCheck(): Boolean{
        val timeNow = setting_val_urgent.text.toString().toLong()+setting_val_warning.text.toString().toLong()

        return unitNow != unitSt || timeNow != timeSt
    }

    private fun getSettingNow(): SettingModel{
        return SettingModel(unitNow,
            setting_val_urgent.text.toString().toLong(),
            setting_val_warning.text.toString().toLong())
    }

    override fun onBackPressed() {
        val s = getSettingNow()
        when {
            s.urgent > s.warning -> {
                toastError(this, getString(R.string.warning_urgent_greaterthan_warning))
            }
            setChangeCheck() -> {
                showDialogSave()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    private fun showDialogSave() {
        val dialog = AlertDialog.Builder(this)
        dialog.setMessage("Terapkan perubahan?")
        dialog.setPositiveButton("YA"){_, _ ->
            st.saveSetting(getSettingNow(), object : ActionCallback{
                override fun onActionFinish(code: Int, msg: String) {
                    when(code){
                        1 ->  toastSuccess(this@Setting, msg)
                        0 -> toastError(this@Setting, msg)
                    }
                }

            })
            super.onBackPressed()
        }.setNegativeButton("TIDAK"){d, _ ->
            d.cancel()
            super.onBackPressed()
        }
        dialog.show()
    }
}