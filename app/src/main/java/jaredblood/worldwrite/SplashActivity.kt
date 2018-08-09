package jaredblood.worldwrite

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.crashlytics.android.Crashlytics
import com.firebase.ui.auth.AuthUI
import io.fabric.sdk.android.Fabric


class SplashActivity: AppCompatActivity() {

    val errorDialog = ErrorDialog(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        class InitFabric: AsyncTask<Unit, Unit, Unit>(){
            override fun doInBackground(vararg params: Unit?) {
                val fabric = Fabric.Builder(MainActivity.instance).kits(Crashlytics()).debuggable(true).build()
                Fabric.with(fabric)
            }
        }
        InitFabric().execute()
        FirebaseHandler(applicationContext)
        FirebaseHandler.instance.errorMessage.addOnPropertyChangedCallback(errorDialog)
    }

    override fun onResume() {
        super.onResume()
        FirebaseHandler.instance.errorMessage.addOnPropertyChangedCallback(errorDialog)
        if(!FirebaseHandler.instance.isSignedIn){
            startActivityForResult(FirebaseHandler.instance.signInIntent, 1)
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        FirebaseHandler.instance.errorMessage.removeOnPropertyChangedCallback(errorDialog)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1){
            if(resultCode == Activity.RESULT_OK){
                //signed in
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
        finish()
        }
    }
}