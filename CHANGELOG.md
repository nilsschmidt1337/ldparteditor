### 25 Jul 2025
With the release of 1.8.95 you are able to...
-  ...see a hint if the part type does not match to its name.

The following critical issues are fixed:
1. A recurring subpart reference loop can cause the program to crash or hang.


### 16 Jun 2025
With the release of 1.8.94 you are able to...
-  ...detect overlapping surfaces (with the new OverlappingSurfacesFinder tool).


### 07 Jun 2025
With the release of 1.8.93 you are able to...
-  ...show two-thirds views from other angles (click on "Two-Thirds" multiple times).
-  ...see the user name and current date displayed as default in the dialog for meta commands.
-  ...use the new MATERIAL FABRIC colours.

The following critical issues are fixed:
1. The keyboard shortcut for showing the "Add comment / meta data" dialog did not work anymore.
2. The detection of "~Moved to" files did not always work.
3. [OpenGL 3.3] Flickering lines on Intel Arc dGPUs.


### 27 Apr 2025
With the release of 1.8.92 you are able to...
-  ...see a hint about wrong colours for lines and condlines or other non-line objects with colour 24.

The following critical issues are fixed:
1. Warnings on BFC meta commands deactivated the meta command on the affecting line (e.g. a "0 BFC CERTIFY CW" on line 7).
2. [OpenGL 2.0] The LDraw Standard Render Mode (with TEXMAP) did not work on Intel Arc dGPUs.


### 12 Apr 2025
With the release of 1.8.91 you are able to...
-  ...benefit from the fact that Rectifier "does not convert bordered rhombuses into rect primitives" by default.
-  ...customise if Rectifier "does convert bordered rhombuses into rect primitives" by default.
-  ...choose if Rectifier creates rect primitives with a shear or non-trivial rotation matrix.
-  ...see a warning if the file is BFC CW certified instead of CCW.
-  ...see a hint for "freezing the selection" when adding new objects.
-  ...use the old sphere mesh for CSG operations, too (CSG_ELLIPSOID2).
-  ...use a "Concave/Convex Heatmap" mode which visualises convexity between surfaces (type 3 and 4, double-click on heatmap button).
-  ...see the !PREVIEW meta with syntax highlighting.

The following critical issues are fixed:
1. The selection filter logic was not consistent.
2. [OpenGL 3.3] LDPE didn't switch to "special condline mode" when the user tried to add a condline.


### 07 Dec 2024
With the release of 1.8.90 you are able to...
-  ...drag and drop Windows file shortcuts to LDPE to open the linked *.dat file.
-  ...reference subfiles by a full path (without having double spaces in the path).
-  ...have a quad "auto-created" when you move one vertex enough to allow a flat quad (during "Coplanarity Heatmap" mode in the 3D editor).
-  ...benefit from the fact that txt2dat writes the used font setting to the file.

The following critical issues are fixed:
1. Lower/upper case differences between the filename in the 3D editor and in the text editor tab.
2. High-quality edges (type 2 and 5) were not displayed with OpenGL 3.3 on some graphic cards.


### 27 Sep 2024
With the release of 1.8.89 the following critical issues are fixed:
1. Descriptions containing a hashtag # are modified by unrelated quick fixes.
2. The movement type of the manipulator changes if, during the move, the mouse gets close enough to a plane move square.
3. The CSG tree does not switch back to a higher quality if the manipulator was moved and nothing CSG releated was selected.
4. The MacOS X ARM installer package was still indicating a 1.8.80 version, but the contents were newer.


### 20 Sep 2024
With the release of 1.8.88 you are able to...
-  ...double click on a text editor error/warning/hint to also select the object in the 3D editor.

The following critical issues are fixed:
1. Primitive substitution on subfiles from PartReview causes subfiles to be "empty".
2. "Rings and Cones" uses old deprecated "rin" and "ri" ring primitives.
3. The context menu entries on the error/warning/hint area are always enabled.
4. After loading a part using the part review tool, the 3D view properly shows the part asked for, but the text window generally shows a subpart of it, presumably the last loaded one.


