### 19 Aug 2018
With the release of 0.8.45 you are able to...
-  ...directly set the render mode (via GUI toolbar and shortkeys, Alt + NUMPAD + 1 to 9)
-  ...set the viewport perspective (via GUI toolbar and shortkeys, Ctrl + NUMPAD + 8,4,5,6,2,0)
-  ...close the view (via GUI toolbar and shortkey, Q)

The following critical issues are fixed:
1. The vertex window stayed on top of all open programs.
2. [OpenGL 2.0 only] TEXMAP with transparency did not show the expected result.


### 04 Jul 2018
With the release of 0.8.44b the following critical issues are fixed:
1. Clicking on the tabs no longer changed the 3D view. / Scrolling in the text editor didn't refresh line numbers.


### 01 Jul 2018
With the release of 0.8.44 you are able to...
-  ...translate the 3D view by just moving the cursor to the border of the 3D view (open the options menu to enable this).
-  ...use a vertex window in the top right corner to show/modify the x, y, z values of the selected vertex.
-  ...see the transformation delta info placed right after the cursor coordinates on the status bar.
-  ...benefit from an enhanced "BFC + TEXMAP / Materials" render mode (OpenGL 3.3 only)

The following critical issues are fixed:
1. The context menu button was invisible when rulers were shown on the 3D view.


### 08 Feb 2018
With the release of 0.8.43 you are able to...
-  ...check an option during setup which associates *.dat files to LDPartEditor.
-  ...benefit from an enhanced surface creation by Lines2Pattern.
-  ...see the poles correctly on !TEXMAP SPHERICAL.

The following critical issues are fixed:
1. Ytruder still generated triangles sometimes when it should generate quads.
2. Switching the manipulator via keyboard (translate, rotate, scale) "cleared" the current transformation.
3. Selecting a color for the font in Txt2dat did not work. Letters were always rendered in black.
4. Fixed a rare NullPointerException which occured while closing the application.


### 23 Jan 2018
With the release of 0.8.42 you are able to...
-  ...to open *.dat files with LDPartEditor from the Windows file explorer ("Open with..." and select LDPartEditor.exe).

The following critical issues are fixed:
1. Fixed a rare but critical undo/redo bug.
2. Fixed a synchronization bug between the text and 3D editor.
3. Fixed a bug which could freeze the text editor.


### 11 Jan 2018
With the release of **0.8.41** you are able to...
-  ...launch an LDPartEditor executable under Windows / install it with a setup file.

The following critical issues are fixed:
1. Colour table (OS: Windows) - The scroll on mouse over, in the list view, only worked when the pointer was over the slider. Not when it was over the list.
2. The primitive area disappeared sometimes, because a paint event cleared the canvas.


### 04 Jan 2018
With the release of **0.8.40** you are able to...
-  ...unscale something without losing current origin/orientation (double click on selected subfile -> hold ctrl and click on "Move selected subfile to manipulator").
-  ..."Move selected subfile to manipulator" and it keeps subfile scaling when doing so.
-  ...choose more practical icon sizes (range 12px ... 32px).
-  ...benefit from more "clean" tabs (Snap/Selection/BG-Image) on the 3D editor.
-  ...benefit from little performance improvements.

The following critical issues are fixed:
1. Unificator deleted some surfaces from the unified mesh.
2. Edger2 created wrong condlines if there is a slighty mismatch. 
3. "Move Manipulator to Subfile" warped the manipulator if there was shearing involved.
4. Colour table, linux-only: The scroll on mouse over, in the list view, only worked when the pointer was over the slider. Not when it was over the list.
5. It was possible to activate "Move Adjacent Data" on the selection tab for subfiles, but it has no effect (it should not be possible to activate the button in this case!)


### 29 Dec 2017
With the release of **0.8.39** you are able to...
-  ...use a SlantingMatrixProjector tool.
-  ...save all your user settings in a file, and restore it when you want to.
-  ...restore your 3D view settings when you completed a PartReview.
-  ...use ALT + UP/DOWN to move a line up and down in the text editor.
-  ...see a progress bar when downloading files from the PT for PartReview.
-  ...set the coplanarity threshold (Tools... -> Options...).

The following critical issues are fixed:
1. Colour palette: The window was sometimes not wide enough.
2. Sometimes long edges were hard to select until both vertices were visible.
3. The logger did not detect errors while loading the workbench.
4. An edge, or cond-line was selectable through a surface, even if it was hidden behind that surface.
5. Ytruder generated triangles when it should generate quads.
6. "Save As..." created a wrong part type (Flexible_Section).
7. Divide by zero during Catmull-Clark subdivision


### 1 Dec 2017
With the release of **0.8.38b** the following critical issues are fixed:
1. The CSG mesh optimisation was deactivated when one of the 3D view had the wireframe render mode enabled.
2. Under linux mint it was not possible to rotate/translate the 3D view with the keyboard key (default keys: "M"/"L").


### 30 Nov 2017
With the release of **0.8.38** you are able to...
-  ...use a brand new CSG engine (with automatic mesh optimisation, its on by default)
-  ...remove the "UPDATE" info from the part type line with a quick fix.
-  ...add the "Unofficial_" prefix to the part type line with a quick fix.
-  ...use Isecalc on the current selection by default.
-  ...benefit from the fact that "Move Adjacent Data" gets restored on start.
-  ...use the new part type "**Part Flexible_Section**".
-  ...see a pop-up dialog with progress bar + cancel for "Calculate Line Intersection Points".
-  ...use a "Coplanarity Heatmap Mode" to visually identify coplanar quads.
-  ...see some info that the CSG optimisation will only work if the file is displayed on the 3D editor.
-  ...turn the new CSG mesh optimisation off (with the meta command **0 !LPE CSG_DONT_OPTIMISE**)
-  ...set the optimisation threshold of the CSG edge collabser (with the meta command **0 !LPE CSG_EDGE_COLLAPSE_EPSILON 0.9999**. The default value is 0.9999.  Please use higher or unlikely lower values greater zero and lower one only if necessary.)
-  ...set the optimisation threshold of the CSG T-junction finder (with the meta command **0 !LPE CSG_TJUNCTION_EPSILON 0.1**. The default value is 0.1 LDU. Please use lower or unlikely higher values greater zero only if necessary.)

