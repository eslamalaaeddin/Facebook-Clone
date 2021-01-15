package com.example.facebook_clone.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LiveData
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.RecentSearchesAdapter
import com.example.facebook_clone.adapter.SearchedGroupsAdapter
import com.example.facebook_clone.adapter.SearchedUsersAdapter
import com.example.facebook_clone.helper.listener.SearchedItemListener
import com.example.facebook_clone.model.group.Group
import com.example.facebook_clone.model.user.User
import com.example.facebook_clone.model.user.search.Search
import com.example.facebook_clone.viewmodel.activity.SearchActivityViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_search.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "SearchActivity"

class SearchActivity : AppCompatActivity(), SearchedItemListener {
    private val auth: FirebaseAuth by inject()
    private var usersSearchResultLiveData: LiveData<List<User>>? = null
    private var groupsSearchResultLiveData: LiveData<List<Group>>? = null
    private val searchActivityViewModel by viewModel<SearchActivityViewModel>()
    private lateinit var searchedUsersAdapter: SearchedUsersAdapter
    private lateinit var searchedGroupsAdapter: SearchedGroupsAdapter
    private lateinit var recentSearchesAdapter: RecentSearchesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        upButtonImageView.setOnClickListener { finish() }

        val myLiveData = searchActivityViewModel.getMe(auth.currentUser?.uid.toString())

        myLiveData?.observe(this, {user ->
            val recentSearches = user.searches.orEmpty()
            recentSearchesAdapter = RecentSearchesAdapter(recentSearches.reversed(), this)
            usersRecentSearchRecyclerView.adapter = recentSearchesAdapter
        })

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(query: Editable?) {
                if (query?.isNotEmpty()!!) {
                    usersRecentSearchRecyclerView.visibility = View.GONE
                    if (query.toList().size >= 3) {
                        usersSearchResultRecyclerView.visibility = View.VISIBLE
//                        pagesSearchResultRecyclerView.visibility = View.VISIBLE
                        groupsSearchResultRecyclerView.visibility = View.VISIBLE

                        usersSearchResultLiveData =
                            searchActivityViewModel.searchForUsers(query.toString())
                        if (usersSearchResultLiveData != null) {
                            usersSearchResultLiveData?.observe(this@SearchActivity, { users ->
                                searchedUsersAdapter =
                                    SearchedUsersAdapter(users, this@SearchActivity)
                                usersSearchResultRecyclerView.adapter = searchedUsersAdapter
                            })
                        }

                        groupsSearchResultLiveData = searchActivityViewModel.searchForGroup(query.toString())
                        if (groupsSearchResultLiveData != null) {
                            groupsSearchResultLiveData?.observe(this@SearchActivity, { groups ->
                                searchedGroupsAdapter =
                                    SearchedGroupsAdapter(groups, this@SearchActivity)
                                usersSearchResultRecyclerView.adapter = searchedGroupsAdapter
                            })
                        }
                    }
                } else {
                    usersRecentSearchRecyclerView.visibility = View.VISIBLE
                    //recentSearchesAdapter = RecentSearchesAdapter()
                   // recentSearchRecyclerView.adapter = recentSearchesAdapter
                    usersSearchResultRecyclerView.visibility = View.GONE
                    pagesSearchResultRecyclerView.visibility = View.GONE
                    groupsSearchResultRecyclerView.visibility = View.GONE
                }
            }
        })
    }

    override fun onSearchedUserClicked(searchedUser: User) {
        val search = Search(
            searchedId = searchedUser.id,
            searchedName = searchedUser.name,
            searchedImageUrl = searchedUser.profileImageUrl,
            searchedType = 1
        )
        addSearchToRecentSearches(search, auth.currentUser?.uid.toString())
        navigateToSearchedUserActivity(search)

    }

    override fun onRecentSearchedItemClicked(search: Search) {
        //I have to check if it is group or page or user
        if (search.searchedType == 1) {
            navigateToSearchedUserActivity(search)
        }
        else if (search.searchedType == 2) {
            navigateToSearchedGroupActivity(search)
        }

        //navigateToSearchedUserActivity(search)
    }

    override fun onSearchedGroupClicked(searchedGroup: Group) {
        val search = Search(
            searchedId = searchedGroup.id,
            searchedName = searchedGroup.name,
            searchedImageUrl = searchedGroup.coverImageUrl,
            searchedType = 2
        )
        addSearchToRecentSearches(search, auth.currentUser?.uid.toString())
        navigateToSearchedGroupActivity(search)
    }

    private fun navigateToSearchedGroupActivity(searchedGroup: Search) {
        val intent = Intent(this, GroupActivity::class.java)
        intent.putExtra("groupId", searchedGroup.searchedId)
        startActivity(intent)
    }

    private fun navigateToSearchedUserActivity(search: Search){
        if (search.searchedId == auth.currentUser?.uid.toString()){
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        else {
            val intent = Intent(this, OthersProfileActivity::class.java)
            intent.putExtra("userId", search.searchedId)
            startActivity(intent)
        }
    }


    override fun onDeleteSearchIconClicked(search: Search) {
        deleteSearchToRecentSearches(search, auth.currentUser?.uid.toString())
    }

    override fun onBackPressed() {
        finish()
    }

    private fun addSearchToRecentSearches(search: Search, searcherId: String){
        searchActivityViewModel.addSearchToRecentSearches(search, searcherId).addOnCompleteListener {
            if (it.isSuccessful){
                Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, it.exception?.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteSearchToRecentSearches(search: Search, searcherId: String){
        searchActivityViewModel.deleteSearchFromRecentSearches(search, searcherId)
    }

}