### 01 Sep 2024
With the release of 1.8.87 you are able to...
-  ...display the axes of coordinates in the center of the 3D screen (it is also used in the first 3D view for PartReview)
-  ...set and see a different selection color than red in backface-culling modes.
-  ...have 4 digits in the last section of a mixed-mode torus description (e.g. 1 x 2.0000 x 0.2500).

The following critical issue is fixed:
1. [OpenGL 3] Crash when adding a line, triangle, quad, condline, distance meter or protractor to an empty file.


### 20 Aug 2024
With the release of 1.8.86 you are able to...
-  ...use the sphere mesh from LDView for CSG operations, too (!LPE CSG_ELLIPSOID).

The following critical issue is fixed:
1. !LPE CSG_UNION "consumed" a few triangles. Other triangle defects could still occur, but only with very complex union intersections.


### 16 Aug 2024
With the release of 1.8.85 you are able to...
-  ...use primitive substitution (blue circle button at the upper left corner, ctrl-click to modify substitution quality).
-  ...benefit from a 3D grid that shows 3D model intersections and has a lager size.
-  ...benefit from inverted icons for a better contrast with dark buttons.
-  ...create a coplanar quad within the "Coplanarity Heatmap" mode.

The following critical issues are fixed:
1. PartReview had loading problems under MacOS (the 3D view was gone).
2. (Linux/Mac) Case-sensitivity when following a reference from the text editor with a mouse click if the target file was already opened caused problems.


### 28 Jul 2024
With the release of 1.8.84 you are able to...
-  ...visually identify which surfaces or lines belong to the model being edited or to a primitive/subpart (with the green "S" button in the upper left corner of the 3D editor).
-  ...press a button to run Edger2 verbose with the current settings.
-  ...set the number of open views for part review (1-4).
-  ...see the location where PartReview will store its files and "Save All" files for review.
-  ...benefit from a better button contrast on the dark UI themes.
-  ...benefit from a better contrast for the coplanarity heatmap.
-  ...benefit from an improved Edger2 performance.
-  ...show hi-quality edge lines in the OpenGL 3.3 renderer (TYPE 2 and 5).

The following critical issues are fixed:
1. Uninlining left an additional blank line after un-inlining in the target file. 
2. The ESC key reset the Manipulator when it was set by a double click on a surface before.
3. Added a a missing border on text boxes which had a UI theme applied.


### 19 Jul 2024
With the release of 1.8.83 you are able to...
-  ...change colour themes via "Tools..."->"Options..."->"Customise Colours:"->"Theme" (e.g. to dark mode/dracula theme) to make the software easier on the eyes.
-  ...use "Swap X/Y/Z (for Selection)".
-  ...use "Set X/Y/Z (for Selection)" to set the manipulator position, too.

The following critical issue is fixed:
1. Sometimes the text-editor remained read-only until the file was saved (e.g. after Edger2 runs).


### 13 Jul 2024
With the release of 1.8.82 you are able to...
-  ..."un-inline" linked subfiles in the main part (with ctrl+click on the "Inline selection (Linked)" button).
-  ...customise a key shortcut to activate links in the text editor (it was Ctrl/Cmd before, now it is Ctrl+Cmd+Shift).
-  ...use key shortcuts for merge triangles to quads / split quads to triangles (shift+q and shift+t).
-  ...use "Save As..." to export a file with its references into a single zip file.

The following critical issues are fixed:
1. "Empty Subfiles" was not properly checked in the context menu of the 3D view.
2. The path "parts/textures" was not used as a search path for PNG textures.


### 04 Jul 2024
With the release of 1.8.81 you are able to...
-  ...ctrl-click a subpart or prim in the text editor to open it in the 3D editor and jump straight to it (cmd-click on Mac).
-  ...use a faster way to move (G), rotate (R) and scale (S) (press G, S or R on the 3D view, press X, Y or Z to lock to an axis. Or ctrl-shift-x and so on to lock to a plane.).
-  ...calibrate a background image with a measurement (click on the new "Calibrate Background Image" button, select a line (type 2 or 5 on the image plane) and enter the new distance)
-  ...better see "empty" primitives.
-  ...get even better automatic rounding of 9999s and 0000s (e.g. for PathTruder results).
-  ...use an upgraded gizmo/manipulator (to move and scale in XY, XZ, YZ planes).
-  ...benefit from cleaned up inlined results from CSG_COMPILE.
-  ...benefit from the fact that empty tree items for hints / warnings / errors / duplicates in the text editor will be expanded when they get items to display.

