/*
 * Copyright 2004-2010 Information & Software Engineering Group (188/1)
 *                     Institute of Software Technology and Interactive Systems
 *                     Vienna University of Technology, Austria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.ifs.tuwien.ac.at/dm/somtoolbox/license.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.tuwien.ifs.somtoolbox.apps.viewer;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class represents the panel for the viewing documents.<br/>
 * It has an EditorPane for viewing HTML documents.
 * 
 * @author Christoph Becker
 * @author Rudolf Mayer
 * @version $Id: DocViewPanel.java 3873 2010-10-28 09:29:58Z frank $
 */
public class DocViewPanel extends JPanel implements ItemSelectionListener {
    private static final long serialVersionUID = 1L;

    private Log log = LogFactory.getLog(this.getClass());

    private JScrollPane docScroller;

    private JEditorPane editDoc;

    private String documentPath;

    private String documentSuffix = ".html";

    public String getDocumentSuffix() {
        return documentSuffix;
    }

    public void setDocumentSuffix(String documentSuffix) {
        this.documentSuffix = documentSuffix;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public void setDocumentPath(String filePath) {
        if (!filePath.equals("") && new File(filePath).isDirectory() && !filePath.endsWith(File.separator)) {
            filePath += File.separator;
        }
        this.documentPath = filePath;
    }

    /**
     * displays the document with the provided name in the docviewer. If not existent, the text is cleared. NOTE: we are
     * only caring about the first item, because we can only display one document at a time. We assume that the
     * listselectionode in the panel is set to singleselection
     */
    @Override
    public void itemSelected(Object[] items) {
        if (items == null || items.length == 0) {
            editDoc.setText("");
        } else {
            String absoluteDocumentName = documentPath + items[0] + documentSuffix;
            try {
                editDoc.setPage(absoluteDocumentName);
            } catch (IOException e) {
                try {
                    editDoc.setPage("file://" + absoluteDocumentName);
                } catch (IOException e2) {
                    log.error(e);
                    editDoc.setText("problem loading text: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 
     */
    public DocViewPanel() {
        super(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Document viewer"),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        this.setPreferredSize(new java.awt.Dimension(216, 260));
        docScroller = new JScrollPane();
        this.add(docScroller, BorderLayout.CENTER);
        docScroller.setVisible(true);
        editDoc = new JEditorPane();
        editDoc.setEditable(false);
        docScroller.setViewportView(editDoc);
    }

}