The following critical issues are fixed:
1. MeshReducer did a wrong check for common points.
2. The label for hints, warnings, etc. was cut off.
3. TJunctionFinder was not able to fix T-junctions automatically
4. When you double-clicked on the last word in the last line, the last character of the file was not selected.
5. The visible line size for CSG selections was not always correct.
6. Subdivision silently removed all condlines (now it warns about this!)
7. The quick fix for missing part type didn't remove the truncated "0 !LDRAW_ORG" meta command.
8. "Radio buttons" were not highlighted on the grid icons and in the Ytruder tool dialog.
9. Rare NullPointerException during a double click on the text editor problem tree.


### 30 Aug 2017
With the release of **0.8.37** the following critical issues are fixed:
1. Edger2 created duplicated TYPE 2 lines which were hard to spot.
2. The primitive text search field was sometimes failing / sometimes primitives were not shown at all.
3. Edger2 only created condlines if the adjacent surfaces vertices matched perfectly.
4. Undo/Redo did not work with the CSG_EXTRUDE meta command.
5. CSG inlining did not use all CPU cores.
6. The new numeric input fields were blocking the common thread pool (no crash, only performance degradation).
7. There was an endless loop regarding the new numeric input fields (no crash, only performance degradation).


### 18 Aug 2017
With the release of **0.8.36b** you are able to...
-  ...benefit from more eye-catching buttons.

The following critical issues are fixed:
1. The coplanarity calculation was incomplete


### 14 Aug 2017
With the release of **0.8.36** you are able to...
-  ...select (and see in 3D view) elements producing an error or warning (when meaningful).
-  ...benefit from improved usability for the "hover over right click menu-icon" in the 3D view. 
-  ...benefit from the fact that vertices which are close together are considered "as one" (when you add something; dist < threshold in LDU).
-  ...customise the 3D distance below which vertices are considered the same when you add new elements.
-  ...customise the 2D distance in pixels below which vertices are considered the same when you add new elements.
-  ...see a warning if the value of a DecimalSpinner differs from the displayed value.
-  ...drag&drop primitives on the text editor.
-  ..."Select Connected..." / "Touching..." "...with same type."
-  ...notice that closing a tab in the text editor close it also in the 3D editor, when the 3D and text editors share one window.
-  ...select objects, regardless of the selection filter (but there are some special exceptions).

The following critical issues are fixed:
1. Rectifier: There was no difference in the result on the fourth filter. "Convert if possible/Do not convert if adjacent cond-line."
2. Text-Editor: The cursor jumped to the first line when the file was saved / the Save-button "stayed" in 'down'-position (actually, it was focused).
3. The 3D editor placed the tab of a newly opened part on the left of an already opened part, while in the text editor it was placed as expected on the right.
4. Selection issues with vertices which were close together on the screen.


### 5 Aug 2017
With the release of **0.8.35** you are able to...
-  ...benefit from better decimal spinner widgets.
-  ...make use of a "Select All Types" and a "Select Nothing" menu item along with Vertices, Lines, Triangles, etc. 
-  ...use "Show All" in the context menu from the text editor
-  ...use a shortkey to swap the BFC winding of a selection ("J" key).
-  ...see where the distance meter starts.
-  ...see where the protractor is fixed.
-  ...use the selection filter to control the element types which are inserted by copy & paste
-  ...use the selection filter to control which element types are selectable. 
-  ...see tooltips which explain that ALT+Click removes the corresponding object type from selection when you activate Vertex/Surface/Line/Subfile Mode
-  ...benefit from better performance if you have a large number of hidden objects.
-  ...benefit from the fact that the primitive area is reset to the top left corner on load.

The following critical issues are fixed:
1. Selection got cleared while using the subfile mode. 
2. Wrong number format for the "Stud" in the unit converter.
3. Snap on edges: The projection was incorrect.


### 20 Jul 2017
With the release of **0.8.34** you are able to...
-  ...see some information for the number of currently selected parts in the status bar.
-  ...copy protractor and distance meter values in the clipboard. 
-  ...hide/unhide things from the text editor.
-  ...see (in text editor) which lines are currently hidden.
-  ...use the shortkey "B" to toggle "Move Adjacent Data"
-  ...control if the mesh reducer tool should not destroy patterns.
-  ..."Draw Only the Selection" (text editor context menu) 
-  ..."Draw Until Selection" (text editor context menu) 

The following critical issues are fixed:
1. The undo/redo feature for hidden and shown elements was not correct.


### 24 Jun 2017 (first official MacOS X release)
With the release of **0.8.33** you are able to...
-  ...use LDPartEditor under Mac OS X.
-  ...move the 3D view with Alt+Cursor Keys
-  ...enable a verbose mode for Rectifier
-  ...enable a verbose mode for Edger2
-  ...specify the number of iterations for the "Rotate Dialog"
-  ...specify the number of iterations for the "Translate Dialog"
-  ...copy and transform a selection if you hold Ctrl and modify the manipulator with the cursor keys
-  ...benefit from an absolute scaling mode which uses a length in LDU as an initial scale.

The following critical issues are fixed:
1. Ctrl+Z with focus on 3D window shifted the focus to the text window.
2. A mouse click was sometimes ignored (while adding something in the 3D view)
3. Unificator: A vertex already snapped on a primitive vertex was able to move to another primitive.
4. Minor bug: The primitive area renderer tried to render after the 3D window was closed.


