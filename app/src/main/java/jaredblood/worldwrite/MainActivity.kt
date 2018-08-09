package jaredblood.worldwrite

import jaredblood.worldwrite.databinding.ActivityMainBinding
import jaredblood.worldwrite.databinding.BottomdrawerMainWriteBinding
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottomdrawer_main_nav.*
import android.databinding.DataBindingUtil
import android.databinding.Observable
import android.os.AsyncTask
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import com.crashlytics.android.Crashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_main.view.*

class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var mapFragment: SupportMapFragment
    }
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var writeBinding: BottomdrawerMainWriteBinding
    val errorDialog = ErrorDialog(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapHandler(main_mapfragment as SupportMapFragment)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val writeBind: BottomdrawerMainWriteBinding? = DataBindingUtil.bind(binding.root.include_bottomdrawer_main_write)
        if(writeBind != null)
            writeBinding = writeBind
        viewModel.onCreate()
        binding.viewmodel = viewModel
        writeBinding.viewmodel = viewModel
        writeBinding.selectedwrite = viewModel.selectedWrite
        viewModel.errorMessage.addOnPropertyChangedCallback(errorDialog)
        writeBinding.selectedWriteHasRatedGood = viewModel.selectedWriteHasRatedGood
        writeBinding.selectedWriteHasRatedPoor = viewModel.selectedWriteHasRatedPoor
        lifecycle.addObserver(viewModel)
    }

    fun closeWrite() {
        binding.root.include_bottomdrawer_main_write.visibility = View.GONE
        bottomdrawer_main_nav_layout.visibility = View.VISIBLE
    }

    fun showWrite() {
        bottomdrawer_main_nav_layout.visibility = View.GONE
        binding.root.include_bottomdrawer_main_write.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    @Suppress("UNUSED_PARAMETER")
    fun rateGood(view: View){
        viewModel.rateGood()
    }

    @Suppress("UNUSED_PARAMETER")
    fun ratePoor(view: View){
        viewModel.ratePoor()
    }

    @Suppress("UNUSED_PARAMETER")
    fun startMessageActivity(view: View) {
        FirebaseHandler.instance.logEvent("attempt_share")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Terms and Conditions")
        builder.setView(R.layout.item_termsandconditions)
        builder.setPositiveButton("Agree")
        { _, _: Int ->
            ContextCompat.startActivity(this, Intent(this, MessageActivity::class.java), null)
        }
        builder.setNegativeButton("Cancel")
        { _, _: Int ->

        }
        builder.create().show()
    }
}