The following critical issues are fixed:
1. The ldraw.org link to download the contents of categories.txt was outdated.
2. It was possible to assign a key twice (with a newly introduced shortcut).


### 22 Jun 2024
With the release of 1.8.80 you are able to...
-  ...do simple math in the text editor (e.g. (-3+5)*2 and hit enter).
-  ...do simple math in floating point number input fields.
-  ...benefit from auto-rounding for the results of scripted expressions (e.g. 1.99999998 becomes 2).
-  ...benefit from better rounding in general (e.g. for PathTruder results).
-  ...try a new 3D grid (Ctrl+Shift+G).
-  ...try LDPartEditor on ARM Macs.

The following critical issue is fixed:
1. Undoing at the end of the file led to wrong line number.


### 10 Jun 2024
With the release of 1.8.79 you are able to...
-  ...inline a selection without any comments (ctrl+click on inline button).
-  ...decide if the right mouse click cancels the action to add a line/triangle/quad or not. Cancel is not the default anymore! ("Tools..."->"Options...").
-  ...have a shortcut which adds an empty subfile reference line, mirrored on X (Ctrl-Shift-R).

The following critical issues are fixed:
1. Join Selection (Text Editor) had problems when INVERTNEXT was on top of the selection.
2. Text line selection in the text editor (by clicking on the line numbers) resulted in wrong inlining when the next line was a reference (type 1).
3. It was not possible to import empty STL files without an error ("File not found" was displayed).


### 31 May 2024
With the release of 1.8.78 you are able to...
-  ...move a set of subfiles to the manipulator, using "Move a selected Subfile to the Manipulator" (only one subfile was supported before).
-  ...customise Edger2 in the options ("Tools..."->"Options...") to include or not include unmatched edges by default.
-  ...use buttons for (global) 3D transformations (rotate + translate) in the text editor.
-  ...choose on which line the point gets projected when you calculate the nearest point between two skew lines.

The following critical issue is fixed:
1. Rounding in the text editor did not update the vertex meta commands.


### 26 Mar 2024
With the release of 1.8.77 you are able to...
-  ...benefit from improved accuracy of "Move the Manipulator to the Average Point of the Selection".

The following critical issues are fixed:
1. Sometimes, the 3D manipulator rotated the object instead of moving it.
2. Sometimes, the triangle to quad conversion tool didn't use the selected triangles, but another nearby one.


### 15 Mar 2024
With the release of 1.8.76 you are able to...
-  ...be warned about "slashes" in subfile references and you can "Quick Fix" it.
-  ..."Exclude Unmatched Edges" by default in Edger2.

The following critical issue is fixed, finally:
1. The 3D manipulator orientation got polluted during rotation.


### 04 Feb 2024
With the release of 1.8.75 you are able to...
-  ...export a reference card (cheat sheet) for LDPE ("?" button).
-  ...lock the protractor angle between the opposite and adjacent side.
-  ...use the OpenGL 3.3 renderer on AMD graphic cards.
-  ...inrease/decrease the font size of the text editor (with Ctrl+ and Ctrl-).
-  ...benefit from the fact that the manipulator position between 3D views is synchronised now for CSG manipulations.

The following critical issues are fixed:
1. Drafts can't be saved under their original filename.
2. [OpenGL 3.3] Selected vertices stay hidden behind surfaces (uncritical)
3. The "Smooth Vertices" action had swapped labels for "Iterations:" and "Factor:".


