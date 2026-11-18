package com.lazybuzz.features.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lazybuzz.LazyBuzzApplication
import com.lazybuzz.core.GestureRuntime
import com.lazybuzz.core.RuntimeState
import com.lazybuzz.core.RuntimeStatus
import com.lazybuzz.data.models.InteractionMode
import com.lazybuzz.navigation.Dest
import com.lazybuzz.ui.animations.PointerMark
import com.lazybuzz.ui.components.LbCard
import com.lazybuzz.ui.components.StatusPill

private val Tools = listOf(Dest.Preview, Dest.Studio, Dest.Mapping, Dest.Calibration, Dest.Interaction, Dest.Floating, Dest.Privacy)

@Composable
fun DashboardScreen(onOpen: (Dest) -> Unit) {
    val repo = (LocalContext.current.applicationContext as LazyBuzzApplication).settings
    val vm: DashboardViewModel = viewModel(factory = DashboardViewModel.factory(repo))
    val s by vm.settings.collectAsStateWithLifecycle()
    val rt by GestureRuntime.state.collectAsStateWithLifecycle()
    val cs = MaterialTheme.colorScheme

    Scaffold { pad ->
        LazyColumn(
            Modifier.padding(pad),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            item {
                Column {
                    Text("lazybuzz", style = MaterialTheme.typography.headlineLarge)
                    Text(
                        "Control your phone without touching it.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurfaceVariant,
                    )
                }
            }
            item { Hero(rt) }
            item {
                Section("How you point") {
                    Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ChoiceCard(
                            "Virtual cursor", "A pointer follows your fingertip. Bend to select.",
                            s.mode == InteractionMode.VIRTUAL_CURSOR,
                            { vm.setMode(InteractionMode.VIRTUAL_CURSOR) },
                            Modifier.weight(1f).fillMaxHeight(),
                        )
                        ChoiceCard(
                            "Direct gesture", "Flick to scroll and navigate. No pointer on screen.",
                            s.mode == InteractionMode.DIRECT_GESTURE,
                            { vm.setMode(InteractionMode.DIRECT_GESTURE) },
                            Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                }
            }
            item {
                Section("How you click") {
                    LbCard {
                        ToggleRow("Finger bend", "Bend your index finger, then straighten it.", s.bendClickEnabled, vm::setBendClick)
                        HorizontalDivider(color = cs.outline)
                        ToggleRow(
                            "Forward tap",
                            "Push your fingertip toward the screen and release. Less reliable, so it's off by default.",
                            s.forwardTapEnabled, vm::setForwardTap,
                        )
                    }
                }
            }
            item {
                Section("Tools") {
                    LbCard {
                        Tools.forEachIndexed { i, d ->
                            if (i > 0) HorizontalDivider(color = cs.outline)
                            ToolRow(d, onOpen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Hero(rt: RuntimeState) {
    val cs = MaterialTheme.colorScheme
    LbCard(radius = 28.dp) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                PointerMark(active = rt.status == RuntimeStatus.RUNNING, modifier = Modifier.size(88.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusPill(rt.status.label(), active = rt.status == RuntimeStatus.RUNNING)
                    Text("Gesture control", style = MaterialTheme.typography.titleLarge)
                    Text(rt.hint(), style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                }
            }
            // Disabled until camera tracking exists (Milestones 2-3).
            Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text("Start gesture control")
            }
        }
    }
}

private fun RuntimeStatus.label() = when (this) {
    RuntimeStatus.STOPPED -> "Off"
    RuntimeStatus.STARTING -> "Starting"
    RuntimeStatus.RUNNING -> "Active"
    RuntimeStatus.PAUSED -> "Paused"
    RuntimeStatus.INTERRUPTED -> "Camera interrupted"
}

private fun RuntimeState.hint() = when (status) {
    RuntimeStatus.STOPPED -> "Camera tracking isn't built into this version yet."
    RuntimeStatus.STARTING -> "Starting the camera."
    RuntimeStatus.RUNNING -> if (handDetected) "Hand detected." else "Show your hand to the front camera."
    RuntimeStatus.PAUSED -> "Tracking is paused."
    RuntimeStatus.INTERRUPTED -> "Another app is using the camera."
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

@Composable
private fun ChoiceCard(title: String, body: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Surface(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) cs.primary.copy(alpha = 0.10f) else cs.surface,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) cs.primary else cs.outline),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
        }
    }
}

@Composable
private fun ToggleRow(title: String, body: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(16.dp))
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
private fun ToolRow(d: Dest, onOpen: (Dest) -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .clickable(role = Role.Button) { onOpen(d) }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(d.title, style = MaterialTheme.typography.titleMedium)
            Text(d.blurb, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(12.dp))
        Text("›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