### 18 Jun 2017
With the release of **0.8.32** you are able to...
-  ...see the distance traveled/angle rotated in the status bar while using the move or rotate tool.
-  ...access the manipulator features from a sub menu in the 3D editor context menu.
-  ...see a tooltip for view rotation in the primitive area.
-  ...move the Manipulator with left/right or up/down arrows on keyboard.
-  ...remove all "0 // Inlined:" notices with a "quick fix".
-  ...download/update the "ldconfig.ldr" file from LDraw.org.
-  ...download/update the "categories.txt" file from LDraw.org.
-  ...benefit from much faster undo/redo.
-  ...benefit fom a faster program start.
-  ...benefit from the fact that when using the translate/scale/set etc actions, it displays the global axis, if global mode is active.

The following critical issues are fixed:
1. Sometimes, deselected elements were re-selected when selecting a subpart.
2. "Save As..." did not trigger a check for warnings / hints / errors
3. "LDConfig.ldr" / "ldconfig.ldr" issues on case sensitive file systems.
4. LDPE false reported that the single !HELP meta command was "split apart"
5. Sometimes, the UI freezed permanently under Linux.
6. Implementation: Little resource leak with org.eclipse.swt.graphics.Cursor instances


### 8 Apr 2017
With the release of **0.8.31** you are able to...
-  ...delete a !LPE VERTEX without "Move Adjacent Data" being on.
-  ...see the selection of !LPE VERTEX meta commands (3D -> text).
-  ..."Show Selection In 3D View" of a !LPE VERTEX line in text editor.
-  ..."Show Selection In Text Editor" of a !LPE VERTEX line in 3D view.
-  ...add a !HISTORY line quickly in the text editor (with Ctrl+H).
-  ...add a !KEYWORDS line quickly in the text editor (with Ctrl+K).

The following critical issues are fixed:
1. YTruder accidentally deleted the selected lines.
2. YTruder was not able to do symmetry or projection on planes with value=0
3. Toggle comment / !TEXMAP freezed the text editor on empty lines.
4. SyncEdit did not mark the !LPE VERTEX meta command.


### 2 Apr 2017
With the release of **0.8.30** you are able to...
-  ...use a keyboard shortcut (F) to Flip/Rotate vertices.
-  ...use Ytruder.
-  ...see that the menu bar tools button became a normal sized "Tools..." button (was a tiny arrow).
-  ...see the triples of "protractor" and "distance" as well as the color digit colored.

The following critical issues are fixed:
1. It was not possible to set the length of the protractor.
2. "Flip/rotate vertices" transformed protractors into triangles and distance meters into lines. 
3. Wrong transparency calculation for transparent PNG textures (!TEXMAP)
4. One search path was missing (!TEXMAP parser)
5. Wrong "Circular reference" error on subfiles in combination with !TEXMAP
6. All axes were enabled if you used the "Translate..." feature (X/Y/Z)


### 1 Mar 2017
With the release of **0.8.29** you are able to...
-  ...use a tool that creates a vertex on line intersection.
-  ...use a "Create Copy" button for the transformation tools (translate / rotate / scale selection / set X,Y,Z)
-  ...see the manipulator while using the transformation tools (translate / rotate / scale selection / set X,Y,Z)
-  ...use "Set x/y/z" to set the origin of subfiles and primitives to x/y/z
-  ...set the translation to the manipulator position (translate selection)
-  ...benefit from the fact that colour changes to the main colour are updated after leaving the options menu. 
-  ...benefit from the fact that the dialog for local rotation and scale transformations now initialises the pivot point with the manipulator position.

The following critical issues are fixed:
1. "Moved to" reference resolve function for subfiles, 8- and 48-primitives was incorrect.
2. "More Colours..." did not work after the colour palette was reloaded.


### 17 Feb 2017
With the release of **0.8.28** you are able to...
-  ...override colour 16. 
-  ...set the distance meter length in the selection window.
-  ...constrain "Merge to Nearest..." to operate in a fixed direction (X/Y/Z).
-  ...choose between world axis X/Y/Z and manipulator axis X/Y/Z for transformation tools (translate / rotate / scale selection / set selection X/Y/Z)

The following critical issues are fixed:
1. The colour value for colour 16 was not read from LDConfig.ldr
2. If you split a distance meter you got two normal edge lines instead of two distance meters.
3. LDPE did not complain about some duplicated lines (TYPE 2, 3, and 4).
4. "Local" mode for the distance meter showed sometimes wrong values for dx / dy / dz


### 31 Dec 2016
With the release of **0.8.27** you are able to...
- ...choose whether or not "Merge to Nearest Edge" splits the edge on merged vertices.
- ...limit unrectifier to only inline rect primitives (hold Ctrl and press the unrectifier button).
- ...adjust the direction of the manipulator to the direction of an edge. I changed the behavior of "Move Manipulator to the Nearest Edge"!

The following critical issues are fixed:
1. Fixed an endless loop on the first start (affected 0.8.26 only).
2. Rectifier added the same edge line into two adjacent rectangle primitives.
3. Toggle comment / TEXMAP didn't adjust the text selection. 
4. The new render engine is now optional. Its automatic hardware detection was broken.
5. When you inserted a protractor, the order of the vertex triplet was not deterministic.
6. Unrectifier inserted an extraneous empty lines where rects were previously.
7. The quick fix of "invalid use of 'BFC INVERTNEXT' / Flat subfile" only removed the BFC INVERTNEXT statement. It should also invert the direction of the flat subfile in flat direction to keep the same BFC winding.


### 20 Nov 2016
With the release of **0.8.26** you are able to...
-  ...benefit from a brand new render engine (thanks to the power of OpenGL 3.3, which makes better use of the GPU).
-  ...activate "Smooth Shading" (requires the new render engine).
-  ...benefit from a faster program start.
-  ...benefit from better performance for "Show All".

The following critical issues are fixed:
1. Toggle comment / TEXMAP didn't adjust the text selection.
2. Sometimes a time-consuming selection with the selection rectangle did select nothing.
3. Background Image: "Previous" and "Next" buttons skipped images.
4. Edger2 wrongly added condlines in the middle of slightly warped quads
5. Edger2 put condlines/edge lines between surfaces and... protractors
6. Double click doesn't select the last character of a value if it was at end of line
7. !TEXMAP PLANAR: Wrong winding for CCW faces (with INVERTNEXT)
8. Fixed a rare exception regarding directory I/O


