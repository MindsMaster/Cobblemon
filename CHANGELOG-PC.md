### Additions
- Added ability to rename PC Boxes by clicking on the name of a box
- Added ability to set wallpapers per PC Box by clicking the button right to the box name
- Added sorting by Name, Level and type by clicking the button left of the box name (shift reverses order)
- Added filtering/search functionality in PC UI, which also supports pokemon properties (e.g. shiny=yes shows all shinies)
- Added /changewallpaper <player> <boxNumber> <wallpaper> command to change a wallpaper through commands
- Added /renamebox <player> <boxNumber> <name> command to rename a PC box through commands

### Developer
- Added RenamePCBoxEvent.Pre/Post events to prevent players from renaming a box or change their input
- Added ChangePCBoxWallpaperEvent.Pre/Post to prevent players from changing to a wallpaper or change their selection
- Added WallpaperCollectionEvent which gets called when clients connect to a server, allowing the server which of the clients found wallpapers it's allowed to move (collected wallpapers can be removed for example to make it "vanish" client-side)
- Added WallpaperUnlockedEvent.
- Renamed `SetPCBoxPokemonPacket` and the respective handler to `SetPCBoxPacket`

### Addons
- Added Flows for `player_tick_pre`, `player_tick_post`, `advancement_earned`, `right_clicked_entity`, `right_clicked_block`, `player_died`, `wallpaper_unlocked`.
- Flows now have `q.` for any context properties in addition to the `c.` properties, making it a bit more convenient when calling other scripts from Flows.
- Added `unlockable_pc_box_wallpapers` datapack folder for wallpapers that need to be unlocked before they're usable.
- Wallpapers are loaded from `assets/<namespace>/textures/gui/pc/wallpaper/` and all wallpapers in this folder are available to the client to  choose by default
