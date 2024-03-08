package com.dluvian.voyage.ui.views.main

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.model.RootPost
import com.dluvian.voyage.core.model.Upvote
import com.dluvian.voyage.core.navigation.HomeNavView
import com.dluvian.voyage.core.navigation.InboxNavView
import com.dluvian.voyage.core.navigation.MainNavView
import com.dluvian.voyage.core.navigation.TopicsNavView
import com.dluvian.voyage.ui.views.main.components.MainScaffold
import com.dluvian.voyage.ui.views.main.subViews.HomeView
import com.dluvian.voyage.ui.views.main.subViews.InboxView
import com.dluvian.voyage.ui.views.main.subViews.TopicsView

@Composable
fun MainView(currentView: MainNavView, snackbarHostState: SnackbarHostState, onUpdate: OnUpdate) {
    MainScaffold(
        currentView = currentView,
        snackBarHostState = snackbarHostState,
        onUpdate = onUpdate
    ) {
        when (currentView) {
            is HomeNavView -> HomeView(
                listOf(
                    RootPost(
                        "38ce9e0bfd0f4752d84f859d571154479de828d1f5ea201cab96a2a16f9815c5",
                        "38ce9e0bfd0f4752d84f859d571154479de828d1f5ea201cab96a2a16f9815c5",
                        "austronesian",
                        "time",
                        "Austronesians were the first humans with seafaring vessels that could cross large distances on the open ocean, which allowed them to colonize a large part of the Indo-Pacific region",
                        "Languages from the family are today spoken by about 386 million people (4.9% of the global population), making it the fifth-largest language family by number of speakers. Major Austronesian languages are Malay (around 250–270 million in Indonesia alone in its own literary standard, named Indonesian), Javanese, and Filipino (Tagalog). The family contains 1,257 languages, the second most of any language family.[89]\n" +
                                "\n" +
                                "The geographic region that encompasses native Austronesian-speaking populations is sometimes referred to as Austronesia.[73] Other geographic names for various subregions include Malay Peninsula, Greater Sunda Islands, Lesser Sunda Islands, Island Melanesia, Island Southeast Asia, Malay Archipelago, Maritime Southeast Asia, Melanesia, Micronesia, Near Oceania, Oceania, Pacific Islands, Remote Oceania, Polynesia, and Wallacea. In Indonesia, the nationalistic term Nusantara, from Old Javanese, is also popularly used for their islands",
                        Upvote,
                        21,
                        69,
                        12,
                        5
                    ),
                    RootPost(
                        "38ce9e0bfd0f4752d84f859d571154479de828d1f5ea201cab96a2a16f9815c5",
                        "38ce9e0bfd0f4752d84f859d571154479de828d1f5ea201cab96a2a16f9815c5",
                        "austronesian",
                        "time",
                        "Austronesians were the first humans with seafaring vessels that could cross large distances on the open ocean, which allowed them to colonize a large part of the Indo-Pacific region",
                        "Languages from the family are today spoken by about 386 million people (4.9% of the global population), making it the fifth-largest language family by number of speakers. Major Austronesian languages are Malay (around 250–270 million in Indonesia alone in its own literary standard, named Indonesian), Javanese, and Filipino (Tagalog). The family contains 1,257 languages, the second most of any language family.[89]\n" +
                                "\n" +
                                "The geographic region that encompasses native Austronesian-speaking populations is sometimes referred to as Austronesia.[73] Other geographic names for various subregions include Malay Peninsula, Greater Sunda Islands, Lesser Sunda Islands, Island Melanesia, Island Southeast Asia, Malay Archipelago, Maritime Southeast Asia, Melanesia, Micronesia, Near Oceania, Oceania, Pacific Islands, Remote Oceania, Polynesia, and Wallacea. In Indonesia, the nationalistic term Nusantara, from Old Javanese, is also popularly used for their islands",
                        Upvote,
                        21,
                        69,
                        12,
                        5
                    ),
                    RootPost(
                        "38ce9e0bfd0f4752d84f859d571154479de828d1f5ea201cab96a2a16f9815c5",
                        "38ce9e0bfd0f4752d84f859d571154479de828d1f5ea201cab96a2a16f9815c5",
                        "austronesian",
                        "time",
                        "Austronesians were the first humans with seafaring vessels that could cross large distances on the open ocean, which allowed them to colonize a large part of the Indo-Pacific region",
                        "Languages from the family are today spoken by about 386 million people (4.9% of the global population), making it the fifth-largest language family by number of speakers. Major Austronesian languages are Malay (around 250–270 million in Indonesia alone in its own literary standard, named Indonesian), Javanese, and Filipino (Tagalog). The family contains 1,257 languages, the second most of any language family.[89]\n" +
                                "\n" +
                                "The geographic region that encompasses native Austronesian-speaking populations is sometimes referred to as Austronesia.[73] Other geographic names for various subregions include Malay Peninsula, Greater Sunda Islands, Lesser Sunda Islands, Island Melanesia, Island Southeast Asia, Malay Archipelago, Maritime Southeast Asia, Melanesia, Micronesia, Near Oceania, Oceania, Pacific Islands, Remote Oceania, Polynesia, and Wallacea. In Indonesia, the nationalistic term Nusantara, from Old Javanese, is also popularly used for their islands",
                        Upvote,
                        21,
                        69,
                        12,
                        5
                    ),
                    RootPost(
                        "38ce9e0bfd0f4752d84f859d571154479de828d1f5ea201cab96a2a16f9815c5",
                        "38ce9e0bfd0f4752d84f859d571154479de828d1f5ea201cab96a2a16f9815c5",
                        "austronesian",
                        "time",
                        "Austronesians were the first humans with seafaring vessels that could cross large distances on the open ocean, which allowed them to colonize a large part of the Indo-Pacific region",
                        "Languages from the family are today spoken by about 386 million people (4.9% of the global population), making it the fifth-largest language family by number of speakers. Major Austronesian languages are Malay (around 250–270 million in Indonesia alone in its own literary standard, named Indonesian), Javanese, and Filipino (Tagalog). The family contains 1,257 languages, the second most of any language family.[89]\n" +
                                "\n" +
                                "The geographic region that encompasses native Austronesian-speaking populations is sometimes referred to as Austronesia.[73] Other geographic names for various subregions include Malay Peninsula, Greater Sunda Islands, Lesser Sunda Islands, Island Melanesia, Island Southeast Asia, Malay Archipelago, Maritime Southeast Asia, Melanesia, Micronesia, Near Oceania, Oceania, Pacific Islands, Remote Oceania, Polynesia, and Wallacea. In Indonesia, the nationalistic term Nusantara, from Old Javanese, is also popularly used for their islands",
                        Upvote,
                        21,
                        69,
                        12,
                        5
                    ),
                    RootPost(
                        "38ce9e0bfd0f4752d84f859d571154479de828d1f5ea201cab96a2a16f9815c5",
                        "38ce9e0bfd0f4752d84f859d571154479de828d1f5ea201cab96a2a16f9815c5",
                        "austronesian",
                        "time",
                        "Austronesians were the first humans with seafaring vessels that could cross large distances on the open ocean, which allowed them to colonize a large part of the Indo-Pacific region",
                        "Languages from the family are today spoken by about 386 million people (4.9% of the global population), making it the fifth-largest language family by number of speakers. Major Austronesian languages are Malay (around 250–270 million in Indonesia alone in its own literary standard, named Indonesian), Javanese, and Filipino (Tagalog). The family contains 1,257 languages, the second most of any language family.[89]\n" +
                                "\n" +
                                "The geographic region that encompasses native Austronesian-speaking populations is sometimes referred to as Austronesia.[73] Other geographic names for various subregions include Malay Peninsula, Greater Sunda Islands, Lesser Sunda Islands, Island Melanesia, Island Southeast Asia, Malay Archipelago, Maritime Southeast Asia, Melanesia, Micronesia, Near Oceania, Oceania, Pacific Islands, Remote Oceania, Polynesia, and Wallacea. In Indonesia, the nationalistic term Nusantara, from Old Javanese, is also popularly used for their islands",
                        Upvote,
                        21,
                        69,
                        12,
                        5
                    ),
                    RootPost(
                        "38ce9e0bfd0f4752d84f859d571154479de828d1f5ea201cab96a2a16f9815c5",
                        "38ce9e0bfd0f4752d84f859d571154479de828d1f5ea201cab96a2a16f9815c5",
                        "austronesian",
                        "time",
                        "Austronesians were the first humans with seafaring vessels that could cross large distances on the open ocean, which allowed them to colonize a large part of the Indo-Pacific region",
                        "Languages from the family are today spoken by about 386 million people (4.9% of the global population), making it the fifth-largest language family by number of speakers. Major Austronesian languages are Malay (around 250–270 million in Indonesia alone in its own literary standard, named Indonesian), Javanese, and Filipino (Tagalog). The family contains 1,257 languages, the second most of any language family.[89]\n" +
                                "\n" +
                                "The geographic region that encompasses native Austronesian-speaking populations is sometimes referred to as Austronesia.[73] Other geographic names for various subregions include Malay Peninsula, Greater Sunda Islands, Lesser Sunda Islands, Island Melanesia, Island Southeast Asia, Malay Archipelago, Maritime Southeast Asia, Melanesia, Micronesia, Near Oceania, Oceania, Pacific Islands, Remote Oceania, Polynesia, and Wallacea. In Indonesia, the nationalistic term Nusantara, from Old Javanese, is also popularly used for their islands",
                        Upvote,
                        21,
                        69,
                        12,
                        5
                    )
                ), false, onUpdate = onUpdate
            )

            InboxNavView -> InboxView()
            TopicsNavView -> TopicsView()
        }
    }
}