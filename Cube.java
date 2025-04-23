import java.util.*;

/**
 * Rubik's Cube model that handles state management and move operations.
 */
public class Cube {
    private static final int SIZE = 3;
    private final int[][][] faces;
    private String randomSequence;

    public static final List<String> MOVES = List.of("U", "U'", "R", "R'", "F", "F'", "D", "D'", "L", "L'", "B", "B'");

    public Cube() {
        faces = new int[6][SIZE][SIZE];
        reset();
    }

    /**
     * Resets the cube to solved state.
     */
    public final void reset() {
        for (int f = 0; f < 6; f++)
            for (int i = 0; i < SIZE; i++)
                for (int j = 0; j < SIZE; j++)
                    faces[f][i][j] = f;
    }

    /**
     * Scrambles the cube with random moves.
     */
    public void scramble(int moves) {
        Random rand = new Random();
        StringBuilder sequence = new StringBuilder();
        for (int i = 0; i < moves; i++) {
            String randMove = MOVES.get(rand.nextInt(MOVES.size()));
            move(randMove);
            sequence.append(randMove).append(" ");
        }
        randomSequence = sequence.toString().trim();
    }

    /**
     * Returns the scramble sequence.
     */
    public String getMoveSequence() {
        return randomSequence;
    }

    public void move(String m){
        switch (m) {
            case "U": case "U'": case "R": case "R'": case "D": case "D'":
            case "L": case "L'": case "F": case "F'": case "B": case "B'":
                applySingleMove(m);
                break;
            case "U2": applySingleMove("U"); applySingleMove("U"); break;
            case "R2": applySingleMove("R"); applySingleMove("R"); break;
            case "F2": applySingleMove("F"); applySingleMove("F"); break;
            case "D2": applySingleMove("D"); applySingleMove("D"); break;
            case "L2": applySingleMove("L"); applySingleMove("L"); break;
            case "B2": applySingleMove("B"); applySingleMove("B"); break;
            default: throw new IllegalArgumentException("Invalid move: " + m);
        }
    }

