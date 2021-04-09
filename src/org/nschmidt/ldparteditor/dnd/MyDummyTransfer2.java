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

/**
 * @author nils
 *
 */
public class MyDummyTransfer2 extends ByteArrayTransfer {
    private static final String MYTYPENAME = "name_for_my_type2"; //$NON-NLS-1$
    private static final int MYTYPEID = registerType(MYTYPENAME);
    private static MyDummyTransfer2 instance = new MyDummyTransfer2();

    public static MyDummyTransfer2 getInstance() {
        return instance;
    }

    @Override
    public void javaToNative(Object object, TransferData transferData) {
        if (!checkMyType(object) || !isSupportedType(transferData)) {
            DND.error(DND.ERROR_INVALID_DATA);
        }
        MyDummyType2 myTypes = (MyDummyType2) object;
        try {
            // write data to a byte array and then ask super to convert to
            // pMedium
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream writeOut = new DataOutputStream(out);
            writeOut.writeInt(myTypes.dummy);
            byte[] buffer2 = out.toByteArray();
            writeOut.close();
            super.javaToNative(buffer2, transferData);
        } catch (IOException e) {
        }
    }

    @Override
    public Object nativeToJava(TransferData transferData) {
        if (isSupportedType(transferData)) {
            byte[] buffer = (byte[]) super.nativeToJava(transferData);
            if (buffer == null)
                return null;

            MyDummyType2 datum = new MyDummyType2();
            try {
                ByteArrayInputStream in = new ByteArrayInputStream(buffer);
                DataInputStream readIn = new DataInputStream(in);
                datum.dummy = readIn.readInt();
                readIn.close();
            } catch (IOException ex) {
                return null;
            }
            return datum;
        }

        return null;
    }

    @Override
    protected String[] getTypeNames() {
        return new String[] { MYTYPENAME };
    }

    @Override
    protected int[] getTypeIds() {
        return new int[] { MYTYPEID };
    }

    private boolean checkMyType(Object object) {
        if (object == null || !(object instanceof MyDummyType2)) {
            return false;
        }
        return true;
    }

    @Override
    protected boolean validate(Object object) {
        return checkMyType(object);
    }
}
