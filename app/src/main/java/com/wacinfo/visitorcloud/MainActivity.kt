/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wacinfo.visitorcloud

import android.app.SearchManager
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.actionBar
import com.mikepenz.iconics.utils.paddingDp
import com.mikepenz.materialdrawer.holder.ImageHolder
import com.mikepenz.materialdrawer.iconics.iconicsIcon
import com.mikepenz.materialdrawer.model.*
import com.mikepenz.materialdrawer.model.interfaces.*
import com.mikepenz.materialdrawer.util.addStickyDrawerItems
import com.mikepenz.materialdrawer.util.setupWithNavController
import com.mikepenz.materialdrawer.widget.AccountHeaderView
import com.wacinfo.visitorcloud.Adapter.OnFragmentInteractionListener
import com.wacinfo.visitorcloud.Login.LoginActivity
import com.wacinfo.visitorcloud.databinding.ActivityMainBinding
import com.wacinfo.visitorcloud.ui.log.LogFragment
import com.wacinfo.visitorcloud.utils.*
import eu.davidea.fastscroller.FastScroller
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


/**
 * An activity that inflates a layout that has a [BottomNavigationView].
 */
class MainActivity : AppCompatActivity(), FastScroller.OnScrollStateChangeListener,
    OnFragmentInteractionListener, SearchView.OnQueryTextListener {
    private var TAG = "MainActivityLog"
    private lateinit var binding: ActivityMainBinding
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: FlexibleAdapter<AbstractFlexibleItem<*>>? = null
    private var currentNavController: LiveData<NavController>? = null
    private var mSearchView: SearchView? = null
    private var mFragment: Fragment? = null

    private lateinit var headerView: AccountHeaderView
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var profile: IProfile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("globalVar", AppSettings.visitorType.toString())
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Handle Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_container) as NavHostFragment
        val navController = navHostFragment.navController


        actionBarDrawerToggle = ActionBarDrawerToggle(
            this@MainActivity,
            binding.root,
            binding.toolbar,
            com.mikepenz.materialdrawer.R.string.material_drawer_open,
            com.mikepenz.materialdrawer.R.string.material_drawer_close
        )
        binding.root.addDrawerListener(actionBarDrawerToggle)
        // Create a few sample profile

        if (AppSettings.USER_LOGO != null) {
            profile = ProfileDrawerItem().apply {
                nameText = AppSettings.USER_NAME; descriptionText = AppSettings.USER_RULE;
                iconBitmap = AppSettings.USER_LOGO!!
            }
        } else {
            profile = ProfileDrawerItem().apply {
                nameText = AppSettings.USER_NAME; descriptionText = AppSettings.USER_RULE;
                iconRes = R.drawable.icons8_user_64px_1
            }
        }


        // Create the AccountHeader
        buildHeader(false, savedInstanceState)
        binding.slider.apply {
            itemAdapter.add(

                //here we use a customPrimaryDrawerItem we defined in our sample app
                //this custom DrawerItem extends the PrimaryDrawerItem so it just overwrites some methods

                NavigationDrawerItem(R.id.titleScreen,
                    SecondaryDrawerItem().apply {
                        nameRes = R.string.title_home; descriptionText = "Dashboard";iconicsIcon =
                        GoogleMaterial.Icon.gmd_home
                        ; identifier = 4.toLong()
                    }),
                NavigationDrawerItem(R.id.logScreen, SecondaryDrawerItem().apply {
                    nameText = "Log"; iconicsIcon =
                    GoogleMaterial.Icon.gmd_description;
                }),
                SecondaryDrawerItem().apply {
                    nameText = "Appointment"; iconicsIcon = GoogleMaterial.Icon.gmd_event;
                    isEnabled = false
                },
                NavigationDrawerItem(R.id.securitylistScreen,
                    SecondaryDrawerItem().apply {
                        nameRes = R.string.blacklist_whitelist; iconicsIcon =
                        GoogleMaterial.Icon.gmd_do_not_disturb;
                    })

            )

            addStickyDrawerItems(
                NavigationDrawerItem(R.id.settingscreen, SecondaryDrawerItem().apply {
                    nameText = "Setting"; iconDrawable =
                    IconicsDrawable(
                        this@MainActivity,
                        GoogleMaterial.Icon.gmd_settings
                    ).apply { actionBar(); paddingDp = 5 }; isIconTinted = true;identifier =
                    10.toLong()
                })
            )

            addStickyDrawerItems(
                SecondaryDrawerItem().apply {
                    nameText = "Logout"; iconDrawable =
                    IconicsDrawable(
                        this@MainActivity,
                        GoogleMaterial.Icon.gmd_logout
                    ).apply { actionBar(); paddingDp = 5 }; isIconTinted = true;identifier =
                    10.toLong()
                    onDrawerItemClickListener = { v, drawerItem, position ->
                        logout()
                        false
                    }
                }
            )

            setupWithNavController(navController) { v, drawerItem, position ->
                var intent: Intent? = null

                if (intent != null) {
                    this@MainActivity.startActivity(intent)
                }

//                if (drawerItem is Nameable) {
//
//                    Toast.makeText(
//                        this@MainActivity,
//                        drawerItem.name?.getText(this@MainActivity),
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
                false
            }

            setSavedInstance(savedInstanceState)
        }
    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Now that BottomNavigationBar has restored its instance state
        // and its selectedItemId, we can proceed with setting up the
        // BottomNavigationBar with Navigation
        Log.d(TAG, "onRestoreInstanceState: ")