### 11 Dec 2023
With the release of 1.8.74 you are able to...
-  ...use a "stud" grid setting (Plate 1 x 1, Y = 8 LDU, XZ = 10 LDU half-stud).
-  ...see an optional "x", "y", "z" on the coloured axes of the coordinate system (activate with "Tools..."->"Options..."->"Show Axis Labels", or with the 3D view context menu "View Actions"->"Axis Label").
-  ...create a new file draft (Ctrl+Click on "New Part File"), without saying where it should be saved. When it is saved for the first time, the program will ask where to save it. Unsaved drafts are lost when the program gets closed.
-  ...access a set of standard bricks, tiles and plates in the primitives view.
-  ...benefit from a more precise unit converter (it avoids the division).

The following critical issue is fixed:
1. Creating/opening a project kept the "Part Review" active, when doing a review.


### 01 Nov 2023
With the release of 1.8.73 you are able to...
-  ...align and distribute objects (isolated vertices, lines, triangles, quads and subfiles). It behaves differently if "Move Adjacent Data" is ON or OFF. ON keeps the objects connected together. OFF will seperate them.
-  ...snap vertices and subfiles to the current grid.
-  ...auto-remove superfluous arguments from text lines (type 1,2,3,4,5) and also add missing ones.
-  ...benefit from the fact that pasting a single vertex will not enable "Single Vertex Modification" if "Automatically disable "Move Adjacent Data" on paste (3D Editor)" is checked.
-  ...benefit from a slightly faster 3D editor (e.g. lower latency when you add a triangle).

The following critical issue is fixed:
1. Wrong "Invalid number format" warning on "!LPE DISTANCE".


### 07 Oct 2023
With the release of 1.8.72 you are able to...
-  ...use a shortcut for "Merge to Nearest Vertex" (Ctrl+N).
-  ...see a hint for wrong comment lines with just one slash (with quick-fix option).
-  ...benefit from the fact that running a tool in the "3D Editor" will update the "Text Editor" immediately.
-  ...see additional hints for the pivot point in the "Rotate" and "Scale" dialog.
-  ...set the filesize limit for the !DATA meta command (in the program options).

The following critical issues are fixed:
1. Pasting something via clipboard did not deselect the currently selected vertex.
2. A direct colour is not an invalid number (when it contains the letter "E").


### 19 Sep 2023
With the release of 1.8.71 you are able to...
-  ...benefit from a slightly faster program start (technical details: JDK upgrade from version 17 to 21, LWJGL upgrade from version 3.2 to 3.3)
-  ...use the !DATA meta command (currently limited to small PNGs of 42KB size)
-  ...access the unit converter in a separate tab.
-  ...have a new "double-arrow" icon for the "Update/Compile Subfile Data" function.

The following critical issues are fixed:
1. [Linux-only] Rotating the 3D view via keyboard shortcut (not middle mouse button) warped the viewport and let it "jump".
2. Selecting a line via click on the line number in the text editor and then showing the selection in the 3D view, selected the next line, too.


### 19 Aug 2023
With the release of 1.8.70 you are able to...
-  ...show the error logfiles only ("Tools..." -> "Show Error Logs"). I removed and disabled the upload feature.
-  ...get no error log entry for the case when a file could not be downloaded for part review from library.ldraw.org (there is still an error dialog, when this happens).
-  ...get a better renderer result for pearlescent material with the new OpenGL 3.3 renderer pipeline.

The following critical issues are fixed:
1. "Replace all" did not work in the text editor.
2. Changing the colour palette in the text editor disabled the "More..." colour button.
3. It was not possible to save special colours to the custom palette (e.g. CHROME, RUBBER, METAL, ...)
4. The feature which checked the part file header for hints had hiccups (uncritical). 
5. A saved part file was not closed when the user wanted to open or create a new project and decided not keep the current files and views open.


### 15 Jul 2023
With the release of 1.8.69 you are able to...
-  ...import STL files (via "Open Part File" -> choose "STL-File (*.stl)", instead of "LDraw Source File (*.dat)").

The following critical issues are fixed:
1. Switching the line size caused long reloading times.
2. "BFC INVERTNEXT" was invalid when the previous subfile was not found.


### 04 Jun 2023
With the release of 1.8.68 you are able to...
-  ...lock/unlock the viewport rotation.
-  ...see "Author:" and "Name:" formatted as bold and italic in the text editor.

