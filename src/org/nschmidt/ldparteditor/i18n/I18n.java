/* MIT - License

Copyright (c) 2012 - this year, Nils Schmidt

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package org.nschmidt.ldparteditor.i18n;

import java.awt.ComponentOrientation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.enums.View;

/**
 * This class provides quick access to all translated Strings and RTL relevant constants (NEVER EVER FORMAT THIS CLASS!)
 * It does not have much documentation, I think this class is not so complex to understand.. ;)
 *
 * @author nils
 *
 */
public final class I18n {

    private static final Field[] fields = getSortedFields();

    /** Has the value {@code SWT.RIGHT_TO_LEFT} on systems with rtl languages */
    public static final int I18N_RTL() {return I18N_RTL;};
    private static final int I18N_RTL;
    /** Has the value {@code SWT.NONE} to indicate that this component does not support bi-directional text */
    public static final int I18N_NON_BIDIRECT() {return SWT.NONE;};
    /** {@code true} on RTL systems */
    public static final boolean isRtl() {return isRtl;};
    private static final boolean isRtl;

    private static int line_offset;

    // Bundles
    private static final ResourceBundle EDITORTEXT = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.TextEditor", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle UNITS = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.Units", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle PARTS = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.Parts", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle VERSION = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.Version", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle PERSPECTIVE = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.Perspective", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle SPLASH = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.SplashScreen", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle PROJECT = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.Project", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle COPYNPASTE = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.CopyAndPaste", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle DIALOG = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.Dialog", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle EDITOR3D = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.3DEditor", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle COLOURDIALOG = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.ColourDialog", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle COMPOSITETAB = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.CompositeTab", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle COORDINATESDIALOG = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.CoordinatesDialog", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle DATFILE = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.DatFile", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle DATPARSER = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.DatParser", MyLanguage.LOCALE); //$NON-NLS-1$
    // Bundles end

    private static boolean notAdjusted = true;

