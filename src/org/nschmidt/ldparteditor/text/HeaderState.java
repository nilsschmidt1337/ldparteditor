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
package org.nschmidt.ldparteditor.text;

/**
 * @author nils
 *
 */
public class HeaderState {

    public static final int H00_TITLE = 0;
    public static final int H01_NAME = 1;
    public static final int H02_AUTHOR = 2;
    public static final int H03_TYPE = 3;
    public static final int H04_LICENSE = 4;
    public static final int H05_OPTIONAL_HELP = 5;
    public static final int H06_BFC = 6;
    public static final int H07_OPTIONAL_CATEGORY = 7;
    public static final int H08_OPTIONAL_KEYWORDS = 8;
    public static final int H09_OPTIONAL_CMDLINE = 9;
    public static final int H10_OPTIONAL_HISTORY = 10;
    public static final int H11_OPTIONAL_COMMENT = 11;
    public static final int H12_OPTIONAL_BFC2 = 12;

    private boolean hasTITLE = false;
    private boolean hasNAME = false;
    private boolean hasAUTHOR = false;
    private boolean hasTYPE = false;
    private boolean hasUNOFFICIAL = true;
    private boolean hasUPDATE = false;
    private boolean hasLICENSE = false;
    private boolean hasHELP = false;
    private boolean hasBFC = false;
    private boolean hasCATEGORY = false;
    private boolean hasKEYWORDS = false;
    private boolean hasCMDLINE = false;
    private boolean hasHISTORY = false;
    private boolean hasCOMMENT = false;
    private boolean hasBFC2 = false;

    private int lineTITLE = 0;
    private int lineNAME = 0;
    private int lineAUTHOR = 0;
    private int lineTYPE = -1;
    private int lineLICENSE = 0;
    private int lineHELP_start = 0;
    private int lineHELP_end = 0;
    private int lineBFC = 0;
    private int lineCATEGORY = 0;
    private int lineKEYWORDS_start = 0;
    private int lineKEYWORDS_end = 0;
    private int lineCMDLINE = 0;
    private int lineHISTORY_start = 0;
    private int lineHISTORY_end = 0;
    private int lineCOMMENT_start = 0;
    private int lineCOMMENT_end = 0;
    private int lineBFC2_start = 0;
    private int lineBFC2_end = 0;

    private int state = 0;
    private String lastHistoryEntry = null;

    public boolean hasTITLE() {
        return hasTITLE;
    }

    public void setHasTITLE(boolean hasTITLE) {
        this.hasTITLE = hasTITLE;
    }

    public boolean hasNAME() {
        return hasNAME;
    }

    public void setHasNAME(boolean hasNAME) {
        this.hasNAME = hasNAME;
    }

    public boolean hasAUTHOR() {
        return hasAUTHOR;
    }

    public void setHasAUTHOR(boolean hasAUTHOR) {
        this.hasAUTHOR = hasAUTHOR;
    }

    public boolean hasTYPE() {
        return hasTYPE;
    }

    public void setHasTYPE(boolean hasTYPE) {
        this.hasTYPE = hasTYPE;
    }

    public boolean hasUNOFFICIAL() {
        return hasUNOFFICIAL;
    }

    public void setHasUNOFFICIAL(boolean hasUNOFFICIAL) {
        this.hasUNOFFICIAL = hasUNOFFICIAL;
    }

    public boolean hasUPDATE() {
        return hasUPDATE;
    }

    public void setHasUPDATE(boolean hasUPDATE) {
        this.hasUPDATE = hasUPDATE;
    }

    public boolean hasHELP() {
        return hasHELP;
    }

    public void setHasHELP(boolean hasHELP) {
        this.hasHELP = hasHELP;
    }

    public boolean hasBFC() {
        return hasBFC;
    }

    public void setHasBFC(boolean hasBFC) {
        this.hasBFC = hasBFC;
    }

    public boolean hasCATEGORY() {
        return hasCATEGORY;
    }

    public void setHasCATEGORY(boolean hasCATEGORY) {
        this.hasCATEGORY = hasCATEGORY;
    }

    public boolean hasKEYWORDS() {
        return hasKEYWORDS;
    }

    public void setHasKEYWORDS(boolean hasKEYWORDS) {
        this.hasKEYWORDS = hasKEYWORDS;
    }

    public boolean hasCMDLINE() {
        return hasCMDLINE;
    }

    public void setHasCMDLINE(boolean hasCMDLINE) {
        this.hasCMDLINE = hasCMDLINE;
    }

    public boolean hasHISTORY() {
        return hasHISTORY;
    }

    public void setHasHISTORY(boolean hasHISTORY) {
        this.hasHISTORY = hasHISTORY;
    }

    public boolean hasCOMMENT() {
        return hasCOMMENT;
    }

    public void setHasCOMMENT(boolean hasCOMMENT) {
        this.hasCOMMENT = hasCOMMENT;
    }

    public boolean hasBFC2() {
        return hasBFC2;
    }

    public void setHasBFC2(boolean hasBFC2) {
        this.hasBFC2 = hasBFC2;
    }

    public int getLineTITLE() {
        return lineTITLE;
    }

    public void setLineTITLE(int lineTITLE) {
        this.lineTITLE = lineTITLE;
    }

    public int getLineNAME() {
        return lineNAME;
    }

    public void setLineNAME(int lineNAME) {
        this.lineNAME = lineNAME;
    }

    public int getLineAUTHOR() {
        return lineAUTHOR;
    }

