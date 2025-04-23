import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javax.swing.*;

/**
 * GUI visualizer for the Rubik's Cube with manual controls.
 */
public class CubeVisualizer {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Rubik's Cube");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            Cube cube = new Cube();
            int scrambleCount = 20; // Number of moves to scramble

            // Drawing panel for cube visualization
            JPanel drawPanel = new JPanel() {
                private static final int SIZE = 50;
                private static final int FACE_SIZE = 3;
                private final int[][] layout = {{1,0},{2,1},{1,1},{1,2},{0,1},{3,1}};

                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    int[][][] faces = cube.getFaces();
                    for (int f = 0; f < faces.length; f++) {
                        int x0 = layout[f][0] * SIZE * FACE_SIZE;
                        int y0 = layout[f][1] * SIZE * FACE_SIZE;
                        for (int i = 0; i < FACE_SIZE; i++) {
                            for (int j = 0; j < FACE_SIZE; j++) {
                                g.setColor(new Color[]{Color.WHITE,Color.RED,Color.GREEN,Color.YELLOW,Color.ORANGE,Color.BLUE}[faces[f][i][j]]);
                                g.fillRect(x0 + j*SIZE, y0 + i*SIZE, SIZE, SIZE);
                                g.setColor(Color.BLACK);
                                g.drawRect(x0 + j*SIZE, y0 + i*SIZE, SIZE, SIZE);
                            }
                        }
                    }
                }
            };
            drawPanel.setPreferredSize(new Dimension(600,600));

            // Status display
            JLabel status = new JLabel("Last Move: none");

            // Control buttons for cube manipulation
            JPanel controlPanel = new JPanel(new FlowLayout());
            for (String mv : Cube.MOVES) {
                JButton btn = new JButton(mv);
                btn.addActionListener(e -> {
                    cube.move(mv);
                    status.setText("Last Move: " + mv + (cube.isSolved() ? " | SOLVED!" : ""));
                    drawPanel.repaint();
                });
                controlPanel.add(btn);
            }
            
            // Reset button - returns cube to solved state
            JButton resetBtn = new JButton("Reset");
            resetBtn.addActionListener(e -> {
                cube.reset();
                status.setText("Last Move: reset");
                drawPanel.repaint();
            });
            
            // Scramble button - randomizes cube state
            JButton scrambleBtn = new JButton("Scramble");
            scrambleBtn.addActionListener(e -> {
                cube.reset();
                cube.scramble(scrambleCount);
                status.setText("Last Move: scramble");
                drawPanel.repaint();
            });
            controlPanel.add(resetBtn);
            controlPanel.add(scrambleBtn);
            
            // Panel for cube state input and solution display
            JPanel statePanel = new JPanel(new BorderLayout(5, 5));
            statePanel.setBorder(BorderFactory.createTitledBorder("Cube State and Solution"));
            
            // Color mapping legend for cube faces
            JPanel mappingPanel = new JPanel(new GridLayout(2, 3, 5, 5));
            String[] faceLabels = {"0: Up (White)", "1: Right (Red)", "2: Front (Green)", 
                                    "3: Down (Yellow)", "4: Left (Orange)", "5: Back (Blue)"};
            for (String label : faceLabels) {
                JLabel faceMappingLabel = new JLabel(label, JLabel.CENTER);
                faceMappingLabel.setBorder(BorderFactory.createEtchedBorder());
                mappingPanel.add(faceMappingLabel);
            }
            
            // Manual cube state input area
            JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
            JTextField stateInput = new JTextField();
            stateInput.setToolTipText("Enter cube state as comma-separated numbers (54 values, 0-5)");
            JButton applyStateBtn = new JButton("Apply State");
            inputPanel.add(new JLabel("Cube State:"), BorderLayout.WEST);
            inputPanel.add(stateInput, BorderLayout.CENTER);
            inputPanel.add(applyStateBtn, BorderLayout.EAST);
            
            // Solution display area
            JPanel solutionPanel = new JPanel(new BorderLayout(5, 5));
            JTextArea solutionArea = new JTextArea(3, 20);
            solutionArea.setEditable(false);
            solutionPanel.add(new JLabel("Solution:"), BorderLayout.NORTH);
            solutionPanel.add(new JScrollPane(solutionArea), BorderLayout.CENTER);
            
            // Solve button - connects to external solver
            JButton solveBtn = new JButton("Solve");
            
            // Function to apply custom cube state from text input
            applyStateBtn.addActionListener(e -> {
                try {
                    String[] values = stateInput.getText().split(",");
                    if (values.length != 54) {
                        throw new IllegalArgumentException("Exactly 54 values required (9 per face)");
                    }
                    
                    int[][][] newState = new int[6][3][3];
                    int index = 0;
                    
                    // Parse input into cube representation
                    for (int f = 0; f < 6; f++) {
                        for (int i = 0; i < 3; i++) {
                            for (int j = 0; j < 3; j++) {
                                newState[f][i][j] = Integer.parseInt(values[index++].trim());
                                if (newState[f][i][j] < 0 || newState[f][i][j] > 5) {
                                    throw new IllegalArgumentException("Values must be between 0-5");
                                }
                            }
                        }
                    }
                    
                    // Apply to cube
                    cube.setFaces(newState);
                    status.setText("Applied custom cube state");
                    drawPanel.repaint();
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(frame, 
                        "Invalid input format: " + ex.getMessage(), 
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            // Function to connect to solver server
            solveBtn.addActionListener(e -> {
                status.setText("Requesting solution...");
                solutionArea.setText("Working...");
                drawPanel.repaint();
                String cubeStateString = cube.getStateString();

                // fire off the HTTP call in a background thread so the UI stays responsive
                new Thread(() -> {
                    try {
                        // assume you already have:
                        // String cubeStateString = ...;  // your 54-char or CSV state string

                        String apiUrl = "https://cuby-solve-api.onrender.com/solve/" + cubeStateString;

                        HttpClient client = HttpClient.newHttpClient();
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(apiUrl))
                                .GET()
                                .build();

                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        String solution = response.body();

                        // back onto the EDT to update Swing components
                        SwingUtilities.invokeLater(() -> {
                            status.setText("Solution retrieved");
                            solutionArea.setText(solution);
                        });

                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> {
                            status.setText("Error getting solution");
                            solutionArea.setText("Error: " + ex.getMessage());
                            JOptionPane.showMessageDialog(frame,
                                    "Failed to connect to API:\n" + ex.getMessage(),
                                    "Connection Error", JOptionPane.ERROR_MESSAGE);
                        });
                    }
                }).start();
            });


            // Add solve button to control panel
            controlPanel.add(solveBtn);
            
            // Panel organization for UI layout
            // Main cube visualization with input field below
            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.add(drawPanel, BorderLayout.CENTER);
            centerPanel.add(inputPanel, BorderLayout.SOUTH);
            
            // Configure state panel for legend and solution display
            statePanel.removeAll();
            statePanel.add(mappingPanel, BorderLayout.NORTH);
            statePanel.add(solutionPanel, BorderLayout.CENTER);
            
            // Assemble main interface components
            JPanel leftPanel = new JPanel(new BorderLayout());
            leftPanel.add(centerPanel, BorderLayout.CENTER);
            leftPanel.add(controlPanel, BorderLayout.SOUTH);
            
            // Final application layout
            frame.setLayout(new BorderLayout());
            frame.add(leftPanel, BorderLayout.CENTER);
            frame.add(status, BorderLayout.NORTH);
            frame.add(statePanel, BorderLayout.EAST);
            
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}