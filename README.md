# Cobblemon

This repository is a fork of [Cobblemon](https://gitlab.com/cable-mc/cobblemon), published under the Mozilla Public License 2.0. All original contributors are credited via Git history.

## Getting started

To set up the development workspace, first clone the project and open the build.gradle with Intellij. Make sure that you clone it to a folder that has no spaces in its path (for example, C:/Development/Cobblemon Stuff/cobblemon/ is bad) since Architectury Plugin seems to dislike it.

After it takes ages to load, you should hopefully have runnable configurations of the project in the top right, such as Minecraft Client (:fabric). If not, try running `./gradlew genEclipseRuns`.

Troubleshooting:
- Try running `./gradlew --refresh-dependencies`
- Try File -> Invalidate Caches.
- Try deleting the `.idea` folder in the project root (make sure IntelliJ is closed when you try it).
- Try completely reclone the thing lmao.

## Is just a fork repo