    /**
     * Executes a single move on the cube.
     */
    private void applySingleMove(String m) {
        switch (m) {
            case "U":
                // now counterclockwise
                rotateFaceCW(0);
                // reverse cycle Back → Right → Front → Left
                cycleEdges(new int[][]{{4,0}, {2,0}, {1,0}, {5,0}}, false, false);
                break;
            case "U'":
                // now clockwise
                rotateFaceCCW(0);
                // cycle Left → Front → Right → Back
                cycleEdges(new int[][]{{4,0}, {2,0}, {1,0}, {5,0}}, false, true);
                break;

            case "R": {
                // 1) rotate the right face itself (clockwise)
                rotateFaceCW(1);

                // 2) grab all three stickers from each adjacent face
                int[] down  = new int[SIZE];  // right col of D
                int[] front = new int[SIZE];  // right col of F
                int[] up    = new int[SIZE];  // right col of U
                int[] back  = new int[SIZE];  // left col of B

                for (int i = 0; i < SIZE; i++) {
                    down[i]  = faces[3][i][2];
                    front[i] = faces[2][i][2];
                    up[i]    = faces[0][i][2];
                    back[i]  = faces[5][i][0];
                }

                // 3) cycle: Down → Front (straight)
                for (int i = 0; i < SIZE; i++) {
                    faces[2][i][2] = down[i];
                }

                // 4) cycle: Front → Up (straight)
                for (int i = 0; i < SIZE; i++) {
                    faces[0][i][2] = front[i];
                }

                // 5) cycle: Up → Back (reverse)
                for (int i = 0; i < SIZE; i++) {
                    faces[5][i][0] = up[SIZE - 1 - i];
                }

                // 6) cycle: Back → Down (reverse)
                for (int i = 0; i < SIZE; i++) {
                    faces[3][i][2] = back[SIZE - 1 - i];
                }
                break;
            }

            case "R'": {
                // 1) rotate the right face itself (counterclockwise)
                rotateFaceCCW(1);

                // 2) grab all three stickers from each adjacent face
                int[] down  = new int[SIZE];  // right col of D
                int[] front = new int[SIZE];  // right col of F
                int[] up    = new int[SIZE];  // right col of U
                int[] back  = new int[SIZE];  // left col of B

                for (int i = 0; i < SIZE; i++) {
                    down[i]  = faces[3][i][2];
                    front[i] = faces[2][i][2];
                    up[i]    = faces[0][i][2];
                    back[i]  = faces[5][i][0];
                }

                // 3) cycle: Back → Up (reverse order)
                for (int i = 0; i < SIZE; i++) {
                    faces[0][i][2] = back[SIZE - 1 - i];
                }

                // 4) cycle: Up → Front (straight)
                for (int i = 0; i < SIZE; i++) {
                    faces[2][i][2] = up[i];
                }

                // 5) cycle: Front → Down (straight)
                for (int i = 0; i < SIZE; i++) {
                    faces[3][i][2] = front[i];
                }

                // 6) cycle: Down → Back (reverse order)
                for (int i = 0; i < SIZE; i++) {
                    faces[5][i][0] = down[SIZE - 1 - i];
                }

                break;
            }


            case "F": {
                // 1) rotate the front face itself (clockwise)
                rotateFaceCW(2);

                // 2) grab all three stickers into independent buffers
                int[] up    = Arrays.copyOf(faces[0][2], SIZE);   // bottom row of U
                int[] right = new int[SIZE];                      // left col of R
                int[] down  = Arrays.copyOf(faces[3][0], SIZE);   // top row of D
                int[] left  = new int[SIZE];                      // right col of L

                for (int i = 0; i < SIZE; i++) {
                    right[i] = faces[1][i][0];
                    left[i]  = faces[4][i][2];
                }

                // 3) cycle: Left → Up (reverse order)
                for (int i = 0; i < SIZE; i++) {
                    faces[0][2][i] = left[SIZE - 1 - i];
                }

                // 4) cycle: Up → Right (straight)
                for (int i = 0; i < SIZE; i++) {
                    faces[1][i][0] = up[i];
                }

                // 5) cycle: Right → Down (reverse)
                for (int i = 0; i < SIZE; i++) {
                    faces[3][0][i] = right[SIZE - 1 - i];
                }

                // 6) cycle: Down → Left (straight)
                for (int i = 0; i < SIZE; i++) {
                    faces[4][i][2] = down[i];
                }
                break;
            }

            case "F'": {
                // 1) rotate the front face CCW
                rotateFaceCCW(2);

                // 2) buffer the four edge-strips
                int[] up    = Arrays.copyOf(faces[0][2], SIZE);   // bottom row of U
                int[] right = new int[SIZE];                      // left col of R
                int[] down  = Arrays.copyOf(faces[3][0], SIZE);   // top row of D
                int[] left  = new int[SIZE];                      // right col of L
                for (int i = 0; i < SIZE; i++) {
                    right[i] = faces[1][i][0];
                    left[i]  = faces[4][i][2];
                }

                // 3) cycle Up → Left (reverse order)
                for (int i = 0; i < SIZE; i++) {
                    faces[4][i][2] = up[SIZE - 1 - i];
                }

                // 4) cycle Left → Down (straight)
                System.arraycopy(left, 0, faces[3][0], 0, SIZE);

                // 5) cycle Down → Right (reverse)
                for (int i = 0; i < SIZE; i++) {
                    faces[1][i][0] = down[SIZE - 1 - i];
                }

                // 6) cycle Right → Up (straight)
                System.arraycopy(right, 0, faces[0][2], 0, SIZE);
                break;
            }

            case "D":
                rotateFaceCW(3);
                // reverse cycle Right → Back → Left → Front
                cycleEdges(new int[][]{{2,2}, {4,2}, {5,2}, {1,2}}, false, false);
                break;
            case "D'":
                rotateFaceCCW(3);
                cycleEdges(new int[][]{{2,2}, {4,2}, {5,2}, {1,2}}, false, true);
                break;

            case "L": {
                // 1) rotate the left face itself (clockwise)
                rotateFaceCW(4);

                // 2) grab all three stickers from each adjacent face
                int[] up    = new int[SIZE];  // left column of U
                int[] front = new int[SIZE];  // left column of F
                int[] down  = new int[SIZE];  // left column of D
                int[] back  = new int[SIZE];  // right column of B
                for (int i = 0; i < SIZE; i++) {
                    up[i]    = faces[0][i][0];
                    front[i] = faces[2][i][0];
                    down[i]  = faces[3][i][0];
                    back[i]  = faces[5][i][2];
                }

                // 3) cycle: Up → Front (straight)
                for (int i = 0; i < SIZE; i++) {
                    faces[2][i][0] = up[i];
                }

                // 4) cycle: Front → Down (straight)
                for (int i = 0; i < SIZE; i++) {
                    faces[3][i][0] = front[i];
                }

                // 5) cycle: Down → Back (reverse)
                for (int i = 0; i < SIZE; i++) {
                    faces[5][SIZE - 1 - i][2] = down[i];
                }

                // 6) cycle: Back → Up (reverse)
                for (int i = 0; i < SIZE; i++) {
                    faces[0][i][0] = back[SIZE - 1 - i];
                }
                break;
            }

            case "L'": {
                // 1) rotate the left face itself (counterclockwise)
                rotateFaceCCW(4);

                // 2) grab all three stickers from each adjacent face
                int[] up    = new int[SIZE];  // left column of U
                int[] front = new int[SIZE];  // left column of F
                int[] down  = new int[SIZE];  // left column of D
                int[] back  = new int[SIZE];  // right column of B
                for (int i = 0; i < SIZE; i++) {
                    up[i]    = faces[0][i][0];
                    front[i] = faces[2][i][0];
                    down[i]  = faces[3][i][0];
                    back[i]  = faces[5][i][2];
                }

                // 3) cycle: Front → Up (straight)
                for (int i = 0; i < SIZE; i++) {
                    faces[0][i][0] = front[i];
                }

                // 4) cycle: Down → Front (straight)
                for (int i = 0; i < SIZE; i++) {
                    faces[2][i][0] = down[i];
                }

                // 5) cycle: Back → Down (reverse)
                for (int i = 0; i < SIZE; i++) {
                    faces[3][i][0] = back[SIZE - 1 - i];
                }

                // 6) cycle: Up → Back (reverse)
                for (int i = 0; i < SIZE; i++) {
                    faces[5][SIZE - 1 - i][2] = up[i];
                }
                break;
            }


            case "B": {
                // 1) rotate the back face itself (clockwise)
                rotateFaceCW(5);

                // 2) grab all three stickers from each adjacent face
                int[] up    = Arrays.copyOf(faces[0][0], SIZE);       // top row of U
                int[] right = new int[SIZE];                          // right column of R
                int[] down  = Arrays.copyOf(faces[3][SIZE - 1], SIZE); // bottom row of D
                int[] left  = new int[SIZE];                          // left column of L
                for (int i = 0; i < SIZE; i++) {
                    right[i] = faces[1][i][SIZE - 1];
                    left[i]  = faces[4][i][0];
                }

                // 3) cycle: Right → Up (straight)
                System.arraycopy(right, 0, faces[0][0], 0, SIZE);

                // 4) cycle: Down → Right (reverse)
                for (int i = 0; i < SIZE; i++) {
                    faces[1][i][SIZE - 1] = down[SIZE - 1 - i];
                }

                // 5) cycle: Left → Down (straight)
                System.arraycopy(left, 0, faces[3][SIZE - 1], 0, SIZE);

                // 6) cycle: Up → Left (reverse)
                for (int i = 0; i < SIZE; i++) {
                    faces[4][i][0] = up[SIZE - 1 - i];
                }
                break;
            }

            case "B'": {
                // 1) rotate the back face itself (counterclockwise)
                rotateFaceCCW(5);

                // 2) grab all three stickers from each adjacent face
                int[] up    = Arrays.copyOf(faces[0][0], SIZE);
                int[] right = new int[SIZE];
                int[] down  = Arrays.copyOf(faces[3][SIZE - 1], SIZE);
                int[] left  = new int[SIZE];
                for (int i = 0; i < SIZE; i++) {
                    right[i] = faces[1][i][SIZE - 1];
                    left[i]  = faces[4][i][0];
                }

                // 3) cycle: Up → Right (straight)
                for (int i = 0; i < SIZE; i++) {
                    faces[1][i][SIZE - 1] = up[i];
                }

                // 4) cycle: Right → Down (reverse)
                for (int i = 0; i < SIZE; i++) {
                    faces[3][SIZE - 1][i] = right[SIZE - 1 - i];
                }

                // 5) cycle: Down → Left (straight)
                for (int i = 0; i < SIZE; i++) {
                    faces[4][i][0] = down[i];
                }

                // 6) cycle: Left → Up (reverse)
                for (int i = 0; i < SIZE; i++) {
                    faces[0][0][i] = left[SIZE - 1 - i];
                }
                break;
            }

            default:
                throw new IllegalArgumentException("Invalid move: " + m);
        }
    }



