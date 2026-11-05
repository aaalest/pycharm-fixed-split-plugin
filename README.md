# Fixed Editor Width

An IntelliJ Platform / PyCharm plugin that prevents the main editor pane from shrinking or squeezing horizontally when resizing the IDE window.

---

> **Note:** This entire plugin codebase, architecture discovery, and implementation were completely generated via Google Gemini.

---

## The Problem

In standard JetBrains IDE layout behavior, the editor and side tool windows (such as the Run, Debug, or Terminal panes) are managed inside a `ThreeComponentsSplitter`. 

When the IDE window is resized narrower, the IDE defaults to preserving the fixed width of the right-side tool windows, aggressively squeezing the central editor pane into word-wrap or horizontal scroll.

## How It Works

This plugin hooks into IntelliJ's underlying `ThreeComponentsSplitter` hierarchy during IDE startup:

- Tracks the width delta whenever the IDE window or root layout is resized.
- Forces horizontal size adjustments exclusively into the docked right tool window (`splitter.lastSize`).
- Preserves the editor text pane's exact horizontal pixel width when tiling or resizing windows on Linux/Wayland and multi-monitor setups.

## Compatibility

- **IDE:** PyCharm / IntelliJ IDEA (Build 261.x and newer)
- **Platforms:** Linux (tested on Fedora GNOME Wayland), macOS, Windows

## Installation

### From Pre-built Zip
1. Download the latest `Fixed-Editor-Width-x.x.x.zip` from the [Releases](https://github.com/aaalest/pycharm-fixed-split-plugin/releases) tab.
2. Open PyCharm / IntelliJ IDEA.
3. Navigate to **Settings** (`Ctrl + Alt + S`) -> **Plugins**.
4. Click the gear icon (⚙️) -> **Install Plugin from Disk...**.
5. Select the downloaded `.zip` file and restart the IDE.

### Build from Source
```bash
git clone [https://github.com/aaalest/pycharm-fixed-split-plugin.git](https://github.com/aaalest/pycharm-fixed-split-plugin.git)
cd pycharm-fixed-split-plugin
./gradlew buildPlugin
```

The distributable .zip file will be generated in build/distributions/.
