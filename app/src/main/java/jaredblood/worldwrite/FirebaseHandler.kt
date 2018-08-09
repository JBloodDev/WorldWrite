package jaredblood.worldwrite

import android.content.Context
import android.content.Intent
import android.databinding.ObservableField
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.util.ArrayList


class FirebaseHandler (context: Context){

    companion object {
        lateinit var instance: FirebaseHandler
    }

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val messagesDatabaseReference = firebaseDatabase.reference.child("messages")
    lateinit var usersDatabaseReference: DatabaseReference
    lateinit var currentUser: User
    lateinit var firebaseUser: FirebaseUser
    val mFirebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)
    val writes: ArrayList<Write> = ArrayList()
    val errorMessage= ObservableField<String>()
    var isSignedIn: Boolean = false
    val signInIntent: Intent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setIsSmartLockEnabled(false)
            .setAvailableProviders(mutableListOf(
                    AuthUI.IdpConfig.EmailBuilder().build(),
                    AuthUI.IdpConfig.GoogleBuilder().build()))
            .build()

    fun removeListeners(){
        firebaseAuth.removeAuthStateListener(authStateListener)
        messagesDatabaseReference.removeEventListener(messagesEventListener)
    }

    fun addListeners(){
        firebaseAuth.addAuthStateListener(authStateListener)
        messagesDatabaseReference.addChildEventListener(messagesEventListener)
    }

    fun init(){
        messagesDatabaseReference.addChildEventListener(messagesEventListener)
        usersDatabaseReference = firebaseDatabase.reference.child("users").child(firebaseUser.uid)
        usersDatabaseReference.addValueEventListener(userEventListener)
    }

    private fun onSignedOutCleanup() {
        messagesDatabaseReference.removeEventListener(messagesEventListener)
    }

    fun rateGood(write: Write){
        messagesDatabaseReference.child(write.messageUID).child("ratingGood").setValue(write.ratingGood + 1)
        currentUser.goodRatings.add(write.messageUID)
        usersDatabaseReference.setValue(currentUser)
    }

    fun ratePoor(write: Write){
        messagesDatabaseReference.child(write.messageUID).child("ratingPoor").setValue(write.ratingPoor + 1)
        currentUser.poorRatings.add(write.messageUID)
        usersDatabaseReference.setValue(currentUser)
    }

    //manages the data for each Write in the database
    private val messagesEventListener: ChildEventListener = object : ChildEventListener {

        override fun onCancelled(error: DatabaseError) {
            Log.d("messagesEventListener", "onCancelled, ${error.details}")
            errorMessage.set("Error communicating with Firebase servers \n" + error.details)
            //This method will be triggered in the event that this listener either failed at the server, or is removed as a result of the security and Firebase rules.
        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
            return
            //This method is triggered when a child location's priority changes. See setPriority(Object) and Ordered Data for more information on priorities and ordering data.
            //should never be called as all entries should be ordered by key and never change priority
        }

        //called on ratings change
        override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
            val write = dataSnapshot.getValue(Write::class.java)
            if (write != null) {
                writes.forEach{it:Write ->
                    if(it.messageUID == write.messageUID){
                        it.set(write)
                    }
                }
                /*if (write.messageUID == selectedWrite.messageUID) {
                    selectedWrite.set(write)
                }
                mapMarkerToWriteHashMap.forEach {( key: String, value: Write) ->
                    if (value.messageUID == write.messageUID) {
                        mapMarkerToWriteHashMap[key] = write
                    }
                }*/
            }
        }

        //called for every message at the start and for any new messages added
        override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
            val write = dataSnapshot.getValue(Write::class.java)
            if (write != null) {
                writes.add(write)
                /*val marker = mMap.addMarker(MarkerOptions()
                        .position(LatLng(write.lat, write.lon))
                        .icon(mapPin))
                mapMarkerToWriteHashMap[marker.id] = write*/
            }
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            return
            //only called when a message is removed while the app is running
        }

    }

    fun signout(){
        firebaseAuth.signOut()
    }

    //manages user UID and ratings
    private val userEventListener: ValueEventListener = object : ValueEventListener {
        override fun onCancelled(error: DatabaseError) {
            Log.d("userEventListener", "onCancelled, ${error.details}")
            errorMessage.set("Error communicating with Firebase servers \n" + error.details)
        }

        //called immediately with the contents of the current user and every time the user's data changes (after rating)
        override fun onDataChange(snapshot: DataSnapshot) {
            val user = snapshot.getValue(User::class.java)
            if (user != null) {
                currentUser = user
            } else {//first time user
                usersDatabaseReference.setValue(User(firebaseUser.uid))
            }
        }

    }

    //called when the user signs in or out
    //called on instantiation
    private val authStateListener = { auth: FirebaseAuth ->
        val user = auth.currentUser
        if (user != null) {
            logEvent(FirebaseAnalytics.Event.LOGIN, Pair(FirebaseAnalytics.Param.METHOD, user.providerId))
            firebaseUser = user
            isSignedIn = true
            init()
        } else {
            isSignedIn = false
            onSignedOutCleanup()
        }
    }

    fun logEvent(eventType: String, vararg params: Pair<String, String>){
        var bundle: Bundle? = null
        if(params.isNotEmpty()){
            bundle = Bundle()
            params.forEach {
                bundle.putString(it.first, it.second)
            }
        }
        mFirebaseAnalytics.logEvent(eventType, bundle)
    }
}