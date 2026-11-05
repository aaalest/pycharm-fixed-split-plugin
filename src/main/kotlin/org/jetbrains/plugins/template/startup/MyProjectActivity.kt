package org.jetbrains.plugins.template.startup

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.ui.ThreeComponentsSplitter
import com.intellij.openapi.wm.WindowManager
import java.awt.Container
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.util.WeakHashMap
import javax.swing.SwingUtilities
import javax.swing.Timer

class MyProjectActivity : ProjectActivity {

    private val monitoredSplitters = WeakHashMap<ThreeComponentsSplitter, SplitterState>()

    private class SplitterState(
        var lastTotalWidth: Int = -1,
        var isAdjusting: Boolean = false
    )

    override suspend fun execute(project: Project) {
        SwingUtilities.invokeLater {
            val frame = WindowManager.getInstance().getFrame(project) ?: return@invokeLater

            // Bind after initial IDE frames finish loading
            Timer(1500) {
                bindAllSplitters(frame)
            }.apply {
                isRepeats = false
                start()
            }

            // Secondary check to catch late-mounted tool windows
            Timer(4000) {
                bindAllSplitters(frame)
            }.apply {
                isRepeats = false
                start()
            }
        }
    }

    private fun bindAllSplitters(container: Container) {
        for (comp in container.components) {
            if (comp is ThreeComponentsSplitter) {
                attachToSplitter(comp)
            }
            if (comp is Container) {
                bindAllSplitters(comp)
            }
        }
    }

    private fun attachToSplitter(splitter: ThreeComponentsSplitter) {
        if (monitoredSplitters.containsKey(splitter)) return

        val state = SplitterState()
        monitoredSplitters[splitter] = state

        splitter.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                if (state.isAdjusting) return

                // Skip vertical splitters (top/bottom panels)
                if (splitter.orientation) return

                val currentTotalWidth = splitter.width
                if (currentTotalWidth <= 0) return

                val lastComp = splitter.lastComponent
                // Only adjust if the right tool window is open and has an active size
                if (lastComp == null || !lastComp.isVisible || splitter.lastSize <= 0) {
                    state.lastTotalWidth = currentTotalWidth
                    return
                }

                if (state.lastTotalWidth <= 0) {
                    state.lastTotalWidth = currentTotalWidth
                    return
                }

                val delta = currentTotalWidth - state.lastTotalWidth
                state.lastTotalWidth = currentTotalWidth

                if (delta != 0) {
                    val currentLastSize = splitter.lastSize
                    // Absorb the full resize delta into the right tool window
                    val newLastSize = (currentLastSize + delta).coerceAtLeast(100)

                    if (newLastSize != currentLastSize) {
                        state.isAdjusting = true
                        SwingUtilities.invokeLater {
                            try {
                                splitter.lastSize = newLastSize
                                splitter.revalidate()
                            } finally {
                                state.isAdjusting = false
                            }
                        }
                    }
                }
            }
        })
    }
}