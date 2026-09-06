package com.artemis.mgrsnav

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.artemis.mgrsnav.ui.map.MapScreen
import com.artemis.mgrsnav.ui.navigate.NavigateScreen
import com.artemis.mgrsnav.ui.position.PositionScreen
import com.artemis.mgrsnav.ui.theme.MeridianTheme
import com.artemis.mgrsnav.ui.tools.ToolsScreen
import com.artemis.mgrsnav.ui.waypoints.WaypointsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeridianTheme {
                MeridianRoot()
            }
        }
    }
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

@Composable
fun MeridianRoot() {
    val tabs = listOf(
        Tab("position", "Position", Icons.Default.MyLocation),
        Tab("navigate", "Navigate", Icons.Default.Explore),
        Tab("map", "Map", Icons.Default.Map),
        Tab("waypoints", "Waypoints", Icons.Default.Place),
        Tab("tools", "Tools", Icons.Default.Handyman)
    )
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = current == tab.route,
                        onClick = {
                            nav.navigate(tab.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "position",
            modifier = Modifier.padding(padding)
        ) {
            composable("position") { PositionScreen() }
            composable("navigate") { NavigateScreen() }
            composable("map") { MapScreen() }
            composable("waypoints") { WaypointsScreen() }
            composable("tools") { ToolsScreen() }
        }
    }
}
