import javafx.geometry.Pos;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;

//the word element will be used instead of M or T, which is smaller of the two
//weight is the amount of element in one possibility
//should be noted that the right and lower border are non inclusive
public class Solver {
    private char thePizza [][];
    private boolean clearCell [][];
    private Possibility areasUsed [][]; //this is a 2d matrix of all the pizza and has pointers to the areas used
    private ArrayList<Point> smallerLocs;
    private ArrayList<Point> possibleArrangements;
    private ArrayList<Possibility> slices;
    private char smaller;
    private int rows;
    private int columns;
    private int maxsize;
    private int minPerSlice;

    public Solver(String fileName){
        possibleArrangements = new ArrayList<Point>();
        slices = new ArrayList<Possibility>();
        calculate(fileName);
        calculateAllPossibleArrangements();
    }

    public String solveIt(){
        ArrayList<Possibility> solutions = new ArrayList<Possibility>();
        int x,y;
        Possibility calculatedPossibilities = null;
        while(smallerLocs.size() > 0) {
            x = (int) smallerLocs.get(0).getX();
            y = (int) smallerLocs.get(0).getY();

            //Before it is called it should be checked if the element is already taken
            if(clearCell[y][x]){
                calculatedPossibilities = getTheBestPossibility(x,y);
            }

            if(calculatedPossibilities != null){
                solutions.add(calculatedPossibilities);

                //mark the area of the possibility as taken
                for(int i = calculatedPossibilities.getLeft(); i < calculatedPossibilities.getRight();i++){
                    for(int j = calculatedPossibilities.getUp(); j < calculatedPossibilities.getDown(); j++){
                        clearCell[j][i] = false;
                        areasUsed[j][i] = calculatedPossibilities;
                    }
                }
                calculatedPossibilities = null;
            }

            //remove the one since we are done with it
            smallerLocs.remove(0);

        }
        //at this point we have an array full of possibilities now we need to expand
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < columns; j++){
                if(clearCell[i][j]){
                    expand(j,i);
                }
            }
        }
        String solved = solutions.size() + "\n";

        for(int i = 0;i < solutions.size(); i++){
            solved += solutions.get(i).toString();

        }

        return solved;
    }

    //this fcn will expand around one point
    //it will find the four closes neighbours of the point not taken
    //namely the one up, down, left and right
    //it will expand the one which will take the desired cell and that
    //will be the smallest as we want to allow others to grow, however if two
    //have the same amount it will take the one with more possibility to grow
    private void expand(int x, int y){
        Possibility up = null, down = null, left = null, right = null;
        int leftInc = 0, rightInc = 0, upInc = 0, downInc = 0;
        int leftEXP[] = {0,0,0,0,0};
        int rightEXP[] = {0,0,0,0,0};
        int upEXP[] = {0,0,0,0,0};
        int downEXP[] = {0,0,0,0,0};

        //finding the four closest neighbours
        for(int i = x; i >= 0; i--){
            if(!clearCell[y][i]){
                left = areasUsed[y][i];
                break;
            }
        }
        for(int i = y; i >= 0; i--){
            if(!clearCell[i][x]){
                up = areasUsed[i][x];
                break;
            }
        }
        for(int i = x; i < columns; i++){
            if(!clearCell[y][i]){
                right = areasUsed[y][i];
                break;
            }
        }
        for(int i = y; i < rows; i++){
            if(!clearCell[i][x]){
                down = areasUsed[i][x];
                break;
            }
        }

        int incValues [] = {-1,-1,-1,-1};
        //the results from expanding amount will be used on basis with the location on where
        //the neighbour stands in respect with the point to cover
        if(left != null && left.canExpand()){
            leftEXP = left.expandingCapabilities(x,y);
            leftInc = leftEXP[4];
            if(leftInc > 0){
                if(!checkIfFree(left.getRight(),left.getRight()+leftEXP[1],left.getUp() - leftEXP[2],left.getDown() + leftEXP[3])){
                    leftInc = - 1;
                }
                incValues[0] = leftInc;
            }

        }

        //this checks if the size increase is correct and there is enough space to do the desired increase
        if(right != null && right.canExpand()){
           rightEXP = right.expandingCapabilities(x,y);
           rightInc = rightEXP[4];
            if(rightInc > 0){
                if(!checkIfFree(right.getLeft()-rightEXP[1],right.getLeft()-1,left.getUp() - rightEXP[2],left.getDown() + rightEXP[3])){
                    rightInc = - 1;
                }
                incValues[1] = rightInc;
            }

        }
        if(up != null && up.canExpand()){
            upEXP = up.expandingCapabilities(x,y);
            upInc = upEXP[4];
            if(upInc > 0){
                if(!checkIfFree(up.getLeft() - upEXP[0],up.getRight()+upEXP[1],up.getDown(),up.getDown() + upEXP[3])){
                    upInc = - 1;
                }
                incValues[2] = upInc;
            }
        }
        if(down != null && down.canExpand()){
            downEXP = down.expandingCapabilities(x,y);
            downInc = downEXP[4];
            if(downInc > 0){
                if(!checkIfFree(down.getLeft() - downEXP[0],down.getRight()+downEXP[1],down.getUp() - downEXP[2],down.getUp() - 1)){
                    downInc = - 1;
                }
                incValues[3] = downInc;
            }
        }
        //the increase of each of them is calculated

        //find out which one is the smallest direction
        char smallestDirection = 'o';
        int smallestInc = -1;
        for(int i = 0; i < incValues.length; i++){
            if(incValues[i] > 0){
                if(smallestInc == -1){
                    smallestInc = i;
                }
                else if(incValues[i] < incValues[smallestInc]){
                    smallestInc = i;
                }
            }
        }
        if(smallestInc == 0){
            smallestDirection = 'L';
            if(checkIfFree(left.getLeft(),left.getRight() + leftEXP[1], left.getUp(),left.getDown())) {
                left.expand(leftEXP[1], smallestDirection);
                coverAreaTaken(left);
            }
        }
        else if(smallestInc == 1){
            smallestDirection = 'R';
            if(checkIfFree(right.getLeft() - rightEXP[0],right.getRight(), right.getUp(),right.getDown())) {
                right.expand(rightEXP[0], smallestDirection);
                coverAreaTaken(right);
            }
        }
        else if(smallestInc == 2){
            smallestDirection = 'U';
            if(checkIfFree(up.getLeft(),up.getRight(), up.getUp(),up.getDown() + upEXP[3])) {
                up.expand(upEXP[3], smallestDirection);
                coverAreaTaken(up);
            }
        }
        else if(smallestInc == 3){
            smallestDirection = 'D';

                if (checkIfFree(down.getLeft(), down.getRight(), down.getUp() - downEXP[2], down.getDown())) {
                    down.expand(downEXP[2], smallestDirection);
                    coverAreaTaken(down);
                }

        }
        //smallest direction found and expanded to that direction
        //areas marked as covered

    }

    //this fcn makes all the cells within the slice as taken and used
    //and puts the pointer appropriately to the possibility
    private void coverAreaTaken(Possibility p){
        for(int i = p.getLeft(); i < p.getRight();i++){
            for(int j = p.getUp(); j < p.getDown(); j++){
                clearCell[j][i] = false;
                areasUsed[j][i] = p;
            }
        }
    }

    //this fcn calculates all the possible solutions for the smaller (M or T) within the given coordinates
    private Possibility getTheBestPossibility(int locx, int locy){
        int x,y,left,right,up,down;
        ArrayList<Possibility> possibilities = new ArrayList<Possibility>();

        //go through all the possible arrangements of the desired element being in all places
        for(int arrangements = 0; arrangements < possibleArrangements.size(); arrangements++){
            x = (int) possibleArrangements.get(arrangements).getX();
            y = (int) possibleArrangements.get(arrangements).getY();
            for(int i = 0; i <= x ; i++){ //not sure about the equalities
                for(int j = 0; j <= y; j++){
                    left = locx - i;
                    if(left < 0){
                        continue;
                    }

                    right = locx + x -i;//we add one because we don't include the right side
                    if(right > columns){
                        continue;
                    }
                    up = locy - j;
                    if(up < 0){
                        continue;
                    }
                    down = locy + y - j;
                    if(down > rows){
                        continue;
                    }
                    if(checkIfFree(left,right,up,down) && includesPoint(locx,locy,left,right,up,down) && isMinimalySatisfactory(left,right,up,down)){
                        possibilities.add(new Possibility(left,right,up,down,thePizza,smaller,maxsize));
                    }
                }
            }
        }
        //after this point all the possible arrangements are calculated

        //the minimum weight is found
        int minimumWeight = 0;
        for(int i = 1 ; i < possibilities.size(); i++){
            if(possibilities.get(minimumWeight).getWeight() > possibilities.get(i).getWeight()){
                minimumWeight = i;
            }
        }

        if(possibilities.size() != 0) {
            //those with more weight are removed
            removeObese(possibilities, possibilities.get(minimumWeight).getWeight());
        }

        //check if there are possibilities in the corners
        if(isCornerPossibilityPresent(possibilities)){
            removeNonCornerPossibilities(possibilities);
        }

        //searching for the biggest slice
        int maxSize = 0;
        for(int i = 1; i < possibilities.size(); i++){
            if(possibilities.get(i).getSize() > possibilities.get(maxSize).getSize()){
                maxSize = i;
            }
        }
        if(possibilities.size() <= 0){
            return null;
        }
        return possibilities.get(maxSize);

    }
    //fcn that checks if the area designated fullfills the desired minimum
    private boolean isMinimalySatisfactory(int left, int right, int up, int down){
        int tomatoes = 0;
        int mushrooms = 0;
        for(int i = up; i < down; i++){
            for(int j = left; j < right; j++){
                if(thePizza[i][j] == 'M'){
                    mushrooms++;
                }else {
                    tomatoes++;
                }
            }
        }
        if((mushrooms >= minPerSlice) && (tomatoes>= minPerSlice)){
            return true;
        }
        return false;
    }
    //fcn that checks if the point given is within the boundaries set
    private boolean includesPoint(int x, int y, int left, int right, int up, int down){
        return (x >= left) && (x < right) && (y >= up) && (y < down);
    }
    private void removeNonCornerPossibilities(ArrayList<Possibility> possibilities){
        for(int i = 0; i < possibilities.size(); i++){
            if(!possibilities.get(i).isCorner()){
                possibilities.remove(i);
                i = i - 1;
            }
        }
    }

    //the idea is to have as little elements as possible in a slice such that more slices can be done
    //weight represents the amount of elements in a possibility
    private void removeObese(ArrayList<Possibility> possibilities, int weight){
        for(int i = 0; i < possibilities.size(); i++){
            if(possibilities.get(i).getWeight() > weight){
                possibilities.remove(i);
                i = i - 1;
            }
        }
    }

    private boolean isCornerPossibilityPresent(ArrayList<Possibility> possibilities){
        for(int i = 0 ; i < possibilities.size(); i++){
            if(possibilities.get(i).isCorner()){
                return true;
            }
        }
        return false;
    }

    //checks if the desired area is free (not taken by another chosen possibility)
    private boolean checkIfFree(int leftBorder, int rightBorder, int upperBorder, int lowerBorder){
        for(int i = leftBorder; i < rightBorder; i++){
            for(int j = upperBorder; j < lowerBorder; j++){
                if(!clearCell[j][i]){
                    return false;
                }
            }
        }
        return true;
    }

    //this calculates all the possible dimensions of the selection areas that can be made
    private void calculateAllPossibleArrangements(){
        //the start is twice the minimum since it has to have at least minimum mushrooms and minimum tomatoes
        //it is then increased by one as all bigger values represent an opportunity
        int minimum = minPerSlice * 2;
        for(int i = 1 ; i <= maxsize ; i++){
            for(int j = 1; j <= maxsize; j++){
                if((i * j <= maxsize) && (i * j >= minimum)){
                    possibleArrangements.add(new Point(i ,j));
                }
            }
        }
    }


    //this will fill the pizza array with the needed characters
    //this will also fill the tomatoes and mushrooms array lists with their locations
    //set the smaller
    //also get the row,column and other variables set
    private void calculate(String filename) {

        ArrayList<Point> tomatoes = new ArrayList<Point>();
        ArrayList<Point> mushrooms = new ArrayList<Point>();
        File file = new File(filename);
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));
            String text = null;

            text = reader.readLine();
            int i,j = 0;
            for(i = 0; i < text.length(); i++ ){
                if(text.charAt(i) == 32){
                    rows = Integer.parseInt(text.substring(0, i));
                    j  = ++i;
                    break;
                }
            }

            for(; i < text.length(); i++){
                if(text.charAt(i) == 32){
                    columns = Integer.parseInt(text.substring(j, i));
                    j  = ++i;
                    break;
                }
            }

            for(; i < text.length(); i++){
                if(text.charAt(i) == 32){
                    minPerSlice = Integer.parseInt(text.substring(j, i));
                    j  = ++i;
                    break;
                }
            }


            maxsize = Integer.parseInt(text.substring(j, text.length()));



            thePizza = new char[rows][columns];
            clearCell = new boolean[rows] [columns];
            areasUsed = new Possibility[rows][columns];
            for(i = 0; i  < rows; i++){
                for(j = 0; j < columns ; j++){
                    clearCell[i][j] = true;
                    areasUsed[i][j] = null;
                }
            }
            for(i = 0; i < rows; i++){
                text = reader.readLine();
                for(j = 0; j < columns;j++){
                    thePizza[i][j] = text.charAt(j);
                    if (thePizza[i][j] == 'M') {
                        mushrooms.add(new Point(j,i));
                    }
                    else{
                        tomatoes.add(new Point(j,i));
                    }
                }

            }

            if(tomatoes.size() < mushrooms.size()){
                smaller = 'T';
                smallerLocs = tomatoes;
            }else{
                smaller = 'M';
                smallerLocs = mushrooms;
            }
            //possible memory optimization:
            //if the bigger array list with locations is not needed it can be set to null
            //so that the garbage collector removes it and frees up memory


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }

        }
    }
}
