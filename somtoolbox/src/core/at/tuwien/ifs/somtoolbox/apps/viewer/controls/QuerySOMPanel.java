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
package at.tuwien.ifs.somtoolbox.apps.viewer.controls;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.pdfbox.exceptions.CryptographyException;
import org.pdfbox.exceptions.InvalidPasswordException;

import at.ec3.Document.ExcelDocument;
import at.ec3.Document.HTMLDocument;
import at.ec3.Document.PDFDocument;
import at.ec3.Document.TextDocument;
import at.ec3.Document.WordDocument;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;
import at.tuwien.ifs.somtoolbox.layers.Unit;

/**
 * This class implements a user-interface to send queries to the map.
 * 
 * @author Rudolf Mayer
 * @version $Id: QuerySOMPanel.java 3873 2010-10-28 09:29:58Z frank $
 */
public class QuerySOMPanel extends AbstractViewerControl implements ActionListener {
    private static final long serialVersionUID = 1L;

    private static final String NO_QUERY_ENTERED = "-- No query entered --";

    private static final String NO_DOCUMENT_SELECTED = "-- No document selected --";

    private JButton querySearchButton = null;

    private JButton documentSearchButton = null;

    private JButton queryClearButton = null;

    private JFileChooser documentFileChooser = null;

    private JTextField queryTextField = null;

    private JLabel queryStatusLabel = null;

    private JLabel documentStatusLabel = null;

    private GeneralUnitPNode winningUnitPNode;

    private SOMLibTemplateVector templateVector;

    public QuerySOMPanel(String title, CommonSOMViewerStateData state) {
        super(title, state, new GridBagLayout());
        templateVector = state.inputDataObjects.getTemplateVector();

        GridBagConstraints c = new GridBagConstraints();

        c.gridy = 0;

        c.weightx = 1.0;
        queryTextField = new JTextField(15);
        queryTextField.addActionListener(this);
        getContentPane().add(queryTextField, c);

        c.weightx = 0.0;
        querySearchButton = new JButton("search");
        querySearchButton.addActionListener(this);
        getContentPane().add(querySearchButton, c);

        c.gridy = 1;

        c.weightx = 1.0;
        documentStatusLabel = new JLabel(NO_DOCUMENT_SELECTED);
        getContentPane().add(documentStatusLabel, c);

        c.weightx = 0.0;

        documentSearchButton = new JButton("file");
        documentSearchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                documentFileChooser = new JFileChooser();
                documentFileChooser.setApproveButtonText("Open");
                documentFileChooser.setApproveButtonToolTipText("... file for the projection");
                int returnVal = documentFileChooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    // get text from file
                    String content = getText(documentFileChooser.getSelectedFile().toString());
                    if (!content.equals("")) {
                        doQuery(content);
                        documentStatusLabel.setText(documentFileChooser.getSelectedFile().toString());
                    } else {
                        JOptionPane.showMessageDialog(null, "The file is empty!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

        });
        getContentPane().add(documentSearchButton, c);

        c.gridy = 2;

        c.weightx = 1.0;
        queryStatusLabel = new JLabel(NO_QUERY_ENTERED);
        getContentPane().add(queryStatusLabel, c);

        c.weightx = 0.0;
        queryClearButton = new JButton("clear");
        queryClearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (winningUnitPNode != null) {
                    winningUnitPNode.removeQueryHit();
                }
                queryStatusLabel.setText(NO_QUERY_ENTERED);
                queryTextField.setText("");
                documentStatusLabel.setText(NO_DOCUMENT_SELECTED);
                winningUnitPNode = null;
            }

        });
        getContentPane().add(queryClearButton, c);
        if (templateVector == null) {
            setVisible(false);
        } else {
            setVisible(true);
        }
        // TODO: new Dimension(state.controlElementsWidth, 100);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String query = queryTextField.getText();
        doQuery(query);
    }

    private void doQuery(String query) {
        // clear old winning unit
        if (winningUnitPNode != null) {
            winningUnitPNode.removeQueryHit();
        }

        // find new winner
        Unit[] winners = state.growingLayer.getWinnersFromQuery(query, 3, templateVector);
        Unit winner = winners[0];

        winningUnitPNode = state.mapPNode.getUnit(winner.getXPos(), winner.getYPos());
        winningUnitPNode.setQueryHit();
        String labelText = "Found Node(s): ";
        for (Unit winner2 : winners) {
            labelText += winner2.getXPos() + "/" + winner2.getYPos() + "; ";
        }
        queryStatusLabel.setText(labelText);
        String[] mappedInputNames = state.growingLayer.getWinningInputDataFromQuery(query, 5, templateVector);
        for (int i = 0; i < mappedInputNames.length; i++) {
            System.out.println("found " + i + ": " + mappedInputNames[i]);
        }
    }

    private String getText(String fileName) {
        String txt = "";

        try {
            // FIXME: detecting document type must go into TeSeT, which should throw an appropriate Exception if needed
            // possibly this could be a static Document.getDocument(fileName).
            // cause this is
            // a.) repeating code
            // b.) would need to be adapted for each new filetype TeSeT supports

            if (fileName.toLowerCase().endsWith(".htm") || fileName.toLowerCase().endsWith(".html")) {
                HTMLDocument doc = new HTMLDocument();
                txt = doc.getText(new File(fileName));
            } else if (fileName.toLowerCase().endsWith(".txt")) {
                TextDocument doc = new TextDocument();
                txt = doc.getText(new File(fileName));
            } else if (fileName.toLowerCase().endsWith(".pdf")) {
                PDFDocument doc = new PDFDocument();
                txt = doc.getText(new File(fileName));
            } else if (fileName.toLowerCase().endsWith(".xls")) {
                ExcelDocument doc = new ExcelDocument();
                txt = doc.getText(new File(fileName));
            } else if (fileName.toLowerCase().endsWith(".doc")) {
                WordDocument doc = new WordDocument();
                txt = doc.getText(new File(fileName));
            } else if (fileName.toLowerCase().endsWith(".")) {
                TextDocument doc = new TextDocument();
                txt = doc.getText(new File(fileName));
            } else {
                JOptionPane.showMessageDialog(null, "Cannot get the content from the file " + fileName + "!", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
            // FIXME: This exception handling should be done in TeSeT, which would throw one meaningful exception
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CryptographyException e) {
            e.printStackTrace();
        } catch (InvalidPasswordException e) {
            e.printStackTrace();
        }
        return txt;
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

}
