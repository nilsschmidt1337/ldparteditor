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
package org.nschmidt.ldparteditor.dnd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;
import org.nschmidt.ldparteditor.logger.NLogger;

/**
 * @author nils
 *
 */
public class PrimitiveDragAndDropTransfer extends ByteArrayTransfer {
    private static final String TYPENAME = "ldraw_primitive"; //$NON-NLS-1$
    private static final int TYPEID = registerType(TYPENAME);
    private static PrimitiveDragAndDropTransfer instance = new PrimitiveDragAndDropTransfer();

    public static PrimitiveDragAndDropTransfer getInstance() {
        return instance;
    }

    @Override
    public void javaToNative(Object object, TransferData transferData) {
        if (!checkMyType(object) || !isSupportedType(transferData)) {
            DND.error(DND.ERROR_INVALID_DATA);
        }
        PrimitiveDragAndDropType myTypes = (PrimitiveDragAndDropType) object;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DataOutputStream writeOut = new DataOutputStream(out)) {
            // write data to a byte array and then ask super to convert
            writeOut.writeInt(myTypes.placeholderData);
            byte[] buffer2 = out.toByteArray();
            super.javaToNative(buffer2, transferData);
        } catch (IOException ex) {
            NLogger.error(getClass(), ex);
        }
    }

    @Override
    public Object nativeToJava(TransferData transferData) {
        if (isSupportedType(transferData)) {
            byte[] buffer = (byte[]) super.nativeToJava(transferData);
            if (buffer == null)
                return null;

            PrimitiveDragAndDropType data = new PrimitiveDragAndDropType();
            try (ByteArrayInputStream in = new ByteArrayInputStream(buffer);
                 DataInputStream readIn = new DataInputStream(in)) {
                data.placeholderData = readIn.readInt();
            } catch (IOException ex) {
                NLogger.error(getClass(), ex);
                return null;
            }
            return data;
        }

        return null;
    }

    @Override
    protected String[] getTypeNames() {
        return new String[] { TYPENAME };
    }

    @Override
    protected int[] getTypeIds() {
        return new int[] { TYPEID };
    }

    private boolean checkMyType(Object object) {
        return object instanceof PrimitiveDragAndDropType;
    }

    @Override
    protected boolean validate(Object object) {
        return checkMyType(object);
    }
}
