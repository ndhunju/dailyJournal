package com.ndhunju.dailyjournal.controller.tutorial

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ListView
import com.ndhunju.dailyjournal.R
import com.ndhunju.dailyjournal.controller.IconTextAdapter
import com.ndhunju.dailyjournal.controller.NavDrawerActivity

class TutorialActivity: NavDrawerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addContentFrame(R.layout.activity_tutorial)

        val listView = findViewById<ListView>(R.id.activity_tutorial_recycler_view)
        listView.adapter = IconTextAdapter(this).apply {
            addStringArray(R.array.tutorial_video_title, R.drawable.ic_tutorial_video)
        }

        val links = resources.getStringArray(R.array.tutorial_video_links);

        listView.setOnItemClickListener { _, _, i, _ ->
            val link = links[i]
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
        }

    }
}