    /**
     * Rotates a face clockwise.
     */
    private void rotateFaceCW(int f) {
        int[][] tmp = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                tmp[j][SIZE - 1 - i] = faces[f][i][j];
        faces[f] = tmp;
    }

    /**
     * Rotates a face counterclockwise.
     */
    private void rotateFaceCCW(int f) {
        rotateFaceCW(f);
        rotateFaceCW(f);
        rotateFaceCW(f);
    }

    /**
     * Cycles edge strips between faces.
     * @param specs Array of face/index pairs defining the edges to cycle
     * @param vertical Whether to cycle columns (true) or rows (false)
     * @param reverse Direction to cycle (true for reverse)
     */
    private void cycleEdges(int[][] specs, boolean vertical, boolean reverse) {
        int len = specs.length;
        int[] buffer = new int[SIZE];

        if (reverse) {
            // Save last strip
            int lf = specs[len - 1][0], li = specs[len - 1][1];
            for (int i = 0; i < SIZE; i++)
                buffer[i] = vertical ? faces[lf][i][li] : faces[lf][li][i];
            // Shift backwards
            for (int s = len - 1; s > 0; s--) {
                int ff = specs[s][0], fi = specs[s][1];
                int pf = specs[s - 1][0], pi = specs[s - 1][1];
                for (int i = 0; i < SIZE; i++) {
                    if (vertical) faces[ff][i][fi] = faces[pf][i][pi];
                    else faces[ff][fi][i] = faces[pf][pi][i];
                }
            }
            // Restore first strip
            int f0 = specs[0][0], i0 = specs[0][1];
            for (int i = 0; i < SIZE; i++) {
                if (vertical) faces[f0][i][i0] = buffer[i];
                else faces[f0][i0][i] = buffer[i];
            }
        } else {
            // Save first strip
            int f0 = specs[0][0], i0 = specs[0][1];
            for (int i = 0; i < SIZE; i++)
                buffer[i] = vertical ? faces[f0][i][i0] : faces[f0][i0][i];
            // Shift forwards
            for (int s = 0; s < len - 1; s++) {
                int ff = specs[s][0], fi = specs[s][1];
                int nf = specs[s + 1][0], ni = specs[s + 1][1];
                for (int i = 0; i < SIZE; i++) {
                    if (vertical) faces[ff][i][fi] = faces[nf][i][ni];
                    else faces[ff][fi][i] = faces[nf][ni][i];
                }
            }
            // Restore last strip
            int lf = specs[len - 1][0], li = specs[len - 1][1];
            for (int i = 0; i < SIZE; i++) {
                if (vertical) faces[lf][i][li] = buffer[i];
                else faces[lf][li][i] = buffer[i];
            }
        }
    }