The following critical issue is fixed:
1. The "Browse..." button for the Background Picture path opened a dialog to "save" the image instead of "open".


### 26 Mar 2023
With the release of 1.8.67 you are able to...
-  ...see a hint on numbers with scientific notation.

The following critical issues are fixed:
1. The 3D manipulator orientation gets polluted.
2. LDPE was not able to download from library.ldraw.org
3. The tabs in the 3D editor couldn't be moved around. The text editor was not affected.
4. SymSplitter cutted with data from subfiles too, in addition to the cutting plane (see images on https://github.com/nilsschmidt1337/ldparteditor/issues/959 )
5. Intersector calculated sometimes wrong results with some triangles missing.
6. It was not possible to do a "copy + transform" action on MacOS X with the cursor keys (see https://github.com/nilsschmidt1337/ldparteditor/issues/958 ).


### 15 Mar 2023
With the release of 1.8.66 you are able to...
-  ...use the change winding tool (j) on type 2 and type 5 lines, swapping the end points.
-  ...have the "verbose" option common to nearly all tools, and be remembered between sessions. 
-  ...deep inline a subfile and it will mark the end of that subfile, too.
-  ...have the CR LF at the end of a line to be included in the selection when you click on a line number.

The following critical issues are fixed:
1. "Merge To Nearest Vertex" recognized condline control points as vertices to merge for.
2. The LDraw library URL was outdated (www.ldraw.org -> library.ldraw.org).
3. 3D scenes only rendered to 1/4 of 3D window in some rare cases.


### 06 Mar 2023
With the release of 1.8.65 you are able to...
-  ...see the currently used font name and style (txt2dat).
-  ...add a small margin on all sides, so that the text is completely included in background (txt2dat).

The following critical issues are fixed:
1. [txt2dat] There was a glitch in "w" top (Arial font, default parameters).
2. [txt2dat] Removed parameter "min angle between line segments" which had no action.
3. [txt2dat] Removed the unnecessary "interpolate flatness" parameter.


### 24 Feb 2023
With the release of 1.8.64 you are able to...
-  ...benefit from a new Rectifier default setting (it will not convert quad to rect when the quad has adjacent condlines by default).
-  ...use the "Copy" command on the icon bar at the bottom of the tab, mirrored from the contextual of the "Problems" tab
-  ...have a single background bounding rectangle for the whole string (txt2dat).
-  ...use an alternative option to not have a background but only the characters themselves (txt2dat).


### 01 Oct 2022
With the release of 1.8.63 you are able to...
-  ...keep all files from the initial temporary project on application restart.
-  ...see the D value (distance) of the distance meter not only shown in "LDU" but also in "mm" and "Stud".
-  ...copy hint/error/warning messages to the clipboard to paste it to the PartsTracker.
-  ...have a customizable line size button where you can set the thickness of the line for yourself (Ctrl+Click on a line size button). 
-  ...use more line size buttons (five).

The following critical issues are fixed:
1. "Save as..." removed sometimes a correct directory prefix (e.g. "48\") from the file header.


### 28 Aug 2022
With the release of 1.8.62 the following critical issues are fixed:
1. The generated condline coordinates for the Torus from PrimGen2 did not match the coordinates of the generated quads.
2. The transparency calculation for TEXMAP images was incorrect on the old OpenGL 2 renderer.
3. Fallback geometry in TEXMAP sections was shown (in LDraw Standard render mode with the old OpenGL 2 renderer).


### 03 Aug 2022
With the release of 1.8.61 you are able to...
-  ...create a mixed mode torus with PrimGen2. 


### 10 Jul 2022
With the release of 1.8.60 you are able to...
-  ...use the "!LPE CONST" meta command to define re-usable constants in parts and subparts.
-  ...use calculated expressions for constants, too.
-  ...quick-fix/replace constants with the text editor.
-  ...combine "!LPE CONST" with "!LPE CSG" meta commands (but not with !TEXMAP commands, yet).


### 05 Jul 2022
With the release of 1.8.59 the following critical issues are fixed:
1. The middle mouse button rotation was locked (in combination with the Alt key). 
2. Different lines were marked as hidden in the text editor.
3. When the source lines came from an offset subpart, Ytruder's "symmetry across plane" didn't create the correct result. 


### 02 Jul 2022
With the release of 1.8.58 you are able to...
-  ...press and hold shortkeys to rotate the view around a fixed axis with the mouse (snapping to angles, with Shift, Ctrl and Alt). 

The following critical issues are fixed:
1. The check for BFC-Meta command was not strict enough (it allowed prefix lines like "0 BFC CERTIFY CCWxyz").
2. !HISTORY meta command: Removed the [4.0] tag in favor of a fixed implementation date.
3. Added missing buttons on the UI for area expansion / missing buttons on "Find+Replace" dialog.


### 06 Jun 2022
With the release of 1.8.57 you are able to...
-  ...use the upgraded "!LICENSE" / "!HISTORY" meta command to support the new CC BY 4.0 agreement.
-  ...know the name of the selected subfile if there is only one subfile selected (shown in status bar).
-  ...benefit from the fact that linked inlining will happen in the folder the current file was saved in (except for project files).

The following critical issues are fixed:
1. Rare error while creating a subfile from 3D selection.
2. "Show selection in 3D view" selected vertices from non-subfile surfaces and lines.


### 21 May 2022
With the release of 1.8.56 you are able to...
-  ...create all intersection points between lines and surfaces in a selection (Ctrl+Click on "Calculate Line Intersection Points").
-  ...use drag and drop to open a file on the 3D Editor and open it on the Text Editor, too.
-  ...use the shortkey Shift+L for C2L and Shift+C for L2C.
-  ...scale "empty" subfiles in X or Z directions without a warning.
-  ...enable anti-aliasing on all plattforms ("Tools...->Anti-Aliasing", works on Windows, Linux, Mac OS X)


### 26 Apr 2022
With the release of 1.8.55 you are able to...
-  ...to see a warning for subpart references with no content. You can also transform empty subparts and "see" them as a single vertex in the 3D editor.
-  ...use "Show Selection in Text Editor" to show the first line that uses that vertex, when you selected a vertex (or the next lines when you repeat the function).
-  ...benefit from a better contrast of hidden lines in the text editor.

The following critical issues are fixed:
1. Empty primitives couldn't be selected/rotated (e.g. 1-16chrd's)
2. The zoom value for newly created files was not the default value (3D Editor)
3. When the user did not wanted to save the file in the text editor it was still displayed in the 3D editor.
4. "Show selection in 3D View" did not select the vertices from subfiles.
5. When you used separate text and 3D windows and created a new file, a new text window was opened, instead of using a new tab in the already opened text window.
6. If you used "save as", a new file was created, but no text window or tab was created, although the sync tabs button was active.
7. "Sync. 3D Editor Tabs with Text Editor" setting was sometimes deactivated.
8. (back-ported to 1.8.54) LDPartEditor 1.8.54 won't start on Linux (because of a classpath error)
9. Minor IO error regarding the "(no file selected)" tab.


### 22 Feb 2022
With the release of 1.8.54 you are able to...
-  ...use installers of LDPartEditor for Windows, Linux and Mac OS X (without the need to install Java)
-  ...see more digits (at least 4 decimal places) on the protractor and the distance meter.

The following critical issues are fixed:
1. "Set X/Y/Z (for Selection)" loaded always the vertex from the clipboard if there was one vertex stored in it.
2. Outdated UI libraries for Mac OS X were updated.


### 24 May 2021
With the release of 0.8.53 you are able to...
-  ...benefit from about 100 different optimizations to LDPEs code base (I added 180000 lines and deleted 185000 lines since the last release).
-  ...benefit from a new, more robust and secure method which loads and saves LDPEs configuration file.
-  ...get improved hints and tips for the startup dialog (seen on first program start).

The following critical issues are fixed:
1. Undo / Redo broke the 3D model if the file ended with empty lines or contained duplicated empty line groups.
2. The calculated colour for transparent textures was too dark. (OpenGL 2.0)
3. On MacOS X it was not possible to reassign a shortkey, because the dialog window had not the required focus for detecting the keyboard input.
4. Avoided obvious divisions by zero on "zoom to fit" with empty models and by scaling a zero-height font with Txt2Dat.
5. The 3D editor freezed by an BFC INVERTNEXT without a following TYPE 1 reference line.
6. "Make subpart from 3D selection" duplicated "0 BFC INVERTNEXT" meta commands.


### 01 Sep 2020
With the release of 0.8.52 the following critical issue is fixed:
1. The Windows program (LDPartEditor.exe) will now start with support of newer Java versions.


### 10 Aug 2020
With the release of 0.8.51 the following critical issue is fixed:
1. Some keyboard keys can't be properly assigned as a shortcut for the 3D editor view (e.g. the 'R' key).


### 01 Jun 2020
With the release of 0.8.50 you are able to...
-  ...use a button to re-open the 3D view (if no view is shown)
-  ...see a warning if you try to configure a shortcut key which can't be assigned.
-  ...start the program with a broken config file (it's not a bug, it's a feature!).

The following critical issues are fixed:
1. The program could break if a subfile refered to a non-existent or read-protected nested subfile in very rare cases.
2. There was a critical selection problem which occured if you have tried to select a single quad, but you deactivated the selection of quads before (selection filter).
3. It was problematic if the program could not read from the official and unoffical LDRAW library folder.
4. It was impossible to save a file if its directory does not exist anymore.
5. There was a breaking bug which could deactivate the duplication check on a single file (did not affect other files).
6. Pressing the ESC key reset the (scale) snapping to an unexpected 2.00 value.


### 08 Oct 2019
With the release of 0.8.49b the following critical issue is fixed:
1. "Open File" : A "\" character was missing between the folder name and the file name.


### 07 Oct 2019
With the release of 0.8.49 you are able to...
-  ...invert the mouse wheel zoom direction (Tools... -> Options... -> Customise Shortkeys).
-  ...open multiple files in one go.
-  ...use a shortcut to reset the manipulator to the origin and world orientation ("R" key).
-  ...see the coordinates of up to 4 selected vertices in the status bar.
-  ...benefit from the automatically deactivation of "Add..." when you focus or select something in the text editor.

The following critical issues are fixed:
1. The scale tool displayed the manipulator with "global" coordinates when "local" coordinates were active.
2. There was a missing comma in the selection info on the status bar.


### 01 Sep 2019
With the release of 0.8.48 you are able to...
-  ...swap the middle and right mouse button assignment (view translation and view rotation)
-  ...get the PNG textures from the PartsTracker when you are using the PartReview tool.
-  ...configure a viewport scale factor (this is only necessary for 4K screens with a high pixel density)

The following critical issues are fixed:
1. Display issues on a 3840x2160 display: empty regions & misplaced cursor.
2. The vertex window pop-up was causing an internal error (with no negative side effects for the user).


### 29 Jul 2019
With the release of 0.8.47 the following critical issue is fixed:
1. High colour numbers prevent the colour dialog from showing up


### 08 Jun 2019
With the release of 0.8.46 you are able to...
-  ...see some hints on the configuration screen when you are launching LDPartEditor for the first time.

The following critical issues are fixed:
1. The PartReview tool got broken by the introduction of HTTPS on the Parts Tracker.
2. The access to categories.txt and ldconfig.ldr got broken by the introduction of HTTPS on LDraw.org
3. The program did nor really quit when the configuration screen was cancelled.


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


### Development Principles

    PERFORMANCE - Faster is always better

    AGILITY - Requirements should be delivered just-in-time

    PRECISION - The tool must not handicap the user's business

    STABILITY - No Crashes, No Complains, No Data Loss

    TEST - Only a known bug can be fixed before the release

    INDEPENDENCE - of Operating Systems and hardware manufacturers

    BOTTOM-UP - Implement only what is necessary now to get a good program

    STRUCTURE

    DOCUMENTATION to the programmer

    HELP to the user
