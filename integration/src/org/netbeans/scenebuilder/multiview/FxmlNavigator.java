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

import com.oracle.javafx.scenebuilder.app.info.InfoPanelController;
import com.oracle.javafx.scenebuilder.kit.editor.EditorController;
import com.oracle.javafx.scenebuilder.kit.editor.panel.hierarchy.HierarchyPanelController;
import java.util.Optional;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javax.swing.JComponent;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author sven
 */
@NavigatorPanel.Registration(displayName = "Hierarchy", mimeType = "text/scenebuilder")
public class FxmlNavigator implements NavigatorPanel, LookupListener {

    private final JFXPanel panel = new JFXPanel();
    private final Lookup lkp;
    private final InstanceContent ic;
    private Lookup.Result<EditorController> editorControllerResult;

    public FxmlNavigator() {
        ic = new InstanceContent();
        lkp = new AbstractLookup(ic);
    }

    @Override
    public String getDisplayName() {
        return "Hierarchy";
    }

    @Override
    public String getDisplayHint() {
        return "Hierarchy Hint";
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public void panelActivated(Lookup lkp) {
        editorControllerResult = lkp.lookupResult(EditorController.class);
        editorControllerResult.addLookupListener(this);
        resultChanged(new LookupEvent(editorControllerResult));
    }

    @Override
    public void panelDeactivated() {
        System.out.println("Deactivated");
    }

    @Override
    public Lookup getLookup() {
        return lkp;
    }

    @Override
    public void resultChanged(LookupEvent le) {
        final Optional<? extends EditorController> optionalController = editorControllerResult.allInstances().stream().findFirst();
        if (optionalController.isPresent()) {
            Platform.runLater(() -> {
                HierarchyPanelController hierarchy = new HierarchyPanelController(optionalController.get());
                InfoPanelController info = new InfoPanelController(optionalController.get());
                StackPane hierarchyStackPane = new StackPane(hierarchy.getPanelRoot());
                hierarchyStackPane.setStyle("-fx-padding: 0;");
                TitledPane hierarchyPane = new TitledPane("Hierarchy", hierarchyStackPane);
                StackPane infoStackPane = new StackPane(info.getPanelRoot());
                infoStackPane.setStyle("-fx-padding: 0;");
                TitledPane infoPane = new TitledPane("Controller", infoStackPane);
                Accordion accordion = new Accordion(hierarchyPane, infoPane);
                accordion.setExpandedPane(hierarchyPane);
                Scene scene = new Scene(accordion);
                panel.setScene(scene);
            });
        }
    }

}