### 27 Sep 2016
With the release of **0.8.25** you are able to...
-  ...double click on a decimal number to select the whole value, including integer part, decimal part, decimal separator and minus sign if any (Text Editor).
- ...see that all three coordinates are unchecked when the coordinate dialog opens. The coordinates where you type in a value will become active (3D Editor, Merge/Split -> Set X/Y/Z).
- ...benefit a little from a faster draw method for lines and condlines.

The following critical issues are fixed:
1. Flipper: Condlines were "not recalculated" so their control points were wrong after flipping.
2. The calculation for quad (and triangle) collinearity detection was wrong
3. Last added elements were not saved (3D Editor).
4. Wrong winding colour in some rare cases (CW: clockwise winding only)
5. Merge/Split -> Set X/Y/Z did not work sometimes
6. A ConcurrentModificationException occured in the process which hides condline control points
7. A ConcurrentModificationException occured during normal calculation for the LDraw Standard Mode
8. There were non-threadsafe method calls within the logger class.


### 3 Sep 2016 (last 32-bit release)
With the release of **0.8.24** you are able to...
- ...use per-component rounding (X, Y, Z) instead of the per-vertex rounding (useful for patterns on slopes).
- ...use the metadata dialog (AKA "header dialog") on the text editor, too. 
- ...benefit from a faster program start.

The following critical issues are fixed:
1. It was not possible to activate "Create a new conditional line..." via a shortkey.
2. The new LDU to stud converter did not round correctly (20 LDU = 1 stud, rounded to one decimal place)
3. The Merge/split->Set X,Y,Z window appears only with a single vertex selection, it no longer works on a multiple selection (eg. to snap a selection on a plane).
4. The error message "Invalid use of 'BFC INVERTNEXT' / Flat subfile" got duplicated.


### 29 Aug 2016
With the release of **0.8.23** you are able to...
-  ...convert a unit to stud (20 LDU = 1.0 stud, rounded to one decimal place).
-  ...access all context menu features from somewhere else, too.
-  ...use the "Expand Area" buttons to "toggle" the divider position between the text and the 3d editor.
-  ...use more dynamic context menus.
-  ...benefit from the fact that there are bigger previews for the primitive area by default.
-  ...benefit from the fact that "Text on the right / 3D on the left" is now the default window setting. 
-  ...see the stud logos on the upper left view when you use the PartReview tool.
-  ...benefit from an asynchronous file header check (performance improvement / preparations for more validation features).

The following critical issues are fixed:
1. Deleting all text lines from a file with the text editor could sometimes freeze LDPE.
2. The PartReview tool did not close+reload already opened "duplicated" files.
3. When PrimGen2 opened the file in the 3D editor, the corresponding file tab was not selected.
4. The menu key (from the keyboard) did not work on the file tree.
5. The menu key (from the keyboard) did not work on the 3D view.
6. Wrong singular / plural use for the word "duplicate".


### 19 Aug 2016
With the release of **0.8.22** you are able to...
- ...use a "new" tool: A PrimGen2 clone is now added to the list of tools.
- ...choose whether Intersector should hide things or not (default is not to hide).
- ...benefit from "realtime" asynchronous identical line detection. LDPE detects now duplicates during input with very low latency.
- ...benefit from a better "Save As..." implementation. It shows a warning if a file is going to be overwritten.
- ...set the thickness of the lines (type 2 or 5) to zero.
- ...to see your LDraw username / real name on the header dialog ("Add a comment or header entry...").
- ...benefit from the fact that the position of the divider between the text and the 3D editor gets restored on start.
- ...use buttons to quickly "fullscreen" either the text or 3D editor and a third icon which restores the divided view.
- ...use buttons to maximise / rearrange the area on the left side of the 3D view (selection, snapping, part tree, primitive area).
- ...set the second line for an angle protractor to a defined length.

The following critical issues are fixed:
1. "Toggle Comment" / "Toggle !TEXMAP" caused data corruption when it was toggled on the last line of a file.
2. The "Selection:" tab was broken (it was not possible to edit lines, distance meters and subfile references)
3. You were not able to open a file in the text editor sometimes (on separate window mode).
4. Wrong line width was used in "Special Condline Mode" / "Random Colour Mode".
5. When you opened a file in the 3D editor sometimes it was not added to the recent file list.


### 6 Aug 2016
With the release of **0.8.21** you are able to...
-  ...set the second line in a angle protractor to a defined angle (with the selection tab).
-  ...decide when "Move Adjacent Data" will be deactivated (new option).
-  ...orientate the manipulator relative to the vertex location.
-  ...set the vertex position to the position from a clipboard vertex.
-  ...set the vertex position to the manipulator position.
-  ...remove the target type from the selection (switch the object type while holding  Alt). 
-  ...deselect an object with the selection rectangle (while holding Ctrl+Alt). 
-  ...benefit from more digits on some important numerical fields.

The following critical issues are fixed:
1. Issues regarding "Move Adjacent Data".
2. The red close cross on the tabs in the 3D editor did not close the correct tab sometimes.
3. It was not possible to measure an existing edge line / triangle because LDPE prevented the creation of a distance meter / protractor on top of an edge line / triangle.
4. Various usability bugs with "Open Part File" / "Save as..."
5. Drag&Drop a file to the text editor opened the file in the 3D window, too (when sync. tabs was off). 
6. Drag&Drop a file to the text editor created new superflous tabs when a revert was cancelled.
7. "New Part File" from the text editor opened the file in the 3D window, too (when sync. tabs was off). 
8. A change to the colour palette deleted the palette separator all editor windows. 
9. "Open Part File" from the text editor opened the file in the 3D window, too (when sync. tabs was off). 
10. "Save As..." from the text editor opened the file in the 3D window, too (when sync. tabs was off). 