    private static void adjust() { // Calculate line offset
        StackTraceElement stackTraceElements[] = new Throwable().getStackTrace();
        String stack = stackTraceElements[0].toString();
        int line_number  = Integer.parseInt(stack.substring(stack.lastIndexOf(":") + 1, stack.lastIndexOf(")"))); //$NON-NLS-1$ //$NON-NLS-2$
        line_offset = line_number + 7;
        notAdjusted = false;
    }
    // Constants (Need case sensitive sorting!)
    public static final String COLOURDIALOG_Colour = COLOURDIALOG.getString(getProperty());
    public static final String COLOURDIALOG_ColourTitle = COLOURDIALOG.getString(getProperty());
    public static final String COLOURDIALOG_DirectColour = COLOURDIALOG.getString(getProperty());
    public static final String COLOURDIALOG_ShowColourTable = COLOURDIALOG.getString(getProperty());
    public static final String COLOURDIALOG_StandardColours = COLOURDIALOG.getString(getProperty());
    public static final String COMPOSITETAB_FileEncodingError = COMPOSITETAB.getString(getProperty());
    public static final String COMPOSITETAB_FileNotFound = COMPOSITETAB.getString(getProperty());
    public static final String COMPOSITETAB_FileReadError = COMPOSITETAB.getString(getProperty());
    public static final String COORDINATESDIALOG_SetXYZ = COORDINATESDIALOG.getString(getProperty());
    public static final String COORDINATESDIALOG_X = COORDINATESDIALOG.getString(getProperty());
    public static final String COORDINATESDIALOG_Y = COORDINATESDIALOG.getString(getProperty());
    public static final String COORDINATESDIALOG_Z = COORDINATESDIALOG.getString(getProperty());
    public static final String COPYNPASTE_Copy = COPYNPASTE.getString(getProperty());
    public static final String COPYNPASTE_Cut = COPYNPASTE.getString(getProperty());
    public static final String COPYNPASTE_Delete = COPYNPASTE.getString(getProperty());
    public static final String COPYNPASTE_Paste = COPYNPASTE.getString(getProperty());
    public static final String DATFILE_HeaderHint = DATFILE.getString(getProperty());
    public static final String DATFILE_MissingAuthor = DATFILE.getString(getProperty());
    public static final String DATFILE_MissingBFC = DATFILE.getString(getProperty());
    public static final String DATFILE_MissingFileName = DATFILE.getString(getProperty());
    public static final String DATFILE_MissingLicense = DATFILE.getString(getProperty());
    public static final String DATFILE_MissingPartType = DATFILE.getString(getProperty());
    public static final String DATFILE_MissingTitle = DATFILE.getString(getProperty());
    public static final String DATPARSER_InvalidNumberFormat = DATPARSER.getString(getProperty());
    public static final String DATPARSER_SyntaxError = DATPARSER.getString(getProperty());
    public static final String DIALOG_AlreadyAllocatedName = DIALOG.getString(getProperty());
    public static final String DIALOG_AlreadyAllocatedNameTitle = DIALOG.getString(getProperty());
    public static final String DIALOG_Apply = DIALOG.getString(getProperty());
    public static final String DIALOG_Browse = DIALOG.getString(getProperty());
    public static final String DIALOG_Cancel = DIALOG.getString(getProperty());
    public static final String DIALOG_CantSaveFile = DIALOG.getString(getProperty());
    public static final String DIALOG_CantSaveProject = DIALOG.getString(getProperty());
    public static final String DIALOG_CopyFileAndRequired = DIALOG.getString(getProperty());
    public static final String DIALOG_CopyFileAndRequiredAndRelated = DIALOG.getString(getProperty());
    public static final String DIALOG_CopyFileOnly = DIALOG.getString(getProperty());
    public static final String DIALOG_CreateMetaCommand = DIALOG.getString(getProperty());
    public static final String DIALOG_Delete = DIALOG.getString(getProperty());
    public static final String DIALOG_DeleteTitle = DIALOG.getString(getProperty());
    public static final String DIALOG_DirectorySelect = DIALOG.getString(getProperty());
    public static final String DIALOG_Error = DIALOG.getString(getProperty());
    public static final String DIALOG_KeepFilesOpen = DIALOG.getString(getProperty());
    public static final String DIALOG_KeepFilesOpenTitle = DIALOG.getString(getProperty());
    public static final String DIALOG_Modified = DIALOG.getString(getProperty());
    public static final String DIALOG_ModifiedTitle = DIALOG.getString(getProperty());
    public static final String DIALOG_NoProjectLocation = DIALOG.getString(getProperty());
    public static final String DIALOG_NoProjectLocationTitle = DIALOG.getString(getProperty());
    public static final String DIALOG_NotFoundRequired = DIALOG.getString(getProperty());
    public static final String DIALOG_NotFoundRequiredTitle = DIALOG.getString(getProperty());
    public static final String DIALOG_OK = DIALOG.getString(getProperty());
    public static final String DIALOG_RenameOrMove = DIALOG.getString(getProperty());
    public static final String DIALOG_Replace = DIALOG.getString(getProperty());
    public static final String DIALOG_ReplaceTitle = DIALOG.getString(getProperty());
    public static final String DIALOG_Revert = DIALOG.getString(getProperty());
    public static final String DIALOG_RevertTitle = DIALOG.getString(getProperty());
    public static final String DIALOG_SkipAll = DIALOG.getString(getProperty());
    public static final String DIALOG_Sync = DIALOG.getString(getProperty());
    public static final String DIALOG_SyncTitle = DIALOG.getString(getProperty());
    public static final String DIALOG_TheNewProject = DIALOG.getString(getProperty());
    public static final String DIALOG_TheOldProject = DIALOG.getString(getProperty());
    public static final String DIALOG_Unavailable = DIALOG.getString(getProperty());
    public static final String DIALOG_UnavailableTitle = DIALOG.getString(getProperty());
    public static final String DIALOG_UnsavedChanges = DIALOG.getString(getProperty());
    public static final String DIALOG_UnsavedChangesTitle = DIALOG.getString(getProperty());
    public static final String EDITOR3D_AddComment = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_AddCondline = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_AddLine = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_AddQuad = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_AddSubpart = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_AddTriangle = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_AddVertex = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_CloseView = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Combined = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_DragHint = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Exit = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_FarClipping = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_FarClippingHint = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_File = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Grid = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Group = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_ModeLine = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_ModeSubpart = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_ModeSurface = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_ModeVertex = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Move = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_NearClipping = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_NearClippingHint = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_New = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_NewDat = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Open = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_OpenDat = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Origin = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Pipette = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_PositionX1 = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_PositionX2 = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_PositionX3 = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_PositionX4 = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_PositionY1 = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_PositionY2 = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_PositionY3 = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_PositionY4 = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_PositionZ1 = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_PositionZ2 = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_PositionZ3 = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_PositionZ4 = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_ReadyStatus = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Redo = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Rotate = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_RotateClockwise = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Ruler = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Save = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_SaveAll = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Scale = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Select = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Snapshot = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_SplitHorizontally = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_SplitVertically = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Undo = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Ungroup = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_ViewActions = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_ViewingAngles = EDITOR3D.getString(getProperty());
    public static final String EDITORTEXT_Error = EDITORTEXT.getString(getProperty());
    public static final String EDITORTEXT_Errors = EDITORTEXT.getString(getProperty());
    public static final String EDITORTEXT_NewFile = EDITORTEXT.getString(getProperty());
    public static final String EDITORTEXT_Other = EDITORTEXT.getString(getProperty());
    public static final String EDITORTEXT_Others = EDITORTEXT.getString(getProperty());
    public static final String EDITORTEXT_ReadOnly = EDITORTEXT.getString(getProperty());
    public static final String EDITORTEXT_ShowHideErrorTab = EDITORTEXT.getString(getProperty());
    public static final String EDITORTEXT_Warning = EDITORTEXT.getString(getProperty());
    public static final String EDITORTEXT_Warnings = EDITORTEXT.getString(getProperty());
    public static final String PARTS_HiResPrimitives = PARTS.getString(getProperty());
    public static final String PARTS_LowResPrimitives = PARTS.getString(getProperty());
    public static final String PARTS_Parts = PARTS.getString(getProperty());
    public static final String PARTS_Primitives = PARTS.getString(getProperty());
    public static final String PARTS_Subparts = PARTS.getString(getProperty());
    public static final String PERSPECTIVE_BACK = PERSPECTIVE.getString(getProperty());
    public static final String PERSPECTIVE_BOTTOM = PERSPECTIVE.getString(getProperty());
    public static final String PERSPECTIVE_FRONT = PERSPECTIVE.getString(getProperty());
    public static final String PERSPECTIVE_LEFT = PERSPECTIVE.getString(getProperty());
    public static final String PERSPECTIVE_RIGHT = PERSPECTIVE.getString(getProperty());
    public static final String PERSPECTIVE_TOP = PERSPECTIVE.getString(getProperty());
    public static final String PERSPECTIVE_TwoThirds = PERSPECTIVE.getString(getProperty());
    public static final String PERSPECTIVE_Zoom = PERSPECTIVE.getString(getProperty());
    public static final String PROJECT_CreateNewProject = PROJECT.getString(getProperty());
    public static final String PROJECT_DefineProjectLocation = PROJECT.getString(getProperty());
    public static final String PROJECT_NewProject = PROJECT.getString(getProperty());
    public static final String PROJECT_OfficialLibRead = PROJECT.getString(getProperty());
    public static final String PROJECT_ProjectLocation = PROJECT.getString(getProperty());
    public static final String PROJECT_ProjectName = PROJECT.getString(getProperty());
    public static final String PROJECT_ProjectOverwrite = PROJECT.getString(getProperty());
    public static final String PROJECT_ProjectOverwriteTitle = PROJECT.getString(getProperty());
    public static final String PROJECT_SaveProject = PROJECT.getString(getProperty());
    public static final String PROJECT_SelectProjectLocation = PROJECT.getString(getProperty());
    public static final String PROJECT_UnofficialLibReadWrite = PROJECT.getString(getProperty());
    public static final String SPLASH_CheckPlugIn = SPLASH.getString(getProperty());
    public static final String SPLASH_Error = SPLASH.getString(getProperty());
    public static final String SPLASH_InvalidOpenGLVersion = SPLASH.getString(getProperty());
    public static final String SPLASH_LoadWorkbench = SPLASH.getString(getProperty());
    public static final String SPLASH_NoMkDirNoRead = SPLASH.getString(getProperty());
    public static final String SPLASH_NoRead = SPLASH.getString(getProperty());
    public static final String SPLASH_NoWrite = SPLASH.getString(getProperty());
    public static final String SPLASH_Title = SPLASH.getString(getProperty());
    public static final String UNITS_Factor_primary = UNITS.getString(getProperty());
    public static final String UNITS_Factor_secondary = UNITS.getString(getProperty());
    public static final String UNITS_LDU = UNITS.getString(getProperty());
    public static final String UNITS_Name_LDU = UNITS.getString(getProperty());
    public static final String UNITS_Name_primary = UNITS.getString(getProperty());
    public static final String UNITS_Name_secondary = UNITS.getString(getProperty());
    public static final String UNITS_primary = UNITS.getString(getProperty());
    public static final String UNITS_secondary = UNITS.getString(getProperty());
    public static final String VERSION_Contributors = VERSION.getString(getProperty());
    public static final String VERSION_DevelopmentLead = VERSION.getString(getProperty());
    public static final String VERSION_Stage = VERSION.getString(getProperty());
    public static final String VERSION_Testers = VERSION.getString(getProperty());
    public static final String VERSION_Version = VERSION.getString(getProperty());