    public void setLineAUTHOR(int lineAUTHOR) {
        this.lineAUTHOR = lineAUTHOR;
    }

    public int getLineTYPE() {
        return lineTYPE;
    }

    public void setLineTYPE(int lineTYPE) {
        this.lineTYPE = lineTYPE;
    }

    public int getLineLICENSE() {
        return lineLICENSE;
    }

    public void setLineLICENSE(int lineLICENSE) {
        this.lineLICENSE = lineLICENSE;
    }

    public int getLineHELP_start() {
        return lineHELP_start;
    }

    public void setLineHELP_start(int lineHELPstart) {
        this.lineHELP_start = lineHELPstart;
    }

    public int getLineHELP_end() {
        return lineHELP_end;
    }

    public void setLineHELP_end(int lineHELPend) {
        this.lineHELP_end = lineHELPend;
    }

    public int getLineBFC() {
        return lineBFC;
    }

    public void setLineBFC(int lineBFC) {
        this.lineBFC = lineBFC;
    }

    public int getLineCATEGORY() {
        return lineCATEGORY;
    }

    public void setLineCATEGORY(int lineCATEGORY) {
        this.lineCATEGORY = lineCATEGORY;
    }

    public int getLineKEYWORDS_start() {
        return lineKEYWORDS_start;
    }

    public void setLineKEYWORDS_start(int lineKEYWORDSstart) {
        this.lineKEYWORDS_start = lineKEYWORDSstart;
    }

    public int getLineKEYWORDS_end() {
        return lineKEYWORDS_end;
    }

    public void setLineKEYWORDS_end(int lineKEYWORDSend) {
        this.lineKEYWORDS_end = lineKEYWORDSend;
    }

    public int getLineCMDLINE() {
        return lineCMDLINE;
    }

    public void setLineCMDLINE(int lineCMDLINE) {
        this.lineCMDLINE = lineCMDLINE;
    }

    public int getLineHISTORY_start() {
        return lineHISTORY_start;
    }

    public void setLineHISTORY_start(int lineHISTORYstart) {
        this.lineHISTORY_start = lineHISTORYstart;
    }

    public int getLineHISTORY_end() {
        return lineHISTORY_end;
    }

    public void setLineHISTORY_end(int lineHISTORYend) {
        this.lineHISTORY_end = lineHISTORYend;
    }

    public int getLineCOMMENT_start() {
        return lineCOMMENT_start;
    }

    public void setLineCOMMENT_start(int lineCOMMENTstart) {
        this.lineCOMMENT_start = lineCOMMENTstart;
    }

    public int getLineCOMMENT_end() {
        return lineCOMMENT_end;
    }

    public void setLineCOMMENT_end(int lineCOMMENTend) {
        this.lineCOMMENT_end = lineCOMMENTend;
    }

    public int getLineBFC2_start() {
        return lineBFC2_start;
    }

    public void setLineBFC2_start(int lineBFC2start) {
        this.lineBFC2_start = lineBFC2start;
    }

    public int getLineBFC2_end() {
        return lineBFC2_end;
    }

    public void setLineBFC2_end(int lineBFC2end) {
        this.lineBFC2_end = lineBFC2end;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getLastHistoryEntry() {
        return lastHistoryEntry;
    }

    public void setLastHistoryEntry(String lastHistoryEntry) {
        this.lastHistoryEntry = lastHistoryEntry;
    }

    public HeaderState createClone() {
        HeaderState h = new HeaderState();
        h.hasTITLE = hasTITLE;
        h.hasNAME = hasNAME;
        h.hasAUTHOR = hasAUTHOR;
        h.hasTYPE = hasTYPE;
        h.hasUNOFFICIAL = hasUNOFFICIAL;
        h.hasLICENSE = hasLICENSE;
        h.hasHELP = hasHELP;
        h.hasBFC = hasBFC;
        h.hasCATEGORY = hasCATEGORY;
        h.hasKEYWORDS = hasKEYWORDS;
        h.hasCMDLINE = hasCMDLINE;
        h.hasHISTORY = hasHISTORY;
        h.hasCOMMENT = hasCOMMENT;
        h.hasBFC2 = hasBFC2;
        h.lineTITLE = lineTITLE;
        h.lineNAME = lineNAME;
        h.lineAUTHOR = lineAUTHOR;
        h.lineTYPE = lineTYPE;
        h.lineLICENSE = lineLICENSE;
        h.lineHELP_start = lineHELP_start;
        h.lineHELP_end = lineHELP_end;
        h.lineBFC = lineBFC;
        h.lineCATEGORY = lineCATEGORY;
        h.lineKEYWORDS_start = lineKEYWORDS_start;
        h.lineKEYWORDS_end = lineKEYWORDS_end;
        h.lineCMDLINE = lineCMDLINE;
        h.lineHISTORY_start = lineHISTORY_start;
        h.lineHISTORY_end = lineHISTORY_end;
        h.lineCOMMENT_start = lineCOMMENT_start;
        h.lineCOMMENT_end = lineCOMMENT_end;
        h.lineBFC2_start = lineBFC2_start;
        h.lineBFC2_end = lineBFC2_end;
        h.state = state;
        h.lastHistoryEntry = lastHistoryEntry;
        return h;
    }

    public boolean hasLICENSE() {
        return hasLICENSE;
    }

    public void setHasLICENSE(boolean hasLICENSE) {
        this.hasLICENSE = hasLICENSE;
    }
}