### 30 Jul 2016
With the release of **0.8.20** you are able to...
- ...use the 3D editor and the text editor in one window. You can enable this under "Options...->Misc. Options->Text and 3D editor arrangement". It needs a restart of the application.
- ...use the TJunctionFinder to just "find" possible T-junctions without changing the mesh.
- ...deselect objects only with Ctrl+Click (Ctrl+Marquee is not possible anymore).
- ...add objects to the selection (Ctrl is pressed and you use the selection marquee or Ctrl+Click)
- ...benefit from the fact that the existing selection is cleared if nothing was selected and Ctrl was not pressed.
- ...find the "Last opened Files/Projects" on the "New Part - Open Part - ..." toolbar
- ...benefit from the fact that empty text editor windows are populated with a new tab instead of creating a new text editor window.
- ...restore the complete viewport state for different files. Each file has now its own 3D viewport configuration. Open file A, activate "Random Colours" and you switch to file B to do something different without "Random Colours". If you switch back to file A, "Random Colours" are activated again. However, this automatic feature is not limited to random colours. It includes every 3D viewport setting.
- ..."group" a selection. Select non-subfile content in the 3D editor. Open the context menu over the 3D view and select "Join selection (Text Editor)" to bring together what belongs together!
- ...see a warning/info, when "Move Adjacent Data" is on (for translate/rotate/scale).
- ...benefit from the fact that "Quick Fix Similar" for vertex declarations deletes other vertices (!LPE VERTEX), too. 

The following critical issues are fixed:
1. SyncEdit: The cursor jumped ahead after I tried to edit a vertex 
2. Switching the tabs in the 3D editor modified the last visited file location. 
3. Some issues with the hint/warning/error tree.
4. Pasting something does not disable "Move Adjacent Data".
5. The selection highlight feature in the text editor was sometimes not synchronised with the 3D view.


### 23 Jul 2016
With the release of **0.8.19** you are able to...
-  ...use tabs in the 3D editor along with the tree on the left side.
-  ...use a button to sync. the tab selection from tabs in the 3D editor with the text editor.
-  ...see a dialog when you try to re-load an unsaved file again.
-  ...smooth a set of vertices (with realtime preview)
-  ...access the colour palette functions (load / save / reset) from a sub-menu.
-  ...benefit from little performance improvements / refactoring.

The following critical issues are fixed:
1. [Recurring] "Conditional Control Point Vertices" caused flickering on multiple views.
2. Annoying mouse issues with the manipulator. 
3. The selection highlight feature in the text editor was sometimes not synchronised with the 3D view.
4. [CSG] Rotating a part at 90 degree: It got undesired 0.0 digits.


### 12 Jul 2016
With the release of **0.8.18** you are able to...
- ...trigger "Show selection in Text Editor" from a button on the GUI of the 3D editor.
- ...benefit from the fact that selecting "Show Selection In Text Editor" jumps to the _next_ selected line.
- ...benefit from the fact that a change to the colour palette is updated instantly on all editor windows.
- ...customize the colours of the selection cross (3D editor) 

The following critical issues are fixed:
1. Wrong BFC rendering for primitives (in the primitve area)
2. TJunctionFinder eliminates lines at random (normal mode)
3. The "(!)" sign is set for (part) files which are created by the PartReview tool. This is not desired. (the "(!)" sign indicates that the file is located outside the project or the library structure of the project)


### 6 Jul 2016
With the release of **0.8.17** you are able to...
-  ...set the maximum amount of custom colours.
-  ...export the colour palette (*_pal.dat)
-  ...import the colour palette (*_pal.dat)
-  ...reset the colour palette.
-  ...see only the 3D editor on start / "new.dat" is virtual and not a critical "unsaved" file.
-  ...benefit from more parser magic: a BFC INVERTNEXT placed before a flat primitive triggers a warning with a quick fix. 
-  ...benefit from more digits for the position/scale/rotate/translate entry fields.

The following critical issues are fixed:
1. Problem with selection: selection menu shows checkboxes for line/triangle... etc. They should act as filters, but if you uncheck "lines", select a triangle, and use "select connected" the lines connected are selected, too.
2. Flipper: Condlines are "not recalculated" so their control points are wrong after flipping.
3. If you hide a primitive preceded by an INVERTNEXT, this INVERTNEXT remains active and inverts all elements until an other primitive is encountered.
4. Select the last line in text editor by clicking on line number, and if that last line has no CRLF behind, then the last two characters of the line are not selected.


### 29 Jun 2016
With the release of **0.8.16** you are able to...
-  ...benefit from automatically deselection of a construction tool (create quad/line/triangle...), if you select a modification tool (select/move/rotate etc...).
- ...use a shortkey to move the manipulator to the selection center (the "A" key).
- ...use linear interpolation for a complete vertex selection with the "Move On Line" feature

The following critical issues are fixed:
1. Invalid parsing of 0 BFC INVERTNEXT (the parser implementation was wrong)
2. "Conditional Control Point Vertices" caused flickering on multiple views.
3. The behavior of automatically selecting as well the neighboring line when clicking in the line numbers bar was incorrect.
4. Flipping two triangles was only possible with a different triangle colour than colour 16.
5. While are able to select a surface by clicking on it, even if only a small portion is visible, you were able to select only lines/condlines if BOTH end vertices were visible.
6. Rounding issues (NullPointerException)
7. Helper function issues with vertices and their relationship to LDraw data (NullPointerException)
8. "Hide & Show" issues (NullPointerException)
9. Clipboard issues (NullPointerException)
10. Colour change issues (NullPointerException)
11. Cut and delete issues (NullPointerException)
12. The subfile compiler meta command (" 0 !LPE INLINE") created sometimes a new virtual part file, but it did not remove the old one!
13. There was a synchronisation bug regarding the 3D manipulator.
14. When you mirrored a selection, the winding of triangle/quads was correctly inverted. But primitives in selection were not BFC inverted!


