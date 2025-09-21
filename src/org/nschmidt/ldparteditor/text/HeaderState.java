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
    public static final int H10_OPTIONAL_PREVIEW = 10;
    public static final int H11_OPTIONAL_HISTORY = 11;
    public static final int H12_OPTIONAL_COMMENT = 12;
    public static final int H13_OPTIONAL_BFC2 = 13;

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
    private boolean hasPREVIEW = false;
    private boolean hasHISTORY = false;
    private boolean hasCOMMENT = false;
    private boolean hasBFC2 = false;

    private int lineTITLE = 0;
    private int lineNAME = 0;
    private int lineAUTHOR = 0;
    private int lineTYPE = -1;
    private int lineLICENSE = 0;
    private int lineHELPstart = 0;
    private int lineHELPend = 0;
    private int lineBFC = 0;
    private int lineCATEGORY = 0;
    private int lineKEYWORDSstart = 0;
    private int lineKEYWORDSend = 0;
    private int lineCMDLINE = 0;
    private int lineHISTORYstart = 0;
    private int lineHISTORYend = 0;
    private int lineCOMMENTstart = 0;
    private int lineCOMMENTend = 0;
    private int lineBFC2start = 0;
    private int lineBFC2end = 0;

    private int state = 0;
    private String lastHistoryEntry = null;
    private String description = null;

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

    public boolean hasPREVIEW() {
        return hasPREVIEW;
    }

    public void setHasPREVIEW(boolean hasPREVIEW) {
        this.hasPREVIEW = hasPREVIEW;
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

    public int getLineHELPstart() {
        return lineHELPstart;
    }

    public void setLineHELPstart(int lineHELPstart) {
        this.lineHELPstart = lineHELPstart;
    }

    public int getLineHELPend() {
        return lineHELPend;
    }

    public void setLineHELPend(int lineHELPend) {
        this.lineHELPend = lineHELPend;
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

    public int getLineKEYWORDSstart() {
        return lineKEYWORDSstart;
    }

    public void setLineKEYWORDSstart(int lineKEYWORDSstart) {
        this.lineKEYWORDSstart = lineKEYWORDSstart;
    }

    public int getLineKEYWORDSend() {
        return lineKEYWORDSend;
    }

    public void setLineKEYWORDSend(int lineKEYWORDSend) {
        this.lineKEYWORDSend = lineKEYWORDSend;
    }

    public int getLineCMDLINE() {
        return lineCMDLINE;
    }

    public void setLineCMDLINE(int lineCMDLINE) {
        this.lineCMDLINE = lineCMDLINE;
    }

    public int getLineHISTORYstart() {
        return lineHISTORYstart;
    }

    public void setLineHISTORYstart(int lineHISTORYstart) {
        this.lineHISTORYstart = lineHISTORYstart;
    }

    public int getLineHISTORYend() {
        return lineHISTORYend;
    }

    public void setLineHISTORYend(int lineHISTORYend) {
        this.lineHISTORYend = lineHISTORYend;
    }

    public int getLineCOMMENTstart() {
        return lineCOMMENTstart;
    }

    public void setLineCOMMENTstart(int lineCOMMENTstart) {
        this.lineCOMMENTstart = lineCOMMENTstart;
    }

    public int getLineCOMMENTend() {
        return lineCOMMENTend;
    }

    public void setLineCOMMENTend(int lineCOMMENTend) {
        this.lineCOMMENTend = lineCOMMENTend;
    }

    public int getLineBFC2start() {
        return lineBFC2start;
    }

    public void setLineBFC2start(int lineBFC2start) {
        this.lineBFC2start = lineBFC2start;
    }

    public int getLineBFC2end() {
        return lineBFC2end;
    }

    public void setLineBFC2end(int lineBFC2end) {
        this.lineBFC2end = lineBFC2end;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        h.lineHELPstart = lineHELPstart;
        h.lineHELPend = lineHELPend;
        h.lineBFC = lineBFC;
        h.lineCATEGORY = lineCATEGORY;
        h.lineKEYWORDSstart = lineKEYWORDSstart;
        h.lineKEYWORDSend = lineKEYWORDSend;
        h.lineCMDLINE = lineCMDLINE;
        h.lineHISTORYstart = lineHISTORYstart;
        h.lineHISTORYend = lineHISTORYend;
        h.lineCOMMENTstart = lineCOMMENTstart;
        h.lineCOMMENTend = lineCOMMENTend;
        h.lineBFC2start = lineBFC2start;
        h.lineBFC2end = lineBFC2end;
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
