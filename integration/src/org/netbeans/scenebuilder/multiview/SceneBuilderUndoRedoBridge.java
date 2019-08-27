/*
 * The MIT License
 *
 * Copyright 2014 Sven Reimers
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

import com.oracle.javafx.scenebuilder.kit.editor.JobManager;
import java.awt.EventQueue;
import javafx.application.Platform;
import javax.swing.event.ChangeListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.openide.awt.UndoRedo;
import org.openide.util.ChangeSupport;

/**
 * Bridge NetBeans UndoRedo to JobManager from Scene Builder Kit. This is a pure
 * delegate logic implemeting NetBeans API delegating to JobManager
 * implementation from Scene Builder Kit.
 *
 * @author sven
 */
public class SceneBuilderUndoRedoBridge implements UndoRedo {

    private final JobManager manager;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    SceneBuilderUndoRedoBridge(JobManager manager) {
        this.manager = manager;
        this.manager.revisionProperty().addListener((observable) -> {
            EventQueue.invokeLater(() -> {
                changeSupport.fireChange();
            });
        });
    }

    @Override
    public boolean canUndo() {
        return manager.canUndo();
    }

    @Override
    public boolean canRedo() {
        return manager.canRedo();
    }

    @Override
    public void undo() throws CannotUndoException {
        Platform.runLater(()-> manager.undo());
    }

    @Override
    public void redo() throws CannotRedoException {
        Platform.runLater(()-> manager.redo());
    }

    @Override
    public void addChangeListener(ChangeListener cl) {
        changeSupport.addChangeListener(cl);
    }

    @Override
    public void removeChangeListener(ChangeListener cl) {
        changeSupport.removeChangeListener(cl);
    }

    @Override
    public String getUndoPresentationName() {
        return manager.getUndoDescription();
    }

    @Override
    public String getRedoPresentationName() {
        return manager.getRedoDescription();
    }

}