### 17 May 2016
With the release of **0.8.15** you are able to...
- ...randomise the colours for a selection.
- ...benefit from a little bit better 3D render performance.
- ...see the version number in the window title from the 3D editor. 

The following critical issues are fixed:
1. It was not possible to change the colour of CSG objects (3D editor)
2. After closing a file in the 3D editor with "Close" in the parts tree window and actually vanishing from the 3D and Text editor it cannot be deleted in the explorer telling that the file is open in the SE java platform.
3. Fixed a NullPointerException which occurred during the transformation of CSG bodies (3D editor only)
4. Fixed a NullPointerException which occurred during the colour change for CSG bodies (3D editor only)
5. The QuickFix for the part description transformed the first line into a comment.
6. Line-type 1 Syntax Highlighting: Wrong yellow underline for files which were found on the hard-disk. 


### 13 May 2016
The following critical issues are fixed:
1. The PartReview progress monitor did not update the status info.
2. "Split View Horizontally / Vertically" did not work correct with the new sync. feature (Zoom / Manipulator / Translation).
3. "Intersector" did not hide primitives.
4. The LDU to Inch factor was not correct (was 0.015625, but should be 0.015748).
5. Released translation / property files for the German language version too early.
6. MeshReducer did not always terminate automatically (but the manual "Cancel" button worked).
7. SWTException: Widget is Disposed, when inserted something.
8. Transform selection (Subfiles) threw a NullPointerException.
9. A colour change (Subfiles) threw a NullPointerException.
10. The OpenGL line width for primitives was not constant (Primitive Area).
11. After I used SymSplitter to get half of a file, and then used the Select ...All Shown, all edges, tris and quads in hidden primitives and subfiles were selected too.
12. "Drag & Drop" with primitives led to an event overflow, which slowed down the OpenGL render engine.
13. Open *.dat File (3D Editor) threw a NullPointerException.
14. The Subfiler didn't include and move comments. They're left behind in the main file. The structure of the file was lost.


### 9 May 2016
With the release of **0.8.13** you are able to...
-  ...use a new tabbed "Add Metadata" window.
-  ...benefit from better "Sort" performance.
-  ...configure all the settings from the "Welcome Dialog" in the "Options Dialog", too.
-  ...select a line of code by clicking on the line number in the text editor (it is possible to extend the selection while pressing Ctrl.)
-  ...synchronise the manipulator between standard perspectives.
-  ...synchronise the translation of the view between standard perspectives.
-  ...synchronise the zoom level between standard perspectives.

The following critical issues are fixed:
1. "Copy" did throw an exception in some rare cases
2. The generated file name for downloaded files was not correct (PartReview tool)


### 30 Apr 2016
With the release of **0.8.12** you are able to...
- ...use the new "PartReview" tool. Enter a part name and start a review in no-time!
- ...use the new "Decolour" function to quickly remove all colours from a file.
- ...use the new !LPE CSG_TRANSFORM meta command to transform and clone existing CSG bodies.
- ...use the new !LPE CSG_MESH meta command to convert the following LDraw triangles and quads (type 3 and 4) to a CSG mesh.
- ...use the new !LPE CSG_EXTRUDE meta command to generate a CSG mesh from the following PathTruder input lines (type 2).
- ...adjust the rotation center of the view by double clicking on CSG surfaces.
- ...benefit from better performance of the MeshReducer tool.
- ...benefit from better performance of "Round" (Text Editor only).
- ...benefit from better (but slower) CSG triangulation.

The following critical issues are fixed:
1. Mirroring now correctly preserves the surface winding.
2. The protractor is now drawn without lighting.
3. A selection conflict with protractors and triangles is resolved.
4. Issues with "Select All", "Select Same Colour" in combination with CSG are gone.


### 20 Apr 2016
With the release of **0.8.11b** the following critical issues are fixed:
1. **Critical:** Broken triangle selection.


### 20 Apr 2016
With the release of 0.8.11 (and also 0.8.11b) you are able to...
- ...to manipulate the CSG bodies in the same way as the sub-parts (supports also colour changes, "Select All", "Select all with Same Colours" and "Select None", global and local transformation).
- ...to quickly convert selected triangles into quads.
- ...to create and use angle protractors for 3 points.
- ...to create and use distance meters between 2 points (distances depend on local or global manipulator settings).
- ...use a little unit converter on the 3D editor window (located at the snapping area).
- ...to benefit from better undo/redo performance.

The following critical issues are fixed:
1. If you copied a LPE vertex and then tried to move one of them, both copies were moved ("Move Adjacent Data" is OFF).


### 12 Apr 2016
With the release of **0.8.10b** you are able to...
- ...customise GUI colours with the new "Options" dialog (text / 3D editor).
- ...activate/deactivate the error detection for new faces (with the new "Options" dialog)
- ...activate/deactivate no structure-aware sorting (text editor)
- ...copy/cut/delete modifies the currently "selected" line in the text editor (without text selection, it works now on the line where the caret is placed, too)
- ...better performance / usablility for "Toggle Comment / Toggle TEXMAP" (text editor)
- ...better performance for "Synchronise Folders / Editor Content".
- ...benefit from auto-removal of CR/LF line endings at the end of the file (on save)
- ...benefit from usability improvements for Add... Line, Triangle, Quad & Condline (the selection rectangle does not create a new vertex anymore)

The following critical issues are fixed:
1. Subfiles references with "s\" as prefix were not found when they were located in the same folder as the parent file 
2. Toggle comment/toggle !TEXMAP on multiple lines did not work as expected.
3. SymSplitter: Result was shifted if there was a "0 BFC INVERTNEXT"
4.  Bounding-box calculation errors with empty sub-files (some sub-files were invisible).


