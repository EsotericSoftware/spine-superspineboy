![Super Spineboy](http://i.imgur.com/6jMhdeU.jpg)

Super Spineboy is a platformer game for Windows, Mac and Linux written using [Spine](http://esotericsoftware.com/) and [spine-libgdx](https://github.com/EsotericSoftware/spine-runtimes/tree/master/spine-libgdx). Spine is a 2D animation tool specifically for games and Super Spineboy shows some of the ways to make use of Spine skeletons and animations in an actual game.

[![](http://i.imgur.com/7QMVJmt.png)](https://www.youtube.com/watch?v=zAZ_PxxEgDI)

## Download

Super Spineboy can be [downloaded](http://esotericsoftware.com/files/runtimes/superSpineboy.jar) in binary form and run on Windows, Mac or Linux. Java 1.6+ is required. To run Super Spineboy, double click the `superSpineboy.jar` file or run it from the command line:

```
java -jar superSpineboy.jar
```

## Controls

* Left click shoots toward the mouse position. Hold to keep shooting.
* `A` is left, `D` is right, `W` is jump.
* Alternatively, `left arrow` is left, `right arrow` is right, `space` is jump.
* Press `alt + enter` or click `Fullscreen` in the menu to run the game fullscreen.
* Press the number keys `1` through `6` to control the game speed, allowing you to see how smooth the animations are and the transitions between animations.
* Press `tilda` or `P` to pause.
* Press `Z` to zoom in/out so you can see the animations more easily.

## Gameplay tips

You may find Super Spineboy difficult at first. The game is relatively short, so the difficulty ramps up quickly. These tips may help you overcome the hordes!

* Don't move through further into the level until you've killed all the enemies you find.
* Spineboy's weapon shoots very fast, but suffers from reduced accuracy when shot continuously. Cease firing momentarily to regain accuracy. 
* You cannot shoot backward when running away, but you can jump while running away and shoot backward in the air.
* Standing your ground and mowing down enemies is great, but there are quickly so many enemies that you get overrun. When this happens, goomba head stomp the enemies. This is key to winning!
* Getting sandwiched between two groups of enemies is a sure way to die. Head stomp your way to one side so you aren't surrounded.
* If the game runs poorly, try unchecking `Background` in the menu.

## Source

Super Spineboy is written in Java and uses OpenGL, [libgdx](http://libgdx.badlogicgames.com/), [Spine](http://esotericsoftware.com/) and [spine-libgdx](https://github.com/EsotericSoftware/spine-runtimes/tree/master/spine-libgdx). A loose [MVC](http://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller) design pattern is used for code organization. This makes for a clean separation between the game logic and what is drawn.

The Super Spineboy source can be downloaded from GitHub using a Git client or as a [zip file](https://github.com/EsotericSoftware/spine-superspineboy/archive/master.zip). To run from source using Eclipse, click `File` -> `Import` -> `Existing projects`.

## License

Super Spineboy is licensed under the [Spine Runtime License](https://github.com/EsotericSoftware/spine-superspineboy/blob/master/LICENSE). Please see the license for full details, but this basically means that if you license [Spine](http://esotericsoftware.com/), you can create derivative works or otherwise use Super Spineboy however you like.

## Screenshots

![](http://i.imgur.com/TQi1qXB.png)

![](http://i.imgur.com/j3RwiU7.png)

![](http://i.imgur.com/Y3uAOSj.png)
