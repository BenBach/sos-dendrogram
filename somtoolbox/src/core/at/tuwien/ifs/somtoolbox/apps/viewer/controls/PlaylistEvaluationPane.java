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

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;

import at.tuwien.ifs.somtoolbox.apps.viewer.ArrowPNode;
import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import at.tuwien.ifs.somtoolbox.layers.Unit;

/**
 * @author Jakob Frank
 * @version $Id: PlaylistEvaluationPane.java 3589 2010-05-21 10:42:01Z mayer $
 */
public class PlaylistEvaluationPane extends AbstractViewerControl {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JTextField txtInfile;

    private JButton btnEvalPL;

    private JButton btnPrintPL;

    private PNode playlistPrint;

    private PNode playlistDots;

    private PNode playlistLines;

    private SpinnerNumberModel spmSeconds;

    private JButton btnStopPLMovie;

    private boolean stopPLMovie = false;

    private JButton btnRememberPlaylist;

    public PlaylistEvaluationPane(String title, CommonSOMViewerStateData state) {
        super(title, state);

        initialize();
    }

    private void evaluate(File in) throws FileNotFoundException {
        evaluate(in, false);
    }

    private void evaluate(File file, boolean shortPrint) throws FileNotFoundException {
        BufferedReader fr = new BufferedReader(new FileReader(file));

        String pre = CommonSOMViewerStateData.fileNamePrefix;
        String suf = CommonSOMViewerStateData.fileNameSuffix;

        LinkedList<Unit> list = new LinkedList<Unit>();
        try {
            for (String line = fr.readLine(); line != null; line = fr.readLine()) {
                line = line.trim();
                if (line.length() == 0 || line.startsWith("#")) {
                    // Blank Lines & Comments
                    continue;
                }
                line = line.trim();
                if (line.startsWith(pre)) {
                    line = line.substring(pre.length());
                }
                if (line.endsWith(suf)) {
                    line = line.substring(0, line.length() - suf.length());
                }

                Unit u = state.growingLayer.getUnitForDatum(line);
                list.add(u);
            }
            fr.close();

            Unit prev = null;
            double max = Double.MIN_VALUE, min = Double.MAX_VALUE, sum = 0;
            int count = 0;
            LinkedList<Double> dists = new LinkedList<Double>();
            for (Unit unit : list) {
                if (prev != null) {
                    count++;
                    double dist = Math.sqrt(Math.pow(unit.getXPos() - prev.getXPos(), 2)
                            + Math.pow(unit.getXPos() - prev.getXPos(), 2));

                    sum += dist;
                    if (dist > max) {
                        max = dist;
                    }
                    if (dist < min) {
                        min = dist;
                    }
                    dists.add(dist);
                } else {
                    dists.add(-1d);
                }

                prev = unit;
            }

            double avg = sum / count;

            PrintStream ps = new PrintStream(file.getName() + ".stat");
            ps.print(file.getName());
            for (int i = 0; i < dists.size(); i++) {
                if (!shortPrint) {
                    System.out.printf("%d %.3f%n", i, dists.get(i));
                }
                ps.printf(";%.3f", dists.get(i));
            }
            ps.println();
            ps.close();

            System.out.printf("PlayList: %s AVG: %.3f MIN: %.3f MAX: %.3f%n", file.getName(), avg, min, max);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void evaluate(File[] listFiles) throws FileNotFoundException {
        for (File file : listFiles) {
            evaluate(file, true);
        }
    }

    private void initialize() {
        JPanel mainP = new JPanel();
        this.setContentPane(mainP);
        mainP.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;

        playlistLines = new PNode();
        playlistDots = new PNode();
        playlistPrint = new PNode();
        playlistPrint.addChild(playlistDots);
        playlistPrint.addChild(playlistLines);
        state.mapPNode.addChild(playlistPrint);

        txtInfile = new JTextField();
        txtInfile.setColumns(10);
        mainP.add(txtInfile, gbc);

        JButton btnFCDlg = new JButton("...");
        btnFCDlg.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = state.fileChooser;
                if (jfc.showOpenDialog(PlaylistEvaluationPane.this) == JFileChooser.APPROVE_OPTION) {
                    txtInfile.setText(jfc.getSelectedFile().getAbsolutePath());
                }
            }
        });
        mainP.add(btnFCDlg, gbc);
        gbc.gridy++;