### 2 Apr 2016
With the release of **0.8.9** you are able to...
- ...get a standard file header on any new *.dat file.
- ...benefit from a better performing colour change and BFC swap in the text editor.
- ...benefit from a little bit faster render engine (depends on the 3D settings).
- ...shutdown LDPE quicker. LDPE does not ask you for unsaved projects anymore.
- ...get lowercase folders names in the project structure.
- ...see "Ctrl+Click" hints on button tooltips (only for modifiable functions, e.g. "Round")

The following critical issues are fixed:
1. **Risk of data loss**: "Delete" in the text editor with no selected text threw an exception.
2. The pipette copied the RGBA values from an overwritten colour 16.
3. The subfile-compiler did not support "s\" sub-parts on linux. 
4. "Save As..." did not use the lowercase "s\" prefix for subfile names.
5. "New Project" created a folder in the application directory.
6. Shaders did not compile on Intel HD Graphics.
7. The rubber band selection got cleared if the selection process was computationally intensive.
8. Opening a file in the 3D editor was not sychronised with the part tree on the left. 


### 30 Mar 2016
With the release of **0.8.8c** the following critical issues are fixed:
1. Primitive caching issues, causing a stackoverflow on all systems and a slowdown on 32-bit (severe)
2. The subfile creator added a capital S in front of the sub-part number, instead of a lower case s and created wrong names for primitives, too.
 

### 29 Mar 2016
With the release of **0.8.8b** the following critical issues are fixed:
1. Primitive caching freezes on 32-bit (severe)
2. Quick Fix synchronisation errors with the text editor (severe)
3. "Show All" did not make sub-file surfaces visible again.
4. Errors on sub-file folder searching on case sensitive file systems.
5. The shortkeys for Cut/Copy/Paste (3D Editor) were displayed on the shortkey dialog.
6. "Hide" makes more than the selected object invisible in some rare cases. 
7. The selection of sub-file content did not get restored with undo/redo.
8. Spelling errors / typos on the "Customise Shortkeys" dialog.


### 26 Mar 2016
With the release of **0.8.8** you are able to...
-  ...insert a reference line (type 1) with Ctrl+R (Text Editor only)
-  ...enable "Edge Adjacency" in combination with "Select Connected/Touching..." (instead of the default vertex adjacency)
-  ...switch between "solid" edge lines and classic OpenGL lines
-  ...get some usability improvements: When "Insert Objects Behind The Cursor Position" is active, a carriage return / linefeed will be inserted (you don't have to hit the Enter button anymore)
-  ...set the pivot point to the manipulator position.
-  ...use a "close" menu item in the context menu of the 3D editor (to close open files in the 3D view).
-  ...use a "close" menu item in the context menu of the file tree (to close open files in the 3D view).
-  ...use the new "two thirds" perspective (similar to the setting in LDView)
-  ...benefit from a better description for the Intersector tool.
-  ...benefit from another line colour during edge/triangle/quad creation

The following critical issues are fixed:
1. A rare deadlock is fixed. It occured during the development of 0.8.8 for the first time.
2. Select Connected/Touching in combination "with Whole Subfile Selection" did not select the subfile.
3. Several problems with empty sub-files (like "1-16chrd") are fixed.
4. Primitve caching issues on Win XP (32 bit) are gone.
5. The file search for file references/textures did not always use the base path of the current file.
6. The hide/show state was too volatile.
7. SymSplitter did not treat the first line of the file.
8. An uncritical drag&drop type exception is fixed.


### 23 Mar 2016
With the release of **0.8.7** you are able to...
- ...benefit from a faster program start.
- ...toggle the visibility of conditional line control points.
- ...drag&drop files on the 3D editor view.
- ...drag&drop files on the text editor view.
- ...use the new "modern" GUI layout on a fresh installation.
- ...activate anti-aliasing on some linux systems with MESA/Gallium. (on Windows, for AMD/NVIDIA cards, you have to enable it manually, with AMD Catalyst or the NVIDIA Control Panel)

The following critical issues are fixed:
1. Risk of data loss: When you open a file again (which was unsaved in LDPE) the file in the 3D view gets cleared.
2. "Show selection in text editor" did not work correctly for subfiles, if the text editor was closed before.
3. "Open Part File" from the 3D editor opens a text editor window, too.
4. Identical lines are not detected if they have different colours.
5. CSG does not work with invalid colour numbers.
6. "Unselect" (Ctrl+Click) does not work for subfiles.
7. Subfile selection issues with adjacent subfiles.
8. The coarse/medium/fine grid buttons don't stay pushed.


### 19 Mar 2016
With the release of **0.8.6** you are able to...
-  ...customise the layout of the 3D editor toolbars by placing one of the "layout_3D_editor.cfg" files in the application folder. You find different layouts in the attached archive "[layouts.zip](https://github.com/nilsschmidt1337/ldparteditor/files/181003/layouts.zip)".
- ...zoom in/out on the primitive area with two new buttons (-) and (+) (instead of using Ctrl+Mousewheel)
- ...use "Cut, Copy, Paste, Delete" in the right-click contextual menu from the text and 3D editor
- ...browse in the last visited folder if you open a file dialog again.
- ...select something and move it into a subfile (with a click on the _old_ "Add Subfile" button), supports cut **and** copy :)
- ...draw to the cursor position from the text editor (there is a button on the 3D editor, to enable this behaviour).
- ...use two new buttons ("Save Part" and "Save Part As...") in the 3D editor.
- ...use a new "Switch BFC" button in the text editor.
- ...reset the snapping/grid values to the default value with a right click on the corresponding "coarse", "middle" or "fine" button.

The following critical issues are fixed:
1. Creating a new *.dat (Text Editor) did not rebuild the hints/warning/errors tree. 
2. Java heap space problems on 32bit machines
3. "Merge / Split => Transform / Rotate / Scale" was able to break the 3D model triangulation. 
4. "Merge/Split => Rotate Selection" did not rotate around Z axis
5. "Open *.dat File" did not rebuilt the text editor tab, if the same file was saved before.
6. Copy->Paste->Mirror, mirrored the initial copy.
7. The hide/show state was deleted on undo/redo.
8. "Save as..."  did not update the <0 Name:> (and type) entry accordingly.
9. The "Select Touching" implementation was not correct. It selected too much.
10. Focus issues: A click on a button in the 3D editor removed the focus from the 3D view
11. A small memory leak (<1KB)


