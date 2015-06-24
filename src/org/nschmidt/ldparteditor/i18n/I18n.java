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
    private static final ResourceBundle EDITOR3D = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.Editor3D", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle COLOURDIALOG = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.ColourDialog", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle COMPOSITETAB = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.CompositeTab", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle COORDINATESDIALOG = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.CoordinatesDialog", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle DATFILE = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.DatFile", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle DATPARSER = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.DatParser", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle HINTFIXER = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.HintFixer", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle EDGER = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.Edger", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle META = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.Meta", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle INTERSECTOR = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.Intersector", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle ISECALC = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.Isecalc", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle LINES = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.LinesPattern", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle PATHTRUDER = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.PathTruder", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle RECTIFIER = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.Rectifier", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle RCONES = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.RingsAndCones", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle ROTATE = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.Rotate", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle ROUND = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.Round", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle SCALE = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.Scale", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle SEARCH = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.Search", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle SLICERPRO = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.Slicerpro", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle SORT = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.Sort", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle SYMSPLITTER = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.Symsplitter", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle TRANSLATE = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.Translate", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle TREEITEM = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.TreeItem", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle TXT2DAT = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.Txt2Dat", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle UNIFICATOR = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.Unificator", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle VM = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.VertexManager", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle C3D = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.Composite3D", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle KEYBOARD = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.Keyboard", MyLanguage.LOCALE); //$NON-NLS-1$
    private static final ResourceBundle ERRORFIXER = ResourceBundle.getBundle("org.nschmidt.ldparteditor.i18n.ErrorFixer", MyLanguage.LOCALE); //$NON-NLS-1$
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
    public static final String C3D_Anaglyph3D = C3D.getString(getProperty());
    public static final String C3D_CondLineMode = C3D.getString(getProperty());
    public static final String C3D_GreenRed = C3D.getString(getProperty());
    public static final String C3D_GridSize = C3D.getString(getProperty());
    public static final String C3D_HiddenVertices = C3D.getString(getProperty());
    public static final String C3D_HideAll = C3D.getString(getProperty());
    public static final String C3D_LDrawLines = C3D.getString(getProperty());
    public static final String C3D_LDrawStandard = C3D.getString(getProperty());
    public static final String C3D_Lights = C3D.getString(getProperty());
    public static final String C3D_LockFile = C3D.getString(getProperty());
    public static final String C3D_MeshLines = C3D.getString(getProperty());
    public static final String C3D_NoBackfaceCulling = C3D.getString(getProperty());
    public static final String C3D_OpenInText = C3D.getString(getProperty());
    public static final String C3D_PerspectiveLabel = C3D.getString(getProperty());
    public static final String C3D_PreviewNote = C3D.getString(getProperty());
    public static final String C3D_RandomColours = C3D.getString(getProperty());
    public static final String C3D_RealBackfaceCulling = C3D.getString(getProperty());
    public static final String C3D_RealPreview = C3D.getString(getProperty());
    public static final String C3D_RedBackfaces = C3D.getString(getProperty());
    public static final String C3D_RenderMode = C3D.getString(getProperty());
    public static final String C3D_SetGridSize = C3D.getString(getProperty());
    public static final String C3D_ShowAll = C3D.getString(getProperty());
    public static final String C3D_ShowEdges = C3D.getString(getProperty());
    public static final String C3D_ShowInText = C3D.getString(getProperty());
    public static final String C3D_StudLogo = C3D.getString(getProperty());
    public static final String C3D_SubfileMeshLines = C3D.getString(getProperty());
    public static final String C3D_UseAlwaysBlackLines = C3D.getString(getProperty());
    public static final String C3D_Vertices = C3D.getString(getProperty());
    public static final String C3D_Wireframe = C3D.getString(getProperty());
    public static final String C3D_XYZAxis = C3D.getString(getProperty());
    public static final String COLOURDIALOG_ChooseDirectColour = COLOURDIALOG.getString(getProperty());
    public static final String COLOURDIALOG_Colour = COLOURDIALOG.getString(getProperty());
    public static final String COLOURDIALOG_ColourTitle = COLOURDIALOG.getString(getProperty());
    public static final String COLOURDIALOG_DirectColour = COLOURDIALOG.getString(getProperty());
    public static final String COLOURDIALOG_ShowColourTable = COLOURDIALOG.getString(getProperty());
    public static final String COLOURDIALOG_StandardColours = COLOURDIALOG.getString(getProperty());
    public static final String COMPOSITETAB_Description = COMPOSITETAB.getString(getProperty());
    public static final String COMPOSITETAB_Errors = COMPOSITETAB.getString(getProperty());
    public static final String COMPOSITETAB_FileEncodingError = COMPOSITETAB.getString(getProperty());
    public static final String COMPOSITETAB_FileNotFound = COMPOSITETAB.getString(getProperty());
    public static final String COMPOSITETAB_FileReadError = COMPOSITETAB.getString(getProperty());
    public static final String COMPOSITETAB_Hints = COMPOSITETAB.getString(getProperty());
    public static final String COMPOSITETAB_Location = COMPOSITETAB.getString(getProperty());
    public static final String COMPOSITETAB_NewFile = COMPOSITETAB.getString(getProperty());
    public static final String COMPOSITETAB_Problems = COMPOSITETAB.getString(getProperty());
    public static final String COMPOSITETAB_QuickFix = COMPOSITETAB.getString(getProperty());
    public static final String COMPOSITETAB_QuickFixSimilar = COMPOSITETAB.getString(getProperty());
    public static final String COMPOSITETAB_Type = COMPOSITETAB.getString(getProperty());
    public static final String COMPOSITETAB_Warnings = COMPOSITETAB.getString(getProperty());
    public static final String COORDINATESDIALOG_SetXYZ = COORDINATESDIALOG.getString(getProperty());
    public static final String COORDINATESDIALOG_X = COORDINATESDIALOG.getString(getProperty());
    public static final String COORDINATESDIALOG_Y = COORDINATESDIALOG.getString(getProperty());
    public static final String COORDINATESDIALOG_Z = COORDINATESDIALOG.getString(getProperty());
    public static final String COPYNPASTE_Copy = COPYNPASTE.getString(getProperty());
    public static final String COPYNPASTE_Cut = COPYNPASTE.getString(getProperty());
    public static final String COPYNPASTE_Delete = COPYNPASTE.getString(getProperty());
    public static final String COPYNPASTE_Paste = COPYNPASTE.getString(getProperty());
    public static final String DATFILE_HeaderHint = DATFILE.getString(getProperty());
    public static final String DATFILE_Inlined = DATFILE.getString(getProperty());
    public static final String DATFILE_Line = DATFILE.getString(getProperty());
    public static final String DATFILE_MissingAuthor = DATFILE.getString(getProperty());
    public static final String DATFILE_MissingBFC = DATFILE.getString(getProperty());
    public static final String DATFILE_MissingFileName = DATFILE.getString(getProperty());
    public static final String DATFILE_MissingLicense = DATFILE.getString(getProperty());
    public static final String DATFILE_MissingPartType = DATFILE.getString(getProperty());
    public static final String DATFILE_MissingTitle = DATFILE.getString(getProperty());
    public static final String DATPARSER_CollinearVertices = DATPARSER.getString(getProperty());
    public static final String DATPARSER_ConcaveQuadrilateral = DATPARSER.getString(getProperty());
    public static final String DATPARSER_Coplanarity = DATPARSER.getString(getProperty());
    public static final String DATPARSER_DataError = DATPARSER.getString(getProperty());
    public static final String DATPARSER_DitheredColour = DATPARSER.getString(getProperty());
    public static final String DATPARSER_DuplicatedAuthor = DATPARSER.getString(getProperty());
    public static final String DATPARSER_DuplicatedBFC = DATPARSER.getString(getProperty());
    public static final String DATPARSER_DuplicatedCategory = DATPARSER.getString(getProperty());
    public static final String DATPARSER_DuplicatedCommandLine = DATPARSER.getString(getProperty());
    public static final String DATPARSER_DuplicatedFilename = DATPARSER.getString(getProperty());
    public static final String DATPARSER_DuplicatedLicense = DATPARSER.getString(getProperty());
    public static final String DATPARSER_DuplicatedType = DATPARSER.getString(getProperty());
    public static final String DATPARSER_FileNotFound = DATPARSER.getString(getProperty());
    public static final String DATPARSER_FilenameWhitespace = DATPARSER.getString(getProperty());
    public static final String DATPARSER_HeaderHint = DATPARSER.getString(getProperty());
    public static final String DATPARSER_HistoryWrongOrder = DATPARSER.getString(getProperty());
    public static final String DATPARSER_HourglassQuadrilateral = DATPARSER.getString(getProperty());
    public static final String DATPARSER_IdenticalControlPoints = DATPARSER.getString(getProperty());
    public static final String DATPARSER_IdenticalVertices = DATPARSER.getString(getProperty());
    public static final String DATPARSER_InvalidColour = DATPARSER.getString(getProperty());
    public static final String DATPARSER_InvalidHeader = DATPARSER.getString(getProperty());
    public static final String DATPARSER_InvalidNumberFormat = DATPARSER.getString(getProperty());
    public static final String DATPARSER_InvalidTEXMAP = DATPARSER.getString(getProperty());
    public static final String DATPARSER_InvalidType = DATPARSER.getString(getProperty());
    public static final String DATPARSER_LogicError = DATPARSER.getString(getProperty());
    public static final String DATPARSER_MLCAD_Clip = DATPARSER.getString(getProperty());
    public static final String DATPARSER_MLCAD_ClipCCW = DATPARSER.getString(getProperty());
    public static final String DATPARSER_MLCAD_ClipCW = DATPARSER.getString(getProperty());
    public static final String DATPARSER_MLCAD_InvertNext = DATPARSER.getString(getProperty());
    public static final String DATPARSER_MLCAD_NoClip = DATPARSER.getString(getProperty());
    public static final String DATPARSER_MisplacedAuthor = DATPARSER.getString(getProperty());
    public static final String DATPARSER_MisplacedBFC = DATPARSER.getString(getProperty());
    public static final String DATPARSER_MisplacedBFC0 = DATPARSER.getString(getProperty());
    public static final String DATPARSER_MisplacedCategory = DATPARSER.getString(getProperty());
    public static final String DATPARSER_MisplacedCommandLine = DATPARSER.getString(getProperty());
    public static final String DATPARSER_MisplacedComment = DATPARSER.getString(getProperty());
    public static final String DATPARSER_MisplacedFilename = DATPARSER.getString(getProperty());
    public static final String DATPARSER_MisplacedHelp = DATPARSER.getString(getProperty());
    public static final String DATPARSER_MisplacedHistory = DATPARSER.getString(getProperty());
    public static final String DATPARSER_MisplacedKeyword = DATPARSER.getString(getProperty());
    public static final String DATPARSER_MisplacedLicense = DATPARSER.getString(getProperty());
    public static final String DATPARSER_MisplacedTitle = DATPARSER.getString(getProperty());
    public static final String DATPARSER_MisplacedType = DATPARSER.getString(getProperty());
    public static final String DATPARSER_MovedTo = DATPARSER.getString(getProperty());
    public static final String DATPARSER_MultipleBFC = DATPARSER.getString(getProperty());
    public static final String DATPARSER_NearCoplanarity = DATPARSER.getString(getProperty());
    public static final String DATPARSER_Recursive = DATPARSER.getString(getProperty());
    public static final String DATPARSER_SingularMatrix = DATPARSER.getString(getProperty());
    public static final String DATPARSER_SplitBFC = DATPARSER.getString(getProperty());
    public static final String DATPARSER_SplitCommment = DATPARSER.getString(getProperty());
    public static final String DATPARSER_SplitHelp = DATPARSER.getString(getProperty());
    public static final String DATPARSER_SplitHistory = DATPARSER.getString(getProperty());
    public static final String DATPARSER_SplitKeyword = DATPARSER.getString(getProperty());
    public static final String DATPARSER_SyntaxError = DATPARSER.getString(getProperty());
    public static final String DATPARSER_TODO = DATPARSER.getString(getProperty());
    public static final String DATPARSER_UnknownLineType = DATPARSER.getString(getProperty());
    public static final String DATPARSER_UnofficialMetaCommand = DATPARSER.getString(getProperty());
    public static final String DATPARSER_VertexAt = DATPARSER.getString(getProperty());
    public static final String DATPARSER_VertexDeclaration = DATPARSER.getString(getProperty());
    public static final String DATPARSER_Warning = DATPARSER.getString(getProperty());
    public static final String DATPARSER_WrongArgumentCount = DATPARSER.getString(getProperty());
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
    public static final String DIALOG_Info = DIALOG.getString(getProperty());
    public static final String DIALOG_KeepFilesOpen = DIALOG.getString(getProperty());
    public static final String DIALOG_KeepFilesOpenTitle = DIALOG.getString(getProperty());
    public static final String DIALOG_Modified = DIALOG.getString(getProperty());
    public static final String DIALOG_ModifiedTitle = DIALOG.getString(getProperty());
    public static final String DIALOG_No = DIALOG.getString(getProperty());
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
    public static final String DIALOG_Warning = DIALOG.getString(getProperty());
    public static final String DIALOG_Yes = DIALOG.getString(getProperty());
    public static final String EDGER_0to180 = EDGER.getString(getProperty());
    public static final String EDGER_0to90 = EDGER.getString(getProperty());
    public static final String EDGER_Condition1 = EDGER.getString(getProperty());
    public static final String EDGER_Condition2 = EDGER.getString(getProperty());
    public static final String EDGER_Condition3 = EDGER.getString(getProperty());
    public static final String EDGER_Condition4 = EDGER.getString(getProperty());
    public static final String EDGER_CondlineMaxAngle = EDGER.getString(getProperty());
    public static final String EDGER_EdgeMaxAngle = EDGER.getString(getProperty());
    public static final String EDGER_ExcludeUnmatched = EDGER.getString(getProperty());
    public static final String EDGER_FlatMaxAngle = EDGER.getString(getProperty());
    public static final String EDGER_IncludeUnmatched = EDGER.getString(getProperty());
    public static final String EDGER_Precision = EDGER.getString(getProperty());
    public static final String EDGER_Range = EDGER.getString(getProperty());
    public static final String EDGER_ScopeFile = EDGER.getString(getProperty());
    public static final String EDGER_ScopeFileSubfiles = EDGER.getString(getProperty());
    public static final String EDGER_ScopeSelection = EDGER.getString(getProperty());
    public static final String EDGER_Title = EDGER.getString(getProperty());
    public static final String EDGER_UnmatchedOnly = EDGER.getString(getProperty());
    public static final String EDITOR3D_AddComment = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_AddCondline = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_AddLine = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_AddQuad = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_AddSubpart = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_AddTriangle = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_AddVertex = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_AdvancedSelect = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_All = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_AllFiles = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_AllSameColours = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_AllSameColoursShown = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_AllShown = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_AngleX = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_AngleY = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_AngleZ = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_BackgroundImage = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_CloseView = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Coarse = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Combined = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_CompileSubfileData = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_CondlineToLine = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Connected = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_CopyToUnofficialLibrary = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_CreateNewDat = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Delete = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_DragHint = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Everything = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_ExceptSubfile = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Exit = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_FarClipping = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_FarClippingHint = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_File = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Fine = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Focus = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Global = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Grid = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Group = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Hide = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Image = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_InvalidColour = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_InvalidFilename = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_InvalidMatrix = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Inverse = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_LDrawConfigurationFile1 = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_LDrawConfigurationFile2 = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_LDrawSourceFile = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_LastOpened = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_LineSize1 = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_LineSize2 = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_LineSize3 = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_LineSize4 = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_LineToCondline = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_LoadingData = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_LoadingLibrary = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_LoadingPrimitives = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Local = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_LogUploadData = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_LogUploadLimit = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_LogUploadNoLogFiles = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_LogUploadSuccess = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_LogUploadUnexpectedException = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Medium = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_ModeLine = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_ModeSubpart = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_ModeSurface = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_ModeVertex = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_More = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Move = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_MoveAdjacentData = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_MoveSnap = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_NearClipping = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_NearClippingHint = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_New = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_NewDat = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_NewFile = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Next = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_NextItem = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_NoFileSelected = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_NoPrimitiveSelected = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_None = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Open = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_OpenDat = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_OpenDatFile = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_OpenIn3DEditor = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_OpenInTextEditor = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_OpenLDConfig = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_OpenPngImage = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Origin = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Pipette = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_PortableNetworkGraphics = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_PositionX = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_PositionX1 = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_PositionX2 = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_PositionX3 = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_PositionX4 = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_PositionY = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_PositionY1 = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_PositionY2 = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_PositionY3 = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_PositionY4 = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_PositionZ = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_PositionZ1 = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_PositionZ2 = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_PositionZ3 = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_PositionZ4 = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Previous = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_PreviousItem = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_ReadyStatus = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Redo = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_RenameMove = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Reset = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_RevertAllChanges = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Rotate = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_RotateClockwise = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_RotateSnap = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Round = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Ruler = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Save = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_SaveAll = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Scale = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_ScaleSnap = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_ScaleX = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_ScaleY = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Search = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_SearchPrimitives = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Select = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Selection = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_ShowAll = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Snapshot = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_SplitHorizontally = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_SplitQuad = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_SplitVertically = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_SwapWinding = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_SyncFolders = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_TextLine = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_ToggleBFC = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_ToggleTransparent = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Touching = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Undo = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_Ungroup = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_ViewActions = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_ViewingAngles = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_WaitForUpdate = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_WhatIsHidden = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_WithAccuracy = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_WithSameColour = EDITOR3D.getString(getProperty());
    public static final String EDITOR3D_WithSameOrientation = EDITOR3D.getString(getProperty());
    public static final String EDITORTEXT_Colour1 = EDITORTEXT.getString(getProperty());
    public static final String EDITORTEXT_Colour2 = EDITORTEXT.getString(getProperty());
    public static final String EDITORTEXT_Comment = EDITORTEXT.getString(getProperty());
    public static final String EDITORTEXT_Compile = EDITORTEXT.getString(getProperty());
    public static final String EDITORTEXT_Error = EDITORTEXT.getString(getProperty());
    public static final String EDITORTEXT_Errors = EDITORTEXT.getString(getProperty());
    public static final String EDITORTEXT_FindReplace = EDITORTEXT.getString(getProperty());
    public static final String EDITORTEXT_Inline1 = EDITORTEXT.getString(getProperty());
    public static final String EDITORTEXT_Inline2 = EDITORTEXT.getString(getProperty());
    public static final String EDITORTEXT_Inline3 = EDITORTEXT.getString(getProperty());
    public static final String EDITORTEXT_NewFile = EDITORTEXT.getString(getProperty());
    public static final String EDITORTEXT_Other = EDITORTEXT.getString(getProperty());
    public static final String EDITORTEXT_Others = EDITORTEXT.getString(getProperty());
    public static final String EDITORTEXT_ReadOnly = EDITORTEXT.getString(getProperty());
    public static final String EDITORTEXT_RemoveDuplicates = EDITORTEXT.getString(getProperty());
    public static final String EDITORTEXT_Round = EDITORTEXT.getString(getProperty());
    public static final String EDITORTEXT_ShowHideErrorTab = EDITORTEXT.getString(getProperty());
    public static final String EDITORTEXT_Sort = EDITORTEXT.getString(getProperty());
    public static final String EDITORTEXT_SplitQuad = EDITORTEXT.getString(getProperty());
    public static final String EDITORTEXT_Texmap = EDITORTEXT.getString(getProperty());
    public static final String EDITORTEXT_Unrectify = EDITORTEXT.getString(getProperty());
    public static final String EDITORTEXT_Warning = EDITORTEXT.getString(getProperty());
    public static final String EDITORTEXT_Warnings = EDITORTEXT.getString(getProperty());
    public static final String ERRORFIXER_MovedTo = ERRORFIXER.getString(getProperty());
    public static final String ERRORFIXER_MovedToHint = ERRORFIXER.getString(getProperty());
    public static final String HINTFIXER_Title = HINTFIXER.getString(getProperty());
    public static final String INTERSECTOR_ColourMods = INTERSECTOR.getString(getProperty());
    public static final String INTERSECTOR_NoMods = INTERSECTOR.getString(getProperty());
    public static final String INTERSECTOR_ScopeFile = INTERSECTOR.getString(getProperty());
    public static final String INTERSECTOR_ScopeSelection = INTERSECTOR.getString(getProperty());
    public static final String INTERSECTOR_Title = INTERSECTOR.getString(getProperty());
    public static final String ISECALC_ScopeFile = ISECALC.getString(getProperty());
    public static final String ISECALC_ScopeSelection = ISECALC.getString(getProperty());
    public static final String ISECALC_Title = ISECALC.getString(getProperty());
    public static final String KEYBOARD_Alt = KEYBOARD.getString(getProperty());
    public static final String KEYBOARD_Ctrl = KEYBOARD.getString(getProperty());
    public static final String KEYBOARD_Del = KEYBOARD.getString(getProperty());
    public static final String KEYBOARD_Shift = KEYBOARD.getString(getProperty());
    public static final String LINES_Hint = LINES.getString(getProperty());
    public static final String LINES_ScopeSelection = LINES.getString(getProperty());
    public static final String LINES_Title = LINES.getString(getProperty());
    public static final String META_Author = META.getString(getProperty());
    public static final String META_BackFaceCulling = META.getString(getProperty());
    public static final String META_CSGCompile = META.getString(getProperty());
    public static final String META_CSGEpsilon1 = META.getString(getProperty());
    public static final String META_CSGEpsilon2 = META.getString(getProperty());
    public static final String META_CSGSource1 = META.getString(getProperty());
    public static final String META_CSGSource2 = META.getString(getProperty());
    public static final String META_CSGSource3 = META.getString(getProperty());
    public static final String META_CSGTarget1 = META.getString(getProperty());
    public static final String META_CSGUnique = META.getString(getProperty());
    public static final String META_CSGUniqueHint = META.getString(getProperty());
    public static final String META_ChoosePng = META.getString(getProperty());
    public static final String META_Colour = META.getString(getProperty());
    public static final String META_ColourHint = META.getString(getProperty());
    public static final String META_CommandLine = META.getString(getProperty());
    public static final String META_Comment = META.getString(getProperty());
    public static final String META_CylinderBottomCenter = META.getString(getProperty());
    public static final String META_CylinderTopCenter = META.getString(getProperty());
    public static final String META_DecimalMark = META.getString(getProperty());
    public static final String META_Description = META.getString(getProperty());
    public static final String META_Filename = META.getString(getProperty());
    public static final String META_Help = META.getString(getProperty());
    public static final String META_History1 = META.getString(getProperty());
    public static final String META_History2 = META.getString(getProperty());
    public static final String META_History3 = META.getString(getProperty());
    public static final String META_History4 = META.getString(getProperty());
    public static final String META_Keywords1 = META.getString(getProperty());
    public static final String META_Keywords2 = META.getString(getProperty());
    public static final String META_LDrawHeader = META.getString(getProperty());
    public static final String META_LPE = META.getString(getProperty());
    public static final String META_M00 = META.getString(getProperty());
    public static final String META_M01 = META.getString(getProperty());
    public static final String META_M02 = META.getString(getProperty());
    public static final String META_M10 = META.getString(getProperty());
    public static final String META_M11 = META.getString(getProperty());
    public static final String META_M12 = META.getString(getProperty());
    public static final String META_M20 = META.getString(getProperty());
    public static final String META_M21 = META.getString(getProperty());
    public static final String META_M22 = META.getString(getProperty());
    public static final String META_NewLineNote = META.getString(getProperty());
    public static final String META_Quality = META.getString(getProperty());
    public static final String META_RotationX = META.getString(getProperty());
    public static final String META_RotationY = META.getString(getProperty());
    public static final String META_RotationZ = META.getString(getProperty());
    public static final String META_ScaleX = META.getString(getProperty());
    public static final String META_ScaleY = META.getString(getProperty());
    public static final String META_TextureAngle1 = META.getString(getProperty());
    public static final String META_TextureAngle2 = META.getString(getProperty());
    public static final String META_TextureBottomCenter = META.getString(getProperty());
    public static final String META_TextureCenter = META.getString(getProperty());
    public static final String META_TextureGeom1 = META.getString(getProperty());
    public static final String META_TextureGeom2 = META.getString(getProperty());
    public static final String META_TextureMapping = META.getString(getProperty());
    public static final String META_TexturePNG = META.getString(getProperty());
    public static final String META_TextureSphereCenter = META.getString(getProperty());
    public static final String META_TextureTopCenter = META.getString(getProperty());
    public static final String META_TextureX1 = META.getString(getProperty());
    public static final String META_TextureX2 = META.getString(getProperty());
    public static final String META_TextureX3 = META.getString(getProperty());
    public static final String META_TextureY1 = META.getString(getProperty());
    public static final String META_TextureY2 = META.getString(getProperty());
    public static final String META_TextureY3 = META.getString(getProperty());
    public static final String META_TextureZ1 = META.getString(getProperty());
    public static final String META_TextureZ2 = META.getString(getProperty());
    public static final String META_TextureZ3 = META.getString(getProperty());
    public static final String META_Todo = META.getString(getProperty());
    public static final String META_TransMatrix = META.getString(getProperty());
    public static final String META_Username = META.getString(getProperty());
    public static final String META_VertexX = META.getString(getProperty());
    public static final String META_VertexY = META.getString(getProperty());
    public static final String META_VertexZ = META.getString(getProperty());
    public static final String META_YearRelease = META.getString(getProperty());
    public static final String PARTS_HiResPrimitives = PARTS.getString(getProperty());
    public static final String PARTS_LowResPrimitives = PARTS.getString(getProperty());
    public static final String PARTS_Parts = PARTS.getString(getProperty());
    public static final String PARTS_Primitives = PARTS.getString(getProperty());
    public static final String PARTS_Subparts = PARTS.getString(getProperty());
    public static final String PATHTRUDER_ColourCodes = PATHTRUDER.getString(getProperty());
    public static final String PATHTRUDER_ControlCurve = PATHTRUDER.getString(getProperty());
    public static final String PATHTRUDER_ControlCurveCenter = PATHTRUDER.getString(getProperty());
    public static final String PATHTRUDER_InvertShape = PATHTRUDER.getString(getProperty());
    public static final String PATHTRUDER_InvertShape1 = PATHTRUDER.getString(getProperty());
    public static final String PATHTRUDER_InvertShape2 = PATHTRUDER.getString(getProperty());
    public static final String PATHTRUDER_LineThresh = PATHTRUDER.getString(getProperty());
    public static final String PATHTRUDER_MaxPathLength = PATHTRUDER.getString(getProperty());
    public static final String PATHTRUDER_NumTransitions = PATHTRUDER.getString(getProperty());
    public static final String PATHTRUDER_RotAngle = PATHTRUDER.getString(getProperty());
    public static final String PATHTRUDER_ShapeComp = PATHTRUDER.getString(getProperty());
    public static final String PATHTRUDER_ShapeComp1 = PATHTRUDER.getString(getProperty());
    public static final String PATHTRUDER_ShapeComp2 = PATHTRUDER.getString(getProperty());
    public static final String PATHTRUDER_Title = PATHTRUDER.getString(getProperty());
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
    public static final String RCONES_Angle = RCONES.getString(getProperty());
    public static final String RCONES_Angle01 = RCONES.getString(getProperty());
    public static final String RCONES_Angle02 = RCONES.getString(getProperty());
    public static final String RCONES_Angle03 = RCONES.getString(getProperty());
    public static final String RCONES_Angle04 = RCONES.getString(getProperty());
    public static final String RCONES_Angle05 = RCONES.getString(getProperty());
    public static final String RCONES_Angle06 = RCONES.getString(getProperty());
    public static final String RCONES_Angle07 = RCONES.getString(getProperty());
    public static final String RCONES_Angle08 = RCONES.getString(getProperty());
    public static final String RCONES_Angle09 = RCONES.getString(getProperty());
    public static final String RCONES_Angle10 = RCONES.getString(getProperty());
    public static final String RCONES_Angle11 = RCONES.getString(getProperty());
    public static final String RCONES_Angle12 = RCONES.getString(getProperty());
    public static final String RCONES_Angle13 = RCONES.getString(getProperty());
    public static final String RCONES_Angle14 = RCONES.getString(getProperty());
    public static final String RCONES_Angle15 = RCONES.getString(getProperty());
    public static final String RCONES_Angle16 = RCONES.getString(getProperty());
    public static final String RCONES_Angle17 = RCONES.getString(getProperty());
    public static final String RCONES_Angle18 = RCONES.getString(getProperty());
    public static final String RCONES_Angle19 = RCONES.getString(getProperty());
    public static final String RCONES_Angle20 = RCONES.getString(getProperty());
    public static final String RCONES_Angle21 = RCONES.getString(getProperty());
    public static final String RCONES_Angle22 = RCONES.getString(getProperty());
    public static final String RCONES_Angle23 = RCONES.getString(getProperty());
    public static final String RCONES_Angle24 = RCONES.getString(getProperty());
    public static final String RCONES_Angle25 = RCONES.getString(getProperty());
    public static final String RCONES_Angle26 = RCONES.getString(getProperty());
    public static final String RCONES_Angle27 = RCONES.getString(getProperty());
    public static final String RCONES_Angle28 = RCONES.getString(getProperty());
    public static final String RCONES_Angle29 = RCONES.getString(getProperty());
    public static final String RCONES_Angle30 = RCONES.getString(getProperty());
    public static final String RCONES_Angle31 = RCONES.getString(getProperty());
    public static final String RCONES_Angle32 = RCONES.getString(getProperty());
    public static final String RCONES_Angle33 = RCONES.getString(getProperty());
    public static final String RCONES_Angle34 = RCONES.getString(getProperty());
    public static final String RCONES_Angle35 = RCONES.getString(getProperty());
    public static final String RCONES_Angle36 = RCONES.getString(getProperty());
    public static final String RCONES_Angle37 = RCONES.getString(getProperty());
    public static final String RCONES_Angle38 = RCONES.getString(getProperty());
    public static final String RCONES_Angle39 = RCONES.getString(getProperty());
    public static final String RCONES_Angle40 = RCONES.getString(getProperty());
    public static final String RCONES_Angle41 = RCONES.getString(getProperty());
    public static final String RCONES_Angle42 = RCONES.getString(getProperty());
    public static final String RCONES_Angle43 = RCONES.getString(getProperty());
    public static final String RCONES_Angle44 = RCONES.getString(getProperty());
    public static final String RCONES_Angle45 = RCONES.getString(getProperty());
    public static final String RCONES_Angle46 = RCONES.getString(getProperty());
    public static final String RCONES_Angle47 = RCONES.getString(getProperty());
    public static final String RCONES_Angle48 = RCONES.getString(getProperty());
    public static final String RCONES_Cone = RCONES.getString(getProperty());
    public static final String RCONES_Cone48 = RCONES.getString(getProperty());
    public static final String RCONES_Create1 = RCONES.getString(getProperty());
    public static final String RCONES_Create2 = RCONES.getString(getProperty());
    public static final String RCONES_Height = RCONES.getString(getProperty());
    public static final String RCONES_Hint = RCONES.getString(getProperty());
    public static final String RCONES_NoSolution = RCONES.getString(getProperty());
    public static final String RCONES_Prims1 = RCONES.getString(getProperty());
    public static final String RCONES_Prims2 = RCONES.getString(getProperty());
    public static final String RCONES_Radius1 = RCONES.getString(getProperty());
    public static final String RCONES_Radius2 = RCONES.getString(getProperty());
    public static final String RCONES_Ring = RCONES.getString(getProperty());
    public static final String RCONES_Ring48 = RCONES.getString(getProperty());
    public static final String RCONES_Shape = RCONES.getString(getProperty());
    public static final String RCONES_Task = RCONES.getString(getProperty());
    public static final String RCONES_Title = RCONES.getString(getProperty());
    public static final String RECTIFIER_Colour1 = RECTIFIER.getString(getProperty());
    public static final String RECTIFIER_Colour2 = RECTIFIER.getString(getProperty());
    public static final String RECTIFIER_MaxAngle = RECTIFIER.getString(getProperty());
    public static final String RECTIFIER_Rect1 = RECTIFIER.getString(getProperty());
    public static final String RECTIFIER_Rect2 = RECTIFIER.getString(getProperty());
    public static final String RECTIFIER_Rect3 = RECTIFIER.getString(getProperty());
    public static final String RECTIFIER_Rect4 = RECTIFIER.getString(getProperty());
    public static final String RECTIFIER_ScopeFile = RECTIFIER.getString(getProperty());
    public static final String RECTIFIER_ScopeSelection = RECTIFIER.getString(getProperty());
    public static final String RECTIFIER_Title = RECTIFIER.getString(getProperty());
    public static final String RECTIFIER_TriQuads1 = RECTIFIER.getString(getProperty());
    public static final String RECTIFIER_TriQuads2 = RECTIFIER.getString(getProperty());
    public static final String ROTATE_Pivot = ROTATE.getString(getProperty());
    public static final String ROTATE_Title = ROTATE.getString(getProperty());
    public static final String ROTATE_X = ROTATE.getString(getProperty());
    public static final String ROTATE_Y = ROTATE.getString(getProperty());
    public static final String ROTATE_Z = ROTATE.getString(getProperty());
    public static final String ROUND_CoordPrecision = ROUND.getString(getProperty());
    public static final String ROUND_InDecPlaces = ROUND.getString(getProperty());
    public static final String ROUND_MatrixPrecision = ROUND.getString(getProperty());
    public static final String ROUND_Title = ROUND.getString(getProperty());
    public static final String SCALE_Pivot = SCALE.getString(getProperty());
    public static final String SCALE_Title = SCALE.getString(getProperty());
    public static final String SCALE_X = SCALE.getString(getProperty());
    public static final String SCALE_Y = SCALE.getString(getProperty());
    public static final String SCALE_Z = SCALE.getString(getProperty());
    public static final String SEARCH_All = SEARCH.getString(getProperty());
    public static final String SEARCH_Backward = SEARCH.getString(getProperty());
    public static final String SEARCH_CaseSensitive = SEARCH.getString(getProperty());
    public static final String SEARCH_Close = SEARCH.getString(getProperty());
    public static final String SEARCH_Direction = SEARCH.getString(getProperty());
    public static final String SEARCH_Find = SEARCH.getString(getProperty());
    public static final String SEARCH_Find2 = SEARCH.getString(getProperty());
    public static final String SEARCH_Forward = SEARCH.getString(getProperty());
    public static final String SEARCH_Incremental = SEARCH.getString(getProperty());
    public static final String SEARCH_Options = SEARCH.getString(getProperty());
    public static final String SEARCH_Replace = SEARCH.getString(getProperty());
    public static final String SEARCH_ReplaceAll = SEARCH.getString(getProperty());
    public static final String SEARCH_ReplaceFind = SEARCH.getString(getProperty());
    public static final String SEARCH_ReplaceWith = SEARCH.getString(getProperty());
    public static final String SEARCH_Scope = SEARCH.getString(getProperty());
    public static final String SEARCH_SelectedLines = SEARCH.getString(getProperty());
    public static final String SLICERPRO_Title = SLICERPRO.getString(getProperty());
    public static final String SORT_ByColourAsc = SORT.getString(getProperty());
    public static final String SORT_ByColourDesc = SORT.getString(getProperty());
    public static final String SORT_ByTypeAsc = SORT.getString(getProperty());
    public static final String SORT_ByTypeColourAsc = SORT.getString(getProperty());
    public static final String SORT_ByTypeColourDesc = SORT.getString(getProperty());
    public static final String SORT_ByTypeDesc = SORT.getString(getProperty());
    public static final String SORT_ScopeFile = SORT.getString(getProperty());
    public static final String SORT_ScopeSelection = SORT.getString(getProperty());
    public static final String SORT_Title = SORT.getString(getProperty());
    public static final String SPLASH_CheckPlugIn = SPLASH.getString(getProperty());
    public static final String SPLASH_Error = SPLASH.getString(getProperty());
    public static final String SPLASH_InvalidOpenGLVersion = SPLASH.getString(getProperty());
    public static final String SPLASH_LoadWorkbench = SPLASH.getString(getProperty());
    public static final String SPLASH_NoMkDirNoRead = SPLASH.getString(getProperty());
    public static final String SPLASH_NoRead = SPLASH.getString(getProperty());
    public static final String SPLASH_NoWrite = SPLASH.getString(getProperty());
    public static final String SPLASH_Title = SPLASH.getString(getProperty());
    public static final String SYMSPLITTER_Colourise = SYMSPLITTER.getString(getProperty());
    public static final String SYMSPLITTER_Cut = SYMSPLITTER.getString(getProperty());
    public static final String SYMSPLITTER_DoNotCut = SYMSPLITTER.getString(getProperty());
    public static final String SYMSPLITTER_Hint = SYMSPLITTER.getString(getProperty());
    public static final String SYMSPLITTER_NoValidation = SYMSPLITTER.getString(getProperty());
    public static final String SYMSPLITTER_NotColourise = SYMSPLITTER.getString(getProperty());
    public static final String SYMSPLITTER_ScopeFile = SYMSPLITTER.getString(getProperty());
    public static final String SYMSPLITTER_ScopeSelection = SYMSPLITTER.getString(getProperty());
    public static final String SYMSPLITTER_SelectWhat = SYMSPLITTER.getString(getProperty());
    public static final String SYMSPLITTER_ShowAll = SYMSPLITTER.getString(getProperty());
    public static final String SYMSPLITTER_ShowBehind = SYMSPLITTER.getString(getProperty());
    public static final String SYMSPLITTER_ShowFront = SYMSPLITTER.getString(getProperty());
    public static final String SYMSPLITTER_ShowMiddle = SYMSPLITTER.getString(getProperty());
    public static final String SYMSPLITTER_SplittingPlane = SYMSPLITTER.getString(getProperty());
    public static final String SYMSPLITTER_Title = SYMSPLITTER.getString(getProperty());
    public static final String SYMSPLITTER_Validation = SYMSPLITTER.getString(getProperty());
    public static final String SYMSPLITTER_VertexThreshold = SYMSPLITTER.getString(getProperty());
    public static final String SYMSPLITTER_Xm = SYMSPLITTER.getString(getProperty());
    public static final String SYMSPLITTER_Xp = SYMSPLITTER.getString(getProperty());
    public static final String SYMSPLITTER_Ym = SYMSPLITTER.getString(getProperty());
    public static final String SYMSPLITTER_Yp = SYMSPLITTER.getString(getProperty());
    public static final String SYMSPLITTER_Zm = SYMSPLITTER.getString(getProperty());
    public static final String SYMSPLITTER_Zp = SYMSPLITTER.getString(getProperty());
    public static final String TRANSLATE_Title = TRANSLATE.getString(getProperty());
    public static final String TRANSLATE_X = TRANSLATE.getString(getProperty());
    public static final String TRANSLATE_Y = TRANSLATE.getString(getProperty());
    public static final String TRANSLATE_Z = TRANSLATE.getString(getProperty());
    public static final String TREEITEM_Line = TREEITEM.getString(getProperty());
    public static final String TXT2DAT_Angle = TXT2DAT.getString(getProperty());
    public static final String TXT2DAT_Flatness = TXT2DAT.getString(getProperty());
    public static final String TXT2DAT_Font = TXT2DAT.getString(getProperty());
    public static final String TXT2DAT_FontHeight = TXT2DAT.getString(getProperty());
    public static final String TXT2DAT_InterpolateFlatness = TXT2DAT.getString(getProperty());
    public static final String TXT2DAT_Select = TXT2DAT.getString(getProperty());
    public static final String TXT2DAT_Text = TXT2DAT.getString(getProperty());
    public static final String TXT2DAT_Title = TXT2DAT.getString(getProperty());
    public static final String TXT2DAT_Triangulate = TXT2DAT.getString(getProperty());
    public static final String UNIFICATOR_ScopeFile = UNIFICATOR.getString(getProperty());
    public static final String UNIFICATOR_ScopeSelection = UNIFICATOR.getString(getProperty());
    public static final String UNIFICATOR_SnapOn = UNIFICATOR.getString(getProperty());
    public static final String UNIFICATOR_SubpartVertices = UNIFICATOR.getString(getProperty());
    public static final String UNIFICATOR_Title = UNIFICATOR.getString(getProperty());
    public static final String UNIFICATOR_VertexSnap = UNIFICATOR.getString(getProperty());
    public static final String UNIFICATOR_VertexUnifiation = UNIFICATOR.getString(getProperty());
    public static final String UNIFICATOR_Vertices = UNIFICATOR.getString(getProperty());
    public static final String UNIFICATOR_VerticesSubpartVertices = UNIFICATOR.getString(getProperty());
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
    public static final String VM_DetectNewEdges = VM.getString(getProperty());
    public static final String VM_FlatScaledX = VM.getString(getProperty());
    public static final String VM_FlatScaledY = VM.getString(getProperty());
    public static final String VM_FlatScaledZ = VM.getString(getProperty());
    public static final String VM_Intersector = VM.getString(getProperty());
    public static final String VM_Lines2Pattern = VM.getString(getProperty());
    public static final String VM_PathTruder = VM.getString(getProperty());
    public static final String VM_Rectify = VM.getString(getProperty());
    public static final String VM_SearchIntersection = VM.getString(getProperty());
    public static final String VM_Selecting = VM.getString(getProperty());
    public static final String VM_Slicerpro = VM.getString(getProperty());
    public static final String VM_Snap = VM.getString(getProperty());
    public static final String VM_SortOut = VM.getString(getProperty());
    public static final String VM_SymsplitterBehind = VM.getString(getProperty());
    public static final String VM_SymsplitterBetween = VM.getString(getProperty());
    public static final String VM_SymsplitterFront = VM.getString(getProperty());
    public static final String VM_Triangulate = VM.getString(getProperty());
    public static final String VM_Unificator = VM.getString(getProperty());
    public static final String VM_Unify = VM.getString(getProperty());

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
