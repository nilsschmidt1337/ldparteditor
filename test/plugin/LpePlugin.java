package plugin;

// AUTO GENERATED PLUGIN CLASS
import org.nschmidt.ldparteditor.plugin.Plugin;
import org.nschmidt.ldparteditor.composite.Composite3D;
import org.nschmidt.ldparteditor.data.DatFile;

public class LpePlugin implements Plugin {

    public LpePlugin() {

    }

    @Override
    public String getPlugInAuthor() {
        return "Nils Schmidt [BlackBrick89]"; //$NON-NLS-1$
    }

    @Override
    public String getPlugInName() {
        return "blah"; //$NON-NLS-1$
    }

    @Override
    public void onCanvas(Composite3D c3d) {
        return;
    }

    @Override
    public void onData(DatFile data) {
        return;
    }

}
