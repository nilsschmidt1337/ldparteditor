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

    public static final int _00_TITLE = 0;
    public static final int _01_NAME = 1;
    public static final int _02_AUTHOR = 2;
    public static final int _03_TYPE = 3;
    public static final int _04_LICENSE = 4;
    public static final int _05o_HELP = 5;
    public static final int _06_BFC = 6;
    public static final int _07o_CATEGORY = 7;
    public static final int _08o_KEYWORDS = 8;
    public static final int _09o_CMDLINE = 9;
    public static final int _10o_HISTORY = 10;
    public static final int _11o_COMMENT = 11;
    public static final int _12o_BFC2 = 12;

    private boolean hasTITLE = false;
    private boolean hasNAME = false;
    private boolean hasAUTHOR = false;
    private boolean hasTYPE = false;
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

    public static final int _99_DONE = 99;

    /**
     * @return the hasTITLE
     */
    public boolean hasTITLE() {
        return hasTITLE;
    }

    /**
     * @param hasTITLE
     *            the hasTITLE to set
     */
    public void setHasTITLE(boolean hasTITLE) {
        this.hasTITLE = hasTITLE;
    }

    /**
     * @return the hasNAME
     */
    public boolean hasNAME() {
        return hasNAME;
    }

    /**
     * @param hasNAME
     *            the hasNAME to set
     */
    public void setHasNAME(boolean hasNAME) {
        this.hasNAME = hasNAME;
    }

    /**
     * @return the hasAUTHOR
     */
    public boolean hasAUTHOR() {
        return hasAUTHOR;
    }

    /**
     * @param hasAUTHOR
     *            the hasAUTHOR to set
     */
    public void setHasAUTHOR(boolean hasAUTHOR) {
        this.hasAUTHOR = hasAUTHOR;
    }

    /**
     * @return the hasTYPE
     */
    public boolean hasTYPE() {
        return hasTYPE;
    }

    /**
     * @param hasTYPE
     *            the hasTYPE to set
     */
    public void setHasTYPE(boolean hasTYPE) {
        this.hasTYPE = hasTYPE;
    }

    /**
     * @return the hasHELP
     */
    public boolean hasHELP() {
        return hasHELP;
    }

    /**
     * @param hasHELP
     *            the hasHELP to set
     */
    public void setHasHELP(boolean hasHELP) {
        this.hasHELP = hasHELP;
    }

    /**
     * @return the hasBFC
     */
    public boolean hasBFC() {
        return hasBFC;
    }

    /**
     * @param hasBFC
     *            the hasBFC to set
     */
    public void setHasBFC(boolean hasBFC) {
        this.hasBFC = hasBFC;
    }

    /**
     * @return the hasCATEGORY
     */
    public boolean hasCATEGORY() {
        return hasCATEGORY;
    }

    /**
     * @param hasCATEGORY
     *            the hasCATEGORY to set
     */
    public void setHasCATEGORY(boolean hasCATEGORY) {
        this.hasCATEGORY = hasCATEGORY;
    }

    /**
     * @return the hasKEYWORDS
     */
    public boolean hasKEYWORDS() {
        return hasKEYWORDS;
    }

    /**
     * @param hasKEYWORDS
     *            the hasKEYWORDS to set
     */
    public void setHasKEYWORDS(boolean hasKEYWORDS) {
        this.hasKEYWORDS = hasKEYWORDS;
    }

    /**
     * @return the hasCMDLINE
     */
    public boolean hasCMDLINE() {
        return hasCMDLINE;
    }

    /**
     * @param hasCMDLINE
     *            the hasCMDLINE to set
     */
    public void setHasCMDLINE(boolean hasCMDLINE) {
        this.hasCMDLINE = hasCMDLINE;
    }

    /**
     * @return the hasHISTORY
     */
    public boolean hasHISTORY() {
        return hasHISTORY;
    }

    /**
     * @param hasHISTORY
     *            the hasHISTORY to set
     */
    public void setHasHISTORY(boolean hasHISTORY) {
        this.hasHISTORY = hasHISTORY;
    }

    /**
     * @return the hasCOMMENT
     */
    public boolean hasCOMMENT() {
        return hasCOMMENT;
    }

    /**
     * @param hasCOMMENT
     *            the hasCOMMENT to set
     */
    public void setHasCOMMENT(boolean hasCOMMENT) {
        this.hasCOMMENT = hasCOMMENT;
    }

    /**
     * @return the hasBFC2
     */
    public boolean hasBFC2() {
        return hasBFC2;
    }

    /**
     * @param hasBFC2
     *            the hasBFC2 to set
     */
    public void setHasBFC2(boolean hasBFC2) {
        this.hasBFC2 = hasBFC2;
    }

    /**
     * @return the lineTITLE
     */
    public int getLineTITLE() {
        return lineTITLE;
    }

    /**
     * @param lineTITLE
     *            the lineTITLE to set
     */
    public void setLineTITLE(int lineTITLE) {
        this.lineTITLE = lineTITLE;
    }

    /**
     * @return the lineNAME
     */
    public int getLineNAME() {
        return lineNAME;
    }

    /**
     * @param lineNAME
     *            the lineNAME to set
     */
    public void setLineNAME(int lineNAME) {
        this.lineNAME = lineNAME;
    }

    /**
     * @return the lineAUTHOR
     */
    public int getLineAUTHOR() {
        return lineAUTHOR;
    }

    /**
     * @param lineAUTHOR
     *            the lineAUTHOR to set
     */
    public void setLineAUTHOR(int lineAUTHOR) {
        this.lineAUTHOR = lineAUTHOR;
    }

    /**
     * @return the lineTYPE
     */
    public int getLineTYPE() {
        return lineTYPE;
    }

    /**
     * @param lineTYPE
     *            the lineTYPE to set
     */
    public void setLineTYPE(int lineTYPE) {
        this.lineTYPE = lineTYPE;
    }

    /**
     * @return the lineLICENSE
     */
    public int getLineLICENSE() {
        return lineLICENSE;
    }

    /**
     * @param lineLICENSE
     *            the lineLICENSE to set
     */
    public void setLineLICENSE(int lineLICENSE) {
        this.lineLICENSE = lineLICENSE;
    }

    /**
     * @return the lineHELP_start
     */
    public int getLineHELP_start() {
        return lineHELP_start;
    }

    /**
     * @param lineHELP_start
     *            the lineHELP_start to set
     */
    public void setLineHELP_start(int lineHELP_start) {
        this.lineHELP_start = lineHELP_start;
    }

    /**
     * @return the lineHELP_end
     */
    public int getLineHELP_end() {
        return lineHELP_end;
    }

    /**
     * @param lineHELP_end
     *            the lineHELP_end to set
     */
    public void setLineHELP_end(int lineHELP_end) {
        this.lineHELP_end = lineHELP_end;
    }

    /**
     * @return the lineBFC
     */
    public int getLineBFC() {
        return lineBFC;
    }

    /**
     * @param lineBFC
     *            the lineBFC to set
     */
    public void setLineBFC(int lineBFC) {
        this.lineBFC = lineBFC;
    }

    /**
     * @return the lineCATEGORY
     */
    public int getLineCATEGORY() {
        return lineCATEGORY;
    }

    /**
     * @param lineCATEGORY
     *            the lineCATEGORY to set
     */
    public void setLineCATEGORY(int lineCATEGORY) {
        this.lineCATEGORY = lineCATEGORY;
    }

    /**
     * @return the lineKEYWORDS_start
     */
    public int getLineKEYWORDS_start() {
        return lineKEYWORDS_start;
    }

    /**
     * @param lineKEYWORDS_start
     *            the lineKEYWORDS_start to set
     */
    public void setLineKEYWORDS_start(int lineKEYWORDS_start) {
        this.lineKEYWORDS_start = lineKEYWORDS_start;
    }

    /**
     * @return the lineKEYWORDS_end
     */
    public int getLineKEYWORDS_end() {
        return lineKEYWORDS_end;
    }

    /**
     * @param lineKEYWORDS_end
     *            the lineKEYWORDS_end to set
     */
    public void setLineKEYWORDS_end(int lineKEYWORDS_end) {
        this.lineKEYWORDS_end = lineKEYWORDS_end;
    }

    /**
     * @return the lineCMDLINE
     */
    public int getLineCMDLINE() {
        return lineCMDLINE;
    }

    /**
     * @param lineCMDLINE
     *            the lineCMDLINE to set
     */
    public void setLineCMDLINE(int lineCMDLINE) {
        this.lineCMDLINE = lineCMDLINE;
    }

    /**
     * @return the lineHISTORY_start
     */
    public int getLineHISTORY_start() {
        return lineHISTORY_start;
    }

    /**
     * @param lineHISTORY_start
     *            the lineHISTORY_start to set
     */
    public void setLineHISTORY_start(int lineHISTORY_start) {
        this.lineHISTORY_start = lineHISTORY_start;
    }

    /**
     * @return the lineHISTORY_end
     */
    public int getLineHISTORY_end() {
        return lineHISTORY_end;
    }

    /**
     * @param lineHISTORY_end
     *            the lineHISTORY_end to set
     */
    public void setLineHISTORY_end(int lineHISTORY_end) {
        this.lineHISTORY_end = lineHISTORY_end;
    }

    /**
     * @return the lineCOMMENT_start
     */
    public int getLineCOMMENT_start() {
        return lineCOMMENT_start;
    }

    /**
     * @param lineCOMMENT_start
     *            the lineCOMMENT_start to set
     */
    public void setLineCOMMENT_start(int lineCOMMENT_start) {
        this.lineCOMMENT_start = lineCOMMENT_start;
    }

    /**
     * @return the lineCOMMENT_end
     */
    public int getLineCOMMENT_end() {
        return lineCOMMENT_end;
    }

    /**
     * @param lineCOMMENT_end
     *            the lineCOMMENT_end to set
     */
    public void setLineCOMMENT_end(int lineCOMMENT_end) {
        this.lineCOMMENT_end = lineCOMMENT_end;
    }

    /**
     * @return the lineBFC2_start
     */
    public int getLineBFC2_start() {
        return lineBFC2_start;
    }

    /**
     * @param lineBFC2_start
     *            the lineBFC2_start to set
     */
    public void setLineBFC2_start(int lineBFC2_start) {
        this.lineBFC2_start = lineBFC2_start;
    }

    /**
     * @return the lineBFC2_end
     */
    public int getLineBFC2_end() {
        return lineBFC2_end;
    }

    /**
     * @param lineBFC2_end
     *            the lineBFC2_end to set
     */
    public void setLineBFC2_end(int lineBFC2_end) {
        this.lineBFC2_end = lineBFC2_end;
    }

    /**
     * @return the state
     */
    public int getState() {
        return state;
    }

    /**
     * @param state
     *            the state to set
     */
    public void setState(int state) {
        this.state = state;
    }

    /**
     * @return the lastHistoryEntry
     */
    public String getLastHistoryEntry() {
        return lastHistoryEntry;
    }

    /**
     * @param lastHistoryEntry
     *            the lastHistoryEntry to set
     */
    public void setLastHistoryEntry(String lastHistoryEntry) {
        this.lastHistoryEntry = lastHistoryEntry;
    }

    @Override
    public HeaderState clone() {
        HeaderState h = new HeaderState();
        h.hasTITLE = hasTITLE;
        h.hasNAME = hasNAME;
        h.hasAUTHOR = hasAUTHOR;
        h.hasTYPE = hasTYPE;
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

    /**
     * @return the hasLICENSE
     */
    public boolean hasLICENSE() {
        return hasLICENSE;
    }

    /**
     * @param hasLICENSE
     *            the hasLICENSE to set
     */
    public void setHasLICENSE(boolean hasLICENSE) {
        this.hasLICENSE = hasLICENSE;
    }
}
