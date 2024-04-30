package useless.gui;

import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;

public class WindowMonitor implements AWTEventListener {
    private boolean isOpened = true;
    public boolean isWindowOpen(){
        return isOpened;
    }
    @Override
    public void eventDispatched(AWTEvent event) {
        switch (event.getID()){
            case WindowEvent.WINDOW_OPENED:
                isOpened = true;
                break;
            case WindowEvent.WINDOW_CLOSED:
                isOpened = false;
                break;
        }
    }
}