### 7 Mar 2016
With the release of **0.8.5** you are able to...
-  ...use up to 9 decimal places for rounding (coordinates + matrix).
-  ...open the part in the 3D editor from the text editor window (orange "3D" button)
-  ...benefit from a new "how-to-use" description for the SlicerPro2 tool. 
-  ...use "save as..." to store files under a new filename (text editor only)
-  ...use a button for single vertex manipulation ("SyncEdit") in the text editor.

The following critical issues are fixed:
1. Rectifier created rect#.dat primitives when "Split" and "Slicerpro" were used.
2. The internal Rectifier created rect-prims that were scaled in three dimensions.
3. Fixed an issue which was related to the parsing of the !HISTORY meta command
4. Fixed a null pointer issue with "Add Quad" and other "Add..." functions.
5. Fixed incorrect cursor movement when single vertex manipulation was active.
6. Fixed a GUI access issue, which occured on drag&drop with text editor tabs.
7. Fixed a random null pointer issue, which could disable the tab from the text editor window when more than one tab was opened.
8. In some rare cases when a flat subfile was scaled in X/Y or Z and the transformation matrix contained more than one space as seperator between the values, the corresponding "Quick Fix" was not able fix the scaling.



### 19 Jan 2016
With the release of **0.8.4c** the following issues are fixed:
1. Fixed a critical bug within the primitive sorting algorithm (crash on start!)
2. Fixed a  bug which disabled the possibility to split a condline.
3. Fixed a bug which could trigger a exception when the "Single Vertex Manipulation" (aka 'SyncEdit') was active
4. Fixed a bug for "Merge to nearest line" / "TJunctionFinder". They did not ignore control point lines from condlines.
5. Leading spaces from file header stopped the header validation.
6. It was possible to modify read-only files with the functions from the "Selection:" tab.


### 23 Dec 2015
With the release of **0.8.4b** the following issues are fixed:
1. Fixed a bug for "Move Subfile To Manipulator"
2. Fixed a critical bug for all "Move To Nearest..." functions


### 13 Dec 2015
With the release of **0.8.4** you are able to...
1. ...use the new "MeshReducer" tool, to simplify generated CSG meshes.
2. ...use a more "aggressive" T-junction-finder (can destroy the mesh geometry under some circumstances!) 
3. ...use the "Move on Line" function, to move a vertex on a line between two vertices (beta)


### 2 Dec 2015
With the release of **0.8.3c** the following issues are fixed:
1. An infinite loop, which could occur when "Single Vertex Replace Mode" was active ("SyncEdit")
2. The Isecalc tool did not generate all intersection lines
3. "Synchronise Folders..." did not work properly
4. Undo/Redo triggered an infinite loop.


### 25 Nov 2015
With the release of **0.8.3b** you will be able to...
- ...enable Single Vertex Modification (aka SyncEdit) in the Text Editor when you select one vertex in the 3D Editor.
- ...benefit from a little performance gain (due to some modifications related to logging in the debug mode).

This release includes 1 important bug fix as well.


### 24 Nov 2015
With the release of **0.8.3** you will be able to...
- ...configure shortkeys
- ...experience a faster program start
- ...auto-detect and fix T-Junctions
- ...show the text line selection in the 3D editor as a selection (if the 3D editor is opened)

This release includes 9 important bug fixes as well.

### 26 Aug 2015
With the release of **0.8.2d** you will be able to...
- ...benefit from improved CSG triangulation. The process is not free from errors, but in general a great improvement.

This release includes 5 important bug fixes as well.


### 17 Jun 2015
With the release of **0.8.2c** you will be able to...
- ...correct 0 BFC CERTIFY INVERTNEXT, NOCLIP and CLIP meta commands (MLCAD bug)
- ...see dithered colours and convert dithered colour codes into direct colours.

This release includes 1 important bug fix as well.


### 8 Jun 2015
With the release of **0.8.2b** you will be able to...
- ...see and validate the log data before you upload it to the internet.

This release includes 1 important bug fix as well.


### 4 Jun 2015
With the release of **0.8.2** you will be able to...
- ...experience a faster program start.
- ...use a wireframe render mode.

This release includes 9 important bug fixes as well.


### 1 Jun 2015
With the release of **0.8.1d** you will be able to...
- use a new program updater.

This release includes bug fixes as well.


### 31 May 2015
With the release of **0.8.1c** you will be able to...
- use a even better "Delete" function in the parts tree (it creates .bak files for project parts and deletes only the reference path to non-project parts). 
- emulate mouse keys on a focused 3D window with keyboard keys (K = Left Mouse Button, L = Right Mouse Button, M = Middle Mouse Button)
- Convert selected Lines to Conditional Lines

This release includes bug fixes as well.


### 25 May 2015
With the release of **0.8.1b** you will be able to...
- use the background picture feature. It was broken.


### 3 Jun 2015
With the release of **0.8.1** you will be able to...
- (you won't be able to) delete your parts physically from the harddisk anymore.
- choose your locale.
- benefit from better icon design.
- manipulate the subfile matrix numbers without the text editor.
- send error logs to me.
- use a coloured cross cursor.
- benefit from auto-reading the %LDRAWDIR% environment variable. 

This release includes many bug fixes as well.


### 11 Apr 2015
First public beta release of version **0.8.0**.


### 17 Feb 2015
Switched from SVN repository (sf.net) to git (github.com), because the code-base exceeded 100.000 Lines of Code and I had to do offline commits. I did about 770 commits on sf.net. 


### 26 Jul 2012
Project start / first commit on sourceforge.net


### 15 May 2012
Project start, https://forums.ldraw.org/thread-4918.html