//        setupBottomNavigationBar()
    }


    override fun onFastScrollerStateChange(scrolling: Boolean) {
    }

    override fun onFragmentChange(
        swipeRefreshLayout: SwipeRefreshLayout,
        recyclerView: RecyclerView,
        mode: Int
    ) {
        mRecyclerView = recyclerView
        mAdapter = recyclerView.adapter as FlexibleAdapter<AbstractFlexibleItem<*>>?

        mSwipeRefreshLayout = swipeRefreshLayout

    }

    override fun initSearchView(menu: Menu?) {
        // Associate searchable configuration with the SearchView

        // Associate searchable configuration with the SearchView
        eu.davidea.flexibleadapter.utils.Log.d("onCreateOptionsMenu setup SearchView!")
        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        val searchItem = menu!!.findItem(R.id.action_search)
        if (searchItem != null) {

            mSearchView = MenuItemCompat.getActionView(searchItem) as SearchView
            mSearchView!!.inputType = InputType.TYPE_TEXT_VARIATION_FILTER
            mSearchView!!.imeOptions =
                EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_NO_FULLSCREEN
            mSearchView!!.queryHint = getString(R.string.search)
            mSearchView!!.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            mSearchView!!.setOnQueryTextListener(this)
        }

    }


    private fun initializeFragment(savedInstanceState: Bundle?) {
        mFragment = LogFragment().newInstance(2)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        Log.v(TAG, "onQueryTextSubmit called!")
        return onQueryTextChange(query)
    }

    override fun onQueryTextChange(newText: String?): Boolean {

        return true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        actionBarDrawerToggle.syncState()
    }

    override fun onSaveInstanceState(_outState: Bundle) {
        var outState = _outState
        //add the values which need to be saved from the drawer to the bundle
        outState = binding.slider.saveInstanceState(outState)

        //add the values which need to be saved from the accountHeader to the bundle
        outState = headerView.saveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity\
        if (binding.root.isDrawerOpen(binding.slider)) {
            binding.root.closeDrawer(binding.slider)
        } else {

            super.onBackPressed()
            actionBarDrawerToggle.syncState()
            binding.toolbar.setNavigationOnClickListener {
                binding.slider.drawerLayout?.openDrawer(binding.slider)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        actionBarDrawerToggle.syncState()

//        binding.toolbar.setNavigationOnClickListener {
//            binding.slider.drawerLayout?.openDrawer(binding.slider)
//        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
//            Log.d(TAG, "onOptionsItemSelected: ${item.itemId}")
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun buildHeader(compact: Boolean, savedInstanceState: Bundle?) {
        // Create the AccountHeader
        headerView = AccountHeaderView(this, compact = compact).apply {
            attachToSliderView(binding.slider)
            headerBackground = ImageHolder(R.drawable.header)
            addProfiles(
                profile
            )
            selectionListEnabledForSingleProfile = false
            withSavedInstance(savedInstanceState)
        }
    }

    private fun logout() {
        val dialog = PublicFunction().retrofitDialog(this)
        if (!dialog!!.isShowing) {
            runOnUiThread {
                dialog.show()
            }
        }

        val url: String = resources.getString(R.string.URL) + resources.getString(R.string.PORT)
        val apiname = resources.getString(R.string.API_Logout)
        val client: OkHttpClient =
            OkHttpClient.Builder().addInterceptor(TokenInterceptor(this)).build()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        val logoutbody = RetrofitData.Logout.Post()
        logoutbody.refresh_token = AppSettings.REFRESH_TOKEN

        retrofit.create(API::class.java)
            .postLogout(logoutbody, apiname)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RetrofitData.Logout.Respones> {
                override fun onComplete() {
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: RetrofitData.Logout.Respones) {
                    dialog.cancel()
                    AppSettings().resetSettings()
                    getSharedPreferences(getString(R.string.SharePreferencesSetting),0)
                        .edit()
                        .putBoolean(getString(R.string.Pref_Login_Status),false)
                        .putString(getString(R.string.Pref_Login_Username),"")
                        .putString(getString(R.string.Pref_Login_Password),"")
                        .apply()

                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    finish()
                }

                override fun onError(e: Throwable) {
                    PublicFunction().errorDialog(dialog).show()

                    e.printStackTrace()
                    if (e is HttpException) {
                        try {
                            val jObjError = JSONObject(e.response()!!.errorBody()?.string())
                            Log.d(TAG, jObjError.getString("message"))
                        } catch (e: Exception) {
                            Log.d(TAG, "onError: $e")
                        }
                    }

                }
            })
    }


}
