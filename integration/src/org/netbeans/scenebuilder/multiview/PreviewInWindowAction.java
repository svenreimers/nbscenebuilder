/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.scenebuilder.multiview;

import com.oracle.javafx.scenebuilder.kit.editor.EditorController;
import com.oracle.javafx.scenebuilder.kit.editor.panel.content.ContentPanelController;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.scenebuilder.multiview.kit.PreviewWindowController;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

@ActionID(
        category = "SceneBuilder",
        id = "org.netbeans.scenebuilder.multiview.PreviewInWindowAction"
)
@ActionRegistration(
        displayName = "#CTL_PreviewInWindowAction",
        iconBase = "org/netbeans/scenebuilder/multiview/resources/preview_fxml.png",
        lazy = true
)
@ActionReference(path = "Editors/text/scenebuilder/Toolbars/Default", position = 100)
@Messages("CTL_PreviewInWindow=Preview in Window")
public final class PreviewInWindowAction extends AbstractAction implements ContextAwareAction {

    private final EditorController editorController;
    private static Stage stage;
    private PreviewWindowController previewWindowController;

    public PreviewInWindowAction() {
        this(Utilities.actionsGlobalContext());
    }

    public PreviewInWindowAction(Lookup lookup) {
        this.editorController = lookup.lookup(Node.class).getLookup().lookup(EditorController.class);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Platform.runLater(() -> {
            if (null == stage) {
                stage = new Stage();
            }
            if (null == previewWindowController) {
                previewWindowController = new PreviewWindowController(editorController, stage);
                previewWindowController.openWindow();
                previewWindowController.getStage().setTitle(
                        this.editorController.getFxmlLocation().getPath().substring(
                                this.editorController.getFxmlLocation().getPath().lastIndexOf("/")+1));
                
                Image icon16 = new Image(SBFxmlMultiViewElement.SCENEBUILDER_ICON_16);
                Image icon32 = new Image(SBFxmlMultiViewElement.SCENEBUILDER_ICON_32);
                previewWindowController.getStage().getIcons().addAll(icon16, icon32);

                EventHandler<WindowEvent> onHiddenHandler = previewWindowController.getStage().onHiddenProperty().get();
                previewWindowController.getStage().setOnHidden(we -> {
                    if (null != onHiddenHandler) {
                        onHiddenHandler.handle(we);
                    }
                    previewWindowController = null;
                });
            } else {
                previewWindowController.getStage().toFront();
            }
        });
    }

    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        return new PreviewInWindowAction(lkp);
    }

}