        mainP.add(new JLabel("Seconds per Path"), gbc);
        spmSeconds = new SpinnerNumberModel(1.0, 0.5, 20.0, 0.5);
        JSpinner spnSeconds = new JSpinner();
        spnSeconds.setModel(spmSeconds);
        mainP.add(spnSeconds, gbc);
        gbc.gridy++;

        btnEvalPL = new JButton("Evaluate Playlist(s)");
        btnEvalPL.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File in = new File(txtInfile.getText());
                if (!in.exists()) {
                    return;
                }

                try {
                    if (in.isDirectory()) {
                        evaluate(in.listFiles(new FilenameFilter() {
                            @Override
                            public boolean accept(File dir, String name) {
                                return name.endsWith(".m3u");
                            }
                        }));
                    } else {
                        evaluate(in);
                    }
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                }
            }
        });
        mainP.add(btnEvalPL, gbc);

        btnPrintPL = new JButton("Print Playlist(s)");
        btnPrintPL.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File pl = new File(txtInfile.getText());
                if (pl.isDirectory()) {
                    doPlaylistMovie(pl);
                } else {
                    printSinglePlaylist(pl);
                }
            }
        });
        mainP.add(btnPrintPL, gbc);
        gbc.gridy++;

        btnStopPLMovie = new JButton("Stop");
        btnStopPLMovie.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopPLMovie = true;
            }
        });
        btnStopPLMovie.setEnabled(false);
        mainP.add(btnStopPLMovie, gbc);

        btnRememberPlaylist = new JButton("Remember");
        btnRememberPlaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rememberCurrentPlaylist();
            }
        });
        btnRememberPlaylist.setEnabled(false);
        mainP.add(btnRememberPlaylist, gbc);
        gbc.gridy++;

        gbc.gridwidth = 2;
        JButton btnClear = new JButton("Clear");
        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playlistDots.removeAllChildren();
                playlistLines.removeAllChildren();
            }
        });
        mainP.add(btnClear, gbc);
        gbc.gridy++;
        gbc.gridwidth = 2;

    }

    private String currentPlaylist;

    private StringBuilder rememberedPlaylists;

    private void rememberCurrentPlaylist() {
        if (rememberedPlaylists != null) {
            rememberedPlaylists.append(currentPlaylist).append('\n');
        }
    }

    private void doPlaylistMovie(final File pl) {

        Thread t = new Thread() {
            @Override
            public void run() {
                String buttonText = btnPrintPL.getText();
                try {
                    btnPrintPL.setEnabled(false);
                    btnStopPLMovie.setEnabled(true);
                    btnRememberPlaylist.setEnabled(true);
                    this.setName("PlaylistMovie");
                    rememberedPlaylists = new StringBuilder();

                    File[] pls = pl.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return name.endsWith(".m3u");
                        }
                    });

                    stopPLMovie = false;
                    int i = 0;
                    for (File playlist : pls) {
                        long startTime = System.currentTimeMillis();
                        if (stopPLMovie) {
                            break;
                        }
                        try {
                            btnPrintPL.setText("Printing " + ++i + "/" + pls.length);
                            currentPlaylist = playlist.getName();
                            printSinglePlaylist(playlist);
                            long wait = (long) (1000 * spmSeconds.getNumber().doubleValue() - (System.currentTimeMillis() - startTime));
                            Thread.sleep(wait);
                        } catch (InterruptedException e) {
                        }
                    }
                    if (rememberedPlaylists.length() > 0) {
                        System.out.println("Remembered:");
                        System.out.println(rememberedPlaylists);
                        System.out.println();
                    }
                } finally {
                    btnPrintPL.setText(buttonText);
                    btnPrintPL.setEnabled(true);
                    btnStopPLMovie.setEnabled(false);
                    btnRememberPlaylist.setEnabled(false);
                }
            }
        };
        t.start();
    }

    private void printSinglePlaylist(File file) {
        try {
            playlistDots.removeAllChildren();
            playlistLines.removeAllChildren();

            float dotS = state.mapPNode.getUnitHeight() / 3;

            BufferedReader fr = new BufferedReader(new FileReader(file));

            Unit lastU = null;

            Map<Unit, Integer> countList = new HashMap<Unit, Integer>();
            Map<Unit, PPath> dotList = new HashMap<Unit, PPath>();
            Map<Unit, PText> txtList = new HashMap<Unit, PText>();
            float lastX = 0, lastY = 0;
            int skipCount = 0;
            for (String line = fr.readLine(); line != null; line = fr.readLine()) {
                // Skip blank lines
                if (line.trim().length() == 0) {
                    continue;
                }
                // Skip lines starting with #
                if (line.trim().startsWith("#")) {
                    continue;
                }

                String pre = CommonSOMViewerStateData.fileNamePrefix;
                String suf = CommonSOMViewerStateData.fileNameSuffix;
                line = line.trim();
                if (line.startsWith(pre)) {
                    line = line.substring(pre.length());
                }
                if (line.endsWith(suf)) {
                    line = line.substring(0, line.length() - suf.length());
                }

                Unit u = state.growingLayer.getUnitForDatum(line);
                if (u == null) {
                    System.err.println(line + " not found...");
                    skipCount++;
                    continue;
                }

                GeneralUnitPNode gup = state.mapPNode.getUnit(u);
                float currentX = (float) (gup.getX() + gup.getWidth() / 2);
                float currentY = (float) (gup.getY() + gup.getHeight() / 2);

                PPath dot = dotList.get(u);
                PText txt = txtList.get(u);
                if (dot == null) {
                    // New unit, create a dot
                    dot = PPath.createEllipse(currentX - dotS / 2, currentY - dotS / 2, dotS, dotS);
                    dotList.put(u, dot);
                    txt = new PText("1");
                    txt.setX(currentX - txt.getWidth() / 2);
                    txt.setY(currentY - txt.getHeight() / 2);
                    txtList.put(u, txt);
                    countList.put(u, 1);
                    playlistDots.addChild(dot);
                    playlistDots.addChild(txt);
                } else {
                    // Grow the dot... (+10%)
                    dot.setWidth(dot.getWidth() + dotS / 10);
                    dot.setHeight(dot.getHeight() + dotS / 10);
                    dot.setX(dot.getX() - dotS / 20);
                    dot.setY(dot.getY() - dotS / 20);
                    int c = countList.get(u) + 1;
                    txt.setText(c + "");
                    txt.setX(currentX - txt.getWidth() / 2);
                    txt.setY(currentY - txt.getHeight() / 2);
                    countList.put(u, c);
                }
                if (lastU != null && u != lastU) {
                    // lines.addChild(PPath.createLine(lastX, lastY, currentX, currentY));
                    // double alpha = Math.atan((currentX - lastX) / (currentY - lastY));
                    // double sigX = 1;//Math.signum(currentX - lastX);
                    // double sigY = 1;//Math.signum(currentY - lastY);
                    // double deltax1 = (dot.getWidth() / 2) * Math.sin(alpha);
                    // double deltay1 = (dot.getHeight() / 2) * Math.cos(alpha);
                    //                            
                    // double deltax2 = (dot.getWidth() / 2) * Math.cos(alpha);
                    // double deltay2 = (dot.getHeight() / 2) * Math.sin(alpha);
                    // System.out.printf("last = (%.0f/%.0f) cur = (%.0f/%.0f) dx = %.2f dy = %.2f sX = %.0f sY = %.0f%n",
                    // lastX, lastY,
                    // currentX, currentY, deltax1 * sigX, deltay1 * sigY, deltax2, deltay2);
                    // ArrowPNode ar = new ArrowPNode(lastX, lastY, currentX - (deltax1 * sigX), currentY - (deltay1 *
                    // sigY));
                    ArrowPNode ar = new ArrowPNode(lastX, lastY, currentX, currentY);
                    ar.setColor(Color.RED);
                    ar.setLineWidth(15);
                    ar.setArrowHeadScale(.5f);
                    // ar.setTransparency(.45f);
                    playlistLines.addChild(ar);
                }
                lastU = u;
                lastX = currentX;
                lastY = currentY;
                // skipCount = 0;
            }
            if (skipCount > 0) {
                System.out.printf("Skipped %d Songs on this playlist%n", skipCount);
            }
            playlistDots.repaint();
            playlistLines.repaint();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

}
