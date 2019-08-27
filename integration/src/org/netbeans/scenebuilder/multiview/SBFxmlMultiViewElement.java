/*
 * The MIT License
 *
 * Copyright 2013-2014 Sven Reimers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.netbeans.scenebuilder.multiview;

import com.oracle.javafx.scenebuilder.kit.editor.EditorController;
import com.oracle.javafx.scenebuilder.kit.editor.panel.content.ContentPanelController;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.spi.navigator.NavigatorLookupHint;
import org.openide.awt.UndoRedo;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;

/**
 *
 * @author Sven Reimers
 */
@MultiViewElement.Registration(displayName = "#LBL_SceneBuilder",
        iconBase = "org/netbeans/scenebuilder/multiview/resources/SceneBuilderLogo_16.png",
        mimeType = "text/x-fxml+xml",
        persistenceType = TopComponent.PERSISTENCE_NEVER,
        preferredID = "SceneBuilder",
        position = 3000)
@NbBundle.Messages({
    "LBL_SceneBuilder=Design"
})
public class SBFxmlMultiViewElement implements MultiViewElement {

    public static final String SCENEBUILDER_ICON_16 = SBFxmlMultiViewElement.class.getResource("/org/netbeans/scenebuilder/multiview/resources/SceneBuilderLogo_16.png").toExternalForm();
    public static final String SCENEBUILDER_ICON_32 = SBFxmlMultiViewElement.class.getResource("/org/netbeans/scenebuilder/multiview/resources/SceneBuilderLogo_32.png").toExternalForm();        
    
    private MultiViewElementCallback callback;
    private final DataObject dao;
    private EditorController editorController;

    private final Lookup lkp;
    private final InstanceContent ic;

    private JFXPanel jfxPanel;
    private UndoRedo undoRedo;
    
    private EditorToolbar toolbar = null;

    public SBFxmlMultiViewElement(Lookup lookup) {
        Platform.setImplicitExit(false);
        dao = lookup.lookup(DataObject.class);
        assert dao != null;
        ic = new InstanceContent();
        lkp = new AbstractLookup(ic);
    }

    @Override
    public JComponent getVisualRepresentation() {
        if (null == jfxPanel) {
            jfxPanel = new JFXPanel();
            try {
                final File fxmlFile = FileUtil.toFile(dao.getPrimaryFile());
                final String fxmlText = readContentFromFile(fxmlFile);
                final URL fxmlLocation = Utilities.toURI(fxmlFile).toURL();
                Platform.runLater(() -> {
                    try {
                        editorController = new EditorController();
                        undoRedo = new SceneBuilderUndoRedoBridge(editorController.getJobManager());
                        ContentPanelController contentPanelController = new ContentPanelController(editorController);
                        Node node = new AbstractNode(Children.LEAF, Lookups.fixed(editorController, contentPanelController));
                        ic.add(node);
                        ic.add(SBTypeLookupHint.INSTANCE);
                        final BorderPane pane = new BorderPane();
                        pane.setCenter(contentPanelController.getPanelRoot());
                        Scene scene = new Scene(pane);
                        jfxPanel.setScene(scene);
                        editorController.setFxmlTextAndLocation(fxmlText, fxmlLocation);
                        editorController.getJobManager().revisionProperty().addListener((ov, oldValue, newValue) -> {
                            try {
                                String updatedFXMLText = editorController.getFxmlText();
                                EditorCookie editorCookie = dao.getLookup().lookup(EditorCookie.class);
                                if (null != editorCookie.getDocument()) {
                                    editorCookie.getDocument().remove(0, editorCookie.getDocument().getLength());
                                    editorCookie.getDocument().insertString(0, updatedFXMLText, null);
                                }
                            } catch (BadLocationException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        });
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                });

            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return jfxPanel;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        if (null == toolbar) {
            toolbar = new EditorToolbar();
            toolbar.setFloatable(false);
            toolbar.setRollover(true);

            toolbar.addSeparator();
            
            List<? extends Action> actionsForPath = Utilities.actionsForPath("Editors/text/scenebuilder/Toolbars/Default");
            actionsForPath.stream().map(action -> {
                if (action instanceof ContextAwareAction) {
                    return ((ContextAwareAction)action).createContextAwareInstance(lkp);
                } else {
                    return action;
                }
            }).map(toolbar::add).forEach(b -> b.setRolloverEnabled(true));            
        }
        return toolbar;
    }

    @Override
    public Action[] getActions() {
        return new Action[]{};
    }

    @Override    
    public Lookup getLookup() {
        return lkp;
    }

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
    }

    @Override
    public void componentShowing() {
//        ic.add(SBTypeLookupHint.INSTANCE);
        TopComponentGroup group = WindowManager.getDefault().findTopComponentGroup("SceneBuilderGroup");
        if (group == null) {
            return;
        }
        group.open();
    }

    @Override
    public void componentHidden() {
//        ic.remove(SBTypeLookupHint.INSTANCE);
        TopComponentGroup group = WindowManager.getDefault().findTopComponentGroup("SceneBuilderGroup");
        if (group == null) {
            return;
        }
        group.close();
    }

    @Override
    public void componentActivated() {
        ic.add(SBTypeLookupHint.INSTANCE);
        TopComponentGroup group = WindowManager.getDefault().findTopComponentGroup("SceneBuilderGroup");
        if (group == null) {
            return;
        }
        group.open();        
    }

    @Override
    public void componentDeactivated() {
        ic.remove(SBTypeLookupHint.INSTANCE);
        TopComponentGroup group = WindowManager.getDefault().findTopComponentGroup("SceneBuilderGroup");
        if (group == null) {
            return;
        }
        group.close();
    }

    @Override
    public UndoRedo getUndoRedo() {
        return null != undoRedo ? undoRedo : UndoRedo.NONE;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    private static String readContentFromFile(File file) throws IOException {
        final byte[] buffer;

        buffer = new byte[(int) file.length()];
        try (DataInputStream is = new DataInputStream(new FileInputStream(file))) {
            is.readFully(buffer);
        }

        return new String(buffer, Charset.forName("UTF-8"));
    }

    static class SBTypeLookupHint implements NavigatorLookupHint {

        private static NavigatorLookupHint INSTANCE = new SBTypeLookupHint();                
        
        public String getContentType() {
            return "text/scenebuilder";
        }
    }

    private static class EditorToolbar extends org.openide.awt.Toolbar {
        public EditorToolbar() {
            Border b = UIManager.getBorder("Nb.Editor.Toolbar.border"); //NOI18N
            setBorder(b);
            if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
                setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
            }
        }

        @Override
        public String getUIClassID() {
            if( UIManager.get("Nb.Toolbar.ui") != null ) { //NOI18N
                return "Nb.Toolbar.ui"; //NOI18N
            }
            return super.getUIClassID();
        }

        @Override
        public String getName() {
            return "editorToolbar"; //NOI18N
        }
    }    
    
}
