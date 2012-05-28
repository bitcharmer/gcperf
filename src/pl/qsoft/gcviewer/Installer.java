/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.qsoft.gcviewer;

import org.openide.modules.ModuleInstall;

public class Installer extends ModuleInstall {

@Override
public void restored() {
    GCViewerProvider.initialize();
}

@Override
public void uninstalled() {
    GCViewerProvider.unregister();
}
}