    /**
     * Checks if the cube is in a solved state.
     */
    public boolean isSolved() {
        for (int f = 0; f < 6; f++) {
            int color = faces[f][0][0];
            for (int i = 0; i < SIZE; i++)
                for (int j = 0; j < SIZE; j++)
                    if (faces[f][i][j] != color) return false;
        }
        return true;
    }

    /**
     * Returns the current state of the cube.
     */
    public int[][][] getFaces() {
        return faces;
    }

    /**
     * Sets the cube to a specific configuration.
     */
    public void setFaces(int[][][] newFaces) {
        if (newFaces.length != 6) {
            throw new IllegalArgumentException("Must provide exactly 6 faces");
        }
        
        for (int f = 0; f < 6; f++) {
            if (newFaces[f].length != SIZE) {
                throw new IllegalArgumentException("Each face must be " + SIZE + "×" + SIZE);
            }
            for (int i = 0; i < SIZE; i++) {
                if (newFaces[f][i].length != SIZE) {
                    throw new IllegalArgumentException("Each face must be " + SIZE + "×" + SIZE);
                }
                System.arraycopy(newFaces[f][i], 0, faces[f][i], 0, SIZE);
            }
        }
    }

    /**
     * Converts a flattened cube representation to the 3D array format.
     * The flattened format is assumed to be a 2D array with the following layout:
     *     [U]
     *  [L][F][R][B]
     *     [D]
     *
     * @param flattenedCube 2D array representing the flattened cube
     * @return 3D array with faces ordered as U, R, F, L, D, B
     */
    public static int[][][] convertFlattenedTo3D(int[][] flattenedCube) {
        int[][][] result = new int[6][SIZE][SIZE];

        // Convert and rearrange faces according to the required order: U, R, F, L, D, B

        // Process Up face (U)
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(flattenedCube[i], 3, result[0][i], 0, SIZE);
        }