    // Custom Methods
    public static String UNIT_CurrentUnit() {
        return UNITS.getString(View.unit);
    }

    private static Field[] getSortedFields() {
        Field[] fieldsToSort = I18n.class.getFields();

        TreeMap<String, Field> map = new TreeMap<String, Field>();

        for (Field f : fieldsToSort) {
            map.put(f.getName(), f);
        }

        Collection<Field> fields = map.values();
        return fields.toArray(new Field[fields.size()]);
    }

    // Clever reflection method to get the suffix YYY from XXX_YYY
    private static String getProperty()
    {
        if (notAdjusted) {
            adjust();
        }
        StackTraceElement stackTraceElements[] = new Throwable().getStackTrace();
        String stack = stackTraceElements[1].toString();
        int line_number  = Integer.parseInt(stack.substring(stack.lastIndexOf(":") + 1, stack.lastIndexOf(")"))); //$NON-NLS-1$ //$NON-NLS-2$
        String methodName = fields[line_number - line_offset].getName();
        return methodName.substring(methodName.indexOf("_") + 1); //$NON-NLS-1$
    }

    static { // Make a test if the current locale use RTL layout
        if (!ComponentOrientation.getOrientation(MyLanguage.LOCALE).isLeftToRight()) {
            isRtl = true;
            I18N_RTL = SWT.RIGHT_TO_LEFT;
        } else {
            isRtl = false;
            I18N_RTL = SWT.NONE;
        }
    }
}