        // Process Right face (R)
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(flattenedCube[SIZE + i], 6, result[1][i], 0, SIZE);
        }

        // Process Front face (F)
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(flattenedCube[SIZE + i], 3, result[2][i], 0, SIZE);
        }

        // Process Left face (L)
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(flattenedCube[SIZE + i], 0, result[4][i], 0, SIZE);
        }

        // Process Down face (D)
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(flattenedCube[2 * SIZE + i], 3, result[3][i], 0, SIZE);
        }

        // Process Back face (B)
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(flattenedCube[SIZE + i], 9, result[5][i], 0, SIZE);
        }

        return result;
    }

    private int[][][] deepCopyFaces() {
        int[][][] copy = new int[6][SIZE][SIZE];
        for (int f = 0; f < 6; f++)
            for (int i = 0; i < SIZE; i++)
                System.arraycopy(faces[f][i], 0, copy[f][i], 0, SIZE);
        return copy;
    }




    /**
     * Converts the cube state to a 54-character string using facelet letters.
     */
    public String getStateString() {
        int[][][] cube3D = deepCopyFaces();
        Map<Integer, Character> faceletMap = new HashMap<>();
        faceletMap.put(0, 'U');
        faceletMap.put(1, 'R');
        faceletMap.put(2, 'F');
        faceletMap.put(3, 'D');
        faceletMap.put(4, 'L');
        faceletMap.put(5, 'B');
        StringBuilder state = new StringBuilder();

        for (int[][] face : cube3D) {
            for (int[] row : face) {
                for (int piece : row) {
                    state.append(faceletMap.get(piece));
                }
            }
        }
        return state.toString().trim();
    }
}